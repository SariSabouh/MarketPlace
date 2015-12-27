//package cs499.test;
//
//import static org.junit.Assert.*;
//
//import java.io.FileInputStream;
//import java.sql.Connection;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.dbunit.database.DatabaseConnection;
//import org.dbunit.database.IDatabaseConnection;
//import org.dbunit.dataset.IDataSet;
//import org.dbunit.dataset.xml.FlatXmlDataSet;
//import org.dbunit.operation.DatabaseOperation;
//import org.junit.Before;
//import org.junit.Test;
//
//import blackboard.data.course.Course;
//import blackboard.data.course.CourseMembership;
//import blackboard.data.user.User;
//import blackboard.persist.Id;
//import blackboard.persist.PersistenceException;
//import blackboard.platform.gradebook2.GradableItem;
//import cs499.controllers.JSUBbDatabase;
//import cs499.controllers.MarketPlaceDAO;
//import cs499.itemHandler.Item;
//import cs499.util.Student;
//
//public class BlackboardHandlerTest {
//	
//	BlackboardHandlerMock bbHandler;
//	MarketPlaceDAO marketPlaceDao;
//	
//	protected IDatabaseConnection getConnection() throws Exception {
//		Class.forName("com.mysql.jdbc.Driver");
//		Connection jdbcConnection = JSUBbDatabase.getConnection(true);
//		return new DatabaseConnection(jdbcConnection);
//	}
//	
//	protected IDataSet getDataSet() throws Exception {
//		return new FlatXmlDataSet(new FileInputStream("./resources/dbUnitDataSet.xml"));
//	}
//	
//	@Before
//	public void setUp() throws Exception
//    {
//        IDatabaseConnection connection = getConnection();
//        IDataSet dataSet = getDataSet();
//        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
//        connection.close();
//        User sessionUser = new User();
//		sessionUser.setFamilyName("LAST");
//		sessionUser.setGivenName("FIRST");
//		sessionUser.setId(Id.newId(User.DATA_TYPE));
//		sessionUser.setUserName("username");
//		sessionUser.setStudentId("00111");
//		List<Item> itemList = new MarketPlaceDAO(true).loadItems();
//		bbHandler = new BlackboardHandlerMock(Id.newId(Course.DATA_TYPE), sessionUser, itemList);
//		marketPlaceDao = new MarketPlaceDAO(true);
//    }
//
//	@Test
//	public void testAddGoldColumn() throws PersistenceException{
//		assertEquals(0, bbHandler.getGradableItemList().size());
//		bbHandler.addGoldColumn();
//		assertEquals("Gold", bbHandler.getGradableItemList().get(0).getTitle());
//	}
//	
//	@Test
//	public void testFailAddGoldColumn() throws PersistenceException{
//		GradableItem gradableItem = new GradableItem();
//		gradableItem.setTitle("Gold");
//		bbHandler.addGradableItem(gradableItem);
//		assertEquals("Gold", bbHandler.getGradableItemList().get(0).getTitle());
//		bbHandler.addGoldColumn();
//		assertEquals("Gold", bbHandler.getGradableItemList().get(0).getTitle());
//	}
//	
//	@Test
//	public void testAddGoldColumnWithOtherColumns() throws PersistenceException{
//		GradableItem gradableItem = new GradableItem();
//		gradableItem.setTitle("Final");
//		assertEquals(0, bbHandler.getGradableItemList().size());
//		bbHandler.addGradableItem(gradableItem);
//		assertEquals(1, bbHandler.getGradableItemList().size());
//		bbHandler.addGoldColumn();
//		assertEquals(2, bbHandler.getGradableItemList().size());
//	}
//	
//	@Test
//	public void testSetStudentsWithoutGoldColumn(){
//		List<CourseMembership> memberList = new ArrayList<CourseMembership>();
//		CourseMembership student = new CourseMembership();
//		student.setCourseId(bbHandler.courseID);
//		student.setId(Id.newId(CourseMembership.DATA_TYPE));
//		User user = new User();
//		user.setFamilyName("LAST");
//		user.setGivenName("FIRST");
//		user.setStudentId("00111");
//		user.setUserName("username");
//		student.setUser(user);
//		memberList.add(student);
//		assertEquals(0, bbHandler.students.size());
//		bbHandler.setStudentsList(memberList.iterator());
//		assertEquals("00111", bbHandler.students.get(0).getStudentID());
//	}
//	
//	@Test
//	public void testSetStudentsWithGoldColumn(){
//		List<CourseMembership> memberList = new ArrayList<CourseMembership>();
//		CourseMembership student = new CourseMembership();
//		student.setCourseId(bbHandler.courseID);
//		student.setId(Id.newId(CourseMembership.DATA_TYPE));
//		User user = new User();
//		user.setFamilyName("LAST");
//		user.setGivenName("FIRST");
//		user.setStudentId("00111");
//		user.setUserName("username");
//		student.setUser(user);
//		memberList.add(student);
//		assertEquals(0, bbHandler.students.size());
//		bbHandler.addGoldColumn();
//		bbHandler.setStudentsList(memberList.iterator());
//		assertEquals("00111", bbHandler.students.get(0).getStudentID());
//	}
//	
//	@Test
//	public void testSetStudentsWithItem(){
//		List<CourseMembership> memberList = new ArrayList<CourseMembership>();
//		CourseMembership student = new CourseMembership();
//		student.setCourseId(bbHandler.courseID);
//		student.setId(Id.newId(CourseMembership.DATA_TYPE));
//		User user = new User();
//		user.setFamilyName("LAST");
//		user.setGivenName("FIRST");
//		user.setStudentId("00111");
//		user.setUserName("username");
//		student.setUser(user);
//		memberList.add(student);
//		assertEquals(0, bbHandler.students.size());
//		bbHandler.addGoldColumn();
//		marketPlaceDao.persistPurhcase("00111", "Once");
//		bbHandler.setStudentsList(memberList.iterator());
//		assertEquals("00111", bbHandler.students.get(0).getStudentID());
//	}
//	
//	@Test
//	public void testUpdateStudentGold(){
//		addStudent(true);
//		Student studentTest = bbHandler.students.get(0);
//		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
//		bbHandler.updateStudentGold();
//		assertEquals(125, studentTest.getGold());
//	}
//	
//	@Test
//	public void testActivateWaitList(){
//		addStudent(true);
//		marketPlaceDao.persistPurhcase("00111", "Once");
//		marketPlaceDao.updateItemUsage("Once", "00111");
//		bbHandler.activateWaitList();
//		
//	}
//	
//	@Test
//	public void testProcessItemTwiceWhileStoreHasSupply() {
//		addStudent(true);
//		assertEquals(0, marketPlaceDao.loadNewPurchases("00111").size());
//		bbHandler.processItem("Once");
//		assertEquals("Once", marketPlaceDao.loadNewPurchases("00111").get(0));
//	}
//	
//	@Test
//	public void testProcessItemWhileStoreHasSupplyReturnsOneItem() {
//		addStudent(true);
//		bbHandler.processItem("OnceTwo");
//		bbHandler.processItem("OnceTwo");
//		assertEquals("OnceTwo", marketPlaceDao.loadNewPurchases("00111").get(0));
//	}
//	
//	@Test
//	public void testProcessAsInstructor() {
//		addStudent(false);
//		bbHandler.processItem("OnceTwo");
//		assertEquals(0, marketPlaceDao.loadNewPurchases("00111").size());
//	}
//	
//	@Test
//	public void testProcessAsInstructorAfterStudent() {
//		addStudent(true);
//		bbHandler.processItem("Once");
//		bbHandler.students.remove(0);
//		addStudent(false);
//		bbHandler.processItem("OnceTwo");
//		assertEquals(1, marketPlaceDao.loadNewPurchases("00111").size());
//	}
//	
//	@Test
//	public void testProcessItemWhileStoreDoesNotHaveSupply() {
//		addStudent(true);
//		bbHandler.processItem("OnceTwo");
//		bbHandler.processItem("OnceTwo");
//		assertEquals("OnceTwo", marketPlaceDao.loadNewPurchases("00111").get(0));
//		bbHandler.processItem("OnceTwo");
//		assertEquals(0, marketPlaceDao.loadNewPurchases("00111").size());
//	}
//	
//	@Test
//	public void testProcessItemStudentCannotAfford() {
//		addStudent(true);
//		bbHandler.processItem("Once");
//		bbHandler.processItem("Once");
//		assertEquals("Once", marketPlaceDao.loadNewPurchases("00111").get(0));
//	}
//	
//	@Test
//	public void testProcessDifferentItems(){
//		addStudent(true);
//		bbHandler.processItem("OnceTwo");
//		bbHandler.processItem("Passive");
//		assertEquals(2, marketPlaceDao.loadNewPurchases("00111").size());
//	}
//
//	@Test
//	public void testUseInstantItem() {
//		addStudent(true);
//		bbHandler.processItem("Once");
//		Item item = marketPlaceDao.loadItem("Once");
//		assertEquals(0, marketPlaceDao.loadWaitList().size());
//		bbHandler.useItem(item);
//		assertEquals("Once", marketPlaceDao.loadWaitList().get(0).getName());
//	}
//	
//	@Test
//	public void testUseItemAsInstructor() {
//		addStudent(true);
//		bbHandler.processItem("Once");
//		Item item = marketPlaceDao.loadItem("Once");
//		assertEquals(0, marketPlaceDao.loadWaitList().size());
//		bbHandler.students.remove(0);
//		addStudent(false);
//		assertEquals(false, bbHandler.useItem(item));
//	}
//	
//	@Test
//	public void testUseItemAsInstructorWithNoPurchase() {
//		addStudent(false);
//		assertEquals(false, bbHandler.useItem(new Item("Once")));		
//	}
//		
//	@Test
//	public void testUsePassiveItem() {
//		addStudent(true);
//		bbHandler.processItem("Passive");
//		Item item = marketPlaceDao.loadItem("Passive");
//		assertEquals(0, marketPlaceDao.loadWaitList().size());
//		bbHandler.useItem(item);
//		assertEquals("Passive", marketPlaceDao.loadWaitList().get(0).getName());
//	}
//	
//	@Test
//	public void testUseContinuousItem() {
//		addStudent(true);
//		bbHandler.processItem("Continuous");
//		Item item = marketPlaceDao.loadItem("Continuous");
//		assertEquals(0, marketPlaceDao.loadWaitList().size());
//		bbHandler.useItem(item);
//		assertEquals("Continuous", marketPlaceDao.loadWaitList().get(0).getName());
//	}
//	
//	@Test
//	public void testUseMultipleItems(){
//		addStudent(true);
//		bbHandler.processItem("OnceTwo");
//		bbHandler.processItem("Passive");
//		Item item = marketPlaceDao.loadItem("OnceTwo");
//		bbHandler.useItem(item);
//		item = marketPlaceDao.loadItem("Passive");
//		bbHandler.useItem(item);
//		assertEquals(2, marketPlaceDao.loadWaitList().size());
//	}
//	
//	@Test
//	public void testInstantItemCycle(){
//		addStudent(true);
//		bbHandler.processItem("Once");
//		assertEquals(125, bbHandler.students.get(0).getGold());
//		bbHandler.useItem(marketPlaceDao.loadItem("Once"));
//		assertEquals("Once", marketPlaceDao.loadWaitList().get(0).getName());
//		bbHandler.activateWaitList();
//	}
//
//	@Test
//	public void testGetStudent() {
//		addStudent(true);
//		assertEquals("00111", bbHandler.getStudent().getStudentID());
//	}
//	
//	@Test
//	public void testFailedGetStudent() {
//		addStudent(false);
//		assertEquals(null, bbHandler.getStudent());
//	}
//	
//	public void addStudent(boolean isStudent){
//		List<CourseMembership> memberList = new ArrayList<CourseMembership>();
//		CourseMembership student = new CourseMembership();
//		student.setCourseId(bbHandler.courseID);
//		student.setId(Id.newId(CourseMembership.DATA_TYPE));
//		User user = new User();
//		user.setFamilyName("LAST");
//		user.setGivenName("FIRST");
//		user.setUserName("username");
//		if(isStudent){
//			user.setStudentId("00111");
//			user.setId(bbHandler.sessionUser.getId());
//		}
//		else{
//			user.setStudentId(null);
//			user.setId(Id.newId(User.DATA_TYPE));
//		}
//		student.setUser(user);
//		memberList.add(student);
//		bbHandler.addGoldColumn();
//		bbHandler.setStudentsList(memberList.iterator());
//	}
//
//}
