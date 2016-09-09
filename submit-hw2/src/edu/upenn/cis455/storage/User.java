package edu.upenn.cis455.storage;

import java.util.ArrayList;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class User {
	
@PrimaryKey
private String username;
private String password;
private ArrayList<String> channel_names; 

public User(){}

public User(String username, String password)
{
	this.username=username;
	this.password=password;
	channel_names=new ArrayList<String>();
}

public String getUsername()
{
	return username;
}
public String getPassword()
{
	return password;
}
public ArrayList<String> getChannels()
{
	return channel_names;
}
public void addChannel(String channelname)
{ 
	if(channelname!=null&&channel_names!=null)
		channel_names.add(channelname);
}
public void deleteChannel(String channelname)
{
	if(channel_names!=null)
	channel_names.remove(channelname);
}
public boolean hasChannel(String channelname)
{
	if(channel_names!=null)
		return channel_names.contains(channelname);
	else
		return false;
}

}
