package cs499.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import blackboard.data.course.CourseMembership;
import blackboard.data.user.User;
import cs499.controllers.Grade.Condition;

public class BlackboardHandler {

	public List<Student> getStudentsList(Iterator<CourseMembership> i){
		List<Student> students = new ArrayList<Student>();
		while(i.hasNext()){
			CourseMembership selectedMember = (CourseMembership) i.next();
			User currentUser = selectedMember.getUser();
			Student student = new Student();
			student.setFirstName(currentUser.getGivenName());
			student.setLastName(currentUser.getFamilyName());
			student.setStudentID(currentUser.getStudentId());
			student.setId(currentUser.getId());
			student.setUserName(currentUser.getUserName());
			students.add(student);
		}
		return students;
	}
	
	public boolean passesCondition(double score, Grade grade){
		Condition condition = grade.getCondition();
		switch(condition){
		case FULLCREDIT:
			if(score == grade.getPointsPossible()){
				return true;
			}
			break;
		case HALFCREDIT:
			if(score == grade.getPointsPossible()/2){
				return true;
			}
			break;
		case PASSINGGRADE:
			if(score == grade.getPassingGrade()){
				return true;
			}
			break;
		}
		return false;
	}
	
}
