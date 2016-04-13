package cs499.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import blackboard.data.course.Course;
import blackboard.data.course.CourseMembership;
import blackboard.data.user.User;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.platform.gradebook2.AttemptDetail;
import blackboard.platform.gradebook2.GradableItem;
import cs499.controllers.BlackboardHandler;
import cs499.controllers.JSUBbDatabase;
import cs499.controllers.MarketPlaceDAO;
import cs499.object.CommunityItem;
import cs499.object.Item;

/**
 * The Class BlackboardHandlerTest.
 */
public class BlackboardHandlerTest {
	
	/** The bb handler. */
	private BlackboardHandler bbHandler;
	
	/** The market place dao. */
	private MarketPlaceDAO marketPlaceDao;
	
	/** The course id. */
	private Id courseID;
	
	/** The session user. */
	private User sessionUser;
	
	/**
	 * Gets the connection.
	 *
	 * @return the connection
	 * @throws Exception the exception
	 */
	protected IDatabaseConnection getConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		Connection jdbcConnection = JSUBbDatabase.getConnection(true);
		return new DatabaseConnection(jdbcConnection);
	}
	
	/**
	 * Gets the data set.
	 *
	 * @return the data set
	 * @throws Exception the exception
	 */
	protected IDataSet getDataSet() throws Exception {
		return new FlatXmlDataSet(new FileInputStream("./resources/dbUnitDataSet.xml"));
	}
	
	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception
    {
        IDatabaseConnection connection = getConnection();
        courseID = Id.newId(Course.DATA_TYPE);
        updateDatabaseInfo();
        IDataSet dataSet = getDataSet();
        marketPlaceDao = new MarketPlaceDAO(true, courseID.toExternalString());
        marketPlaceDao.emptyDatabase();
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        connection.close();
        sessionUser = new User();
		sessionUser.setFamilyName("LAST");
		sessionUser.setGivenName("FIRST");
		sessionUser.setId(Id.newId(User.DATA_TYPE));
		sessionUser.setUserName("username");
		sessionUser.setStudentId("00111");
		bbHandler = new BlackboardHandler(courseID, sessionUser, marketPlaceDao.loadItems());
    }

	/**
	 * Test constructor add gold column.
	 *
	 * @throws PersistenceException the persistence exception
	 */
	@Test
	public void testConstructorAddGoldColumn() throws PersistenceException{
		assertEquals(0, bbHandler.getGradableItemList().size());
		bbHandler.testingConstructor(null, true);
		assertEquals("Gold", bbHandler.getGradableItemList().get(0).getTitle());
	}
	
	/**
	 * Test add gold column.
	 *
	 * @throws PersistenceException the persistence exception
	 */
	@Test
	public void testAddGoldColumn() throws PersistenceException{
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("Test");
		bbHandler.addGradableItem(gradableItem);
		assertEquals(1, bbHandler.getGradableItemList().size());
	}
	
	/**
	 * Test set students without gold column.
	 *
	 * @throws PersistenceException the persistence exception
	 */
	@Test
	public void testSetStudentsWithoutGoldColumn() throws PersistenceException{
		List<CourseMembership> memberList = new ArrayList<CourseMembership>();
		CourseMembership student = new CourseMembership();
		student.setCourseId(courseID);
		student.setId(Id.newId(CourseMembership.DATA_TYPE));
		User user = new User();
		user.setFamilyName("LAST");
		user.setGivenName("FIRST");
		user.setStudentId("00111");
		user.setUserName("username");
		student.setUser(user);
		memberList.add(student);
		bbHandler.testingConstructor(memberList.iterator(), false);
		assertEquals(0, bbHandler.getStudent().getGold());
	}
	
	/**
	 * Test set students with gold column.
	 *
	 * @throws PersistenceException the persistence exception
	 */
	@Test
	public void testSetStudentsWithGoldColumn() throws PersistenceException{
		createStudents("00111");
		assertEquals(1000, bbHandler.getStudent().getGold());
	}
	
	/**
	 * Test set students with items.
	 *
	 * @throws PersistenceException the persistence exception
	 */
	@Test
	public void testSetStudentsWithItems() throws PersistenceException{
		Item item = new Item("Once");
		marketPlaceDao.persistPurhcase("00111", item);
		createStudents("00111");
		assertEquals(1000, bbHandler.getStudent().getGold());
	}
	
	/**
	 * Test process item if instructor.
	 */
	@Test
	public void testProcessItemIfInstructor(){
		bbHandler.processItem("Once");
	}
	
	/**
	 * Test process item if student can afford.
	 */
	@Test
	public void testProcessItemIfStudentCanAfford(){
		createStudents("00111");
		assertEquals(0, bbHandler.getStudent().getItemList().size());
		bbHandler.processItem("Once");
		assertEquals("Once", bbHandler.getStudent().getItemList().get(0).getName());
	}

	/**
	 * Test process item if student can not afford.
	 */
	@Test
	public void testProcessItemIfStudentCanNotAfford(){
		createStudents("00111");
		bbHandler.getStudent().setGold(0);
		bbHandler.processItem("Once");
		assertEquals(0, bbHandler.getStudent().getItemList().size());
	}
	
	/**
	 * Test process item if out of supply.
	 */
	@Test
	public void testProcessItemIfOutOfSupply(){
		createStudents("00111");
		bbHandler.processItem("Once");
		bbHandler.getStudent().setGold(1000);
		bbHandler.processItem("Once");
		assertEquals(1, bbHandler.getStudent().getItemList().size());
	}
	
	/**
	 * Test not gradable item in process item.
	 */
	@Test
	public void testNotGradableItemInProcessItem(){
		List<CourseMembership> memberList = new ArrayList<CourseMembership>();
		CourseMembership student = new CourseMembership();
		student.setCourseId(courseID);
		student.setId(Id.newId(CourseMembership.DATA_TYPE));
		User user = new User();
		user.setId(sessionUser.getId());
		user.setFamilyName("LAST");
		user.setGivenName("FIRST");
		user.setStudentId("00111");
		user.setUserName("username");
		student.setUser(user);
		memberList.add(student);
		try {
			bbHandler.testingConstructor(memberList.iterator(), false);
			bbHandler.getStudent().setGold(1000);
			bbHandler.processItem("Once");
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test no gold gradable item in process item.
	 */
	@Test
	public void testNoGoldGradableItemInProcessItem(){
		List<CourseMembership> memberList = new ArrayList<CourseMembership>();
		CourseMembership student = new CourseMembership();
		student.setCourseId(courseID);
		student.setId(Id.newId(CourseMembership.DATA_TYPE));
		User user = new User();
		user.setId(sessionUser.getId());
		user.setFamilyName("LAST");
		user.setGivenName("FIRST");
		user.setStudentId("00111");
		user.setUserName("username");
		student.setUser(user);
		memberList.add(student);
		try {
			bbHandler.testingConstructor(memberList.iterator(), false);
			GradableItem gradableItem = new GradableItem();
			gradableItem.setTitle("Test");
			bbHandler.addGradableItem(gradableItem);
			bbHandler.getStudent().setGold(1000);
			bbHandler.processItem("Once");
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test use due date item with no due date columns.
	 */
	@Test
	public void testUseDueDateItemWithNoDueDateColumns(){
		createStudents("00111");
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("Test");
		bbHandler.addGradableItem(gradableItem);
		Item item = marketPlaceDao.loadItem("Once");
		bbHandler.useItem(item, "TEST");
		assertEquals(false, bbHandler.getGradableItemList().get(1).isDueDateSet());
	}
	
	/**
	 * Test instant due date item with due date columns.
	 */
	@Test
	public void testInstantDueDateItemWithDueDateColumns(){
		createStudents("00111");
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("Test");
		Calendar cal = Calendar.getInstance();
		gradableItem.setDueDate(cal);
		bbHandler.addGradableItem(gradableItem);
		Item item = marketPlaceDao.loadItem("Once");
		bbHandler.useItem(item, "Test");
		cal.add(Calendar.HOUR, 24);
		assertEquals(cal, bbHandler.getGradableItemList().get(1).getDueDate());
	}

	/**
	 * Test instant num attempt item with unlimited attempts.
	 */
	@Test
	public void testInstantNumAttemptItemWithUnlimitedAttempts(){
		createStudents("00111");
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("Test");
		assertEquals(0, gradableItem.getMaxAttempts());
		bbHandler.addGradableItem(gradableItem);
		Item item = marketPlaceDao.loadItem("OnceTwo");
		bbHandler.useItem(item, "Test");
		assertEquals(true, gradableItem.isAllowUnlimitedAttempts());
	}
	
	/**
	 * Test instant num attempt item.
	 */
	@Test
	public void testInstantNumAttemptItem(){
		createStudents("00111");
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("Test");
		gradableItem.setMaxAttempts(2);
		bbHandler.addGradableItem(gradableItem);
		Item item = marketPlaceDao.loadItem("OnceTwo");
		marketPlaceDao.persistPurhcase("00111", item);
		bbHandler.useItem(item, "Test");
		assertEquals(4, gradableItem.getMaxAttempts());
	}
	
	/**
	 * Test continuous grade item first insert.
	 */
	@Test
	public void testContinuousGradeItemFirstInsert(){
		createStudents("00111");
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("TEST");
		bbHandler.addGradableItem(gradableItem);
		Item item = marketPlaceDao.loadItem("Continuous");
		bbHandler.processItem("Continuous");
		bbHandler.useItem(item, "TEST");
		createStudents("00111");
		assertEquals("TEST" ,marketPlaceDao.getGradebookColumnByNameAndStudentId("TEST", "00111").getName());
		assertEquals(120 ,marketPlaceDao.getGradebookColumnByNameAndStudentId("TEST", "00111").getGrade());
	}
	
	/**
	 * Test continuous grade item second run when date is not before.
	 */
	@Test
	public void testContinuousGradeItemSecondRunWhenDateIsNotBefore(){
		createStudents("00111");
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("TEST");
		bbHandler.addGradableItem(gradableItem);
		Item item = marketPlaceDao.loadItem("Continuous");
		bbHandler.processItem("Continuous");
		bbHandler.useItem(item, "TEST");
		createStudents("00111");
		createStudents("00111");
		assertEquals("TEST" ,marketPlaceDao.getGradebookColumnByNameAndStudentId("TEST", "00111").getName());
		assertEquals(120 ,marketPlaceDao.getGradebookColumnByNameAndStudentId("TEST", "00111").getGrade());
	}
	
	/**
	 * Test instant item with update columns.
	 */
	@Test
	public void testInstantItemWithUpdateColumns(){
		createStudents("00111");
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("TEST");
		bbHandler.addGradableItem(gradableItem);
		bbHandler.processItem("Once");
		createStudents("00111");
		assertEquals(null ,marketPlaceDao.getGradebookColumnByNameAndStudentId("TEST", "00111"));
	}
	
	/**
	 * Test not activated continuous item with update columns.
	 */
	@Test
	public void testNotActivatedContinuousItemWithUpdateColumns(){
		createStudents("00111");
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("TEST");
		bbHandler.addGradableItem(gradableItem);
		bbHandler.processItem("Continuous");
		createStudents("00111");
		assertEquals(null ,marketPlaceDao.getGradebookColumnByNameAndStudentId("TEST", "00111"));
	}
	
	/**
	 * Test continuous item with update columns when item expired and pending.
	 */
	@Test
	public void testContinuousItemWithUpdateColumnsWhenItemExpiredAndPending(){
		createStudents("00111");
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("TEST");
		bbHandler.addGradableItem(gradableItem);
		Item item = marketPlaceDao.loadItem("Continuous");
		bbHandler.processItem("Continuous");
		bbHandler.useItem(item, "TEST");
		createStudents("00111");
		marketPlaceDao.editItemUseInfoExpDate("00111", "Continuous", new DateTime().minusDays(2).toString());
		AttemptDetail attempt = new AttemptDetail();
		attempt.setScore(-1);
		attempt.setAttemptDate(Calendar.getInstance());
		marketPlaceDao.updateGradebookColumn(attempt, "00111");
		createStudents("00111");
		assertEquals(120 ,marketPlaceDao.getGradebookColumnByNameAndStudentId("TEST", "00111").getGrade());
	}
	
	/**
	 * Test use passive item.
	 */
	@Test
	public void testUsePassiveItem(){ // Passives are used not automatically but more like Teacher has to reply to email
		createStudents("00111");
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("TEST");
		gradableItem.setMaxAttempts(2);
		bbHandler.addGradableItem(gradableItem);
		Item item = marketPlaceDao.loadItem("Passive");
		assertEquals(2, gradableItem.getMaxAttempts());
		bbHandler.processItem("Passive");
		bbHandler.useItem(item, "TEST");
		assertEquals(2, gradableItem.getMaxAttempts());
	}
		
	/**
	 * Test get student if instructor.
	 */
	@Test
	public void testGetStudentIfInstructor(){
		assertNull(bbHandler.getStudent());
	}
	
	/**
	 * Test get student if right student.
	 */
	@Test
	public void testGetStudentIfRightStudent(){
		createStudents("00111");
		assertNotNull(bbHandler.getStudent());
	}
	
	/**
	 * Test get student if wrong student.
	 */
	@Test
	public void testGetStudentIfWrongStudent(){
		createStudents("0011");
		assertNull(bbHandler.getStudent());
	}
	
	/**
	 * Test get all columns by type when continuous.
	 */
	@Test
	public void testGetAllColumnsByTypeWhenContinuous(){
		List<String> columns = bbHandler.getAllColumnsByType("Continuous");
		assertEquals(1, columns.size());
		assertEquals("ALL", columns.get(0));
	}
	
	/**
	 * Test get all columns by type when all.
	 */
	@Test
	public void testGetAllColumnsByTypeWhenAll(){
		GradableItem gradableItem = new GradableItem();
		gradableItem.setCategory("_12_1");
		gradableItem.setCategoryId(Id.newId(GradableItem.DATA_TYPE));
		gradableItem.setTitle("Test");
		gradableItem.setVisibleToStudents(true);
		bbHandler.addGradableItem(gradableItem);
		GradableItem gradableItem2 = new GradableItem();
		gradableItem2.setCategory("_10_1");
		gradableItem2.setCategoryId(Id.newId(GradableItem.DATA_TYPE));
		gradableItem2.setTitle("Assignment");
		gradableItem2.setVisibleToStudents(true);
		bbHandler.addGradableItem(gradableItem2);
		List<String> columns = bbHandler.getAllColumnsByType("OnceTwo");
		assertEquals(2, columns.size());
		assertEquals("[Test, Assignment]", columns.toString());
	}
	
	/**
	 * Test get all columns by type when assignment.
	 */
	@Test
	public void testGetAllColumnsByTypeWhenAssignment(){
		GradableItem gradableItem = new GradableItem();
		gradableItem.setCategoryId(Id.newId(GradableItem.DATA_TYPE));
		gradableItem.setTitle("Test");
		gradableItem.setVisibleToStudents(true);
		bbHandler.addGradableItem(gradableItem);
		GradableItem gradableItem2 = new GradableItem();
		gradableItem2.setCategoryId(Id.newId(GradableItem.DATA_TYPE));
		gradableItem2.setTitle("Assignment");
		gradableItem2.setVisibleToStudents(true);
		bbHandler.addGradableItem(gradableItem2);
		List<String> columns = bbHandler.getAllColumnsByType(gradableItem2.getCategoryId().getExternalString());
		assertEquals(1, columns.size());
		assertEquals("[Assignment]", columns.toString());
	}
	
	/**
	 * Test get all columns by type when test.
	 */
	@Test
	public void testGetAllColumnsByTypeWhenTest(){
		GradableItem gradableItem = new GradableItem();
		gradableItem.setCategoryId(Id.newId(GradableItem.DATA_TYPE));
		gradableItem.setTitle("Test");
		gradableItem.setVisibleToStudents(true);
		bbHandler.addGradableItem(gradableItem);
		GradableItem gradableItem2 = new GradableItem();
		gradableItem2.setCategoryId(Id.newId(GradableItem.DATA_TYPE));
		gradableItem2.setTitle("Assignment");
		gradableItem2.setVisibleToStudents(true);
		bbHandler.addGradableItem(gradableItem2);
		List<String> columns = bbHandler.getAllColumnsByType(gradableItem.getCategoryId().getExternalString());
		assertEquals(1, columns.size());
		assertEquals("[Test]", columns.toString());
	}
	
	/**
	 * Test add gold to all.
	 */
	@Test
	public void testAddGoldToAll(){
		createStudents("00111");
		assertEquals(1000, bbHandler.getStudent().getGold());
		bbHandler.addGoldToAll("1000");
		assertEquals("2000.0", bbHandler.getCurrentGradeDetail().getManualGrade());
	}
	
	/**
	 * Test instant grade item with not this column.
	 */
	@Test (expected=NullPointerException.class)
	public void testInstantGradeItemWithNotThisColumn(){
		createStudents("00111");
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("TEST");
		bbHandler.addGradableItem(gradableItem);
		Item item = marketPlaceDao.loadItem("SpecificNot");
		bbHandler.useItem(item, "TEST");
		assertEquals("0.0", bbHandler.getCurrentGradeDetail().getManualGrade());
	}
	
	/**
	 * Test instant grade item with only this column.
	 */
	@Test
	public void testInstantGradeItemWithOnlyThisColumn(){
		createStudents("00111");
		Item item = marketPlaceDao.loadItem("SpecificOnly");
		marketPlaceDao.persistPurhcase("00111", item);
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("TEST");
		bbHandler.addGradableItem(gradableItem);
		bbHandler.useItem(item, "TEST");
		assertEquals("1002.0", bbHandler.getCurrentGradeDetail().getManualGrade());
	}
	
	/**
	 * Test set community item.
	 */
	@Test
	public void testSetCommunityItem(){
		createStudents("00111");
		Item item = marketPlaceDao.loadItem("Once");
		CommunityItem cItem = new CommunityItem(item);
		bbHandler.setCommunityItem(cItem);
		assertEquals("Once", marketPlaceDao.getCurrentCommunityItem().getName());
	}
	
	/**
	 * Test use community item.
	 */
	@Test
	public void testUseCommunityItem(){
		createStudents("00111");
		Item item = marketPlaceDao.loadItem("Once");
		CommunityItem cItem = new CommunityItem(item);
		cItem.setColumnName("TEST");
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("TEST");
		Calendar cal = Calendar.getInstance();
		gradableItem.setDueDate(cal);
		bbHandler.addGradableItem(gradableItem);
		bbHandler.useCommunityItem(cItem);
		cal.add(Calendar.HOUR_OF_DAY, 24);
		assertEquals(cal, bbHandler.getGradableItemList().get(1).getDueDate());
	}
	
	/**
	 * Test refund community item.
	 */
	@Test
	public void testRefundCommunityItem(){
		createStudents("00111");
		Item item = marketPlaceDao.loadItem("Once");
		CommunityItem cItem = new CommunityItem(item);
		cItem.setColumnName("TEST");
		cItem.setPaid(500);
		cItem.setCost(500);
		bbHandler.getStudent().substractGold(cItem);
		bbHandler.setCommunityItem(cItem);
		assertEquals(500, bbHandler.getStudent().getGold());
		cItem = marketPlaceDao.getCurrentCommunityItem();
		bbHandler.refundCommunityItem(cItem.getForeignId());
		assertEquals("1000", bbHandler.getCurrentGradeDetail().getManualGrade());
	}
	
	/**
	 * Tear down.
	 */
	@After
	public void tearDown(){
		File file = new File("./resources/dbUnitDataSet.xml");
		Scanner scan = null;
		try {
			scan = new Scanner(file);
			String data = scan.useDelimiter("\\Z").next();
			data = data.replace("course_id=\"" + courseID.getExternalString() + "\"", "course_id=\"courseID\"");
			PrintWriter writer = new PrintWriter("./resources/dbUnitDataSet.xml");
			writer.write(data);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally{
			scan.close();
		}
	}
	
	/**
	 * Creates the students.
	 *
	 * @param studentId the student id
	 */
	private void createStudents(String studentId){
		List<CourseMembership> memberList = new ArrayList<CourseMembership>();
		CourseMembership student = new CourseMembership();
		student.setCourseId(courseID);
		student.setId(Id.newId(CourseMembership.DATA_TYPE));
		User user = new User();
		user.setId(sessionUser.getId());
		user.setFamilyName("LAST");
		user.setGivenName("FIRST");
		user.setStudentId(studentId);
		user.setUserName("username");
		student.setUser(user);
		memberList.add(student);
		try {
			bbHandler.testingConstructor(memberList.iterator(), true);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Update database info.
	 */
	private void updateDatabaseInfo() {
		File file = new File("./resources/dbUnitDataSet.xml");
		Scanner scan = null;
		try {
			scan = new Scanner(file);
			String data = scan.useDelimiter("\\Z").next();
			data = data.replace("course_id=\"courseID\"", "course_id=\"" + courseID.getExternalString() + "\"");
			PrintWriter writer = new PrintWriter("./resources/dbUnitDataSet.xml");
			writer.write(data);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally{
			scan.close();
		}
	}
	
}
