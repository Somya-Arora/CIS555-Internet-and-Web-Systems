 package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

//import edu.upenn.cis455.webserver.TestHarness.Handler;

public class Manager {
	static ServerSocket ss;
	static String root_directory;
	static String xml_url;
	static ThreadPool thread_pool;
	static RequestQueue request_queue;
	static boolean shutdown = false;
	
	//MS2
	static HashMap<String, HttpServlet> servletContainer;//fixed into this kind of naming !!!! a good habit
	static MyHandler m_handler;
	static ArrayList<FakeSession> sessionList=new ArrayList<FakeSession>();
	
	
	public Manager(int port, String root_directory, String xml_url) throws IOException
	{
	
		ss=new ServerSocket(port);
		Manager.root_directory=root_directory;
		Manager.xml_url=xml_url;
		request_queue = new RequestQueue(root_directory);
		thread_pool=new ThreadPool(10,request_queue);
	}
	
	public void ManagerListening() throws IOException
	{
		while(!shutdown) {
  			Socket socket = ss.accept();
  			boolean flag=request_queue.add(socket);
  			if(!flag)System.out.println("overload");
		}	
	}
	
	public static void shutdown() throws IOException
	{
		thread_pool.stop();
		ss.close();	
	}
	
	//MS2
	//parse web.xml
	// parser use handler to read the xml 
	public void initServlet() throws Exception
	{	
		System.out.println("start init servlet");
		
		m_handler=parseWebdotxml(xml_url);
		//System.out.println(m_handler.m_servletMappings.get("/demo"));
		FakeContext m_context=createContext(m_handler);
		servletContainer=createServlets(m_handler,m_context);	
	}
	
    //Myhandler: handle the xml file
	 static class MyHandler extends DefaultHandler {
		
		private int m_state = 0;
		private String m_servletName;//used to store different servlet name temporaryily.
		private String m_paramName;
		HashMap<String,String> m_servlets = new HashMap<String,String>();
		HashMap<String,String> m_contextParams = new HashMap<String,String>();
		HashMap<String,HashMap<String,String>> m_servletParams = new HashMap<String,HashMap<String,String>>();//<servletName, <paramname, value>>;
		//MS2
		private int m_sessionTimeout=0;
		HashMap<String,String> m_servletMappings=new HashMap<String,String>();	
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.compareTo("servlet-name") == 0) {
				m_state = 1;
			} else if (qName.compareTo("servlet-class") == 0) {
				m_state = 2;
			} else if (qName.compareTo("context-param") == 0) {
				m_state = 3;
			} else if (qName.compareTo("init-param") == 0) {
				m_state = 4;
			} else if (qName.compareTo("param-name") == 0) {
				m_state = (m_state == 3) ? 10 : 20;
			} else if (qName.compareTo("param-value") == 0) {
				m_state = (m_state == 10) ? 11 : 21;
			} else if (qName.compareTo("session-timeout")==0){
				m_state = 5;
			}else if (qName.compareTo("url-pattern")==0){
				m_state = 6;
			}
		}
		public void characters(char[] ch, int start, int length) {
			String value = new String(ch, start, length);
			if (m_state == 1) {
				m_servletName = value;
				m_state = 0;
			} else if (m_state == 2) {
				m_servlets.put(m_servletName, value);
				m_state = 0;
			} else if (m_state == 6) { 
				m_servletMappings.put(value,m_servletName); //should be url-servletname
				m_state = 0;
			}
				else if (m_state == 10 || m_state == 20) {
				m_paramName = value;
			} else if (m_state == 11) {
				if (m_paramName == null) {
					System.err.println("Context parameter value '" + value + "' without name");
					System.exit(-1);
				}
				m_contextParams.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			} else if (m_state == 21) {
				if (m_paramName == null) {
					System.err.println("Servlet parameter value '" + value + "' without name");
					System.exit(-1);
				}
				HashMap<String,String> p = m_servletParams.get(m_servletName);//get this servlet's paramname-value map from the m_servletParams map;
				if (p == null) {
					p = new HashMap<String,String>();
					m_servletParams.put(m_servletName, p);
				}
				p.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			}else if (m_state==5){  //MS2
				m_sessionTimeout=Integer.parseInt(value);
			}
		}
		
	}
		
	private static MyHandler parseWebdotxml(String webdotxml) throws Exception {
		MyHandler h = new MyHandler();
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		return h;
	}
	
	private static FakeContext createContext(MyHandler h) {    // for all the server, can be moved to fakecontext
		FakeContext fc = new FakeContext();
		for (String param : h.m_contextParams.keySet()) {
			fc.setInitParam(param, h.m_contextParams.get(param));
		}
		fc.setAttribute("session-timeout", h.m_sessionTimeout);
		fc.setAttribute("servletMappings", h.m_servletMappings);
		return fc;
	}
	
	private static HashMap<String,HttpServlet> createServlets(MyHandler h, FakeContext fc) throws Exception {
		HashMap<String,HttpServlet> servlets = new HashMap<String,HttpServlet>();
		for (String servletName : h.m_servlets.keySet()) {
			FakeConfig config = new FakeConfig(servletName, fc);
			String className = h.m_servlets.get(servletName);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String,String> servletParams = h.m_servletParams.get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(servletName, servlet);
			
		}
		return servlets;
	}
	
	public static void addSession(FakeSession session)
	{
		sessionList.add(session);
	}
	public static FakeSession getSession(String id)
	{
		for(FakeSession fs : sessionList){
			if(fs.isValid()&&fs.getId().equals(id))
			return fs;
		}
		return null;
	}
	public static void removeSession(FakeSession fakeSession) {
		// TODO Auto-generated method stub
		sessionList.remove(fakeSession);
	}
}