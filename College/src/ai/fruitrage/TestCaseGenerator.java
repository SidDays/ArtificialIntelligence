package ai.fruitrage;

import java.io.PrintWriter;
import java.util.Random;

public class TestCaseGenerator {

	/** Width and height of the square board (0 < n <= 26) */
	private static final int N_DEFAULT = 20;

	/** Number of fruit types (0 < p <= 9) */
	private static final int P_DEFAULT = 4;
	
	private static final float TIME_LIMIT_DEFAULT = 300f;
	
	private static final String FILENAME_SUFFIX_DEFAULT = "";
	
	private static final double HOLE_PROBABILITY = 0.01;
	
	private static final Random RAND = new Random();
	
	/**
	 * Alters the current grid in such a way that 
	 * all empty spaces rise to the top.
	 */
	private static void gravitate(byte[][] grid, int n)
	{

		// Go column-wise
		for (int j = 0; j < n; j++)
		{

			for (int i = (n-1)-1; i >= 0; i--)
			{
				// No need to swap if the top element already contains EMPTY
				if (grid[i][j] == FruitRageNode.EMPTY)
				{
					for(int k = i; k < n-1; k++)
					{
						grid[k][j] = grid[k+1][j];
					}
					grid[n-1][j] = FruitRageNode.EMPTY;
				}
			}
		}
	}

	public static void createTestCase()
	{
		createTestCase(N_DEFAULT, P_DEFAULT, TIME_LIMIT_DEFAULT, FILENAME_SUFFIX_DEFAULT);
	}
	
	public static float getRandomTime(float timeLimit)
	{
		return ((int)(RAND.nextFloat()*timeLimit*1000))/1000f;
	}
	
	public static void createTestCase(int n, int p, String fileNameSuffix)
	{
		createTestCase(n, p, getRandomTime(TIME_LIMIT_DEFAULT), fileNameSuffix);
	}
	
	public static void createTestCase(int n, int p, float time, String fileNameSuffix)
	{

		// Randomize the grid
		byte[][] gridNew = new byte[n][n];
		for(int i = 0; i < n; i++)
		{
			for(int j = 0; j < n; j++)
			{
				if(RAND.nextDouble() < HOLE_PROBABILITY)
				{
					gridNew[i][j] = FruitRageNode.EMPTY;
				}
				else
				{
					gridNew[i][j] = (byte)RAND.nextInt(p);
				}
			}
		}
		
		// Apply gravity
		gravitate(gridNew, n);

		PrintWriter writerInput = null;

		try {

			String fileName = homework.inputFileName.substring(0, homework.inputFileName.length()-4) + fileNameSuffix + ".txt";
			writerInput = new PrintWriter(fileName, "UTF-8");

			System.out.println("\nGenerating input to file "+fileName+":");

			System.out.println(n);
			writerInput.println(n);

			System.out.println(p);
			writerInput.println(p);

			// Print remaining time
			System.out.format("%.3f\n", time);
			writerInput.format("%.3f", time);
			writerInput.println();

			// Print this grid to the file and console
			for(int i = n-1; i >= 0; i--)
			{
				for(int j = 0; j < n; j++)
				{
					if(gridNew[i][j] == FruitRageNode.EMPTY)
					{
						System.out.print(FruitRageNode.EMPTY_CHAR);
						writerInput.print(FruitRageNode.EMPTY_CHAR);
					}
					else {
						System.out.print(gridNew[i][j]);
						writerInput.print(gridNew[i][j]);
					}
				}
				System.out.println();
				writerInput.println();
			}

			writerInput.close();

		}
		catch(Exception e) { e.printStackTrace(); }
	}

	public static void main(String[] args) {
		
		for(int n = 5; n <= 5; n++)
		{
			for(int p = 6; p <= 6; p++)
			{
				float time = getRandomTime(TIME_LIMIT_DEFAULT);
				String suffix = " "+n+" "+p+" "+time;
				createTestCase(n, p, time, suffix);
			}
		}
		
	}

}
