package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * @author Todd J. Green
 */
class FakeRequest implements HttpServletRequest {

	
	private Properties m_params = new Properties();
	private Properties m_props = new Properties();
	private FakeSession m_session = null;
	private String m_method;
	
	//HWMS2
	private HashMap<String,String> m_request_head=new HashMap<String,String>();
	private Vector<Cookie> m_cookies = new Vector<Cookie>();
	
	private StringBuffer m_request_body=new StringBuffer();
	private Socket m_socket;
	private FakeContext m_context;
	private String m_servletPath;
	private String m_pathInfo;
	private String queryString;
	private String version;
	private InputStream inputStream;
	private Locale locale;
	private String encoding;

	
	public FakeRequest() {
	}
	
	public FakeRequest(FakeSession session) {
		m_session = session;
	}
	
	public FakeRequest(Socket socket, HashMap<String,String> request_head, 
			FakeContext context) {
		m_socket = socket;
		m_session =  null;
		m_request_head = request_head;
		m_context = context;
		m_method = request_head.get("method");
		version=request_head.get("version");
		
		//get cookie and session from request to build the fake request.
		for(String key: m_request_head.keySet()){
			if(key.equals("cookie")){
				String cookies[] = m_request_head.get("cookie").trim().split(";");
					for(String cookie : cookies){
						String c_key = cookie.split("=")[0].trim();
						String c_value = cookie.split("=")[1].trim();
						//System.out.println("fr78: "+c_key);
						//System.out.println("fr78: "+c_value);
						if(c_key.equalsIgnoreCase("jsessionid"))
							m_session=Manager.getSession(c_value);//may be null.
						else
						{
						Cookie ck = new Cookie(c_key, c_value);//has not specify the max age!
						m_cookies.add(ck);
						}
						//all the cookies will be added in the request automatically by browser!
						//System.out.println("fake request: cookie info: " + c_key + c_value);
					}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		// TODO Auto-generated method stub
		return "BASIC";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return m_cookies.toArray(new Cookie[m_cookies.size()]);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String key) {
		// TODO Auto-generated method stub
		key=key.toLowerCase();
		if(!m_request_head.containsKey(key))
			return -1;
		
		SimpleDateFormat format=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		String dateString=m_request_head.get(key);
		try {
			Date date=format.parse(dateString);
			return date.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			throw new IllegalArgumentException();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String arg0) {
		// TODO Auto-generated method stub
			return m_request_head.get(arg0.toLowerCase());//maybe null
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	//test it!!!!!!!!!!!!!!!!!
	public Enumeration getHeaders(String arg0) {
		// TODO Auto-generated method stub
		//can use vector to transfer array to enumeration
		String key = arg0.toLowerCase();
		Vector<String> headers = new Vector<String>();
		if(m_request_head.containsKey(key)){
			String[] values = m_request_head.get(key).trim().split(",");
			for(String value: values)
				headers.addElement(value);
		}
		return headers.elements();//will it return null is there is no values in the vector?
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	public Enumeration getHeaderNames() {
		// TODO Auto-generated method stub
		return Collections.enumeration(m_request_head.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		return m_method;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		// TODO Auto-generated method stub
		if(m_pathInfo == "")
			return null;
		return m_pathInfo;
	}

	public void setPathInfo(String path){
		m_pathInfo = path;//start with a "/"

	}
	// set the ServletPath inorder to get the full path.
	public void setServletPath(String path){	
		m_servletPath = path;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	//NR
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		// TODO Auto-generated method stub
		return "";//????is tihs right??
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	public String getQueryString() {
		// TODO Auto-generated method stub
		return queryString;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	//NR
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	//NR
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		//System.out.println("at FakeRequest 245");
		return m_session.getId();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {
		// TODO Auto-generated method stub
		return m_servletPath + m_pathInfo;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		StringBuffer buffer = new StringBuffer();
		buffer.append("Http://");//buffer.append(getScheme().append("://"))
		buffer.append(getServerName()).append(":").append(getServerPort());
		buffer.append(getContextPath()).append(getServletPath()).append(getPathInfo());	
		return buffer;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		// TODO Auto-generated method stub
		return m_servletPath;//....servletpath....
	}
	
	
	boolean hasSession() {
		if(m_session==null)System.out.println("session is null");
		else if(!m_session.isValid())System.out.println("session is not valid");
		return ((m_session != null) && m_session.isValid());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean create) {// modified!
			//build a new session
			if (create) 
			{
				if (! hasSession()) 
				{
				System.out.println("create session!!!");
				m_session = new FakeSession(m_context);
				Manager.addSession(m_session);
				m_request_head.put("cookie", "jsessionid=" + m_session.getId());
				return m_session;
				}
			}
			else 
			{
				if (! hasSession()) //not valid or not exist
				{
					Manager.removeSession(m_session);
					m_session = null;
					return null;
				}
				else
				{
				//check isvalid
					
					long current=System.currentTimeMillis();
					long last=m_session.getLastAccessedTime();
					System.out.println("last accessedTime: "+last);
					if((current-last) < m_session.getMaxInactiveInterval()*1000)
					{
					m_session.setLastAccessedTime(current);
					}
					else //too old
					{
					System.out.println("session is too old!!!");
					m_session.invalidate();//remove from manager
					return null;
					}
				}
			}
		
		return m_session;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return getSession(true);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	// remained to do
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		if(Manager.sessionList.contains(getRequestedSessionId()))
			return m_session.isValid();
		else return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		//??????not sure
		if(m_request_head.containsKey("session"))
			return true;
		else return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String key) {
		// TODO Auto-generated method stub
		return m_props.get(key);
	}
	
	public Object getAttributes() {
		// TODO Auto-generated method stub
		return m_props;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		// TODO Auto-generated method stub
		return m_props.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		if(m_request_head.containsKey("Character-Encoding"))
			return m_request_head.get("Character-Encoding");
		return encoding==null?"ISO-8859-1":encoding;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		encoding = arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	public int getContentLength() {
		// TODO Auto-generated method stub
		if(m_request_head.containsKey("content-length")){// notice that I make every key word to lower case!
			return Integer.parseInt(m_request_head.get("content-length"));
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {
		// TODO Auto-generated method stub
		if(m_request_head.containsKey("content-type")){
			return m_request_head.get("content-type");
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	//NR
	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String name) {
		return m_params.getProperty(name);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	public Enumeration getParameterNames() {
		return m_params.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	//some parameters may have a lot of value
	public String[] getParameterValues(String name) {
		// TODO Auto-generated method stub
		String[] values = null;
		if(m_params.containsKey(name)){
			String valueStr = m_params.getProperty(name);
			values = valueStr.split(",");
		}	
		return values;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map getParameterMap() {
		// TODO Auto-generated method stub
		if(m_params == null || m_params.isEmpty())
			return null;
		return m_params;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		// TODO Auto-generated method stub
		return version;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {
		// TODO Auto-generated method stub
		return "http";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {
		// TODO Auto-generated method stub
		if(!m_request_head.containsKey("host"))
			return null;
		String host=m_request_head.get("host");
		return host.substring(0, host.indexOf(":"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {
		// TODO Auto-generated method stub
		return m_socket.getPort();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return new BufferedReader(new InputStreamReader(inputStream));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		if(m_socket.isConnected())
			return m_socket.getRemoteSocketAddress().toString();
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {//todo
		// TODO Auto-generated method stub
		if(m_socket.isConnected())
			return m_request_head.get("user-agent");
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object value) {
		m_props.put(name, value);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		// TODO Auto-generated method stub
		m_props.remove(name);
	}
	
	public void removeAttributes() {
		// TODO Auto-generated method stub
		m_props.clear();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return locale;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	//NR
	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	//NR
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	public int getRemotePort() {
		//same as the get port
		// TODO Auto-generated method stub
		if(m_socket != null)
			return m_socket.getPort();
		return -1;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		// TODO Auto-generated method stub
		if(m_socket != null)
			return m_socket.getInetAddress().getHostName();
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return m_socket.getLocalAddress().toString();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return getServerPort();
	}

	void setMethod(String method) {
		m_method = method;
	}
	
	void setParameter(String key, String value) {
		m_params.setProperty(key, value);
	}
	
	void clearParameters() {
		m_params.clear();
	}

	@Override
	public int getIntHeader(String arg0) {
		// TODO Auto-generated method stub
		String key=arg0.toLowerCase();
		if(!m_request_head.containsKey(key))
			return -1;
		try{
			;
			return Integer.parseInt(m_request_head.get(key));
		}catch(NumberFormatException e){
			throw new NumberFormatException();
		}
	}

	public void setQueryString(String queryString) {
		// TODO Auto-generated method stub
		this.queryString=queryString;
	}	
}
