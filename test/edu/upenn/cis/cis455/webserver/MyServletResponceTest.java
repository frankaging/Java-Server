package edu.upenn.cis.cis455.webserver;

import java.util.ArrayList;

import javax.servlet.http.Cookie;


import junit.framework.TestCase;

public class MyServletResponceTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testMyServletResponce(){
		MyBuffer myBuffer = new MyBuffer();
		MyServletResponce testMyServletResponce = new MyServletResponce(myBuffer);
		Cookie fackCookie = new Cookie("test", "1234");
		testMyServletResponce.addCookie(fackCookie);
		assertEquals(testMyServletResponce.cookiesList.get(0).getName(), "test");
		assertEquals(testMyServletResponce.containsHeader("fake"), false);
		ArrayList<String> testList = new ArrayList<>();
		testList.add("this is test");
		testMyServletResponce.headLinesResponceDictionary.put("test", testList);
		assertEquals(testMyServletResponce.getHeader("test"), "this is test");
		testMyServletResponce.addHeader("kkk", "zzz");
		assertEquals(testMyServletResponce.getHeader("kkk"), "zzz");
		testMyServletResponce.setContentLength(9999);
		assertEquals(testMyServletResponce.getHeader("Content-length"), "9999");
		testMyServletResponce.setContentType("testtype");;
		assertEquals(testMyServletResponce.getHeader("Content-Type"), "testtype");
		testMyServletResponce.addHeader("kkk", "zzz");
		assertEquals(testMyServletResponce.getHeader("kkk"), "zzz");
		testMyServletResponce.setContentLength(9999);
		assertEquals(testMyServletResponce.getHeader("Content-length"), "9999");
		testMyServletResponce.setContentType("testtype");;
		assertEquals(testMyServletResponce.getHeader("Content-Type"), "testtype");
		testMyServletResponce.addHeader("kkkg", "zzzg");
		assertEquals(testMyServletResponce.getHeader("kkkg"), "zzzg");
		testMyServletResponce.setContentLength(99);
		assertEquals(testMyServletResponce.getHeader("Content-length"), "99");
		testMyServletResponce.setContentType("testtypeqqq");;
		assertEquals(testMyServletResponce.getHeader("Content-Type"), "testtypeqqq");
	}
	
}
