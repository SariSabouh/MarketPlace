package cs499.dao;

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

public class DatabaseController {
	
	public List<Item> loadItems(){
		System.out.print("Load Items");
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
		        	item.setAmount(rSet.getInt("effect_magnitude"));
		        	item.setSupply(rSet.getInt("supply"));
		        	item.setType(AssessmentType.valueOf(rSet.getString("type")));
		        	item.setId(rSet.getInt("item_pk1"));
		        	System.out.println(item.toString());
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
		        	item.setAmount(rSet.getInt("effect_magnitude"));
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
	            insertQuery.setInt(5, (int)item.getAmount());
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
	        queryString.append("INSERT INTO dt_pruchaseinfo");
            queryString.append("(student_id, item_pk1, purchase_date, used_date, expiry_date, new) ");
            queryString.append(" VALUES (?, (select item_pk1 from dt_item where name = ?), ?, \'NOT_USED\', \'NA\', \'Y\') ");
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
	
	public void deletePurhcase(int studentID, String itemName) {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from dt_pruchaseinfo ");
            queryString.append("where student_id = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setInt(1, studentID);
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
	}

	public int loadNewPurchases(int studentID) {
		ConnectionManager cManager = null;
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        int allGold = 0;
        try {
			cManager = BbDatabase.getDefaultInstance().getConnectionManager();
	        conn = cManager.getConnection();
	        PreparedStatement selectQuery = null;
	        queryString.append("select cost from dt_item where item_pk1 in (");
            queryString.append("select item_pk1 from dt_purchaseinfo where new = \'Y\' and student_id = ?)");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setInt(1, studentID);
	        ResultSet rSet = selectQuery.executeQuery();
	        boolean notEmpty = false;
	        while(rSet.next()){
	        	allGold += rSet.getInt("cost");
	        	notEmpty = true;
	        }
	        if(notEmpty){
		        queryString = new StringBuffer("");
		        queryString.append("update dt_purchaseinfo ");
	            queryString.append("set new = \'N\' where new = \'Y\' and student_id = ?");
	            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		        selectQuery.setInt(1, studentID);
		        selectQuery.executeQuery();
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
        return allGold;
	}
}
