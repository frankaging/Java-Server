package edu.upenn.cis.cis455.webserver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.servlet.http.Cookie;

public class MyBuffer {

	/**
	 * Logger for this particular class
	 */
	public ErrorLogging _logging = HttpServer._logging;
	
	/*
	 * Self0defined buffer to store all the information about response
	 */
	
	public StringBuffer httpResponce;
	public StringBuffer headLinesResponce;
	public StringBuffer cookieLinesResponce;
	public StringBuffer messageBodyResponce;
	
	public String httpResponceFinal = "";
	public String headLinesResponceFinal = "";
	public String messageBodyResponceFinal = "";
	
	public byte[] binaryData;
	
	public ArrayList<Cookie> cookiesList = new ArrayList<>();
	
	public static HashMap<Integer, String> CODEMATCH = new HashMap<>();
	
	public MyBuffer(){
		httpResponce = new StringBuffer();
		headLinesResponce = new StringBuffer();
		cookieLinesResponce = new StringBuffer();
		messageBodyResponce = new StringBuffer();
		CODEMATCH.put(200, "OK");
		CODEMATCH.put(302, "Redirect");
		CODEMATCH.put(400, "Bad Request");
		CODEMATCH.put(403, "Forbidden");
		CODEMATCH.put(404, "Not Found");
		CODEMATCH.put(500, "Server Error");
		CODEMATCH.put(501, "Not Implement");
		CODEMATCH.put(505, "Not Support");
	}
	
	// support image transimission
	public void appendBinaryData(byte[] obj){
		this.binaryData = obj;
	}
	
	//message body responce
	public void append(String obj){
		messageBodyResponce.append("" + String.valueOf(obj));
	}
	
	// clean the message body
	public void resetBuffer(){
		messageBodyResponce.delete(0, messageBodyResponce.length());
	}
	
	// set redirect message body
	public void redirectMessageBody(String redirectURL){
		
		messageBodyResponce.append("<html>\r\n");
		messageBodyResponce.append("<head>\r\n");
		messageBodyResponce.append("<meta http-equiv= Refresh content=0; url=" + "'" + redirectURL + "'" + "/>");
		messageBodyResponce.append("</head>\r\n");
		messageBodyResponce.append("<body>");
		messageBodyResponce.append("<p>Please follow <a href=" + "'" + redirectURL + "'" + ">this link</a>.</p>");
		messageBodyResponce.append("</body>");
		messageBodyResponce.append("</html>");
		
	}
	
	// clean the whole message
	public void reset(){
		httpResponce.delete(0, httpResponce.length());
		headLinesResponce.delete(0, headLinesResponce.length());
		messageBodyResponce.delete(0, messageBodyResponce.length());
	}
	
	//http line responce
	public void addHttpResponce(String httpVersion, String statusCode, String statusDescription){
		httpResponce.delete(0, httpResponce.length());
		httpResponce.append(httpVersion);
		httpResponce.append(" ");
		httpResponce.append(statusCode);
		httpResponce.append(" ");
		httpResponce.append(statusDescription);
		httpResponce.append("\r\n");
	}
	
	public void addHttpResponce(String httpVersion, String statusCode){
		httpResponce.delete(0, httpResponce.length());
		httpResponce.append(httpVersion);
		httpResponce.append(" ");
		httpResponce.append(statusCode);
		httpResponce.append(" ");
		httpResponce.append(CODEMATCH.get(Integer.parseInt(statusCode)));
		httpResponce.append("\r\n");
	}
	
	//headers responce
	public void addHeadLinesResponce(Hashtable<String, ArrayList<String>> headLinesResponceDictionary){
		
		if(headLinesResponceDictionary.get("Content-length") == null){
			ArrayList<String> temp = new ArrayList<>();
			temp.add(Integer.toString(messageBodyResponce.toString().length()));
			headLinesResponceDictionary.put("Content-length", temp);
		}
		
		for(String key : headLinesResponceDictionary.keySet()){
			// we will process set cookie separately
			if(!key.equals("Set-Cookie")){
				headLinesResponce.append(key);
				headLinesResponce.append(":");
				ArrayList<String> headerValues = headLinesResponceDictionary.get(key);
				if(headerValues.isEmpty()){
					headLinesResponce.append("");
				}else{
					// iterate through headers
					for(int i = 0; i < headerValues.size(); i++){
						headLinesResponce.append(headerValues.get(i));
						if(i != headerValues.size() - 1)
							headLinesResponce.append(",");
					}
				}
				headLinesResponce.append("\r\n");
			}
		}
		headLinesResponce.append("\r\n");
	}
	
	// cookies responce
	public void addCookieLinesResponce(){
		for(Cookie cookie : MyServletRequest.cookies){
			// no expiration date
			if(cookie.getMaxAge() == -1){
				cookieLinesResponce.append("Set-Cookie" + ":" + cookie.getName() + "=" + cookie.getValue());
				cookieLinesResponce.append("\r\n");
			}else{
				Date expdate= new Date();
				expdate.setTime (expdate.getTime() + (cookie.getMaxAge() * 1000));
				DateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
				df.setTimeZone(TimeZone.getTimeZone("GMT"));
				String cookieExpire = "expires=" + df.format(expdate);
				cookieLinesResponce.append("Set-Cookie" + ":" + cookie.getName() + "=" + cookie.getValue());
				cookieLinesResponce.append(";");
				cookieLinesResponce.append(cookieExpire);
				cookieLinesResponce.append("\r\n");
			}
		}
	}
	
	// 
	
	public void flushBuffer(int bufferSize){
		httpResponceFinal = httpResponce.toString();
		headLinesResponceFinal = headLinesResponce.toString();
		if(bufferSize == 0){
			messageBodyResponceFinal = messageBodyResponce.toString();
		}else{
			messageBodyResponceFinal = messageBodyResponce.toString().substring(0, bufferSize);
		}
		
	}
	
	public String getFlushedBuffer(){
		// set cookies now
		addCookieLinesResponce();
		//System.out.println(cookieLinesResponce.toString());
		
		return httpResponceFinal + cookieLinesResponce.toString() + headLinesResponceFinal + messageBodyResponceFinal;
	}
	
	public byte[] getBinaryData(){
		return this.binaryData;
	}
	
}
