package ai.fruitrage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Implements minimax algorithm and alpha-beta pruning.
 * 
 * @author Siddhesh Karekar
 */
public class homework 
{

	/** If true, prints node information to the console. */
	// public static final boolean DEBUG_MODE = false;

	// private static String DEPTH_SEPARATOR = ".";

	/**
	 * If the search space is sufficiently small, you might not require a
	 * cutoff.<br>
	 * <br>
	 * For the IDS variant, this keeps on changing.
	 */
	private static int depth = 3;

	/**
	 * Never explore beyond these many levels.
	 */
	public static final int MAX_DEPTH = 8;

	public static String inputFileName = "input.txt";
	public static String outputFileName = "output.txt";

	// Time variables

	/** System.nanoTime() values. */
	private static long timeStart, timeCurrent;

	/**
	 * The time at which we start iterative deepening.
	 */
	private static long timeMovesearchStart;

	/** Convert floating value read from input into precise-r nanoseconds */
	private static long durAllotted;

	/**
	 * These values get updated every time you check how much time has elapsed.
	 */
	private static long durElapsedSinceStart, durRemaining;

	private static long durAllotToMove;

	// What if this happened when depth was 1?
	/** Must be initialized */
	private static boolean timeLimitExceeded;

	/**
	 * When these many nanoseconds (seconds * 10^9) are left, force the program
	 * to return the move already selected or a random one.
	 */
	// private static long durLimit = (long) (1.0f * 1000 * 1000000);

	// Other

	/** The initial values for alpha and beta */
	public static final int INF = Integer.MAX_VALUE - 1;
	
	/** The move (child of root) that is finally picked as the go-to move. */
	private static FruitRageNode bestChildSaved = null;
	
	/**
	 * The move (child of root) that is picked if you're running out of time. A
	 * legal child will be assigned to it..
	 */
	private static FruitRageNode fallbackChild = null;
	
	/** The utility of the finally picked go-to move. */
	private static int bestChildUtilitySaved = -INF;
	
	

