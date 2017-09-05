package ai.lizardnursery;

import java.util.LinkedList;
import java.util.Queue;

/**
 * You will write a program that will take an input file that has an arrangement
 * of trees and will output a new arrangement of lizards such that no baby
 * lizard can eat another one. To find the solution you will use the following
 * algorithms: BFS, DFS, SA. <br>
 * <br>
 * 
 * Input: The file input.txt in the current directory of your program will be
 * formatted as follows: <br>
 * <br>
 * 
 * First line: instruction of which algorithm to use: BFS, DFS or SA,<br>
 * Second line: strictly positive 32-bit integer n, the width and height of the
 * square nursery,<br>
 * Third line: strictly positive 32-bit integer p, the number of baby lizards
 * Next n lines: the n x n nursery, one file line per nursery row (to show you
 * where the trees are). <br>
 * <br>
 * 
 * It will have a 0 where there is nothing, and a 2 where there is a tree.
 */
public class LizardNursery {
	
	private static final String FILE_INPUT = "input.txt";
	private static final String FILE_OUTPUT = "output.txt";
	
	/**
	 * The algorithm to use: BFS, DFS or SA.
	 */
	private static String algo;
	
	/**
	 * The width and height of the square nursery.
	 */
	private static int n;
	
	/**
	 * The number of baby lizards.
	 */
	private static int p;
	
	/**
	 * 
	 */
	// private static int[][] nursery;

	public static void main(String[] args)
	{
		// Hardcode away! <3
		algo = "BFS";
		n = 8;
		p = 8;
		int[][] nursery =
					{{0,0,0,0,0,0,0,0}, 
					{0,0,0,0,0,0,0,0}, 
					{0,0,0,0,0,0,0,0}, 
					{0,0,0,0,2,0,0,0}, 
					{0,0,0,0,0,0,0,0}, 
					{0,0,0,0,0,2,0,0}, 
					{0,0,0,0,0,0,0,0}, 
					{0,0,0,0,0,0,0,0}};

	}

}

/**
 * 
 */
class NurseryGridPoint
{
	/** The location of this point on the grid. */
	private int x, y;
	
	public NurseryGridPoint(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

}

/**
 * Defines a basic state in the search tree.
 */
class NurseryState {
	Queue<NurseryGridPoint> availablePoints;
	
	NurseryState()
	{
		availablePoints = new LinkedList<NurseryGridPoint>();
	}
}