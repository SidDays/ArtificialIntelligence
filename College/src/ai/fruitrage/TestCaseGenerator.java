package ai.fruitrage;

import java.io.PrintWriter;
import java.util.Random;

public class TestCaseGenerator {

	/** Width and height of the square board (0 < n <= 26) */
	private static final int N = 24;

	/** Number of fruit types (0 < p <= 9) */
	private static final int P = 3;
	
	private static final double HOLE_PROBABILITY = 0.1;
	
	/**
	 * Alters the current grid in such a way that 
	 * all empty spaces rise to the top.
	 */
	private static void gravitate(byte[][] grid)
	{

		// Go column-wise
		for(int j = 0; j < N; j++)
		{
			/* Go downwards the column fixing the top element
			 * if EMPTY is found anywhere, swap it with the top element */
			for(int i = N-1; i > 0; i--)
			{
				// No need to swap if the top element already contains EMPTY
				if(grid[i][j] == FruitRageNode.EMPTY)
					continue;

				for(int k = i-1; k >= 0; k--)
				{
					if(grid[k][j] == FruitRageNode.EMPTY)
					{
						// swap
						byte temp = grid[k][j];
						grid[k][j] = grid[i][j];
						grid[i][j] = temp;

						break;
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		
		Random rand = new Random();

		// Randomize the time
		float time = ((int)(rand.nextFloat()*300*1000))/1000f;

		// Randomize the grid
		byte[][] gridNew = new byte[N][N];
		for(int i = 0; i < N; i++)
		{
			for(int j = 0; j < N; j++)
			{
				if(rand.nextDouble() < HOLE_PROBABILITY)
				{
					gridNew[i][j] = FruitRageNode.EMPTY;
				}
				else
				{
					gridNew[i][j] = (byte)rand.nextInt(P);
				}
			}
		}
		
		// Apply gravity
		gravitate(gridNew);

		PrintWriter writerInput = null;

		try {

			writerInput = new PrintWriter(homework.inputFileName, "UTF-8");

			System.out.println("\nGenerated input to file:");

			System.out.println(N);
			writerInput.println(N);

			System.out.println(P);
			writerInput.println(P);

			// Print remaining time
			System.out.format("%.3f\n", time);
			writerInput.format("%.3f", time);
			writerInput.println();

			// Print this grid to the file and console
			for(int i = N-1; i >= 0; i--)
			{
				for(int j = 0; j < N; j++)
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

}
