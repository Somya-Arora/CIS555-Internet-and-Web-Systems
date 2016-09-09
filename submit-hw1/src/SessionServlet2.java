import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class SessionServlet2 extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		System.out.println("run at sessionservlet 2 ln9");
		String val1 = (String) session.getAttribute("TestAttribute1");
		String val2 = (String) session.getAttribute("TestAttribute2");
		//String attributes=(String) session.getAttributes();
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<HTML><HEAD><TITLE>Session Servlet 1</TITLE></HEAD><BODY>");
		out.println("<P>TestAttribute1 value is '" + val1 + "'.</P>");
		out.println("<P>TestAttribute2 value is '" + val2 + "'.</P>");
		session.invalidate();
		out.println("<P>Session invalidated.</P>");
		out.println("<P>Continue to <A HREF=\"session3\">Session Servlet 3</A>.</P>");
		out.println("</BODY></HTML>");		
	}
}
