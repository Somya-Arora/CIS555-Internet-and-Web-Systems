package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

// this class take a request queue as parameter, get socket from the queue and give the response.
public class WorkerThread extends Thread{
	
	private String root_directory;
	private RequestQueue request_queue;
	private Socket socket;
	
	private HashMap<String,String> request_head=new HashMap<String,String>();//used to store all the key values in the request header
	private StringBuffer request_body=new StringBuffer();

	private String status;
	public boolean running;
	
	private BufferedReader in;
	private OutputStream out;
	
	//different url 
	private String fullrequrl;//full url after the root directory
	private String req_url="";//request url string, before the "?"
	private String queryString="";//request query string, after "?"
	private String postString="";//request post string / request body
	private String servletPath;
	private String pathInfo;
	
	public WorkerThread(RequestQueue queue) {
		// TODO Auto-generated constructor stub
		root_directory=Manager.root_directory;
		request_queue=queue;
		running=true;
		status="wait";
	}
	
	public void run(){
		while(running)
		{
			try {
				socket = request_queue.remove();//the worker will wait if there is no socket in the queue.
				socket.setSoTimeout(40000);//System.out.println("remove -> socket queue size :"+ request_queue.queue.size());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("Time out!!!!");
				Manager.shutdown=true;//stop listening 
				try {
					Manager.shutdown();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}	
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = socket.getOutputStream();
				
				SocketReader(in);
				ResponseBuilder(out);
				
				in.close();
	  			out.close();
	  			socket.close();
	  			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	  public void SocketReader(BufferedReader in) throws IOException
	  {
		  //*******read the first line*******//
		  String initial[]=in.readLine().split(" ");
		  request_head.put("method", initial[0]);
		  request_head.put("fullrequrl", initial[1]);
		  request_head.put("version", initial[2]);
		  //System.out.println(request_head.get("method"));
		  
		  //*******read the header*******//
		  String thisline=in.readLine();
		  while(thisline!=null&&!thisline.equals(""))
		  {
			  String headerssub[]=thisline.split(":");
			  request_head.put(headerssub[0].toLowerCase(), headerssub[1].trim());
			  thisline=in.readLine();
		  }	  
		  
		  //*******MS2: read the request body*******//
		  if(request_head.get("method").equals("POST")&&request_head.containsKey("content-type"))
		  {
			  //System.out.println("POST detected");
			  //why can not use this!!!???
//			  thisline=in.readLine();
//			  while(thisline!=null&&!thisline.equals(""))
//			  {
//				  System.out.println(thisline);
//				  request_body.append(thisline+"\r\n");
//				  thisline=in.readLine();
//			  }		  
				int length = Integer.parseInt( request_head.get("content-length"));
				int readChar = 0;
				while (true) {
					request_body.append((char)in.read());
					readChar ++;
					if(readChar >= length)break;
				}
		  }
		  
	  }
	  
	  public void ResponseBuilder(OutputStream out) throws IOException
	  {
		  StringBuffer response_body=new StringBuffer();
		  StringBuffer response_head=new StringBuffer();
		  File file=null;
		  boolean initial_flag=false;
		  boolean ispic_flag=false;
		  boolean canhandle=false;
		  //boolean isplain_flag=false;
		  
		  String method=request_head.get("method");
		  
		  //body builder
		  if(method.equals("GET")||method.equals("HEAD")||method.equals("POST"))
		  {
		  fullrequrl=request_head.get("fullrequrl");
		  String version=request_head.get("version");
		  
		  if(fullrequrl.equalsIgnoreCase("/control"))
		  {
			  canhandle=true;
			  response_body.append("<html><body>");
			  response_body.append("Control Panel <br>");
			  response_body.append("Menglu Wang----mengluw <br>");
			  response_body.append("Thread                   status <br>");
			  for(WorkerThread wt: ThreadPool.worker_list)
			  {
				  response_body.append(wt.getName()+"          "+status +"<br>");
			  }		  
			  response_body.append("<a href = \"shutdown\"> Shutdown </a><br>");
			  response_body.append("<a href = \"error\"> error log </a><br>");
			  response_body.append("</body></html>");
		  }
		  
		  else if(fullrequrl.equalsIgnoreCase("/shutdown"))
		  {
			  canhandle=true;
			  Manager.shutdown=true;//stop listening 
			  Manager.shutdown();//stop all the threads
		  }

		  else //normal url handle the (1) servlet (2) directory (3) file (4) post request
		  {   
			  //
			  if(version.equals("HTTP/1.1"))
			  {
				  ResponseInitialBuilder(100,response_head);
				  if(!request_head.containsKey("host"))
					  ResponseInitialBuilder(400,response_head);
			  }
			  
			  
			  //*************deal with the url*************//
		  //if(fullrequrl.equals("/"))fullrequrl="";//if it is empty, will show "/", delete it will be more clear
		//
		  if(fullrequrl.endsWith("/"))
			  fullrequrl=fullrequrl.substring(0,fullrequrl.length()-1);

			  req_url = fullrequrl;
			  if(req_url.contains("?")){
					  String str[]=fullrequrl.split("\\?");
					  if(str.length==2)
					  {
						  //req_url=str[0].toLowerCase();
						  req_url=str[0];
						  queryString=str[1];
					  }
					  				  
			  }
			  else req_url=req_url;//.toLowerCase();
			  	  
			  //****postbody**** 
			  if(method.equals("POST"))
			  {  			  
				  //deal with the reqest body
				  postString=request_body.toString();//.replace("\\s", "");// delete all the space,tab...can not just use trim!!
				  System.out.println("postString: "+postString);
				  if(!postString.contains("=")){
					  postString="";//do not contain the = then, it is not the key value set. we do not deal with it!
					  }
			  }  
			 
//				  System.out.println(req_url);
//				  System.out.println(queryString);
//				  System.out.println(postString);
//			
		   //*******servlet*******//
			 HttpServlet m_servlet=findServlet(req_url);
			 if(m_servlet!=null)
			 {	 
				canhandle=true;
				invokeServlet(m_servlet);
				//return;
			 }

		  //*******handle the directory and file*******//
		  String fullurl=root_directory+fullrequrl;
		  file=new File(fullurl);
		  
		  //handle the diretory
		  if(file.isDirectory())
		  {   
			  canhandle=true;
			  System.out.println("isDirectory");
			  response_body.append("<html><br><body><br>");
			  for(int i=0;i<file.list().length;i++)
			  {
				  String subfilepath=fullrequrl+"/"+file.list()[i];
				  response_body.append("<a href = \""+subfilepath+"\">"+file.list()[i]+"</a><br>");  
			  }
			  response_body.append("</body><br></html>");
		  }
		  
		  //handle the file
		  if(file.isFile())
		  {
			  canhandle=true;
			  System.out.println("isfile");
			  
			  if(!fullurl.contains(root_directory))//?meaningless???
			  {
				  ResponseInitialBuilder(403,response_head);
				  response_body.append("<html><body>");
				  response_body.append(response_head);
				  response_body.append("</body></html>");
				  initial_flag=true;
			  }
			  else if(!file.exists()||!file.canRead())//can not read
			  {
				  ResponseInitialBuilder(404,response_head);
				  response_body.append("<html><body>");
				  response_body.append(response_head);
				  response_body.append("</body></html>");
				  initial_flag=true;
			  } 
			  else if(fullurl.endsWith(".txt")||fullurl.endsWith(".html"))//html, txt. write to the body directly, can also be handled latter, together with picture 
				  {
				   BufferedReader textReader = new BufferedReader(new FileReader(file));
					while(textReader.ready()){
						response_body.append((char)textReader.read());
					}
					textReader.close();	 
				  }
			  else if(fullurl.endsWith(".jpg")||fullurl.endsWith(".png")||fullurl.endsWith(".gif"))//picture, will be handled latter
				  {
				    ispic_flag=true;
				  }
			  else // unsupported type of file
			  {
				  ResponseInitialBuilder(501,response_head);
				  response_body.append("<html><body>");
				  response_body.append(response_head);
				  response_body.append("</body></html>");
				  initial_flag=true;
			  }
		  }//is file
		  	  
		  //System.out.println(response_body.toString());
	  
		  }// normal url
		  
		  
		  //**********head builder************//
		  if(!canhandle)
		  {
			  ResponseInitialBuilder(404,response_head);
			  initial_flag=true;
		  }
		  
		  if(!initial_flag)//normal head
		  {
			  ResponseInitialBuilder(200,response_head);
			  initial_flag=true;
		  } 
		   // type 
		   response_head.append("Content-Type: "+getType(fullrequrl)+"\r\n");
		   //length
		   long length;
		   if(fullrequrl.equals("/control") || (file!=null && file.isDirectory()))
	            length = response_body.length();// we have to know the body length so we have to build up body first
		   else length = file.length();
		   response_head.append("Content-Length: " + length+"\r\n");
		   response_head.append("Host: Menglu Wang\r\n");
		   response_head.append("Connection: close\r\n");
		   
		  //**********out.write************//
		   out.write(response_head.toString().getBytes()); //head
		   out.write("\n".getBytes());
		   if(ispic_flag)// if the file is a picture, write it in a special way.
		   {
			   FileInputStream inStream = new FileInputStream(file);
				byte[] buf = new byte[1024];
				int len=0;
				while((len = inStream.read(buf)) != -1){
					out.write(buf, 0 , len);
				}
				out.write("\r\n".getBytes());
				inStream.close();
				out.flush();
				out.close();
		   }
		   else //else, write out the response body
		   {
				if(method.equals("GET")||method.equals("POST"))
				{
					out.write(response_body.toString().getBytes()); //if the file is not a picture, write it in a normal way.
				}
				else{//HEAD
					out.write("".getBytes());
				}
		   }
		}
		   
	}

	public void ResponseInitialBuilder(int code,StringBuffer head)
	  {
		  String initial = request_head.get("version");
	      switch(code){
	      case 200: initial += " 200 OK\r\n";break;
	      case 403: initial += " 403 FORBIDDEN\r\n";break;     
	      case 404: initial += " 404 NOT FOUND\r\n";break;
	      case 501: initial += " 501 Not Implemented or Unsupported File\r\n";break;
	      case 100: initial += " 100 CONTINUE\r\n\r\n";break;
	      case 400: initial += " 400 BAD REQUEST\r\n";break;
	      }
	      head.append(initial);
	  }
	  
	  public static String getType(String fullrequrl)
	  {
		  if(fullrequrl.endsWith("jpeg")||fullrequrl.endsWith("jpg"))
			  return "image/jpeg";
		  else if(fullrequrl.endsWith("gif"))
			  return "image/gif";
		  else if(fullrequrl.endsWith("png"))
			  return "image/png";
		  else if(fullrequrl.endsWith("txt"))
			  return "text/plain";
		  else return "text/html";
	  }
	
	  private HttpServlet findServlet(String url)
	  {
		  servletPath=url;
		  pathInfo = null;
		  
		  HashMap<String,String> map=Manager.m_handler.m_servletMappings;
		  String  servletName="";
		  if(map.get(url)!=null)//full match
		  {
			  servletName=map.get(url);
			  servletPath=url;
			  pathInfo = null;
		  }
		  else
		  {	  System.out.println("have no full match!!");
			  for(String key_url:map.keySet())
			  {
				  System.out.println("map pattern: " + key_url);
				  if(key_url.contains("*"))
				  {
					  String prefix = key_url.split("\\*")[0];//split with *, keep the first part, must use \\* since * is a dangling character
					  prefix = prefix.endsWith("/")?prefix.substring(0, prefix.length() - 1):prefix;//if the firstpart contain /, delete it.
					  System.out.println("prefix of map: " + prefix);
					  if(url.startsWith(prefix)||prefix.equals("")){
							 servletName = map.get(key_url);
							 servletPath=prefix;
							 pathInfo = url.substring(servletPath.length(), url.length());//keep the following information as pathinfor. start with "/"
							 //if(pathInfo.startsWith("/"))pathInfo=pathInfo.substring(1);
					  		 break;
						}
					  //deal with the /* 
				  }			  
			  }
		  }
		  HttpServlet m_servlet = Manager.servletContainer.get(servletName);
		  return m_servlet;
	  }
	  
	  
	  
	  private void invokeServlet(HttpServlet m_servlet) throws IOException {
		// TODO Auto-generated method stub
		    //FakeSession session = null;

			//build servlet request;
			FakeContext context = (FakeContext)m_servlet.getServletContext();
			FakeRequest request = new FakeRequest(socket,request_head, context);
			request.setServletPath(servletPath);
			request.setPathInfo(pathInfo);
			request.setQueryString(queryString);

			//deal with the query
			//should also be set in the fake request, using set query???
			if( queryString!= null && !queryString.isEmpty()){
				
				String[] queries = queryString.split("\\&");
				for(String query : queries){
					String[] sub=query.split("=");
					if(sub.length==2)
					{
					String key = sub[0];
					String value = sub[1];
					request.setParameter(key, value);
					System.out.println("set queruString Parameter: "+key+"   "+value);
					}
				}
			}
			//deal with the post
			if(postString != null && !postString.isEmpty()){
				System.out.println("Deal with the post string");
				String[] queries = postString.split("\\&");
				for(String query : queries){
					String[] sub=query.split("=");
					if(sub.length==2)
					{
					String key = sub[0];
					String value = sub[1];
					request.setParameter(key, value);
					System.out.println("set postString Parameter: "+key+"   "+value);
					}
				}
			}

			//************build response*********************//
			FakeResponse response  = new FakeResponse(request, out);
			String serverName = m_servlet.getServletContext().getServerInfo();
			response.setHeader("Server", serverName);

			try {
				m_servlet.service(request, response);
				response.flushBuffer();
			} catch (ServletException e) {
				e.printStackTrace();
				String str=request_head.get("version")+" 500 Servlet Error!\r\n";
				out.write(str.getBytes());
			}

			in.close();
			out.flush();
			out.close();
			return;
	}
}
