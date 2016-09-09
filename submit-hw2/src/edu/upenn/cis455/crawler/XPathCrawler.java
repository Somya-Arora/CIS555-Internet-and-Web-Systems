package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import edu.upenn.cis455.storage.*;

public class XPathCrawler {

	String start_url;
	String root_directory;
	int max_size;
	int max_num;
	static Queue<String> url_tocrawl = new LinkedList<String>();
	ValidFileFetcher fetcher;
	Processor processor;

	// three parameters: start url, root storage directory, the max size of
	// files to be crawled, the max number have to be crawled.
	public static void main(String args[]) {
		/* TODO: Implement crawler */
		if (args.length < 3)
			System.out.println("Name: Menglu Wang \nSEAS Login: mengluw");
		else {
			XPathCrawler crawler;
			if (args.length == 3)
				crawler = new XPathCrawler(args[0], args[1],
						Integer.parseInt(args[2]));
			else
				crawler = new XPathCrawler(args[0], args[1],
						Integer.parseInt(args[2]), Integer.parseInt(args[3]));

			if (crawler != null) {
				try {
					crawler.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				crawler.stop();
			}
		}
	}

	public XPathCrawler(String url, String root, int maxs) {
		this.start_url = url;
		this.root_directory = root;
		this.max_size = maxs;
		this.max_num = 1000;
		url_tocrawl.add(start_url);
	}

	public XPathCrawler(String url, String root, int maxs, int maxn) {
		this(url, root, maxs);
		this.max_num = maxn;
	}

	public void start() throws UnknownHostException, IOException {
		int crawled = 0;

		DBWrapper.setUp(root_directory);

		fetcher = new ValidFileFetcher(max_size);
		processor = new Processor();

		while (!url_tocrawl.isEmpty() && crawled < max_num) {

			String url_str = url_tocrawl.poll();

			FileContent file_content = fetcher.fetch(url_str);
			if(file_content==null)
				{
				System.out.println("can not fetch the content for this url.");
				}
			processor.process(file_content, url_tocrawl);
			processor.xpathMatcher(file_content);

			crawled++;
			System.out.println("url has been crawled: " + crawled);
		}
		
		//get channels from db and match it;
	}

	public void stop() {
		url_tocrawl.clear();
		System.out.println("Stop Crawling");
	}
	
}