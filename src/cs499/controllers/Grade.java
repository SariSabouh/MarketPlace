package cs499.controllers;

import blackboard.platform.gradebook2.GradeWithAttemptScore;

public class Grade extends GradeWithAttemptScore{

	public enum Condition{
		FULLCREDIT, HALFCREDIT, PASSINGGRADE
	}
	
	private int goldWorth;
	private Condition condition;
	private double passingGrade;
	
	public Grade(){
		goldWorth = 0;
		condition = Condition.FULLCREDIT;
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
