package cs499.itemHandler;

import java.io.Serializable;

/**
 * @author SabouhS
 * 
 * The Class Item.
 */
public class Item implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8072867929811246027L;

	/**
	 * The Enum AssessmentType.
	 */
	public enum AssessmentType {
		
		/** The assignment. */
		ASSIGNMENT,
		/** The exams. */
		EXAMS,
		/** The all. */
		ALL
	};
	
	/**
	 * The Enum AttributeAffected.
	 */
	public enum AttributeAffected {		
		/** The duedate. */
		DUEDATE, 
		/** The numattempts. */
		NUMATTEMPTS, 
		/** The grade. */
		GRADE
	};

	/** The supply. cost. duration */
	private int duration, cost, supply;
	
	/** The {@link AssessmentType}. */
	private AssessmentType type;
	
	/** The {@link AttributeAffected}. */
	private AttributeAffected attributeAffected;
	
	/** The name. */
	private String name;
	
	/** The effect magnitude. */
	private float effectMagnitude;

	/**
	 * Instantiates a new item by name.
	 *
	 * @param name the name
	 */
	public Item(String name) {
		this.name = name;
	}
	

	/**
	 * Sets the cost.
	 *
	 * @param cost the new cost
	 */
	public void setCost(int cost) {
		this.cost = cost;
	}

	/**
	 * Sets the supply. If bigger than 9 then its Passive
	 *
	 * @param supply the new supply
	 */
	public void setSupply(int supply) {
		if(supply > 9){
			supply = 9999999;
		}
		else{
			this.supply = supply;
		}
	}

	/**
	 * Sets the effect magnitude.
	 *
	 * @param effectMagnitude the new effect magnitude
	 */
	public void setEffectMagnitude(float effectMagnitude) {
		this.effectMagnitude = effectMagnitude;
	}

	/**
	 * Sets the {@link AssessmentType}.
	 *
	 * @param type the new type
	 */
	public void setType(AssessmentType type) {
		this.type = type;
	}

	/**
	 * Sets the duration.
	 *
	 * @param duration the new duration
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	/**
	 * Gets the duration.
	 *
	 * @return the duration
	 */
	public int getDuration(){
		return duration;
	}

	/**
	 * Gets the {@link AssessmentType}.
	 *
	 * @return the type
	 */
	public AssessmentType getType() {
		return type;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the cost.
	 *
	 * @return the cost
	 */
	public float getCost() {
		return cost;
	}

	/**
	 * Gets the supply.
	 *
	 * @return the supply
	 */
	public float getSupply() {
		return supply;
	}

	/**
	 * Gets the effect magnitude.
	 *
	 * @return the effect magnitude
	 */
	public float getEffectMagnitude() {
		return effectMagnitude;
	}
	
	/**
	 * Gets the {@link AttributeAffected}.
	 *
	 * @return the attribute affected
	 */
	public AttributeAffected getAttributeAffected(){
		return attributeAffected;
	}

	/**
	 * Sets the {@link AttributeAffected}.
	 *
	 * @param attributeAffected the new attribute affected
	 */
	public void setAttributeAffected(AttributeAffected attributeAffected) {
		this.attributeAffected = attributeAffected;
	}
	
	/** 
	 * Returns the toString for this class.
	 * It contains the relevant information about the item.
	 */
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