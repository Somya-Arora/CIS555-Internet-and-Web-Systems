package edu.upenn.cis455.crawler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.FileContent;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;

public class Processor {

	private ArrayList<String> extrated_urls = new ArrayList<String>();

	public Processor() {
	}

	public void process(FileContent file_content, Queue<String> url_tocrawl) {
		// TODO Auto-generated method stub
		System.out.println("-------------------- 4. Extracting url-------------");
		if (file_content == null)
			return; 
		String current_url = file_content.getUrl();
		String type = file_content.getType();
		String raw_content = file_content.getRawContent();
		
		if(raw_content==null)
			System.out.println("raw_content  is null");
		

		// only deal with the html
		if (type.equals("text/html")) {
			// use regex to capture the url, group are used to capture ();
			String regex = "<a\\s+href\\s*=\\s*[\\\"](.*?)[\\\"]";
			Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(raw_content);
			while (matcher.find()) {
				String new_url = matcher.group(1).trim();
				// System.out.println("new url: " + new_url);
				// useless url
				if (new_url.equals("#") || new_url.startsWith("mailto:")
						|| new_url.startsWith("javascript"))
					continue;
				// absolute url
				if (new_url.startsWith("http")) {
					if (!extrated_urls.contains(new_url))
						extrated_urls.add(new_url);
					// regrade
					continue;
				}
				// relative url, cut the "/" first;
				int len = current_url.length();
				if (current_url.endsWith("/"))
					current_url = current_url.substring(0, len - 1);
				if (new_url.startsWith("/"))
					new_url = new_url.substring(1);
				new_url = current_url + "/" + new_url;

				if (!extrated_urls.contains(new_url))
					extrated_urls.add(new_url);

				System.out.println("new url(store to db): " + new_url);

			}

			url_tocrawl.addAll(extrated_urls);

		}
	}

// todo!!!!!!!!!!!!!!!!by 4/26!!!!!!!!!!!!!!!!!
	public void xpathMatcher(FileContent file_content)
			throws UnknownHostException, IOException {

		if(file_content==null)
			return;
		System.out.println("-------------------- 5. XPath Matching...-------------");
		Document doc = buildDocFromStr(file_content.getRawContent(),
				file_content.getType());
		ArrayList<Channel> channels = DBWrapper.getAllChannels();
		for (Channel channel : channels) {
			System.out.println(" channel name: "+ channel.getChannelName());
			ArrayList<String> xpaths_array = channel.getXpaths();
			String[] xpaths = xpaths_array.toArray(new String[xpaths_array
					.size()]);
			XPathEngineImpl matcher = new XPathEngineImpl();
			matcher.setXPaths(xpaths);
			boolean[] matched = matcher.evaluate(doc);
			for (int i = 0; i < xpaths.length; i++) {
				if (matched[i]) {
					
					System.out.println( xpaths[i] + " matches file "+file_content.getUrl());
					channel.addMatchedDoc(file_content.getUrl());
					DBWrapper.putChannel(channel);
					
					//debug result:
					System.out.println("******************After detect matching for: " + file_content.getUrl());
					ArrayList<String> thischannel_matchedfile=DBWrapper.getChannel(channel.getChannelName()).getMatchedDoc();
					System.out.println("there are  "+thischannel_matchedfile.size()+" files matching this xpatch");	
				}
				else
					System.out.println(xpaths[i] + " does not match file "+file_content.getUrl());
			}

		}
		
	}

	public static Document buildDocFromStr(String content, String type)
			throws UnknownHostException, IOException {
		// String content=getContentFromUrl(url);
		if (content == null || content == "")
			return null;

		Document doc = null;
		Tidy tidy = new Tidy();
		// used to debug
		File out = new File("output.txt");
		FileOutputStream fos = new FileOutputStream(out);
		InputStream body_stream = new ByteArrayInputStream(
				content.getBytes(StandardCharsets.UTF_8));
		if (type.endsWith("text/html")) {
			tidy.setDocType("omit");
			tidy.setXHTML(true);// ????????//do we still need this???
			doc = tidy.parseDOM(body_stream, fos);
		} else if (type.endsWith("/xml")) {
			tidy.setXmlTags(true);
			doc = tidy.parseDOM(body_stream, fos);
		}
		return doc;
	}
}