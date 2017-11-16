package ai.inference;

import java.io.File;

/**
 * Runs the homework file for all input filenames that match a pattern, and
 * generates the correspondingly named output file. You <b>must</b> add code to
 * allow the program to accept input filename in String[] args.
 * 
 * @author Siddhesh Karekar
 */
public class HomeworkRunner {

	public static void main(String[] args) {
		
		File dir = new File(".");
		File[] filesList = dir.listFiles();
		
		for (File file : filesList) 
		{
		    if (file.isFile()) 
		    {
		    	// Check if it is an input filename
		    	if(file.getName().toLowerCase().endsWith(".txt") && 
		    			file.getName().toLowerCase().startsWith("input"))
		    	{
		    		// Run the homework program
		    		System.out.println("Running the game for input file "+file.getName()+".");
		    		homework.main(new String[] { file.getName() });
		    	}
		    	System.out.println();
		    }
		}

	}

}
