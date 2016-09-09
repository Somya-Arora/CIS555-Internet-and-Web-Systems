package edu.upenn.cis455.mapreduce.job;

import java.util.StringTokenizer;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

public class PageRankStepOne implements Job {

	@Override
	public void map(String key, String value, Context context) {
		// TODO Auto-generated method stub
		String[] parts=value.split("\t",2);
		
		if(parts.length!=2)
		{
			System.out.println("invalid url line");
			return;
		}
		
		String url=parts[0];
		StringTokenizer outlinks=new StringTokenizer(parts[1]);
		
		//use a flag to eliminate the urls which has not been downloaded
		context.write(url,"isDownloaded");
		
		while(outlinks.hasMoreTokens())
			context.write(outlinks.nextToken(),url);
	}

	@Override
	public void reduce(String key, String[] values, Context context) {
		// TODO Auto-generated method stub
		StringBuilder inlinks=new StringBuilder();
		boolean isDownloaded=false;
		for(String value:values)
		{
			String v=value;
			if(v.equals("isDownloaded"))
			{
				isDownloaded=true;
				context.write(key, "isDownloaded");
			}
			else inlinks.append(" "+v);
		}
		
		//filter out urls which has not been downloaded
		if(isDownloaded&&inlinks.length()!=0)
			context.write(key,inlinks.toString().substring(1, inlinks.length()));
	}

}
