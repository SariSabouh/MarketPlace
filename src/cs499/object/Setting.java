package cs499.object;

/**
 * Settings for Module POJO.
 * @author Sari Sabouh
 *
 */
public class Setting {
	
	/**
	 * Name of Setting.
	 */
	private String name;

	/**
	 * Value of Setting.
	 */
	private String value;
	
	
	/**
	 * Getter of the Name of Setting.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter of the Name of Setting.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter of the Value of Setting.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	
	/**
	 * Setter of the Value of Setting.
	 *
	 * @param value the new value
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Setting [name=" + name + ", value=" + value + "]";
	}
}
