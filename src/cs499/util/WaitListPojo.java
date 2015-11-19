package cs499.util;

/**
 * The Class Wait List POJO.
 */
public class WaitListPojo {
	
	/** The name. */
	private String name;
	
	/** The student id. */
	private int primaryKey, studentID;
	
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
	 * Gets the primary key.
	 *
	 * @return the primary key
	 */
	public int getPrimaryKey() {
		return primaryKey;
	}
	
	/**
	 * Sets the primary key.
	 *
	 * @param primaryKey the new primary key
	 */
	public void setPrimaryKey(int primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	/**
	 * Gets the student id.
	 *
	 * @return the student id
	 */
	public int getStudentID() {
		return studentID;
	}
	
	/**
	 * Sets the student id.
	 *
	 * @param studentID the new student id
	 */
	public void setStudentID(int studentID) {
		this.studentID = studentID;
	} 
}
