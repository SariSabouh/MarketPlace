package cs499.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import blackboard.base.FormattedText;
import blackboard.data.course.CourseMembership;
import blackboard.data.user.User;
import blackboard.persist.Id;
import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.persist.course.CourseMembershipDbLoader;
import blackboard.platform.blog.Blog;
import blackboard.platform.blog.BlogEntriesUserStatus;
import blackboard.platform.blog.BlogEntry;
import blackboard.platform.blog.BlogEntry.BlogEntryStatus;
import blackboard.platform.blog.impl.BlogDAO;
import blackboard.platform.blog.impl.BlogEntryDAO;
import blackboard.platform.blog.impl.BlogManagerImpl;
import blackboard.platform.gradebook2.BookData;
import blackboard.platform.gradebook2.BookDataRequest;
import blackboard.platform.gradebook2.GradableItem;
import blackboard.platform.gradebook2.GradebookException;
import blackboard.platform.gradebook2.GradebookManager;
import blackboard.platform.gradebook2.GradebookManagerFactory;
import blackboard.platform.security.authentication.BbSecurityException;
import cs499.controllers.Grade.Condition;
import cs499.itemHandler.Item;

public class BlackboardHandler {
	
	private GradebookManager gradebookManager;
	private BookData bookData;
	private List<GradableItem> gradableItemList;
	private List<Student> students;
	private User sessionUser;
	private List<Item> itemList;
	private Id courseID;

	public BlackboardHandler(Id courseID, User sessionUser) throws GradebookException, BbSecurityException, KeyNotFoundException, PersistenceException{
		this.courseID = courseID;
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
			student.setStudentID(currentUser.getStudentId());
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
			if(student.getStudentID().equals(sessionUser.getStudentId())){
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
	
	public void buyItem(String itemName){
		Student student = getStudent();
		if(student != null){
			for(Item item: itemList){
				if(item.getName().equals(itemName)){
					if(student.canAfford(item.getCost())){
						student.payPrice(item.getCost(), item);
						addItemToBlog(itemName);
					}
				}
			}
		}
	}
	
	public void addItemToBlog(String itemName){
		List<Blog> blogs = BlogDAO.get().loadByCourseId(courseID, true, false, false);
		for(Blog blog : blogs){
			BlogEntry blogEntry = createBlogEntry(blog, itemName);
			BlogManagerImpl blogManager = new BlogManagerImpl();
			BlogEntriesUserStatus blogStatus = new BlogEntriesUserStatus(sessionUser.getId(), blog.getId());
			blogStatus.markEntryAsNew(blogEntry.getId());
			BlogEntryDAO.get().persist(blogEntry);
			BlogEntryDAO.get().persistAndUpdateTimeStamp(blogEntry);
			BlogEntryDAO.get().persistAndUpdateTimeStamp(blogEntry);
			BlogDAO.get().persist(blog);
			blogManager.persistBlogEntry(blog, sessionUser.getId(), blogEntry, false, blogStatus);
			blogManager.saveBlog(blog);
		}
	}
	
	private BlogEntry createBlogEntry(Blog blog, String itemName){
		BlogEntry blogEntry = new BlogEntry();
		blogEntry.setBlogId(blog.getId());
		blogEntry.setCreatorCourseUserId(sessionUser.getId());
		blogEntry.setStatus(BlogEntryStatus.POSTED);
		blogEntry.setTitle(itemName);
		blogEntry.setAnonymous(false);
		blogEntry.setCreationDate(Calendar.getInstance());
		FormattedText formatText = FormattedText.toFormattedText("Item name is: " + itemName);
		blogEntry.setDescription(formatText);
		blogEntry.setId(Id.toId(BlogEntry.DATA_TYPE, "_200_1"));
		return blogEntry;
	}

	public boolean hasItem(String itemName) {
		List<Blog> blogs = BlogDAO.get().loadByCourseId(courseID, true, false, false);
		for(Blog blog : blogs){
			if(blog.getTitle().equalsIgnoreCase("item")){
				List<BlogEntry> blogEntries = BlogEntryDAO.get().loadAllByBlogId(blog.getId());
				for(BlogEntry blogEntry : blogEntries){
					if(blogEntry.getTitle().equals(itemName)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
}
