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
import blackboard.platform.gradebook2.impl.GradingSchemaDAO;
import blackboard.platform.security.authentication.BbSecurityException;
import cs499.itemHandler.Item;
import cs499.itemHandler.Item.AssessmentType;
import cs499.itemHandler.Item.AttributeAffected;
import cs499.itemHandler.ItemController;
import cs499.util.Grade;
import cs499.util.Student;
import cs499.util.WaitListPojo;
import cs499.util.Grade.Condition;

/**
 * @author SabouhS
 * 
 * The Class BlackboardHandler. It is our version of Blackboard, as it holds the necessary
 * information to connect and do everything blackboard does.
 */
public class BlackboardHandler {
	
	/** The gradebook manager.*/
	private GradebookManager gradebookManager;
	
	/** The book data taken from the gradebook. */
	private BookData bookData;
	
	/** A list of gradable items. One is any column that can have a grade, like a test or an assignment.*/
	private List<GradableItem> gradableItemList;
	
	/** The {@link Student} list. */
	private List<Student> students;
	
	/** The session user. */
	private User sessionUser;
	
	/** The {@link Item} list in the Market Place. */
	private List<Item> itemList;
	
	/** The person logged in is student. */
	private boolean isStudent;
	
	/** The course id. */
	private Id courseID;

	/**
	 * Instantiates a new blackboard handler. It also sets all students in the course
	 * to the student list and updates their gold column after activating any item that is pending.
	 *
	 * @param courseID the course id
	 * @param sessionUser the session user
	 * @param itemList the item list
	 * @throws GradebookException the gradebook exception
	 * @throws BbSecurityException the blackboard security exception
	 * @throws KeyNotFoundException the key not found exception
	 * @throws PersistenceException the persistence exception
	 */
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
		addGoldColumn();
		setStudentsList(i);
		if(!isStudent){
			updateStudentGold();
			activateWaitList();
		}
	}
	

	/**
	 * Processes item purchase for the student that is logged in.
	 *
	 * @param itemName the item name
	 */
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
	
	/**
	 * Uses the @{link Item} that is requested by the student that is currently logged in.
	 *
	 * @param item the item
	 * @return true, if successful
	 */
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
	
	/**
	 * Gets the @{link Student} that is currently logged in.
	 *
	 * @return the student
	 */
	public Student getStudent(){
		for(Student student : students){
			if(student.getStudentID() == Integer.parseInt(sessionUser.getStudentId())){
				return student;
			}
		}
		return null;
	}
	
	/**
	 * Adds the Gold Column to the Gradebook if it is not already there.
	 * @throws PersistenceException
	 */
	private void addGoldColumn() throws PersistenceException {
		for(GradableItem grade : gradableItemList){
			if(grade.getTitle().equals("Gold")){
				System.out.println("Column Found");
				return;
			}
		}
		
		gradebookManager.createGradableItem("Gold", courseID, 9999.0D, null, null, "None", GradingSchemaDAO.get().getGradingSchemaByCourse(courseID).get(0).getTitle(), false);
	}
	
	/**
	 * Sets the @{link Student} list for this course.
	 *
	 * @param i the new students list
	 */
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
	
	/**
	 * Activates @{link Item} in the wait list when the teacher logs in.
	 * It is automatically called when this class is instantiated
	 * when the teacher logs in. Then it removes the items from the wait list.
	 */
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
	
	/**
	 * Gets the @{link Student} from the students list by his student id.
	 *
	 * @param id the id
	 * @return the student by id
	 */
	private Student getStudentById(int id){
		for(Student student : students){
			if(student.getStudentID() == id){
				return student;
			}
		}
		return null;
	}
	
	/**
	 * Updates the @{link Student} gold.
	 */
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
	
	/**
	 * Gets the grade detail object from the blackboard database
	 * by passing the gradable item and the @{link Student} we are trying to update.
	 *
	 * @param gradeItem the grade item
	 * @param @{link Student} the student
	 * @return the grade detail
	 */
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
	
	/**
	 * Passes condition to get the benefit from an item.
	 * It is not yet used
	 *
	 * @param score the score
	 * @param grade the {@link Grade} object
	 * @return true, if successful
	 */
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
	
	/**
	 * Sets the @{link Student} gold.
	 */
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
					}
				}
			}
		}
	}
	
	/**
	 * Activate @{link Item}.
	 *
	 * @param item the @{link Item}
	 * @param student the @{link Student}
	 * @return true, if successful
	 */
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
	
	/**
	 * Adjust gradebook column due date.
	 *
	 * @param effectMagnitude the effect magnitude
	 * @param columnName the column name
	 */
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
	
	/**
	 * Adjust gradebook column grade.
	 *
	 * @param effectMagnitude the effect magnitude
	 * @param columnName the column name
	 * @param student the @{link Student}
	 */
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
	
	/**
	 * Update @{link Item} status in the database.
	 *
	 * @param item the item
	 * @return true, if successful
	 */
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
