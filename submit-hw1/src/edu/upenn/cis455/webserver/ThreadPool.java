package edu.upenn.cis455.webserver;

import java.util.ArrayList;

//this class 
public class ThreadPool {
	
	public static ArrayList<WorkerThread> worker_list;
	private int number;

	public ThreadPool(int num, RequestQueue request_queue) {
		worker_list=new ArrayList<WorkerThread>();
		number=num;
		System.out.println("thread pool built");
		for(int i=0;i<number;i++)
		{
			WorkerThread wor_thread=new WorkerThread(request_queue);//the worker will wait if there is no socket in the queue.
			worker_list.add(wor_thread);
			wor_thread.start();
		}
	}
	
	public synchronized void stop()
	{
		System.out.println("stop all thread!");
		if(worker_list != null)
		{
			for(WorkerThread wt : worker_list)
			    {
				wt.running=false;
				wt.interrupt();
			    }
		}
	}
}
