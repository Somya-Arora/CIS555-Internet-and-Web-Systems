package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis455.storage.Channel;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.FileContent;
import edu.upenn.cis455.storage.User;

@SuppressWarnings("serial")
public class CrawlerServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		// PrintWriter out=new PrintWriter(response.getOutputStream());
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		String action = request.getParameter("action");

		// print the page according to the action received
		if (action == null || action.equals("login")) {
			printLoginPage(out);
			return;
		}
		if (action.equals("register")) {
			printRegisterPage(out);
			return;
		}
		if (action.equals("displaychannels")) {
			// need to set up database to get the channels
			String database_root = getServletConfig().getServletContext()
					.getInitParameter("BDBstore");
			DBWrapper.setUp(database_root);
			printDisplayChannelsPage(out, request);
			// DBWrapper.close();
			return;
		}
		if (action.equals("displaychannelcontent")) {
			// need to set up database to get the channels
			String database_root = getServletConfig().getServletContext()
					.getInitParameter("BDBstore");
			DBWrapper.setUp(database_root);
			printDisplayChannelContentPage(out, request);
			return;
		}
		if (action.equals("logout")) {
			printLogoutPage(out, request);
			return;
		}
		if (action.equals("delete")) {
			printDeleteChannelPage(out, request);
			return;
		}

		return;
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		String action = request.getParameter("action");
		String username;
		String password;

		// setup database;
		String database_root = getServletConfig().getServletContext()
				.getInitParameter("BDBstore");
		System.out.println("data base root directory: " + database_root);
		DBWrapper.setUp(database_root);

		// print the page according to the action received
		if (action == null) {
			System.out.println("Not Found");
			doGet(request, response);
			return;
		}

		if (action.equals("login")) {
			username = request.getParameter("username");
			password = request.getParameter("password");
			if (username == null || password == null || username == ""
					|| password == "") {
				System.out.println("username or password is empty");
				doGet(request, response);
				return;
			}
			if (DBWrapper.UserPasswordMatch(username, password)) {
				request.getSession().setAttribute("username", username);
				response.sendRedirect(request.getContextPath()
						+ "/crawler?action=displaychannels");
			} else {
				System.out.println("username and passowrd mismatch!");
				printMessagePage(out, "LoginError");
			}
			return;
		}

		if (action.equals("register")) {
			username = request.getParameter("username");
			password = request.getParameter("password");

			if (username == null || password == null || username == ""
					|| password == "") {
				System.out.println("username or password is empty");
				doGet(request, response);
				return;
			}

			if (DBWrapper.containsUser(username)) {
				printMessagePage(out, "UserExisted");
			} else {
				User user = new User(username, password);
				DBWrapper.putUser(user);
				printMessagePage(out, "RegisterSucess");
			}
			return;
		}

		if (action.equals("addchannel")) {

			String channelname = request.getParameter("channelname");
			String xpaths = request.getParameter("xpaths");
			String xslurl = request.getParameter("xslurl");
			username = (String) request.getSession().getAttribute("username");

			System.out.println("Username in the session: " + username);
			System.out.println("channelname:  " + channelname);
			System.out.println("xpaths: " + xpaths);
			System.out.println("xslurln: " + xslurl);

			if (channelname == null || xpaths == null || xslurl == null
					|| channelname == "" || xpaths == "" || xslurl == "") {
				printMessagePage(out, "InformationMissing");
				return;
			}

			if (DBWrapper.containsChannel(channelname)) {
				printMessagePage(out, "ChannelExisted");
			} else {
				ArrayList<String> xpaths_array = new ArrayList<String>();
				for (String piece : xpaths.split(";")) {
					xpaths_array.add(piece);
				}
				Channel channel = new Channel(channelname, username,
						xpaths_array, xslurl);
				DBWrapper.putChannel(channel);
				
				//debug
				channel=DBWrapper.getChannel(channelname);
				if(channel==null)
					System.out.println("failed to put channel!");
				
				// update user
				User user = DBWrapper.getUser(username);
				if (user != null && channelname != null) {
					user.addChannel(channelname);
					DBWrapper.putUser(user);
				} else
					System.out.println("user or channel name lost!!!");
			

				printMessagePage(out, "AddChannelSucess");

			}
		}

	}

	// **********************************html
	// page***********************************//

	public void printLoginPage(PrintWriter out) {
		out.println("<html><body>");
		out.println("<form action=\"crawler\" method = \"POST\" >");
		out.println("<label>Username </label>");
		out.println("<input type=\"text\" id=\"username\" name=\"username\"><br>");
		out.println("<label>Password </label>");
		out.println("<input type=\"text\" id=\"password\" name=\"password\"><br>");
		out.println("<input type=\"submit\" name=\"action\"  value=\"login\"/></form>");

		out.println("<form action=\"crawler\" method=\"GET\">");
		out.println("<input type=\"submit\" name=\"action\"   value=\"register\"></form>");

		out.println("<form action=\"crawler\" method=\"GET\">");
		out.println("<input type=\"submit\" name=\"action\"   value=\"displaychannels\"></form>");

		out.println("<form action=\"crawler\" method=\"GET\">");
		out.println("<input type=\"submit\" name=\"action\"  value=\"logout\"></form>");
		out.println("</body></html>");
	}

	public void printRegisterPage(PrintWriter out) {
		out.println("<html><body>");
		out.println("<form action=\"crawler\" method = \"POST\" id=\"wrapper\">");
		out.println("Create New Account<br>");
		out.println("<label>Username</label>");
		out.println("<input type=\"text\" name=\"username\"><br>");
		out.println("<label>Password </label>");
		out.println("<input type=\"text\" name=\"password\"><br>");
		out.println("<input type=\"submit\" name=\"action\" value=\"register\"/>");
		out.println("<input type=\"hidden\"  name='page' value=\"2\"/></form>");
		out.println("</html></body>");
	}

	private void printDisplayChannelsPage(PrintWriter out,
			HttpServletRequest request) {
		// TODO Auto-generated method stub

		// get the information
		String username = (String) request.getSession()
				.getAttribute("username");// can not use tostring!!!!since maybe
											// null!!!
		User user = null;

		if (username == null)
			System.out.println("username==null");
		else
			System.out.println("logged username: " + username);

		if (username != null)
			user = DBWrapper.getUser(username);

		if (user == null)
			System.out.println("can not find the user");
		else {
			System.out.println("user information: ");
			System.out.println(user.getUsername() + user.getPassword());
		}

		ArrayList<Channel> channels = DBWrapper.getAllChannels();

		// display information
		out.println("<html><body>");

		if (channels == null || channels.size() == 0)
			out.println("No channnels found!!!<br>");
		else {
			out.println("All Channels: <table>");

			// not logged;
			if (username == null) {
				out.println("<tr>Channel Name && Owner name</tr>");
				for (Channel channel : channels) {
					out.println("<tr><td>" + channel.getChannelName() + "&&"
							+ channel.getUserName() + "</td></tr>");
				}
				out.println("Logged in to see channel contents and add/delete channels:  <a href=\"?action=login\">Log In</a></table>");
			}
			// logged
			else {

				out.println("<tr>Channel Name<tr>");
				for (Channel channel : channels) {
					String channelname = channel.getChannelName();
					String ownername = channel.getUserName();
					if (user.hasChannel(channelname)
							|| ownername.equals(username)) {
						out.println("<tr><td><a href=\"?action=displaychannelcontent&channelname="
								+ channelname
								+ "\">"
								+ channelname
								+ "</a></td>"
								+ "<td><a href=\"?action=delete&channelname="
								+ channelname + "\">delete</a></td></tr>");
					} else {
						out.println("<tr> <td>" + channelname + "</td></tr>");
					}
				}
			}
			out.println("</table><br><br><br>");
		}

		if (username != null)
			printAddChannelPage(out);
		out.println("</html></body>");

	}

	public void printAddChannelPage(PrintWriter out) {
		out.println("<br><br><br><br>");
		out.println("<form action=\"crawler\" method = \"POST\">");
		out.println("Add Channel<br>");

		out.println("<label>Channel Name</label>");
		out.println("<input type=\"text\" name=\"channelname\" id=\"channelname\"/><br>");
		out.println("<label>XPaths(Separate by ; )</label>");
		out.println("<input type=\"text\" name=\"xpaths\" id=\"xpaths\"/><br>");
		out.println("<label>XSL Stylesheet URL </label>");
		out.println("<input type=\"text\" name=\"xslurl\" id=\"xslurl\" value=\"\"/><br>");

		out.println("<input type=\"submit\" name=\"action\" value=\"addchannel\"/>");
		out.println("</form><a href=\"crawler\">Back</a>");
	}

	private void printDisplayChannelContentPage(PrintWriter out,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		String username = (String) request.getSession()
				.getAttribute("username");
		String channelname = request.getParameter("channelname");

		System.out.println("printDisplayChannelContentPage: username"
				+ username);
		System.out.println("printDisplayChannelContentPage: channelname"
				+ channelname);

		Channel channel = DBWrapper.getChannel(channelname);

		// display page
		out.println("<html><body>");
		// null
		if (username == null)
			out.println("Logged in to see channel contents and add/delete channels:  <a href=\"?action=login\">Log In</a>");
		else if (channel == null)
			out.println("No xmls for this channel has found.");// actually will
																// not be used.

		// Logged, show contents of the channel
		else {
			out.println("Channel name:   " + channelname + "<br><br>");
			
			out.println("Channel xpaths: ");
			ArrayList<String> xpaths=channel.getXpaths();
			for(String s:xpaths)
				out.println(s);
			
			out.println("<br>Content on the Channel:<br><br><br>");
			// show the xml;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
			String xsl_url = channel.getXslUrls();// ???

			out.println("<documentcollection>");
			ArrayList<String> urls = channel.getMatchedDoc();
			if (urls != null) {
				for (String url : urls) {
					FileContent content = DBWrapper.getFileContent(url);
					if (content == null)
						continue;// the content of this url may not be stored
					String raw_content = content.getRawContent();
					if (raw_content.contains("?>")) {
						// cut some useless content
						int startId = raw_content.indexOf("?>") + 2;
						raw_content = raw_content.substring(startId);
					}
					String lastCrawled = dateFormat.format(content
							.getLastCrawled())
							+ "T"
							+ timeFormat.format(content.getLastCrawled());

					// Print out document
					out.println("<document crawled=\"" + lastCrawled
							+ "\" location=\"" + content.getUrl() + "\">");
					out.println("<a href=\""+content.getUrl()+"\">"+content.getUrl()+"</a><br>");
					out.println("</document>");
				}
			}
			out.println("</documentcollection>");
		}

		// show add channel
		if (username != null)
			printAddChannelPage(out);
		out.println("</html></body>");
	}

	private void printDeleteChannelPage(PrintWriter out,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		String username = (String) request.getSession()
				.getAttribute("username");
		if (username == null)
			printMessagePage(out, "LoginNeeded");

		// user mismatch!
		String channelname = request.getParameter("channelname");
		System.out.println("channel name from browser:" + channelname);

		String owner = DBWrapper.getChannel(channelname).getUserName();
		if (!owner.equals(username))
			printMessagePage(out, "DeleteError");
		else {
			DBWrapper.deleteChannel(username, channelname);// including update
															// for user;
			printMessagePage(out, "DeleteChannelSuccess");
		}
	}

	private void printLogoutPage(PrintWriter out, HttpServletRequest request) {
		// TODO Auto-generated method stub
		request.getSession().setAttribute("username", null);
		printMessagePage(out, "Logout");
	}

	private void printMessagePage(PrintWriter out, String message) {
		// TODO Auto-generated method stub
		// String message="";
		out.println("<html><body>");
		while (true) {
			if (message.equals("LoginError")) {
				message = "Error: User not found or Username, password mismatch. <br><a href=\"crawler\">Back</a>";
				break;
			}
			if (message.equals("UserExisted")) {
				message = "Error: User already existed. <br><a href=\"crawler\">Back</a>";
				break;
			}
			if (message.equals("RegisterSucess")) {
				message = "Success: You have successfully registered. <br><a href=\"crawler\">Back</a>";
				break;
			}
			if (message.equals("InformationMissing")) {
				message = "Error: Channel/XPaths/XSL URL cannot be empty.<a href=\"crawler?action=displaychannels\">Back</a>";
				break;
			}
			if (message.equals("ChannelExisted")) {
				message = "Error: Channel already existed. <br><a href=\"crawler?action=displaychannels\">Back</a>";
				break;
			}
			if (message.equals("AddChannelSucess")) {
				message = "Success: You have successfully added a channel. <br><a href=\"crawler?action=displaychannels\">Back</a>";
				break;
			}
			if (message.equals("LoginNeeded")) {
				message = "Error: You have to login. <br><a href=\"crawler\">Back</a>";
				break;
			}
			if (message.equals("DeleteError")) {
				message = "Error: this channel do not belongs to you <br><a href=\"crawler?action=displaychannels\">Back</a>";
				break;
			}
			if (message.equals("DeleteChannelSuccess")) {
				message = "Success: You have successfully deleted a channel. <br><a href=\"crawler?action=displaychannels\">Back</a>";
				break;
			}
			if (message.equals("Logout")) {
				message = "Success: You have successfully Logged out. <br><a href=\"crawler\">Back</a>";
				break;
			}

		}

		out.println(message);
		out.println("</body></html>");
	}

}
