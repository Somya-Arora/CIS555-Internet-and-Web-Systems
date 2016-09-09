import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class SessionServlet1 extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		System.out.println("run at servlet ln9: session create: "+session.getId());
		session.setAttribute("TestAttribute1", "12345");
		session.setAttribute("TestAttribute2", "678910");
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println("<HTML><HEAD><TITLE>Session Servlet 1</TITLE></HEAD><BODY>");
		out.println("<P>TestAttribute set to 12345.</P>");
		out.println("<P>Continue to <A HREF=\"session2\">Session Servlet 2</A>.</P>");
		out.println("</BODY></HTML>");		
	}
}
