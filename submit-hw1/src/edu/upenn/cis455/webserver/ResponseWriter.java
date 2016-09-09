package edu.upenn.cis455.webserver;


import java.io.OutputStream;
import java.io.PrintWriter;

public class ResponseWriter extends PrintWriter {
	
	// head and body for a response;
	private StringBuffer response_head;
	private StringBuffer response_body;
	
	public ResponseWriter(OutputStream outputStream){
		super(outputStream);
		response_body = new StringBuffer();
		response_head = new StringBuffer();
	}
	
	public void setHead(StringBuffer head){
		response_head = head;
	}
	
	public void addHead(String head){
		response_head.append(head).append("\r\n");	
	}
	
	public void println(String str){
		response_body.append(str).append("\r\n");//
	}
	
	public void flush(){
		if(response_head.length() != 0){
			int bodyLength = response_body.toString().getBytes().length;
			response_head.append("Content-Length: " + bodyLength + "\r\n");	
			super.print(response_head.toString() + "\r\n");
			super.print(response_body.toString());
			super.flush();
		}
	}
	
	public void close(){
		if(response_head.length() == 0){
			return;
		}
		else super.close();
	}
}
