package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;

import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.FileContent;

public class ValidFileFetcher {

	private int max_size;
	private HttpClient client;
	private String host;
	private String agent = "cis455crawler";
	HashMap<String, String> head;

	private HashMap<String, String> hostDisallowMap = new HashMap<String, String>();
	private HashMap<String, Long> hostDelayMap = new HashMap<String, Long>();
	private HashMap<String, Long> hostLastCrawled = new HashMap<String, Long>();

	public ValidFileFetcher(int maxs) {
		this.max_size = maxs;
	}

	public FileContent fetch(String url_str) throws UnknownHostException,
			IOException {
		FileContent content = new FileContent();
		client = new HttpClient();
		boolean url_valid = client.initialize(url_str);

		if (!url_valid)
			return null;

		host = client.getHost();
		if (!isPolite(host, url_str) || !suitableSize(url_str))
			return null;

		content = storeToDB(url_str);

		return content;
	}

	public boolean isPolite(String host, String url_str)
			throws UnknownHostException, IOException {
		System.out.println("------------------- 1. Checking Politness! -----------------");

		// system do not store the host information. Need to download it and
		// parse it to the maps;
		if (!hostDisallowMap.containsKey(host)) {
			System.out.println("-------------------- 1-1. download the robot.txt! ------------------");
			// get robot.txt content, parse it and get the restriction according
			// to agent, agent for this assignment is "cis455crawler";
			String robot_content = client.getRobotContent();
			//System.out.println(robot_content);
			if (robot_content == null) {
				// do not has any restrictions. alway return true in this case;
				System.out.println("Can not find the robot.txt");
				return true;
			}
			HashMap<String, String> robot_map = client.parseRobotContent(
					robot_content, agent);
			String disallow = robot_map.get("disallow");
			String delay = robot_map.get("delay");

			System.out.println(url_str + "disallow: " + disallow);
			System.out.println(url_str + "delay: " + delay);

			hostDisallowMap.put(host, disallow);
			hostDelayMap.put(host, Long.parseLong(delay) * 1000);
			hostLastCrawled.put(host, System.currentTimeMillis());
		}

		// check the time restriction
		long time_left = hostDelayMap.get(host)
				- (System.currentTimeMillis() - hostLastCrawled.get(host));
		if (time_left > 0) {
			System.out.println("PLease wait for: " + time_left
					+ " milliseconds");
			try {
				Thread.sleep(time_left);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		hostLastCrawled.put(host, System.currentTimeMillis());

		// Check the disallow restriction
		String[] disallows = hostDisallowMap.get(host).split(";");
		for (String disallow : disallows) {
			// all the paths are disallowed from this agent in this host;
			if (disallow.equals("/")) {
				System.out
						.println("All the paths are disallowed from this agent in this host;");
				return false;
			}
			// deal with the "/"
			String temp = url_str;
			if (disallow.endsWith("/") && !temp.endsWith("/"))
				temp = temp + "/";
			// using contains maybe problematic in some case;
			if (temp.contains(disallow)) {
				System.out
						.println("This path is disallowed from this agent in this host;");
				return false;
			}
		}
		System.out.println("Is polite now, continue...");
		return true;
	}

	public boolean suitableSize(String url_str) throws UnknownHostException,
			IOException {
		System.out
				.println("------------------ 2. Checking Content-type and content size: ------------------");
		head = client.getHeadFromUrl(url_str);// ?????

		if (head == null) {
			System.out.println("Can not get head!!!");
			return false;
		}

		// get content type
		String content_type = head.get("content-type");
		if (content_type == null) {
			System.out.println("Can not get content type.");
			return false;
		}
		if (!content_type.equals("text/html")
				&& !content_type.equals("text/xml")
				&& !content_type.equals("application/xml")
				&& !content_type.endsWith("/xml")) {
			System.out.println("Unsupported type.");
			return false;
		}

		// get content length
		String content_length = head.get("content-length");
		if (content_length == null) {
			System.out.println("Can not get content length");
			return false;
		}
		if (Double.parseDouble(content_length) > max_size * 1024 * 1024) {
			System.out.println("Over size.");
			return false;
		}

		System.out.println("suitale size and content type.");
		return true;
	}

	public FileContent storeToDB(String url_str) throws UnknownHostException,
			IOException {
		Date lastModified = null;
		String type = null;
		FileContent content = new FileContent();

		System.out.println("--------------------3. Start fetching this webpage and store it to datebase...");
		System.out.println("url: " + url_str);

		if (head.containsKey("last-modified")) {
			String last_mod_date = head.get("last-modified");
			lastModified = DBWrapper.stringToDate(last_mod_date);
			System.out.println("modyfidate(After parse): " + lastModified);
		}
		if (head.containsKey("content-type")) {
			type = head.get("content-type");
			System.out.println("content-type: " + type);
		}

		content = DBWrapper.getFileContent(url_str);
		// if did not meet before, store it to the DB
		if (content == null) {
			System.out.println("New file, start saving to DB...");
			String raw_data = client.getContentFromUrl(url_str);
			content = new FileContent(url_str, type, raw_data, new Date());
			DBWrapper.putFileContent(content);
			// debug:
			content = DBWrapper.getFileContent(url_str);
			if(content==null)
				System.out.println("insert failed!");
			
		}
		// otherwise, check whether we need to update it.
		else {
			System.out
					.println("Has already crawled before, start checking modified date...");

			FileContent content_saved = DBWrapper.getFileContent(url_str);
			if (content_saved.getLastCrawled().before(lastModified)) {
				System.out
						.println("Has been modified after last crawling, saving the new content to DB...");
				DBWrapper.deleteFileContent(url_str);
				String raw_data = client.getContentFromUrl(url_str);
				content = new FileContent(url_str, type, raw_data, new Date());
				DBWrapper.putFileContent(content);
			} else
				System.out
						.println("Has not been modified after last crawl.");
		}

		// debug:
		content = DBWrapper.getFileContent(url_str);
		if(content==null)
			System.out.println("insert failed!");

		//else
			//System.out.println("After saving: " + " length: "
				//+ content.getRawContent().length());
		return content;
	}

}
