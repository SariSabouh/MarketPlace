package cs499.util;

import java.util.ArrayList;
import java.util.List;

import blackboard.persist.Id;
import cs499.controllers.MarketPlaceDAO;
import cs499.itemHandler.Item;

/**
 * @author SabouhS
 * 
 * The Class Student.
 */
public class Student{
	
	/** The user name. */
	private String fName, lName, userName, studentID;
	
	/** The @{link Item} list. */
	private List<Item> itemList;
	
	/** The student id. */
	private int gold;
	
	/** The id. */
	private Id id;
	
	/**
	 * Instantiates a new student.
	 */
	public Student(){
		gold = 0;
		itemList = new ArrayList<Item>();
	}
	
	/**
	 * Adds the @{link Item} to the list.
	 *
	 * @param item the item
	 */
	public void addItem(Item item){
		itemList.add(item);
	}
	
	/**
	 * Gets the item list.
	 *
	 * @return the item list
	 */
	public List<Item> getItemList(){
		return itemList;
	}
	
	/**
	 * Gets the gold.
	 *
	 * @return the gold
	 */
	public int getGold() {
		return gold;
	}

	/**
	 * Sets the gold.
	 *
	 * @param gold the new gold
	 */
	public void setGold(int gold) {
		this.gold = gold;
	}

	/**
	 * Sets the student id.
	 *
	 * @param ID the new student id
	 */
	public void setStudentID(String ID){
		this.studentID = ID;
	}

	/**
	 * Sets the first name.
	 *
	 * @param fName the new first name
	 */
	public void setFirstName(String fName) {
		this.fName = fName;
	}
	
	/**
	 * Sets the last name.
	 *
	 * @param lName the new last name
	 */
	public void setLastName(String lName) {
		this.lName = lName;
	}
	
	
	/**
	 * Sets the user name.
	 *
	 * @param userName the new user name
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	/**
	 * Gets the student id.
	 *
	 * @return the student id
	 */
	public String getStudentID(){
		return studentID;
	}
	
	/**
	 * Gets the first name.
	 *
	 * @return the first name
	 */
	public String getFirstName() {
		return fName;
	}
	
	/**
	 * Gets the last name.
	 *
	 * @return the last name
	 */
	public String getLastName() {
		return lName;
	}
	
	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(Id id) {
		this.id = id;
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public Id getId(){
		return id;
	}
	
	/**
	 * Checks if student can afford @{link Item}.
	 *
	 * @param price the price
	 * @return true, if successful
	 */
	public boolean canAfford(float price){
		if(price > gold){
			return false;
		}
		return true;
	}
	
	/**
	 * Adds @{link Item} to list and persists in database.
	 *
	 * @param item the item
	 */
	public void buyItem(Item item, boolean testing){
		gold = (int) (gold - item.getCost());
		MarketPlaceDAO dbController = new MarketPlaceDAO(testing);
		System.out.println("Gold substracted about to persist");
		dbController.persistPurhcase(studentID, item.getName());
		itemList.add(item);
	}

	/**
	 * Substract gold from student if @{link Item} is purchased.
	 *
	 * @param item the item
	 */
	public void substractGold(Item item) {
		System.out.println("Gold Substracted is: " + item.getCost());
		gold -= item.getCost();
		itemList.add(item);
	}
}