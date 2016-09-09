import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class DemoServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute("testAttribute", "1234");
		//request.removeAttribute("test");
		String test=(String) request.getAttribute("testAttribute");
		response.setContentType("text/html");
		//response.addHeader("Date", "3/31/2016");
		PrintWriter out = response.getWriter();
		if(out!=null)System.out.println("can get writer!!");
		
		out.println("<HTML><HEAD><TITLE>Simple Servlet</TITLE></HEAD><BODY>");
		out.println("<P>Hello!</P>");
		out.println("method: "+request.getMethod()+" head: "+ request.getHeader("version"));
		out.println("Request Content-Type: " + request.getContentType() + "Response Content-Type: " + response.getContentType());
		out.println("Request pathInfo: " + request.getPathInfo());
		out.println("Request queryString: " + request.getQueryString());
		out.println("Request Parameter: testParameter=" + request.getParameter("testParameter"));
		out.println("Request Test Attribute: "+test);
		
		//test the post method
		out.println("<P>Input the username and password<P>");
		out.println("<form method=\"post\">");
		out.println("UserName: <input type=\"text\" name=\"username\"><br>");
		out.println("PassWord: <input type=\"text\" name=\"password\"><br>");
		out.println("<input type=\"submit\" value=\"Submit\">");
		out.println("</form>");
		out.println("</BODY></HTML>");	
		
		out.flush();
		out.close();
		
		//test add header after write
	}
	
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.getParameter("username");
		PrintWriter out = response.getWriter();
		if(out!=null)System.out.println("can get writer!!");
		out.println("<HTML><HEAD><TITLE>Simple Servlet</TITLE></HEAD><BODY>");
		out.println("<P>Hello!</P>");
		out.println("method: "+request.getMethod()+" head: "+ request.getHeader("version"));
		out.println("Request Content-Type: " + request.getContentType() + "Response Content-Type: " + response.getContentType());
		out.println("Request pathInfo: " + request.getPathInfo());
		out.println("Request queryString: " + request.getQueryString());
		out.println("Request Parameter: username=" + request.getParameter("username"));
		out.println("</BODY></HTML>");		
		
		out.flush();
		out.close();
	}
}
