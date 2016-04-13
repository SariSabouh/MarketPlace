package cs499.object;

import blackboard.platform.gradebook2.GradeWithAttemptScore;
/**
 * The Class Grade.
 *
 * @author SabouhS
 * 
 * The Class Grade.
 */
public class Grade extends GradeWithAttemptScore{

	/**
	 * The Enum Condition.
	 */
	public enum Condition{
		
		/** The full credit. */
		FULLCREDIT, 
		/** The half credit. */
		HALFCREDIT, 
		/** The passing grade. */
		PASSINGGRADE
	}
	
	/** The gold worth. */
	private int goldWorth;
	
	/** The condition. */
	private Condition condition;
	
	/** The passing grade. */
	private double passingGrade;
	
	/**
	 * Instantiates a new grade.
	 *
	 * @param p the p
	 */
	public Grade(GradeWithAttemptScore p){
		goldWorth = 0;
		condition = Condition.FULLCREDIT;
		passingGrade = 0;
		try{
			setAttemptGrade(p.getAttemptGrade());
			setAttemptScore(p.getAttemptScore());
			setManualGrade(p.getManualGrade());
			setManualScore(p.getManualScore());
			setCourseUserId(p.getCourseUserId());
			setGradableItem(p.getGradableItem());
			setGradableItemId(p.getGradableItemId());
			setId(p.getId());
			setPointsPossible(p.getPointsPossible());
		}catch(NullPointerException e){
		}
	}

	/**
	 * Gets the gold worth.
	 *
	 * @return the gold worth
	 */
	public int getGoldWorth() {
		return goldWorth;
	}

	/**
	 * Sets the gold worth.
	 *
	 * @param goldWorth the new gold worth
	 */
	public void setGoldWorth(int goldWorth) {
		this.goldWorth = goldWorth;
	}

	/**
	 * Gets the {@link Condition}.
	 *
	 * @return the condition
	 */
	public Condition getCondition() {
		return condition;
	}

	/**
	 * Sets the {@link Condition}.
	 *
	 * @param condition the new condition
	 */
	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	/**
	 * Gets the passing grade.
	 *
	 * @return the passing grade
	 */
	public double getPassingGrade() {
		return passingGrade;
	}
	
	/**
	 * Sets the passing grade.
	 *
	 * @param passingGrade the new passing grade
	 */
	public void setPassingGrade(double passingGrade) {
		this.passingGrade = passingGrade;
	}
}