	/**
	 * Reads the input from the text files in the format specified, and returns
	 * a byte array corresponding to the initial grid.
	 */
	private static byte[][] readInput(Scanner sc) {
		
		// Start reading input
		FruitRageNode.n = Integer.parseInt(sc.nextLine());
		// System.out.println("Grid size (n) is " + FruitRageNode.n + ".");

		FruitRageNode.p = Integer.parseInt(sc.nextLine());
		// System.out.println("Fruit types (p) are " + FruitRageNode.p + ".");

		float durSecondsAllotted = Float.parseFloat(sc.nextLine());
		durAllotted = secondsToNanoseconds(durSecondsAllotted);

		// System.out.println("Time remaining is " + durSecondsAllotted + " seconds.");

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
	 * Computes the utility value for any node.
	 * 
	 * @return
	 */
	private static int minimaxValue(FruitRageNode node, int alpha, int beta, int cutoff) {

		int v;

		if (node.isTerminalNode()) {
			
			/*if (homework.DEBUG_MODE) {
				for (int i = 0; i < node.depth; i++)
					System.out.print(DEPTH_SEPARATOR);

				System.out.format("%s value (terminal node) computed to be %d\n", (node.isMaxNode()) ? "Max" : "Min",
						node.utilityPassedDown);
			}*/

			return node.utilityPassedDown;
		} 
		else {
			// Not a terminal node

			// Time-Cutoff condition
			long durElapsedOnMoveSoFar = System.nanoTime() - timeMovesearchStart;
			if (timeLimitExceeded || durElapsedOnMoveSoFar > durAllotToMove) {

				if (!timeLimitExceeded) {
					System.out.printf("Time limit exceeded mid-iteration - %.3f > %.3f.\n",
							nanosecondsToSeconds(durElapsedOnMoveSoFar), nanosecondsToSeconds(durAllotToMove));
					timeLimitExceeded = true;
					
					if(depth == 1)
					{
						emergencyExit();
					}
				}
				return node.utilityPassedDown;
			}

			// Compute children
			List<FruitRageNode> children = node.generateChildren();

			// Sort children greedily to maximize cutoff
			if (node.isMaxNode())
				Collections.sort(children, Collections.reverseOrder());
			else
				Collections.sort(children);

			// Evaluation procedure for depth-cutoff
			if (node.depth >= cutoff) {
				
				/*if (homework.DEBUG_MODE) {

					for (int i = 0; i < node.depth; i++)
						System.out.print(DEPTH_SEPARATOR);

					System.out.format("%s value (cutoff node) computed to be %d\n", (node.isMaxNode()) ? "Max" : "Min",
							node.utilityPassedDown);
				}*/

				return node.utilityPassedDown;
			} else {

				if (node.isMaxNode()) // Max node
				{
					v = -INF;

					for (FruitRageNode child : children) {
						int result = minimaxValue(child, alpha, beta, cutoff);
						v = Math.max(v, result);
						if (v >= beta)
						{
							
							/*if (homework.DEBUG_MODE) {
								for (int i = 0; i < node.depth; i++)
									System.out.print(DEPTH_SEPARATOR);

								System.out.println("Max value (pruned) computed to be " + v);
							}*/
							
							return v;
						}
						alpha = Math.max(v, alpha);
					}

				} else // Min node
				{
					v = +INF;

					for (FruitRageNode child : children) {
						int result = minimaxValue(child, alpha, beta, cutoff);
						v = Math.min(v, result);
						if (v <= alpha)
						{
							/*if (homework.DEBUG_MODE) {

								for (int i = 0; i < node.depth; i++)
									System.out.print(DEPTH_SEPARATOR);

								System.out.println("Min value (pruned) computed to be " + v);
							}*/

							return v;
						}
						beta = Math.min(v, beta);
					}
				}
			}

		}

		/*if (homework.DEBUG_MODE) {

			for (int i = 0; i < node.depth; i++)
				System.out.print(DEPTH_SEPARATOR);

			System.out.println("Minimax value computed to be " + v);
		}*/

		return v;
	}

	/**
	 * Pick a certain (random?) move if you're running out of time fast.
	 */
	private static void emergencyExit() {

		System.out.println("Emergency exit! A random move will be chosen.\n");	
		bestChildSaved = fallbackChild;
		bestChildUtilitySaved = fallbackChild.moveFromParentScore;

	}

	/**
	 * An evaluation function that estimates the utility of a non-terminal node
	 * by ignoring gravity, so that all future moves are selected in the order
	 * of decreasing group size only.
	 * 
	 * @param node
	 *            The current node for which Minimax is being evaluated
	 * @param children
	 *            You must generate the node's children for this kind of
	 *            evaluation.
	 */
	/*private static int estimateUtilityGain(FruitRageNode node, List<FruitRageNode> children) {
		int estimatedUtilityGain = 0;

		for (int i = 0; i < children.size(); i++) {
			if ((i + node.depth) % 2 == 0) {
				estimatedUtilityGain += children.get(i).moveFromParentScore;
			} else {
				estimatedUtilityGain -= children.get(i).moveFromParentScore;
			}
		}

		return estimatedUtilityGain;
	}*/

	/**
	 * TODO
	 * 
	 * @param moveToPrint
	 */
	private static void finishSolved(FruitRageNode bestChild) {
		// Print solution to console as well as file
		PrintWriter writerOutput = null;
		// PrintWriter writerResult = null;
		try 
		{

			writerOutput = new PrintWriter(outputFileName, "UTF-8");
			// writerResult = new PrintWriter(resultFileName, "UTF-8");

			System.out.println("\nSolution: "+bestChild.moveFromParent);
			
			System.out.printf("Seconds left: %.3fs\n",nanosecondsToSeconds(durRemaining));

			// If no children - print blank lines (?)
			/* if (bestChild == null) 
			{
				System.out.println();
				writerOutput.println();

				System.out.println();
				writerOutput.println();

				// writerResult.println();
			}

			else
			{

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

			}*/

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

		/*if (DEBUG_MODE)
			System.out.println("Elapsed time: " + nanosecondsToSeconds(durElapsedSinceStart) + " seconds."); */
		return durRemaining;
	}

	public static float nanosecondsToSeconds(long nanoseconds) {
		return nanoseconds / (1000.0f * 1000000);
	}

	public static long secondsToNanoseconds(float seconds) {
		return (long) (seconds * 1000 * 1000000);
	}

	public static void main(String[] args) {
		Scanner inInput;

		timeStart = System.nanoTime();
		
		timeLimitExceeded = false;

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

			// System.out.println("\nStarting configuration: \n" + initNode + "\n");

			initNode.gravitate();

			List<FruitRageNode> children = initNode.generateChildren();
			
			// Assign a random legal move in case time runs out - this appears to be slower
			// fallbackChild = children.get(new Random().nextInt(children.size()));

			// Maximize pruning?
			Collections.sort(children, Collections.reverseOrder());
			
			// Assign the greediest legal move in case time runs out.
			fallbackChild = children.get(0);

			FruitRageNode bestChild = null;
			int bestChildUtility = -INF;

			// Check if no children exist!
			if (children.isEmpty()) {
				// moveToPlay = "";
				// Handle this in finish(FruitRageNode)
			} 
			else // Find the move to perform
			{

				// Check time remaining
				getRemainingTime();
				// System.out.printf("Remaining time is %.3f seconds.\n", nanosecondsToSeconds(durRemaining));

				// Approximately calculate how much time the depth-searching in
				// TOTAL should take.
				durAllotToMove = durRemaining / children.size() * 2;
				
				/*System.out.printf("The move search for all depths is allowed to be %.3f seconds.\n",
						nanosecondsToSeconds(durAllotToMove));*/

				timeMovesearchStart = System.nanoTime();

				// depth is global
				for (depth = 1; depth < MAX_DEPTH; depth++) {
					// System.out.println("\nGoing to depth " + depth + ".");

					bestChild = null;
					bestChildUtility = -INF;

					for (int i = 0; i < children.size(); i++) {
						FruitRageNode currentChild = children.get(i);

						int currentChildUtility = minimaxValue(currentChild, -INF, +INF, depth);

						if (currentChildUtility > bestChildUtility) {
							bestChild = currentChild;
							bestChildUtility = currentChildUtility;
						}
					}

					if (!timeLimitExceeded)
					{
						bestChildSaved = bestChild;
						bestChildUtilitySaved = bestChildUtility;

						/*System.out.println("At this depth, max value (root) is " + bestChildUtilitySaved + " given by "
								+ bestChildSaved.moveFromParent + " (" + bestChildSaved.moveFromParentScore
								+ " pts).\n");*/

						long durIterationsSoFar = System.nanoTime() - timeMovesearchStart;
						/*System.out.printf("So far, depth searching took %.3f seconds.\n",
								nanosecondsToSeconds(durIterationsSoFar));*/

						if (durIterationsSoFar * (depth) > durAllotToMove) {
							// System.out.println("Searching the next depth will be too expensive.");
							break;
						}

					} 
					else {
						break;
					}

				}

				System.out.println("Final max value (root) is " + bestChildUtilitySaved + " given by "
						+ bestChildSaved.moveFromParent + " (" + bestChildSaved.moveFromParentScore + " pts).");
			}

			finishSolved(bestChildSaved);

			inInput.close();

		} catch (FileNotFoundException e) {
		}
	}

}

class FruitRageNode implements Comparable<FruitRageNode> {

