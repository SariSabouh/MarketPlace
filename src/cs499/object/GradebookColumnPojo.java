package cs499.object;

import java.util.Date;

import org.joda.time.DateTime;

/**
 * The Class GradebookColumnPojo.
 */
public class GradebookColumnPojo {
	
	/** The student id. */
	private String name, studentID;
	
	/** The last date. */
	private Date lastDate;

	/**
	 * Gets the student id.
	 *
	 * @return the student id
	 */
	public String getStudentID() {
		return studentID;
	}
	
	/**
	 * Sets the student id.
	 *
	 * @param studentID the new student id
	 */
	public void setStudentID(String studentID) {
		this.studentID = studentID;
	}
	
	/** The grade. */
	private int grade;

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the last date.
	 *
	 * @return the last date
	 */
	public Date getLastDate() {
		return lastDate;
	}
	
	/**
	 * Sets the last date.
	 *
	 * @param lastDate the new last date
	 */
	public void setLastDate(String lastDate) {
		this.lastDate = new DateTime(lastDate).toDate();
	}
	
	/**
	 * Gets the grade.
	 *
	 * @return the grade
	 */
	public int getGrade() {
		return grade;
	}
	
	/**
	 * Sets the grade.
	 *
	 * @param grade the new grade
	 */
	public void setGrade(int grade) {
		this.grade = grade;
	}
}
