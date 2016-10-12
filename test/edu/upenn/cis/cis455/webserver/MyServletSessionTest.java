package edu.upenn.cis.cis455.webserver;


import junit.framework.TestCase;

public class MyServletSessionTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testMyServletSession(){
		MyServletSession fakeSession = new MyServletSession();
		assertEquals(fakeSession.getCreationTime(), 0);
		fakeSession.setMaxInactiveInterval(10);
		assertEquals(fakeSession.getMaxInactiveInterval(), 10);
		fakeSession.setMaxInactiveInterval(15);
		assertEquals(fakeSession.getMaxInactiveInterval(), 15);
		fakeSession.setMaxInactiveInterval(20);
		assertEquals(fakeSession.getMaxInactiveInterval(), 20);
		fakeSession.setMaxInactiveInterval(30);
		assertEquals(fakeSession.getMaxInactiveInterval(), 30);
	}
	
}
