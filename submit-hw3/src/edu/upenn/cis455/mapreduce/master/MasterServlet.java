package edu.upenn.cis455.mapreduce.master;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

import javax.servlet.*;
import javax.servlet.http.*;

public class MasterServlet extends HttpServlet {

  static final long serialVersionUID = 455555001;
  //mapping, waiting, reducing, idle
 //temporary map to store status;
  private HashMap<String, String> status=new HashMap<String, String>();
  //use hashmap to store the information of all the workers;
  private HashMap<String, HashMap<String,String>> workers=new HashMap<String, HashMap<String, String>>();
  private HashMap<String, Long> lastReceived=new HashMap<String, Long>();//only contain the port and time
  private HashMap<String, String> mapreduceParam=new HashMap<String, String>();
 
  public void init()
  {
	  System.out.println("master created!");
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) 
       throws java.io.IOException
  {
	String requestURI=request.getRequestURI();
	if(requestURI.equals("/workerstatus"))
		statusUpdate(request, response);
	if(requestURI.equals("/status"))
		displayStatus(request, response);

  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException
	{
	  //send the request to worker;
	  String requestURI=request.getRequestURI();
	  if(requestURI.equals("/runmapreduce"))
			runMapReduce(request, response);
	}
  
 
//update the worker status; do not need to display anything.
private void statusUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
	// TODO Auto-generated method stub
	System.out.println("Update  Worker Status");
	
	PrintWriter out = response.getWriter();
	response.setContentType("text/html");

	String ip = (String)request.getRemoteAddr();//(String)request.getAttribute("IPAddress");
	String port=request.getParameter("port");
	String worker=ip+":"+port;
	System.out.println(worker);
	
	//must rebuild another one since if the status is the same one, all the status in workers will be the same!
	status=new HashMap<String, String>();
	status.put("status", request.getParameter("status"));
	status.put("job", request.getParameter("job"));
	status.put("keys_read", request.getParameter("keys_read"));
	status.put("keys_written", request.getParameter("keys_written"));
	
	//will update if the port number is same
	workers.put(worker, status);
	Long time = System.currentTimeMillis();
	lastReceived.put(worker, time);
	
	//deal with the trigering reduce???
	
	out.close();
	
}

//display all the information about the workers and 
private void displayStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
	// TODO Auto-generated method stub
	PrintWriter out= response.getWriter();
	response.setContentType("text/html");
	
	out.println("<html><head><title>Status Page</title></head><body>");
	out.println("<table><tr>" +
						"<td>WorkerPort</td>" +
						"<td>Status</td>" +
						"<td>Job</td>" +
						"<td>KeysRead</td>" +
						"<td>KeysWritten</td><tr>"   );

	for(String worker: workers.keySet()) {
		System.out.println("workers: "+worker);
			if(System.currentTimeMillis()-lastReceived.get(worker)<300000) {
				out.print("<tr>");
				out.println("<td>"+worker+"</td>");
				out.println("<td>"+workers.get(worker).get("status")+"</td>");
				out.println("<td>"+workers.get(worker).get("job")+"</td>");
				out.println("<td>"+workers.get(worker).get("keys_read")+"</td>");
				out.println("<td>"+workers.get(worker).get("keys_written")+"</td>");
				out.print("</tr>");
			}
			else {
				//inactive, delete from the map;
				System.out.println("Time Out!!!");
				workers.remove(worker);
				lastReceived.remove(worker);		
			}
	}
	 
	out.println("</table>");
	out.println("</br>");
	out.println("<h3>Form for Submitting Jobs</h3>");
	out.println("<form name =\"statusform\" action = \"/runmapreduce\" method = \"POST\"><br/>");
	out.println("ClassName of Job: <input type = \"text\" value = \"edu.upenn.cis455.mapreduce.job.WordCount\" name = \"job\"/><br/>");
	out.println("Input Directory: <input type = \"text\"  value = \"input\" name = \"input\"/><br/>");
	out.println("Output Directory: <input type = \"text\"  value = \"output\" name = \"output\"/><br/>");
	out.println("Number of Map Threads: <input type = \"text\" value = \"2\" name = \"numThreadsMap\"/><br/>");
	out.println("Number of Reduce Threads: <input type = \"text\" value = \"2\" name = \"numThreadsReduce\"/><br/>");
	out.println("<input type = \"submit\" value = \"Submit\"/></form>");
	out.println("</body></html>");
	
