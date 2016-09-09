package edu.upenn.cis455.xpathengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import java.util.regex.*;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XPathEngineImpl implements XPathEngine {
	
	String xpaths[]; 
	//remember to clear the arraylist!
	LinkedList<String> segments;//=new LinkedList<String>();	//used to store segments:  segment/segment/segment
	HashMap<String,ArrayList<String>> nodemap;//=new HashMap<String,ArrayList<String>>();//used to store node[][][]...

  public XPathEngineImpl() {
    // Do NOT add arguments to the constructor!!
  }
	
  public void setXPaths(String[] s) {
    /* TODO: Store the XPath expressions that are given to this method */
	  xpaths = s;
  }
  
  
  public boolean isValid(int i) {
    /* TODO: Check which of the XPath expressions are valid */
	String xpath=xpaths[i];
	return isValid(xpath);
  }
  
  // overide the int edition
  
  public boolean isValid(String xpath) {
	    /* TODO: Check which of the XPath expressions are valid */
	  
		/* step1: easy case;
		 * setp2: break path into segments;
		 * step3: check segments*/
	 
	if(!xpath.startsWith("/")||xpath.contains(":")||xpath.equals("/"))
		return false;
	
	
	nodemap=new HashMap<String,ArrayList<String>>();
	segments=new LinkedList<String>();//refresh former one!
	
	String temp_segments[]=breakPath(xpath);
	// !!!!!!!!!!"/" might between the [ ], so do as below
	for(int j=0;j<temp_segments.length;j++)
		{
			String temp=temp_segments[j];
			if(temp.contains("["))
			{
			int nlb=temp.length()-temp.replace("[", "").length();//number of left bracket
			int nrb=temp.length()-temp.replace("]", "").length();//number of right bracket
			while(nlb>nrb && j<temp_segments.length-1)
			{
				temp=temp+"/"+temp_segments[++j];
				nlb=temp.length()-temp.replace("[", "").length();
				nrb=temp.length()-temp.replace("]", "").length();
			}
			if(nlb>nrb)     //the path is invalid...
				return false;
			}
			segments.add(temp);		
		}
	
	//check every segment
	for(String seg:segments)
	{
		System.out.println("segments part: "+seg);
	   if(!isSegValid(seg))return false;
	}
	
    return true;
  }
	
  private String[] breakPath(String xpath) {
	// TODO Auto-generated method stub
	if(xpath.startsWith("/"))
		xpath=xpath.substring(1);
	String result[]=xpath.split("/");
	return result;
}  
  
  private boolean isSegValid(String seg) {
	// TODO Auto-generated method stub
	//if(seg.startsWith("@"))return false;  //used to determine the false immediately;
	  
	// \s:space, *:0 or many times, a-zA-Z_0-9: alpha and numerica, .:every char, + at least has one
	// example:        a2bcd   [sdsa][dsa]
	String regex="(\\s)*([a-zA-Z_][a-zA-Z_0-9]*)(\\s)*(\\[.+\\])*";//"(\s)*([a-zA-Z_]\w*)(\s)*(\[.+\])*"
	Pattern pattern=Pattern.compile(regex);
	Matcher matcher=pattern.matcher(seg);

	if(!matcher.matches())return false;
	else System.out.println("match regex 1");
	
	String temp=seg;
	if(temp.length()-temp.replace("\"", "").length()>2)return false;// only support a pair of "";
	
	if(!seg.contains("["))
	{
		nodemap.put(seg, null);
		return true;
	}
	
	String node=""; //node[inbrace]
	String inbrace="";
	boolean hadNode=false;
	boolean strStart=false;
	boolean braceStart=false;
	Stack<Character> helper=new Stack<Character>();//store "[".
	
	for(int i=0;i<seg.length();i++)
	{
		char c=seg.charAt(i);
		switch(c)
		{
		case '[':
			if(!strStart)
			{
				braceStart=true;
				helper.push(c);
				if(!hadNode)
				{
					node=seg.substring(0,i);// the node is the part before '[';
					System.out.println("node is "+node);
					hadNode=true;
				}
			}
			break;
		case ']':
			if(!strStart)
			{
				helper.pop();
				if(helper.isEmpty()) //a complete part in [] has been stored
				{
					braceStart=false;//notice that this ] will not be stored in the inbrace.
					System.out.println("what is in brace: "+inbrace);
					if(!isValidInBrace(inbrace))return false;
					else//store it into map!
					{
						if(nodemap.containsKey(node))
							nodemap.get(node).add(inbrace.substring(1));//cut the first [
						else
						{
							ArrayList<String> value=new ArrayList<String>();
							value.add(inbrace.substring(1));//cut the first [
							nodemap.put(node, value);
						}
					}
					inbrace="";
				}
			}
			break;
		case '"':
			strStart=!strStart;
			break;
		}
		if(braceStart)
			inbrace+=c;	
	}  
	return true;
}

private boolean isValidInBrace(String inbrace) {
	// TODO Auto-generated method stub
	//check what between the [];
	
	//notice the [ at the start!
	inbrace=inbrace.substring(1);
	//use regex to represent the 3 conditions

	String regex1="(\\s)*text(\\s)*\\((\\s)*\\)(\\s)*=(\\s)*\\\"[^\\\"]+\\\"(\\s)*";//text()="";//(\s)*text(\s)*\((\s)*\)(\s)*=(\s)*\"[^\"]+\"(\s)*
	String regex2="(\\s)*contains(\\s)*\\((\\s)*text(\\s)*\\((\\s)*\\)(\\s)*,(\\s)*\\\"[^\\\"]+\\\"(\\s)*\\)";
	String regex3="(\\s)*\\@(\\s)*([A-Z_a-z][A-Z_a-z0-9-.]*)(\\s)*\\=\\\"[^\\\"]+\\\"(\\s)*";
	String[] regexes={regex1,regex2,regex3};
	
	for(int i=0;i<regexes.length;i++)
	{
		Pattern pattern=Pattern.compile(regexes[i]);
		Matcher matcher=pattern.matcher(inbrace);
		if(matcher.matches())
			{
			System.out.println("match the regex:"+regexes[i]);
			return true;
			}
	}
	//if there are many layers of [[]], treat the inner layer like a new xpath
	System.out.println("need further detection for valid!");
	return isValid("/"+inbrace);
}

public boolean[] evaluate(Document d) { 
    /* TODO: Check whether the document matches the XPath expressions */
	boolean[] ismatch=new boolean[xpaths.length];
	for(int i=0;i<ismatch.length;i++)ismatch[i]=false;//initialized to false
	if (d==null)return ismatch;
	else System.out.println("document was found, continue to check match");
	
	ArrayList<Node> docrootNode=new ArrayList<Node>();
	docrootNode.add(d.getDocumentElement());
	
	for(int i=0;i<xpaths.length;i++)
	{
		if(isValid(i))
		{
			String xpath=xpaths[i].substring(1);
			ismatch[i]=evaluate(xpath,docrootNode);
		}
	}
    return ismatch; 
  }

private boolean evaluate(String xpath, ArrayList<Node> currents) {
	// TODO Auto-generated method stub

	//get all the segment again, since the segment might be refreshed in the inner layer.
	segments=new LinkedList<String>();//refresh former one!
	String temp_segments[]=breakPath(xpath);
	
	for(String t_seg:temp_segments)
	{
		System.out.println("the temp segments are: "+t_seg);
	}
	// !!!!!!!!!!"/" might between the [ ], so do as below
	for(int j=0;j<temp_segments.length;j++)
		{
			String temp=temp_segments[j];
			if(temp.contains("["))
			{
			int nlb=temp.length()-temp.replace("[", "").length();//number of left bracket
			int nrb=temp.length()-temp.replace("]", "").length();//number of right bracket
			while(nlb>nrb && j<temp_segments.length-1)
			{
				temp=temp+"/"+temp_segments[++j];
				nlb=temp.length()-temp.replace("[", "").length();
				nrb=temp.length()-temp.replace("]", "").length();
			}
			}
			segments.add(temp);	
			System.out.println("Segment:" + temp);
		}
	
	ArrayList<Node> nexts=currents;
	//check every segment
	for(String seg:segments)
	{
		System.out.println("(check match)segments part: "+seg);
		nexts=isSegMatched(seg,nexts);
		if(nexts==null||nexts.size()==0)
			{
			System.out.println("no nexts candidated found!");
			return false;
			}
	}
     return nexts != null && nexts != currents;
}

//find whether there are suitable node from nodes for segments to match!!!
//at the same time return the next segment's candidate match!
private ArrayList<Node> isSegMatched(String seg, ArrayList<Node> nodes) {
	// TODO Auto-generated method stub
	ArrayList<Node> nexts=new ArrayList<Node>();
	String nodename="";
	if(!seg.contains("["))nodename=seg.trim();
	else nodename=seg.split("[\\[]")[0];
	
	for(Node node:nodes)
	{
		if(nodemap.get(nodename)!=null)
		{
			ArrayList<String> values=nodemap.get(nodename);//value represent what is in the brace[]
			for(String value:values)
			{
				if(matchedInBrace(value,node))
				{
					NodeList children=node.getChildNodes();
					for(int i=0;i<children.getLength();i++)
					{
						nexts.add(children.item(i));
					}					
				}
			}
			System.out.println("the number of children of "+seg+":  "+nexts.size());
			
			return nexts;
			
		}
		else
		{
			if(nodename.equals(node.getNodeName()))
				{
					NodeList children=node.getChildNodes();
					for(int i=0;i<children.getLength();i++)
					{
						nexts.add(children.item(i));
					}
					System.out.println("the number of children of "+seg+":  "+nexts.size());
					return nexts;					
				}
		}
	}

	return null;
}

private boolean matchedInBrace(String value, Node node) {
	// TODO Auto-generated method stub
	value=value.trim();//actually, should not be trimed between " "???
	int flag=regexHelper(value);
	
	if(flag==1)//match text();
	{
		String text=value.split("\"")[1];
		Node textnode=node.getFirstChild();
		if(textnode!=null&&textnode.getNodeType()==Node.TEXT_NODE&&textnode.getNodeValue().equals(text))
		{
			System.out.println("text matched");
			return true;
		}
		
	}
	else if(flag==2)//match contains
	{
		String text=value.split("\"")[1];
		Node textnode=node.getFirstChild();
		if(textnode!=null&&textnode.getNodeType()==Node.TEXT_NODE&&textnode.getNodeValue().contains(text))
		{
			System.out.println("text contained");
			return true;
		}
	}
	else if(flag==3)//match attributes  @key=""
	{
		String temp=value;
		String key_att=temp.split("=")[0].replace("@","").trim();
		String value_att=value.split("\"")[1].trim();
		
		System.out.println("key_att: "+key_att);
		System.out.println("value_att: "+value_att);
		
		NamedNodeMap attributes = node.getAttributes();
		if(attributes!=null)
		{
			System.out.println("have attibutes number: "+attributes.getLength());
			Node att=attributes.getNamedItem(key_att);
			if(att != null && att.getNodeValue().equals(value_att))
			{
				System.out.println("attributes matched");
				return true;
			}
		}
		
		
	}
	else//need further detection, get the nodes children then 
	{
		System.out.println("need further detection for matching");
		ArrayList<Node> nexts = new ArrayList<Node>();
		NodeList children = node.getChildNodes();
		for(int i = 0; i < children.getLength(); i ++){
			nexts.add(children.item(i));
		}
		return evaluate(value, nexts);
	}
	
	return false;
}

private int regexHelper(String text)
{
	String regex1="(\\s)*text(\\s)*\\((\\s)*\\)(\\s)*=(\\s)*\\\"[^\\\"]+\\\"(\\s)*";//text()="";//(\s)*text(\s)*\((\s)*\)(\s)*=(\s)*\"[^\"]+\"(\s)*
	String regex2="(\\s)*contains(\\s)*\\((\\s)*text(\\s)*\\((\\s)*\\)(\\s)*,(\\s)*\\\"[^\\\"]+\\\"(\\s)*\\)";
	String regex3="(\\s)*\\@(\\s)*([A-Z_a-z][A-Z_a-z0-9-.]*)(\\s)*\\=\\\"[^\\\"]+\\\"(\\s)*";
	
	String[] regexes={regex1,regex2,regex3};
	for(int i=0;i<regexes.length;i++)
	{
		Pattern pattern=Pattern.compile(regexes[i]);
		Matcher matcher=pattern.matcher(text);
		if(matcher.matches())
			{
			System.out.println("match the regex:"+regexes[i]);
			return i+1;
			}
	}
	return 0;
}

}
