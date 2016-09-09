package edu.upenn.cis455.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.*;

import org.w3c.dom.Document;
import org.w3c.tidy.*;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	  /* TODO: Implement user interface for XPath engine here */

	private Socket s=null;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		response.setContentType("text/html");
		PrintWriter out= response.getWriter();
		/*
		<html><head><title>Input the XPath and HTML/XML document URL</title></head>
		<body>
		<form method="post">
        XPath: <input type="text" name="xpath"><br>
  		HTML/XML Document URL: <input type="text" name="docurl"><br>
        <input type="submit" value="Submit">
        </form>
        </body></html>
		 */
		out.println("<html><head><title>Input the XPath and HTML/XML document URL</title></head>");
		out.println("<body><form method=\"post\">");
		out.println("XPath: <input type=\"text\" name=\"xpath\"><br>");
		out.println("HTML/XML Document URL: <input type=\"text\" name=\"fullurl\"><br>");
		out.println("<input type=\"submit\" value=\"Submit\">");
		out.println("</form> </body></html>");
		out.flush();
		out.close();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		//System.out.println("Post!");
		response.setContentType("text/html");
		PrintWriter out= response.getWriter();
		XPathEngineImpl xpath_engine= new XPathEngineImpl();
		String result = "";

		//Handle the request
		String xpath = request.getParameter("xpath");
		String fullurl = request.getParameter("fullurl");//break it into host and path
		
		String host="";
		int port=0;
		String path="";

		fullurl.trim();
		if(!fullurl.startsWith("http://"))
		{
			fullurl="http://"+fullurl;
		}
		
		//example:http://www.google.com/blabla/dsads.xml
		//http://localhost:8080/servlet/xpath.xml
		String temp = fullurl;
		String split1[] = fullurl.split("//"); //   http:  localhost:8080/servlet/xpath.xml
		String split2[] = split1[1].split("/",2);//   localhost:8080   servlet/xpath.xml
		String split3[] = split2[0].split(":");//   localhost   8080
		host=split3[0];
		if(split3.length>1)
			port=Integer.parseInt(split3[1]);
		else port=80;//?????????what if we use google? why do we have to set a port?
		
		//String without_path=split1[0]+"//"+split2[0]+"/"; //http://localhost:8080/
		//path=fullurl.substring(without_path.length());
		path=split2[1];
		path="/"+path;
		
		System.out.println("host: "+host);
		System.out.println("port: "+port);
		System.out.println("path: "+path);
		//open the connection
//		Socket s=new Socket();//act like a client to request document from origin server.
//		try{
//		s.connect(new InetSocketAddress(host,port));
//		}catch(UnknownHostException e)
//		{
//			System.out.println("Connection Failed!");
//		}
		
		//or
		try{
		s=new Socket(host,port);
		System.out.println("Connecting...");
		}catch(UnknownHostException e)
		{
			System.out.println("Connection Failed!");
		}
		
		//create and send the get request
		if(s!=null)
		{
			PrintWriter newrequest=new PrintWriter(s.getOutputStream());
			String head="GET "+path+" HTTP/1.1\r\n"
					+"Host: "+host+"\r\n"
					+"Connection: close\r\n\r\n";
			newrequest.write(head);
			newrequest.flush();
			
			
			//get the stream from origin servlet and make it to DOM.
			InputStream newresponse = s.getInputStream();

			//if(newresponse!=null)System.out.println("got the inputstream");//got
			//what does the servlet return if the file can not be find, the response contains the response head and body, we only want to use the body
			//solution: use buffer readline to discard the head! 
			BufferedReader res_buf=new BufferedReader(new InputStreamReader(newresponse));
			
			String body=null;// can be used to build teh body?  // or use StringBuffer
			
			//String t="";
			//while(( t = res_buf.readLine()) != null) System.out.println(t);//done!
			
			if(res_buf.readLine().contains("200"))
			{				
				System.out.println("response success!");
				String helper=res_buf.readLine();
				while(helper!=null&&!helper.equals(""))
				{
					//System.out.println(helper);
					helper=res_buf.readLine();
				}
				String current=res_buf.readLine();
				while(current!=null)
				{
					body+=current;
					current=res_buf.readLine();
				}
			}
			
			System.out.println("body: "+body);
			
			//newrequest.close();
			//newresponse.close();
			Tidy tidy=new Tidy();
			if(path.endsWith(".xml"))
			{
				tidy.setXmlTags(true);
			}
			Document doc=null;
			if(body!=null)
			{
				InputStream body_stream=new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
				doc=tidy.parseDOM(body_stream, null);
			}
			
			//do the evaluation
			String xpaths[]={xpath};
			xpath_engine.setXPaths(xpaths);
			
//			boolean isvalid=xpath_engine.isValid(0);
//			if(isvalid)result="the xpath is valid!";
//			else result="the xpath is not valid!";
//			
			boolean ismatch[]=xpath_engine.evaluate(doc);
			System.out.println(" ");
			
			for(int i=0;i<xpaths.length;i++)
			{
				if(xpath_engine.isValid(i))
				{
					result=xpaths[i]+"  is valid!\n";
				if(ismatch[i])
					result=result+xpaths[i]+" was found!";
				else result=result+xpaths[i]+" can not be found!";
				}
				else
					result=xpaths[i]+"   is not valid!!! please check it again\n";
			}			
		}
	
		out.println("<html><head><title>Result</title></head><body>");
		out.write(result);
		out.println("</body><html>");
		out.flush();
		out.close();
		
	}

}