	/** Width and height of the square board (0 < n <= 26) */
	public static int n;

	/** Number of fruit types (0 < p <= 9) */
	public static int p;

	/** The value used for empty spaces on the grid */
	public static final byte EMPTY = -1;

	/** How the empty spaces are displayed */
	public static final char EMPTY_CHAR = '*';

	// Now begin the instance variables.

	/** Stores all the fruit positions. */
	public byte[][] grid;

	/**
	 * Measures how deep the tree has become. Also specifies whether it is a MIN
	 * or a MAX node; for a MAX node, the value of depth will be even (since it
	 * starts from 0).
	 */
	public int depth;

	/**
	 * Records the move that the parent node played to result in this child.
	 */
	public String moveFromParent;

	/**
	 * Records the score gained by parent while generating this child.
	 */
	public int moveFromParentScore;

	/**
	 * The utility value of the node.<br>
	 * <br>
	 * This value is only valid for terminal nodes. For any non-terminal node,
	 * the utility value is always calculated by using Minimax.
	 */
	public int utilityPassedDown;

	/*
	 * public FruitRageNode() { for(int i = 0; i < n; i++) for(int j = 0; j < n;
	 * j++) grid[i][j] = EMPTY;
	 * 
	 * depth = 0; }
	 */

	public FruitRageNode(byte[][] gridParam) {
		this.grid = gridParam;
		this.depth = 0;
		this.utilityPassedDown = 0;
		this.moveFromParent = "";
		this.moveFromParentScore = 0;
	}

