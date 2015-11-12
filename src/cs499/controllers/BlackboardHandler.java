package cs499.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import blackboard.data.course.CourseMembership;
import blackboard.data.user.User;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.platform.gradebook2.AttemptDetail;
import blackboard.platform.gradebook2.AttemptStatus;
import blackboard.platform.gradebook2.BookData;
import blackboard.platform.gradebook2.BookDataRequest;
import blackboard.platform.gradebook2.GradableItem;
import blackboard.platform.gradebook2.GradeDetail;
import blackboard.platform.gradebook2.GradebookException;
import blackboard.platform.gradebook2.GradebookManager;
import blackboard.platform.gradebook2.GradebookManagerFactory;
import blackboard.platform.gradebook2.impl.GradeDetailDAO;
import blackboard.platform.security.authentication.BbSecurityException;
import cs499.itemHandler.Item;
import cs499.itemHandler.Item.AssessmentType;
import cs499.itemHandler.Item.AttributeAffected;
import cs499.itemHandler.ItemController;
import cs499.util.Grade;
import cs499.util.Student;
import cs499.util.WaitListPojo;
import cs499.util.Grade.Condition;

public class BlackboardHandler {
	
	private GradebookManager gradebookManager;
	private BookData bookData;
	private List<GradableItem> gradableItemList;
	private List<Student> students;
	private User sessionUser;
	private List<Item> itemList;
	private boolean isStudent;
	private Id courseID;

	public BlackboardHandler(Id courseID, User sessionUser, List<Item> itemList) throws GradebookException, BbSecurityException, KeyNotFoundException, PersistenceException{
		this.courseID = courseID;
		this.itemList = itemList;
		isStudent = false;
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
		if(!isStudent){
			updateStudentGold();
			activateWaitList();
		}
	}
	
	public void processItem(String itemName){
		if(isStudent){
			Student student = getStudent();
			System.out.print("In process");
			if(student != null){
				ItemController itemController = new ItemController();
				Item item = itemController.getItemByName(itemList, itemName);
				if(student.canAfford(item.getCost())){
					System.out.println("Student can afford");
					student.buyItem(item);
				}
			}
		}
	}
	
	public boolean useItem(Item item){
		if(isStudent){
			Student student = getStudent();
			if(student != null){
				updateItem(item);
				return true;
			}
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
			if(currentUser.getId().toExternalString().equals(sessionUser.getId().toExternalString())){
				isStudent = true;
				System.out.println("isStudent");
			}
		}
		setStudentsGold();
	}
	
	private void activateWaitList(){
		MarketPlaceDAO dbController = new MarketPlaceDAO();
		List<WaitListPojo> itemStudent = dbController.loadWaitList();
		for(WaitListPojo waitList : itemStudent){
			int studentID = waitList.getStudentID();
			String itemName = waitList.getName();
			ItemController itemController = new ItemController();
			boolean done = activateItem(itemController.getItemByName(itemList, itemName), getStudentById(studentID));
			if(done){
				int primaryKey = waitList.getPrimaryKey();
				dbController.removeItemWaitList(primaryKey);
			}
		}
	}
	
	private Student getStudentById(int id){
		for(Student student : students){
			if(student.getStudentID() == id){
				return student;
			}
		}
		return null;
	}
	
	private void updateStudentGold() {
		MarketPlaceDAO dbController = new MarketPlaceDAO();
		for(Student student: students){
			boolean purchased = false;
			List<String> itemList = dbController.loadNewPurchases(student.getStudentID());
			for(Item item : this.itemList){
				if(itemList.contains(item.getName())){
					System.out.println("Gold used is: " + item.getCost());
					student.substractGold(item);
					purchased = true;
				}
			}
			if(purchased){
				for (GradableItem gradeItem : gradableItemList){
					if (gradeItem.getTitle().equals("Gold")){
						try {
							gradebookManager.updateGrade(getGradeDetail(gradeItem, student), true, courseID);
							break;
						} catch (BbSecurityException e) {
							e.printStackTrace();
						}
						System.out.println("Attempted to change grade for student: " + student.getFirstName());
					}
				}
			}
		}
	}
	
