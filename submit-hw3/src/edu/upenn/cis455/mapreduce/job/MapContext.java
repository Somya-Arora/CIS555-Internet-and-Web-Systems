package edu.upenn.cis455.mapreduce.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import edu.upenn.cis455.mapreduce.Context;

public class MapContext implements Context
{
	int numWorkers;
	String storagedir, output;
//	int keys_read;
//	int keys_written;
	
	//used to store the range, the first param is worker id, second is the upper size of this param.
	HashMap<Integer, BigInteger> workerRanges =new HashMap<Integer,BigInteger>();
	HashMap<String, String> status=new HashMap<String, String>();
	
	public MapContext(){};
	
	//initialization function for mapping.
	public MapContext(int num,String storagedir, HashMap<String, String> status) throws IOException
	{
		this.numWorkers=num;
		this.storagedir = storagedir;
		this.status=status;
		createRanges(numWorkers);
		createSpoolDirs(storagedir);
	}
	
	//initialization function for reducing.
	public MapContext(String storagedir, String output, HashMap<String, String> status) {
		this.storagedir = storagedir;
		this.status=status;
		this.output = output;
		String outPath=storagedir+output;
		File file = new File(outPath);
		if(file.exists()) {
			System.out.println("Output Directory Already Exists, deleted it successfully");
			deleteDirectory(file);
			file.delete();
		}
		if(new File(outPath).mkdir()) {
			System.out.println("Output Directory Successfully Created");
		}
	}

	@Override
	public void write(String key, String value) {
		// TODO Auto-generated method stub
		if(key==null||value==null)
			return;
		if(key.equals("null")||value.equals("null"))
			return;
		String fileName="";
		//if the status is mapping, it means we are doing the mapping work, should write things to corresponding(hash) workeri.txt in sppol-out;
		if(status.get("status").equals("mapping"))
		{
			int index;
			try {
				index = hashndetermineWorker(key);
				fileName = storagedir+"/spool-out/worker"+index+".txt";
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else //if the status is not mapping, it means we are doing the reducing work, should write things to output;
		{
			fileName = storagedir+output+"/result.txt";
		}
		
		FileWriter filewriter;
		try {
			filewriter = new FileWriter(fileName,true);
			filewriter.write(key+"\t"+value+"\n");
			
			int count=Integer.parseInt(status.get("keys_written"));
			status.put("keys_written",Integer.toString(++count));
			
			filewriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}	

	//writer the request body to file mapping result;
	public void StoreDataInSpoolIn(String content) {
		String fileName = storagedir+"/spool-in/mapping_from_allworkers.txt";
		String fileName1=storagedir+"/spool-in/"+System.currentTimeMillis()+".txt";
		FileWriter filewriter;
		FileWriter filewriter1;
		try {
			filewriter = new FileWriter(fileName,true);
			filewriter.write(content+"\n");
			filewriter.close();	
			filewriter1 = new FileWriter(fileName1,true);
			filewriter1.write(content+"\n");
			filewriter1.close();	
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	
	//create the ranges according to the number of workers.
	private void createRanges(int n) {
		// TODO Auto-generated method stub
		String minStr = "0000000000000000000000000000000000000000";
		String maxStr = "ffffffffffffffffffffffffffffffffffffffff";
		BigInteger min = new BigInteger(minStr, 16);//BigInteger.ZERO;
		
		BigInteger max = new BigInteger(maxStr, 16);
		BigInteger interval = max.divide(new BigInteger(Integer.toString(n)));

		for(int i = 0; i<n; i++) {
			BigInteger upper=min.add(interval);
			workerRanges.put(i, upper);
			min = upper;
		}
	}

	//hash the key and determining worker's id;
	private int hashndetermineWorker(String key) throws NoSuchAlgorithmException {
		
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		md.update(key.getBytes());
		byte[] digest = md.digest();
		BigInteger bi = new BigInteger(1, digest);
		
		for(int i = 0; i<numWorkers; i++)			
		{
			BigInteger  upper = workerRanges.get(i);
			if((bi.compareTo(upper))<0)
				return i;
		}
		
		return -1;
	}
	
	//delete the file recursively.
	private void deleteDirectory(File directory)
	{
		for(File file: directory.listFiles()) {
			if(file.isDirectory())
				deleteDirectory(file);
			else 
				file.delete();
		}
	}
	
	private void createSpoolDirs(String storage) throws IOException {
		// TODO Auto-generated method stub
		String outPath=storage+"/spool-out";
		String inPath=storage+"/spool-in";
		
		//Create spool-out to store the result from mapping of each worker
		File spool_out = new File(outPath);
		if(spool_out.exists()) {
			System.out.println("Delete pool-out");
			deleteDirectory(spool_out);
			spool_out.delete();
		}
		if(new File(outPath).mkdir()) {
			System.out.println("Creating Spool Out Directory Successfully!");
		}
		//Create file for each worker.
		for(int i = 0; i<numWorkers; i++) {
			File file = new File(outPath+"/worker"+i+".txt");
			if(file.createNewFile())
				System.out.println(file + "created");

		}
		
		//Create spool-in. to receive data from all the workers.
		File spool_in = new File(inPath);
		if(spool_in.exists()) {
			System.out.println("Delete pool-in");
			deleteDirectory(spool_in);
			spool_in.delete();
		}
		if(new File(inPath).mkdir()) {
			System.out.println("Creating Spool In Directory Successfully!");
		}
		//create file to store the reault of mapping.
		File file = new File(inPath+"/mapping_from_allworkers.txt");
		if(file.createNewFile())
			System.out.println(file+"created");
	}
}
