/**
 * 
 */
package ai.fruitrage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class homework {
	
	private static String inputFileName = "input.txt";
	private static String outputFileName = "output.txt";
	
	private static long timeStart, timeCurrent;
	private static long secondsRemaining;

	/** If true, prints node information to the console. */
	private static final boolean DEBUG_MODE = true;
	
	private static byte[][] readInput(Scanner sc)
	{
		// TODO Start reading input
		FruitRageNode.n = Integer.parseInt(sc.nextLine());
		FruitRageNode.p = Integer.parseInt(sc.nextLine());	
		secondsRemaining = (long)(Float.parseFloat(sc.nextLine())*1000);

		// Read the grid
		byte[][] gridInitial = new byte[FruitRageNode.n][FruitRageNode.n];
		for(int i = FruitRageNode.n - 1; i >=0 ; i--)
		{
			String row = sc.nextLine();
			for(int j = 0; j < FruitRageNode.n; j++)
			{
				char ch = row.charAt(j);

				if(ch == '*')
					gridInitial[i][j] = FruitRageNode.EMPTY;
				else
					gridInitial[i][j] = (byte)(ch - '0');
			}
		}
		
		return gridInitial;
	}
	
	public static void main(String[] args)
	{
		Scanner sc;

		timeStart = System.nanoTime();

		// Read contents of input file
		try
		{
			// if input file specified
			if(args.length > 0) {
				inputFileName = args[0];
				
				if(inputFileName.toLowerCase().contains("input"))
				{
					// Use the same pattern for output file now
					outputFileName = inputFileName.toLowerCase().replace("input", "output");
				}
			}
			
			sc = new Scanner(new File(inputFileName));
			
			byte[][] gridInitial = readInput(sc);
			
			// Create starting node
			FruitRageNode initNode = new FruitRageNode(gridInitial);
			
			System.out.println(initNode);
			
			FruitRageNode.gravity(gridInitial);
			
			System.out.println(initNode);
			
			
			sc.close();

		} catch (FileNotFoundException e) { }

		timeCurrent = System.nanoTime();

		System.out.println("\nCompleted in " +
				(TimeUnit.MILLISECONDS.convert(timeCurrent - timeStart, TimeUnit.NANOSECONDS) / 1000.0) + " seconds.");
	}

}


class FruitRageNode {
	
	/** Width and height of the square board (0 < n <= 26) */
	public static int n;
	
	/** Number of fruit types (0 < p <= 9) */
	public static int p;
	
	/** Stores all the fruit positions */
	private byte[][] grid;
	
	/** The value used for empty spaces on the grid */
	public static final byte EMPTY = -1;
	
	/** How the empty spaces are displayed */
	public static final char EMPTY_CHAR = '*';
	
	public FruitRageNode()
	{
		for(int i = 0; i < n; i++)
			for(int j = 0; j < n; j++)
				grid[i][j] = EMPTY;
	}
	
	public FruitRageNode(byte[][] gridParam)
	{
		grid = gridParam;
	}

	/**
	 * Alters a grid in such a way that all empty spaces rise to the top.
	 * 
	 * @param gridToChange
	 */
	public static void gravity(byte[][] gridToChange)
	{
		
		// System.out.println("Applying gravity...");
		
		// Go column-wise
		for(int j = 0; j < n; j++)
		{
			/* Go downwards the column fixing the top element
			 * if EMPTY is found anywhere, swap it with the top element */
			for(int i = n-1; i > 0; i--)
			{
				// No need to swap if the top element already contains EMPTY
				if(gridToChange[i][j] == EMPTY)
					continue;
				
				for(int k = i-1; k >= 0; k--)
				{
					if(gridToChange[k][j] == EMPTY)
					{
						// swap
						byte temp = gridToChange[k][j];
						gridToChange[k][j] = gridToChange[i][j];
						gridToChange[i][j] = temp;
						
						break;
					}
				}
			}
		}
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		// sb.append("The grid looks like: \n");
		sb.append(gridString(grid));

		return sb.toString();
	}
	
	public static String gridString(byte[][] gridToPrint)
	{
		StringBuilder sb = new StringBuilder();
		
		for(int j = 0; j < 2*n+1; j++)
			sb.append("-");
		sb.append("\n");
		
		for(int i = n-1; i >= 0; i--)
		{

			for(int j = 0; j < n; j++)
			{
				if(j == 0)
					sb.append("|");
				if(gridToPrint[i][j] == EMPTY)
					sb.append(EMPTY_CHAR);
				else
					sb.append(gridToPrint[i][j]);
				sb.append("|");
			}

			sb.append("\n");
		}
		
		for(int j = 0; j < 2*n+1; j++)
			sb.append("-");
		
		return sb.toString();
	}
}