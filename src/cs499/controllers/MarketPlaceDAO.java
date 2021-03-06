package cs499.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import blackboard.platform.gradebook2.AttemptDetail;
import cs499.object.CommunityItem;
import cs499.object.GradebookColumnPojo;
import cs499.object.Item;
import cs499.object.Setting;
import cs499.object.Student;
import cs499.object.Item.AssessmentType;
import cs499.object.Item.AttributeAffected;
import cs499.util.ItemController;

/**
 * The Class MarketPlaceDAO.
 *
 * @author SabouhS
 * 
 * The Class MarketPlaceDAO. This is the class that controls the database
 * accessing and modifying of this blackboard module using blackboard's OpenDb
 */
public class MarketPlaceDAO {
	
	/** The testing boolean that defines if this is in testing state or not. */
	private boolean testing;
	
	/** The course id. */
	private String courseId;
	
	
	/**
	 * Instantiates a new Market Place Dao.
	 *
	 * @param testing the testing
	 * @param courseId the course id
	 */
	public MarketPlaceDAO(boolean testing, String courseId){
		this.testing = testing;
		this.courseId = courseId;
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
	            queryString.append("(name, attribute_affected, cost, duration, effect_magnitude, supply, type, course_id) ");
	            queryString.append(" VALUES (\'ITEM_INIT\', \'GRADE\', 0, 0, 0, 0, \'ALL\', ?) ");
	            insertQuery = conn.prepareStatement(queryString.toString());
	            insertQuery.setString(1, courseId);
	            insertQuery.executeUpdate();
		        for(Item item : itemContr.getItemList()){
		        	queryString = new StringBuffer("");
		            queryString.append("INSERT INTO jsu_item");
		            queryString.append("(name, attribute_affected, cost, duration, effect_magnitude, supply, type, course_id) ");
		            queryString.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?) ");
		            insertQuery = conn.prepareStatement(queryString.toString());
		            insertQuery.setString(1, item.getName());
		            insertQuery.setString(2, item.getAttributeAffected().toString());
		            insertQuery.setInt(3, (int)item.getCost());
		            insertQuery.setInt(4, item.getDuration());
		            insertQuery.setInt(5, (int)item.getEffectMagnitude());
		            insertQuery.setInt(6, (int)item.getSupply());
		            insertQuery.setString(7, item.getType().toString());
		            insertQuery.setString(8, courseId);
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
	        queryString.append("jsu_item where course_id = ?");
	        selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, courseId);
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
		        	item.setSpecific(rSet.getString("specific_column"));
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
	        queryString.append("WHERE name = ? and course_id = ?");
	        selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, itemName);
	        selectQuery.setString(2, courseId);
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
		        	item.setSpecific(rSet.getString("specific_column"));
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
	 * Sets the default @{link Setting}
	 */
	private void setDefaultSettings() {
		Connection conn = null;
	    StringBuffer queryString = new StringBuffer("");
	    try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("INSERT INTO jsu_settings");
	        queryString.append("(name, value, course_id) ");
	        queryString.append(" VALUES (\'visible_columns\', \'N\', ?) ");
	        insertQuery = conn.prepareStatement(queryString.toString());
	        insertQuery.setString(1, courseId);
	        insertQuery.executeUpdate();
	        queryString = new StringBuffer("");
	        queryString.append("INSERT INTO jsu_settings");
	        queryString.append("(name, value, course_id) ");
	        queryString.append(" VALUES (\'community_item_wait\', \'7\', ?) ");
	        insertQuery = conn.prepareStatement(queryString.toString());
	        insertQuery.setString(1, courseId);
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
	 * Update setting.
	 *
	 * @param setting the @{link Setting}
	 */
	public void updateSetting(Setting setting) {
		Connection conn = null;
	    StringBuffer queryString = new StringBuffer("");
	    try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("UPDATE jsu_settings ");
	        queryString.append("set value = ? where name = ? and course_id = ?");
	        insertQuery = conn.prepareStatement(queryString.toString());
	        insertQuery.setString(1, setting.getValue());
	        insertQuery.setString(2, setting.getName());
	        insertQuery.setString(3, courseId);
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
	 * Gets the default @{link Setting}
	 *
	 * @return the default settings
	 */
	public List<Setting> getDefaultSettings(){
		List<Setting> settings = new ArrayList<Setting>();
		Connection conn = null;
	    StringBuffer queryString = new StringBuffer("");
	    PreparedStatement selectQuery = null;
	    try {
			conn = JSUBbDatabase.getConnection(testing);
	        queryString.append("SELECT name, value from jsu_settings where course_id = ?");
	        selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, courseId);
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
	
	/**
	 * Gets the @{link Setting}
	 *
	 * @param name the name
	 * @return the @{link Setting}
	 */
	public Setting getSetting(String name){
		Setting setting = new Setting();
		Connection conn = null;
	    StringBuffer queryString = new StringBuffer("");
	    PreparedStatement selectQuery = null;
	    try {
			conn = JSUBbDatabase.getConnection(testing);
	        queryString.append("SELECT * from jsu_settings where name = ? and course_id = ?");
	        selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, name);
	        selectQuery.setString(2, courseId);
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
	 * Persist purhcase of @{link Item} in the database.
	 *
	 * @param studentID the {@link Student} id
	 * @param item the item
	 * @return true, if successful
	 */
	public boolean persistPurhcase(String studentID, Item item) {
		System.out.print("Persist Items ");
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("INSERT INTO jsu_purchaseinfo");
            queryString.append("(student_id, item_pk1, purchase_date, purchase_cost, course_id) ");
            queryString.append(" VALUES (?, (select item_pk1 from jsu_item where name = ? and course_id = ?), ?, ?, ?) ");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, studentID);
            insertQuery.setString(2, item.getName());
            insertQuery.setString(3, courseId);
            insertQuery.setString(4, new DateTime().toString());
            insertQuery.setInt(5, (int) item.getCost());
            insertQuery.setString(6, courseId);
            if(insertQuery.executeUpdate() > 0){
            	persistInfo(studentID, item);
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
	 * Persist info.
	 *
	 * @param studentID the student id
	 * @param item the item
	 * @return true, if successful
	 */
	private boolean persistInfo(String studentID, Item item) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
        	String getPurInfoRow = "";
        	if(testing){
        		getPurInfoRow = "select purchase_info_pk1 from jsu_purchaseinfo where item_pk1 = (select item_pk1 from jsu_item where name = ? and course_id = ?) and student_id = ? and course_id = ? order by purchase_info_pk1 desc limit 1))";
        	}
        	else{
        		getPurInfoRow = "select max(purchase_info_pk1) from jsu_purchaseinfo where item_pk1 = (select item_pk1 from jsu_item where name = ? and course_id = ?) and student_id = ? and course_id = ?))";
        	}
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("insert into jsu_item_use_info (student_id, item_pk1, used_date, expiration_date, gradebook_column_name, course_id, purchase_info_pk1) ");
		    queryString.append("values(?, (select item_pk1 from jsu_item where name = ? and course_id = ?), \'NA\', \'NA\', \'NA\', ?, (");
		    queryString.append(getPurInfoRow);
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, studentID);
            insertQuery.setString(2, item.getName());
            insertQuery.setString(3, courseId);
            insertQuery.setString(4, courseId);
            insertQuery.setString(5, item.getName());
            insertQuery.setString(6, courseId);
            insertQuery.setString(7, studentID);
            insertQuery.setString(8, courseId);
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
        deleteItemUseInfo();
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from jsu_purchaseinfo where course_id = ?");
	        insertQuery = conn.prepareStatement(queryString.toString());
	        insertQuery.setString(1, courseId);
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
        deleteCommunityItemInfo();
        deleteItemList();
        System.out.println("Database Truncated");
	}
	
	/**
	 * Delete @{link Item} list from table.
	 * It is only called bye {@link #emptyDatabase()}
	 */
	private void deleteItemUseInfo() {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from jsu_item_use_info where course_id = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, courseId);
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
	 * Delete @{link Item} list from table.
	 * It is only called bye {@link #emptyDatabase()}
	 */
	private void deleteItemList() {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from jsu_item where course_id = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, courseId);
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
	        queryString.append("delete from jsu_gradebook where course_id = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, courseId);
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
	        queryString.append("delete from jsu_settings where course_id = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, courseId);
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
	 * Truncates the Community Item Info table.
	 * It is only called bye {@link #emptyDatabase()}
	 */
	private void deleteCommunityItemInfo() {
		deleteCommunityItemUsage();
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from jsu_community_item_info where course_id = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, courseId);
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
	 * Truncates the Community Item Usage table.
	 * It is only called bye {@link #emptyDatabase()}
	 */
	private void deleteCommunityItemUsage() {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("delete from jsu_community_item_usage where course_id = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, courseId);
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
	 * Load unused {@link Item}.
	 *
	 * @param items the items
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
	        queryString.append("SELECT jsu_item.name, jsu_item_use_info.expiration_date, jsu_item_use_info.times_used ");
	        queryString.append("FROM jsu_item, jsu_item_use_info ");
	        queryString.append("where jsu_item.item_pk1 = jsu_item_use_info.item_pk1 and jsu_item.course_id = jsu_item_use_info.course_id ");
	        queryString.append("and jsu_item_use_info.student_id = ? and jsu_item.item_pk1 = jsu_item_use_info.item_pk1 and jsu_item.course_id = ? and jsu_item_use_info.course_id = ?");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, studentID);
	        selectQuery.setString(2, courseId);
	        selectQuery.setString(3, courseId);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	ItemController itemCont = new ItemController();
	        	String expirationString = rSet.getString("expiration_date");
	        	String itemName = rSet.getString("name");
	        	System.out.println(itemName + " date " + expirationString);
	        	if(expirationString == null){
	        		expirationString = "NA";
	        	}
	        	if(expirationString.equals("NA")){
		        	System.out.println("Item found: " + itemName);
		        	Item item = itemCont.getItemByName(items, itemName);
		        	item.setExpirationDate("NA");
		        	item.setTimesUsed(rSet.getInt("times_used"));
		        	itemList.add(item);
	        	}
	        	else{
	        		DateTime expirationDate = new DateTime(expirationString);
	        		if(expirationDate.isAfterNow()){
	    	        	System.out.println("Item found: " + itemName);
	    	        	Item item = itemCont.getItemByName(items, itemName);
			        	item.setExpirationDate(expirationString);
			        	item.setTimesUsed(rSet.getInt("times_used"));
			        	itemList.add(item);
	        		}
	        		else{
	        			Item item = itemCont.getItemByName(items, itemName);
	        			if(item.getDuration() != 0){
		    	        	System.out.println("Expired Continuous Item found: " + itemName);
				        	item.setExpirationDate(expirationString);
				        	item.setTimesUsed(rSet.getInt("times_used"));
				        	itemList.add(item);
	        			}
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
	 * @param columnName the column name
	 * @return true, if successful
	 */
	public boolean expireInstantItem(String name, String studentID, String columnName) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("UPDATE jsu_item_use_info");
            queryString.append(" set times_used = 1, gradebook_column_name = ?, used_date = ?, expiration_date = ?");
            queryString.append(" where student_id = ? and item_pk1 = (select item_pk1 from jsu_item where name = ? and course_id = ?) and course_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, columnName);
            selectQuery.setString(2, new DateTime().toString());
            selectQuery.setString(3, new DateTime().toString());
	        selectQuery.setString(4, studentID);
	        selectQuery.setString(5, name);
	        selectQuery.setString(6, courseId);
            selectQuery.setString(7, courseId);
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
        System.out.println("Expired from database");
		return true;
	}
	
	/**
	 * Increment the usage of the item in the database table.
	 *
	 * @param name the @{link Item} name
	 * @param studentID the @{link Student} id
	 * @param columnName the column name
	 * @return true, if successful
	 */
	public boolean updateItemUsage(String name, String studentID, String columnName) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
        	if(!testing){
        		setLastUsedDate(name, studentID);
        	}
        	else if((name.equals("Passive") || studentID.equals("001111"))&& testing){
        		setLastUsedDate(name, studentID);
        	}
        	List<Item> items = new ArrayList<Item>();
        	Item item = new Item(name);
        	items.add(item);
        	int timesUsed = loadNotExpiredItems(items, studentID).get(0).getTimesUsed();
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update jsu_item_use_info ");
	        queryString.append("set times_used = ?, gradebook_column_name = ? where item_pk1 = ( ");
	        queryString.append("select item_pk1 from jsu_item where name = ? and course_id = ?) and student_id = ? and course_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setInt(1, timesUsed+1);
	        selectQuery.setString(2, columnName);
	        selectQuery.setString(3, name);
	        selectQuery.setString(4, courseId);
            selectQuery.setString(5, studentID);
            selectQuery.setString(6, courseId);
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
	        if(testing){
	        	queryString = getLastUsedTestQuery(name, studentID);
	        }
	        else{
		        queryString.append("if exists (select * from jsu_item_use_info where item_pk1 = ( ");
		        queryString.append("select item_pk1 from jsu_item where name = ? and course_id = ?) and course_id = ? and student_id = ?) ");
		        queryString.append("update jsu_item_use_info ");
		        queryString.append("set used_date = ? where item_pk1 = ( ");
		        queryString.append("select item_pk1 from jsu_item where name = ? and course_id = ?) and student_id = ? and course_id = ?");
		        queryString.append("ELSE insert into jsu_item_use_info (student_id, item_pk1, used_date, gradebook_column_name, course_id) ");
		        queryString.append("values(?, (select item_pk1 from jsu_item where name = ? and course_id = ?), ?, \'NA\', ?)");
	        }
		    PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		    if(testing){
		    	selectQuery.setString(1, studentID);
	    		selectQuery.setString(2, name);
	    		selectQuery.setString(3, courseId);
	    		selectQuery.setString(4, new DateTime().toString());
	            selectQuery.setString(5, courseId);
	            selectQuery.setString(6, new DateTime().toString());
		    }
		    else{
		        selectQuery.setString(1, name);
		        selectQuery.setString(2, courseId);
		        selectQuery.setString(3, courseId);
	            selectQuery.setString(4, studentID);
	            selectQuery.setString(5, new DateTime().toString());
	            selectQuery.setString(6, name);
	            selectQuery.setString(7, courseId);
	            selectQuery.setString(8, studentID);
	            selectQuery.setString(9, courseId);
	            selectQuery.setString(10, studentID);
	            selectQuery.setString(11, name);
	            selectQuery.setString(12, courseId);
	            selectQuery.setString(13, new DateTime().toString());
	            selectQuery.setString(14, courseId);
		    }
	        selectQuery.executeUpdate();
	        selectQuery.close();
        } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
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
	 * @return true, if is out of supply
	 */
	public boolean isOutOfSupply(Item item) {
		Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement selectQuery = null;
	        queryString.append("select count(*) as times from jsu_purchaseinfo where item_pk1 = (select item_pk1 from jsu_item where name = ? and course_id = ?) and course_id = ?");
            selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            selectQuery.setString(1, item.getName());
            selectQuery.setString(2, courseId);
            selectQuery.setString(3, courseId);
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
	        System.out.println("\nExpiring item for studentid: " + studentID);
	        if(testing){
	        	queryString = getUsedExpiryTestQuery(item, studentID);
	        }
	        else{
		        queryString.append("if exists (select * from jsu_item_use_info where item_pk1 = ( ");
		        queryString.append("select item_pk1 from jsu_item where name = ? and course_id = ?) and course_id = ? and student_id = ?) ");
		        queryString.append("update jsu_item_use_info ");
		        queryString.append("set used_date = ?, expiration_date = ? where item_pk1 = ( ");
		        queryString.append("select item_pk1 from jsu_item where name = ? and course_id = ?) and student_id = ? and course_id = ?");
		        queryString.append("ELSE insert into jsu_item_use_info (student_id, item_pk1, used_date, expiration_date, gradebook_column_name, course_id) ");
		        queryString.append("values(?, (select item_pk1 from jsu_item where name = ? and course_id = ?), ?, ?, \'NA\', ?)");
	        }
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        DateTime date = new DateTime();
	        if(testing){
	        	selectQuery.setString(1, studentID);
	    		selectQuery.setString(2, item.getName());
	    		selectQuery.setString(3, courseId);
	    		selectQuery.setString(4, date.toString());
	            selectQuery.setString(5, date.plusHours(item.getDuration()).toString());
	            selectQuery.setString(6, courseId);
	            selectQuery.setString(7, date.toString());
	            selectQuery.setString(8, date.plusHours(item.getDuration()).toString());
	        }
	        else{
		        selectQuery.setString(1, item.getName());
		        selectQuery.setString(2, courseId);
		        selectQuery.setString(3, courseId);
		        selectQuery.setString(4, studentID);
	            selectQuery.setString(5, date.toString());
	            selectQuery.setString(6, date.plusHours(item.getDuration()).toString());
	            selectQuery.setString(7, item.getName());
	            selectQuery.setString(8, courseId);
	            selectQuery.setString(9, studentID);
	            selectQuery.setString(10, courseId);
	            selectQuery.setString(11, studentID);
	            selectQuery.setString(12, item.getName());
	            selectQuery.setString(13, courseId);
	            selectQuery.setString(14, date.toString());
	            selectQuery.setString(15, date.plusHours(item.getDuration()).toString());
	            selectQuery.setString(16, courseId);
	        }
	        selectQuery.executeUpdate();
	        selectQuery.close();
	    } catch (java.sql.SQLException sE){
	    	sE.printStackTrace();
	    }		
	}
	
	/**
	 * Gets the {@link GradebookColumnPojo} from database by Name and StudentId.
	 *
	 * @param title the title of the Gradebook Column
	 * @param studentID the student id
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
	        queryString.append("jsu_gradebook where student_id = ? and gradebook_column_name = ? and course_id = ?");
	        selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, studentID);
	        selectQuery.setString(2, title);
	        selectQuery.setString(3, courseId);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	gradebook = new GradebookColumnPojo();
	        	gradebook.setGrade(rSet.getInt("grade"));
	        	gradebook.setLastDate(rSet.getString("last_date"));
	        	gradebook.setName(rSet.getString("gradebook_column_name"));
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
	 * Updates the {@link GradebookColumnPojo} in database.
	 *
	 * @param attempt the AttemptDetail object
	 * @param studentID the student id
	 * @return true if success
	 */
	public boolean updateGradebookColumn(AttemptDetail attempt, String studentID) {
		Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        PreparedStatement updateQuery = null;
        try {
        	conn = JSUBbDatabase.getConnection(testing);
        	queryString.append("update jsu_gradebook ");
	        queryString.append("set last_date = ?, grade = ? ");
	        queryString.append("where gradebook_column_name = ? and student_id = ? and course_id = ?");
	        updateQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        updateQuery.setInt(2, (int) attempt.getScore());
	        updateQuery.setString(4, studentID);
	        updateQuery.setString(5, courseId);
	        if(testing){
	        	updateQuery.setString(3, "TEST");
	        	updateQuery.setString(1, new DateTime(attempt.getAttemptDate().getTime()).toString());
	        }
	        else{
	        	updateQuery.setString(3, attempt.getGradebookItem().getTitle());
	        	updateQuery.setString(1, new DateTime().toString());
	        }
	        if(updateQuery.executeUpdate() > 0){
	        	return true;
	        }
	        updateQuery.close();
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
	 * Inserts a {@link GradebookColumnPojo} to the database.
	 *
	 * @param grade the grade
	 * @param gradeTitle the grade title
	 * @param studentID the student id
	 */
	public void insertGradebookColumn(int grade, String gradeTitle, String studentID) {
		Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        PreparedStatement insertQuery = null;
        try {
        	conn = JSUBbDatabase.getConnection(testing);
        	queryString.append("insert into jsu_gradebook(gradebook_column_name, last_date, grade, student_id, course_id) ");
	        queryString.append("VALUES (?, ?, ?, ?, ?)");
	        insertQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        insertQuery.setString(2, new DateTime().toString());
	        insertQuery.setInt(3, grade);
	        insertQuery.setString(4, studentID);
	        insertQuery.setString(5, courseId);
        	insertQuery.setString(1, gradeTitle);
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
	 * Adds new {@link Item} to the Database.
	 *
	 * @param item the item
	 */
	public void addItem(Item item){
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("INSERT INTO jsu_item");
            queryString.append("(name, attribute_affected, cost, duration, effect_magnitude, supply, type, specific_column, course_id) ");
            queryString.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(1, item.getName());
            insertQuery.setString(2, item.getAttributeAffected().toString());
            insertQuery.setInt(3, (int)item.getCost());
            insertQuery.setInt(4, item.getDuration());
            insertQuery.setInt(5, (int)item.getEffectMagnitude());
            insertQuery.setInt(6, (int)item.getSupply());
            insertQuery.setString(7, item.getType().toString());
            insertQuery.setString(8, item.getSpecific());
            insertQuery.setString(9, courseId);
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
	 * Edits {@link Item} information and sets it to the Database.
	 *
	 * @param item the item
	 */
	public void editItem(Item item){
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        PreparedStatement insertQuery = null;
	        queryString.append("UPDATE jsu_item ");
            queryString.append("set attribute_affected = ?, cost = ?, duration = ?, effect_magnitude = ?, supply = ?, type = ?, specific_column = ? ");
            queryString.append("where name = ? and course_id = ?");
            insertQuery = conn.prepareStatement(queryString.toString());
            insertQuery.setString(8, item.getName());
            insertQuery.setString(1, item.getAttributeAffected().toString());
            insertQuery.setInt(2, (int)item.getCost());
            insertQuery.setInt(3, item.getDuration());
            insertQuery.setInt(4, (int)item.getEffectMagnitude());
            insertQuery.setInt(5, (int)item.getSupply());
            insertQuery.setString(9, courseId);
            insertQuery.setString(6, item.getType().toString());
            insertQuery.setString(7, item.getSpecific());
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
	 * Adds the @{link CommunityItem} to the database.
	 *
	 * @param item the @{link CommunityItem}
	 * @param studentID the student id
	 */
	public void addCommunityItem(CommunityItem item, String studentID) {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        int newId = -1;
        try {
        	Setting setting = getSetting("community_item_wait");
			conn = JSUBbDatabase.getConnection(testing);
	        queryString.append("insert into jsu_community_item_info(item_pk1, purchase_date, expiration_date, active, column_name, course_id) ");
	        queryString.append("values((select item_pk1 from jsu_item where name = ? and course_id = ?), ?, ?, 1, ?, ?)");
	        PreparedStatement insertQuery = conn.prepareStatement(queryString.toString(), new String[]{"community_item_info_pk1"});
	        insertQuery.setString(1, item.getName());
	        insertQuery.setString(2, courseId);
	        insertQuery.setString(3, new DateTime().toString());
	        insertQuery.setString(4, new DateTime().plusDays(Integer.parseInt(setting.getValue())).toString());
	        insertQuery.setString(5, item.getColumnName());
	        insertQuery.setString(6, courseId);
	        System.out.println("Item name: " + item.getColumnName());
	        insertQuery.execute();
	        ResultSet rs = insertQuery.getGeneratedKeys();
	        while(rs.next()){
	        	newId = rs.getInt(1);
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
        System.out.println("Added Community Item to table");
        addCommunityItemPayment(item, studentID, newId);
	}
	
	/**
	 * Adds the @{link CommunityItem} payment.
	 *
	 * @param item the @{link CommunityItem}
	 * @param studentID the student id
	 * @param newId the latest id inserted to the CommunityItemInfo table.
	 */
	public void addCommunityItemPayment(CommunityItem item, String studentID, int newId){
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        queryString.append("insert into jsu_community_item_usage(community_item_info_pk1, student_id, paid, course_id) ");
	        queryString.append("values(?, ?, ?, ?)");
	        PreparedStatement insertQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        insertQuery.setInt(1, newId);
	        insertQuery.setString(2, studentID);
	        insertQuery.setInt(3, item.getPaid());
	        insertQuery.setString(4, courseId);
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
        System.out.println("Added Community Item Payment to Table");

	}
	
	/**
	 * Gets the current @{link CommunityItem}.
	 *
	 * @return the current @{link CommunityItem}
	 */
	public CommunityItem getCurrentCommunityItem() {
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        CommunityItem item = new CommunityItem("NO$ITEM");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        queryString.append("select jsu_item.*, jsu_community_item_info.* ");
	        queryString.append("from jsu_community_item_info join jsu_item on(jsu_community_item_info.item_pk1 = jsu_item.item_pk1) where jsu_community_item_info.course_id = ? and jsu_community_item_info.active = 1 and jsu_item.course_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, courseId);
	        selectQuery.setString(2, courseId);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	item = new CommunityItem(rSet.getString("name"));
	        	item.setAttributeAffected(AttributeAffected.valueOf(rSet.getString("attribute_affected")));
	        	item.setCost(rSet.getInt("cost"));
	        	item.setDuration(rSet.getString("duration"));
	        	item.setEffectMagnitude(rSet.getInt("effect_magnitude"));
	        	item.setSupply(rSet.getInt("supply"));
	        	item.setType(AssessmentType.valueOf(rSet.getString("type")));
	        	item.setSpecific(rSet.getString("specific_column"));
	        	item.setActivationLimitDate(rSet.getString("expiration_date"));
	        	item.setColumnName(rSet.getString("column_name"));
	        	item.setForeignId(rSet.getInt("community_item_info_pk1"));
	        	System.out.println("Community Item ID: " + item.getForeignId());
	        }
	        rSet.close();
	        selectQuery.close();
	        if(!item.getName().equals("NO ITEM")){
	        	item.setPaid(getCommunityItemPay(item.getForeignId()));
	        }
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
	 * Gets the @{link CommunityItem} total amount paid.
	 *
	 * @param id the id
	 * @return the total amount paid for the @{link CommunityItem}.
	 */
	private int getCommunityItemPay(long id){
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        int totalPaid = 0;
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        queryString.append("select paid from jsu_community_item_usage where community_item_info_pk1 = ? and course_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setLong(1, id);
	        selectQuery.setString(2, courseId);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	totalPaid += rSet.getInt("paid");
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
        return totalPaid;
	}
	
	/**
	 * Check @{link CommunityItem} status.
	 *
	 * @param item the @{link CommunityItem}
	 * @return Activated if it was paid in full, Refunded if its time finished and Pending if it has not had any updates.
	 */
	public String checkCommunityItemStatus(CommunityItem item){
		Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        int totalPaid = 0;
        DateTime date = null;
        try {
			conn = JSUBbDatabase.getConnection(testing);
			queryString.append("select jsu_community_item_usage.paid, jsu_community_item_info.expiration_date ");
			queryString.append("from jsu_community_item_usage join jsu_community_item_info on (jsu_community_item_info.community_item_info_pk1 = jsu_community_item_usage.community_item_info_pk1) where jsu_community_item_usage.community_item_info_pk1 = ? and jsu_community_item_usage.course_id = ? and jsu_community_item_info.course_id = ? and jsu_community_item_info.active = 1");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setLong(1, item.getForeignId());
	        selectQuery.setString(2, courseId);
	        selectQuery.setString(3, courseId);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	totalPaid += rSet.getInt("paid");
	        	date = new DateTime(rSet.getString("expiration_date"));
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
        if(totalPaid >= item.getCost() && totalPaid != 0){
        	inactiveCommunityItem(item);
        	return "Activated";
        }
        else if(date.isBeforeNow()){
        	inactiveCommunityItem(item);
        	return "Refunded";
        }
        return "Pending";
	}
	
	/**
	 * Turns off the @{link CommunityItem} from Active to InActive.
	 *
	 * @param item the @{link CommunityItem}
	 */
	private void inactiveCommunityItem(CommunityItem item){
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        queryString.append("update jsu_community_item_info set active = 0 where course_id = ? and community_item_info_pk1 = ?");
	        PreparedStatement insertQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        insertQuery.setString(1, courseId);
	        insertQuery.setLong(2, item.getForeignId());
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
	 * Gets the @{link CommunityItem} students list that paid for it.
	 *
	 * @param id the id
	 * @return The list of @{link Student} that paid for the @{link CommunityItem}
	 */
	public List<Student> getCommunityItemStudentsList(int id){
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        List<Student> studentList = new ArrayList<Student>();
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        queryString.append("select student_id, paid from jsu_community_item_usage where course_id = ? and community_item_info_pk1 = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, courseId);
	        selectQuery.setInt(2, id);
	        ResultSet rSet = selectQuery.executeQuery();
	        while(rSet.next()){
	        	Student student = new Student();
	        	student.setStudentID(rSet.getString("student_id"));
	        	student.setGold(rSet.getInt("paid"));
	        	studentList.add(student);
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
        return studentList;
	}
	
	/**
	 * Gets the LastUsed test query. This is ONLY used for TESTING on MySQL.
	 *
	 * @param name the name
	 * @param studentID the student id
	 * @return the last used test query
	 */
	private StringBuffer getLastUsedTestQuery(String name, String studentID){
		StringBuffer queryString = new StringBuffer("");
		queryString.append("insert into jsu_item_use_info ");
		queryString.append("(student_id, item_pk1, used_date, gradebook_column_name, course_id) ");
		queryString.append("values(?, (select item_pk1 from jsu_item where name = ? and course_id = ?), ?, \'NA\', ?) ");
		queryString.append("ON DUPLICATE KEY UPDATE used_date = ?");
		return queryString;
	}
	
	/**
	 * Gets the used expiry test query. This is ONLY used for TESTING on MySQL.
	 *
	 * @param item the item
	 * @param studentID the student id
	 * @return the used expiry test query
	 */
	private StringBuffer getUsedExpiryTestQuery(Item item, String studentID){
		StringBuffer queryString = new StringBuffer("");
		queryString.append("insert into jsu_item_use_info ");
		queryString.append("(student_id, item_pk1, used_date, expiration_date, gradebook_column_name, course_id) ");
		queryString.append("values(?, (select item_pk1 from jsu_item where name = ? and course_id = ?), ?, ?, \'NA\', ?) ");
		queryString.append("ON DUPLICATE KEY UPDATE used_date=?, expiration_date=?");
		return queryString;
	}
	
	/**
	 * Only used for testing.
	 *
	 * @param studentID the student id
	 * @param name the name
	 * @param date the date
	 */
	public void editItemUseInfoExpDate(String studentID, String name, String date){
        Connection conn = null;
        StringBuffer queryString = new StringBuffer("");
        try {
			conn = JSUBbDatabase.getConnection(testing);
	        System.out.println("Expiring item for studentid: " + studentID);
	        queryString.append("update jsu_item_use_info ");
	        queryString.append("set expiration_date = ? ");
	        queryString.append("where item_pk1 = (select item_pk1 from jsu_item where name = ? and course_id = ?) and student_id = ? and course_id = ?");
	        PreparedStatement selectQuery = conn.prepareStatement(queryString.toString(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	        selectQuery.setString(1, date);
	        selectQuery.setString(2, name);
	        selectQuery.setString(3, courseId);
            selectQuery.setString(4, studentID);
            selectQuery.setString(5, courseId);
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
        System.out.println("Expired from database");
	}
}
