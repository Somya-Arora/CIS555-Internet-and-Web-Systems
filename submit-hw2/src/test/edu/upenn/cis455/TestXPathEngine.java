package test.edu.upenn.cis455;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.xpathengine.XPathEngineImpl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestXPathEngine extends TestCase {
	String xml = "http://crawltest.cis.upenn.edu/nytimes/Africa.xml";

	// check the xpath validation ()this case is not valid
	public void testValid() throws UnknownHostException, IOException,
			InterruptedException {
		String xpath = "/rss/channel/item/link\"";
		XPathEngineImpl xpath_engine = new XPathEngineImpl();
		String[] xpaths = new String[1];
		xpaths[0] = xpath;
		xpath_engine.setXPaths(xpaths);
		boolean success = xpath_engine.isValid(0);
		assertEquals(false, success);
	}

	// check the normal one
	public void test() throws UnknownHostException, IOException,
			InterruptedException {
		String xpath = "/rss/channel/item/link";
		XPathEngineImpl xpath_engine = new XPathEngineImpl();
		String[] xpaths = new String[1];
		xpaths[0] = xpath;
		xpath_engine.setXPaths(xpaths);
		Document doc = getDocument(xml);
		boolean[] ismatch = xpath_engine.evaluate(doc);
		assertEquals(true, ismatch[0]);
	}

	// check the text() value
	public void testText() throws UnknownHostException, IOException,
			InterruptedException {
		String xpath = "/rss/channel/title[text()=\"NYT > Africa\"]";
		XPathEngineImpl xpath_engine = new XPathEngineImpl();
		String[] xpaths = new String[1];
		xpaths[0] = xpath;
		xpath_engine.setXPaths(xpaths);
		Document doc = getDocument(xml);
		boolean[] ismatch = xpath_engine.evaluate(doc);
		assertEquals(true, ismatch[0]);
	}

	public void testContain() throws UnknownHostException, IOException,
			InterruptedException {
		String xpath = "/rss/channel/item/guid[@isPermaLink=\"false\"]";
		XPathEngineImpl xpath_engine = new XPathEngineImpl();
		String[] xpaths = new String[1];
		xpaths[0] = xpath;
		xpath_engine.setXPaths(xpaths);
		Document doc = getDocument(xml);
		boolean[] ismatch = xpath_engine.evaluate(doc);
		assertEquals(true, ismatch[0]);
	}

	private Document getDocument(String xmlurl) {
		// TODO Auto-generated method stub
		// do we have to build the servlet again???!
		URL url;
		InputStream in = null;
		if (!xmlurl.contains("http://"))
			xmlurl = "http://" + xmlurl;

		try {
			url = new URL(xmlurl);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		try {
			in = url.openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Tidy t = new Tidy(); // obtain a new Tidy instance
		if (xmlurl.endsWith(".xml")) {
			t.setXmlTags(true);
		}
		Document d = t.parseDOM(in, null);
		return d;
	}

}
