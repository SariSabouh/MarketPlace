package cs499.object;

import java.util.Date;

import org.joda.time.DateTime;

public class GradebookColumnPojo {
	private String name, studentID;
	
	private Date lastDate;

	public String getStudentID() {
		return studentID;
	}
	public void setStudentID(String studentID) {
		this.studentID = studentID;
	}
	private int grade;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getLastDate() {
		return lastDate;
	}
	public void setLastDate(String lastDate) {
		this.lastDate = new DateTime(lastDate).toDate();
	}
	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}
}
