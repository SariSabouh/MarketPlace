package cs499.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import blackboard.db.BbDatabase;
import blackboard.db.ConnectionManager;
import blackboard.db.ConnectionNotAvailableException;
import cs499.itemHandler.Item;
import cs499.itemHandler.Item.AssessmentType;
import cs499.itemHandler.Item.AttributeAffected;
import cs499.itemHandler.ItemController;
import cs499.util.WaitListPojo;

public class MarketPlaceDAO {
	
	public List<Item> loadItems(){
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        PreparedStatement selectQuery = null;
        List<Item> itemList = new ArrayList<Item>();
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        queryString.append("SELECT * ");
	        queryString.append("FROM ");
	        queryString.append("dt_item");
	        selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	if(!rSet.getString("name").equals("ITEM_INIT")){
		        	Item item = new Item(rSet.getString("name"));
		        	item.setAttributeAffected(AttributeAffected.valueOf(rSet.getString("attribute_affected")));
		        	item.setCost(rSet.getInt("cost"));
		        	item.setDuration(rSet.getInt("duration"));
		        	item.setEffectMagnitude(rSet.getInt("effect_magnitude"));
		        	item.setSupply(rSet.getInt("supply"));
		        	item.setType(AssessmentType.valueOf(rSet.getString("type")));
		        	item.setId(rSet.getInt("item_pk1"));
		        	itemList.add(item);
	        	}
	        }
	        rSet.close();
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
        return itemList;
	}
	
	public Item loadItem(String itemName){
		ConnectionManager cManager = null;	
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        PreparedStatement selectQuery = null;
        Item item = null; 
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        queryString.append("SELECT * ");
	        queryString.append("FROM ");
	        queryString.append("dt_item ");
	        queryString.append("WHERE name = ?");
	        selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, itemName);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	if(rSet.getString(2).equals("ITEM_INIT")){
	        		item = new Item(rSet.getString("name"));
	        	}
	        	else{
		        	item = new Item(rSet.getString("name"));
		        	item.setAttributeAffected(AttributeAffected.valueOf(rSet.getString("attribute_affected")));
		        	item.setCost(rSet.getInt("cost"));
		        	item.setDuration(rSet.getInt("duration"));
		        	item.setEffectMagnitude(rSet.getInt("effect_magnitude"));
		        	item.setSupply(rSet.getInt("supply"));
		        	item.setType(AssessmentType.valueOf(rSet.getString("type")));
		        	item.setId(rSet.getInt("item_pk1"));
	        	}
	        }
	        rSet.close();
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	System.out.println("SQL Excetpion" + sE.getMessage());
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	System.out.println("ConnectionNot" + cE.getFullMessageTrace());
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
        return item;
	}
	
	public List<Item> initilizeDatabase(String content){
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        ItemController itemContr = getDataSeed(content);
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        PreparedStatement insertQuery = null;
	        queryString.append("INSERT INTO dt_item");
            queryString.append("(name, attribute_affected, cost, duration, effect_magnitude, supply, type ) ");
            queryString.append(" VALUES (\'ITEM_INIT\', \'GRADE\', 0, 0, 0, 0, \'ALL\') ");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.executeUpdate();
	        for(Item item : itemContr.getItemList()){
	        	queryString = new StringBuffer("");
	            queryString.append("INSERT INTO dt_item");
	            queryString.append("(name, attribute_affected, cost, duration, effect_magnitude, supply, type ) ");
	            queryString.append(" VALUES (?, ?, ?, ?, ?, ?, ?) ");
	            insertQuery = conn.prepareStatement(queryString.toString());
	            insertQuery.setString(1, item.getName());
	            insertQuery.setString(2, item.getAttributeAffected().toString());
	            insertQuery.setInt(3, (int)item.getCost());
	            insertQuery.setInt(4, item.getDuration());
	            insertQuery.setInt(5, (int)item.getEffectMagnitude());
	            insertQuery.setInt(6, (int)item.getSupply());
	            insertQuery.setString(7, item.getType().toString());
	            insertQuery.executeUpdate();
	        }
            insertQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
        return itemContr.getItemList();
	}
	
	private ItemController getDataSeed(String content){
		ItemController itemContr = new ItemController();
		itemContr.createItemListFromContents(content);
		return itemContr;
	}

	public void persistPurhcase(int studentID, String itemName) {
		System.out.print("Persist Items");
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        PreparedStatement insertQuery = null;
	        queryString.append("INSERT INTO dt_purchaseinfo");
            queryString.append("(student_id, item_pk1, purchase_date, used_date, expiry_date, new, usage) ");
            queryString.append(" VALUES (?, (select item_pk1 from dt_item where name = ?), ?, \'NOT_USED\', \'NA\', \'Y\', 0) ");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setInt(1, studentID);
            insertQuery.setString(2, itemName);
            insertQuery.setString(3, new DateTime().toString());
            System.out.println("Before executing persistence");
            insertQuery.executeUpdate();
            System.out.println("After execution");
            insertQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }		
	}
	
	public void deletePurhcases() { // VERY DANGEROUS METHOD REMOVES ALL ITEMS IN ALL TABLES IN DB USE WISELY
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from dt_purchaseinfo ");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.executeUpdate();
            insertQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
        deleteWaitList();
        System.out.println("Deleted Purchase Info");
	}
	
	private void deleteWaitList() {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from dt_waitlist ");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.executeUpdate();
            insertQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
        deleteItemList();
        System.out.println("Deleted Wait List");
	}
	
	private void deleteItemList() {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from dt_item ");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.executeUpdate();
            insertQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
        System.out.println("Deleted Item List");
	}

	public List<String> loadNewPurchases(int studentID) {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        List<String> itemList = new ArrayList<String>();
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        PreparedStatement selectQuery = null;
	        System.out.println("Updating gold for studentid: " + studentID);
	        
	        queryString.append("select name from dt_item where item_pk1 in (");
            queryString.append("select item_pk1 from dt_purchaseinfo where new = \'Y\' and student_id = ?)");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setInt(1, studentID);
	        ResultSet rSet = selectQuery.executeQuery();
	        boolean notEmpty = false;
	        while(rSet.next()){
	        	String itemName = rSet.getString("name");
	        	System.out.println("Item found: " + itemName);
	        	itemList.add(itemName);
	        	notEmpty = true;
	        }
	        if(notEmpty){
		        queryString = new StringBuffer("");
		        queryString.append("update dt_purchaseinfo ");
	            queryString.append("set new = \'N\' where new = \'Y\' and student_id = ?");
	            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		        selectQuery.setInt(1, studentID);
		        selectQuery.executeUpdate();
	        }
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
        return itemList;
	}
	
	public List<String> loadUnusedItems(int studentID) {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        List<String> itemList = new ArrayList<String>();
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        PreparedStatement selectQuery = null;
	        queryString.append("select name from dt_item where item_pk1 in (");
            queryString.append("select item_pk1 from dt_purchaseinfo where expiry_date = \'NA\' and student_id = ?)");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setInt(1, studentID);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	String itemName = rSet.getString("name");
	        	System.out.println("Item found: " + itemName);
	        	itemList.add(itemName);
	        }
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
        return itemList;
	}

	public boolean expireInstantItem(String name, int studentID) {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update dt_purchaseinfo ");
	        queryString.append("set used_date = ?, expiry_date = ?, usage = usage+1 where item_pk1 = ( ");
	        queryString.append("select item_pk1 from dt_item where name = ?) and student_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, new DateTime().toString());
            selectQuery.setString(2, new DateTime().toString());
            selectQuery.setString(3, name);
            selectQuery.setInt(4, studentID);
	        int rowsUpdated = selectQuery.executeUpdate();
	        if(rowsUpdated == 0){
	        	return false;
	        }
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
        addToWaitList(name, studentID);
		return true;
	}
	
	private boolean expireItem(String name, int studentID) {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update dt_purchaseinfo ");
	        queryString.append("set expiry_date = ?, usage = usage+1 where item_pk1 = ( ");
	        queryString.append("select item_pk1 from dt_item where name = ?) and student_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, new DateTime().toString());
            selectQuery.setString(2, name);
            selectQuery.setInt(3, studentID);
	        int rowsUpdated = selectQuery.executeUpdate();
	        if(rowsUpdated == 0){
	        	return false;
	        }
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
        addToWaitList(name, studentID);
		return true;
	}
	
	private void addToWaitList(String name, int studentID){
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        System.out.println("Inserting item in waiting list: " + studentID);
	        queryString.append("insert into dt_waitlist(student_id, name) ");
	        queryString.append("values(?, ?)");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(2, name);
            selectQuery.setInt(1, studentID);
	        selectQuery.executeUpdate();
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
	}
	
	public List<WaitListPojo> loadWaitList() {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        List<WaitListPojo> itemStudent = new ArrayList<WaitListPojo>();
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        PreparedStatement selectQuery = null;
	        queryString.append("select * from dt_waitlist");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	WaitListPojo waitList = new WaitListPojo();
	        	waitList.setName(rSet.getString("name"));
	        	waitList.setPrimaryKey(rSet.getInt("waitlist_pk1"));
	        	waitList.setStudentID(rSet.getInt("student_id"));
	        	itemStudent.add(waitList);
	        }
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
        return itemStudent;
	}

	public void removeItemWaitList(int primaryKey) {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from dt_waitlist ");
            queryString.append("where waitlist_pk1 = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setInt(1, primaryKey);
            insertQuery.executeUpdate();
            insertQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
        System.out.println("Item removed from wait list");
	}

	public boolean updateUsageItem(String name, int studentID) {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update dt_purchaseinfo ");
	        queryString.append("set usage = usage+1 where item_pk1 = ( ");
	        queryString.append("select item_pk1 from dt_item where name = ?) and student_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, name);
            selectQuery.setInt(2, studentID);
	        int rowsUpdated = selectQuery.executeUpdate();
	        if(rowsUpdated == 0){
	        	return false;
	        }
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
        addToWaitList(name, studentID);
		return true;
	}

	public boolean isOutOfSupply(Item item, int studentID) {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        boolean outOfSupply = false;
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        PreparedStatement selectQuery = null;
	        queryString.append("select usage from dt_purchaseinfo where name = ? and student_id = ?");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setInt(2, studentID);
            selectQuery.setString(1, item.getName());
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	if(rSet.getInt("usage") == 0){
	        		setUsedDate(item.getName(), studentID);
	        	}
	        	if(item.getSupply() <= rSet.getInt("usage")){
	        		outOfSupply = true;
	        		expireItem(item.getName(), studentID);
	        	}
	        }
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
		return outOfSupply;
	}
	
	private void setUsedDate(String name, int studentID) {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update dt_purchaseinfo ");
	        queryString.append("set used_date = ? where item_pk1 = ( ");
	        queryString.append("select item_pk1 from dt_item where name = ?) and student_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, new DateTime().toString());
            selectQuery.setString(2, name);
            selectQuery.setInt(3, studentID);
	        selectQuery.executeUpdate();
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
	}

	public boolean isExpired(Item item, int studentID) {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        boolean expired = true;
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        PreparedStatement selectQuery = null;
	        queryString.append("select usage, expiry_date from dt_purchaseinfo where item_pk1 = (select item_pk1 from item where name = ?) and student_id = ?");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setInt(2, studentID);
            selectQuery.setString(1, item.getName());
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	if(rSet.getInt("usage") == 0){
	        		setUsedExpiryDate(item, studentID);
	        	}
	        	String date = rSet.getString("expiry_date");
	        	if(date.equals("NA")){
	        		expired = false;
	        	}
	        	else if(new DateTime(date).isAfterNow()){
	        		expired = false;
	        	}
	        	else{
	        		expired = true;
	        		expireItem(item.getName(), studentID);
	        	}
	        }
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
		return expired;
	}

	private void setUsedExpiryDate(Item item, int studentID) {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update dt_purchaseinfo ");
	        queryString.append("set used_date = ?, expiry_date = ?, usage = usage + 1 where item_pk1 = ( ");
	        queryString.append("select item_pk1 from dt_item where name = ?) and student_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        DateTime date = new DateTime();
            selectQuery.setString(1, date.toString());
            selectQuery.setString(2, date.plusHours(item.getDuration()).toString());
            selectQuery.setString(3, item.getName());
            selectQuery.setInt(4, studentID);
	        selectQuery.executeUpdate();
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }		
	}

	public boolean updateContinuousItem(Item item, int studentID) {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update dt_purchaseinfo ");
	        queryString.append("set usage = usage+1 where item_pk1 = ( ");
	        queryString.append("select item_pk1 from dt_item where name = ?) and student_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, item.getName());
            selectQuery.setInt(2, studentID);
	        int rowsUpdated = selectQuery.executeUpdate();
	        if(rowsUpdated == 0){
	        	return false;
	        }
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } catch (ConnectionNotAvailableException cE){
	    	cE.printStackTrace();;
	    } finally {
	        if(conn != null){
	            cManager.releaseConnection(conn);
	        }
	    }
        addToWaitList(item.getName(), studentID);
		return true;
	}
	
}
