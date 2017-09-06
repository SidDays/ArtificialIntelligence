package ai.lizardnursery;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

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
	// private static final String FILE_OUTPUT = "output.txt";

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
	 * Stores the initial nursery state that becomes the first node.
	 */
	private static int[][] nursery;

	/**
	 * Frontier for Breadth-First Search.
	 */
	private static Queue<NurseryNode> bfsQueue;

	private static boolean isSolvable = false;

	/**
	 * Inserts a lizard in the first free available spot on the grid for
	 * a node, and returns a new node with that configuration.
	 * 
	 * After the lizard is placed, also makes all spots in the lizard's LOS
	 * (and the lizard's position) unavailable.
	 * 
	 * This method will need to be modified to place a lizard at a
	 * specified point instead - TODO
	 * 
	 * @param node
	 * @return
	 */
	@Deprecated
	public static NurseryNode insertLizard(NurseryNode node)
	{
		NurseryNode newNode = new NurseryNode(node);

		// Find a free point to place a lizard in
		List<NurseryGridPoint> availablePointsForThis = newNode.getAvailablePoints();
		if(!availablePointsForThis.isEmpty())
		{
			NurseryGridPoint pointFree = availablePointsForThis.remove(0); // <- change this code

			// Place a lizard
			int xpos = pointFree.getX();
			int ypos = pointFree.getY();
			int[][] nursery = newNode.getNursery();
			nursery[xpos][ypos] = 1;

			int xlos, ylos;

			// Propogate the lizard's LOS and mark those spaces as unavailable
			xlos = xpos; ylos = ypos;

			// Left side
			while(--xlos >= 0)
			{		
				if(nursery[xlos][ylos] == 2) // Break if it's a tree
					break;
				NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
				if(availablePointsForThis.remove(pointToRemove))
					System.out.println(pointToRemove+" (left) removed from availablePoints.");
			}
			xlos = xpos; ylos = ypos;

			// Right side
			while(++xlos < n)
			{		
				if(nursery[xlos][ylos] == 2)
					break;
				NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
				if(availablePointsForThis.remove(pointToRemove))
					System.out.println(pointToRemove+" (right) removed from availablePoints.");
			}
			xlos = xpos; ylos = ypos;

			// Top side
			while(++ylos < n)
			{		
				if(nursery[xlos][ylos] == 2) // Break if it's a tree
					break;
				NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
				if(availablePointsForThis.remove(pointToRemove))
					System.out.println(pointToRemove+" (top) removed from availablePoints.");
			}
			xlos = xpos; ylos = ypos;

			// Bottom side
			while(--ylos >= 0)
			{		
				if(nursery[xlos][ylos] == 2) // Break if it's a tree
					break;
				NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
				if(availablePointsForThis.remove(pointToRemove))
					System.out.println(pointToRemove+" (bottom) removed from availablePoints.");
			}
			xlos = xpos; ylos = ypos;

			// Top-left side
			while(--xlos >= 0 && ++ylos < n)
			{		
				if(nursery[xlos][ylos] == 2) // Break if it's a tree
					break;
				NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
				if(availablePointsForThis.remove(pointToRemove))
					System.out.println(pointToRemove+" (top-left) removed from availablePoints.");
			}
			xlos = xpos; ylos = ypos;

			// Top-right side
			while(++xlos < n && ++ylos < n)
			{		
				if(nursery[xlos][ylos] == 2) // Break if it's a tree
					break;
				NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
				if(availablePointsForThis.remove(pointToRemove))
					System.out.println(pointToRemove+" (top-right) removed from availablePoints.");
			}
			xlos = xpos; ylos = ypos;

			// Bottom-left side
			while(--xlos >= 0 && --ylos >= 0)
			{		
				if(nursery[xlos][ylos] == 2) // Break if it's a tree
					break;
				NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
				if(availablePointsForThis.remove(pointToRemove))
					System.out.println(pointToRemove+" (bottom-left) removed from availablePoints.");
			}
			xlos = xpos; ylos = ypos;

			// Bottom-right side
			while(++xpos < n && --ylos >= 0)
			{		
				if(nursery[xlos][ylos] == 2) // Break if it's a tree
					break;
				NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
				if(availablePointsForThis.remove(pointToRemove))
					System.out.println(pointToRemove+" (bottom-right) removed from availablePoints.");
			}
			xlos = xpos; ylos = ypos;
			
			// Lizard has been placed, all blocked spots removed
			return newNode;
		}
		else
			return null;

	}

	public static void main(String[] args) 
	{
		Scanner sc;

		// Read contents of input file
		try
		{
			sc = new Scanner(new File(FILE_INPUT));

			algo = sc.nextLine().trim().toUpperCase();

			n = Integer.parseInt(sc.nextLine());
			nursery = new int[n][n];

			p = Integer.parseInt(sc.nextLine());

			// Read initial nursery layout
			for(int i = 0; i < n; i++)
			{
				String row = sc.nextLine();
				for(int j = 0; j < n; j++)
				{
					nursery[i][j] = Integer.parseInt(String.valueOf(row.charAt(j)));
				}
			}

			/*System.out.println(algo); // Print everything
			System.out.println(n);
			System.out.println(p);
			for(int i = 0; i < n; i++)
			{
				for(int j = 0; j < n; j++)
					System.out.print(nursery[i][j]);
				System.out.println();
			}*/

			// Run the algorithm
			if(algo.equals("BFS"))
			{
				// TODO Process this stuff
				bfsQueue = new LinkedList<>();

				// create initial node
				NurseryNode initNode = new NurseryNode(nursery);
				bfsQueue.add(initNode);

				while(!bfsQueue.isEmpty())
				{
					NurseryNode current = bfsQueue.remove();
				}



				// Finished
				if(!isSolvable)
				{
					System.out.println("FAIL");
				}
			}
			else if(algo.equals("DFS"))
			{
				// TODO Process this stuff
			}
			else if(algo.equals("SA"))
			{
				// TODO Process this stuff
			}
			else {
				System.out.println("FAIL");
			}

			sc.close();

		} catch (FileNotFoundException e) { }

	}

}

