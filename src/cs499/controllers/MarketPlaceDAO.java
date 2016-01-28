package cs499.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import blackboard.platform.gradebook2.AttemptDetail;
import cs499.itemHandler.Item;
import cs499.itemHandler.Item.AssessmentType;
import cs499.itemHandler.Item.AttributeAffected;
import cs499.itemHandler.ItemController;
import cs499.util.GradebookColumnPojo;
import cs499.util.Setting;
import cs499.util.WaitListPojo;

/**
 * @author SabouhS
 * 
 * The Class MarketPlaceDAO. This is the class that controls the database
 * accessing and modifying of this blackboard module using blackboard's OpenDb
 */
public class MarketPlaceDAO {
	
	private boolean testing;
	
	private String courseId;
	
	private String instructorId;
	
	public MarketPlaceDAO(boolean testing, String courseId, String instructorId){
		this.testing = testing;
		this.courseId = courseId;
		this.instructorId = instructorId;
		System.out.println("Course Id:" + courseId);
        System.out.println("instructor Id:" + instructorId);
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
	        queryString.append("jsu_item where course_id = ? and instructor_id = ?");
	        selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, courseId);
	        selectQuery.setString(2, instructorId);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	if(!rSet.getString("name").equals("ITEM_INIT")){
		        	Item item = new Item(rSet.getString("name"));
		        	item.setAttributeAffected(AttributeAffected.valueOf(rSet.getString("attribute_affected")));
		        	item.setCost(rSet.getInt("cost"));
		        	item.setDuration(rSet.getString("duration"));
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
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
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
	        queryString.append("jsu_item ");
	        queryString.append("WHERE name = ? and course_id = ? and instructor_id = ?");
	        selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, itemName);
	        selectQuery.setString(2, courseId);
	        selectQuery.setString(3, instructorId);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	if(rSet.getString(2).equals("ITEM_INIT")){
	        		item = new Item(rSet.getString("name"));
	        	}
	        	else{
		        	item = new Item(rSet.getString("name"));
		        	item.setAttributeAffected(AttributeAffected.valueOf(rSet.getString("attribute_affected")));
		        	item.setCost(rSet.getInt("cost"));
		        	item.setDuration(rSet.getString("duration"));
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
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
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
		        queryString.append("INSERT INTO jsu_item");
	            queryString.append("(name, attribute_affected, cost, duration, effect_magnitude, supply, type, course_id, instructor_id ) ");
	            queryString.append(" VALUES (\'ITEM_INIT\', \'GRADE\', 0, 0, 0, 0, \'ALL\', ?, ?) ");
	            insertQuery = conn.prepareStatement(queryString.toString());
	            insertQuery.setString(1, courseId);
	            insertQuery.setString(2, instructorId);
	            insertQuery.executeUpdate();
		        for(Item item : itemContr.getItemList()){
		        	queryString = new StringBuffer("");
		            queryString.append("INSERT INTO jsu_item");
		            queryString.append("(name, attribute_affected, cost, duration, effect_magnitude, supply, type, course_id, instructor_id ) ");
		            queryString.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
		            insertQuery = conn.prepareStatement(queryString.toString());
		            insertQuery.setString(1, item.getName());
		            insertQuery.setString(2, item.getAttributeAffected().toString());
		            insertQuery.setInt(3, (int)item.getCost());
		            insertQuery.setInt(4, item.getDuration());
		            insertQuery.setInt(5, (int)item.getEffectMagnitude());
		            insertQuery.setInt(6, (int)item.getSupply());
		            insertQuery.setString(7, item.getType().toString());
		            insertQuery.setString(8, courseId);
		            insertQuery.setString(9, instructorId);
		            insertQuery.executeUpdate();
		        }
	            insertQuery.close();
	        } catch (java.sql.SQLException sE){
		    	sE.printStackTrace();
		    } finally {
		    	try {
					if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
				} catch (SQLException e) {
					e.printStackTrace();
				}
		    }
        setDefaultSettings();
        return itemContr.getItemList();
	}
	
	private void setDefaultSettings() {
		Connection conn = null;
	    StringBuffer queryString = new StringBuffer("");
	    try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("INSERT INTO jsu_settings");
	        queryString.append("(name, value, course_id, instructor_id) ");
	        queryString.append(" VALUES (\'visible_columns\', \'Y\', ?, ?) ");
	        insertQuery = conn.prepareStatement(queryString.toString());
	        insertQuery.setString(1, courseId);
	        insertQuery.setString(2, instructorId);
	        insertQuery.executeUpdate();
	        insertQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
	}
	
	public void updateSetting(Setting setting) {
		Connection conn = null;
	    StringBuffer queryString = new StringBuffer("");
	    try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("UPDATE jsu_settings ");
	        queryString.append("set value = ? where name = ? and course_id = ? and instructor_id = ?");
	        insertQuery = conn.prepareStatement(queryString.toString());
	        insertQuery.setString(1, setting.getValue());
	        insertQuery.setString(2, setting.getName());
	        insertQuery.setString(3, courseId);
	        insertQuery.setString(4, instructorId);
	        insertQuery.executeUpdate();
	        insertQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
	}
	
	public List<Setting> getDefaultSettings(){
		List<Setting> settings = new ArrayList<Setting>();
		Connection conn = null;
	    StringBuffer queryString = new StringBuffer("");
	    PreparedStatement selectQuery = null;
	    try {
			conn = JSUBbDatabase.getConnection(testing);
	        queryString.append("SELECT name, value from jsu_settings where course_id = ? and instructor_id = ?");
	        selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, courseId);
	        selectQuery.setString(2, instructorId);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	Setting setting = new Setting();
	        	setting.setName(rSet.getString("name"));
	        	setting.setValue(rSet.getString("value"));
	        	settings.add(setting);
	        }
	        rSet.close();
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
	    return settings;
	}
	
	public Setting getSetting(String name){
		Setting setting = new Setting();
		Connection conn = null;
	    StringBuffer queryString = new StringBuffer("");
	    PreparedStatement selectQuery = null;
	    try {
			conn = JSUBbDatabase.getConnection(testing);
	        queryString.append("SELECT * from jsu_settings where name = ? and course_id = ? and instructor_id = ?");
	        selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, name);
	        selectQuery.setString(2, courseId);
	        selectQuery.setString(3, instructorId);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	setting.setName(rSet.getString("name"));
	        	setting.setValue(rSet.getString("value"));
	        }
	        rSet.close();
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
	    return setting;
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
	        queryString.append("INSERT INTO jsu_purchaseinfo");
            queryString.append("(student_id, item_pk1, purchase_date, used_date, expiration_date, new, times_used, gradebook_column_name, course_id, instructor_id) ");
            queryString.append(" VALUES (?, (select item_pk1 from jsu_item where name = ? and course_id = ? and instructor_id = ?), ?, \'NOT_USED\', \'NA\', \'Y\', 0, \'NA\', ?, ?) ");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, studentID);
            insertQuery.setString(2, itemName);
            insertQuery.setString(3, courseId);
            insertQuery.setString(4, instructorId);
            insertQuery.setString(5, new DateTime().toString());
            insertQuery.setString(6, courseId);
            insertQuery.setString(7, instructorId);
            if(insertQuery.executeUpdate() > 0){
            	return true;
            }
            insertQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
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
	        queryString.append("delete from jsu_purchaseinfo where course_id = ? and instructor_id = ?");
	        insertQuery = conn.prepareStatement(queryString.toString());
	        insertQuery.setString(1, courseId);
	        insertQuery.setString(2, instructorId);
	        insertQuery.executeUpdate();
            insertQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        deleteWaitList();
        System.out.println("Deleted Purchase Info");
	}
	
	/**
	 * Deletes all @{link Item} from wait list.
	 * It is only called bye {@link #emptyDatabase()}
	 */
	private void deleteWaitList() {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from jsu_waitlist where course_id = ? and instructor_id = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, courseId);
            insertQuery.setString(2, instructorId);
            insertQuery.executeUpdate();
            insertQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        deleteItemList();
        System.out.println("Deleted Wait List");
	}
	
	/**
	 * Delete @{link Item} list from table.
	 * It is only called bye {@link #emptyDatabase()}
	 */
	private void deleteItemList() {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from jsu_item where course_id = ? and instructor_id = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, courseId);
            insertQuery.setString(2, instructorId);
            insertQuery.executeUpdate();
            insertQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        System.out.println("Deleted Item List");
        deleteGradebookColumn();
	}
	
	/**
	 * Truncates the GradebookColumn table.
	 * It is only called bye {@link #emptyDatabase()}
	 */
	private void deleteGradebookColumn() {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from jsu_gradebook where course_id = ? and instructor_id = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, courseId);
            insertQuery.setString(2, instructorId);
            insertQuery.executeUpdate();
            insertQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        System.out.println("Deleted Gradebook Columns");
        deleteSettings();
	}
	
	/**
	 * Truncates the Setting table.
	 * It is only called bye {@link #emptyDatabase()}
	 */
	private void deleteSettings() {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from jsu_settings where course_id = ? and instructor_id = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, courseId);
            insertQuery.setString(2, instructorId);
            insertQuery.executeUpdate();
            insertQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        System.out.println("Deleted Settings");
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
	        queryString.append("select name from jsu_item where item_pk1 in (");
            queryString.append("select item_pk1 from jsu_purchaseinfo where new = \'Y\' and student_id = ? and course_id = ? and instructor_id = ?) and course_id = ? and instructor_id = ?");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, studentID);
	        selectQuery.setString(2, courseId);
	        selectQuery.setString(3, instructorId);
	        selectQuery.setString(4, courseId);
	        selectQuery.setString(5, instructorId);
	        ResultSet rSet = selectQuery.executeQuery();
	        boolean notEmpty = false;
	        while(rSet.next()){
	        	String itemName = rSet.getString("name");
	        	itemList.add(itemName);
	        	notEmpty = true;
	        }
	        if(notEmpty){
		        queryString = new StringBuffer("");
		        queryString.append("update jsu_purchaseinfo ");
	            queryString.append("set new = \'N\' where new = \'Y\' and student_id = ? and course_id = ? and instructor_id = ?");
	            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		        selectQuery.setString(1, studentID);
		        selectQuery.setString(2, courseId);
		        selectQuery.setString(3, instructorId);
		        selectQuery.executeUpdate();
	        }
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
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
	public List<Item> loadNotExpiredItems(List<Item> items, String studentID) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        List<Item> itemList = new ArrayList<Item>();
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement selectQuery = null;
	        queryString.append("select a.name, b.expiration_date, b.times_used from jsu_item a, jsu_purchaseinfo b ");
            queryString.append("where b.student_id = ? and a.item_pk1 = b.item_pk1 and b.course_id = ? and b.instructor_id = ? and a.course_id = ? and a.instructor_id = ?");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, studentID);
	        selectQuery.setString(2, courseId);
	        selectQuery.setString(3, instructorId);
	        selectQuery.setString(4, courseId);
	        selectQuery.setString(5, instructorId);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	ItemController itemCont = new ItemController();
	        	String expirationString = rSet.getString("expiration_date");
	        	if(expirationString.equals("NA")){
	        		String itemName = rSet.getString("name");
		        	System.out.println("Item found: " + itemName);
		        	Item item = itemCont.getItemByName(items, itemName);
		        	item.setExpirationDate("NA");
		        	item.setTimesUsed(rSet.getInt("times_used"));
		        	itemList.add(item);
	        	}
	        	else{
	        		DateTime expirationDate = new DateTime(expirationString);
	        		if(expirationDate.isAfterNow()){
	        			String itemName = rSet.getString("name");
	    	        	System.out.println("Item found: " + itemName);
	    	        	Item item = itemCont.getItemByName(items, itemName);
			        	item.setExpirationDate(expirationString);
			        	item.setTimesUsed(rSet.getInt("times_used"));
			        	itemList.add(item);
	        		}
	        	}
	        }
	        selectQuery.close();
	        rSet.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
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
	public boolean expireInstantItem(String name, String studentID, String columnName) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update jsu_purchaseinfo ");
	        queryString.append("set used_date = ?, expiration_date = ?, times_used = times_used+1, gradebook_column_name = ? where item_pk1 = ( ");
	        queryString.append("select item_pk1 from jsu_item where name = ? and course_id = ? and instructor_id = ?) and student_id = ? and course_id = ? and instructor_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, new DateTime().toString());
            selectQuery.setString(2, new DateTime().toString());
            selectQuery.setString(3, columnName);
            selectQuery.setString(4, name);
            selectQuery.setString(5, courseId);
            selectQuery.setString(6, instructorId);
            selectQuery.setString(7, studentID);
            selectQuery.setString(8, courseId);
            selectQuery.setString(9, instructorId);
	        int rowsUpdated = selectQuery.executeUpdate();
	        if(rowsUpdated < 1){
	        	return false;
	        }
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        adjustWaitList(name, studentID, columnName);
		return true;
	}
	
	/**
	 * Adds the @{link Item} to wait list.
	 *
	 * @param name the @{link Item} name
	 * @param studentID the @{link Student} id
	 */
	private void adjustWaitList(String name, String studentID, String columnName){
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Inserting item in waiting list: " + studentID);
	        queryString.append("insert into jsu_waitlist(student_id, item_name, gradebook_column_name, course_id, instructor_id) ");
	        queryString.append("values(?, ?, ?, ?, ?)");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(2, name);
	        selectQuery.setString(3, columnName);
            selectQuery.setString(1, studentID);
            selectQuery.setString(4, courseId);
            selectQuery.setString(5, instructorId);
	        selectQuery.executeUpdate();
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
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
	        queryString.append("select * from jsu_waitlist where course_id = ? and instructor_id = ?");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, courseId);
            selectQuery.setString(2, instructorId);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	WaitListPojo waitList = new WaitListPojo();
	        	waitList.setName(rSet.getString("name"));
	        	waitList.setPrimaryKey(rSet.getInt("waitlist_pk1"));
	        	waitList.setStudentID(rSet.getString("student_id"));
	        	waitList.setColumnName(rSet.getString("column_name"));
	        	itemStudent.add(waitList);
	        }
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        return itemStudent;
	}

	/**
	 * Removes the @{link Item} from wait list.
	 *
	 * @param primaryKey the primary key of @{link Item} from jsu_purchaseinfo table
	 */
	public void removeItemWaitList(int primaryKey) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from jsu_waitlist ");
            queryString.append("where waitlist_pk1 = ? and course_id = ? and instructor_id = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setInt(1, primaryKey);
            insertQuery.setString(2, courseId);
            insertQuery.setString(3, instructorId);
            insertQuery.executeUpdate();
            insertQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
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
	public boolean updateItemUsage(String name, String studentID, String columnName) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
        	setLastUsedDate(name, studentID);
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update jsu_purchaseinfo ");
	        queryString.append("set times_used = times_used+1, gradebook_column_name = ? where item_pk1 = ( ");
	        queryString.append("select item_pk1 from jsu_item where name = ? and course_id = ? and instructor_id = ?) and student_id = ? and course_id = ? and instructor_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(2, name);
	        selectQuery.setString(1, columnName);
	        selectQuery.setString(3, courseId);
            selectQuery.setString(4, instructorId);
            selectQuery.setString(5, studentID);
            selectQuery.setString(6, courseId);
            selectQuery.setString(7, instructorId);
	        int rowsUpdated = selectQuery.executeUpdate();
	        if(rowsUpdated == 0){
	        	return false;
	        }
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        adjustWaitList(name, studentID, columnName);
		return true;
	}
	

	
	/**
	 * Sets the used date of the @{link Item}.
	 *
	 * @param name the @{link Item} name
	 * @param studentID the @{link Student} id
	 */
	private void setLastUsedDate(String name, String studentID) {
		Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update jsu_purchaseinfo ");
	        queryString.append("set used_date = ? where item_pk1 = ( ");
	        queryString.append("select item_pk1 from jsu_item where name = ? and course_id = ? and instructor_id = ?) and student_id = ? and course_id = ? and instructor_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, new DateTime().toString());
            selectQuery.setString(2, name);
            selectQuery.setString(3, courseId);
            selectQuery.setString(4, instructorId);
            selectQuery.setString(5, studentID);
            selectQuery.setString(6, courseId);
            selectQuery.setString(7, instructorId);
	        selectQuery.executeUpdate();
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
	}

	/**
	 * Checks if the Store is out of supply of the passed {@link Item}.
	 *
	 * @param item the @{link Item}
	 * @param studentID the @{link Student} id
	 * @return true, if is out of supply
	 */
	public boolean isOutOfSupply(Item item) {
		Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement selectQuery = null;
	        queryString.append("select count(*) as times from jsu_purchaseinfo where item_pk1 = (select item_pk1 from jsu_item where name = ? and course_id = ? and instructor_id = ?) and course_id = ? and instructor_id = ?");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, item.getName());
            selectQuery.setString(2, courseId);
            selectQuery.setString(3, instructorId);
            selectQuery.setString(4, courseId);
            selectQuery.setString(5, instructorId);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	if(item.getSupply() <= rSet.getInt("times")){
	        		return true;
	        	}
	        }
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
		return false;
	}

	/**
	 * Sets the used and expiry date for @{link Item}.
	 *
	 * @param item the item
	 * @param studentID the student id
	 */
	public void setUsedExpiryDate(Item item, String studentID) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update jsu_purchaseinfo ");
	        queryString.append("set used_date = ?, expiration_date = ? where item_pk1 = ( ");
	        queryString.append("select item_pk1 from jsu_item where name = ? and course_id = ? and instructor_id = ?) and student_id = ? and course_id = ? and instructor_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        DateTime date = new DateTime();
            selectQuery.setString(1, date.toString());
            selectQuery.setString(2, date.plusHours(item.getDuration()).toString());
            selectQuery.setString(3, item.getName());
            selectQuery.setString(4, courseId);
            selectQuery.setString(5, instructorId);
            selectQuery.setString(6, studentID);
            selectQuery.setString(7, courseId);
            selectQuery.setString(8, instructorId);
	        selectQuery.executeUpdate();
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    }		
	}
	
	/**
	 * Gets the {@link GradebookColumnPojo} from database by Name and StudentId
	 *
	 * @param title the title of the Gradebook Column
	 * @param studentID the student id
	 * 
	 * @return {@link GradebookColumnPojo}
	 */	
	public GradebookColumnPojo getGradebookColumnByNameAndStudentId(String title, String studentID){
		Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        PreparedStatement selectQuery = null;
        GradebookColumnPojo gradebook = null;
        try {
        	conn = JSUBbDatabase.getConnection(testing);
	        queryString.append("SELECT * ");
	        queryString.append("FROM ");
	        queryString.append("jsu_gradebook where student_id = ? and gradebook_column_name = ? and course_id = ? and instructor_id = ?");
	        selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, studentID);
	        selectQuery.setString(2, title);
	        selectQuery.setString(3, courseId);
	        selectQuery.setString(4, instructorId);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	gradebook = new GradebookColumnPojo();
	        	gradebook.setGrade(rSet.getInt("grade"));
	        	gradebook.setLastDate(rSet.getString("last_date"));
	        	gradebook.setName(rSet.getString("name"));
	        	gradebook.setStudentID(rSet.getString("student_id"));
	        }
	        rSet.close();
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	        try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        return gradebook;
	}

	/**
	 * Updates the {@link GradebookColumnPojo} in database
	 *
	 * @param attempt the AttemptDetail object
	 * @param studentID the student id
	 * 
	 * @return true if success
	 */
	public boolean updateGradebookColumn(AttemptDetail attempt, String studentID) {
		Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        PreparedStatement selectQuery = null;
        try {
        	conn = JSUBbDatabase.getConnection(testing);
        	queryString.append("update jsu_gradebook ");
	        queryString.append("set last_date = ?, grade = ? ");
	        queryString.append("where gradebook_column_name = ? and student_id = ? and course_id = ? and instructor_id = ?");
	        selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, new DateTime().toString());
	        selectQuery.setInt(2, (int) attempt.getScore());
	        selectQuery.setString(4, studentID);
	        selectQuery.setString(5, courseId);
	        selectQuery.setString(6, instructorId);
	        if(testing){
	        	selectQuery.setString(3, "TEST");
	        }
	        else{
	        	selectQuery.setString(3, attempt.getGradebookItem().getTitle());
	        }
	        if(selectQuery.executeUpdate() > 0){
	        	return true;
	        }
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	        try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
        return false;
	}
	
	/**
	 * Inserts a {@link GradebookColumnPojo} to the database
	 *
	 * @param attempt the AttemptDetail object
	 * @param studentID the student id
	 * 
	 */
	public void insertGradebookColumn(int grade, String gradeTitle, String studentID) {
		Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        PreparedStatement selectQuery = null;
        try {
        	conn = JSUBbDatabase.getConnection(testing);
        	queryString.append("insert into jsu_gradebook(gradebook_column_name, last_date, grade, student_id, course_id, instructor_id) ");
	        queryString.append("VALUES (?, ?, ?, ?, ?, ?)");
	        selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(2, new DateTime().toString());
	        selectQuery.setInt(3, grade);
	        selectQuery.setString(4, studentID);
	        selectQuery.setString(5, courseId);
	        selectQuery.setString(6, instructorId);
	        if(testing){
	        	selectQuery.setString(1, "TEST");
	        }
	        else{
	        	selectQuery.setString(1, gradeTitle);
	        }
	        selectQuery.executeUpdate();
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	        try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
	}
	
	/**
	 * Adds new {@link Item} to the Database.
	 * 
	 * @param {@link Item}
	 */
	public void addItem(Item item){
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("INSERT INTO jsu_item");
            queryString.append("(name, attribute_affected, cost, duration, effect_magnitude, supply, type, course_id, instructor_id ) ");
            queryString.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, item.getName());
            insertQuery.setString(2, item.getAttributeAffected().toString());
            insertQuery.setInt(3, (int)item.getCost());
            insertQuery.setInt(4, item.getDuration());
            insertQuery.setInt(5, (int)item.getEffectMagnitude());
            insertQuery.setInt(6, (int)item.getSupply());
            insertQuery.setString(7, item.getType().toString());
            insertQuery.setString(8, courseId);
            insertQuery.setString(9, instructorId);
            insertQuery.executeUpdate();
            insertQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
	}
	
	
	/**
	 * 
	 * Edits {@link Item} information and sets it to the Database.
	 * @param {@link Item}
	 */
	public void editItem(Item item){
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("UPDATE jsu_item ");
            queryString.append("set attribute_affected = ?, cost = ?, duration = ?, effect_magnitude = ?, supply = ?, type = ? ");
            queryString.append("where name = ? and course_id = ? and instructor_id = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(7, item.getName());
            insertQuery.setString(1, item.getAttributeAffected().toString());
            insertQuery.setInt(2, (int)item.getCost());
            insertQuery.setInt(3, item.getDuration());
            insertQuery.setInt(4, (int)item.getEffectMagnitude());
            insertQuery.setInt(5, (int)item.getSupply());
            insertQuery.setString(8, courseId);
            insertQuery.setString(9, instructorId);
            insertQuery.setString(6, item.getType().toString());
            insertQuery.executeUpdate();
            insertQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    } finally {
	    	try {
				if(!JSUBbDatabase.closeConnection(testing)){ conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
	    }
	}
	
}
