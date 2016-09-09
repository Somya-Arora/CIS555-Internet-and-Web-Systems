package test.edu.upenn.cis455.hw1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.TestCase;

public class ServletTest extends TestCase{
	public void testA() throws IOException {
      Socket s=null;
		try{
			s=new Socket("localhost",8080);
			System.out.println("Connecting...");
			}catch(UnknownHostException e)
			{
				System.out.println("Connection Failed!");
			}
			
			//create and send the get request
			if(s!=null)
			{
				PrintWriter newrequest=new PrintWriter(s.getOutputStream());
				String head="GET "+"session1"+" HTTP/1.1\r\n"
						+"Host: localhost:8080"+"\r\n"
						+"Connection: close\r\n\r\n";
				newrequest.write(head);
				newrequest.flush();
			
				InputStream newresponse = s.getInputStream();

				BufferedReader res_buf=new BufferedReader(new InputStreamReader(newresponse));
				
				String body=null;

				if(res_buf.readLine().contains("200"))
				{				
					System.out.println("response success!");
					String helper=res_buf.readLine();
					while(helper!=null&&!helper.equals(""))
					{
						//System.out.println(helper);
						helper=res_buf.readLine();
					}
					String current=res_buf.readLine();
					while(current!=null)
					{
						body+=current;
						current=res_buf.readLine();
					}
				}
				
				System.out.println("body: "+body);
   		 boolean success = true;
         assertEquals(false, success);
	  }			
}
}
