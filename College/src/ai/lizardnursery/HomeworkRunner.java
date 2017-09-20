package ai.lizardnursery;

import java.io.File;

public class HomeworkRunner {

	public static void main(String[] args) {
		
		File dir = new File(".");
		File[] filesList = dir.listFiles();
		
		for (File file : filesList) 
		{
		    if (file.isFile()) 
		    {
		
		    	//System.out.println(file.getName());
		    	
		    	// Check if it is an input filename
		    	if(file.getName().toLowerCase().endsWith(".txt") && 
		    			file.getName().toLowerCase().startsWith("input"))
		    	{
		    		// Run the homework program
		    		System.out.println("Executing for input file: "+file.getName());
		    		homework.main(new String[] { file.getName() });
		    	}
		    	System.out.println();
		    }
		}

	}

}
