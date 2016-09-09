package edu.upenn.cis455.mapreduce.worker;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.*;
import javax.servlet.http.*;


import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.job.*;

public class WorkerServlet extends HttpServlet {

  static final long serialVersionUID = 455555002;
  
  private HashMap<String, String> status=new HashMap<String, String>();//is actually not needed... whatever...
  //better split the map and reduce parameters!!!
  private HashMap<String, String> mapParam=new HashMap<String, String>(); //including the master and directory
  private HashMap<String, String> reduceParam=new HashMap<String, String>();
  //the input and output start with a "/"

  private BlockingQueue<String> lineQ=new LinkedBlockingQueue<String>();
  private MapContext context=null;
//  private String master;
  private String storagedir;
  private String spool_in;
  private String port;
  private int keys_read;
  private int keys_written;
  boolean isSendingMessage;
  //private HashMap<String, Integer> fileReceivedCount=new HashMap<String, Integer>();
  private int fileReceivedCount=0;
 //used to store workers ip and port: (1, ip:port);
private HashMap<String, String> workers=new HashMap<String, String>();
  
  public void init()
  {
	  keys_read=0;
	  keys_written=0;
	  
	  ServletConfig config=getServletConfig();
	  mapParam.put("master",config.getInitParameter("master"));
	   port=config.getInitParameter("port");
	   storagedir=config.getInitParameter("storagedir");
	  //delete the "/"
	  if (storagedir.endsWith("/"))
		  storagedir=storagedir.substring(0,storagedir.length()-1);
	  
	  mapParam.put("storagedir",storagedir);  

	  status.put("status", "idle");
	  status.put("job", null);
	  status.put("keys_read", "0");
	  status.put("keys_written", "0");
	  
	  isSendingMessage=true;
	  
	  Thread thread = new Thread() {
			public void run() {
				while (isSendingMessage) {
					try {
						setWorkerStatus();
						Thread.sleep(10000000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();

  }
  
  public void doGet(HttpServletRequest request,HttpServletResponse response) throws IOException
  {
	  PrintWriter out= response.getWriter();
		response.setContentType("text/html");
	  out.println("<html><head><title>Initialize Worker</title></head>");
	  out.println("<body>This worker has been initialized for mapreduce</body>" );
	  out.println("<a href=\"http://"+mapParam.get("master")+"/status\">Back to Status</a>>");
  }
  
  public void doPost(HttpServletRequest request, HttpServletResponse response) 
	       throws java.io.IOException
	  {
		  String requestURI=request.getRequestURI();
		  if(requestURI.equals("/runmap"))
				runMap(request, response);
		  if(requestURI.equals("/pushdata"))
			    pushData(request, response);
		  if(requestURI.equals("/runreduce"))
				runReduce(request, response);  
	  }

//this function is actually set workstatus in master by sending a "get" request to /workerstatus to master with parameters;
  public void setWorkerStatus() {
		// TODO Auto-generated method stub
	  System.out.println("***Sending the message to master!!!");
	  
	  String fullurl="http://" + mapParam.get("master") + "/workerstatus?port=" + port
				+ "&status=" + status.get("status") + "&job=" + status.get("job") + "&keys_read="
				+ status.get("keys_read") + "&keys_written=" + status.get("keys_written");
	  
	  System.out.println("set status url: "+fullurl);
	  
	  try {
		URL url=new URL(fullurl);
		HttpURLConnection conn=(HttpURLConnection) url.openConnection();
		
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);

		//debug
		BufferedReader buffer=new BufferedReader(new InputStreamReader(conn.getInputStream()));
//		String thisline=buffer.readLine();
//		while(thisline!=null)
//		{
//			System.out.println(thisline);
//			thisline=buffer.readLine();
//		}
		
		conn.disconnect();
		
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}	
  }
	
private void runMap(HttpServletRequest request, HttpServletResponse response) throws IOException  {
	// TODO Auto-generated method stub
	
	//dealt with request parameters
	System.out.println("------------------1. Start Mapping---------------");
	
	mapParam.put("job", request.getParameter("job"));
	mapParam.put("input", request.getParameter("input"));
	mapParam.put("numThreads", request.getParameter("numThreads"));
	mapParam.put("numWorkers", request.getParameter("numWorkers"));
	
	int numWorkers=Integer.parseInt(request.getParameter("numWorkers"));
	for(int i=0;i<numWorkers;i++)
		workers.put("worker"+i, request.getParameter("worker"+i));

	//debug
	System.out.println("all the workers: ");
	for(String w:workers.keySet()) {
		System.out.println(w+": "+workers.get(w));
	}
	
	//update worker information;
	status.put("status", "mapping");
	status.put("job", mapParam.get("job"));
	status.put("keys_read", "0");
	status.put("keys_written", "0");
	setWorkerStatus();
	
	
	context=new MapContext(numWorkers, mapParam.get("storagedir"), status);

    //Start to run the map;
	//build up all the threads and run all the threads
	ArrayList<MapReduceThread> mapreduceThreads = new ArrayList<MapReduceThread>();
	for(int i = 0; i<Integer.parseInt(mapParam.get("numThreads")); i++) {
		MapReduceThread mrt=new MapReduceThread();
		mapreduceThreads.add(mrt);
		mrt.start();
	}

	//read files from input file: 
	System.out.println("Read files: ");
	String inputPath = mapParam.get("storagedir")+mapParam.get("input");
	System.out.println("Input dir:"+inputPath);
	File file = new File(inputPath);
	//add all the lines to lineQ;
	for(File f :file.listFiles()) {
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(f));
			String line;
			while((line = buffer.readLine())!=null) {
				synchronized(lineQ) {
					lineQ.add(line);
					lineQ.notify();//System.out.println("Put into queue: "+line);
					
				}
			}
			buffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//wait for all the lines in queue are handled
	while(lineQ.size()!=0){}
	resetThreads(mapreduceThreads);
	


	//from the file: spool out is right!!!
	//so the problem is merging two files
	System.out.println("------------------2. Start Shuffling---------------");
	//push data
	//get content in every file of each worker;
	HashMap<String, String> dataInWorkers = getDataInWorkers();

	//send sveral request to pushdata. successful!!!
	for(String worker: dataInWorkers.keySet()) {

		System.out.println(port+" is sending Data to: " + workers.get(worker));
		String url = "http://"+workers.get(worker)+"/pushdata";
		String postString= "content="+dataInWorkers.get(worker);
		System.out.println("poststring length: "+postString.length());
		//System.out.println(postString);
		sendRequest(url,postString);
		//PostThreads m = new PostThreads(url, postString);
		//m.start();
	}

	//wait for all the mapping and pushdata work done;
	while(!status.get("status").equals("waiting")) {
		
	}
	
}

private void sendRequest(String requesturl, String postString) {
	// TODO Auto-generated method stub
	//if not sent successfully, resend all;
	boolean sent=false;
	URL url;
		try {
			while(!sent) {
				url = new URL(requesturl);

				System.out.println("send request: url" +url);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
				conn.setRequestProperty("charset", "utf-8");
				conn.setRequestProperty("Content-Length", "" + Integer.toString(postString.getBytes().length));
				System.out.println("content length: "+Integer.toString(postString.getBytes().length));
				
				System.out.println(postString.length());
				DataOutputStream out = new DataOutputStream(conn.getOutputStream());
				out.writeBytes(postString);
				out.flush();
				out.close();
			
				if(conn.getResponseCode()==200) 
					sent=true;
				else//resend the request
				{
					conn.disconnect();
					continue;
				}

				conn.disconnect();
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
}

// this thread do both the map and reduce by check the status. Just like what I have done in MapContext Write.
public class MapReduceThread extends Thread {
	boolean isRunning = true;
	@Override
	public void run() {
		while(isRunning) {
			String line;
			//synchronilly get line;
			try {
				synchronized(lineQ)
				{
					while(lineQ.size()==0 && isRunning) 
						lineQ.wait();
					if(!isRunning)
						continue;
					line = lineQ.remove();
				}

				try {
					Job job = (Job) Class.forName(mapParam.get("job")).newInstance();
					
					//do the mapping when status is mapping
					if(status.get("status").equals("mapping")) 
					{
							String key=line.split("\t",2)[0];
							String value=line.split("\t",2)[1];
							if(key=="")continue;
							job.map(key, value, context);

							int count=Integer.parseInt(status.get("keys_read"));
							status.put("keys_read",Integer.toString(++count));	
					}
					//do the reduce when status is not mapping, it means the status is waiting (after mapping). 
					else {
						String[] part = line.split("\t",2);
						//List<String> values= new ArrayList<String>();
						if(part.length!=2)
						{
							System.out.println("malformed line from sorted file");
							continue;
						}
						if(part[0]=="")continue;
						String values[]=part[1].split("\t");
						job.reduce(part[0], values, context);
						
						int count=Integer.parseInt(status.get("keys_read"));
						status.put("keys_read",Integer.toString(++count));						
					}

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			} catch (InterruptedException e1) {
				//e1.printStackTrace();
				isRunning = false;
			}
		}
	}
}

private void resetThreads(ArrayList<MapReduceThread> threads)
{
	for(MapReduceThread t: threads) {
		t.interrupt();
	}
	for(MapReduceThread t: threads) {
		while(t.isAlive()){
		}
	}
}

//deal with /pushdata
private void pushData(HttpServletRequest request, HttpServletResponse response) {
	// TODO Auto-generated method stub
	String content = request.getParameter("content");
	while(context==null){};
	if(context!=null) {
		fileReceivedCount++;
		context.StoreDataInSpoolIn(content);
	}
	else
	{
		System.out.println("null context detected!!!!!");
		
	}
	System.out.println("************************"+fileReceivedCount);
	if(fileReceivedCount==Integer.parseInt(mapParam.get("numWorkers")))
	{
		status.put("status","waiting");
		setWorkerStatus();
	}
}

private void runReduce(HttpServletRequest request, HttpServletResponse response) throws IOException {
	// TODO Auto-generated method stub
	System.out.println("--------------------------4. Start Reducing--------------");
	
	reduceParam.put("job",request.getParameter("job"));
	reduceParam.put("output",request.getParameter("output"));
	reduceParam.put("numThreads",request.getParameter("numThreads"));
	
	//update status
	status.put("status","reducing");
	status.put("job", reduceParam.get("job"));
	status.put("keys_read", "0");
	status.put("keys_written", "0");
    setWorkerStatus();
    
    //since output directory can be assigned by user, must create another structure function for context;
	context = new MapContext(storagedir, reduceParam.get("output"), status);
	
	
	sortForReduce(storagedir +"/spool-in/mapping_from_allworkers.txt");
	//initialize and run reduce threads
	ArrayList<MapReduceThread> mapreduceThreads = new ArrayList<MapReduceThread>();
	for(int i = 0; i<Integer.parseInt(reduceParam.get("numThreads")); i++) {
		MapReduceThread mrt=new MapReduceThread();
		mapreduceThreads.add(mrt);
		mrt.start();
	}

	//read file for reducing!should combine the same value;
	try {
		BufferedReader br = new BufferedReader(new FileReader(storagedir+"/spool-in/sorted.txt"));
		String line, lastKey="", currentKey="", value = "", currentValue="";
		//get the first valid line
		while((line = br.readLine())!=null&&line.split("\t",2).length==2)
		{
			lastKey = line.split("\t",2)[0];
			value=line.split("\t",2)[1];
			break;
		}
		while((line = br.readLine())!=null) {
			String[] parts=line.split("\t",2);
			if(parts.length!=2)
			{
				System.out.println("malformed line from sorted.txt");
				continue;
			}
			currentKey = line.split("\t",2)[0];
			if(currentKey=="")continue;
			currentValue=line.split("\t",2)[1];
			if(lastKey.equals(currentKey)) {
				value += "\t"+currentValue;//use " "to split the 
			}
			else {
				
				synchronized(lineQ) {
					//System.out.println("Put into Queue: "+line);
					if(currentKey=="")continue;
					lineQ.add(lastKey+"\t"+value);
					lineQ.notify();
				}
				value = currentValue;
				lastKey = currentKey;
			}
		}
		//add the last line
		synchronized(lineQ) {
			if(currentKey!="");
				{
					lineQ.add(lastKey+"\t"+value);
					lineQ.notify();
				}
		}
		
		br.close();
		
		while(lineQ.size()!=0){}
		
		resetThreads(mapreduceThreads);
		
		status.put("status","idle");
		status.put("job", "null");
		//keep the last written count??
		//status.put("keys_read", "0");
		//status.put("keys_written", "0");
	    setWorkerStatus();

	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	
}


//get all file content for each worker;
//HashMap(worki, content);
public HashMap<String, String> getDataInWorkers() throws IOException {
	HashMap<String, String> dataInWorkers = new HashMap<String, String>();
	for(int i = 0; i<workers.size(); i++) {
		String file = storagedir+"/spool-out/worker"+i+".txt";
		String content = "";
		BufferedReader buffer = new BufferedReader(new FileReader(file));
		String thisline=buffer.readLine();
		while(thisline!=null)
		{
			if(thisline.equals("")||thisline.equals("\n"))
				continue;
			content += thisline+"\n";
			thisline=buffer.readLine();
		}
		buffer.close();
		//System.out.println(file+" content: \n"+ content);
		dataInWorkers.put("worker"+i, content);
	}
	return dataInWorkers;
}

//sort mappingresult file for reducing;
public void sortForReduce(String path) throws IOException {
	System.out.println("------------------3.Sorting for Reduce--------");
	String fileName = path;
	//sort:
	Process p = Runtime.getRuntime().exec("sort "+fileName);
	BufferedReader buffer = new BufferedReader(new InputStreamReader(p.getInputStream()));
	//write to file
	File file = new File(storagedir +"/spool-in/sorted.txt");
	FileWriter filewriter = new FileWriter(file.getAbsoluteFile());
	String thisline=buffer.readLine();
	while(thisline!=null)
	{
		//System.out.println(thisline);
		filewriter.write(thisline+"\n");
		thisline=buffer.readLine();
	}
	buffer.close();
	filewriter.close();
}
  
}
  
