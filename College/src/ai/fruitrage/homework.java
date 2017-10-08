/**
 * 
 */
package ai.fruitrage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 *	Implements minimax algorithm and alpha-beta pruning.
 */
public class homework {

	private static String inputFileName = "input.txt";
	private static String outputFileName = "output.txt";

	private static long timeStart, timeCurrent;
	private static long secondsRemaining;

	/** If true, prints node information to the console. */
	public static final boolean DEBUG_MODE = true;
	
	/** The initial values for alpha and beta */
	public static final int INF = Integer.MAX_VALUE;

	/**
	 * Reads the input from the text files in the format specified, and returns
	 * a byte array corresponding to the initial grid.
	 */
	private static byte[][] readInput(Scanner sc)
	{
		// Start reading input
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

	/**
	 * TODO Computes the utility value for any node.
	 * @return
	 */
	private static int minimaxValue(FruitRageNode node, int alpha, int beta)
	{
		if(homework.DEBUG_MODE)
			System.out.format("Computing minimax value for %snode\ns%s\n",
					(node.isMaxNode())?"max":"min", node);
		
		int v;

		if(node.isTerminalNode())
		{
			return node.utilityPassedDown;
		}
		else {
			// Not a terminal node

			// Compute children
			List<FruitRageNode> children = node.generateChildren();

			if(node.isMaxNode()) // Max node
			{
				v = -INF;
				
				for(FruitRageNode child : children)
				{
					int result = minimaxValue(child, alpha, beta);
					v = Math.max(v, result);
					if(v >= beta)
					{
						return v;
					}
					alpha = Math.max(v, alpha);
				}
				
			}
			else // Min node
			{
				v = +INF;
				
				for(FruitRageNode child : children)
				{
					int result = minimaxValue(child, alpha, beta);
					v = Math.min(v, result);
					if(v <= alpha)
					{
						return v;
					}
					beta = Math.min(v, beta);
				}
			}

		}
		
		if(homework.DEBUG_MODE)
			System.out.println("Minimax value computed to be "+v);
		
		return v;
	}
	
	private static void finish(String moveToPrint)
	{
		// Print solution to console as well as file
		PrintWriter writer = null;
		try{

			writer = new PrintWriter(outputFileName, "UTF-8");
			System.out.println(moveToPrint);
			writer.println(moveToPrint);


		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(writer != null)
				writer.close();
		}
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
			
			if(homework.DEBUG_MODE)
				System.out.println("Starting configuration: \n"+initNode);

			initNode.gravitate();
			
			List<FruitRageNode> children = initNode.generateChildren();
			
			String moveToPlay = null;
			
			// TODO Check if no children exist!
			if(children.isEmpty())
				moveToPlay = "";
			else {

				// Find the move to perform			
				FruitRageNode bestChild = children.remove(0);
				int bestChildUtility = minimaxValue(bestChild, -INF, +INF);
				for(FruitRageNode otherChild : children)
				{
					int otherChildUtility = minimaxValue(otherChild, -INF, +INF);
					if(otherChildUtility > bestChildUtility)
					{
						bestChild = otherChild;
						bestChildUtility = otherChildUtility;
					}
				}
				moveToPlay = bestChild.moveFromParent;
			}
			
			finish(moveToPlay);

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
	 * The utility value of the node.<br>
	 * <br>
	 * This value is only valid for terminal nodes. For any non-terminal node,
	 * the utility value is always calculated by using Minimax.
	 */
	public int utilityPassedDown;

	/*public FruitRageNode()
	{
		for(int i = 0; i < n; i++)
			for(int j = 0; j < n; j++)
				grid[i][j] = EMPTY;

		depth = 0;
	}*/
	
	public FruitRageNode(byte[][] gridParam)
	{
		this.grid = gridParam;
		this.depth = 0;
		this.utilityPassedDown = 0;
		this.moveFromParent = "";
	}

	public FruitRageNode(byte[][] gridParam, int depth, int utilityGain, String move)
	{
		this.grid = gridParam;
		this.depth = depth;
		this.utilityPassedDown += utilityGain;
		this.moveFromParent = move;
	}

	/**
	 * Returns a List containing all the point objects corresponding to its grid
	 * @return
	 */
	public List<FruitGridPoint> gridPoints()
	{
		List<FruitGridPoint> points = new ArrayList<>();

		for(int i = 0; i < n; i++)
			for(int j = 0; j < n; j++)
			{
				FruitGridPoint p = new FruitGridPoint(i, j, grid[i][j]);
				points.add(p);
			}


		return points;
	}

	/**
	 * Alters the current grid in such a way that 
	 * all empty spaces rise to the top.
	 */
	public void gravitate()
	{
		if(homework.DEBUG_MODE)
			System.out.println("Applying gravity...");

		// Go column-wise
		for(int j = 0; j < n; j++)
		{
			/* Go downwards the column fixing the top element
			 * if EMPTY is found anywhere, swap it with the top element */
			for(int i = n-1; i > 0; i--)
			{
				// No need to swap if the top element already contains EMPTY
				if(grid[i][j] == EMPTY)
					continue;

				for(int k = i-1; k >= 0; k--)
				{
					if(grid[k][j] == EMPTY)
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

	/**
	 * True if that node is a max node.<br>
	 * <br>
	 * This is decided by the depth of the game tree at that point; if it is
	 * even, it's a max node.
	 */
	public boolean isMaxNode() {
		return depth %2 == 0;
	}

	/** String representation that contains the grid. */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		// sb.append("The grid looks like: \n");
		sb.append(gridString(grid));

		return sb.toString();
	}

	/** Check if all spaces are empty. */
	public boolean isTerminalNode()
	{		
		// Check if any of the grid spaces contains a fruit
		for(int i = 0; i < FruitRageNode.n; i++)
		{
			for (int j = 0; j < FruitRageNode.n; j++)
			{
				if(this.grid[i][j] != FruitRageNode.EMPTY)
					return false;
			}
		}

		return true;
	}

	/**
	 * Return all the children for this node.
	 * Check all the possible moves - each child corresponds to one move.
	 * @param node
	 * @return
	 */
	public List<FruitRageNode> generateChildren()
	{
		if(homework.DEBUG_MODE)
			System.out.println("Generating children...");

		List<FruitRageNode> children = new ArrayList<>();

		// Check all possible moves
		// Possible optimization: store fully empty rows inside node (limit i < some number)
		// Remember, i starts from the rows at the BOTTOM.
		List<FruitGridPoint> allPoints = this.gridPoints();

		/** Stores all the non-duplicated points that form a single group */
		List<List<FruitGridPoint>> groupPoints = new ArrayList<>();

		for(int i = 0; i < allPoints.size(); i++)
		{
			FruitGridPoint currentPoint = allPoints.get(i);
			
			if(homework.DEBUG_MODE)
				System.out.format("currentPoint %s\n", currentPoint);
			
			/** The 'group', containing this point and all its adjacents */
			List<FruitGridPoint> currentGroupPoints = new ArrayList<>();

			currentGroupPoints.add(currentPoint);
			
			// TODO this logic only adds adjacent points, not indirectly connected ones. Fix it!
			
			
			///////////////////////
			
			
			
			fourWayFill(currentGroupPoints, currentPoint, allPoints);
			
			
			
			//////////////////////////
			 
			
			
			if(homework.DEBUG_MODE)
				System.out.format("otherPoints in this currentGroup: %d\n", currentGroupPoints.size());

			// Only make this group valid if its root was a non-empty point
			if(currentPoint.value != FruitRageNode.EMPTY) 
			{
				groupPoints.add(currentGroupPoints);
			}
		}

		// We now have all the points, upon selection of which a new child is formed
		if(homework.DEBUG_MODE)
			System.out.format("%d possible move(s) from this node.\n", groupPoints.size());

		// TODO possibly order them in some way - heuristics

		for(List<FruitGridPoint> action : groupPoints)
		{
			// Copy the grid
			byte[][] childGrid = new byte[FruitRageNode.n][FruitRageNode.n];
			for(int i = 0; i< grid.length; i++){
				for (int j = 0; j < grid[i].length; j++){
					childGrid[i][j] = grid[i][j];
				}
			}

			// Blank out this group in the grid
			for(FruitGridPoint point : action)
				childGrid[point.x][point.y] = FruitRageNode.EMPTY;
			
			// PASS THE UTILITY! The utility should be increased by n^2.
			int utilityIncrease = groupPoints.size() * groupPoints.size();
			
			// if current node is max player, next is min, so make this negative
			if(this.isMaxNode())
				utilityIncrease = -utilityIncrease;
			
			// Record which move was played
			String movePlayed = FruitGridPoint.pointToMove(action.get(0).x, action.get(0).y);
			
			// Create a new node with this configuration
			FruitRageNode child = new FruitRageNode(childGrid, 
					this.depth+1, 
					(this.utilityPassedDown + utilityIncrease),
					movePlayed);
			
			// Apply gravity
			child.gravitate();
			
			if(homework.DEBUG_MODE)
				System.out.println("Adding child "+child);
			
			// add it to children! whoopee!
			children.add(child);
		}

		return children;
	}

	/**
	 * TODO
	 */
	private static void fourWayFill(List<FruitGridPoint> currentGroupPoints, 
			FruitGridPoint currentSeedPoint, 
			List<FruitGridPoint> allPoints) {
		
		int x = currentSeedPoint.x, y = currentSeedPoint.y;
		
		// The four directions
		FruitGridPoint pointUp = new FruitGridPoint(x+1, y, -2);
		FruitGridPoint pointLeft = new FruitGridPoint(x, y-1, -2);
		FruitGridPoint pointRight = new FruitGridPoint(x, y+1, -2);
		FruitGridPoint pointDown = new FruitGridPoint(x-1, y, -2);
		FruitGridPoint[] adjacentPoints = {pointUp, pointLeft, pointRight, pointDown};
		
		// look for, and remove these points from allPoints, and place them in currentGroupPoints
		for(FruitGridPoint adjacentPoint : adjacentPoints)
		{
			// If the adjacent point is not already in the current group
			if(!currentGroupPoints.contains(adjacentPoint))
			{
				// Check if the adjacent point does not cross the bounds of the grid
				int adjacentPointIndex = allPoints.indexOf(adjacentPoint);
				if(adjacentPointIndex != -1 && 
						allPoints.get(adjacentPointIndex).value == currentSeedPoint.value) // If it is found, and is in the same group
				{
					// Move the adjacent point from allPoints into the currentGroup of points.
					currentGroupPoints.add(allPoints.remove(adjacentPointIndex));
					
					// fourWayFill(currentGroupPoints, adjacentPoint, allPoints);
				}
			}
		}
		
		
	}

	/** Returns a string representation like the one specified in the examples. */
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

class FruitGridPoint
{
	/** The location of this point on the grid. */
	public int x, y;
	public int value;

	public FruitGridPoint(int x, int y, int val)
	{
		this.x = x;
		this.y = y;
		this.value = val;
	}

	/** Check if another point is adjacent to this point
	 * (but not on the same square) */
	public boolean isAdjacent(FruitGridPoint p2)
	{
		FruitGridPoint p1 = this;
		if(
				(p1.x == p2.x+1 && p1.y == p2.y) ||
				(p1.x == p2.x-1 && p1.y == p2.y) ||
				(p1.x == p2.x && p1.y == p2.y+1) ||
				(p1.x == p2.x && p1.y == p2.y-1)) {
			return true;
		} else
			return false;
	}
	
	/*public boolean isAdjacent(List<FruitGridPoint> list)
	{
		for(FruitGridPoint pointInList : list)
			if(this.isAdjacent(pointInList))
				return true;
		
		return false;
	}*/
	

	
	@Override
	public boolean equals(Object o) 
	{

		// If the object is compared with itself then return true  
		if (o == this) {
			return true;
		}

		// Check if o is an instance of NurseryGridPoint or not
        //  "null instanceof [type]" also returns false 
		if (!(o instanceof FruitGridPoint)) {
			return false;
		}

		// typecast o to NurseryGridPoint so that we can compare data members 
		FruitGridPoint c = (FruitGridPoint) o;

		// Compare the data members and return accordingly 
		return (this.x == c.x && this.y == c.y);
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	

	@Override
	public String toString()
	{
		return String.format("(%d, %d)", x, y);
	}
	
	public static String toString(int a, int b)
	{
		return String.format("(%d, %d)", a, b);
	}
	
	/**
	 * Your selected move, represented as two characters: A letter from A to Z
	 * representing the column number (where A is the leftmost column, B is the
	 * next one to the right, etc), and A number from 1 to 26 representing the
	 * row number (where 1 is the top row, 2 is the row below it, etc).
	 */
	public static String pointToMove(int x, int y)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append((char)('A'+y));
		sb.append(FruitRageNode.n - x);	
		
		return sb.toString();
	}

}