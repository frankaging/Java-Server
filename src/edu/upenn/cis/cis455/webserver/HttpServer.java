package edu.upenn.cis.cis455.webserver;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;



public class HttpServer extends HttpServlet{

	/**
	 * Logger for this particular class
	 */
	static ErrorLogging _logging = ErrorLogging.getLogger("Error-Logging.txt");
	
	/**
	 * Static Variables
	 */
	static final int workerNumber = 200;
	DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
	Date dateobj = new Date();
	// shared mapping hashtable
	public static HashMap<String, HttpServlet> servlets;
	public static HashMap<String, String> servletsUrlMapping;
	public static MySessionPool mySessionPool;
	private static String webDotXmlPath;
	public static String displayName;
	public static String session_timeout;
	/**
	 * Variables
	 */
	public static int portNumber;
	private File rootDirectory;
	private ServerSocket serverSocket;
	public workerThread[] workers = new workerThread[workerNumber];
	private boolean shutDownFinish = false;
	/**
	 * Constructors
	 * @throws IOException 
	 */
	// normal case
	public HttpServer(int portNumber, File rootDirectory){
		this.portNumber = portNumber;
		this.rootDirectory = rootDirectory;
		try {
			this.serverSocket = new ServerSocket(portNumber);
			// for testing
			serverSocket.setReuseAddress(true);
		} catch (IOException e) {
			// do nothing because the server is already running
			_logging.logging("*Server Socket  Error* - TIMESTAMPE: " + df.format(dateobj));
			System.exit(0);
		}
	}
	
	
	/**
	 * Server Initialisation
	 */
	private void serverInitialise(){
		for(int i = 0; i < workerNumber; i++){
			workers[i] = new workerThread(this.rootDirectory, workers, this.portNumber, this.serverSocket);
			workers[i].start();
		}
	}
	
	/**
	 * Run function of the server
	 */
	public void run(){
		
		System.out.println("Start accepting request through localhost with port: " + portNumber + "...");
		// While(!workerThread.shutdown){}
		// Daemon adding tasks to working queue
		while(!workerThread.isShutDown){
			try{
				
				Socket newRequest = serverSocket.accept();
				// Calling for adding to the waitingList
				workerThread.addToTaskList(newRequest);

			}catch(Exception e){
				_logging.logging(e.getMessage());
				// catch exception only when the server is shutdown
				// close the server
				try {
					serverSocket.close();
				} catch (IOException serverEx) {
					// TODO Auto-generated catch block
					_logging.logging(serverEx.getMessage());	
				}
				// closing out the servlet
				for(HttpServlet s : servlets.values()){
					s.destroy();
				}
				
				// stop the running thread on JVM kernel
				for(int i = 0; i < workerNumber; i++){
					if(workers[i].getState()  == Thread.State.RUNNABLE){
						workers[i].interrupt();
					}
				}
				
				System.out.println("Terminating the http server");
				System.exit(0);
			}
		}
		
		
	}
	
	
	/**
	 * Design the main function to fetching the input from
	 * the system stdin
	 * Parsing args, and configure the HTTP server
	 * 
	 * @param - stdin fields
	 */
	public static void main(String args[]) throws InterruptedException
	{
		_logging.logging("Start of Http Server");
		/*
		 * instant variables
		 */
		int portNumber;
		File rootDirectory;
		/*
		 * Parsing the stdin with corner cases covered
		 */
		
		if(args.length < 2){
			System.out.println("*Exit*-Not Enough Argument Is Given ! {Full Name: Zhengxuan Wu, SEAS ID: wuzhengx}");
			return;
		}
		
		try{
			portNumber = Integer.parseInt(args[0]);
			rootDirectory = new File(args[1]);
			webDotXmlPath = args[2];
			if(portNumber < 0 || portNumber > 65535){
				System.out.println("*Exit*-Invalid Port! {Full Name: Zhengxuan Wu, SEAS ID: wuzhengx}");
				return;
			}
		}catch(Exception e){
			_logging.logging(e.getMessage());
			System.out.println("*Exit*-Invalid Path Or Port Number ! {Full Name: Zhengxuan Wu, SEAS ID: wuzhengx}");
			return;
		}

		// config the server
		HttpServer myServer = new HttpServer(portNumber, rootDirectory);
		// spin up worker threads
		myServer.serverInitialise();
		// spin up session pool
		mySessionPool = MySessionPool.getInstance();
		// load up servlets
		Handler handler = null;
		try {
			handler = parseWebdotxml(webDotXmlPath);
			
		} catch (Exception e) {
			_logging.logging(e.getMessage());
			e.printStackTrace();
		}
		MyServletContext myServletContext = createContext(handler);
		try {
			System.out.println("Creating Servlets!");
			servlets = createServlets(handler, myServletContext);
		} catch (Exception e) {
			_logging.logging(e.getMessage());
			e.printStackTrace();
		}
		// load up servlet url mapping
		servletsUrlMapping = handler.m_urlPattern;
		for(String s : servletsUrlMapping.keySet()){
			//System.out.println(s + ":" + servletsUrlMapping.get(s));
		}
		
		
		// start the server calling server.run()
		myServer.run();
	}
	