	public FruitRageNode(byte[][] gridParam, int depth, int utilityGain, String move, int score) {
		this.grid = gridParam;
		this.depth = depth;
		this.utilityPassedDown += utilityGain;
		// System.out.println("Utility increased by "+utilityGain + " -> " +
		// utilityPassedDown);
		this.moveFromParent = move;
		this.moveFromParentScore = score;
	}

	/**
	 * Alters the current grid in such a way that all empty spaces rise to the
	 * top.
	 */
	public void gravitate() {

		// Go column-wise
		for (int j = 0; j < n; j++)
		{

			for (int i = (n-1)-1; i >= 0; i--)
			{
				// No need to swap if the top element already contains EMPTY
				if (grid[i][j] == EMPTY)
				{
					for(int k = i; k < n-1; k++)
					{
						grid[k][j] = grid[k+1][j];
					}
					grid[n-1][j] = EMPTY;
				}
			}
		}
	}

	/**
	 * True if that node is a max node.<br>
	 * <br>
	 * This is decided by the depth of the game tree at that point; if it is
	 * even, it's a max node.
	 */
	public boolean isMaxNode() {
		return depth % 2 == 0;
	}

	/**
	 * Allow the use of Collections.sort to sort node objects.<br>
	 * <br>
	 * The sort order is used descending or max-nodes, ascending for min-nodes.
	 */
	@Override
	public int compareTo(FruitRageNode otherNode) {
		return Integer.compare(this.moveFromParentScore, otherNode.moveFromParentScore);
	}

	/** String representation that contains the grid. */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		// sb.append("The grid looks like: \n");
		sb.append(this.gridString());

		return sb.toString();
	}

	/** Check if all spaces are empty. */
	public boolean isTerminalNode() {
		// Check if any of the grid spaces contains a fruit
		for (int i = 0; i < FruitRageNode.n; i++) {
			for (int j = 0; j < FruitRageNode.n; j++) {
				if (this.grid[i][j] != FruitRageNode.EMPTY)
					return false;
			}
		}

		return true;
	}

