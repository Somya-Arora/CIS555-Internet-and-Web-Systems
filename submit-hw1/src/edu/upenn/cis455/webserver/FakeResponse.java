package edu.upenn.cis455.webserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author tjgreen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FakeResponse implements HttpServletResponse {

	private FakeRequest request;
	private FakeSession session;
	HashMap<String, StringBuffer> response_headmap = new HashMap<String, StringBuffer>();
	StringBuffer response = new StringBuffer();
	
	Vector<Cookie> cookies = new Vector<Cookie>();
	
	String version;
	String contentType;
	int contentLength;
	
	//most important!!!!!!!!
	OutputStream out;  
	StringBuffer response_head=new StringBuffer();
	StringBuffer response_body=new StringBuffer();
	ResponseWriter response_writer;
	
	//others
	private String encoding;
	int status;
	String message;
	private int bufferSize;
	private boolean isCommitted=false;
	
	public FakeResponse(FakeRequest fakeRequest, OutputStream out) throws IOException{
		request = fakeRequest;
		this.out = out;
		response_writer = new ResponseWriter(new DataOutputStream(out));
		version = request.getProtocol();
		addHeader("Connection", "close");
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	public void addCookie(Cookie arg0) {
		// TODO Auto-generated method stub
		System.out.println("cookie's name value time: "+arg0.getName()+"  "+arg0.getValue()+"  "+arg0.getMaxAge());
		if(cookies != null)
		{
			cookies.add(arg0);
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(String arg0) {
		// TODO Auto-generated method stub
		return response_headmap.containsKey(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)-
	 */
	public String encodeURL(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	public void sendError(int status, String message) throws IOException {
		// TODO Auto-generated method stub
		if(isCommitted())
			throw new IllegalStateException();
		setStatus(status);
		response_writer.println("<html><body>");
		response_writer.println(version + " Status Code: " + status + 
				"; Error: " + message );
		response_writer.println("</body></html>");
		flushBuffer();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	public void sendError(int status) throws IOException {
		// TODO Auto-generated method stub
		if(isCommitted())
			throw new IllegalStateException();
		setStatus(status);
		response_writer.println("Status Code: " + status);
		flushBuffer();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String url) throws IOException {
		setStatus(302);
		response_writer.println(request.getProtocol() + " 302 REDIRECT");
		response_writer.println("Location: " + url);
		response_writer.println("<html><body>");
		response_writer.println("<h1>HTTP 302 Redirect</h1><br/><br/><a href=\"" + url + "\">" + url + "</a>");
		response_writer.println("</body></html>");
		response_writer.flush();
		isCommitted = true;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(String name, long date) {
		// TODO Auto-generated method stub
		//make all the date in one kind of format
		SimpleDateFormat dateFormat=new SimpleDateFormat("EEE, dd MM HH:mm:ss zzz yyyy");
		response_headmap.put(name, new StringBuffer(dateFormat.format(date)));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(String name, long date) {
		// TODO Auto-generated method stub
		SimpleDateFormat dateFormat=new SimpleDateFormat("EEE, dd MM HH:mm:ss zzz yyyy");
		// may have a lot of value in one name;
		if(containsHeader(name))
			response_headmap.get(name).append(", " + dateFormat.format(date));
		response_headmap.put(name, new StringBuffer(dateFormat.format(date)));

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String key, String value) {
		// TODO Auto-generated method stub
		// If the map previously contained a mapping for the key, the old value is replaced
//		if(containsHeader(key))
//			response_headmap.remove(key);
		response_headmap.put(key, new StringBuffer(value));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String key, String value) {
		// TODO Auto-generated method stub
		if(!response_headmap.containsKey(key))
			response_headmap.put(key, new StringBuffer(value));
		else
			response_headmap.put(key, response_headmap.get(key).append(", ").append(value));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	public void setIntHeader(String key, int value) {
		// TODO Auto-generated method stub
//		if(containsHeader(key))
//			response_headmap.remove(key);
		response_headmap.put(key, new StringBuffer(value));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(String key, int value) {
		// TODO Auto-generated method stub
		if(!response_headmap.containsKey(key))
			response_headmap.put(key, new StringBuffer(value));
		else
			response_headmap.put(key, response_headmap.get(key).append(", ").append(value));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	public void setStatus(int arg0) {
		// TODO Auto-generated method stub
		status = arg0;
		switch(status){
		case 200: message = "OK";
		case 301: message = "Moved Permanently";
		case 302: message = "Moved Temporarily";
		break;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	//Deprecated
	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return encoding==null?"ISO-8859-1":encoding;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	public String getContentType() {
		// TODO Auto-generated method stub
		return contentType == null?"text/html":contentType;
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
		return response_writer;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0) {
		// TODO Auto-generated method stub
		if(!isCommitted()){
			encoding = arg0;
			setHeader("Character-Encoding", arg0);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	public void setContentLength(int arg0) {
		// TODO Auto-generated method stub
		if(!isCommitted()&&!response_headmap.containsKey("Content-Length")){
			contentLength = arg0;
			setIntHeader("Content-Length", arg0);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	public void setContentType(String arg0) {
		// TODO Auto-generated method stub
		if(!isCommitted()){
		contentType = arg0;
		setHeader("Content-Type", arg0);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(int arg0) {
		// TODO Auto-generated method stub
		if(!isCommitted())
			bufferSize = arg0;
		else throw new IllegalStateException();

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return bufferSize;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub
		if(isCommitted())
			return;
		
		//the session must gotten from there!!!since the servlet may change the session attribute or create the session~! keep in mind you stupid!! 
		session = (FakeSession) request.getSession(false);//return the session the request have, do not create if not exist.
		//System.out.println("run at fake response ln58: session get: "+session==null?"null":"session is not null");
		//build header
		status = 200;
		ResponseInitialBuilder(status);
		for(String key : response_headmap.keySet()){
			response_head.append(key + ": " + response_headmap.get(key).toString()+"\r\n");
		}
		// build cookies
		for(Cookie cookie : cookies){
			//System.out.println("cookie maxAge: " + cookie.getMaxAge());
			long expireTime;
			if(cookie.getMaxAge() > 0  && session != null)
				expireTime = session.getCreationTime() + 1000 * cookie.getMaxAge();
			expireTime = new Date().getTime() + 1000 * cookie.getMaxAge();
			//formalize expireTime
			DateFormat format = new SimpleDateFormat("EEE, d-MMM-yyyy HH:mm:ss z");
			format.setTimeZone(TimeZone.getTimeZone("GMT"));
			String expireTimeStr=format.format(new Date(expireTime));
			response_head.append("Set-Cookie: " + cookie.getName() + "=" + cookie.getValue() +
					"; expires=" + expireTimeStr + "\r\n") ;
		}
			
		if(session!=null&&session.isValid()){//may not be null after invalidation
			System.out.println("run at fake response ln355: session get: "+session.getId());
			response_head.append("Set-Cookie: " + "JSESSIONID=" + session.getId() + "\r\n");
		}
		
		System.out.println("respone_head:\r\n" + response_head.toString());
		// deliever the head response to the special writer
		response_writer.setHead(response_head);
	
		response_writer.flush();//flush head first, then body.the out.println in the servlet will be excuted here!!!
		response_writer.close();
		isCommitted = true;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		// TODO Auto-generated method stub
			response_writer = new ResponseWriter(new DataOutputStream(out));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return isCommitted;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		// TODO Auto-generated method stub
			// reset all 
			setStatus(200);
			response_headmap = new HashMap<String, StringBuffer>();
			response_head = new StringBuffer();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void ResponseInitialBuilder(int code)
	  {
		  String initial = version;
	      switch(code){
	      case 200: initial += " 200 OK\r\n";break;
	      case 403: initial += " 403 FORBIDDEN\r\n";break;     
	      case 404: initial += " 404 NOT FOUND\r\n";break;
	      case 501: initial += " 501 Not Implemented or Unsupported File\r\n";break;
	      case 100: initial += " 100 CONTINUE\r\n\r\n";break;
	      case 400: initial += " 400 BAD REQUEST\r\n";break;
	      case 302: initial += " 302 REDIRECT\r\n";break; 
	      }
	      response_writer.write(initial);
	  }
}
