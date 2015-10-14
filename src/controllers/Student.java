package controllers;

import java.util.ArrayList;
import java.util.List;

import itemHandler.Item;

public class Student{
	private String fName, lName, userName, studentID;
	private List<Item> itemList;
	private int gold;
	
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

	public void setStudentID(String ID){
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
	
	public String getStudentID(){
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
}