	/**
	 * Return all the children for this node. Check all the possible moves -
	 * each child corresponds to one move.
	 * 
	 * @param node
	 * @return
	 */
	public List<FruitRageNode> generateChildren() {

		List<FruitRageNode> children = new ArrayList<>();

		/**
		 * Stores all the non-duplicated points that form a single group.
		 * Initially empty.
		 */
		List<List<FruitGridPoint>> groupPoints = new ArrayList<>();

		// Check all possible moves
		boolean[][] visited = new boolean[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				visited[i][j] = false;
			}
		}

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (!visited[i][j]) {
					int value = grid[i][j];
					List<FruitGridPoint> currentGroup = new ArrayList<>();

					markGroups(currentGroup, visited, i, j, value);

					if (value != EMPTY)
						groupPoints.add(currentGroup);

					/*
					 * if(homework.DEBUG_MODE)
					 * System.out.format("%d square(s) in this group of %d's.\n"
					 * , currentGroup.size(), value);
					 */
				}
			}
		}

		// We now have all the points, upon selection of which a new child is
		// formed
		/*if (homework.DEBUG_MODE)
			System.out.format("%d possible move(s) from this node.\n", groupPoints.size());*/

		// These will be ordered in the minimax call.
		for (List<FruitGridPoint> action : groupPoints) {
			// Copy the grid
			byte[][] childGrid = new byte[FruitRageNode.n][FruitRageNode.n];
			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid[i].length; j++) {
					childGrid[i][j] = grid[i][j];
				}
			}

			// Blank out this group in the grid
			for (FruitGridPoint point : action)
				childGrid[point.x][point.y] = FruitRageNode.EMPTY;

			/**
			 * Record the score of this move by increasing utility - it should
			 * be increased by n^2.
			 */
			int utilityIncrease = action.size() * action.size();

			// if it is a Min-Node, move is opponent's, so make this negative
			if (!this.isMaxNode())
				utilityIncrease = -utilityIncrease;

			// Record which move was played
			String movePlayed = FruitGridPoint.pointToMoveString(action.get(0).x, action.get(0).y);

			// Create a new node with this configuration
			FruitRageNode child = new FruitRageNode(childGrid, this.depth + 1,
					(this.utilityPassedDown + utilityIncrease), movePlayed, utilityIncrease);

			// Apply gravity
			child.gravitate();

			// add it to children! whoopee!
			children.add(child);
		}

		return children;
	}

	/**
	 * TODO Describe this function
	 */
	private void markGroups(List<FruitGridPoint> currentGroup, boolean[][] visited, int i, int j, int value) {

		if (!visited[i][j] && grid[i][j] == value) {
			visited[i][j] = true;
			currentGroup.add(new FruitGridPoint(i, j, value));

			if (i < n - 1)
				markGroups(currentGroup, visited, i + 1, j, value);
			if (i > 0)
				markGroups(currentGroup, visited, i - 1, j, value);
			if (j < n - 1)
				markGroups(currentGroup, visited, i, j + 1, value);
			if (j > 0)
				markGroups(currentGroup, visited, i, j - 1, value);
		}

	}

	/** Returns a string representation required in the output. */
	public String gridString() {
		StringBuilder sb = new StringBuilder();

		for (int i = n - 1; i >= 0; i--) {
			for (int j = 0; j < n; j++) {
				if (this.grid[i][j] == EMPTY)
					sb.append(EMPTY_CHAR);
				else
					sb.append(this.grid[i][j]);
			}

			if (i > 0)
				sb.append(System.lineSeparator());
		}

		return sb.toString();
	}

	/**
	 * Returns a string representation like the one specified in the examples.
	 */
	/*
	 * public String gridStringPretty() { StringBuilder sb = new
	 * StringBuilder();
	 * 
	 * for(int j = 0; j < 2*n+1; j++) sb.append("-"); sb.append("\n");
	 * 
	 * for(int i = n-1; i >= 0; i--) {
	 * 
	 * for(int j = 0; j < n; j++) { if(j == 0) sb.append("|");
	 * if(this.grid[i][j] == EMPTY) sb.append(EMPTY_CHAR); else
	 * sb.append(this.grid[i][j]); sb.append("|"); }
	 * 
	 * sb.append("\n"); }
	 * 
	 * for(int j = 0; j < 2*n+1; j++) sb.append("-");
	 * 
	 * return sb.toString(); }
	 */
}

class FruitGridPoint {
	/** The location of this point on the grid. */
	public int x, y;
	public int value;

	public FruitGridPoint(int x, int y, int val) {
		this.x = x;
		this.y = y;
		this.value = val;
	}

	/**
	 * Check if another FruitGridPoint has the same x, y coordinates and value.
	 */
	@Override
	public boolean equals(Object o) {

		// If the object is compared with itself then return true
		if (o == this) {
			return true;
		}

		// Check if o is an instance of NurseryGridPoint or not
		// "null instanceof [type]" also returns false
		if (!(o instanceof FruitGridPoint)) {
			return false;
		}

		// typecast o to NurseryGridPoint so that we can compare data members
		FruitGridPoint c = (FruitGridPoint) o;

		// Compare the data members and return accordingly
		return (this.x == c.x && this.y == c.y && this.value == c.value);
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public String toString() {
		return String.format("%d(%d, %d)", value, x, y);
	}

	/**
	 * Your selected move, represented as two characters: A letter from A to Z
	 * representing the column number (where A is the leftmost column, B is the
	 * next one to the right, etc), and A number from 1 to 26 representing the
	 * row number (where 1 is the top row, 2 is the row below it, etc).
	 */
	public static String pointToMoveString(int x, int y) {
		StringBuilder sb = new StringBuilder();

		sb.append((char) ('A' + y));
		sb.append(FruitRageNode.n - x);

		return sb.toString();
	}

}