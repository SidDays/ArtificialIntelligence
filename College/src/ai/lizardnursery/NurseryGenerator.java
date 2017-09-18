package ai.lizardnursery;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates test case configurations for the lizard nursery problem.
 */
public class NurseryGenerator {

	/** Name of generated file */
	private static final String FILE_TESTINPUT = "input.txt";
	
	/** If randomly picking n */
	// private static final int N_LIMIT = 15;
	
	/** If randomly picking an algorithm */
	// private static final String ALGOS[] = {"DFS", "BFS", "SA"};
	
	/** Keep true if you want to use the values defined below. */
	private static boolean useSetValues = true;
	
	/** If algorithm is defined */
	private static String algo = "SA";
	
	/** Side of nursery grid */
	private static int n = 50;
	
	/** Number of lizards. Useless if setPToNOverride is true. */
	private static int p = 50;
	private static boolean setPToNOverride = true;
	
	/** Number of trees */
	private static int t = 0;

	public static void main(String[] args) {

		int[][] nursery = null;


		if(useSetValues) {
			System.out.println("Using preset values.");
			
			// Generate a random test case using fixed values
			nursery = new int[n][n];
			List<NurseryGridPoint> freeSpaces = new ArrayList<>();
			
			for(int i = 0; i < n; i++)
			{
				for(int j = 0; j < n; j++)
				{
					nursery[i][j] = 0;
					freeSpaces.add(new NurseryGridPoint(i, j));
				}

			}
			
			// Generate a random permutation of trees
			Random rand = new Random();
			if(t >= n*n)
			{
				System.err.print("Too many trees. Generating a random number between 0 and (n*n)-1.");
				t = rand.nextInt(n*n);
			}
			for(int i = 0; i < t; i++)
			{
				NurseryGridPoint treePoint = freeSpaces.remove(rand.nextInt(freeSpaces.size()));
				nursery[treePoint.x][treePoint.y] = 2;
			}

		}
		else {
			// TODO
			System.out.println("Randomly generating values.");
		}
		
		if(setPToNOverride)
			p = n;

		// Print solution to console as well as file
		PrintWriter writer = null;
		try {

			writer = new PrintWriter(FILE_TESTINPUT, "UTF-8");
			
			System.out.println("\nGenerating "+FILE_TESTINPUT+".");
			
			System.out.println(algo);
			writer.println(algo);
			
			System.out.println(n);
			writer.println(n);
			
			System.out.println(p);
			writer.println(p);
			
			// Print the nursery
			for(int i = 0; i < n; i++)
			{
				for(int j = 0; j < n; j++)
				{
					System.out.print(nursery[i][j]);
					writer.print(nursery[i][j]);
				}
				System.out.println();
				writer.println();
			}
			

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				writer.close();
		}
	}
}
