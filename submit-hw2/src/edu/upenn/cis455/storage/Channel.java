package edu.upenn.cis455.storage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

//Store the XPath expressions and URLs for XSL stylesheets for the user-defined channels and the name of the user that created them
@Entity
public class Channel {

	@PrimaryKey
	private String channelname;
	private String username;
	private ArrayList<String> xpaths;
	private String xsl_url;
	private ArrayList<String> matched_doc_urls;

	public Channel() {

	}

	public Channel(String channelname, String username,
			ArrayList<String> xpaths, String xsl_url) {
		this.channelname = channelname;
		this.username = username;
		this.xpaths = xpaths;
		this.xsl_url = xsl_url;
		matched_doc_urls = new ArrayList<String>();
	}

	public String getChannelName() {
		return channelname;
	}

	public String getUserName() {
		return username;
	}

	public ArrayList<String> getXpaths() {
		return xpaths;
	}

	public String getXslUrls() {
		return xsl_url;
	}

	public ArrayList<String> getMatchedDoc() {
		if (matched_doc_urls != null)
			return matched_doc_urls;
		else
			return null;
	}

	public void addMatchedDoc(String url) {
		if (matched_doc_urls != null)
			if(!matched_doc_urls.contains(url))
			matched_doc_urls.add(url);
	}

}