	private GradeDetail getGradeDetail(GradableItem gradeItem, Student student){
		GradeDetail gradeDetail = GradeDetailDAO.get().getGradeDetail(gradeItem.getId(), student.getId());
		List<AttemptDetail> attemptList = gradeDetail.getAttempts();
		AttemptDetail attemptDetail = new AttemptDetail();
		attemptDetail.setAttemptDate(Calendar.getInstance());
		attemptDetail.setCreationDate(Calendar.getInstance());
		attemptDetail.setExempt(false);
		attemptDetail.setGrade(student.getGold() + "");
		attemptDetail.setGradeId(gradeItem.getId());
		try {
			attemptDetail.setId(Id.generateId(AttemptDetail.DATA_TYPE, "_22_"));
		} catch (PersistenceException e) {
			e.printStackTrace();
		}
		attemptDetail.setOverride(true);
		attemptDetail.setScore(student.getGold());
		attemptDetail.setStatus(AttemptStatus.COMPLETED);
		attemptList.add(attemptDetail);
		gradeDetail.setManualGrade(student.getGold() + "");
		gradeDetail.setManualScore((double) student.getGold());
		gradeDetail.setAttempts(attemptList);
		return gradeDetail;
	}
	
	private boolean passesCondition(double score, Grade grade){
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
	
	private void setStudentsGold(){
		for(Student student: students){
			for (GradableItem gradeItem : gradableItemList) {
				Grade grade = new Grade(bookData.get(student.getId(), gradeItem.getId()));
				if (gradeItem.getTitle().equals("Gold")){
					try{
						MarketPlaceDAO dbController = new MarketPlaceDAO();
						student.setGold(grade.getScoreValue().intValue());
						List<String> itemList = dbController.loadUnusedItems(student.getStudentID());
						for(Item item : this.itemList){
							if(itemList.contains(item.getName())){
								student.addItem(item);
							}
						}
					}catch(NullPointerException e){
						student.setGold(0);
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private boolean activateItem(Item item, Student student) {
		System.out.println("In activate Item");
		AssessmentType type = item.getType(); // HOW WOULD IT DIFFERENTIATE BETWEEN EXAM AND ASSIGN... etc Also discuss structure and item attrs
		AttributeAffected attribute = item.getAttributeAffected();
		switch(attribute){
		case GRADE:
			adjustColumnGrade(item.getEffectMagnitude(), "Assignment 1", student);
			break;
		case DUEDATE:
			adjustColumnDueDate(item.getEffectMagnitude(), "Exam 1");
			break;
		case NUMATTEMPTS:
			break;
		}
		return true;
	}
	
	private void adjustColumnDueDate(float effectMagnitude, String columnName){
		System.out.println("In Adjust Column Due Date Step");
		for (int i = 0; i<gradableItemList.size(); i ++) {
			GradableItem gradeItem = gradableItemList.get(i);
			if(gradeItem.getTitle().equals(columnName)){
				Calendar cal = gradeItem.getDueDate();
				cal.add(Calendar.HOUR_OF_DAY, (int) effectMagnitude);
				gradeItem.setDueDate(cal);
				System.out.println("Due Date to adjust to is: " + cal.toString());
				try {
					gradebookManager.persistGradebookItem(gradeItem);
					System.out.println("Persisted GradableItem");
				} catch (BbSecurityException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	private void adjustColumnGrade(float effectMagnitude, String columnName, Student student){
		System.out.println("In Adjust Grade Step");
		for (int i = 0; i<gradableItemList.size(); i ++) {
			GradableItem gradeItem = gradableItemList.get(i);
			if(gradeItem.getTitle().equals(columnName)){
				try {
					GradeDetail gradeDetail = GradeDetailDAO.get().getGradeDetail(gradeItem.getId(), student.getId());
					String manualGrade = gradeDetail.getManualGrade();
					manualGrade = (Double.parseDouble(manualGrade) + effectMagnitude) + "";
					double manualScore = gradeDetail.getManualScore();
					manualScore = manualScore + effectMagnitude;
					gradeDetail.setManualGrade(manualGrade);
					gradeDetail.setManualScore(manualScore);
					gradebookManager.updateGrade(gradeDetail, true, courseID);
					break;
				} catch (BbSecurityException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	private boolean updateItem(Item item){
		MarketPlaceDAO dbController = new MarketPlaceDAO();
		if(item.getDuration() == 0){
			System.out.println("Attempting to expire instant item");
			if(dbController.expireInstantItem(item.getName(), getStudent().getStudentID())){
				return true;
			}
		}
		else if(item.getDuration() == -1){
			System.out.println("Attempting to update passive item");
			if(!dbController.isOutOfSupply(item, getStudent().getStudentID())){
				if(dbController.updateUsageItem(item.getName(), getStudent().getStudentID())){
					return true;
				}
			}
		}
		else{
			System.out.println("Attempting to expire continuous item");
			if(!dbController.isExpired(item, getStudent().getStudentID())){
				if(dbController.updateUsageItem(item.getName(), getStudent().getStudentID())){
					return true;
				}
			}
			else{
				if(dbController.updateContinuousItem(item, getStudent().getStudentID())){
					return true;
				}
			}
		}
		System.out.println("Could not update item");
		return false;
	}
	
}
