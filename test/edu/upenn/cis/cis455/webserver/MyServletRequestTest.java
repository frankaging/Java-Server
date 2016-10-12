package edu.upenn.cis.cis455.webserver;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.http.Cookie;


import junit.framework.TestCase;

public class MyServletRequestTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testMyServletRequest(){
		MyBuffer testBuffer = new MyBuffer();
		Hashtable<String, String> httpCall = new Hashtable<String, String>();
		Hashtable<String, String> headLines = new Hashtable<String, String>();
		httpCall.put("Method", "PUT");
		httpCall.put("HTTP", "HTTP/1.1");
		httpCall.put("path", "www/index.html");
		headLines.put("length", "1111111");
		headLines.put("connection", "close");
		MyServletRequest testMyServletRequest = new MyServletRequest(httpCall, headLines,testBuffer);
		assertEquals(testMyServletRequest.getAuthType(), "BASIC");
		assertEquals(testMyServletRequest.getHeader("length"), "1111111");
		assertEquals(testMyServletRequest.getHeader("connection"), "close");
		testMyServletRequest.setMethod("GET");
		assertEquals(testMyServletRequest.getMethod(), "GET");
		testMyServletRequest.setParameter("aaa", "zzzz");
		assertEquals(testMyServletRequest.getParameter("aaa"), "zzzz");
		assertEquals(testMyServletRequest.getContentLength(), 0);
		assertEquals(testMyServletRequest.getScheme(), "http");
		
	}

}
