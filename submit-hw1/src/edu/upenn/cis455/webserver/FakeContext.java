package edu.upenn.cis455.webserver;

import javax.servlet.*;
import java.util.*;

/**
 * @author Nick Taylor
 */
class FakeContext implements ServletContext {
	//attributes: like exchanging data and objects between servlets in the same web application; they have nothing to do with properties of the request.
	//such as session-timeout, servletMappings, (in general all the servlet container should have there objects) etc.
	private HashMap<String,Object> attributes;
	//used to store the initParams, specific params due to different container.
	private HashMap<String,String> initParams;
	
	public FakeContext() {
		attributes = new HashMap<String,Object>();
		initParams = new HashMap<String,String>();
	}
	
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	
	public Enumeration<String> getAttributeNames() {
		Set<String> keys = attributes.keySet();
		Vector<String> atts = new Vector<String>(keys);
		//Enumeration<String> atts1 = Collections.enumeration(initParams.keySet());
		return atts.elements();
	}
	
	public ServletContext getContext(String name) {//????is this correct?
		if(name.equals("/"))
			return this;
		return null;
	}
	
	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	
	public Enumeration<String> getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> initPName = new Vector<String>(keys);
		return initPName.elements();
	}
	
	public int getMajorVersion() {
		return 2;
	}
	
	//NR - not required
	public String getMimeType(String file) {
		return null;
	}
	
	public int getMinorVersion() {
		return 4;
	}
	
	//NR
	public RequestDispatcher getNamedDispatcher(String name) {
		return null;
	}

	public String getRealPath(String path) {
		//path have to start with a "/"
		return Manager.root_directory+path;
	}
	
	public RequestDispatcher getRequestDispatcher(String name) {
		return null;
	}
	
	//NR
	public java.net.URL getResource(String path) {
		return null;
	}
	//NR
	public java.io.InputStream getResourceAsStream(String path) {
		return null;
	}
	//NR
	public java.util.Set getResourcePaths(String path) {
		return null;
	}
	
	//Returns the name and version of the servlet container on which the servlet is running.
	public String getServerInfo() {
		return "Name: Menglu's Servlet Container \n Version: 2";
	}
	
	//Deprecated
	public Servlet getServlet(String name) {
		return null;
	}
	
	
	public String getServletContextName() {
		return "ServletContextName: server";
	}

	//Deprecated
	public Enumeration getServletNames() {
		return null;
	}
	//Deprecated
	public Enumeration getServlets() {
		return null;
	}
	//Deprecated
	public void log(Exception exception, String msg) {
		log(msg, (Throwable) exception);
	}
	
	//Writes the specified message to a servlet log file, usually an event log.
	//just pribt out!
	public void log(String msg) {
		System.err.println(msg);
	}
	
	public void log(String message, Throwable throwable) {
		System.err.println(message);
		throwable.printStackTrace(System.err);
	}
	
	public void removeAttribute(String name) {
		attributes.remove(name);
	}
	
	public void setAttribute(String name, Object object) {
		attributes.put(name, object);
	}
	
	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}
}
