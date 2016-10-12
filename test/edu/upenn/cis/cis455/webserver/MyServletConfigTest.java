package edu.upenn.cis.cis455.webserver;

import junit.framework.TestCase;

public class MyServletConfigTest extends TestCase {
	
	public void testMyServletConfig(){
		MyServletConfig testServletConfig = new MyServletConfig();
		testServletConfig.setInitParam("Test", "1");
		assertEquals(testServletConfig.getInitParameter("Test"), "1");
		assertEquals(testServletConfig.getInitParameter("test"), null);
		MyServletContext testServletContext = new MyServletContext();
		MyServletConfig testServletConfig_2 = new MyServletConfig("test", testServletContext);
		assertEquals(testServletConfig_2.name, "test");
	}

}
