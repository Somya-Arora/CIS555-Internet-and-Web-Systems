package edu.upenn.cis455.storage;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class DBWrapper {

	private static String root_directory = null;
	private static File directory;

	private static Environment environment = null;
	// private static Database database=null;//use entity store instead!!!
	private static EntityStore store = null;
	private static PrimaryIndex<String, User> userIndex;
	private static PrimaryIndex<String, Channel> channelIndex;
	private static PrimaryIndex<String, FileContent> filecontentIndex;

	public DBWrapper() {
	};

	public static void setUp(String root) {
		// build the environment
		if (environment == null || root != root_directory) {
			// deal with the directory
			if (root == null)
				root_directory = System.getProperty("user.dir") + "/mydb";
			else
				root_directory = root;

			File temp = new File(root_directory);
			if (temp.exists())
				directory = temp;
			else {
				temp.mkdirs();// if you are trying to creating a child folder
								// inside parent folder that does not exist, Use
								// mkdirs() instead mkdir().
				directory = temp;
				System.out.println("Directory Created");
			}

			// build up environment, store, and Index;
			EnvironmentConfig envir_config = new EnvironmentConfig();
			envir_config.setAllowCreate(true);
			envir_config.setTransactional(true);
			environment = new Environment(directory, envir_config);

			StoreConfig store_config = new StoreConfig();
			store_config.setAllowCreate(true);
			store_config.setTransactional(true);
			store = new EntityStore(environment, "EntityStore", store_config);

			userIndex = store.getPrimaryIndex(String.class, User.class);
			channelIndex = store.getPrimaryIndex(String.class, Channel.class);
			filecontentIndex = store.getPrimaryIndex(String.class,
					FileContent.class);

		}
		System.out.println("database setted up!");

	}

	public static void close() {
		if (store != null)
			store.close();
		if (environment != null)
			environment.close();
	}

	// functions for User
	public static void putUser(User user)// what if user is null?
	{
		userIndex.put(user);
		System.out.println("user successfully putted!");
	}

	public static User getUser(String username) {
		return userIndex.get(username);
	}

	public static void deleteUser(String username) {
		userIndex.delete(username);
	}

	public static boolean containsUser(String username) {
		return userIndex.contains(username);
	}

	public static boolean UserPasswordMatch(String username, String password) {
		User user = userIndex.get(username);
		if (user == null) {
			System.out.println("no such user found");
			return false;
		}
		return password.equals(user.getPassword());
	}

	// functions for Channel;
	public static void putChannel(Channel channel) {
		channelIndex.put(channel);
	}

	public static Channel getChannel(String channelname) {
		return channelIndex.get(channelname);
	}

	public static ArrayList<Channel> getAllChannels() {
		ArrayList<Channel> channels = new ArrayList<Channel>();

		EntityCursor<Channel> cursor = channelIndex.entities();
		Iterator<Channel> iterator = cursor.iterator();
		while (iterator.hasNext())
			channels.add(iterator.next());
		cursor.close();

		return channels;
	}

	public static boolean deleteChannel(String username, String channelname) {
		// String owner=channelIndex.get(channelname).getUserName();
		// if(!owner.equals(username))
		// {
		// System.out.println("the channel you try to delete does not belongs to you!!!");
		// return false;
		// }
		channelIndex.delete(channelname);
		// update user
		User user = userIndex.get(username);
		user.deleteChannel(channelname);
		putUser(user);
		return true;
	}

	public static boolean containsChannel(String channelname) {
		return channelIndex.contains(channelname);
	}

	// public static ArrayList<Channel> getChannelByUser(String username)
	// {
	// return null;
	// }

	// functions for FileContent
	public static void putFileContent(FileContent file) {
		filecontentIndex.put(file);
	}

	public static FileContent getFileContent(String url) {
		return filecontentIndex.get(url);
	}

	public static void deleteFileContent(String url) {
		filecontentIndex.delete(url);
	}

	// helper function
	public static Date stringToDate(String last_mod_date) {
		// TODO Auto-generated method stub
		Date date = null;
		SimpleDateFormat formatter = null;
		// how many formats do we have to consider???
		String[] format = { "EEE, dd MMM yyyy HH",
				"EEE, dd MMM yyyy HH:mm:ss zzz",
				"EEEEE, dd-MMM-yy HH:mm:ss zzz", "EEE MMM dd HH:mm:ss yyyy" };
		formatter = new SimpleDateFormat(format[0]);
		try {
			date = formatter.parse(last_mod_date);
		} catch (ParseException e0) {
			formatter = new SimpleDateFormat(format[1]);
			try {
				date = formatter.parse(last_mod_date);
			} catch (ParseException e1) {
				formatter = new SimpleDateFormat(format[2]);
				try {
					date = formatter.parse(last_mod_date);
				} catch (ParseException e2) {
					formatter = new SimpleDateFormat(format[2]);
					try {
						date = formatter.parse(last_mod_date);
					} catch (ParseException e3) {
						System.out.println("Can not parse date!!!");
					}
				}

			}
		}
		return date;
	}

}
