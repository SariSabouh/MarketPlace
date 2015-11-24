package cs499.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import blackboard.base.InitializationException;
import blackboard.db.BbDatabase;
import blackboard.db.ConnectionNotAvailableException;
import blackboard.db.DataStoreDescriptor;

public class JSUBbDatabase extends BbDatabase {

	protected JSUBbDatabase(DataStoreDescriptor arg0, Map<String, BbDatabase> arg1) throws InitializationException {
		super(arg0, arg1);
	}

	public static Connection getConnection(boolean testing) {
		if (testing) {
			try {
				return DriverManager.getConnection("jdbc:mysql://localhost:3306/marketplace", "root", "root");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				return  BbDatabase.getDefaultInstance().getConnectionManager().getConnection();
			} catch (ConnectionNotAvailableException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void closeConnection(boolean testing){
		if(!testing){
			BbDatabase.getDefaultInstance().getConnectionManager().close();
		}
	}
	
}
