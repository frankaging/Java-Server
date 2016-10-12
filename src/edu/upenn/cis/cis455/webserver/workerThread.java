package edu.upenn.cis.cis455.webserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import java.lang.Thread.State;

public class workerThread extends Thread{

	public ErrorLogging _logging = HttpServer._logging;

	/**
	 * static variables
	 */
	static boolean isShutDown = false;
	final static int taskListSize = 100000;
	private static LinkedList<Socket> taskList = new LinkedList<>();
	private ServerSocket serverSocket;
	/**
	 * variables
	 */
	File rootDirectory;
	public static workerThread[] workers;
	String indexPage;
	public int portNumber;

	public ArrayList<String> httpCallRaw = new ArrayList<>();
	public ArrayList<String> headLinesRaw = new ArrayList<>();
	public ArrayList<String> postContentRaw = new ArrayList<>();

	public Hashtable<String, String> httpCallRawDictionary = new Hashtable<>();
	public Hashtable<String, String> headLinesRawDictionary = new Hashtable<>();

	boolean expectContinue = false;
	boolean hostContained = false;

	public workerThread(File rootDirectory, workerThread[] workers, int portNumber, ServerSocket serverSocket){
		this.rootDirectory = rootDirectory;
		this.workers = workers;
		this.indexPage = null;
		this.portNumber = portNumber;
		this.serverSocket = serverSocket;
	}

	/**
	 * Daemon Adding Tasks to the queue
	 * @throws InterruptedException 
	 */
	public static void addToTaskList(Socket newRequest) throws InterruptedException{
		// using mesa monitoring method to block the thread

		synchronized(taskList){
			//System.out.println("Now Taking A New Request From Server Socket...");
			taskList.add(taskList.size(), newRequest);
			taskList.notify();
		}
	}

