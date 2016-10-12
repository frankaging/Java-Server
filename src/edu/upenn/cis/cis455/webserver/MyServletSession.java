package edu.upenn.cis.cis455.webserver;

import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * @author Todd J. Green
 */
class MyServletSession implements HttpSession {
	
	public ErrorLogging _logging = HttpServer._logging;
	
	private Properties m_props = new Properties();
	private boolean m_valid = true;
	private long mySessionId = 0;
	private long myTotalSessionNumber = 0;
	Cookie mySessionCookie;
	long Creationtime;
	long time;
	int maxInterval = 0;
	
	public MyServletSession(){
		mySessionId = myTotalSessionNumber + 1;
		myTotalSessionNumber++;
		mySessionCookie = new Cookie("JSESSIONID", Long.toString(mySessionId));
		Creationtime = System.currentTimeMillis();
		time = System.currentTimeMillis();
		if(HttpServer.session_timeout!=null){
			mySessionCookie.setMaxAge(Integer.parseInt(HttpServer.session_timeout));
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getCreationTime()
	 */
	public long getCreationTime() {
		time = System.currentTimeMillis();
		// TODO Auto-generated method stub
		return Creationtime;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getId()
	 */
	public String getId() {
		time = System.currentTimeMillis();
		// TODO Auto-generated method stub
		return Long.toString(mySessionId);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime() {
		// TODO Auto-generated method stub
		return time;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getServletContext()
	 */
	public ServletContext getServletContext() {
		time = System.currentTimeMillis();
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
	 */
	public void setMaxInactiveInterval(int arg0) {
		time = System.currentTimeMillis();
		// TODO Auto-generated method stub
		mySessionCookie.setMaxAge(arg0);
		maxInterval = arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
	 */
	public int getMaxInactiveInterval() {
		time = System.currentTimeMillis();
		// TODO Auto-generated method stub
		return maxInterval;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getSessionContext()
	 */
	public HttpSessionContext getSessionContext() {
		time = System.currentTimeMillis();
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		time = System.currentTimeMillis();
		// TODO Auto-generated method stub
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
	 */
	public Object getValue(String arg0) {
		time = System.currentTimeMillis();
		// TODO Auto-generated method stub
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		time = System.currentTimeMillis();
		// TODO Auto-generated method stub
		return m_props.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValueNames()
	 */
	public String[] getValueNames() {
		time = System.currentTimeMillis();
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		time = System.currentTimeMillis();
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#putValue(java.lang.String, java.lang.Object)
	 */
	public void putValue(String arg0, Object arg1) {
		time = System.currentTimeMillis();
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		time = System.currentTimeMillis();
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
	 */
	public void removeValue(String arg0) {
		time = System.currentTimeMillis();
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#invalidate()
	 */
	public void invalidate() {
		time = System.currentTimeMillis();
		m_valid = false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#isNew()
	 */
	public boolean isNew() {
		time = System.currentTimeMillis();
		// TODO Auto-generated method stub
		return false;
	}

	boolean isValid() {
		time = System.currentTimeMillis();
		return m_valid;
	}

}