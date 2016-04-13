package cs499.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import blackboard.platform.gradebook2.AttemptDetail;
import cs499.controllers.JSUBbDatabase;
import cs499.controllers.MarketPlaceDAO;
import cs499.object.CommunityItem;
import cs499.object.Item;
import cs499.object.Setting;
import cs499.object.Student;
import cs499.object.Item.AssessmentType;
import cs499.object.Item.AttributeAffected;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.joda.time.DateTime;

/**
 * The Class MarketPlaceDAOTest.
 */
public class MarketPlaceDAOTest {

	/** The market place dao. */
	private MarketPlaceDAO marketPlaceDao;
	
	/** The course id. */
	private String courseId;

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
	 * Gets the sets the up operation.
	 *
	 * @return the sets the up operation
	 * @throws Exception the exception
	 */
	protected DatabaseOperation getSetUpOperation() throws Exception {
		return DatabaseOperation.REFRESH;
	}

	/**
	 * Gets the tear down operation.
	 *
	 * @return the tear down operation
	 * @throws Exception the exception
	 */
	protected DatabaseOperation getTearDownOperation() throws Exception {
		return DatabaseOperation.NONE;
	}
	
	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception
    {
		courseId = "10_1";
        updateDatabaseInfo();
        IDatabaseConnection connection = getConnection();
        IDataSet dataSet = getDataSet();
        marketPlaceDao = new MarketPlaceDAO(true, courseId);
        marketPlaceDao.emptyDatabase();
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        connection.close();
    }
	
	/**
	 * Test load item with specific name.
	 */
	@Test
	public void testLoadItemWithSpecificName(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals("875.0", item.getCost() + "");
	}
	
	/**
	 * Test load item that does not exist.
	 */
	@Test(expected=NullPointerException.class)
	public void testLoadItemThatDoesNotExist(){
		Item item = marketPlaceDao.loadItem("UNO");
		assertEquals("UNO", item.getName());
	}
	
	/**
	 * Test load all items.
	 */
	@Test
	public void testLoadAllItems(){
		List<String> itemsList = new ArrayList<String>();
		for(Item item : marketPlaceDao.loadItems()){
			itemsList.add(item.getName());
		}
		assertEquals(itemsList.toString(), "[Once, Continuous, OnceTwo, OnceThree, Passive, SpecificOnly, SpecificNot]");
	}
	
	/**
	 * Test initialize items.
	 */
	@Test
	public void testInitializeItems(){
		String content ="--\nname=Twice\ncost=875\nduration=ONCE\ntype=TEST\nattAffected=DUEDATE\nsupply=1"
					+ "\neffectMagnitude=24\n--\nname=Cont\ncost=115\nduration=65\ntype=ASSIGNMENT\nattAffected=GRADE\nsupply=10\neffectMagnitude=20\n--";
		List<String> itemsList = new ArrayList<String>();
		for(Item item : marketPlaceDao.initilizeDatabase(content)){
			itemsList.add(item.getName());
		}
		assertEquals(itemsList.toString(), "[Twice, Cont]");
		Item item = marketPlaceDao.loadItem("ITEM_INIT");
		assertEquals("ITEM_INIT", item.getName());
	}
	
	/**
	 * Test persist purchase.
	 */
	@Test
	public void testPersistPurchase(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
	}
	
	/**
	 * Test persist purchase with wrong item name.
	 */
	@Test(expected=NullPointerException.class)
	public void testPersistPurchaseWithWrongItemName(){
		Item item = marketPlaceDao.loadItem("UNO");
		assertEquals(false, marketPlaceDao.persistPurhcase("00111", item));
	}
	
	/**
	 * Test empty database.
	 */
	@Test
	public void testEmptyDatabase(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		assertEquals(7, marketPlaceDao.loadItems().size());
		marketPlaceDao.emptyDatabase();
		assertEquals(0, marketPlaceDao.loadItems().size());
	}	
	
	/**
	 * Test unused items.
	 */
	@Test
	public void testUnusedItems(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		List<Item> itemList = marketPlaceDao.loadItems();
		assertEquals("Once", marketPlaceDao.loadNotExpiredItems(itemList, "00111").get(0).getName());
	}
	
	/**
	 * Test unused items with same name that item has been used.
	 */
	@Test
	public void testUnusedItemsWithSameNameThatItemHasBeenUsed(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		marketPlaceDao.expireInstantItem("Once", "00111", "Test");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		List<Item> itemList = marketPlaceDao.loadItems();
		assertEquals("Once", marketPlaceDao.loadNotExpiredItems(itemList, "00111").get(0).getName());
	}
	
	/**
	 * Test load unused items that are used.
	 */
	@Test
	public void testLoadUnusedItemsThatAreUsed(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		marketPlaceDao.expireInstantItem("Once", "00111", "Test");
		List<Item> itemList = marketPlaceDao.loadItems();
		assertEquals("[]", marketPlaceDao.loadNotExpiredItems(itemList, "00111").toString());
	}
	
