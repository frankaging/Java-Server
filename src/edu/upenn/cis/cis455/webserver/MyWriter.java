package edu.upenn.cis.cis455.webserver;

import java.io.PrintWriter;

public class MyWriter extends PrintWriter{
	
	public ErrorLogging _logging = HttpServer._logging;
	
	public MyBuffer myBuffer;
	public MyServletResponce myServletResponce;
	
	public MyWriter(MyBuffer myBuffer, MyServletResponce mySerletResponce){
		super(System.out, true);
		this.myBuffer = myBuffer;
		this.myServletResponce = mySerletResponce;
	}
	
	@Override
	public void flush(){
		return;
	}
	
	@Override
	public void close(){
		return;
	}
	
	public void print(String obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				_logging.logging(e.getMessage());
				e.printStackTrace();
			}
		}else{
			myBuffer.append(obj);
		}
	}
	
	public void println(String obj){
		if(this.myServletResponce.isCommitted()){
			
			try {
				throw new Exception();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				_logging.logging(e.getMessage());
				e.printStackTrace();
			}
		}else{
			myBuffer.append(obj);
			myBuffer.append("\n");
		}
	}
	
	public void print(boolean obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				_logging.logging(e.getMessage());
				e.printStackTrace();
			}
		}else{
			if(obj){
				myBuffer.append("true");
			}else{
				myBuffer.append("false");
			}
		}
	}
	
	public void print(int obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				_logging.logging(e.getMessage());
				e.printStackTrace();
			}
		}else{
			myBuffer.append(Integer.toString(obj));
		}
	}
	
	public void print(char obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				_logging.logging(e.getMessage());
				e.printStackTrace();
			}
		}else{
			String s = "" + obj;
			myBuffer.append(s);
		}
	}
	
	public void print(long obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				_logging.logging(e.getMessage());
				e.printStackTrace();
			}
		}else{
			myBuffer.append(Long.toString(obj));
		}
	}
	
	public void print(float obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				_logging.logging(e.getMessage());
				e.printStackTrace();
			}
		}else{
			myBuffer.append(Float.toString(obj));
		}
	}
	
	public void print(double obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				_logging.logging(e.getMessage());
				e.printStackTrace();
			}
		}else{
			myBuffer.append(Double.toString(obj));
		}
	}
	
	public void print(char[] obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			myBuffer.append(obj.toString());
		}
	}
	
	public void print(Object obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			myBuffer.append(obj.toString());
		}
	}
	
	public void print(byte[] obj){
		myBuffer.appendBinaryData(obj);
	}
	
	public void println(boolean obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			if(obj){
				myBuffer.append("true");
			}else{
				myBuffer.append("false");
			}
		}
		myBuffer.append("\n");
	}
	
	public void println(int obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			myBuffer.append(Integer.toString(obj));
		}
		myBuffer.append("\n");
	}
	
	public void println(char obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			String s = "" + obj;
			myBuffer.append(s);
		}
		myBuffer.append("\n");
	}
	
	public void println(long obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			myBuffer.append(Long.toString(obj));
		}
		myBuffer.append("\n");
	}
	
	public void println(float obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			myBuffer.append(Float.toString(obj));
		}
		myBuffer.append("\n");
	}
	
	public void println(double obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			myBuffer.append(Double.toString(obj));
		}
		myBuffer.append("\n");
	}
	
	public void println(char[] obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			myBuffer.append(obj.toString());
		}
		myBuffer.append("\n");
	}
	
	public void println(Object obj){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			myBuffer.append(obj.toString());
		}
		myBuffer.append("\n");
	}
	
	public void println(byte[] obj){
		myBuffer.appendBinaryData(obj);
	}
	
	public void write(int c){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			myBuffer.append(Integer.toString(c));
		}
	}
	
	public void write(char[] buf, int off, int len){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			String s = new String(buf).substring(off, off+len+1);
			myBuffer.append(s);
		}
	}
	
	public void write(char[] buf){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			myBuffer.append(buf.toString());
		}
	}
	
	public void write(String s, int off, int len){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			String st = s.substring(off, off+len+1);
			myBuffer.append(st);
		}
	}
	
	public void write(String s){
		if(this.myServletResponce.isCommitted()){
			try {
				throw new Exception();
			} catch (Exception e) {
				_logging.logging(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			myBuffer.append(s);
		}
	}
	
	public void write(byte[] obj){
		myBuffer.appendBinaryData(obj);
	}
}