/**
 * A class to simply denote a point on the nursery grid.
 * Does not contain information about what is in that point.
 */
class NurseryGridPoint
{
	/** The location of this point on the grid. */
	private int x = -1, y = -1;

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

	/**
	 * Override the equals method of NurseryGridPoint, so that
	 * two NurseryGridPoints storing the same location are equal.
	 */
	@Override
	public boolean equals(Object o) {

		// If the object is compared with itself then return true  
		if (o == this) {
			return true;
		}

		/* Check if o is an instance of NurseryGridPoint or not
          "null instanceof [type]" also returns false */
		if (!(o instanceof NurseryGridPoint)) {
			return false;
		}

		// typecast o to NurseryGridPoint so that we can compare data members 
		NurseryGridPoint c = (NurseryGridPoint) o;

		// Compare the data members and return accordingly 
		return (this.x == c.x && this.y == c.y);
	}

	/**
	 * When overriding equals, you must also override hashcode to ensure that
	 * two 'equal' objects generate the same hashcode.
	 */
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}


	@Override
	public String toString()
	{
		return String.format("(%d, %d)", x, y);
	}

}

/**
 * Defines a basic state in the search tree.<br>
 * <br>
 * A NurseryNode basically contains a list of all the
 * available 'free' positions where a lizard can be placed.
 */
class NurseryNode
{	
	private List<NurseryGridPoint> availablePoints;

	/**
	 * @return the availablePoints
	 */
	public List<NurseryGridPoint> getAvailablePoints() {
		return availablePoints;
	}

	private int[][] nursery;

	/**
	 * @return the nursery
	 */
	public int[][] getNursery() {
		return nursery;
	}

	private int depth = 0;

	/**
	 * Create a blank NurseryState.
	 */
	NurseryNode()
	{
		// Initialize blank availablePoints
		availablePoints = new LinkedList<>();
	}

	/**
	 * Creates a state of available free positions given the input matrix.
	 * 
	 * @param nursery 0 indicates free position, 1 lizard and 2 tree.
	 * Ideally, input should not have anything but 0's and 2's.
	 */
	NurseryNode(int[][] nursery) {
		this(); // call default constructor

		this.nursery = nursery;

		for(int i = 0; i < nursery.length; i++)
		{
			for(int j = 0; j < nursery[0].length; j++)
			{
				if(nursery[i][j] == 0) {
					NurseryGridPoint pt = new NurseryGridPoint(i, j);
					availablePoints.add(pt);
				}
			}
		}
	}

	/**
	 * Copy constructor.
	 */
	NurseryNode(NurseryNode node)
	{
		this.depth = node.depth;
		this.availablePoints = new LinkedList<>(node.availablePoints);

		this.nursery = new int[node.nursery.length][];
		for(int i = 0; i < node.nursery.length; i++)
			this.nursery[i] = node.nursery[i].clone();
	}

	public String toString()
	{
		return String.format("depth = %d, availablePoints (%d) = %s",
				depth, availablePoints.size(), availablePoints);
	}
}