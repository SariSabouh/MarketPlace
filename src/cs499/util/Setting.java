package cs499.util;

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
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter of the Name of Setting.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter of the Value of Setting.
	 */
	public String getValue() {
		return value;
	}

	
	/**
	 * Setter of the Value of Setting.
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "Setting [name=" + name + ", value=" + value + "]";
	}
}
