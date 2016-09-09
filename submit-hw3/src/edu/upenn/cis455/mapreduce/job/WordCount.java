package edu.upenn.cis455.mapreduce.job;

import edu.upenn.cis455.mapreduce.Context;
import edu.upenn.cis455.mapreduce.Job;

public class WordCount implements Job {
//key-value: line
  public void map(String key, String value, Context context)
  {
    // Your map function for WordCount goes here
	 // System.out.println("mapping: "+value);
	  String[] words = value.split(" ");
	  for (String word : words) {
		  word = word.replaceAll("\\s+", "");
		  if (!word.equals("")) {
			  context.write(word,"1");
		  }
	  }
  }
  
  //key-value: word
  public void reduce(String key, String[] values, Context context)
  {
	  
    // Your reduce function for WordCount goes here
	  context.write(key, String.valueOf(values.length));
  }
  
}
