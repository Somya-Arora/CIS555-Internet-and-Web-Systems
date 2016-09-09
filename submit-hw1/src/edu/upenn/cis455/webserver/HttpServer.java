package edu.upenn.cis455.webserver;
import java.io.*;
import java.net.*;
import java.util.HashMap;

class HttpServer {
  public static void main(String args[]) throws Exception
  {
	  if(args.length!=3)
		  System.out.println("Name: Menglu Wang \nSEAS Login: mengluw");
	  else
	  {
		  int port=Integer.parseInt(args[0]) ;
		  String root_directory=args[1];
		  String xml_url=args[2];//may be null//the initialization file for servlet
		  //why can not find the manager class???
		  Manager manager=new Manager(port,root_directory,xml_url);
		  manager.initServlet();
		  manager.ManagerListening();
	  }
  }
}
  



