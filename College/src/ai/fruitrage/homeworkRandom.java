package ai.fruitrage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * A random FruitRageAgent, that will pick a random legal move.
 * 
 * @author Siddhesh Karekar
 */
public class homeworkRandom 
{

	/** If true, prints node information to the console. */
	private static final boolean DEBUG_MODE = false;

	private static String inputFileName = "input.txt";
	private static String outputFileName = "output.txt";

	// Time variables

	/** System.nanoTime() values. */
	private static long timeStart, timeCurrent;

	/** Convert floating value read from input into precise-r nanoseconds */
	private static long durAllotted;

	/**
	 * These values get updated every time you check how much time has elapsed.
	 */
	private static long durElapsedSinceStart, durRemaining;


	/**
	 * Reads the input from the text files in the format specified, and returns
	 * a byte array corresponding to the initial grid.
	 */
	private static byte[][] readInput(Scanner sc) {
		// Start reading input
		FruitRageNode.n = Integer.parseInt(sc.nextLine());
		System.out.println("Grid size (n) is " + FruitRageNode.n + ".");

		FruitRageNode.p = Integer.parseInt(sc.nextLine());
		System.out.println("Fruit types (p) are " + FruitRageNode.p + ".");

		float durSecondsAllotted = Float.parseFloat(sc.nextLine());
		durAllotted = secondsToNanoseconds(durSecondsAllotted);

		System.out.println("Time remaining is " + durSecondsAllotted + " seconds.");

		// Read the grid
		byte[][] gridInitial = new byte[FruitRageNode.n][FruitRageNode.n];
		for (int i = FruitRageNode.n - 1; i >= 0; i--) {
			String row = sc.nextLine();
			for (int j = 0; j < FruitRageNode.n; j++) {
				char ch = row.charAt(j);

				if (ch == '*')
					gridInitial[i][j] = FruitRageNode.EMPTY;
				else
					gridInitial[i][j] = (byte) (ch - '0');
			}
		}

		return gridInitial;
	}


	/**
	 * TODO
	 * 
	 * @param moveToPrint
	 */
	private static void finishSolved(FruitRageNode bestChild) {
		// Print solution to console as well as file
		PrintWriter writerOutput = null;
		// PrintWriter writerResult = null;
		try {

			

			writerOutput = new PrintWriter(outputFileName, "UTF-8");
			// writerResult = new PrintWriter(resultFileName, "UTF-8");

			System.out.println("\nSolution printed to file:");

			// If no children - print blank lines (?)
			if (bestChild == null) {
				System.out.println();
				writerOutput.println();

				System.out.println();
				writerOutput.println();

				// writerResult.println();
			}

			else {

				System.out.println(bestChild.moveFromParent);
				writerOutput.println(bestChild.moveFromParent);

				System.out.println(bestChild.gridString());
				writerOutput.println(bestChild.gridString());

				// Refresh values of nanoseconds
				getRemainingTime();

				System.out.format("\nGame results:\n" 
								+ "Move score is %d.\n" 
								+ "Grid size (n) is %d.\n"
								+ "Fruit types (p) are %d.\n" 
								+ "Seconds left are %.3fs.\n",
								bestChild.moveFromParentScore, 
								FruitRageNode.n, 
								FruitRageNode.p,
								nanosecondsToSeconds(durRemaining));

			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writerOutput != null)
				writerOutput.close();
			/*
			 * if(writerResult != null) writerResult.close();
			 */
		}
	}

	/**
	 * Updates timeCurrent and durElapsedSinceStart.<br>
	 * <br>
	 * Also prints the elapsed time in seconds.
	 * 
	 * @return The updated durElapsedSinceStart
	 */
	private static long getRemainingTime() {
		timeCurrent = System.nanoTime();

		durElapsedSinceStart = timeCurrent - timeStart;
		durRemaining = durAllotted - durElapsedSinceStart;

		if (DEBUG_MODE)
			System.out.println("Elapsed time: " + nanosecondsToSeconds(durElapsedSinceStart) + " seconds.");

		return durRemaining;
	}

	private static float nanosecondsToSeconds(long nanoseconds) {
		return nanoseconds / (1000.0f * 1000000);
	}

	private static long secondsToNanoseconds(float seconds) {
		return (long) (seconds * 1000 * 1000000);
	}

	public static void main(String[] args) {
		Scanner inInput;

		timeStart = System.nanoTime();

		// Read contents of input file
		try {
			// if input file specified
			if (args.length > 0) {
				inputFileName = args[0];

				if (inputFileName.toLowerCase().contains("input")) {
					// Use the same pattern for other files now
					outputFileName = inputFileName.toLowerCase().replace("input", "output");
					// resultFileName =
					// inputFileName.toLowerCase().replace("input", "result");
				}
			}

			inInput = new Scanner(new File(inputFileName));

			byte[][] gridInitial = readInput(inInput);

			// Create starting node
			FruitRageNode initNode = new FruitRageNode(gridInitial);

			System.out.println("\nStarting configuration: \n" + initNode + "\n");

			initNode.gravitate();

			List<FruitRageNode> children = initNode.generateChildren();
			
			// Assign a random legal move in case time runs out.
			FruitRageNode randomChild = children.get(new Random().nextInt(children.size()));
			
			System.out.println("Final max value (root) is " + randomChild.moveFromParentScore + " given by random move "
					+ randomChild.moveFromParent + " (" + randomChild.moveFromParentScore + " pts).");
			finishSolved(randomChild);

			inInput.close();

		} catch (FileNotFoundException e) {
		}
	}

}
