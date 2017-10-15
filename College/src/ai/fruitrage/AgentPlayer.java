
package ai.fruitrage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 */
public class AgentPlayer {

	private static final boolean REQUIRE_KEY_PRESS = true;

	/** System.nanoTime() values. */
	private static long timeCurrent;

	private static long durNanosecondsLastMove;

	/** Convert above floating value into precise-r nanoseconds */
	private static long durNanosecondsAllotted;

	/** Width and height of the square board (0 < n <= 26) */
	private static int n;

	/** Number of fruit types (0 < p <= 9) */
	private static int p;

	/** The copy of the grid that the referee keeps to check score changes */
	private static byte[][] gridReferee;

	/** Is it player 1 or player 2 (0 or 1 in implementation) */
	private static int turn;

	/**
	 * Player scores. Determines who wins the game.
	 */
	private static int scores[] = {0, 0};
	private static float times[] = {0, 0};

	/**
	 * Initializes the values of n, p, duration alloted and the starting grid.
	 */
	private static void readInputInitialize(Scanner inInput) {

		n = Integer.parseInt(inInput.nextLine());
		p = Integer.parseInt(inInput.nextLine());
		float durSecondsAllotted = Float.parseFloat(inInput.nextLine());
		durNanosecondsAllotted = homework.secondsToNanoseconds(durSecondsAllotted);
		
		// System.out.format("%d, %d, %f\n", n, p, durSecondsAllotted);

		// Read the grid
		gridReferee = new byte[n][n];
		for(int i = n - 1; i >=0 ; i--)
		{
			String row = inInput.nextLine();
			for(int j = 0; j < n; j++)
			{
				char ch = row.charAt(j);

				if(ch == '*')
					gridReferee[i][j] = FruitRageNode.EMPTY;
				else
					gridReferee[i][j] = (byte)(ch - '0');
			}
		}
	}

	/**
	 * Values of n, p, and gridReferee must be initialized first.
	 * 
	 * @return true if the game is over (i.e., output is blank).
	 */
	private static boolean playUntilGameOver()
	{	
		play();
		
		PrintWriter writerInput = null;
		try {

			// condition for an empty board
			if(numberOfEmptySquares(gridReferee) == n*n)
			{
				System.out.println("\nTHE GAME IS ALREADY OVER!");
				System.out.format("\nScoreboard: %d (%.3f) | %d (%.3f)\n\n",
						scores[0], times[0],
						scores[1], times[1]);

				return false;
			} 

			else 
			{
				writerInput = new PrintWriter(homework.inputFileName, "UTF-8");

				System.out.println("\nNew input printed to file:");

				System.out.println(n);
				writerInput.println(n);

				System.out.println(p);
				writerInput.println(p);
				
				long timeCurrentOld = timeCurrent;
				timeCurrent = System.nanoTime();
				durNanosecondsLastMove = timeCurrent - timeCurrentOld;
				// The total time taken by turn player so far
				times[turn] += homework.nanosecondsToSeconds(durNanosecondsLastMove);

				// Print remaining time
				System.out.format("%.3f\n", homework.nanosecondsToSeconds(durNanosecondsAllotted) - times[turn]);
				writerInput.format("%.3f", homework.nanosecondsToSeconds(durNanosecondsAllotted) - times[turn]);
				writerInput.println();

				// read the grid from output file
				Scanner inOutput = new Scanner(new File(homework.outputFileName));

				// Consume the line containing the winning move
				inOutput.nextLine();

				// The next n lines contain the grid
				byte[][] gridNew = new byte[n][n];
				for(int i = n - 1; i >=0 ; i--)
				{
					String row = inOutput.nextLine();
					for(int j = 0; j < n; j++)
					{
						char ch = row.charAt(j);

						if(ch == '*')
							gridNew[i][j] = FruitRageNode.EMPTY;
						else
							gridNew[i][j] = (byte)(ch - '0');
					}
				}

				// Print this grid to the file and console
				for(int i = 0; i < n; i++)
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

				// Update the score for player
				scores[turn] += (int)(Math.pow(numberOfEmptySquares(gridNew)-numberOfEmptySquares(gridReferee), 2));

				// Make this the new referee grid
				gridReferee = gridNew;

				inOutput.close();
			}


		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(writerInput != null)
				writerInput.close();
		}

		return true;
	}

	/**
	 * Counts the number of squares with value EMPTY in an nxn byte grid.<br>
	 * <br>
	 * Helps compute the score difference.
	 */
	private static int numberOfEmptySquares(byte[][] grid)
	{
		int count = 0;
		for(int i = 0; i < n; i++)
		{
			for(int j = 0; j < n; j++)
			{
				if(grid[i][j] == FruitRageNode.EMPTY)
				{
					count++;
				}
			}
		}

		return count;
	}

	/**
	 * Your selected move, represented as two characters: A letter from A to Z
	 * representing the column number (where A is the leftmost column, B is the
	 * next one to the right, etc), and A number from 1 to 26 representing the
	 * row number (where 1 is the top row, 2 is the row below it, etc).
	 */
	
	/*private static FruitGridPoint moveStringToPoint(String move)
	{
		char column = move.charAt(0);
		int y = column - 'A';

		String row = ""+move.charAt(1);
		int x = FruitRageNode.n - Integer.parseInt(row);	

		// By default, let it be empty
		int val = -1;

		return new FruitGridPoint(x, y, val);
	}*/
	
	public static void play()
	{
		System.out.println("\n----------[ Player "+turn+" ]----------\n");
		
		// Call a different program if required
		if(turn == 0) {
			homework.main(new String[] {});
		}
		else {
			homework.main(new String[] {});
		}
		
		// Switch turn
		turn = (turn + 1)%2;
	}

	/**
	 * The main method.
	 */
	public static void main(String args[])
	{
		try
		{
			Scanner in = new Scanner(System.in);
			Scanner inInput = new Scanner(new File(homework.inputFileName));

			// Randomize first player
			if(Math.random() > 0.5)
				turn = 1;
			else
				turn = 0;
			System.out.format("Player %d goes first.\n", (turn));

			// update n, p, durNanosecondsAllotted, gridReferee
			readInputInitialize(inInput);
			
			timeCurrent = System.nanoTime();
			

			// Update scores
			while(playUntilGameOver())
			{
				
				System.out.format("\nScoreboard: %d (%.3f) | %d (%.3f)\n",
						scores[0], times[0],
						scores[1], times[1]);

				if(REQUIRE_KEY_PRESS)
				{
					System.out.print("\nPress Enter to continue: ");
					in.nextLine();
					System.out.println();
				}

				
			}		

			in.close();
		}
		catch(FileNotFoundException fne)
		{
			fne.printStackTrace();
		}
	}

}