	/**
	 * Test load unused items that are passive.
	 */
	@Test
	public void testLoadUnusedItemsThatArePassive(){
		Item item = marketPlaceDao.loadItem("Passive");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		assertEquals(true, marketPlaceDao.updateItemUsage("Passive", "00111", ("Test")));
		List<Item> itemList = marketPlaceDao.loadItems();
		assertEquals("Passive", marketPlaceDao.loadNotExpiredItems(itemList, "00111").get(0).getName());
	}

	/**
	 * Test unused items with wrong student id.
	 */
	@Test
	public void testUnusedItemsWithWrongStudentId(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		List<Item> itemList = marketPlaceDao.loadItems();
		assertEquals("[]", marketPlaceDao.loadNotExpiredItems(itemList, "0111").toString());
	}
	
	/**
	 * Test expire instant item.
	 */
	@Test
	public void testExpireInstantItem(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		assertEquals(true, marketPlaceDao.expireInstantItem("Once", "00111", "Test"));
		List<Item> itemList = marketPlaceDao.loadItems();
		assertEquals("[]", marketPlaceDao.loadNotExpiredItems(itemList, "00111").toString());
	}
	
	/**
	 * Test expire instant item with wrong name.
	 */
	@Test
	public void testExpireInstantItemWithWrongName(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		assertEquals(false, marketPlaceDao.expireInstantItem("UNO", "00111", "Test"));
		List<Item> itemList = marketPlaceDao.loadItems();
		assertEquals("Once", marketPlaceDao.loadNotExpiredItems(itemList, "00111").get(0).getName());
	}
	
