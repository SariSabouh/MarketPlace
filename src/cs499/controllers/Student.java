package cs499.controllers;

import java.util.ArrayList;
import java.util.List;

import blackboard.persist.Id;
import cs499.dao.DatabaseController;
import cs499.itemHandler.Item;

public class Student{
	private String fName, lName, userName;
	private List<Item> itemList;
	private int gold, studentID;
	private Id id;
	
	public Student(){
		gold = 0;
		itemList = new ArrayList<Item>();
	}
	
	public List<Item> getItemList(){
		return itemList;
	}
	
	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public void setStudentID(int ID){
		this.studentID = ID;
	}

	public void setFirstName(String fName) {
		this.fName = fName;
	}
	
	public void setLastName(String lName) {
		this.lName = lName;
	}
	
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public int getStudentID(){
		return studentID;
	}
	
	public String getFirstName() {
		return fName;
	}
	
	public String getLastName() {
		return lName;
	}
	
	public String getUserName() {
		return userName;
	}

	public void setId(Id id) {
		this.id = id;
	}
	
	public Id getId(){
		return id;
	}
	
	public boolean canAfford(float price){
		if(price > gold){
			return false;
		}
		return true;
	}
	
	public void buyItem(Item item){
		gold = (int) (gold - item.getCost());
		DatabaseController dbController = new DatabaseController();
		System.out.println("Gold substracted about to persist");
		dbController.persistPurhcase(studentID, item.getName());
		itemList.add(item);
	}

	public void substractGold(int goldUsed) {
		gold -= goldUsed;
	}
}