	out.flush();
	out.close();
	
}

private void runMapReduce(HttpServletRequest request, HttpServletResponse response) throws IOException {
	// TODO Auto-generated method stub
	PrintWriter out=response.getWriter();
	response.setContentType("text/html");
	
	out.println("<html><body>Doing the Map Reduce Work!");
	out.println("<a href=\"/status\">Back to Status</a>>");
	out.println("</body></html>");
	
	//store the param for latter use; ...can be deleted... 
	String input=request.getParameter("input");
	String output=request.getParameter("output");
	if(!input.startsWith("/"))input="/"+input;
	if(!output.startsWith("/"))output="/"+output;
	
	//parameter from request
	mapreduceParam.put("job",request.getParameter("job"));
	mapreduceParam.put("input", input);
	mapreduceParam.put("output",output);
	mapreduceParam.put("numThreadsMap", request.getParameter("numThreadsMap"));
	mapreduceParam.put("numThreadsReduce",request.getParameter("numThreadsReduce"));
	
	mapreduceParam.put("numWorkers", Integer.toString(workers.size()));
	
	String queryString="job="+mapreduceParam.get("job")+"&"
					  +"input="+mapreduceParam.get("input")+"&"
					  +"numThreads="+mapreduceParam.get("numThreadsMap")+"&"
					  +"numWorkers="+mapreduceParam.get("numWorkers")+"&";
	
	//detailed workers
	int i = 0;
	for(String worker: lastReceived.keySet()) {
		queryString += "worker"+i+"="+worker+"&";
		i++;
	}
	queryString=queryString.substring(0,queryString.length()-1);
	System.out.println("runmap queryString: "+queryString);
	
	sendRequest("/runmap",queryString);

	//not all workers are waiting;
	//wait for all the map work done;
	//should also checked in /workerstatus  handling inorder to make it 
	while(!checkWaiting())
	{
	}

	queryString="job="+mapreduceParam.get("job")+"&"
				+"output="+mapreduceParam.get("output")+"&"
			    +"numThreads="+mapreduceParam.get("numThreadsReduce");
	
	sendRequest("/runreduce",queryString);
	System.out.println("runreduce queryString: "+queryString);
	
}
	
//check whether all the workers are waiting. 
private boolean checkWaiting() {
	// TODO Auto-generated method stub
	for (String port : workers.keySet()) {
		HashMap<String,String> map =  workers.get(port);
		if (!map.get("status").equals("waiting")) {
			return false;
		}
	}
	return true;
}

//send request to each workers
private void sendRequest(String action, String queryString) {
	// TODO Auto-generated method stub
	for(String worker:workers.keySet())
	{
		String requestURI="http://"+worker+action;
		System.out.println("requestURI: "+requestURI);
		
		SendRequestThreads thread = new SendRequestThreads(requestURI, queryString);
		thread.start();

	}	
}

public class SendRequestThreads extends Thread {
	String url, queryString;
	public SendRequestThreads(String url, String queryString) {
		this.url = url;
		this.queryString = queryString;
	}
	@Override
	public void run() {
		URL master;
		try {
			master = new URL(url);
			//	System.out.println(url);
			HttpURLConnection conn = (HttpURLConnection) master.openConnection();

			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			conn.setRequestProperty("Content-Length", "" + Integer.toString(queryString.getBytes().length));

			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			//	System.out.println(urlParameters.substring(0,urlParameters.length()-1));
			out.writeBytes(queryString);
			out.flush();
			out.close();
			
			//debug
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			
			conn.disconnect();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
}

}
  
