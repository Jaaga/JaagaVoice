package util;

import java.io.*;
import java.util.ArrayList;
public class RunSystemCommand {

	/**
	 * @param args
	 */
	static private int runCommand(String cmd) 
	throws IOException
	{  
		//System.out.println("command: " + cmd);
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
		/*try { proc.waitFor(); } 
		catch (InterruptedException e) {
			System.out.println("Process was interrupted"); }*/

		// Note: proc.exitValue() returns the exit value. 
		// (Use if required)
		//System.out.println("Exit: " + proc.exitValue());
		br.close(); // Done.

	/*	// Convert the list to a string and return
		String[] outlist = (String[])list.toArray(new String[0]); 
		// Print the output to screen character by character.
		// Safe and not very inefficient.
		if (outlist.length>0) System.out.println("command output: " );
		for (int i = 0; i < outlist.length; i++){
			System.out.println(outlist[i]);
		}
		return proc.exitValue();*/
		return 0;
	}

	// Actual execution starts here
	static public int runSystemCommand(String string)
	{
		int status = -1; 
		try
		{
			// Run and get the output.
			status = runCommand(string); 

		}
		catch (IOException e) { System.err.println(e); }
		return status;
	}
}
