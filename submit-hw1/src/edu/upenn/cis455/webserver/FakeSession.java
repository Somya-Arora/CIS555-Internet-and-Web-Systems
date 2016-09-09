package edu.upenn.cis455.webserver;

import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * @author Todd J. Green
 */
class FakeSession implements HttpSession {

	private FakeContext context;
	private long creationTime;
	private long lastAccessedTime;
	private int maxInactiveInterval;
	private String id;
	
	private boolean isNew;
	//
	private Properties m_props = new Properties();
	private boolean isValid = true;
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getCreationTime()
	 */
	public FakeSession(FakeContext context)
	{
		this.context=context;
		creationTime = System.currentTimeMillis();
		lastAccessedTime = creationTime;
		if(context.getAttribute("session-timeout") != null)
			setMaxInactiveInterval(Integer.parseInt(context.getAttribute("session-timeout").toString()));
		//id;
		UUID uid = UUID.randomUUID();
		id=uid.toString();

		isNew = true;
		isValid = true;	
	}
	
		/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String key) {
		// TODO Auto-generated method stub
		if(!isValid)
			throw new IllegalStateException();
		return m_props.get(key);
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		// TODO Auto-generated method stub
		if(!isValid)
			throw new IllegalStateException();
		return m_props.keys();
	}
	
	public long getCreationTime() {
		// TODO Auto-generated method stub
		if(!isValid)
			throw new IllegalStateException();
		return creationTime;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getId()
	 */
	public String getId() {
		// TODO Auto-generated method stub
		if(!isValid)
			throw new IllegalStateException();
		return id;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime() {
		// TODO Auto-generated method stub
		if(!isValid)
			throw new IllegalStateException();
		return lastAccessedTime ;
	}

	public void setLastAccessedTime(long time) {
		// TODO Auto-generated method stub
		if(!isValid)
			throw new IllegalStateException();
		lastAccessedTime =time;
	}
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getServletContext()
	 */
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return context;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
	 */
	public void setMaxInactiveInterval(int interval) {
		// TODO Auto-generated method stub
		maxInactiveInterval=interval;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
	 */
	public int getMaxInactiveInterval() {
		// TODO Auto-generated method stub
		return maxInactiveInterval;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getSessionContext()
	 */
	//Deprecated
	public HttpSessionContext getSessionContext() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
	 */
	//Deprecated
	public Object getValue(String arg0) {
		// TODO Auto-generated method stub
		if(!isValid)
			throw new IllegalStateException();
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValueNames()
	 */
	//Deprecated, may be also should return String[] of keys in order to pass test???
	public String[] getValueNames() {
		// TODO Auto-generated method stub
		if(!isValid)
			throw new IllegalStateException();
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object value) {
		if(!isValid)
			throw new IllegalStateException();
		if(value==null)
			removeAttribute(name);
		else{
		m_props.put(name, value);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		if(!isValid)
			throw new IllegalStateException();
		if(m_props.containsKey(name))
			m_props.remove(name);
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#putValue(java.lang.String, java.lang.Object)
	 */
	//Deprecated
	public void putValue(String arg0, Object arg1) {
		if(!isValid)
			throw new IllegalStateException();
		m_props.put(arg0, arg1);
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
	 */
	//Deprecated
	public void removeValue(String arg0) {
		if(!isValid)
			throw new IllegalStateException();
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#invalidate()
	 */
	public void invalidate() {
		if(!isValid)
			throw new IllegalStateException();
		isValid = false;
		Manager.removeSession(this);
		System.out.println("remove session!!!");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#isNew()
	 */
	public boolean isNew() {
		// TODO Auto-generated method stub
		if(!isValid)
			throw new IllegalStateException();
		return isNew;
	}

	public boolean isValid()
	{
		return isValid;
	}
	
}
