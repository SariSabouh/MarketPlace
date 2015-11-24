package cs499.controllers;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import cs499.itemHandler.Item;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
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
        // initialize your database connection here
        IDatabaseConnection connection = getConnection();
        // ...

        // initialize your dataset here
        IDataSet dataSet = getDataSet();
        // ...

        try
        {
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        }
        finally
        {
            connection.close();
        }
    }
	
	@Test
	public void testLoadItemWithSpecificName() throws SQLException {
		marketPlaceDao = new MarketPlaceDAO(true);
		Item item = marketPlaceDao.loadItem("PickAxe");
		assertEquals("875", item.getCost());
	}

}
