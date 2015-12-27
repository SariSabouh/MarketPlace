//package cs499.test;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Iterator;
//import java.util.List;
//
//import org.joda.time.DateTime;
//
//import blackboard.data.course.CourseMembership;
//import blackboard.data.user.User;
//import blackboard.persist.Id;
//import blackboard.persist.PersistenceException;
//import blackboard.persist.PersistenceRuntimeException;
//import blackboard.platform.gradebook2.AttemptDetail;
//import blackboard.platform.gradebook2.GradableItem;
//import blackboard.platform.gradebook2.GradeDetail;
//import blackboard.platform.gradebook2.GradeWithAttemptScore;
//import cs499.controllers.MarketPlaceDAO;
//import cs499.itemHandler.Item;
//import cs499.itemHandler.ItemController;
//import cs499.itemHandler.Item.AttributeAffected;
//import cs499.util.Grade;
//import cs499.util.GradebookColumnPojo;
//import cs499.util.Student;
//import cs499.util.WaitListPojo;
//
//public class BlackboardHandlerMock{
//
//	/** A list of gradable items. One is any column that can have a grade, like a test or an assignment.*/
//	private List<GradableItem> gradableItemList;
//	
//	private List<GradeDetail> gradeDetailList;
//	
//	/** The {@link Student} list. */
//	public List<Student> students;
//	
//	/** The session user. */
//	public User sessionUser;
//	
//	/** The {@link Item} list in the Market Place. */
//	private List<Item> itemList;
//	
//	/** The person logged in is student. */
//	private boolean isStudent;
//	
//	/** The course id. */
//	public Id courseID;
//	
//	/** The flag that defines if this is for testing or not. */
//	private boolean testing;
//	
//	public BlackboardHandlerMock(Id courseID, User sessionUser, List<Item> itemList){
//		this.courseID = courseID;
//		this.itemList = itemList;
//		testing = true;
//		isStudent = false;
//		this.sessionUser = sessionUser;
//		students = new ArrayList<Student>();
//		gradableItemList = new ArrayList<GradableItem>();
//		gradeDetailList = new ArrayList<GradeDetail>();
//	}
//	
//	public List<GradableItem> getGradableItemList(){
//		return gradableItemList;
//	}
//	
//	public void addGradableItem(GradableItem gradableItem){
//		gradableItemList.add(gradableItem);
//	}
//	
//	public List<GradeDetail> getGradeDetailList(){
//		return gradeDetailList;
//	}
//	
//	public void getGradeDetail(GradeDetail gradeDetail){
//		gradeDetailList.add(gradeDetail);
//	}
//
//	/**
//	 * Processes item purchase for the student that is logged in.
//	 *
//	 * @param itemName the item name
//	 */
//	public void processItem(String itemName){
//		if(isStudent){
//			Student student = getStudent();
//			System.out.print("In process");
//			if(student != null){
//				ItemController itemController = new ItemController();
//				Item item = itemController.getItemByName(itemList, itemName);
//				if(student.canAfford(item.getCost())){
//					if(!new MarketPlaceDAO(testing).isOutOfSupply(item)){
//						System.out.println("Student can afford and store has supply");
//						student.buyItem(item, testing);
//					}
//				}
//			}
//		}
//	}
//	
//	/**
//	 * Uses the @{link Item} that is requested by the student that is currently logged in.
//	 *
//	 * @param item the item
//	 * @return true, if successful
//	 */
//	public boolean useItem(Item item, String columnName){
//		if(isStudent){
//			Student student = getStudent();
//			if(student != null){
//				updateItem(item, student, columnName);
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	/**
//	 * Gets the @{link Student} that is currently logged in.
//	 *
//	 * @return the student
//	 */
//	public Student getStudent(){
//		for(Student student : students){
//			if(student.getStudentID().equals(sessionUser.getStudentId())){
//				return student;
//			}
//		}
//		return null;
//	}
//	
//	
//	/**
//	 * Adds the Gold Column to the Gradebook if it is not already there.
//	 * @throws PersistenceException
//	 */
//	public void addGoldColumn(){
//		for(GradableItem grade : gradableItemList){
//			if(grade.getTitle().equals("Gold")){
//				return;
//			}
//		}
//		GradableItem grade = new GradableItem();
//		grade.setTitle("Gold");
//		grade.setId(Id.newId(GradableItem.DATA_TYPE));
//		gradableItemList.add(grade);
//	}
//	
//	/**
//	 * Sets the @{link Student} list for this course.
//	 *
//	 * @param i the new students list
//	 */
//	public void setStudentsList(Iterator<CourseMembership> i){
//		MarketPlaceDAO dbController = new MarketPlaceDAO(testing);
//		while(i.hasNext()){
//			CourseMembership selectedMember = i.next();
//			User currentUser = selectedMember.getUser();
//			Student student = new Student();
//			student.setFirstName(currentUser.getGivenName());
//			student.setLastName(currentUser.getFamilyName());
//			student.setStudentID(currentUser.getStudentId());
//			student.setUserName(currentUser.getUserName());
//			student.setId(selectedMember.getId());
//			List<Item> itemList = dbController.loadNotExpiredItems(this.itemList, student.getStudentID());
//			for(Item item : this.itemList){
//				ItemController itemCont = new ItemController();
//				if(itemCont.getItemByName(itemList, item.getName()) != null){
//					student.addItem(item);
//				}
//			}
//			updateColumns(student);
//			students.add(student);
//			if(currentUser.getId().toExternalString().equals(sessionUser.getId().toExternalString())){
//				isStudent = true;
//				System.out.println("isStudent");
//			}
//		}
//		setStudentsGold();
//	}
//
//	
//	/**
//	 * Updates the @{link Student} gold.
//	 */
//	public void updateStudentGold() {
//		MarketPlaceDAO dbController = new MarketPlaceDAO(testing);
//		for(Student student: students){
//			List<String> itemList = dbController.loadNewPurchases(student.getStudentID());
//			for(Item item : this.itemList){
//				if(itemList.contains(item.getName())){
//					System.out.println("Gold used is: " + item.getCost());
//					student.substractGold(item);
//				}
//			}
//		}
//	}	
//
//	/**
//	 * Activates @{link Item} in the wait list when the teacher logs in.
//	 * It is automatically called when this class is instantiated
//	 * when the teacher logs in. Then it removes the items from the wait list.
//	 */
//	public void activateWaitList(){
//		MarketPlaceDAO dbController = new MarketPlaceDAO(testing);
//		List<WaitListPojo> itemStudent = dbController.loadWaitList();
//		for(WaitListPojo waitList : itemStudent){
//			String studentID = waitList.getStudentID();
//			String itemName = waitList.getName();
//			ItemController itemController = new ItemController();
//			activateItem(itemController.getItemByName(itemList, itemName), getStudentById(studentID));
//			int primaryKey = waitList.getPrimaryKey();
//			dbController.removeItemWaitList(primaryKey);
//		}
//	}
//	
//	private void updateColumns(Student student) {
//		for(Item item : student.getItemList()){
//			if(!isItemExpired(item.getExpirationDate()) && (item.getDuration() != 0 && item.getTimesUsed() > 0)){
//				for(GradeDetail gradeDetail : gradeDetailList){
//					try {
//						String gradeTitle = gradeDetail.getGradableItem().getTitle();
//						if(gradeTitle.equals("Weighted Total") || gradeTitle.equals("Total") || gradeTitle.equals("Gold")){
//							continue;
//						}
//						MarketPlaceDAO dbHandler = new MarketPlaceDAO(testing);
//						Id attemptId = gradeDetail.getLastGradedAttemptId();
//						if(attemptId == null){
//							continue;
//						}
//						AttemptDetail attempt = new AttemptDetail();
//						attempt.setId(attemptId);
//						attempt.setScore(100);
//						attempt.setGrade("100");
//						attempt.setAttemptDate(Calendar.getInstance());
//						Calendar attemptDate = attempt.getAttemptDate();
//						GradebookColumnPojo gradebookColumn = dbHandler.getGradebookColumnByNameAndStudentId(gradeTitle, student.getStudentID());
//						if(gradebookColumn != null){
//							if(gradebookColumn.getName().equals(gradeTitle)){
//								if(gradebookColumn.getLastDate().before(attemptDate.getTime())){
//									attempt = adjustAttemptGrade(attempt, item);
//									gradeDetail.setManualGrade(attempt.getGrade());
//									gradeDetail.setManualScore(attempt.getScore());
//									dbHandler.updateGradebookColumn(attempt, student.getStudentID());
//								}
//							}
//						}
//						else{
//							attempt = adjustAttemptGrade(attempt, item);
//							List<AttemptDetail> attemptList = gradeDetail.getAttempts();
//							attemptList.add(attempt);
//							gradeDetail.setAttempts(attemptList);
//							gradeDetail.setLastAttemptId(attempt.getId());
//							gradeDetail.setLastGradedAttemptId(attemptId);
//							dbHandler.insertGradebookColumn(attempt, student.getStudentID());
//						}
//					} catch (PersistenceRuntimeException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//	}
//	
//	private AttemptDetail adjustAttemptGrade(AttemptDetail attempt, Item item){
//		String grade = attempt.getGrade();
//		double score = attempt.getScore();
//		float effectMagnitude = item.getEffectMagnitude();
//		score = (score * (effectMagnitude/100) + score);
//		grade = (score + "");
//		attempt.setGrade(grade);
//		attempt.setScore(score);
//		return attempt;
//	}
//	
//	private boolean isItemExpired(String expDate){
//		if(expDate.equals("NA")){
//			return false;
//    	}
//    	else{
//    		DateTime expirationDate = new DateTime(expDate);
//    		if(expirationDate.isAfterNow()){
//    			return false;
//    		}
//    	}
//		return true;
//	}
//	
//	
//	/**
//	 * Gets the @{link Student} from the students list by his student id.
//	 *
//	 * @param id the id
//	 * @return the student by id
//	 */
//	private Student getStudentById(String id){
//		for(Student student : students){
//			if(student.getStudentID().equals(id)){
//				return student;
//			}
//		}
//		return null;
//	}
//	
//	/**
//	 * Sets the @{link Student} gold.
//	 */
//	private void setStudentsGold(){
//		for(Student student: students){
//			for (GradableItem gradeItem : gradableItemList) {
//				if (gradeItem.getTitle().equals("Gold")){
//					GradeWithAttemptScore gradeWithAttempt = new GradeWithAttemptScore();
//					gradeWithAttempt.setManualGrade("1000");
//					gradeWithAttempt.setManualScore(1000.0d);
//					gradeWithAttempt.setAttemptScore(1000.0d);
//					gradeWithAttempt.setAttemptGrade("1000");
//					gradeWithAttempt.setCourseUserId(courseID);
//					gradeWithAttempt.setGradableItem(gradeItem);
//					gradeWithAttempt.setGradableItemId(gradeItem.getId());
//					gradeWithAttempt.setId(Id.newId(GradeWithAttemptScore.DATA_TYPE));
//					gradeWithAttempt.setPointsPossible(9999.0d);
//					Grade grade = new Grade(gradeWithAttempt);
//					try{
//						student.setGold(grade.getScoreValue().intValue());
//						break;
//					}catch(NullPointerException e){
//						student.setGold(0);
//						break;
//					}
//				}
//			}
//		}
//	}
//	
//	/**
//	 * Activate @{link Item}.
//	 *
//	 * @param item the @{link Item}
//	 * @param student the @{link Student}
//	 * @return true, if successful
//	 */
//	private void activateItem(Item item, Student student) {
//		System.out.println("In activate Item");
//		AttributeAffected attribute = item.getAttributeAffected();
//		if(item.getDuration() == 0){
//			switch(attribute){
//			case GRADE:
//				adjustColumnGrade(item.getEffectMagnitude(), "Assignment 1", student);
//				break;
//			case DUEDATE:
//				adjustColumnDueDate(item.getEffectMagnitude(), "Exam 1");
//				break;
//			case NUMATTEMPTS:
//				adjustColumnNumberOfAttempts(item.getEffectMagnitude(), "Exam 1");
//				break;
//			}
//		}
//	}
//	
//	/**
//	 * Adjust gradebook column due date.
//	 *
//	 * @param effectMagnitude the effect magnitude
//	 * @param columnName the column name
//	 */
//	private void adjustColumnDueDate(float effectMagnitude, String columnName){
//		System.out.println("In Adjust Column Due Date Step");
//		for (int i = 0; i<gradableItemList.size(); i ++) {
//			GradableItem gradeItem = gradableItemList.get(i);
//			if(gradeItem.getTitle().equals(columnName)){
//				Calendar cal = gradeItem.getDueDate();
//				cal.add(Calendar.HOUR_OF_DAY, (int) effectMagnitude);
//				gradeItem.setDueDate(cal);
//				break;
//			}
//		}
//	}
//	
//	/**
//	 * Adjust gradebook column grade.
//	 *
//	 * @param effectMagnitude the effect magnitude
//	 * @param columnName the column name
//	 * @param student the @{link Student}
//	 */
//	private void adjustColumnGrade(float effectMagnitude, String columnName, Student student){
//		System.out.println("In Adjust Grade Step");
//		for (int i = 0; i<gradableItemList.size(); i ++) {
//			GradableItem gradeItem = gradableItemList.get(i);
//			if(gradeItem.getTitle().equals(columnName)){
//				GradeDetail gradeDetail = gradeDetailList.get(0);
//				String manualGrade = gradeDetail.getManualGrade();
//				manualGrade = (Double.parseDouble(manualGrade) + effectMagnitude) + "";
//				double manualScore = gradeDetail.getManualScore();
//				manualScore = manualScore + effectMagnitude;
//				gradeDetail.setManualGrade(manualGrade);
//				gradeDetail.setManualScore(manualScore);
//				break;
//			}
//		}
//	}
//	
//	/**
//	 * Adjust gradebook column number of attempts.
//	 *
//	 * @param effectMagnitude the effect magnitude
//	 * @param columnName the column name
//	 * @param student the @{link Student}
//	 */
//	private void adjustColumnNumberOfAttempts(float effectMagnitude, String columnName){
//		System.out.println("In Adjust Column Number of Attempts Step");
//		for (int i = 0; i<gradableItemList.size(); i ++) {
//			GradableItem gradeItem = gradableItemList.get(i);
//			if(gradeItem.getTitle().equals(columnName)){
//				int maxAttempts = gradeItem.getMaxAttempts();
//				int newMaxAttemps = (int) (maxAttempts+effectMagnitude);
//				gradeItem.setMaxAttempts(newMaxAttemps);
//				break;
//			}
//		}
//	}
//	
//	/**
//	 * Update @{link Item} status in the database.
//	 *
//	 * @param item the item
//	 * @return true, if successful
//	 */
//	private boolean updateItem(Item item, Student student, String columnName){
//		MarketPlaceDAO dbController = new MarketPlaceDAO(testing);
//		if(item.getDuration() == 0){
//			System.out.println("Attempting to expire instant item");
//			if(dbController.expireInstantItem(item.getName(), getStudent().getStudentID(), columnName)){
//				return true;
//			}
//		}
//		else if(item.getDuration() == -1){
//			System.out.println("Attempting to update passive item");
//			if(dbController.updateItemUsage(item.getName(), getStudent().getStudentID(), columnName)){
//				return true;
//			}
//		}
//		else{
//			List<Item> items = student.getItemList();
//			item = new ItemController().getItemByName(items, item.getName());
//			if(item.getTimesUsed() == 0){
//				System.out.println("Setting used expiry date for item " + item.getName());
//				dbController.setUsedExpiryDate(item, student.getStudentID());
//				dbController.updateItemUsage(item.getName(), student.getStudentID(), columnName);
//				return true;
//			}
//		}
//		return false;
//	}
//	
//}
