package cs499.object;

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
		/** The tests. */
		TEST,
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
	
	/** The Expiration Date. */
	private String expirationDate;

	/** The effect magnitude. */
	private float effectMagnitude;
	
	/** The times this item was used. */
	private int timesUsed;
	
	/** Decides if item is specific for only one Column or only NOT that column. 
	 *  If empty then its for any. */
	private String specific;

	/**
	 * Instantiates a new item by name.
	 *
	 * @param name the name
	 */
	public Item(String name) {
		this.name = name;
		this.specific = "NULL";
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
			this.supply = 9999;
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
	 * Sets the {@link AssessmentType} between ASSIGNMENT, TEST and ALL.
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
	public void setDuration(String duration) {
		if(duration.equals("PASSIVE")){
			this.duration = -1;
		}
		else if(duration.equals("ONCE")){
			this.duration = 0;
		}
		else{
			this.duration = Integer.parseInt(duration.replaceAll("[^0-9]", ""));
		}
	}
	
	public void setDuration(int duration){
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
	 * Gets the {@link AssessmentType} between ASSIGNMENT, TEST and ALL.
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
	 * Gets the {@link AttributeAffected} between DUEDATE, NUMATTEMPTS and GRADE.
	 *
	 * @return the attribute affected
	 */
	public AttributeAffected getAttributeAffected(){
		return attributeAffected;
	}

	/**
	 * Sets the {@link AttributeAffected} between DUEDATE, NUMATTEMPTS and GRADE.
	 *
	 * @param attributeAffected the new attribute affected
	 */
	public void setAttributeAffected(AttributeAffected attributeAffected) {
		this.attributeAffected = attributeAffected;
	}	
	
	public String getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}
	
	public void setTimesUsed(int timesUsed) {
		this.timesUsed = timesUsed;
	}
	
	public int getTimesUsed(){
		return timesUsed;
	}
	
	public String getSpecific() {
		return specific;
	}


	public void setSpecific(String specific) {
		if(specific == null){
			specific = "NULL";
		}
		this.specific = specific;
	}
	
	/** 
	 * Returns the toString for this class.
	 * It contains the relevant information about the item.
	 */
	@Override
	public String toString(){
		String dur = "CONTINUOUS";
		if(duration == 0){
			dur = "ONCE";
		}
		else if(duration == -1){
			dur = "PASSIVE";
		}
		String output = "Item Duration is " + dur;
		if(dur.equals("CONTINUOUS")){
			output += " with a period of " + duration + " hours.";
		}
		output	+= " It costs " + cost + " Gold. The store only has " + supply+ " units available. ";
		output  += "It only affects " + attributeAffected + " by " + effectMagnitude + ". And it can only target " + type;
		if(specific.startsWith("NOT ")){
			output += " It can affect all available collumns of criteria but " + specific;
		}
		else if(specific.startsWith("ONLY ")){
			output += " It can affect " + specific;
		}
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