package cs499.object;

public class CommunityItem extends Item{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6827466472367358582L;

	public CommunityItem(Item item) {
		super(item.getName());
		this.setCost((int)item.getCost());
		this.setAttributeAffected(item.getAttributeAffected());
		this.setDuration(item.getDuration());
		this.setEffectMagnitude(item.getEffectMagnitude());
		this.setSupply((int)item.getSupply());
		this.setType(item.getType());
	}
	
	public CommunityItem(String name) {
		super(name);
		columnName = "";
	}

	private int paid;
	
	private String columnName;
	
	private String activationLimitDate;
	
	private int foreignId;

	public int getPaid() {
		return paid;
	}

	public void setPaid(int paid) {
		this.paid = paid;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getActivationLimitDate() {
		return activationLimitDate;
	}

	public void setActivationLimitDate(String activationLimitDate) {
		this.activationLimitDate = activationLimitDate;
	}

	public int getForeignId() {
		return foreignId;
	}

	public void setForeignId(int foreignId) {
		this.foreignId = foreignId;
	}

	
}
