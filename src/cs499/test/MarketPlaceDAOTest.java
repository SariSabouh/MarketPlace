package cs499.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import blackboard.platform.gradebook2.AttemptDetail;
import cs499.controllers.JSUBbDatabase;
import cs499.controllers.MarketPlaceDAO;
import cs499.itemHandler.Item;
import cs499.itemHandler.Item.AssessmentType;
import cs499.itemHandler.Item.AttributeAffected;
import cs499.util.Setting;

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

public class MarketPlaceDAOTest {

	private MarketPlaceDAO marketPlaceDao;
	
	private String courseId;

	protected IDatabaseConnection getConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
		Connection jdbcConnection = JSUBbDatabase.getConnection(true);
		return new DatabaseConnection(jdbcConnection);
	}
	
	protected IDataSet getDataSet() throws Exception {
		return new FlatXmlDataSet(new FileInputStream("./resources/dbUnitDataSet.xml"));
	}
	
	protected DatabaseOperation getSetUpOperation() throws Exception {
		return DatabaseOperation.REFRESH;
	}

	protected DatabaseOperation getTearDownOperation() throws Exception {
		return DatabaseOperation.NONE;
	}
	
	@Before
	public void setUp() throws Exception
    {
		courseId = "10_1";
        updateDatabaseInfo();
        IDatabaseConnection connection = getConnection();
        IDataSet dataSet = getDataSet();
        marketPlaceDao = new MarketPlaceDAO(true, courseId);
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        connection.close();
    }
	
	@Test
	public void testLoadItemWithSpecificName(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals("875.0", item.getCost() + "");
	}
	
	@Test(expected=NullPointerException.class)
	public void testLoadItemThatDoesNotExist(){
		Item item = marketPlaceDao.loadItem("UNO");
		assertEquals("UNO", item.getName());
	}
	
	@Test
	public void testLoadAllItems(){
		List<String> itemsList = new ArrayList<String>();
		for(Item item : marketPlaceDao.loadItems()){
			itemsList.add(item.getName());
		}
		assertEquals(itemsList.toString(), "[Once, Continuous, OnceTwo, OnceThree, Passive, SpecificOnly, SpecificNot]");
	}
	
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
	
	@Test
	public void testPersistPurchase(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
	}
	
	@Test(expected=NullPointerException.class)
	public void testPersistPurchaseWithWrongItemName(){
		Item item = marketPlaceDao.loadItem("UNO");
		assertEquals(false, marketPlaceDao.persistPurhcase("00111", item));
	}
	
	@Test
	public void testEmptyDatabase(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		assertEquals(7, marketPlaceDao.loadItems().size());
		marketPlaceDao.emptyDatabase();
		assertEquals(0, marketPlaceDao.loadItems().size());
	}	
	
	@Test
	public void testUnusedItems(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		List<Item> itemList = marketPlaceDao.loadItems();
		assertEquals("Once", marketPlaceDao.loadNotExpiredItems(itemList, "00111").get(0).getName());
	}
	
	@Test
	public void testLoadUnusedItemsThatAreUsed(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		marketPlaceDao.expireInstantItem("Once", "00111", "Test");
		List<Item> itemList = marketPlaceDao.loadItems();
		assertEquals("[]", marketPlaceDao.loadNotExpiredItems(itemList, "00111").toString());
	}
	
	@Test
	public void testLoadUnusedItemsThatArePassive(){
		Item item = marketPlaceDao.loadItem("Passive");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		assertEquals(true, marketPlaceDao.updateItemUsage("Passive", "00111", ("Test")));
		List<Item> itemList = marketPlaceDao.loadItems();
		assertEquals("Passive", marketPlaceDao.loadNotExpiredItems(itemList, "00111").get(0).getName());
	}

	@Test
	public void testUnusedItemsWithWrongStudentId(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		List<Item> itemList = marketPlaceDao.loadItems();
		assertEquals("[]", marketPlaceDao.loadNotExpiredItems(itemList, "0111").toString());
	}
	
	@Test
	public void testExpireInstantItem(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		assertEquals(true, marketPlaceDao.expireInstantItem("Once", "00111", "Test"));
		List<Item> itemList = marketPlaceDao.loadItems();
		assertEquals("[]", marketPlaceDao.loadNotExpiredItems(itemList, "00111").toString());
	}
	
	@Test
	public void testExpireInstantItemWithWrongName(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		assertEquals(true, marketPlaceDao.expireInstantItem("UNO", "00111", "Test"));
		List<Item> itemList = marketPlaceDao.loadItems();
		assertEquals("Once", marketPlaceDao.loadNotExpiredItems(itemList, "00111").get(0).getName());
	}
	
	@Test
	public void testUpdateItemUsage(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("001111", item));
		assertEquals(true, marketPlaceDao.updateItemUsage("Once", "001111", "Test"));
	}
	
	@Test(expected=NullPointerException.class)
	public void testUpdateItemUsageWithWrongName(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		marketPlaceDao.updateItemUsage("UNO", "00111", "Test");
	}
	
	@Test
	public void testNotOfSupply() throws SQLException{
		Item item = new Item("Once");
		item.setSupply(5);
		assertEquals(false, marketPlaceDao.isOutOfSupply(item));
	}
	
	@Test
	public void testIsOutOfSupplyWithOnePurchase(){
		Item item = new Item("Once");
		item.setCost(100);
		item.setSupply(1);
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", item));
		assertEquals(true, marketPlaceDao.isOutOfSupply(item));
	}
	
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
	
	@Test
	public void testGetGradebookColumnAfterInsert(){
		marketPlaceDao.insertGradebookColumn(10, "TEST", "00111");
		assertEquals(10, marketPlaceDao.getGradebookColumnByNameAndStudentId("TEST", "00111").getGrade());
	}
	
	@Test
	public void testUpdateGradebookColumnAfterInsert(){
		AttemptDetail attempt = new AttemptDetail();
		attempt.setScore(10);
		marketPlaceDao.insertGradebookColumn(10, "TEST", "00111");
		assertEquals(true, marketPlaceDao.updateGradebookColumn(attempt, "00111"));
	}
	
	@Test
	public void testUpdateSetting(){
		Setting setting = new Setting();
		setting.setName("visible_columns");
		setting.setValue("N");
		marketPlaceDao.updateSetting(setting);
		setting = marketPlaceDao.getSetting("visible_columns");
		assertEquals("N", setting.getValue());
	}
	
	@Test
	public void testGetSetting(){
		Setting setting = marketPlaceDao.getSetting("visible_columns");
		assertEquals("Y", setting.getValue());
	}
	
	@Test
	public void testGetDefaultSetting(){
		List<Setting> settings = marketPlaceDao.getDefaultSettings();
		assertEquals("Y", settings.get(0).getValue());
		assertEquals("visible_columns", settings.get(0).getName());
	}
	
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
	
	@Test
	public void testEditItem(){
		Item item = marketPlaceDao.loadItem("Once");
		assertEquals("875.0", item.getCost() + "");
		item.setCost(999);
		marketPlaceDao.editItem(item);
		item = marketPlaceDao.loadItem("Once");
		assertEquals("999.0", item.getCost() + "");
	}
	
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
