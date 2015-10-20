package cs499.controllers;

import blackboard.platform.gradebook2.GradeWithAttemptScore;

public class Grade extends GradeWithAttemptScore{

	public enum Condition{
		FULLCREDIT, HALFCREDIT, PASSINGGRADE
	}
	
	private int goldWorth;
	private Condition condition;
	private double passingGrade;
	
	public Grade(GradeWithAttemptScore p){
		goldWorth = 0;
		condition = Condition.FULLCREDIT;
		passingGrade = 0;
		setAttemptGrade(p.getAttemptGrade());
		setAttemptScore(p.getAttemptScore());
		setCourseUserId(p.getCourseUserId());
		setGradableItem(p.getGradableItem());
		setGradableItemId(p.getGradableItemId());
		setId(p.getId());
		setManualGrade(p.getManualGrade());
		setManualScore(p.getManualScore());
		setPointsPossible(p.getPointsPossible());
	}

	public int getGoldWorth() {
		return goldWorth;
	}

	public void setGoldWorth(int goldWorth) {
		this.goldWorth = goldWorth;
	}

	public Condition getCondition() {
		return condition;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	public double getPassingGrade() {
		return passingGrade;
	}
	
	public void setPassingGrade(double passingGrade) {
		this.passingGrade = passingGrade;
	}
}
