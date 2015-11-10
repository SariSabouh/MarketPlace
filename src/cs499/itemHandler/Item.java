package cs499.itemHandler;

import java.io.Serializable;

public class Item extends MarketPlaceItem implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8072867929811246027L;

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

	public void setEffectMagnitude(float effectMagnitude) {
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

	public float getEffectMagnitude() {
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
				+ "<br />Item Duration: " + duration
				+ "<br />Item Supply: " + supply
				+ "<br />Item Attribute Affected: " + attributeAffected
				+ "<br />Item Effect Mangnitue: " + effectMagnitude
				+ "<br />Item Type: " + type;
		return output;
	}
	
	@Override
    public boolean equals(Object o) {
        if (o instanceof Item) {
            if (name.equals(((Item) o).name)) {
                return true;
            }
        }
        return false;
    }
}