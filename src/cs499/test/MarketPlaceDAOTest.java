package cs499.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cs499.controllers.JSUBbDatabase;
import cs499.controllers.MarketPlaceDAO;
import cs499.itemHandler.Item;

import java.io.FileInputStream;
import java.sql.Connection;
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
		assertEquals("[]", marketPlaceDao.loadUnusedItems("0111").toString());
		assertEquals("[]", marketPlaceDao.loadNewPurchases("0111").toString());
	}
	

}
