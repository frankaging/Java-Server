package edu.upenn.cis.cis455.webserver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ErrorLogging {
	
	/*
	 * Singleton Logger
	 */
	
	String fileName;
	BufferedWriter output = null;
	private static ErrorLogging _logger = null;
	
	private ErrorLogging(String fileName){
		File file = new File(fileName);
        try {
			output = new BufferedWriter(new FileWriter(file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ErrorLogging getLogger(String fileName){
		if(_logger == null){
			_logger = new ErrorLogging(fileName);
		}
		return _logger;
	}
	
	public void logging(String message){
		try {
			this.output.write(message);
			this.output.write("\n");
			this.output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void close(){
		try {
			this.output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
