package cs499.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import blackboard.base.InitializationException;
import blackboard.db.BbDatabase;
import blackboard.db.ConnectionManager;
import blackboard.db.ConnectionNotAvailableException;
import blackboard.db.DataStoreDescriptor;

public class JSUBbDatabase extends BbDatabase {

	protected JSUBbDatabase(DataStoreDescriptor arg0, Map<String, BbDatabase> arg1) throws InitializationException {
		super(arg0, arg1);
	}
	
	static ConnectionManager cManager;

	public static Connection getConnection(boolean testing) {
		if (testing) {
			try {
				return DriverManager.getConnection("jdbc:mysql://localhost:3306/marketplace", "root", "root");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
//				System.out.println("Open Method: " + Thread.currentThread().getStackTrace()[2].getMethodName() + " class: " + Thread.currentThread().getStackTrace()[2].getClassName() + " line: " + Thread.currentThread().getStackTrace()[2].getLineNumber());
//				System.out.println("Cont: " + Thread.currentThread().getStackTrace()[3].getMethodName() + " class: " + Thread.currentThread().getStackTrace()[3].getClassName() + " line: " + Thread.currentThread().getStackTrace()[3].getLineNumber());
				cManager = BbDatabase.getDefaultInstance().getConnectionManager();
				cManager.cleanPinnedConnections();
				cManager.cleanUnreleasedConnections();
				return  cManager.getConnection();
			} catch (ConnectionNotAvailableException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static boolean closeConnection(boolean testing){
		if(!testing){
//			System.out.println("Close Method: " + Thread.currentThread().getStackTrace()[2].getMethodName());
			cManager.close();
			return true;
		}
		return false;
	}
	
}
