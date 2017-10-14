
package ai.fruitrage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 */
public class AgentPlayer {
	
	private static final boolean REQUIRE_KEY_PRESS = false;

	private static int turn = 0;
	
	/**
	 * Player scores. Determines who wins the game.
	 */
	private static int scores[] = {0, 0};
	private static float times[] = {0, 0};
	
	/**
	 * 
	 * @return true if the game is over (i.e., output is blank).
	 */
	public static boolean outputToInput()
	{
		// values of n, p are constant. the grid must be changed

		PrintWriter writerInput = null;
		try {

			// Read the score of the TODO
			Scanner inResult = new Scanner(new File(homework.resultFileName));
			
			// Check the result
			String resultLine = inResult.nextLine();

			// TODO add condition for an empty board here
			if(resultLine.isEmpty())
			{
				// Game is over
				System.out.println("\nGAME OVER!");
				System.out.format("\nScoreboard: %d (%.3f) | %d (%.3f)\n\n",
						scores[0], times[0],
						scores[1], times[1]);

				inResult.close();
				
				return false;
			} 
			else 
			{
				writerInput = new PrintWriter(homework.inputFileName, "UTF-8");

				System.out.println("New input printed to file:");
				
				System.out.println(FruitRageNode.n);
				writerInput.println(FruitRageNode.n);
				
				System.out.println(FruitRageNode.p);
				writerInput.println(FruitRageNode.p);

				// read the grid from output file
				Scanner inOutput = new Scanner(new File(homework.outputFileName));
				
				// Print remaining time
				System.out.format("%.3f\n", homework.secondsAllotted);
				writerInput.format("%.3f\n", homework.secondsAllotted);
				
				// Consume the line containing the winning move
				inOutput.nextLine();
				
				// The next n lines contain the grid - simply copy it over
				for(int i = 0; i < FruitRageNode.n; i++)
				{
					String nextLine = inOutput.nextLine();
					System.out.println(nextLine);
					writerInput.println(nextLine);
				}

				// Update the score for player 1 and total time taken by them
				String[] parts = resultLine.split(",");
				
				scores[turn] += Integer.parseInt(parts[0]);
				
				// TODO this is remaining time
				times[turn] += Float.parseFloat(parts[3]);
				
				inOutput.close();
			}
			
			inResult.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(writerInput != null)
				writerInput.close();
		}
		
		return true;
	}
	
	/**
	 * Your selected move, represented as two characters: A letter from A to Z
	 * representing the column number (where A is the leftmost column, B is the
	 * next one to the right, etc), and A number from 1 to 26 representing the
	 * row number (where 1 is the top row, 2 is the row below it, etc).
	 */
	public static FruitGridPoint moveStringToPoint(String move)
	{
		char column = move.charAt(0);
		int y = column - 'A';
		
		String row = ""+move.charAt(1);
		int x = FruitRageNode.n - Integer.parseInt(row);	
		 
		// By default, let it be empty
		int val = -1;
		
		return new FruitGridPoint(x, y, val);
	}
	
	public static void main(String args[])
	{

		Scanner in = new Scanner(System.in);

		homework.main(new String[] {});

		// Update scores
		while(outputToInput())
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
			
			if(turn == 0) {
				homework.main(new String[] {});
			}
			else {
				homework.main(new String[] {});
			}

			// Switch turn
			turn = (turn + 1)%2;
		}		

		in.close();
	}

}

