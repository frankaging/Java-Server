package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author tjgreen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MyServletResponce implements HttpServletResponse {
	
	public ErrorLogging _logging = HttpServer._logging;
	
	public MyBuffer myBuffer;
	protected  ArrayList<Cookie> cookiesList = new ArrayList<>();
	protected ArrayList<String> httpResponceArray = new ArrayList<>();
	protected Hashtable<String, ArrayList<String>> headLinesResponceDictionary;
	int bufferSize = 0;
	String contentType = "text/html";
	int contentLength = 0;
	boolean isCommitted = false;
	String characterEncoding = "ISO-8859-1";
	Locale locale = null;

	public MyServletResponce(MyBuffer myBuffer){
		this.myBuffer = myBuffer; 
		this.cookiesList = myBuffer.cookiesList;
		this.httpResponceArray.add("HTTP/1.1");
		this.httpResponceArray.add("200");
		this.httpResponceArray.add("OK");
		this.headLinesResponceDictionary = new Hashtable<String, ArrayList<String>>();

	}
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	public void addCookie(Cookie arg0) {
		for(Cookie cookie : this.cookiesList){
			if(arg0.getName().equals(cookie.getName()) && arg0.getValue().equals(cookie.getValue())){
				System.out.println("Same Cookie");
				return;
			}
		}
		this.cookiesList.add(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(String arg0) {
		return headLinesResponceDictionary.get(arg0) != null ? true : false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(String arg0) {
		try{
			String encoded = URLEncoder.encode(arg0, "UTF-8");
			return encoded;
		}catch(UnsupportedEncodingException ue){
			_logging.logging(ue.getMessage());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(String arg0) {
		try{
			String encoded = URLEncoder.encode(arg0, "UTF-8");
			return encoded;
		}catch(UnsupportedEncodingException ue){
			_logging.logging(ue.getMessage());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(String arg0) {
		try{
			String encoded = URLEncoder.encode(arg0, "UTF-8");
			return encoded;
		}catch(UnsupportedEncodingException ue){
			_logging.logging(ue.getMessage());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		try{
			String encoded = URLEncoder.encode(arg0, "UTF-8");
			return encoded;
		}catch(UnsupportedEncodingException ue){
			_logging.logging(ue.getMessage());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	public void sendError(int arg0, String arg1) throws IOException {
		ArrayList<String> contentTypeArray = new ArrayList<>();
		contentTypeArray.add("text/html");
		headLinesResponceDictionary.put("Content-Type", contentTypeArray);
		myBuffer.addHttpResponce(this.httpResponceArray.get(0), Integer.toString(arg0));
		myBuffer.addHeadLinesResponce(headLinesResponceDictionary);
		myBuffer.append(arg1);
		myBuffer.flushBuffer(this.bufferSize);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	public void sendError(int arg0) throws IOException {
		// committed to buffer, save if and it can not be change
		this.isCommitted = true;
		myBuffer.addHttpResponce(this.httpResponceArray.get(0), Integer.toString(arg0));
		myBuffer.addHeadLinesResponce(headLinesResponceDictionary);
		myBuffer.flushBuffer(this.bufferSize);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String arg0) throws IOException {
		String redirectURL = "";
		myBuffer.addHttpResponce(this.httpResponceArray.get(0), "302");
		
		// check if we have a port number, if we do, we need to append that into abs URL
		if(arg0.contains("http://")){
			redirectURL += arg0;
		}else{
			if(MyServletRequest.headLines.get("Host") != null){
				if(MyServletRequest.headLines.get("Port") != null){
					redirectURL += "http://";
					redirectURL += MyServletRequest.headLines.get("Host");
					redirectURL += ":";
					redirectURL += MyServletRequest.headLines.get("Port");
					redirectURL += arg0;
				}else{
					
				}
			}else{
				redirectURL += "http://";
				redirectURL += "localhost";
				redirectURL += ":";
				redirectURL += Integer.toString(HttpServer.portNumber);
				redirectURL += arg0;
			}
		}
		
		ArrayList<String> temp = new ArrayList<>();
		temp.add(redirectURL);
		
		headLinesResponceDictionary.put("Location", temp);
		myBuffer.addHeadLinesResponce(headLinesResponceDictionary);
		
		myBuffer.redirectMessageBody(redirectURL);
		myBuffer.flushBuffer(this.bufferSize);
		
		System.out.println("[DEBUG] redirect to " + arg0 + " requested");
		System.out.println("[DEBUG] stack trace: ");
		Exception e = new Exception();
		StackTraceElement[] frames = e.getStackTrace();
		for (int i = 0; i < frames.length; i++) {
			System.out.print("[DEBUG]   ");
			System.out.println(frames[i].toString());
		}
	}


	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String arg0) {
		return headLinesResponceDictionary.get(arg0).get(0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	public Enumeration getHeaders(String arg0) {
		return Collections.enumeration(headLinesResponceDictionary.get(arg0));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	public Enumeration getHeaderNames() {
		return Collections.enumeration(headLinesResponceDictionary.keySet());
	}


	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(String arg0, long arg1) {
		Date newDate= new Date();
		newDate.setTime (arg1);
		DateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		String newDateValue = df.format(newDate);
		ArrayList<String> temp = new ArrayList<>();
		temp.add(newDateValue);
		headLinesResponceDictionary.put("Date", temp);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(String arg0, long arg1) {
		Date newDate= new Date();
		newDate.setTime (arg1);
		DateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		String newDateValue = df.format(newDate);
		if(headLinesResponceDictionary.get("Date") == null){
			ArrayList<String> temp = new ArrayList<>();
			temp.add(newDateValue);
			headLinesResponceDictionary.put("Date", temp);
		}else{
			headLinesResponceDictionary.get("Date").add(newDateValue);
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String arg0, String arg1) {
		ArrayList<String> temp = headLinesResponceDictionary.get(arg0);
		if(temp != null){
			temp.clear();
			temp.add(arg1);
		}else{
			temp = new ArrayList<>();
			temp.add(arg1);
			headLinesResponceDictionary.put(arg0, temp);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String arg0, String arg1) {
		ArrayList<String> temp = headLinesResponceDictionary.get(arg0);
		if(temp != null){
			temp.add(arg1);
		}else{
			temp = new ArrayList<>();
			temp.add(arg1);
			headLinesResponceDictionary.put(arg0, temp);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	public void setIntHeader(String arg0, int arg1) {
		ArrayList<String> temp = headLinesResponceDictionary.get(arg0);
		if(temp != null){
			temp.clear();
			temp.add(Integer.toString(arg1));
		}else{
			temp = new ArrayList<>();
			temp.add(Integer.toString(arg1));
			headLinesResponceDictionary.put(arg0, temp);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(String arg0, int arg1) {
		ArrayList<String> temp = headLinesResponceDictionary.get(arg0);
		if(temp != null){
			temp.add(Integer.toString(arg1));
		}else{
			temp = new ArrayList<>();
			temp.add(Integer.toString(arg1));
			headLinesResponceDictionary.put(arg0, temp);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	public void setStatus(int arg0) {
		this.httpResponceArray.set(1, Integer.toString(arg0)); 
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub
		this.httpResponceArray.set(1, Integer.toString(arg0)); 
		this.httpResponceArray.set(2, arg1); 
	}

	public int getStatus() {
		return Integer.parseInt(this.httpResponceArray.get(1)); 
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return "ISO-8859-1";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	public String getContentType() {
		// TODO Auto-generated method stub
		return this.contentType;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	public PrintWriter getWriter() throws IOException {
		return (PrintWriter) (new MyWriter(this.myBuffer, this));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0) {
		// TODO Auto-generated method stub
		if(!this.isCommitted)
			this.characterEncoding = arg0;
		else
			_logging.logging(new IllegalStateException().getMessage());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub
		this.contentLength = arg0;
		ArrayList<String> contentLengthArray = new ArrayList<>();
		contentLengthArray.add(Integer.toString(arg0));
		headLinesResponceDictionary.put("Content-length", contentLengthArray);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	public void setContentType(String arg0) {
		// TODO Auto-generated method stub
		if(!this.isCommitted){
			this.contentType = arg0;
			ArrayList<String> contentTypeArray = new ArrayList<>();
			contentTypeArray.add(arg0);
			headLinesResponceDictionary.put("Content-Type", contentTypeArray);
		}else{
			_logging.logging(new IllegalStateException().getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(int arg0) {
		// TODO Auto-generated method stub
		if(!this.isCommitted){
			if(arg0 > this.contentLength){
				this.bufferSize = arg0;
			}
		}else{
			_logging.logging(new IllegalStateException().getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return this.bufferSize;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub

		// committed to buffer, save if and it can not be change
		this.isCommitted = true;
		myBuffer.addHttpResponce(this.httpResponceArray.get(0), this.httpResponceArray.get(1), this.httpResponceArray.get(2));
		myBuffer.addHeadLinesResponce(headLinesResponceDictionary);
		myBuffer.flushBuffer(this.bufferSize);

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		if(!this.isCommitted){
			myBuffer.resetBuffer();
		}else{
			_logging.logging(new IllegalStateException().getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	public boolean isCommitted() {
		return this.isCommitted;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		if(!this.isCommitted){
			myBuffer.reset();
		}else{
			_logging.logging(new IllegalStateException().getMessage());
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub
		this.locale = arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return this.locale;
	}

}
