package util;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
public class RunSystemCommand {

	public static int executeCommandLine(final String commandLine,
			final boolean printOutput,
			final boolean printError,
			final long timeout)
	throws IOException, InterruptedException, TimeoutException
	{
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(commandLine);
		/* Set up process I/O. */
		Worker worker = new Worker(process);
		worker.start();
		try {
			worker.join(timeout);
			if (worker.exit != null)
				return worker.exit;
			else
				throw new TimeoutException();
		} catch(InterruptedException ex) {
			worker.interrupt();
			Thread.currentThread().interrupt();
			throw ex;
		} finally {
			process.destroy();
		}
	}

	private static class Worker extends Thread {
		private final Process process;
		private Integer exit;
		private Worker(Process process) {
			this.process = process;
		}
		public void run() {
			try { 
				exit = process.waitFor();
			} catch (InterruptedException ignore) {
				return;
			}
		}  
	}


	static private int runCommand(String cmd) 
	throws IOException
	{  
		System.out.println("command: " + cmd);
		// Create a list for storing  output.
		ArrayList<String> list = new ArrayList<String>(); 

		// Execute a command and get its process handle
		Process proc = Runtime.getRuntime().exec(cmd); 

		// Get the handle for the processes InputStream
		InputStream istr = proc.getInputStream(); 

		// Create a BufferedReader and specify it reads 
		// from an input stream.
		BufferedReader br = new BufferedReader(
				new InputStreamReader(istr));
		String str; // Temporary String variable

		// Read to Temp Variable, Check for null then 
		// add to (ArrayList)list
		while ((str = br.readLine()) != null) list.add(str);

		// Wait for process to terminate and catch any Exceptions.
		try { proc.waitFor(); } 
		catch (InterruptedException e) {
			System.out.println("Process was interrupted"); }

		// Note: proc.exitValue() returns the exit value. 
		// (Use if required)
		//System.out.println("Exit: " + proc.exitValue());
		br.close(); // Done.

		// Convert the list to a string and return
		String[] outlist = (String[])list.toArray(new String[0]); 
		// Print the output to screen character by character.
		// Safe and not very inefficient.
		if (outlist.length>0) System.out.println("command output: " );
		for (int i = 0; i < outlist.length; i++){
			System.out.println(outlist[i]);
		}
		return proc.exitValue();
	}

	// Actual execution starts here
	static public int runSystemCommand(String string)
	{
		int status = -1; 
		try
		{
			// Run and get the output.
			status = executeCommandLine(string, true, true, 30000);

		}
		catch (IOException e) { 
			System.err.println(e); 
			} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("process interrupted");
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("timeout");
		}
		return status;
	}
}
