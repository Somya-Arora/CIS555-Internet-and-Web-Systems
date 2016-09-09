package edu.upenn.cis455.storage;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class FileContent {

@PrimaryKey
private String url;
private String type;
private String raw_content;
private Date last_crawled;

public FileContent(){}

public FileContent(String url, String type, String raw, Date last)
{
	this.url=url;
	this.type=type;
	this.raw_content=raw;
	this.last_crawled=last;
}

public String getUrl()
{
	return url;
}
public String getType()
{
	return type;
}


public String getRawContent()
{
	return raw_content;
}

public Date getLastCrawled()
{
	return last_crawled;
}

}
