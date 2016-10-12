package edu.upenn.cis.cis455.webserver;


import junit.framework.TestCase;

public class MyBufferTest extends TestCase {

	MyBuffer testBuffer;
	
	protected void setUp() throws Exception {
		super.setUp();
		testBuffer = new MyBuffer();
	}
	
	public void testMybuffer(){
		// test logistics
		assertEquals(testBuffer.CODEMATCH.get(200), "OK");
		assertEquals(testBuffer.CODEMATCH.get(302), "Redirect");
		assertEquals(testBuffer.CODEMATCH.get(400), "Bad Request");
		assertEquals(testBuffer.CODEMATCH.get(403), "Forbidden");
		assertEquals(testBuffer.CODEMATCH.get(500), "Server Error");
		assertEquals(testBuffer.CODEMATCH.get(501), "Not Implement");
		
		testBuffer.append("This is for testing");
		assertEquals(testBuffer.messageBodyResponce.toString(), "This is for testing");
		
		testBuffer.resetBuffer();
		assertEquals(testBuffer.messageBodyResponce.toString(), "");
	}

}