	// parsing function for xml
	private static Handler parseWebdotxml(String webdotxml) throws Exception {
		Handler h = new Handler();
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		
		return h;
	}
	
	// handler for paring xml file
	static class Handler extends DefaultHandler {
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.compareTo("servlet") == 0) {
				m_state = 99;
			} else if (qName.compareTo("servlet-mapping") == 0) {
				m_state = 1;
			} else if (qName.compareTo("servlet-class") == 0) {
				m_state = 2;
			} else if (qName.compareTo("context-param") == 0) {
				m_state = 3;
			} else if (qName.compareTo("init-param") == 0) {
				m_state = 4;
			} else if (qName.compareTo("url-pattern") == 0) {
				m_state = 5;
			} else if (qName.compareTo("servlet-name") == 0){
				m_state = (m_state == 99) ? 30 :40;
			} else if (qName.compareTo("param-name") == 0) {
				m_state = (m_state == 3) ? 10 : 20;
			} else if (qName.compareTo("param-value") == 0) {
				m_state = (m_state == 10) ? 11 : 21;
			} else if (qName.compareTo("display-name") == 0){
				m_state = 999;
			} else if (qName.compareTo("session-timeout") == 0) {
				m_state = 9999;
			}
		}
		public void characters(char[] ch, int start, int length) {
			String value = new String(ch, start, length);
			if (m_state == 30) {
				m_servletName = value;
				m_state = 0;
			} else if (m_state == 40) {
				m_servletName_mapping = value;
				m_state = 0;
			} else if (m_state == 2) {
				if (m_servletName == null) {
					System.err.println("servlet name value '" + value + "' without name");
					System.exit(-1);
				}
				m_servlets.put(m_servletName, value);
				m_state = 0;
			} else if (m_state == 5) {
				if (m_servletName_mapping == null) {
					System.err.println("servlet mapping value '" + value + "' without name");
					System.exit(-1);
				}
				m_urlPattern.put(value, m_servletName_mapping);
				m_servletName_mapping = null;
				m_state = 0;
			} else if (m_state == 10 || m_state == 20) {
				if (m_paramName == null){
					m_paramName = value;
				}
			} else if (m_state == 11) {
				if (m_paramName == null) {
					System.err.println("Context parameter value '" + value + "' without name");
					System.exit(-1);
				}
				m_contextParams.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			} else if (m_state == 21) {
				if (m_paramName == null) {
					System.err.println("Servlet parameter value '" + value + "' without name");
					System.exit(-1);
				}
				HashMap<String,String> p = m_servletParams.get(m_servletName);
				if (p == null) {
					p = new HashMap<String,String>();
					p.put(m_paramName, value);
					m_servletParams.put(m_servletName, p);
				}
				//System.out.println(m_servletName + ":" + m_paramName + ":" + value);
				p.put(m_paramName, value);
				m_servletParams.put(m_servletName, p);
				m_paramName = null;
				m_state = 0;
			} else if (m_state == 999){
				displayName = value;
				m_state=0;
			} else if (m_state == 9999){
				session_timeout = value;
				m_state=0;
			} 
		}
		private int m_state = 0;
		private String m_servletName;
		private String m_servletName_mapping;
		private String m_paramName;
		private String m_display_name;
		HashMap<String,String> m_servlets = new HashMap<String,String>();
		HashMap<String,String> m_urlPattern = new HashMap<String,String>();
		HashMap<String,String> m_contextParams = new HashMap<String,String>();
		HashMap<String,HashMap<String,String>> m_servletParams = new HashMap<String,HashMap<String,String>>();
			
	}
	
	
	
	
	// create context for the servlet enviroment
	private static MyServletContext createContext(Handler h) {
		MyServletContext myServletContext = new MyServletContext();
		for (String param : h.m_contextParams.keySet()) {
			//System.out.println(param + " : " + h.m_contextParams.get(param));
			myServletContext.setInitParam(param, h.m_contextParams.get(param));
		}
		return myServletContext;
	}
	
	// create the mapping between servlet and servlet name
	private static HashMap<String,HttpServlet> createServlets(Handler h, MyServletContext myServletContext) throws Exception {
		HashMap<String,HttpServlet> servlets = new HashMap<String,HttpServlet>();
		for (String servletName : h.m_servlets.keySet()) {
			//System.out.println(servletName);
			MyServletConfig config = new MyServletConfig(servletName, myServletContext);
			String className = h.m_servlets.get(servletName);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String,String> servletParams = h.m_servletParams.get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
					//System.out.println(servletName + ":" + param + ":" + servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(servletName, servlet);
		}
		return servlets;
	}

}
