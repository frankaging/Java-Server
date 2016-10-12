package edu.upenn.cis.cis455.webserver;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class MyServletContext implements ServletContext{

	public ErrorLogging _logging = HttpServer._logging;
	
	private HashMap<String,Object> attributes;
	private HashMap<String,String> initParams;
	
	public MyServletContext(){
		attributes = new HashMap<String,Object>();
		initParams = new HashMap<String,String>();
	}
	
	@Override
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		return attributes.get(arg0);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		// TODO Auto-generated method stub
		Set<String> keys = attributes.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	@Override
	public ServletContext getContext(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInitParameter(String arg0) {
		// TODO Auto-generated method stub
		return initParams.get(arg0);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		// TODO Auto-generated method stub
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	@Override
	public int getMajorVersion() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public String getMimeType(String arg0) {

		return null;
	}

	@Override
	public int getMinorVersion() {
		// TODO Auto-generated method stub
		return 4;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		File f = new File(arg0);
		if(!f.exists()){
			return null;
		}
		return f.getAbsolutePath();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getResourcePaths(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerInfo() {
		return "Zhengxuan Wu Servlet/Version 1.0";
	}

	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletContextName() {
		return HttpServer.displayName;
	}

	@Override
	public Enumeration<String> getServletNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Enumeration<String> getServlets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void log(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void log(Exception arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void log(String arg0, Throwable arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub
		attributes.remove(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		attributes.put(arg0, arg1);
	}
	
	void setInitParam(String arg0, String arg1) {
		initParams.put(arg0, arg1);
	}

}
