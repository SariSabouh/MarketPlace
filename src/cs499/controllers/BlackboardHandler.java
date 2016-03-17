package cs499.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import blackboard.data.course.CourseMembership;
import blackboard.data.user.User;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.PersistenceRuntimeException;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.platform.gradebook2.AttemptDetail;
import blackboard.platform.gradebook2.AttemptStatus;
import blackboard.platform.gradebook2.BookData;
import blackboard.platform.gradebook2.BookDataRequest;
import blackboard.platform.gradebook2.GradableItem;
import blackboard.platform.gradebook2.GradeDetail;
import blackboard.platform.gradebook2.GradeWithAttemptScore;
import blackboard.platform.gradebook2.GradebookException;
import blackboard.platform.gradebook2.GradebookManager;
import blackboard.platform.gradebook2.GradebookManagerFactory;
import blackboard.platform.gradebook2.impl.AttemptDAO;
import blackboard.platform.gradebook2.impl.GradeDetailDAO;
import blackboard.platform.gradebook2.impl.GradingSchemaDAO;
import blackboard.platform.security.authentication.BbSecurityException;
import cs499.itemHandler.Item;
import cs499.itemHandler.Item.AttributeAffected;
import cs499.itemHandler.ItemController;
import cs499.util.Grade;
import cs499.util.Student;
import cs499.util.Grade.Condition;
import cs499.util.GradebookColumnPojo;

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
	
	/** The flag that defines if this is for testing or not. */
	private boolean testing;
	
	/** This variable is only used for unit testing. */
	private GradeDetail currentGradeDetail;
	

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
		if(sessionUser.getFamilyName().equals("LAST") && sessionUser.getGivenName().equals("FIRST") && sessionUser.getUserName().equals("username")){
			gradableItemList = new ArrayList<GradableItem>();
			testing = true;
			System.out.println("Testing");
		}
		else{
			gradebookManager = GradebookManagerFactory.getInstanceWithoutSecurityCheck();
			bookData = gradebookManager.getBookData(new BookDataRequest(courseID));
			gradableItemList = gradebookManager.getGradebookItems(courseID);
			bookData.addParentReferences();
			bookData.runCumulativeGrading();
			List<CourseMembership> cmlist = CourseMembershipDbLoader.Default.getInstance().loadByCourseIdAndRole(courseID, CourseMembership.Role.STUDENT, null, true);
			Iterator<CourseMembership> i = cmlist.iterator();
			addGoldColumn();
			setStudentsList(i);
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
			ItemController itemController = new ItemController();
			Item item = itemController.getItemByName(itemList, itemName);
			System.out.println("prcessItem exp: " + item.getExpirationDate());
			if(student.canAfford(item.getCost())){
				if(!new MarketPlaceDAO(testing, courseID.toExternalString()).isOutOfSupply(item)){
					System.out.println("Student can afford and store has supply");
					student.buyItem(item, testing, courseID.getExternalString());
					for (GradableItem gradeItem : gradableItemList){
						if (gradeItem.getTitle().equals("Gold")){
							try {
								if(!testing){
									gradebookManager.updateGrade(getGradeDetail(gradeItem, student), true, courseID);
								}
								break;
							} catch (BbSecurityException e) {
								e.printStackTrace();
							}
						}
					}
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
	public void useItem(Item item, String columnName){
		if(isStudent && isColumnAllowedForItem(item, columnName)){
			Student student = getStudent();
			updateItem(item, student, columnName);
		}
	}
	
	private boolean isColumnAllowedForItem(Item item, String columnName) {
		String spec = item.getSpecific();
		System.out.println("Spec " + spec);
		if(spec == null){
			spec = "";
		}
		if(spec.startsWith("NOT ") && spec.equals("NOT " + columnName)){
			return false;
		}
		else if(spec.startsWith("ONLY ") && spec.equals("ONLY " + columnName)){
			return true;
		}
		return true;
	}

	/**
	 * Gets the @{link Student} that is currently logged in.
	 *
	 * @return the student
	 */
	public Student getStudent(){
		for(Student student : students){
			if(student.getStudentID().equals(sessionUser.getStudentId())){
				return student;
			}
		}
		return null;
	}
	
	
	/**
	 * Gets all Gradebook columns by type passed in. It checks if the
	 * Instructor specified in the Settings that the Columns have to be
	 * visible to students or not.
	 * 
	 * @param itemName name of {@link Item}
	 * @return List of all Gradebook Column names
	 */
	public List<String> getAllColumnsByType(String itemName){
		List<String> columns = new ArrayList<String>();
		MarketPlaceDAO marketPlaceDAO = new MarketPlaceDAO(testing, courseID.toExternalString());
		Item item = marketPlaceDAO.loadItem(itemName);
		String type = itemName;
		if(item != null){
			type = item.getType().toString();
			if(item.getDuration() != 0){
				columns.add("ALL");
				return columns;
			}
		}
		for(GradableItem grade : gradableItemList){
			String gradeTitle = grade.getTitle();
			if(gradeTitle.equals("Weighted Total") || gradeTitle.equals("Total") || gradeTitle.equals("Gold")
					|| grade.getCategoryId() == null){
				continue;
			}
			String typeId = "";
			if(type.equals("TEST")){
				typeId = "_12_1";
			}
			else if(type.equals("ASSIGNMENT")){
				typeId = "_10_1";
			}
			if(testing){
				typeId = itemName;
			}
			if(marketPlaceDAO.getSetting("visible_columns").getValue().equals("Y")){
				if(grade.isVisibleToStudents()){
					if(type.equals("ALL")){
						columns.add(gradeTitle);
					}
					else if(grade.getCategoryId().toExternalString().equals(typeId)){
						columns.add(gradeTitle);
					}
				}
			}
			else{
				if(type.equals("ALL")){
					columns.add(gradeTitle);
				}
				else if(grade.getCategoryId().toExternalString().equals(typeId)){
					columns.add(gradeTitle);
				}
			}
		}
		return columns;
	}
	
	/**
	 * Adds Gold to all students with passed parameter.
	 */
	public void addGoldToAll(String gold){
		for(Student student : students){
			adjustColumnGrade(Float.parseFloat(gold), "Gold", student);
		}
	}
	
	
	/**
	 * Adds the Gold Column to the Gradebook if it is not already there.
	 * @throws PersistenceException
	 */
	private void addGoldColumn() throws PersistenceException {
		for(GradableItem grade : gradableItemList){
			if(grade.getTitle().equals("Gold")){
				return;
			}
		}
		if(testing){
			GradableItem grade = new GradableItem();
			grade.setTitle("Gold");
			grade.setId(Id.newId(GradableItem.DATA_TYPE));
			gradableItemList.add(grade);
		}
		else{
			gradebookManager.createGradableItem("Gold", courseID, 9999.0D, null, null, "None", GradingSchemaDAO.get().getGradingSchemaByCourse(courseID).get(1).getTitle(), false);
		}
	}
	
	/**
	 * Sets the @{link Student} list for this course.
	 *
	 * @param i the new students list
	 */
	private void setStudentsList(Iterator<CourseMembership> i){
		MarketPlaceDAO dbController = new MarketPlaceDAO(testing, courseID.toExternalString());
		while(i.hasNext()){
			CourseMembership selectedMember = i.next();
			User currentUser = selectedMember.getUser();
			Student student = new Student();
			student.setFirstName(currentUser.getGivenName());
			student.setLastName(currentUser.getFamilyName());
			student.setStudentID(currentUser.getStudentId());
			student.setUserName(currentUser.getUserName());
			student.setId(selectedMember.getId());
			List<Item> itemList = dbController.loadNotExpiredItems(this.itemList, student.getStudentID());			
			for(Item item : this.itemList){
				ItemController itemCont = new ItemController();
				if(itemCont.getItemByName(itemList, item.getName()) != null){
					student.addItem(item);
				}
			}
			updateColumns(student);
			students.add(student);
			if(currentUser.getId().toExternalString().equals(sessionUser.getId().toExternalString())){
				isStudent = true;
				System.out.println("isStudent");
			}
		}
		setStudentsGold();
	}
	
	private void updateColumns(Student student) {
		for(Item item : student.getItemList()){
			if(item.getDuration() != 0 && item.getTimesUsed() > 0){
				if (!isItemExpired(item.getExpirationDate())){
					List<GradeDetail> gradeDetails = new ArrayList<GradeDetail>();
					if(testing){
						gradeDetails.add(createGradeDetail(gradableItemList.get(0), student));
						gradeDetails.add(createGradeDetailWithAttempt(gradableItemList.get(1), student));
					}
					else{
						gradeDetails = GradeDetailDAO.get().getGradeDetailByCourseUser(student.getId());
					}
					for(GradeDetail gradeDetail : gradeDetails){
						String gradeTitle = gradeDetail.getGradableItem().getTitle();
						if(gradeTitle.equals("Weighted Total") || gradeTitle.equals("Total") || gradeTitle.equals("Gold")){
							continue;
						}
						adjustContinuousGrade(gradeDetail, student, item);
					}
				}

				else{
					List<GradeDetail> gradeDetails = new ArrayList<GradeDetail>();
					if(testing){
						gradeDetails.add(createGradeDetail(gradableItemList.get(0), student));
						gradeDetails.add(createGradeDetailWithAttempt(gradableItemList.get(1), student));
					}
					else{
						gradeDetails = GradeDetailDAO.get().getGradeDetailByCourseUser(student.getId());
					}
					for(GradeDetail gradeDetail : gradeDetails){
						String gradeTitle = gradeDetail.getGradableItem().getTitle();
						if(gradeTitle.equals("Weighted Total") || gradeTitle.equals("Total") || gradeTitle.equals("Gold")){
							continue;
						}
						GradebookColumnPojo gradebookColumn = new MarketPlaceDAO(testing, courseID.toExternalString()).getGradebookColumnByNameAndStudentId(gradeTitle, student.getStudentID());
						if(gradebookColumn.getGrade() == -1){
							adjustContinuousPendingGrade(gradeDetail, student, item);
						}
					}
					
				}
			}
		}
	}
	
	private boolean checkIfAttemptGraded(GradeDetail gradeDetail){
		Id gradedAttemptId = gradeDetail.getLastGradedAttemptId();
		Id attemptId = gradeDetail.getLastAttemptId();
		if(attemptId != null){
			if(gradedAttemptId != null){
				try {
					AttemptDetail gradedAttempt = null;
					AttemptDetail attempt = null;
					if(testing){
						gradedAttempt = gradeDetail.getAttempts().get(0);
						attempt = gradeDetail.getAttempts().get(1);
						Calendar cal = attempt.getAttemptDate();
						cal.set(2015, 3, 6);
						attempt.setAttemptDate(cal);
					}
					else{
						gradedAttempt = AttemptDAO.get().loadById(gradedAttemptId);
						attempt = AttemptDAO.get().loadById(attemptId);
					}
					if(attempt.getAttemptDate().after(gradedAttempt.getAttemptDate())){
						return false;
					}
				} catch (KeyNotFoundException e) {
					e.printStackTrace();
				} catch (PersistenceRuntimeException e) {
					e.printStackTrace();
				}
			}
			else{
				return false;
			}
		}
		return true;
	}
	
	private void adjustContinuousGrade(GradeDetail gradeDetail, Student student, Item item){
		String gradeTitle = gradeDetail.getGradableItem().getTitle();
		MarketPlaceDAO dbHandler = new MarketPlaceDAO(testing, courseID.toExternalString());
		Id attemptId = gradeDetail.getLastGradedAttemptId();
		if(attemptId == null || !checkIfAttemptGraded(gradeDetail)){
			dbHandler.insertGradebookColumn(-1, gradeTitle, student.getStudentID());
			return;
		}
		try{
			GradebookColumnPojo gradebookColumn = dbHandler.getGradebookColumnByNameAndStudentId(gradeTitle, student.getStudentID());
			AttemptDetail attempt = null;
			if(testing){
				attempt = gradeDetail.getAttempts().get(0);
				Calendar cal = Calendar.getInstance();
				cal.set(2017, 02, 02);
				attempt.setAttemptDate(cal);
				dbHandler.updateGradebookColumn(attempt, "00111");
			}
			else{
				attempt = AttemptDAO.get().loadById(attemptId);
			}
			Calendar attemptDate = attempt.getAttemptDate();
			if(gradebookColumn != null){
				if(gradebookColumn.getName().equals(gradeTitle)){
					if(gradebookColumn.getLastDate().before(attemptDate.getTime())){
						attempt = adjustAttemptGrade(attempt, item);
						gradeDetail.setManualGrade(attempt.getGrade());
						gradeDetail.setManualScore(attempt.getScore());
						System.out.println("UPDATING COLUMN " + gradeTitle);
						if(!testing){
							gradebookManager.updateGrade(gradeDetail, true, courseID);
						}
						dbHandler.updateGradebookColumn(attempt, student.getStudentID());
					}
				}
			}
			else{
				attempt = adjustAttemptGrade(attempt, item);
				List<AttemptDetail> attemptList = gradeDetail.getAttempts();
				attemptList.add(attempt);
				gradeDetail.setAttempts(attemptList);
				gradeDetail.setLastAttemptId(attempt.getId());
				gradeDetail.setLastGradedAttemptId(attemptId);
				System.out.println("UPDATING COLUMN " + gradeTitle);
				if(!testing){
					gradebookManager.updateGrade(gradeDetail, true, courseID);
				}
				dbHandler.insertGradebookColumn((int) attempt.getScore(), gradeTitle, student.getStudentID());
			}
		} catch (KeyNotFoundException e) {
			e.printStackTrace();
		} catch (PersistenceRuntimeException e) {
			e.printStackTrace();
		} catch (BbSecurityException e) {
			e.printStackTrace();
		}
	}
	
	private void adjustContinuousPendingGrade(GradeDetail gradeDetail, Student student, Item item){
		String gradeTitle = gradeDetail.getGradableItem().getTitle();
		MarketPlaceDAO dbHandler = new MarketPlaceDAO(testing, courseID.toExternalString());
		Id attemptId = gradeDetail.getLastGradedAttemptId();
		if(attemptId == null){
			return;
		}
		try{
			GradebookColumnPojo gradebookColumn = dbHandler.getGradebookColumnByNameAndStudentId(gradeTitle, student.getStudentID());
			AttemptDetail attempt = null;
			if(testing){
				attempt = gradeDetail.getAttempts().get(0);
			}
			else{
				attempt = AttemptDAO.get().loadById(attemptId);
			}			
			Calendar attemptDate = attempt.getAttemptDate();
			if(gradebookColumn.getName().equals(gradeTitle)){
				if(gradebookColumn.getLastDate().before(attemptDate.getTime())){
					attempt = adjustAttemptGrade(attempt, item);
					gradeDetail.setManualGrade(attempt.getGrade());
					gradeDetail.setManualScore(attempt.getScore());
					System.out.println("UPDATING COLUMN " + gradeTitle);
					if(!testing){
						gradebookManager.updateGrade(gradeDetail, true, courseID);
					}
					dbHandler.updateGradebookColumn(attempt, student.getStudentID());
				}
			}
		} catch (KeyNotFoundException e) {
			e.printStackTrace();
		} catch (PersistenceRuntimeException e) {
			e.printStackTrace();
		} catch (BbSecurityException e) {
			e.printStackTrace();
		}
	}
	
	private AttemptDetail adjustAttemptGrade(AttemptDetail attempt, Item item){
		String grade = attempt.getGrade();
		double score = attempt.getScore();
		float effectMagnitude = item.getEffectMagnitude();
		score = (score * (effectMagnitude/100) + score);
		grade = (score + "");
		attempt.setGrade(grade);
		attempt.setScore(score);
		return attempt;
	}
	
	private boolean isItemExpired(String expDate){
		if(expDate.equals("NA")){
			return false;
    	}
    	else{
    		DateTime expirationDate = new DateTime(expDate);
    		if(expirationDate.isAfterNow()){
    			return false;
    		}
    	}
		return true;
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
		GradeDetail gradeDetail = createGradeDetail(gradeItem, student);
		if(!testing){
			gradeDetail = GradeDetailDAO.get().getGradeDetail(gradeItem.getId(), student.getId());
		}
		else{
			List<AttemptDetail> attempts = new ArrayList<AttemptDetail>();
			gradeDetail.setAttempts(attempts);
		}
		List<AttemptDetail> attemptList = gradeDetail.getAttempts();
		AttemptDetail attemptDetail = new AttemptDetail();
		attemptDetail.setAttemptDate(Calendar.getInstance());
		attemptDetail.setCreationDate(Calendar.getInstance());
		attemptDetail.setExempt(false);
		attemptDetail.setGrade(student.getGold() + "");
		attemptDetail.setGradeId(gradeItem.getId());
		try {
			if(testing){
				attemptDetail.setId(Id.newId(AttemptDetail.DATA_TYPE));
			}
			else{
				attemptDetail.setId(Id.generateId(AttemptDetail.DATA_TYPE, "_22_"));
			}
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
				if (gradeItem.getTitle().equals("Gold")){
					Grade grade = null;
					if(testing){
						GradeWithAttemptScore gradeWithAttempt = new GradeWithAttemptScore();
						gradeWithAttempt.setManualScore(1000.0);
						gradeWithAttempt.setAttemptScore(1000.0);
						grade = new Grade(gradeWithAttempt);
					}
					else{
						grade = new Grade(bookData.get(student.getId(), gradeItem.getId()));
					}
					try{
						student.setGold(grade.getScoreValue().intValue());
						break;
					}catch(NullPointerException e){
						student.setGold(0);
						break;
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
	private void activateItem(Item item, Student student, String columnName) {
		System.out.println("In activate Item");
		AttributeAffected attribute = item.getAttributeAffected();
		if(item.getDuration() == 0){
			switch(attribute){
			case GRADE:
				adjustColumnGrade(item.getEffectMagnitude(), columnName, student);
				break;
			case DUEDATE:
				adjustColumnDueDate(item.getEffectMagnitude(), columnName);
				break;
			case NUMATTEMPTS:
				adjustColumnNumberOfAttempts(item.getEffectMagnitude(), columnName);
				break;
			}
		}
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
				if(gradeItem.isDueDateSet()){
					Calendar cal = gradeItem.getDueDate();
					cal.add(Calendar.HOUR_OF_DAY, (int) effectMagnitude);
					gradeItem.setDueDate(cal);
					System.out.println("Due Date to adjust to is: " + cal.getTime());
					try {
						if(!testing){
							gradebookManager.persistGradebookItem(gradeItem);
						}
						System.out.println("Persisted GradableItem");
					} catch (BbSecurityException e) {
						e.printStackTrace();
					}
				}else{
					System.out.println("It does not have a Due Date");
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
					GradeDetail gradeDetail = null;
					if(!testing){
						gradeDetail = GradeDetailDAO.get().getGradeDetail(gradeItem.getId(), student.getId());
					}
					if(gradeDetail == null){
						gradeDetail = createGradeDetail(gradeItem, student);
					}
					String manualGrade = gradeDetail.getManualGrade();
					manualGrade = (Double.parseDouble(manualGrade) + effectMagnitude) + "";
					double manualScore = gradeDetail.getManualScore();
					manualScore = manualScore + effectMagnitude;
					gradeDetail.setManualGrade(manualGrade);
					gradeDetail.setManualScore(manualScore);
					if(!testing){
						gradebookManager.updateGrade(gradeDetail, true, courseID);
					}
					else{
						currentGradeDetail = gradeDetail;
					}
					break;
				} catch (BbSecurityException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	/**
	 * Adjust gradebook column number of attempts.
	 *
	 * @param effectMagnitude the effect magnitude
	 * @param columnName the column name
	 * @param student the @{link Student}
	 */
	private void adjustColumnNumberOfAttempts(float effectMagnitude, String columnName){
		System.out.println("In Adjust Column Number of Attempts Step");
		for (int i = 0; i<gradableItemList.size(); i ++) {
			GradableItem gradeItem = gradableItemList.get(i);
			if(gradeItem.getTitle().equals(columnName)){
				if(gradeItem.isAllowUnlimitedAttempts()){
					System.out.println("Grade Item has unlimited Attempts!");
					break;
				}
				int maxAttempts = gradeItem.getMaxAttempts();
				int newMaxAttemps = (int) (maxAttempts+effectMagnitude);
				System.out.println("Number of Attempts to adjust to is: " + newMaxAttemps);
				gradeItem.setMaxAttempts(newMaxAttemps);
				try {
					if(!testing){
						gradebookManager.persistGradebookItem(gradeItem);
					}
					System.out.println("Persisted GradableItem");
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
	private boolean updateItem(Item item, Student student, String columnName){
		MarketPlaceDAO dbController = new MarketPlaceDAO(testing, courseID.toExternalString());
		if(item.getDuration() == 0){
			System.out.println("Attempting to expire instant item");
			if(dbController.expireInstantItem(item.getName(), getStudent().getStudentID(), columnName)){
				System.out.println("IN UPDATE ITEM");
				activateItem(item, student, columnName);				
				return true;
			}
		}
		else if(item.getDuration() == -1){
			System.out.println("Attempting to update passive item");
			if(dbController.updateItemUsage(item.getName(), getStudent().getStudentID(), columnName)){
				activateItem(item, student, columnName);
				return true;
			}
		}
		else{
			List<Item> items = student.getItemList();
			item = new ItemController().getItemByName(items, item.getName());
			if(item.getTimesUsed() == 0){
				System.out.println("\nSetting used expiry date for item " + item.getName());
				dbController.setUsedExpiryDate(item, student.getStudentID());
				dbController.updateItemUsage(item.getName(), student.getStudentID(), columnName);
				activateItem(item, student, columnName);
				return true;
			}
			else{
				System.out.println("Item already activated)");
			}
		}
		System.out.println("Could not update item");
		return false;
	}
	
	public GradeDetail createGradeDetail(GradableItem gradableItem, Student student){
		GradeDetail gradeDetail = new GradeDetail();
		gradeDetail.setCourseUserId(student.getId());
		gradeDetail.setGradableItem(gradableItem);
		gradeDetail.setGradableItemId(gradableItem.getId());
		gradeDetail.setGradingRequired(false);
		gradeDetail.setId(Id.newId(GradeDetail.DATA_TYPE));
		gradeDetail.setManualGrade(student.getGold() + "");
		gradeDetail.setManualScore((double)student.getGold());
		return gradeDetail;
	}
	
	public GradeDetail createGradeDetailWithAttempt(GradableItem gradableItem, Student student){
		GradeDetail gradeDetail = new GradeDetail();
		gradeDetail.setCourseUserId(student.getId());
		gradeDetail.setGradableItem(gradableItem);
		gradeDetail.setGradableItemId(gradableItem.getId());
		gradeDetail.setGradingRequired(false);
		gradeDetail.setId(Id.newId(GradeDetail.DATA_TYPE));
		gradeDetail.setManualGrade("100");
		gradeDetail.setManualScore(100.0d);
		List<AttemptDetail> attempts = new ArrayList<AttemptDetail>();
		AttemptDetail attempt2 = new AttemptDetail();
		attempt2.setId(Id.newId(AttemptDetail.DATA_TYPE));
		attempt2.setAttemptDate(Calendar.getInstance());
		attempt2.setGrade("100");
		attempt2.setScore(100.0d);
		attempts.add(attempt2);
		AttemptDetail attempt = new AttemptDetail();
		attempt.setId(Id.newId(AttemptDetail.DATA_TYPE));
		attempt.setAttemptDate(Calendar.getInstance());
		attempt.setGrade("100");
		attempt.setScore(100.0d);
		attempts.add(attempt);
		gradeDetail.setAttempts(attempts);
		gradeDetail.setLastGradedAttemptId(attempt2.getId());
		gradeDetail.setFirstAttemptId(attempt.getId());
		gradeDetail.setFirstGradedAttemptId(attempt.getId());
		gradeDetail.setLastAttemptId(attempt.getId());
		gradeDetail.setLowestAttemptId(attempt.getId());
		gradeDetail.setGradableItem(gradableItem);
		gradeDetail.setGradableItemId(gradableItem.getId());
		return gradeDetail;
	}
	
	/**
	 * This method is only used for testing.
	 * @param gradableItem
	 */
	public void addGradableItem(GradableItem gradableItem){
		gradableItemList.add(gradableItem);
	}
	
	public List<GradableItem> getGradableItemList(){
		return gradableItemList;
	}
	
	public void testingConstructor(Iterator<CourseMembership> i, boolean addGold) throws PersistenceException{
		if(addGold){
			addGoldColumn();
		}
		if(i != null){
			setStudentsList(i);
		}
	}
	
	public GradeDetail getCurrentGradeDetail(){
		return currentGradeDetail;
	}
	
}
