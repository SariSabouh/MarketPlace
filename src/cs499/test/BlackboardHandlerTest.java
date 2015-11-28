package cs499.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import blackboard.persist.KeyNotFoundException;
import blackboard.persist.PersistenceException;
import blackboard.platform.gradebook2.GradebookException;
import blackboard.platform.security.authentication.BbSecurityException;
import cs499.controllers.BlackboardHandler;

public class BlackboardHandlerTest {

	@Test(expected=NullPointerException.class)
	public void testNullConstructor() throws KeyNotFoundException, GradebookException, PersistenceException, BbSecurityException{
		new BlackboardHandler(null, null, null);
	}
	
//	@Test
	public void testProcessItem() {
		
	}

//	@Test
	public void testUseItem() {
		fail("Not yet implemented");
	}

//	@Test
	public void testGetStudent() {
		fail("Not yet implemented");
	}

}
