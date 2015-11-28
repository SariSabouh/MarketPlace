package cs499.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cs499.controllers.JSUBbDatabase;
import cs499.controllers.MarketPlaceDAO;
import cs499.itemHandler.Item;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;

public class MarketPlaceDAOTest {

	private MarketPlaceDAO marketPlaceDao;

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
        IDatabaseConnection connection = getConnection();
        IDataSet dataSet = getDataSet();
        marketPlaceDao = new MarketPlaceDAO(true);
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
		assertEquals(itemsList.toString(), "[Once, Continuous, OnceTwo]");
	}
	
	@Test
	public void testInitializeItems(){
		String content ="--\nname=Twice\ncost=875\nduration=ONCE\ntype=EXAMS\nattAffected=DUEDATE\nsupply=1"
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
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
	}
	
	@Test
	public void testPersistPurchaseWithWrongItemName(){
		assertEquals(false, marketPlaceDao.persistPurhcase("00111", "UNO"));
	}
	
	@Test
	public void testLoadingNewPurchases(){
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals("[Once]", marketPlaceDao.loadNewPurchases("00111").toString());
	}
	
	@Test
	public void testLoadingNewPurchasesWithWrongStudentId(){
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals("[]", marketPlaceDao.loadNewPurchases("111").toString());
	}
	
	@Test
	public void testEmptyDatabase(){
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		marketPlaceDao.emptyDatabase();
		assertEquals(0, marketPlaceDao.loadItems().size());
		assertEquals(0, marketPlaceDao.loadNewPurchases("00111").size());
	}	
	
	@Test
	public void testUnusedItems(){
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals("[Once]", marketPlaceDao.loadUnusedItems("00111").toString());
	}

	@Test
	public void testUnusedItemsWithWrongStudentId(){
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals("[]", marketPlaceDao.loadUnusedItems("0111").toString());
	}
	
	@Test
	public void testExpireInstantItem(){
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals(true, marketPlaceDao.expireInstantItem("Once", "00111"));
		assertEquals("[]", marketPlaceDao.loadUnusedItems("00111").toString());
	}
	
	@Test
	public void testExpireInstantItemWithWrongName(){
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals(false, marketPlaceDao.expireInstantItem("UNO", "00111"));
		assertEquals("[Once]", marketPlaceDao.loadUnusedItems("00111").toString());
	}
	
	@Test
	public void testLoadWaitListWithItems(){
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals(true, marketPlaceDao.expireInstantItem("Once", "00111"));
		assertEquals("Once", marketPlaceDao.loadWaitList().get(0).getName());
		assertEquals("00111", marketPlaceDao.loadWaitList().get(0).getStudentID());
	}
	
	@Test
	public void testLoadWaitListWithoutItems(){
		assertEquals(0, marketPlaceDao.loadWaitList().size());
		assertEquals(0, marketPlaceDao.loadWaitList().size());
	}
	
	@Test
	public void testRemoveFromWaitList() throws SQLException{
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals(true, marketPlaceDao.expireInstantItem("Once", "00111"));
		int primaryKey = marketPlaceDao.loadWaitList().get(0).getPrimaryKey();
		marketPlaceDao.removeItemWaitList(primaryKey);
		assertEquals(0, marketPlaceDao.loadWaitList().size());
	}
	
	@Test
	public void testRemoveFromWaitListWithWrongPrimarykey(){
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals(true, marketPlaceDao.expireInstantItem("Once", "00111"));
		marketPlaceDao.removeItemWaitList(1252134123);
		assertEquals(1, marketPlaceDao.loadWaitList().size());
	}
	
	@Test
	public void testUpdateItemUsage(){
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals(true, marketPlaceDao.updateUsageItem("Once", "00111"));
	}
	
	@Test
	public void testUpdateItemUsageWithWrongName(){
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals(false, marketPlaceDao.updateUsageItem("UNO", "00111"));
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
		item.setSupply(1);
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals(true, marketPlaceDao.isOutOfSupply(item));
	}
	
	@Test
	public void testIsOutOfSupplyWithMultiplePurchase(){
		Item item = new Item("Once");
		item.setSupply(3);
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals(true, marketPlaceDao.isOutOfSupply(item));
	}
	
	@Test
	public void testIsNotExpiredWithNoUsage(){
		Item item = new Item("Once");
		item.setDuration(20);
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Once"));
		assertEquals(false, marketPlaceDao.isExpired(item, "00111"));
		
	}
	
	@Test
	public void testIsNotExpiredWithUsage(){
		Item item = new Item("Continuous");
		item.setDuration(20);
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Continuous"));
		assertEquals(true, marketPlaceDao.updateUsageItem("Continuous", "00111"));
		assertEquals(false, marketPlaceDao.isExpired(item, "00111"));
	}
	
	@Test
	public void testIsNotExpiredWithDate(){
		Item item = new Item("Continuous");
		item.setDuration(20);
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Continuous"));
		assertEquals(true, marketPlaceDao.updateUsageItem("Continuous", "00111"));
		assertEquals(false, marketPlaceDao.isExpired(item, "00111"));
		assertEquals(false, marketPlaceDao.isExpired(item, "00111"));
	}
	
	@Test
	public void testIsExpired(){
		Item item = new Item("Continuous");
		item.setDuration(-20);
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Continuous"));
		assertEquals(false, marketPlaceDao.isExpired(item, "00111"));
		assertEquals(true, marketPlaceDao.isExpired(item, "00111"));
	}
	
	@Test
	public void testUpdateContinuousItem(){
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Continuous"));
		assertEquals(0, marketPlaceDao.loadWaitList().size());
		assertEquals(true, marketPlaceDao.updateContinuousItem("Continuous", "00111"));
		assertEquals("Continuous", marketPlaceDao.loadWaitList().get(0).getName());
		assertEquals("00111", marketPlaceDao.loadWaitList().get(0).getStudentID());
	}
	
	@Test
	public void testUpdateWrongContinuousItem(){
		assertEquals(true, marketPlaceDao.persistPurhcase("00111", "Continuous"));
		assertEquals(0, marketPlaceDao.loadWaitList().size());
		assertEquals(false, marketPlaceDao.updateContinuousItem("Conti", "00111"));
		assertEquals(0, marketPlaceDao.loadWaitList().size());

	}
	
	

}
