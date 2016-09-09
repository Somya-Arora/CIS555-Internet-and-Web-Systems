package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

public class HttpClient {

	private URL url;
	private String url_str;
	private String host;
	private int port;
	private String root_path;
	private String relative_path;
	private boolean is_valid_url = false;

	private String content_type;
	private HashMap<String, String> head;
	private String content;

	public HttpClient() {
	}

	public boolean initialize(String url_str) {
		try {
			this.url = new URL(url_str);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		this.url_str = url_str;
		this.host = url.getHost();
		this.port = url.getPort();
		if (this.port == -1)
			this.port = 80;
		root_path = url.getProtocol() + "://" + url.getHost();
		relative_path = url.getPath();
		head = new HashMap<String, String>();
		// content=new String();//initialized when it has content.
		is_valid_url = true;
		return true;
	}

	public String getRobotContent() throws UnknownHostException, IOException {
		String robot_url = url.getProtocol() + "://" + url.getHost()
				+ "/robots.txt";
		String robot_content = getContentFromUrl(robot_url);
		// System.out.println("the robot content length for robot_url "+url.getPath()+" is: "+robot_content);
		return robot_content;
	}

	// store the "disallow" and "delay" in a map according to the agent, do not
	// consider the default *;
	public HashMap<String, String> parseRobotContent(String content,
			String agent) {
		HashMap<String, String> robot_map = new HashMap<String, String>();
		String disallow = "";
		String delay = "0";

		String[] lines = content.split("\r\n");
		agent = "user-agent: " + agent;
		for (int i = 0; i < lines.length; i++) {
			String thisline = lines[i];
			if (thisline.equalsIgnoreCase(agent)) {
				// disallow.clear();
				i++;
				while (lines[i].contains(":")) {
					thisline = lines[i];
					if (thisline.startsWith("Disallow"))
						disallow += thisline.split(":")[1].trim() + ";";
					if (thisline.startsWith("Crawl-delay"))
						delay = thisline.split(":")[1].trim();
					i++;
				}
				break;
			}
		}
		robot_map.put("disallow", disallow);
		robot_map.put("delay", delay);
		return robot_map;
	}

	// all the get header and content from url function has to contain the url
	// parameter in case of get different (for example, robot file) file
	// get headmap and content at same time;
	// not used in this project;
	public boolean getHeaderAndContentFromUrl(String url)
			throws UnknownHostException, IOException {
		if (!initialize(url))
			return false;

		System.out.println(url_str + ": Downloading...");
		boolean status = false;
		// Send request;
		Socket socket = new Socket(host, port);
		PrintWriter out = new PrintWriter(socket.getOutputStream());
		out.write("GET " + url_str + " HTTP/1.1\r\n");
		out.write("User-Agent: cis455crawler\r\n");
		out.write("Host: " + host + ":80\r\n");
		out.write("Connection: close\r\n\r\n");
		out.flush();
		out.close();

		// Read the response;
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		String thisline = in.readLine();

		// right now just handle the 200 OK ;
		// will handle the redirect latter;
		if (thisline.contains("200")) {
			status = true;
			thisline = in.readLine();
			// read the head to head map;
			while (thisline != null && thisline != "") {
				String[] part = thisline.split(":");
				String key = part[0].trim().toLowerCase();
				String value = part[1].trim();
				head.put(key, value);
				thisline = in.readLine();
			}
			// if contains the content, then read the body
			if (head.containsKey("content-type")) {
				content_type = head.get("content-type");// null before
				// a string changed frequently should use stringbuffer then
				// convert it to string.
				StringBuffer content_buffer = new StringBuffer();
				thisline = in.readLine();
				while (thisline != null) {
					content_buffer.append(thisline + "\r\n");
					thisline = in.readLine();
				}
				content = content_buffer.toString();// null before
			}
		}

		out.close();
		in.close();
		socket.close();
		return status;
	}

	// get head map;
	public HashMap<String, String> getHeadFromUrl(String url)
			throws UnknownHostException, IOException {
		if (!initialize(url))
			return null;

		System.out.println(url_str + ": Geting Head...");
		HashMap<String, String> head = null;
		// Send request;
		Socket socket = new Socket(host, port);
		PrintWriter out = new PrintWriter(socket.getOutputStream());
		out.write("HEAD " + url_str + " HTTP/1.1\r\n");
		out.write("User-Agent: cis455crawler\r\n");
		out.write("Host: " + host + ":80\r\n");
		out.write("Connection: close\r\n\r\n");
		out.flush();

		// Read the response;
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		String thisline = in.readLine();
		System.out.println(thisline);
		// right now just handle the 200 OK ;
		// will handle the redirect latter;
		if (thisline.contains("200")) {
			head = new HashMap<String, String>();
			thisline = in.readLine();
			// read the head to head map;
			while (thisline != null && !thisline.equals(""))
			{
				String[] part = thisline.split(":");
				String key = part[0].trim().toLowerCase();
				String value = part[1].trim();
				head.put(key, value);
				thisline = in.readLine();
			}
		} else if (thisline.contains("302")||thisline.contains("301")) {
			System.out.println("handle the redirect.");
			//find the new location.
			String newlocation=null;
			thisline = in.readLine();
			while (thisline != null && !thisline.equals(""))
			{
				String[] part = thisline.split(":",2);
				String key = part[0].trim().toLowerCase();
				if(key.contains("location"))
					{
					newlocation= part[1].trim();
					System.out.println("new url: "+newlocation);
					break;
					}
				thisline = in.readLine();
			}
			//
			if(newlocation!=null)
			{
				if(!newlocation.startsWith("http"))
					newlocation=root_path+"/"+newlocation;
				head= getHeadFromUrl(newlocation);
			}	
		}else
		{
			while (thisline != null && !thisline.equals(""))
			{
			System.out.println(thisline);
			thisline = in.readLine();
			}
			
		}

		out.close();
		in.close();
		socket.close();
		return head;
	}

	// get content and update variable content_type;
	public StringBuffer getContentBufferFromUrl(String url)
			throws UnknownHostException, IOException {
		if (!initialize(url))
			return null;

		System.out.println(url_str + ": Downloading...");
		// Send request;
		Socket socket = new Socket(host, port);
		PrintWriter out = new PrintWriter(socket.getOutputStream());
		out.write("GET " + url_str + " HTTP/1.1\r\n");
		out.write("User-Agent: cis455crawler\r\n");
		out.write("Host: " + host + ":80\r\n");
		out.write("Connection: close\r\n\r\n");
		out.flush();

		// Read the response;
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		String thisline = in.readLine();
		StringBuffer content_buffer = new StringBuffer();
		// right now just handle the 200 OK ;
		// will handle the redirect latter;

		if (thisline.contains("200")) {
			System.out.println("got right reponse:");
			thisline = in.readLine();
			// get content-type from head;
			while (thisline != null && !thisline.equals("")) {
				String[] part = thisline.split(":");
				String key = part[0].trim().toLowerCase();
				if (key.equals("content-type")) {
					content_type = part[1].trim();
					System.out.println("content-type: " + content_type);
				}
				thisline = in.readLine();
			}
			// if contains the content, then read the body
			if (content_type != null && !content_type.equals("")) {
				thisline = in.readLine();
				while (thisline != null) {
					content_buffer.append(thisline + "\r\n");// remain the \r\n!!!!!it will be used in parse!
					thisline = in.readLine();
				}
			}
		}else if (thisline.contains("302")||thisline.contains("301")) {
			System.out.println("handle the redirect.");
			
			String newlocation=null;
			thisline = in.readLine();
			while (thisline != null && !thisline.equals(""))
			{
				String[] part = thisline.split(":",2);
				String key = part[0].trim().toLowerCase();
				if(key.contains("location"))
					{
					newlocation= part[1].trim();
					System.out.println("new url: "+newlocation);
					break;
					}
				thisline = in.readLine();
			}
			if(newlocation!=null)
			{
				if(!newlocation.startsWith("http"))
					newlocation=root_path+"/"+newlocation;
				content_buffer= getContentBufferFromUrl(newlocation);
			}	
		}
		//else return null;

		out.close();
		in.close();
		socket.close();
		return content_buffer;
	}

	public String getContentFromUrl(String url) throws UnknownHostException,
			IOException {
		String content = null;
		StringBuffer content_buffer = getContentBufferFromUrl(url);

		if (content_buffer != null)
			content = content_buffer.toString();

		if (content == null)
			System.out.println("the content for url " + url + " is: null");
		else
			System.out.println("the content length for url " + url + " is: "
					+ content.length());

		return content;
	}

	// get function;
	public String getHost() {
		return host;
	}

	public HashMap<String, String> getHead() {
		return head;
	}

	public String getContent() {
		return content;
	}

	public String getType() {
		return content_type;
	}

}