	/**
	 * Worker thread run function
	 * Getting tasks from tasks Queue if any
	 * Getting job done before return
	 */
	public void run(){
		while(!isShutDown){

			// mesa monitoring rule, checking if need to wait
			Socket task;

			synchronized(taskList){
				while (taskList.isEmpty()){
					try {
						taskList.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// if it is not empty, pull out the first one
				task = taskList.removeFirst();
			}

			try{
				// reader and writer initialise for the socket
				BufferedReader in = new BufferedReader(new InputStreamReader(task.getInputStream()));			
				Writer out = new OutputStreamWriter(new BufferedOutputStream(task.getOutputStream()));
				OutputStream binaryOut = new BufferedOutputStream(task.getOutputStream());
				PrintStream printStream = new PrintStream(task.getOutputStream(), true);

				// rawDataParsing
				rawDataParsing(in);
				// basic validation check
				if(httpCallRaw.isEmpty()){
					performEmpty(in, out, "1.1");
					continue;
				}

				// parsing initial lines and saving to raw dict
				String httpCall = httpCallRaw.get(0);
				String[] httpCallRaws = httpCall.split("\\s+");
				// basic validation check
				if(httpCallRaws.length < 3){
					performEmpty(in, out, "1.1");
					continue;
				}
				httpCallParsing(httpCallRaws);
				// parsing head lines and saving to raw dict
				headLinesParsing(headLinesRaw);
				// check if meet the http call, if not return error
				if(!isValid()){
					performEmpty(in, out, httpCallRawDictionary.get("HTTP").split("/")[1]);
					continue;
				}
				HashMap<String, String> servletsMapping = HttpServer.servletsUrlMapping;
				String servletName = findServletMatching(httpCallRawDictionary.get("path"), servletsMapping);
				// sent continue before anything
				if(httpCallRawDictionary.get("HTTP").equals("HTTP/1.1") && headLinesRawDictionary.containsKey("Expect") 
						&& headLinesRawDictionary.get("Expect").toLowerCase().equals("100-continue")){
					performExpectContinue(in, out, "1.1");
					expectContinue = true;
				}
				// if we find a match, proceding using servlet
				if(servletName != null){
					System.out.println("This is a servlet call!");
					servletDispatcher(out, binaryOut, servletName);
					// close out after performing
					in.close();
					out.close();
					binaryOut.close();
				}else{

					// proceeding without using servlet
					String urlWithMethod = httpCallRawDictionary.get("Method") + " " + httpCallRawDictionary.get("path") +  " " + httpCallRawDictionary.get("HTTP");
					// check if the path is secured
					if(!isSecure(httpCallRawDictionary.get("path"))){
						System.out.println("This is not a secure path!");
						performEmpty(in, out, httpCallRawDictionary.get("HTTP").split("/")[1]);
						continue;
					}
					// if pass the security
					String[] parsedUrlWithMethod = urlWithMethod.split(" ");
					String method = parsedUrlWithMethod[0];
					String requestDirectory = parsedUrlWithMethod[1];
					// HTTP REQUEST TYPE
					String httpTypeInfo = parsedUrlWithMethod[2];
					String httpType = "";
					if(httpTypeInfo.contains("HTTP/1.1"))
						httpType = "1.1";
					else if(httpTypeInfo.contains("HTTP/1.0"))
						httpType = "1.0";

					// save the request directory for future use
					this.indexPage = requestDirectory;

					// justify the completeness of headers
					if((httpType.equals("1.1")) || httpType.equals("1.0")){
						switch(method){
						case "GET":
							//System.out.println("This is a get responce");
							switch(requestDirectory){
							case "/shutdown":
								performShutDown(in, out, binaryOut, httpType);
								break;
							case "/control":
								performControl(in, out, httpType);
								break;
							default:
								performFetchingFileOrDirectory(in, out, binaryOut, this.rootDirectory, requestDirectory, "GET", httpType);
								break;
							}
							break;
						case "HEAD":
							//System.out.println("This is a head responce");
							switch(requestDirectory){
							case "/control":
								performEmpty(in, out, httpType);
								break;
							case "/shutdown":
								performShutDown(in, out, binaryOut, httpType);
								break;
							default:
								performFetchingFileOrDirectory(in, out, binaryOut, this.rootDirectory, requestDirectory, "HEAD", httpType);
								break;
							}
							break;
						case "POST":
							// TODO: PAGE NOT FOUND
							performNotImplemented(in, out, httpType);
							break;
						default:
							performEmpty(in, out, httpType);
							break;
						}
					}else{
						performEmpty(in, out, httpType);
					}

					// close out after performing
					in.close();
					out.close();
					binaryOut.close();
				}

			}catch(Exception e){
				_logging.logging(e.getMessage());
				e.printStackTrace();
			}finally{
				try {
					task.close();
					this.indexPage = null;
				} catch (IOException e) {
					_logging.logging(e.getMessage());
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	/*
	 * 
	 */

	/*
	 * find servlet matching
	 */
	public String findServletMatching(String path, HashMap<String, String> servletsMapping){ 

		String matchedResult = null;

		String pathString = path.split("\\?")[0];

		//System.out.println(pathString);
		//System.out.println(queryString);

		// removing trailing / if any
		String pathStringStripR = pathString.replaceAll("/+$", "");
		String pathStringStripL = pathStringStripR.replaceAll("^/+", "");
		//System.out.println(pathStringStripL);

		// getting subconponents of the path
		String[] subPath = pathStringStripL.split("/");
		//		for(String s : subPath){
		//			System.out.println(s);
		//		}

		// find the longest match

		for(int i = 0; i <= subPath.length; i++){
			// adding sub components
			String matchingPath = "/";
			String matchPathFinal = "";
			int index = 0;
			for(int j = 0; j < subPath.length - i; j++){
				matchingPath += subPath[j];
				matchingPath += "/";
				index ++;
			}
			if(index == subPath.length){
				String matchingPathStripR = matchingPath.replaceAll("/+$", "");
				matchPathFinal += matchingPathStripR;
			}else{
				matchingPath += "*";
				matchPathFinal += matchingPath;
			}
			// using matchPathFinal to find if there is a matching servlet
			//System.out.println(matchPathFinal);
			if(servletsMapping.get(matchPathFinal) != null){
				// find the longest match
				matchedResult = servletsMapping.get(matchPathFinal);
				break;
			}
		}
		return matchedResult;
	}

	/*
	 * Servlet Dispatch function
	 */
	public void servletDispatcher(Writer out, OutputStream binaryOut, String servletName) throws IOException{
		// initialize the container
		MyBuffer myBuffer = new MyBuffer();
		MyServletRequest myServletRequest = new MyServletRequest(httpCallRawDictionary, headLinesRawDictionary, myBuffer);
		MyServletResponce myServletResponce = new MyServletResponce(myBuffer);
		// set method of the request
		myServletRequest.setMethod(httpCallRawDictionary.get("Method"));
		// set the parameters pass-in in http call or content
		String queryString = null;
		if(httpCallRawDictionary.get("Method").equalsIgnoreCase("GET")){
			String[] pathString = httpCallRawDictionary.get("path").split("\\?");
			if(pathString.length > 1){
				queryString = pathString[1];
			}
		}else if(httpCallRawDictionary.get("Method").equalsIgnoreCase("POST")){
			if(headLinesRawDictionary.get("content-length") != null){
				queryString = postContentRaw.get(0);
			}else if (headLinesRawDictionary.get("Content-length") != null){
				queryString = postContentRaw.get(0);
			}
		}
		// pass in the params to request if any
		if(queryString != null){
			String[] paramArray = queryString.split("&");
			for(int i = 0; i < paramArray.length; i++){
				String[] paramPair = paramArray[i].split("=");
				if(paramPair.length == 2){
					myServletRequest.setParameter(paramPair[0], paramPair[1]);
				}
			}
		}
		// initialize the servlet
		HttpServlet servletInstance = HttpServer.servlets.get(servletName);
		//MyServletSession session = (MyServletSession) myServletRequest.getSession();
		try {
			servletInstance.service(myServletRequest, myServletResponce);
		} catch (ServletException | IOException e) {
			//if any error, we are going to flush an 500 error message
			//and return
			//clean out the message body
			myBuffer.resetBuffer();
			myBuffer.addHttpResponce(httpCallRawDictionary.get("HTTP"), Integer.toString(500));
			out.write(myBuffer.getFlushedBuffer());
			out.flush();
			_logging.logging(e.getMessage());
			e.printStackTrace();
			return;
		}
		// flush responce
		try {
			myServletResponce.flushBuffer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			myBuffer.resetBuffer();
			myBuffer.addHttpResponce(httpCallRawDictionary.get("HTTP"), Integer.toString(500));
			out.write(myBuffer.getFlushedBuffer());
			out.flush();
			_logging.logging(e.getMessage());
			e.printStackTrace();
			return;
		}
		// output responce
		try {
			out.write(myBuffer.getFlushedBuffer());
			// if it is image or other binary type, then flush as byte data
			if(myServletResponce.getContentType().contains("image")){
				binaryOut.write(myBuffer.getBinaryData());
			}
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			myBuffer.resetBuffer();
			myBuffer.addHttpResponce(httpCallRawDictionary.get("HTTP"), Integer.toString(500));
			out.write(myBuffer.getFlushedBuffer());
			out.flush();
			_logging.logging(e.getMessage());
			e.printStackTrace();
			return;
		}

	}

	/*
	 * Parse the input request
	 */
	public void rawDataParsing(BufferedReader in){
		String header = null;
		int postContentLength = 0;
		boolean isPost = false;
		while(true){

			try {
				header = in.readLine();
			} catch (IOException e) {
				_logging.logging(e.getMessage());
				e.printStackTrace();
				return;
			}

			//System.out.println(header);

			if(header != null){

				if(header.toUpperCase().contains("POST")){
					isPost = true;
				}

				if(header.toUpperCase().contains("CONTENT-LENGTH")){
					String contentLengthString = header.split(":")[1].trim();
					postContentLength = Integer.parseInt(contentLengthString);
				}
			}

			if(header == null || header == "\r" || header == "\n" || header.isEmpty()){
				if(isPost){
					// read-in the body content inside the post
					char[] postContent = new char[postContentLength];
					try {
						in.read(postContent);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					postContentRaw.add(String.valueOf(postContent));
					break;
				}else{
					// no read proceeding needed for get
					break;
				}

			}
			if(!header.toUpperCase().contains("POST ") && !header.toUpperCase().contains("GET ")){
				headLinesRaw.add(header);
			}else{
				httpCallRaw.add(header);
			}
		}
	}

	public void httpCallParsing(String[] lines){
		httpCallRawDictionary.put("Method", lines[0].toUpperCase());
		httpCallRawDictionary.put("HTTP", lines[2].toUpperCase());
		if(lines[1].contains("http://")){
			URL urlPath = null;
			try {
				urlPath = new URL(lines[1]);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			httpCallRawDictionary.put("path", urlPath.getPath());
			httpCallRawDictionary.put("Host", urlPath.getHost());
			httpCallRawDictionary.put("Port", Integer.toString(urlPath.getPort()));
			headLinesRawDictionary.put("Host", urlPath.getHost()+":"+Integer.toString(urlPath.getPort()));
		}else{
			httpCallRawDictionary.put("path", lines[1]);
		}
	}

	public void headLinesParsing(ArrayList<String> headLinesRaw){
		for(String headLine : headLinesRaw){
			//System.out.println(headLine);
			// parsing the headline by using :
			String[] headLinePair = headLine.split(":");
			// depends on the situation
			if(headLinePair.length == 2){
				// exact a pair
				headLinesRawDictionary.put(headLinePair[0], headLinePair[1].trim());
			} else if (headLinePair.length > 2){
				// one to many relations
				String temp = headLinePair[1];
				for(int i = 2; i < headLinePair.length; i++){
					temp += ":" + headLinePair[i];
				}
				headLinesRawDictionary.put(headLinePair[0], temp.trim());
			} else {
				// no value pair
				headLinesRawDictionary.put(headLinePair[0], "");
			}
		}
	}

	// check if meet the security requirements
	public boolean isSecure(String filePath){
		String[] filePathSub = filePath.split("/");
		Stack<String> stack = new Stack<>();
		for(String s : filePathSub){
			if(s.equals("..")){
				if(stack.isEmpty()){
					return false;
				}else{
					stack.pop();
					if(stack.isEmpty()){
						return false;
					}
				}
			}else{
				stack.push(s);
			}
		}
		return true;
	}

	// validation check for http protocal
	public boolean isValid(){
		if(httpCallRawDictionary.get("path")==null || httpCallRawDictionary.get("Method")==null ||httpCallRawDictionary.get("HTTP")==null){
			System.out.println("problem 1");
			return false;
		}
		// check if meet 1.1 protocal requirements
		if(httpCallRawDictionary.get("HTTP").equalsIgnoreCase("HTTP/1.1")){
			if(headLinesRawDictionary.get("Host") == null || !headLinesRawDictionary.get("Host").toLowerCase().contains("localhost") || !headLinesRawDictionary.get("Host").toLowerCase().contains(Integer.toString(portNumber)))
			{
				return false;
			}
			// chech if meet 1.0 protocal requirements
		}else if(httpCallRawDictionary.get("HTTP").equalsIgnoreCase("HTTP/1.0")){
			if(httpCallRawDictionary.get("path").contains("http://")){
				return false;
			}
		}
		return true;
	}

	/**
	 * Different return type producers functions
	 */

	/**
	 * This is the function that is used for shutdown the server
	 * @param in - socket stream input
	 * @param out - socket stream output
	 * @throws IOException 
	 */
	private void performShutDown(Reader in, Writer out, OutputStream binaryOut, String httpType) throws IOException{
		Date currDateAndTime = new Date();	
		out.write("HTTP/" + httpType + " 200 OK\r\n");
		out.write("Date: " + currDateAndTime + "\r\n");
		out.write("Server: HttpServer "+ httpType +"\r\n");
		out.write("Connection: close\r\n");
		out.write("Content-Type: " + "text/html" + "\r\n");
		out.flush();
		// close out after performing
		in.close();
		out.close();
		binaryOut.close();
		//closing out the servlet
		for(HttpServlet s : HttpServer.servlets.values()){
			s.destroy();
		}
		for(int i = 0; i < workers.length; i++){
			if(workers[i].getState()  == Thread.State.RUNNABLE){
				workers[i].interrupt();
			}
		}
		this.serverSocket.close();
		isShutDown = true;
	}

	/**
	 * This is the page load for control page
	 * @param in - socket stream input
	 * @param out - socket stream output
	 * @param httpType - get the http protocol version info
	 * @throws IOException
	 */
	private void performControl(Reader in, Writer out, String httpType) throws IOException{
		// forge response as a body
		String responce = "";
		responce += "<HTML>\r\n";
		responce += "<HEAD><TITLE> Control Panel </TITLE></HEAD>\r\n";
		responce += "<BODY>\r\n";
		responce += "<H0> Control Panel </H0>\r\n";
		responce += "<H1> Full Name: Zhengxuan Wu </H1>\r\n";
		responce += "<H1> Login Name: wuzhengx </H1>\r\n";
		responce += "<a href=" + "'" + "/shutdown" + "'" + ">"
				+ "<button style="+ "'" + "background-color:red " + "'" +"><b>Shutdown Website</b></button>" + "</a>";
		responce += "<li><a href=" + "'" + "localhost:" + Integer.toString(this.portNumber) + "/Error-Logging.txt" + "'" + ">" + "Error Log For Servlets" + "</a></li>\r\n";
		responce += "<H1> Thread States: </H1>\r\n";
		responce += "<ul>\r\n";
		for (int i = 0; i < workers.length; i++) {
			String temp = "<li><H2>" + "thread " + Integer.toString(i+1) + " state: " + workers[i].getState() + " ; Assigned Url : " + workers[i].indexPage + "</H2></li>\r\n";
			responce += temp;
		}
		responce += "</ul>\r\n";
		responce += "<style>\r\n";
		responce += "H0{font-size:25} H1{font-size:20} H2{font-size: 15}";
		responce += "</style>\r\n";
		responce += "</BODY>\r\n";
		responce += "</HTML>";
		Date currDateAndTime = new Date();	
		out.write("HTTP/" + httpType + " 200 OK\r\n");
		out.write("Date: " + currDateAndTime + "\r\n");
		out.write("Server: HttpServer "+ httpType +"\r\n");
		out.write("Connection: close\r\n");
		out.write("Content-Type: " + "text/html" + "\r\n");
		out.write("Content-Length: " + responce.getBytes().length + "\r\n\r\n");
		out.flush();
		out.write(responce);
		out.flush();
	}

	/**
	 * This is the page load for Expected Continue page
	 * @param in - socket stream input
	 * @param out - socket stream output
	 * @param httpType - get the http protocol version info
	 * @throws IOException
	 */
	private void performExpectContinue(Reader in, Writer out, String httpType) throws IOException{
		out.write("HTTP/1.1 100 Continue\r\n\r\n");
		out.flush();
	}

	/**
	 * This is the page load for empty page
	 * @param in - socket stream input
	 * @param out - socket stream output
	 * @param httpType - get the http protocol version info
	 * @throws IOException
	 */
	private void performEmpty(Reader in, Writer out, String httpType) throws IOException{
		String responce = "";
		responce += "<HTML>\r\n";
		responce += "<HEAD><TITLE> Bad Request </TITLE></HEAD>\r\n";
		responce += "<BODY>\r\n";
		responce += "<H0> Bad Request </H0>\r\n";
		responce += "<H1> Full Name: Zhengxuan Wu </H1>\r\n";
		responce += "<H1> Login Name: wuzhengx </H1>\r\n";
		responce += "<style>\r\n";
		responce += "H0{font-size:25} H1{font-size:20}";
		responce += "</style>\r\n";
		responce += "</BODY>\r\n";
		responce += "</HTML>";

		Date currDateAndTime = new Date();	
		out.write("HTTP/" + httpType + " 400 Bad Request\r\n");
		out.write("Date: " + currDateAndTime + "\r\n");
		out.write("Server: HttpServer "+ httpType +"\r\n");
		out.write("Connection: close\r\n");
		out.write("Content-Type: " + "text/html" + "\r\n");
		out.write("Content-Length: " + responce.getBytes().length + "\r\n\r\n");
		out.flush();
		out.write(responce);
		out.flush();
	}


	private void performModifySinceError(Reader in, Writer out, String httpType) throws IOException{
		Date currDateAndTime = new Date();	
		out.write("HTTP/" + httpType + " 304 Not Modified\r\n");
		out.write("Date: " + currDateAndTime + "\r\n");
		out.write("Server: HttpServer "+ httpType +"\r\n");
		out.write("Connection: close\r\n");
		out.write("Content-Type: " + "text/html" + "\r\n");
		out.flush();
	}

	private void performUnModifySinceError(Reader in, Writer out, String httpType) throws IOException{
		Date currDateAndTime = new Date();	
		out.write("HTTP/" + httpType + " 412 Precondition Failed\r\n");
		out.write("Date: " + currDateAndTime + "\r\n");
		out.write("Server: HttpServer "+ httpType +"\r\n");
		out.write("Connection: close\r\n");
		out.write("Content-Type: " + "text/html" + "\r\n");
		out.flush();
	}

	/**
	 * This is the page load for non-exist page
	 * @param in - socket stream input
	 * @param out - socket stream output
	 * @param httpType - get the http protocol version info
	 * @throws IOException
	 */
	private void performNoneExistError(Reader in, Writer out, String httpType) throws IOException{

		String responce = "";
		responce += "<HTML>\r\n";
		responce += "<HEAD><TITLE> Not Found </TITLE></HEAD>\r\n";
		responce += "<BODY>\r\n";
		responce += "<H0> Not Found </H0>\r\n";
		responce += "<H1> Full Name: Zhengxuan Wu </H1>\r\n";
		responce += "<H1> Login Name: wuzhengx </H1>\r\n";
		responce += "<style>\r\n";
		responce += "H0{font-size:25} H1{font-size:20}";
		responce += "</style>\r\n";
		responce += "</BODY>\r\n";
		responce += "</HTML>";

		Date currDateAndTime = new Date();	
		out.write("HTTP/" + httpType + " 404 Not Found\r\n");
		out.write("Date: " + currDateAndTime + "\r\n");
		out.write("Server: HttpServer "+ httpType +"\r\n");
		out.write("Connection: close\r\n");
		out.write("Content-Type: " + "text/html" + "\r\n");
		out.write("Content-Length: " + responce.getBytes().length + "\r\n\r\n");
		out.flush();
		out.write(responce);
		out.flush();
	}


	/**
	 * This is the page load for not implemented page
	 * @param in - socket stream input
	 * @param out - socket stream output
	 * @param httpType - get the http protocol version info
	 * @throws IOException
	 */
	private void performNotImplemented(Reader in, Writer out, String httpType) throws IOException{
		String responce = "";
		responce += "<HTML>\r\n";
		responce += "<HEAD><TITLE> Not Implemented </TITLE></HEAD>\r\n";
		responce += "<BODY>\r\n";
		responce += "<H0> Not Implemented </H0>\r\n";
		responce += "<H1> Full Name: Zhengxuan Wu </H1>\r\n";
		responce += "<H1> Login Name: wuzhengx </H1>\r\n";
		responce += "<style>\r\n";
		responce += "H0{font-size:25} H1{font-size:20}";
		responce += "</style>\r\n";
		responce += "</BODY>\r\n";
		responce += "</HTML>";

		Date currDateAndTime = new Date();	
		out.write("HTTP/" + httpType + " 501 Not Implemented\r\n");
		out.write("Date: " + currDateAndTime + "\r\n");
		out.write("Server: HttpServer "+ httpType +"\r\n");
		out.write("Connection: close\r\n");
		out.write("Content-Type: " + "text/html" + "\r\n");
		out.write("Content-Length: " + responce.getBytes().length + "\r\n\r\n");
		out.flush();
		out.write(responce);
		out.flush();

	}

	/**
	 * This is the page load for fetching the file page
	 * @param in - socket stream input
	 * @param out - socket stream output
	 * @param binaryOut - socket stream for binary data stream
	 * @param Directory - the directory of the file
	 * @param httpType - the http version
	 * @throws IOException
	 * @throws ParseException 
	 */
	private void performFetchingFileWithGet(Reader in, Writer out, OutputStream binaryOut, File Directory, String httpType) throws IOException, ParseException{

		// check if modify first
		if(headLinesRawDictionary.get("If-Modified-Since") != null){
			DateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
			Date date = df.parse(headLinesRawDictionary.get("If-Modified-Since").trim());
			if(Directory.lastModified() < date.getTime()){
				performModifySinceError(in, out, httpType);
				return;
			}
		}

		// check if modify first
		if(headLinesRawDictionary.get("If-Unmodified-Since") != null){
			DateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
			Date date = df.parse(headLinesRawDictionary.get("If-Modified-Since").trim());
			if(Directory.lastModified() > date.getTime()){
				performUnModifySinceError(in, out, httpType);
				return;
			}
		}

		// get content type of this file
		String contentType = getContentType(Directory);
		// the html body
		Date currDateAndTime = new Date();	
		out.write("HTTP/" + httpType + " 200 OK\r\n");
		out.write("Date: " + currDateAndTime + "\r\n");
		out.write("Server: HttpServer "+ httpType +"\r\n");
		out.write("Connection: close\r\n");
		out.write("Content-Type: " + contentType + "\r\n");
		// open and read the file into buffer
		if(contentType.contains("image")){
			DataInputStream fileDescription = new DataInputStream(new BufferedInputStream(new FileInputStream(Directory)));
			byte[] fileData = new byte[(int) Directory.length()];
			fileDescription.readFully(fileData);
			fileDescription.close();
			out.write("Content-Length: " + fileData.length + "\r\n\r\n");
			// write the file data
			binaryOut.write(fileData);
			binaryOut.flush();
			return;
		}else{
			FileReader reader = new FileReader(Directory);
			char[] chars = new char[(int) Directory.length()];
			out.write("Content-Length: " + Directory.length() + "\r\n\r\n");
			reader.read(chars);
			System.out.println(new String(chars));
			String content = new String(chars);
			out.write(content);
			out.flush();
			return;
		}
	}

	/**
	 * This is the page load for fetching the file page
	 * @param in - socket stream input
	 * @param out - socket stream output
	 * @param binaryOut - socket stream for binary data stream
	 * @param Directory - the directory of the file
	 * @param httpType - the http version
	 * @throws IOException
	 */
	private void performFetchingFileWithHead(Reader in, Writer out, File Directory, String httpType) throws IOException{
		// get content type of this file
		String contentType = getContentType(Directory);
		// open and read the file into buffer
		DataInputStream fileDescription = new DataInputStream(new BufferedInputStream(new FileInputStream(Directory)));
		byte[] fileData = new byte[(int) Directory.length()];
		fileDescription.readFully(fileData);
		fileDescription.close();
		// the header body
		Date currDateAndTime = new Date();	
		out.write("HTTP/" + httpType + " 200 OK\r\n");
		out.write("Date: " + currDateAndTime + "\r\n");
		out.write("Server: HttpServer "+ httpType +"\r\n");
		out.write("Connection: close\r\n");
		out.write("Content-Type: " + contentType + "\r\n");
		out.write("Content-Length: " + fileData.length + "\r\n\r\n");

		return;
	}

	/**
	 * This is the page load for fetching the directory page
	 * @param Directory - the directory of the file
	 */
	private String getBodyFetchingDirectory(File Directory) throws IOException{
		File[] files = Directory.listFiles();
		String responce = "";
		responce+= "<HTML>\r\n";
		responce+= "<HEAD><TITLE> List Of Files </TITLE></HEAD>\r\n";
		responce+= "<BODY>\r\n";
		responce+= "<H0> List Of Files In This Directory </H0>\r\n";
		responce+= "<H1> Full Name: Zhengxuan Wu </H1>\r\n";
		responce+= "<H1> Login Name: wuzhengx </H1>\r\n";
		responce+= "<H1> Files: </H1>\r\n";
		responce+= "<ul>\r\n";
		for (File file : files) {
			String absolutePath = file.getAbsolutePath();
			System.out.println(absolutePath);
			String relativePath = absolutePath.replace(this.rootDirectory.toString(), "");
			String temp = "<li><a href=" + "'" + "localhost:" + Integer.toString(this.portNumber) + relativePath + "'" + ">" + "File Name:  " + file.getName() + "</a></li>\r\n";
			responce += temp;
		}
		responce+= "</ul>\r\n";
		responce += "<style>\r\n";
		responce += "H0{font-size:25} H1{font-size:20} H2{font-size: 15}";
		responce += "</style>\r\n";
		responce+= "</BODY>\r\n";
		responce+= "</HTML>";
		return responce;
	}

	/**
	 * This is the page load for fetching the directory page
	 * @param in - socket stream input
	 * @param out - socket stream output
	 * @param binaryOut - socket stream for binary data stream
	 * @param Directory - the directory of the file
	 * @param httpType - the http version
	 * @throws IOException
	 */
	private void performFetchingDirectoryWithGet(Reader in, Writer out, File Directory, String httpType) throws IOException{
		Date currDateAndTime = new Date();
		String responce = getBodyFetchingDirectory(Directory);	
		out.write("HTTP/" + httpType + " 200 OK\r\n");
		out.write("Date: " + currDateAndTime + "\r\n");
		out.write("Server: HttpServer "+ httpType +"\r\n");
		out.write("Connection: close\r\n");
		out.write("Content-Type: " + "text/html" + "\r\n");
		out.write("Content-Length: " + responce.getBytes().length + "\r\n\r\n");
		// the list of file body
		out.flush();
		out.write(responce);
		out.flush();
	}

	/**
	 * This is the page load for fetching the directory page
	 * @param in - socket stream input
	 * @param out - socket stream output
	 * @param binaryOut - socket stream for binary data stream
	 * @param Directory - the directory of the file
	 * @param httpType - the http version
	 * @throws IOException
	 */
	private void performFetchingDirectoryWithHead(Reader in, Writer out, File Directory, String httpType) throws IOException{
		Date currDateAndTime = new Date();	
		String responce = getBodyFetchingDirectory(Directory);
		out.write("HTTP/" + httpType + " 200 OK\r\n");
		out.write("Date: " + currDateAndTime + "\r\n");
		out.write("Server: HttpServer "+ httpType +"\r\n");
		out.write("Connection: close\r\n");
		out.write("Content-Type: " + "text/html" + "\r\n\r\n");
		out.write("Content-Length: " + responce.getBytes().length + "\r\n\r\n");
		// the list of file body
		out.flush();
		out.write(responce);
		out.flush();
	}

	/**
	 * Main function for processing fetching directory as well as file
	 * @param in - socket input stream
	 * @param out - socket output stream
	 * @param binaryOut - binary output stream
	 * @param rootDirectory - the root directory of the server
	 * @param requestDirectory - the request directory for this task
	 * @param method - the method of this request
	 * @param httpType - the http protocol version of this request
	 * @throws IOException
	 * @throws ParseException 
	 */
	private void performFetchingFileOrDirectory(Reader in, Writer out, OutputStream binaryOut, File rootDirectory, String requestDirectory, String method, String httpType) throws IOException, ParseException{
		// example file directory : "/home/cis555/git/555-hw1/www/index.html"
		File completeDirectory = new File(rootDirectory.getAbsolutePath(), requestDirectory);
		if(!completeDirectory.exists()){
			performNoneExistError(in, out, httpType);
		}else if(completeDirectory.isDirectory()){
			if(method.equals("GET"))
				performFetchingDirectoryWithGet(in, out, completeDirectory, httpType);
			else
				performFetchingDirectoryWithHead(in, out, completeDirectory, httpType);
		}else if(completeDirectory.isFile()){
			if(method.equals("GET"))
				if(completeDirectory.canRead()){
					performFetchingFileWithGet(in, out, binaryOut, completeDirectory, httpType);
				}else
					performNoneExistError(in, out, httpType);
			else
				if(completeDirectory.canRead())
					performFetchingFileWithHead(in, out, completeDirectory, httpType);
				else
					performNoneExistError(in, out, httpType);
		}else{
			performNoneExistError(in, out, httpType);
		}
		return;
	}

	// Auxiliary function to detect content type
	private String getContentType(File directory){
		int indexOfExtension = directory.getAbsolutePath().lastIndexOf('.');
		String extension = directory.getAbsolutePath().substring(indexOfExtension);
		String mimeType = "";
		switch(extension){
		case ".html":
			mimeType = "text/html";
			break;
		case ".txt":
			mimeType = "text/plain";
			break;
		case ".png":
			mimeType = "image/png";
			break;
		case ".gif":
			mimeType = "image/gif";
			break;
		case ".jpg":
			mimeType = "image/jpeg";
			break;
		default:
			break;
		}
		return mimeType;
	}

}
