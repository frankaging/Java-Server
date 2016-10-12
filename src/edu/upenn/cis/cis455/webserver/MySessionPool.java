package edu.upenn.cis.cis455.webserver;

import java.util.Hashtable;

import javax.servlet.http.HttpSession;

public class MySessionPool {
	
	public ErrorLogging _logging = HttpServer._logging;
	
	/*
	 * This is the class to do session tracking (Singleton Pattern)
	 */
	
	public static Hashtable<String, MyServletSession> sessionsPool = null;
	private static MySessionPool instance = null;
	
	/*
	 * Constructor
	 */
	private MySessionPool(){
		sessionsPool = new Hashtable<String, MyServletSession>();
	}
	
	public static MySessionPool getInstance() {
		if (instance == null) instance = new MySessionPool();
        return instance;
    }
	
	/*
	 * Adding into the pool
	 */
	public static MyServletSession getSession(String arg0){
		if(sessionsPool == null)
			return null;
		return sessionsPool.get(arg0);
	}
	
	/*
	 * Getting from the pool
	 */
	public static void setSession(MyServletSession arg0){
		sessionsPool.put(arg0.getId(), arg0);
	}
	
}

