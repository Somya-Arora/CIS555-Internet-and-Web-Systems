package edu.upenn.cis455.webserver;

import javax.servlet.*;
import java.util.*;

/**
 * @author Nick Taylor
 */
class FakeConfig implements ServletConfig {
	private String name;
	private FakeContext context;
	private HashMap<String,String> initParams;
	
	public FakeConfig(String name, FakeContext context) {
		this.name = name;
		this.context = context;
		initParams = new HashMap<String,String>();
	}

	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	
	public Enumeration<String> getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> initPNames = new Vector<String>(keys);
		//Enumeration<String> initPNames = Collections.enumeration(initParams.keySet());
		return initPNames.elements();
	}
	
	public ServletContext getServletContext() {
		return context;
	}
	
	public String getServletName() {
		return name;
	}

	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}
}
