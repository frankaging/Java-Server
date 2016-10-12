package edu.upenn.cis.cis455.webserver;

import junit.framework.TestCase;

public class MyServletContextTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testMyServletContext(){
		MyServletContext testServletContext = new MyServletContext();
		assertEquals(testServletContext.getNamedDispatcher("test"), null);
		assertEquals(testServletContext.getMimeType("test"), null);
		assertEquals(testServletContext.getResourceAsStream("test"), null);
		assertEquals(testServletContext.getMimeType("test"), null);
		testServletContext.setInitParam("test", "1");
		assertEquals(testServletContext.getInitParameter("test"), "1");
	}
	
}
