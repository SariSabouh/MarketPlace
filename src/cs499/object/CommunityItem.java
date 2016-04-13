package cs499.object;

/**
 * The Class CommunityItem.
 */
public class CommunityItem extends Item{

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6827466472367358582L;

	/**
	 * Instantiates a new community item.
	 *
	 * @param item the item
	 */
	public CommunityItem(Item item) {
		super(item.getName());
		this.setCost((int)item.getCost());
		this.setAttributeAffected(item.getAttributeAffected());
		this.setDuration(item.getDuration());
		this.setEffectMagnitude(item.getEffectMagnitude());
		this.setSupply((int)item.getSupply());
		this.setType(item.getType());
	}
	
	/**
	 * Instantiates a new community item.
	 *
	 * @param name the name
	 */
	public CommunityItem(String name) {
		super(name);
		columnName = "";
	}

	/** The paid. */
	private int paid;
	
	/** The column name. */
	private String columnName;
	
	/** The activation limit date. */
	private String activationLimitDate;
	
	/** The foreign id. */
	private int foreignId;

	/**
	 * Gets the paid.
	 *
	 * @return the paid
	 */
	public int getPaid() {
		return paid;
	}

	/**
	 * Sets the paid.
	 *
	 * @param paid the new paid
	 */
	public void setPaid(int paid) {
		this.paid = paid;
	}

	/**
	 * Gets the column name.
	 *
	 * @return the column name
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * Sets the column name.
	 *
	 * @param columnName the new column name
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * Gets the activation limit date.
	 *
	 * @return the activation limit date
	 */
	public String getActivationLimitDate() {
		return activationLimitDate;
	}

	/**
	 * Sets the activation limit date.
	 *
	 * @param activationLimitDate the new activation limit date
	 */
	public void setActivationLimitDate(String activationLimitDate) {
		this.activationLimitDate = activationLimitDate;
	}

	/**
	 * Gets the foreign id.
	 *
	 * @return the foreign id
	 */
	public int getForeignId() {
		return foreignId;
	}

	/**
	 * Sets the foreign id.
	 *
	 * @param foreignId the new foreign id
	 */
	public void setForeignId(int foreignId) {
		this.foreignId = foreignId;
	}

	
}
