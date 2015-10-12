package itemHandler;

public class Item extends MarketPlaceItem{
	
	public enum AssessmentType {
		ASSIGNMENT, EXAMS, ALL
	};
	
	public enum AttributeAffected {
		DUEDATE, NUMATTEMPTS, GRADE
	};

	private int duration, cost, supply;
	private AssessmentType type;
	private AttributeAffected attributeAffected;
	private String name;
	private float effectMagnitude;

	public Item(String name) {
		this.name = name;
	}
	

	public void setCost(int cost) {
		this.cost = cost;
	}

	public void setSupply(int supply) {
		if(supply > 9){
			supply = 9999999;
		}
		else{
			this.supply = supply;
		}
	}

	public void setAmount(float effectMagnitude) {
		this.effectMagnitude = effectMagnitude;
	}

	public void setType(AssessmentType type) {
		this.type = type;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public int getDuration(){
		return duration;
	}

	public AssessmentType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public float getCost() {
		return cost;
	}

	public float getSupply() {
		return supply;
	}

	public float getAmount() {
		return effectMagnitude;
	}
	
	public AttributeAffected getAttributeAffected(){
		return attributeAffected;
	}

	public void setAttributeAffected(AttributeAffected attributeAffected) {
		this.attributeAffected = attributeAffected;
	}
	
	@Override
	public String toString(){
		String output = "Item Name: " + name
				+ "\nItem Cost: " + cost;
		return output;
	}
}