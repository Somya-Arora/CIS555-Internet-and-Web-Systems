package edu.upenn.cis455.webserver;

import java.net.Socket;
import java.util.LinkedList;
//import java.util.List;
//this class used to store the socket, including add and remove method, add and move should be synchronized to notify and wait the thread;
public class RequestQueue {
	public LinkedList<Socket> queue;
	private int max=500;
	
	public RequestQueue(String root) {
		// TODO Auto-generated constructor stub
		queue=new LinkedList<Socket>();
	}

	public synchronized boolean add(Socket socket) {
		// TODO Auto-generated method stub
		if(queue.size()<max)
		{
			queue.add(socket);
			//System.out.println("add -> socket queue size :"+ queue.size());
			notify();// after add a socket, we must notify the other waiting threads
			return true;
		}
		return false;	
	}
	
	public synchronized Socket remove() throws InterruptedException //get a socket from all the queue 
	{
		if(queue.size() == 0){
			wait(); //thread wait when there is no socket in the queue;
		}
		return queue.remove();
	}

}
