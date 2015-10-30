package cs499.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import blackboard.data.course.CourseMembership;
import blackboard.data.user.User;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.platform.gradebook2.BookData;
import blackboard.platform.gradebook2.BookDataRequest;
import blackboard.platform.gradebook2.GradableItem;
import blackboard.platform.gradebook2.GradebookException;
import blackboard.platform.gradebook2.GradebookManager;
import blackboard.platform.gradebook2.GradebookManagerFactory;
import blackboard.platform.security.authentication.BbSecurityException;
import cs499.controllers.Grade.Condition;
import cs499.dao.DatabaseController;
import cs499.itemHandler.Item;

public class BlackboardHandler {
	
	private GradebookManager gradebookManager;
	private BookData bookData;
	private List<GradableItem> gradableItemList;
	private List<Student> students;
	private User sessionUser;
	private List<Item> itemList;

	public BlackboardHandler(Id courseID, User sessionUser) throws GradebookException, BbSecurityException, KeyNotFoundException, PersistenceException{
		this.sessionUser = sessionUser;
		students = new ArrayList<Student>();
		gradebookManager = GradebookManagerFactory.getInstanceWithoutSecurityCheck();
		bookData = gradebookManager.getBookData(new BookDataRequest(courseID));
		gradableItemList = gradebookManager.getGradebookItems(courseID);
		bookData.addParentReferences();
		bookData.runCumulativeGrading();
		List<CourseMembership> cmlist = CourseMembershipDbLoader.Default.getInstance().loadByCourseIdAndRole(courseID, CourseMembership.Role.STUDENT, null, true);
		Iterator<CourseMembership> i = cmlist.iterator();
		setStudentsList(i);
		updateStudentGold();
	}
	
	private void updateStudentGold() {
		DatabaseController dbController = new DatabaseController();
		System.out.println("Updating student gold");
		for(Student student: students){
			int goldUsed = dbController.loadNewPurchases(student.getStudentID());
			System.out.println("Gold used is: " + goldUsed);
			student.substractGold(goldUsed);
		}
	}

	public void setItemList(List<Item> itemList){
		this.itemList = itemList;
	}
	
	private void setStudentsList(Iterator<CourseMembership> i){
		while(i.hasNext()){
			CourseMembership selectedMember = (CourseMembership) i.next();
			User currentUser = selectedMember.getUser();
			Student student = new Student();
			student.setFirstName(currentUser.getGivenName());
			student.setLastName(currentUser.getFamilyName());
			student.setStudentID(Integer.parseInt(currentUser.getStudentId()));
			student.setUserName(currentUser.getUserName());
			student.setId(selectedMember.getId());
			students.add(student);
		}
	}
	
	public boolean passesCondition(double score, Grade grade){
		Condition condition = grade.getCondition();
		switch(condition){
		case FULLCREDIT:
			if(score == grade.getPointsPossible()){
				return true;
			}
			break;
		case HALFCREDIT:
			if(score == grade.getPointsPossible()/2){
				return true;
			}
			break;
		case PASSINGGRADE:
			if(score == grade.getPassingGrade()){
				return true;
			}
			break;
		}
		return false;
	}
	
	public Student getStudent(){
		for(Student student : students){
			if(student.getStudentID() == Integer.parseInt(sessionUser.getStudentId())){
				return student;
			}
		}
		return null;
	}
	
	public String setStudentGold(){
		String error = "";
		Student student = getStudent();
		for (GradableItem gradeItem : gradableItemList) {
			Grade grade = new Grade(bookData.get(student.getId(), gradeItem.getId()));
			if (gradeItem.getTitle().equals("Gold")){
				try{
					student.setGold(grade.getScoreValue().intValue());
					break;
				}catch(NullPointerException e){
					student.setGold(0);
					error = "Error: Grade score error please contact your instructor immediately.";
				}
			}
		}
		return error;
	}
	
	public void processItem(String itemName){
		Student student = getStudent();
		System.out.print("In process");
		if(student != null){
			for(Item item: itemList){
				if(item.getName().equals(itemName)){
					if(student.canAfford(item.getCost())){
						System.out.println("Student can afford");
						student.buyItem(item);
					}
				}
			}
		}
	}
	
}
