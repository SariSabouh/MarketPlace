package cs499.test;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;

import blackboard.data.course.Course;
import blackboard.data.course.CourseMembership;
import blackboard.data.user.User;
import blackboard.persist.Id;
import blackboard.persist.PersistenceException;
import blackboard.platform.gradebook2.GradableItem;
import cs499.controllers.BlackboardHandler;
import cs499.controllers.JSUBbDatabase;
import cs499.controllers.MarketPlaceDAO;
import cs499.itemHandler.Item;
import cs499.util.Student;

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
        IDataSet dataSet = getDataSet();
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        connection.close();
        sessionUser = new User();
		sessionUser.setFamilyName("LAST");
		sessionUser.setGivenName("FIRST");
		sessionUser.setId(Id.newId(User.DATA_TYPE));
		sessionUser.setUserName("username");
		sessionUser.setStudentId("00111");
		courseID = Id.newId(Course.DATA_TYPE);
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
		createStudents();
		assertEquals(1000, bbHandler.getStudent().getGold());
	}
	
	@Test
	public void testProcessItem(){
		createStudents();
		assertEquals(0, bbHandler.getStudent().getItemList().size());
		bbHandler.processItem("Once");
		assertEquals("Once", bbHandler.getStudent().getItemList().get(0).getName());
	}
	
	private void createStudents(){
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
			bbHandler.testingConstructor(memberList.iterator(), true);
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
	}
	
}
