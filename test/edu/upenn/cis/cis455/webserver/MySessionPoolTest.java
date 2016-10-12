package edu.upenn.cis.cis455.webserver;

import junit.framework.TestCase;

public class MySessionPoolTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testSessionPool(){
		MySessionPool sessionPool = MySessionPool.getInstance();
		assertTrue(sessionPool.sessionsPool.isEmpty());
		MyServletSession s = new MyServletSession();
		sessionPool.setSession(s);
		assertFalse(sessionPool.sessionsPool.isEmpty());
		
	}

}
