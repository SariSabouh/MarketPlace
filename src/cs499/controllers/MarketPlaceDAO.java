package cs499.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import cs499.itemHandler.Item;
import cs499.itemHandler.Item.AssessmentType;
import cs499.itemHandler.Item.AttributeAffected;
import cs499.itemHandler.ItemController;
import cs499.util.WaitListPojo;

/**
 * @author SabouhS
 * 
 * The Class MarketPlaceDAO. This is the class that controls the database
 * accessing and modifying of this blackboard module using blackboard's OpenDb
 */
public class MarketPlaceDAO {
	
	boolean testing;
	
	public MarketPlaceDAO(boolean testing){
		this.testing = testing;
	}
	
	/**
	 * Load all @{link Item} from the database.
	 *
	 * @return the list of @{link Item}
	 */
	public List<Item> loadItems(){
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        PreparedStatement selectQuery = null;
        List<Item> itemList = new ArrayList<Item>();
        try {
        	conn = JSUBbDatabase.getConnection(testing);
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
		        	itemList.add(item);
	        	}
	        }
	        rSet.close();
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	        try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        return itemList;
	}
	
	/**
	 * Gets one specific @{link Item} from the database.
	 *
	 * @param itemName the @{link Item} name
	 * @return the @{link Item}
	 */
	public Item loadItem(String itemName){
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        PreparedStatement selectQuery = null;
        Item item = null; 
        try {
			conn = JSUBbDatabase.getConnection(testing);
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
	        	}
	        }
	        rSet.close();
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        return item;
	}
	
	/**
	 * Initilize database. If this is the very first time this is ran
	 * or if the database was cleared, it will load a starting list
	 * of items from a resource file in this project.
	 *
	 * @param content the content
	 * @return the list
	 */
	public List<Item> initilizeDatabase(String content){
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        ItemController itemContr = getDataSeed(content);
        try {
			conn = JSUBbDatabase.getConnection(testing);
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
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        return itemContr.getItemList();
	}
	
	/**
	 * Gets the data seed from the resource file and converts it
	 * to a list of @{link Item} in @{link ItemController}.
	 *
	 * @param content the content
	 * @return the data seed
	 */
	private ItemController getDataSeed(String content){
		ItemController itemContr = new ItemController();
		itemContr.createItemListFromContents(content);
		return itemContr;
	}

	/**
	 * Persist purhcase of @{link Item} in the database.
	 *
	 * @param studentID the {@link Student} id
	 * @param itemName the @{link Item} name
	 */
	public boolean persistPurhcase(String studentID, String itemName) {
		System.out.print("Persist Items");
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("INSERT INTO dt_purchaseinfo");
            queryString.append("(student_id, item_pk1, purchase_date, used_date, expiration_date, new, times_used) ");
            queryString.append(" VALUES (?, (select item_pk1 from dt_item where name = ?), ?, \'NOT_USED\', \'NA\', \'Y\', 0) ");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, studentID);
            insertQuery.setString(2, itemName);
            insertQuery.setString(3, new DateTime().toString());
            if(insertQuery.executeUpdate() > 0){
            	return true;
            }
            insertQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        return false;
	}
	
	/**
	 * Deletes all purchases from the table.
	 * NEVER use except if a clean slate is required.
	 * It also calls other delete methods which ends up in
	 * a complete truncation of all database tables for this module.
	 */
	public void emptyDatabase() { // VERY DANGEROUS METHOD REMOVES ALL ITEMS IN ALL TABLES IN DB USE WISELY
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from dt_purchaseinfo ");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.executeUpdate();
            insertQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        deleteWaitList();
        System.out.println("Deleted Purchase Info");
	}
	
	/**
	 * Deletes all @{link Item} from wait list.
	 * It is only called by {@link #deletePurhcases()}
	 */
	private void deleteWaitList() {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from dt_waitlist ");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.executeUpdate();
            insertQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        deleteItemList();
        System.out.println("Deleted Wait List");
	}
	
	/**
	 * Delete @{link Item} list from table.
	 * It is only called bye {@link #deletePurhcases()}
	 */
	private void deleteItemList() {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from dt_item ");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.executeUpdate();
            insertQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        System.out.println("Deleted Item List");
	}

	/**
	 * Load new purchases that were purchased while
	 * instructor was offline.
	 *
	 * @param studentID the {@link Student}
	 * @return the list
	 */
	public List<String> loadNewPurchases(String studentID) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        List<String> itemList = new ArrayList<String>();
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement selectQuery = null;
	        System.out.println("Updating gold for studentid: " + studentID);
	        
	        queryString.append("select name from dt_item where item_pk1 in (");
            queryString.append("select item_pk1 from dt_purchaseinfo where new = \'Y\' and student_id = ?)");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, studentID);
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
		        selectQuery.setString(1, studentID);
		        selectQuery.executeUpdate();
	        }
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        return itemList;
	}
	
	/**
	 * Load unused {@link Item}.
	 *
	 * @param studentID the student id
	 * @return the list
	 */
	public List<String> loadUnusedItems(String studentID) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        List<String> itemList = new ArrayList<String>();
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement selectQuery = null;
	        queryString.append("select name from dt_item where item_pk1 in (");
            queryString.append("select item_pk1 from dt_purchaseinfo where expiration_date = \'NA\' and student_id = ?)");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, studentID);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	String itemName = rSet.getString("name");
	        	System.out.println("Item found: " + itemName);
	        	itemList.add(itemName);
	        }
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        return itemList;
	}

	/**
	 * Expire @{link Item} that is only used once and expires.
	 *
	 * @param name the name of the @{link Item}
	 * @param studentID the @{link Student} id
	 * @return true, if successful
	 */
	public boolean expireInstantItem(String name, String studentID) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update dt_purchaseinfo ");
	        queryString.append("set used_date = ?, expiration_date = ?, times_used = times_used+1 where item_pk1 = ( ");
	        queryString.append("select item_pk1 from dt_item where name = ?) and student_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, new DateTime().toString());
            selectQuery.setString(2, new DateTime().toString());
            selectQuery.setString(3, name);
            selectQuery.setString(4, studentID);
	        int rowsUpdated = selectQuery.executeUpdate();
	        if(rowsUpdated < 1){
	        	return false;
	        }
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        addToWaitList(name, studentID);
		return true;
	}
	
	/**
	 * Expire @{link Item} that is continuous or passive.
	 *
	 * @param name the @{link Item} name
	 * @param studentID the @{link Student} id
	 * @return true, if successful
	 */
	private boolean expireItem(String name, String studentID) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update dt_purchaseinfo ");
	        queryString.append("set expiration_date = ?, times_used = times_used+1 where item_pk1 = ( ");
	        queryString.append("select item_pk1 from dt_item where name = ?) and student_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, new DateTime().toString());
            selectQuery.setString(2, name);
            selectQuery.setString(3, studentID);
	        int rowsUpdated = selectQuery.executeUpdate();
	        if(rowsUpdated == 0){
	        	return false;
	        }
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        addToWaitList(name, studentID);
		return true;
	}
	
	/**
	 * Adds the @{link Item} to wait list.
	 *
	 * @param name the @{link Item} name
	 * @param studentID the @{link Student} id
	 */
	private void addToWaitList(String name, String studentID){
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Inserting item in waiting list: " + studentID);
	        queryString.append("insert into dt_waitlist(student_id, name) ");
	        queryString.append("values(?, ?)");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(2, name);
            selectQuery.setString(1, studentID);
	        selectQuery.executeUpdate();
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
	}
	
	/**
	 * Load @{link Item} wait list.
	 *
	 * @return the list of @{link Item}
	 */
	public List<WaitListPojo> loadWaitList() {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        List<WaitListPojo> itemStudent = new ArrayList<WaitListPojo>();
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement selectQuery = null;
	        queryString.append("select * from dt_waitlist");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	WaitListPojo waitList = new WaitListPojo();
	        	waitList.setName(rSet.getString("name"));
	        	waitList.setPrimaryKey(rSet.getInt("waitlist_pk1"));
	        	waitList.setStudentID(rSet.getString("student_id"));
	        	itemStudent.add(waitList);
	        }
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        return itemStudent;
	}

	/**
	 * Removes the @{link Item} from wait list.
	 *
	 * @param primaryKey the primary key of @{link Item} from dt_purchaseinfo table
	 */
	public void removeItemWaitList(int primaryKey) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from dt_waitlist ");
            queryString.append("where waitlist_pk1 = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setInt(1, primaryKey);
            insertQuery.executeUpdate();
            insertQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        System.out.println("Item removed from wait list");
	}

	/**
	 * Increment the usage of the item in the database table
	 *
	 * @param name the @{link Item} name
	 * @param studentID the @{link Student} id
	 * @return true, if successful
	 */
	public boolean updateUsageItem(String name, String studentID) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update dt_purchaseinfo ");
	        queryString.append("set times_used = times_used+1 where item_pk1 = ( ");
	        queryString.append("select item_pk1 from dt_item where name = ?) and student_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, name);
            selectQuery.setString(2, studentID);
	        int rowsUpdated = selectQuery.executeUpdate();
	        if(rowsUpdated == 0){
	        	return false;
	        }
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        addToWaitList(name, studentID);
		return true;
	}

	/**
	 * Checks if the @{link Item} is out of supply.
	 *
	 * @param item the @{link Item}
	 * @param studentID the @{link Student} id
	 * @return true, if is out of supply
	 */
	public boolean isOutOfSupply(Item item, String studentID) {
		Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        boolean outOfSupply = false;
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement selectQuery = null;
	        queryString.append("select times_used from dt_purchaseinfo where name = ? and student_id = ?");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(2, studentID);
            selectQuery.setString(1, item.getName());
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	if(rSet.getInt("times_used") == 0){
	        		setUsedDate(item.getName(), studentID);
	        	}
	        	if(item.getSupply() <= rSet.getInt("times_used")){
	        		outOfSupply = true;
	        		expireItem(item.getName(), studentID);
	        	}
	        }
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
		return outOfSupply;
	}
	
	/**
	 * Sets the used date of the @{link Item}.
	 *
	 * @param name the @{link Item} name
	 * @param studentID the @{link Student} id
	 */
	private void setUsedDate(String name, String studentID) {
		Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update dt_purchaseinfo ");
	        queryString.append("set used_date = ? where item_pk1 = ( ");
	        queryString.append("select item_pk1 from dt_item where name = ?) and student_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, new DateTime().toString());
            selectQuery.setString(2, name);
            selectQuery.setString(3, studentID);
	        selectQuery.executeUpdate();
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
	}

	/**
	 * Checks if the @{link Item} is expired.
	 *
	 * @param item the item
	 * @param studentID the student id
	 * @return true, if is expired
	 */
	public boolean isExpired(Item item, String studentID) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        boolean expired = true;
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement selectQuery = null;
	        queryString.append("select times_used, expiration_date from dt_purchaseinfo where item_pk1 = (select item_pk1 from dt_item where name = ?) and student_id = ?");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(2, studentID);
            selectQuery.setString(1, item.getName());
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	if(rSet.getInt("times_used") == 0){
	        		setUsedExpiryDate(item, studentID);
	        	}
	        	String date = rSet.getString("expiration_date");
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
	    } finally {
	    	try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
		return expired;
	}

	/**
	 * Sets the used and expiry date for @{link Item}.
	 *
	 * @param item the item
	 * @param studentID the student id
	 */
	private void setUsedExpiryDate(Item item, String studentID) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update dt_purchaseinfo ");
	        queryString.append("set used_date = ?, expiration_date = ?, times_used = times_used + 1 where item_pk1 = ( ");
	        queryString.append("select item_pk1 from dt_item where name = ?) and student_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        DateTime date = new DateTime();
            selectQuery.setString(1, date.toString());
            selectQuery.setString(2, date.plusHours(item.getDuration()).toString());
            selectQuery.setString(3, item.getName());
            selectQuery.setString(4, studentID);
	        selectQuery.executeUpdate();
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	        try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }		
	}

	/**
	 * Update continuous @{link Item}.
	 *
	 * @param item the @{link Item}
	 * @param studentID the @{link Student} id
	 * @return true, if successful
	 */
	public boolean updateContinuousItem(Item item, String studentID) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update dt_purchaseinfo ");
	        queryString.append("set times_used = times_used+1 where item_pk1 = ( ");
	        queryString.append("select item_pk1 from dt_item where name = ?) and student_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, item.getName());
            selectQuery.setString(2, studentID);
	        int rowsUpdated = selectQuery.executeUpdate();
	        if(rowsUpdated == 0){
	        	return false;
	        }
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	        try {
				conn.close(); JSUBbDatabase.closeConnection(testing);
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        addToWaitList(item.getName(), studentID);
		return true;
	}
	
}