	/**
	 * Test update item usage.
	 */
	@Test
	public void testUpdateItemUsage(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("001111", item));
		assertEquals(true, marketPlaceDao.updateItemUsage("Once", "001111", "Test"));
	}
	
	/**
	 * Test update item usage with wrong name.
	 */
	@Test(expected=NullPointerException.class)
	public void testUpdateItemUsageWithWrongName(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		marketPlaceDao.updateItemUsage("UNO", "00111", "Test");
	}
	
	/**
	 * Test not of supply.
	 *
	 * @throws SQLException the SQL exception
	 */
	@Test
	public void testNotOfSupply() throws SQLException{
		Item item = new Item("Once");
		item.setSupply(5);
		assertEquals(false, marketPlaceDao.isOutOfSupply(item));
	}
	
	/**
	 * Test is out of supply with one purchase.
	 */
	@Test
	public void testIsOutOfSupplyWithOnePurchase(){
		Item item = new Item("Once");
		item.setCost(100);
		item.setSupply(1);
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		assertEquals(true, marketPlaceDao.isOutOfSupply(item));
	}
	
	/**
	 * Test is out of supply with multiple purchase.
	 */
	@Test
	public void testIsOutOfSupplyWithMultiplePurchase(){
		Item item = new Item("Once");
		item.setCost(100);
		item.setSupply(3);
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		assertEquals(true, marketPlaceDao.isOutOfSupply(item));
	}
	
	/**
	 * Test get gradebook column after insert.
	 */
	@Test
	public void testGetGradebookColumnAfterInsert(){
		marketPlaceDao.insertGradebookColumn(10, "TEST", "00111");
		assertEquals(10, marketPlaceDao.getGradebookColumnByNameAndStudentId("TEST", "00111").getGrade());
	}
	
	/**
	 * Test update gradebook column after insert.
	 */
	@Test
	public void testUpdateGradebookColumnAfterInsert(){
		AttemptDetail attempt = new AttemptDetail();
		attempt.setScore(10);
		marketPlaceDao.insertGradebookColumn(10, "TEST", "00111");
		assertEquals(true, marketPlaceDao.updateGradebookColumn(attempt, "00111"));
	}
	
	/**
	 * Test update setting.
	 */
	@Test
	public void testUpdateSetting(){
		Setting setting = new Setting();
		setting.setName("visible_columns");
		setting.setValue("N");
		marketPlaceDao.updateSetting(setting);
		setting = marketPlaceDao.getSetting("visible_columns");
		assertEquals("N", setting.getValue());
	}
	
	/**
	 * Test get setting.
	 */
	@Test
	public void testGetSetting(){
		Setting setting = marketPlaceDao.getSetting("visible_columns");
		assertEquals("Y", setting.getValue());
	}
	
	/**
	 * Test get default setting.
	 */
	@Test
	public void testGetDefaultSetting(){
		List<Setting> settings = marketPlaceDao.getDefaultSettings();
		assertEquals("Y", settings.get(0).getValue());
		assertEquals("visible_columns", settings.get(0).getName());
	}
	
	/**
	 * Test add item.
	 */
	@Test
	public void testAddItem(){
		Item item = new Item("New Item");
		item.setCost(100);
		item.setDuration(0);
		item.setEffectMagnitude(19);
		item.setSupply(1);
		item.setTimesUsed(0);
		item.setType(AssessmentType.ALL);
		item.setAttributeAffected(AttributeAffected.DUEDATE);
		marketPlaceDao.addItem(item);
		assertEquals("New Item", marketPlaceDao.loadItem("New Item").getName());
	}
	
	/**
	 * Test edit item.
	 */
	@Test
	public void testEditItem(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals("875.0", item.getCost() + "");
		item.setCost(999);
		marketPlaceDao.editItem(item);
		item = marketPlaceDao.loadItem("Once");
		assertEquals("999.0", item.getCost() + "");
	}
	
	/**
	 * Test set used expiry date.
	 */
	@Test 
	public void testSetUsedExpiryDate(){
		Item item = new Item("Continuous");
		item.setCost(100);
		item.setDuration(24);
		marketPlaceDao.persistPurhcase("00111", item);
		marketPlaceDao.setUsedExpiryDate(item, "00111");
		List<Item> items = new ArrayList<Item>();
		items.add(item);
		items = marketPlaceDao.loadNotExpiredItems(items, "00111");
		assertEquals(new DateTime().plusHours(24).toString().substring(0, 10), items.get(0).getExpirationDate().substring(0, 10));
	}
	
	/**
	 * Test load times used.
	 */
	@Test 
	public void testLoadTimesUsed(){
		Item item = new Item("Continuous");
		item.setCost(100);
		List<Item> items = new ArrayList<Item>();
		item.setDuration(24);
		items.add(item);
		marketPlaceDao.persistPurhcase("00111", item);
		marketPlaceDao.setUsedExpiryDate(item, "00111");
		marketPlaceDao.updateItemUsage("Continuous", "00111", "Test");
		items = marketPlaceDao.loadNotExpiredItems(items, "00111");
		assertEquals(1, items.get(0).getTimesUsed());
	}
	
	/**
	 * Test add community item.
	 */
	@Test
	public void testAddCommunityItem(){
		CommunityItem item = new CommunityItem("Once");
		item.setColumnName("Test 1");
		item.setPaid(50);
		marketPlaceDao.addCommunityItem(item, "00111");
		assertEquals("Once", marketPlaceDao.getCurrentCommunityItem().getName());
	}

	/**
	 * Test add community item payment.
	 */
	@Test
	public void testAddCommunityItemPayment(){
		CommunityItem item = new CommunityItem("Once");
		item.setColumnName("Test 1");
		item.setPaid(70);
		marketPlaceDao.addCommunityItem(item, "00111");
		item = marketPlaceDao.getCurrentCommunityItem();
		assertEquals(70, item.getPaid());
		item.setPaid(30);
		marketPlaceDao.addCommunityItemPayment(item, "0011", item.getForeignId());
		item = marketPlaceDao.getCurrentCommunityItem();
		assertEquals(100, item.getPaid());
	}
	
	/**
	 * Test check community item status pending.
	 */
	@Test
	public void testCheckCommunityItemStatusPending(){
		CommunityItem item = new CommunityItem("Once");
		item.setColumnName("Test 1");
		item.setPaid(70);
		marketPlaceDao.addCommunityItem(item, "00111");
		item = marketPlaceDao.getCurrentCommunityItem();
		assertEquals("Pending", marketPlaceDao.checkCommunityItemStatus(item));
	}
	
	/**
	 * Test check community item status activated.
	 */
	@Test
	public void testCheckCommunityItemStatusActivated(){
		CommunityItem item = new CommunityItem("Once");
		item.setColumnName("Test 1");
		item.setPaid(70);
		marketPlaceDao.addCommunityItem(item, "00111");
		item = marketPlaceDao.getCurrentCommunityItem();
		item.setPaid(805);
		marketPlaceDao.addCommunityItemPayment(item, "0011", item.getForeignId());
		assertEquals("Activated", marketPlaceDao.checkCommunityItemStatus(item));
	}
	
	/**
	 * Test check community item status refunded.
	 */
	@Test
	public void testCheckCommunityItemStatusRefunded(){
		CommunityItem item = new CommunityItem("Once");
		item.setColumnName("Test 1");
		item.setPaid(70);
		Setting setting = new Setting();
		setting.setName("community_item_wait");
		setting.setValue("-1");
		marketPlaceDao.updateSetting(setting);
		marketPlaceDao.addCommunityItem(item, "00111");
		item = marketPlaceDao.getCurrentCommunityItem();
		assertEquals("Refunded", marketPlaceDao.checkCommunityItemStatus(item));
	}
	
	/**
	 * Test get community item students.
	 */
	@Test 
	public void testGetCommunityItemStudents(){
		CommunityItem item = new CommunityItem("Once");
		item.setColumnName("Test 1");
		item.setPaid(70);
		marketPlaceDao.addCommunityItem(item, "00111");
		item = marketPlaceDao.getCurrentCommunityItem();
		marketPlaceDao.addCommunityItemPayment(item, "0011", item.getForeignId());
		List<Student> students = marketPlaceDao.getCommunityItemStudentsList(item.getForeignId());
		assertEquals(2, students.size());
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
			data = data.replace("course_id=\"" + courseId + "\"", "course_id=\"courseID\"");
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
	 * Update database info.
	 */
	private void updateDatabaseInfo() {
		File file = new File("./resources/dbUnitDataSet.xml");
		Scanner scan = null;
		try {
			scan = new Scanner(file);
			String data = scan.useDelimiter("\\Z").next();
			data = data.replace("course_id=\"courseID\"", "course_id=\"" + courseId + "\"");
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
