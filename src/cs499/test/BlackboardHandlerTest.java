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
import cs499.itemHandler.Item;

public class BlackboardHandlerTest {
	
	private BlackboardHandler bbHandler;
	private MarketPlaceDAO marketPlaceDao;
	private Id courseID;
	private User sessionUser;
	
	protected IDatabaseConnection getConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		Connection jdbcConnection = JSUBbDatabase.getConnection(true);
		return new DatabaseConnection(jdbcConnection);
	}
	
	protected IDataSet getDataSet() throws Exception {
		return new FlatXmlDataSet(new FileInputStream("./resources/dbUnitDataSet.xml"));
	}
	
	@Before
	public void setUp() throws Exception
    {
        IDatabaseConnection connection = getConnection();
        courseID = Id.newId(Course.DATA_TYPE);
        updateDatabaseInfo();
        IDataSet dataSet = getDataSet();
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        connection.close();
        sessionUser = new User();
		sessionUser.setFamilyName("LAST");
		sessionUser.setGivenName("FIRST");
		sessionUser.setId(Id.newId(User.DATA_TYPE));
		sessionUser.setUserName("username");
		sessionUser.setStudentId("00111");
		marketPlaceDao = new MarketPlaceDAO(true, courseID.toExternalString());
		bbHandler = new BlackboardHandler(courseID, sessionUser, marketPlaceDao.loadItems());
    }

	@Test
	public void testConstructorAddGoldColumn() throws PersistenceException{
		assertEquals(0, bbHandler.getGradableItemList().size());
		bbHandler.testingConstructor(null, true);
		assertEquals("Gold", bbHandler.getGradableItemList().get(0).getTitle());
	}
	
	@Test
	public void testAddGoldColumn() throws PersistenceException{
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("Test");
		bbHandler.addGradableItem(gradableItem);
		assertEquals(1, bbHandler.getGradableItemList().size());
	}
	
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
	
	@Test
	public void testSetStudentsWithGoldColumn() throws PersistenceException{
		createStudents("00111");
		assertEquals(1000, bbHandler.getStudent().getGold());
	}
	
	@Test
	public void testSetStudentsWithItems() throws PersistenceException{
		Item item = new Item("Once");
		marketPlaceDao.persistPurhcase("00111", item);
		createStudents("00111");
		assertEquals(1000, bbHandler.getStudent().getGold());
	}
	
	@Test
	public void testProcessItemIfInstructor(){
		bbHandler.processItem("Once");
	}
	
	@Test
	public void testProcessItemIfStudentCanAfford(){
		createStudents("00111");
		assertEquals(0, bbHandler.getStudent().getItemList().size());
		bbHandler.processItem("Once");
		assertEquals("Once", bbHandler.getStudent().getItemList().get(0).getName());
	}

	@Test
	public void testProcessItemIfStudentCanNotAfford(){
		createStudents("00111");
		bbHandler.getStudent().setGold(0);
		bbHandler.processItem("Once");
		assertEquals(0, bbHandler.getStudent().getItemList().size());
	}
	
	@Test
	public void testProcessItemIfOutOfSupply(){
		createStudents("00111");
		bbHandler.processItem("Once");
		bbHandler.getStudent().setGold(1000);
		bbHandler.processItem("Once");
		assertEquals(1, bbHandler.getStudent().getItemList().size());
	}
	
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
	
	@Test
	public void testInstantNumAttemptItem(){
		createStudents("00111");
		GradableItem gradableItem = new GradableItem();
		gradableItem.setTitle("Test");
		gradableItem.setMaxAttempts(2);
		bbHandler.addGradableItem(gradableItem);
		Item item = marketPlaceDao.loadItem("OnceTwo");
		bbHandler.useItem(item, "Test");
		assertEquals(4, gradableItem.getMaxAttempts());
	}
	
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
	
	@Test
	public void testGetStudentIfInstructor(){
		assertNull(bbHandler.getStudent());
	}
	
	@Test
	public void testGetStudentIfRightStudent(){
		createStudents("00111");
		assertNotNull(bbHandler.getStudent());
	}
	
	@Test
	public void testGetStudentIfWrongStudent(){
		createStudents("0011");
		assertNull(bbHandler.getStudent());
	}
	
	@Test
	public void testGetAllColumnsByTypeWhenContinuous(){
		List<String> columns = bbHandler.getAllColumnsByType("Continuous");
		assertEquals(1, columns.size());
		assertEquals("ALL", columns.get(0));
	}
	
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
	
	@Test
	public void testAddGoldToAll(){
		createStudents("00111");
		assertEquals(1000, bbHandler.getStudent().getGold());
		bbHandler.addGoldToAll("1000");
		assertEquals(2000, bbHandler.getStudent().getGold());
	}
	
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
