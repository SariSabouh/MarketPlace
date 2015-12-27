package cs499.util;

/**
 * @author SabouhS
 * 
 * The Class Wait List POJO.
 */
public class WaitListPojo {
	
	/** The name. */
	private String name, studentID, columnName;

	/** The student id. */
	private int primaryKey;
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the column name.
	 *
	 * @return the name
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * Sets the column name.
	 *
	 * @return the name
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
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
}
