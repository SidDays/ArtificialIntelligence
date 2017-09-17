package ai.lizardnursery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Siddhesh Karekar
 * AI Homework 1 - Lizard Nursery Problem
 * 
 * Problem Description:
 * 
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
public class homework {

	private static final String FILE_INPUT = "input.txt";
	private static final String FILE_OUTPUT = "output.txt";
	
	/**
	 * Each of the 3 algorithms automatically declares failure after this time elapses.
	 */
	private static final long TIMEOUT_MILLISECONDS = (long)(1000 * 60 * 4.75f);
	
	private static long timeStart, timeCurrent;

	/**
	 * If true, prints node information to the console.
	 * TODO Remember to turn it off before submission!
	 */
	private static final boolean DEBUG_MODE = true;

	/**
	 * The maximum number of iterations for Simulated Annealing.
	 */
	private static final int MAX_SA_TIME = Integer.MAX_VALUE;

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
	public static int p;

	/**
	 * Stores the initial nursery state that becomes the first node.
	 */
	private static int[][] nursery;

	/**
	 * Stores the locations of all the trees on the grid.
	 */
	private static List<NurseryGridPoint> treePoints;

	private static boolean isSolvable = false;

	/**
	 * Inserts a lizard in the given free spot on the grid for
	 * a node, and returns a new node with that configuration.
	 * 
	 * After the lizard is placed, also makes all spots in the lizard's LOS
	 * (and the lizard's position) unavailable.
	 * 
	 * @param node The parent node
	 * @param pointFree NurseryGridPoint that does not contain a lizard
	 * @return The new child node with the new configurations
	 */
	private static NurseryNode insertLizard(NurseryNode node, NurseryGridPoint pointFree)
	{
		NurseryNode newNode = new NurseryNode(node); // copy Constructor
		newNode.depth = node.depth + 1;

		List<NurseryGridPoint> availablePointsForThis = newNode.availablePoints;
		availablePointsForThis.remove(pointFree);

		// Place a lizard
		int xpos = pointFree.x;
		int ypos = pointFree.y;
		/*int[][] nursery = newNode.getNursery();
		nursery[xpos][ypos] = 1;*/

		newNode.lizardPoints = new ArrayList<>(node.lizardPoints);
		newNode.lizardPoints.add(new NurseryGridPoint(xpos, ypos));

		int xlos, ylos;

		// Propogate the lizard's LOS and mark those spaces as unavailable
		xlos = xpos; ylos = ypos;

		// Left side
		while(--xlos >= 0)
		{		
			if(nursery[xlos][ylos] == 2) // Break if it's a tree
				break;
			NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
			if(availablePointsForThis.remove(pointToRemove)) {
				// System.out.println(pointToRemove+" (left) removed from availablePoints.");
			}
		}
		xlos = xpos; ylos = ypos;

		// Right side
		while(++xlos < n)
		{		
			if(nursery[xlos][ylos] == 2)
				break;
			NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
			if(availablePointsForThis.remove(pointToRemove)) {
				// System.out.println(pointToRemove+" (right) removed from availablePoints.");
			}
		}
		xlos = xpos; ylos = ypos;

		// Top side
		while(++ylos < n)
		{		
			if(nursery[xlos][ylos] == 2) // Break if it's a tree
				break;
			NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
			if(availablePointsForThis.remove(pointToRemove)) {
				// System.out.println(pointToRemove+" (top) removed from availablePoints.");
			}
		}
		xlos = xpos; ylos = ypos;

		// Bottom side
		while(--ylos >= 0)
		{		
			if(nursery[xlos][ylos] == 2) // Break if it's a tree
				break;
			NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
			if(availablePointsForThis.remove(pointToRemove)) {
				// System.out.println(pointToRemove+" (bottom) removed from availablePoints.");
			}
		}
		xlos = xpos; ylos = ypos;

		// Top-left side
		while(--xlos >= 0 && ++ylos < n)
		{		
			if(nursery[xlos][ylos] == 2) // Break if it's a tree
				break;
			NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
			if(availablePointsForThis.remove(pointToRemove)) {
				// System.out.println(pointToRemove+" (top-left) removed from availablePoints.");
			}
		}
		xlos = xpos; ylos = ypos;

		// Top-right side
		while(++xlos < n && ++ylos < n)
		{		
			if(nursery[xlos][ylos] == 2) // Break if it's a tree
				break;
			NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
			if(availablePointsForThis.remove(pointToRemove)) {
				// System.out.println(pointToRemove+" (top-right) removed from availablePoints.");
			}
		}
		xlos = xpos; ylos = ypos;

		// Bottom-left side
		while(--xlos >= 0 && --ylos >= 0)
		{		
			if(nursery[xlos][ylos] == 2) // Break if it's a tree
				break;
			NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
			if(availablePointsForThis.remove(pointToRemove)) {
				// System.out.println(pointToRemove+" (bottom-left) removed from availablePoints.");
			}
		}
		xlos = xpos; ylos = ypos;

		// Bottom-right side
		while(++xlos < n && --ylos >= 0)
		{		
			if(nursery[xlos][ylos] == 2) // Break if it's a tree
				break;
			NurseryGridPoint pointToRemove = new NurseryGridPoint(xlos, ylos);
			if(availablePointsForThis.remove(pointToRemove)) {
				// System.out.println(pointToRemove+" (bottom-right) removed from availablePoints.");
			}
		}
		xlos = xpos; ylos = ypos;

		// Lizard has been placed, all blocked spots removed
		return newNode;
	}


	/**
	 * Updates the nursery matrix to a given configuration
	 * @param node
	 */
	private static void updateNurseryMatrix(NurseryNode node)
	{
		for(int i = 0; i < n; i++)
			for(int j = 0; j < n; j++)
				nursery[i][j] = 0;

		for(NurseryGridPoint pt : treePoints)
			nursery[pt.x][pt.y] = 2;

		for(NurseryGridPoint pt : node.lizardPoints)
			nursery[pt.x][pt.y] = 1;
	}

	/**
	 * Breadth-First Search
	 */
	private static void solveBFS()
	{
		/**
		 * Frontier for Breadth-First Search.
		 */
		Queue<NurseryNode> bfsQueue = new LinkedList<>();

		// create initial node
		NurseryNode initNode = new NurseryNode(nursery);
		bfsQueue.add(initNode);

		while(!bfsQueue.isEmpty())
		{
			NurseryNode nodeCurrent = bfsQueue.remove();

			if(DEBUG_MODE)
				System.out.println(nodeCurrent);

			// Goal-Test: number of lizards = depth of child node
			if(nodeCurrent.depth == p)
			{
				finishSuccess(nodeCurrent); // includes isSolvable

				break;
			}

			// Check if it's pointless to proceed: lizardsLeft > availablePoints
			int lizardsLeft = homework.p - nodeCurrent.depth;

			if(lizardsLeft > nodeCurrent.availablePoints.size())
			{
				// if(DEBUG_MODE) System.out.println("lizardsLeft > availablePoints. Skipping...");

			} else {

				// create child nodes for all the free available points.
				List<NurseryGridPoint> availablePointsCurrent = nodeCurrent.availablePoints;

				for(NurseryGridPoint pointFreeCurrent : availablePointsCurrent)
				{
					bfsQueue.add(insertLizard(nodeCurrent, pointFreeCurrent));
				}
			}
			
			if(timeOut()) {
				System.out.format("Timeout: %.3f seconds elapsed.\n",
						(TimeUnit.MILLISECONDS.convert(timeCurrent-timeStart, TimeUnit.NANOSECONDS)/1000.0));
				break;
			}
		}


		// Finished
		if(!isSolvable) {
			finishFailure();
		}
	}

	/**
	 * Depth-First Search
	 */
	private static void solveDFS()
	{
		Deque<NurseryNode> dfsStack = new LinkedList<>();

		// create initial node
		NurseryNode initNode = new NurseryNode(nursery);
		dfsStack.addFirst(initNode);

		while(!dfsStack.isEmpty())
		{
			NurseryNode nodeCurrent = dfsStack.removeFirst();

			if(DEBUG_MODE)
				System.out.println(nodeCurrent);

			// Goal-Test: number of lizards = depth of child node
			if(nodeCurrent.depth == p)
			{
				finishSuccess(nodeCurrent);

				break;
			}

			// Check if it's pointless to proceed: lizardsLeft > availablePoints
			int lizardsLeft = homework.p - nodeCurrent.depth;

			if (lizardsLeft > nodeCurrent.availablePoints.size()) {
				// if (DEBUG_MODE) System.out.println("lizardsLeft > availablePoints. Skipping...");

			} else {
				// create child nodes for all the free available points.
				List<NurseryGridPoint> availablePointsCurrent = nodeCurrent.availablePoints;

				for (NurseryGridPoint pointFreeCurrent : availablePointsCurrent) {
					dfsStack.addFirst(insertLizard(nodeCurrent, pointFreeCurrent));
				}
			}
			
			if(timeOut()) {
				System.out.format("Timeout: %.3f seconds elapsed.\n",
						(TimeUnit.MILLISECONDS.convert(timeCurrent-timeStart, TimeUnit.NANOSECONDS)/1000.0));
				break;
			}
		}

		// Finished
		if(!isSolvable) {
			finishFailure();
		}
	}

	/**
	 * Simulated annealing TODO
	 */
	private static void solveSA()
	{
		Random randomizer = new Random();

		// Uses the static nursery as its map
		NurseryNode node = new NurseryNode(nursery);
		
		// If no trees, p must be < n
		if(treePoints.size() > 0 || (treePoints.size() == 0 && p <= n)) {

			// If a solution is possible at all
			if(p <= node.availablePoints.size()) {

				// For the initial state, fill in p availablePoints randomly with lizards
				Collections.shuffle(node.availablePoints);
				for(int i = 0; i < p; i++)
				{
					// Important - make sure to change the nursery as well!
					NurseryGridPoint point = node.availablePoints.remove(0);
					node.lizardPoints.add(point);
					nursery[point.x][point.y] = 1;
				}

				// Compute its energy
				int energyCurrent = energyConflicts(node), energyNew, deltaE;
				double badAcceptProbability;

				// Start Simulated Annealing
				int time = 1;
				NurseryNode nodeNew = null;
				double temp = tempSchedule(time);

				while(temp > 0 || time < MAX_SA_TIME) // TODO add condition to break after 4:30 minutes!
				{
					if(DEBUG_MODE) System.out.println("\nIteration "+time);
					
					// Pick a random successor state TODO slightly modify the first one
					nodeNew = new NurseryNode(node);
					nodeNew.depth = node.depth + 1;

					NurseryGridPoint lizardToMove = nodeNew.lizardPoints.remove(randomizer.nextInt(p));
					if(DEBUG_MODE) System.out.println("Randomly picked lizard "+lizardToMove);

					NurseryGridPoint spotForMe = nodeNew.availablePoints
							.remove(randomizer.nextInt(nodeNew.availablePoints.size()));
					if(DEBUG_MODE) System.out.println("Randomly picked spot "+spotForMe);

					// Swap these
					nodeNew.lizardPoints.add(spotForMe);
					nodeNew.availablePoints.add(lizardToMove);

					energyNew = energyConflicts(nodeNew);

					if(energyNew == 0)
					{
						// Stop processing! Yay!
						finishSuccess(nodeNew);

						break;
					}
					else
					{
						deltaE = energyNew - energyCurrent;

						temp = tempSchedule(time);			

						if(DEBUG_MODE) System.out.format("deltaE = %d - %d = %d, T = %f\n", energyNew, energyCurrent, deltaE, temp);

						if(deltaE > 0) {
							badAcceptProbability = Math.exp(-(double)deltaE/temp);

							if(Math.random() < badAcceptProbability)
							{
								// accept it since probability satisfied
								if(DEBUG_MODE) System.out.format("Bad state accepted (%2.4f%%)\n", badAcceptProbability*100); 
								node = nodeNew;
								energyCurrent = energyNew;

								
							}
							else {
								// reject it

								if(DEBUG_MODE) System.out.format("Bad state rejected (%2.4f%%)\n", badAcceptProbability*100);
							}
						}
						else {
							// good! accept it anyway
							node = nodeNew;
							energyCurrent = energyNew;
							
							if(DEBUG_MODE) System.out.println("Good state accepted!");
						}
					}
					
					if(timeOut()) {
						System.out.format("Timeout: %.3f seconds elapsed.\n",
								(TimeUnit.MILLISECONDS.convert(timeCurrent-timeStart, TimeUnit.NANOSECONDS)/1000.0));
						finishFailure();
						break;
					}

					time++;
				}
			}
			else {
				if(DEBUG_MODE) System.out.format("Lizards (%d) more than availablePoints (%d).\n", p, node.availablePoints.size());
				finishFailure();
			}
		}
		else {
			if(DEBUG_MODE) System.out.println("No trees, but p > n.");
			finishFailure();
		}


	}

	/**
	 * Compute the energy for a given state in Simulated Annealing.
	 * 
	 * A measure of energy is the number of 'conflicting' lizards.
	 * 
	 * By default, computes the energy of the current nursery.
	 */
	private static int energyConflicts(NurseryNode node)
	{
		int conflictingLizards = 0;
		boolean thisLizardConflicts;

		for(NurseryGridPoint pointLizard : node.lizardPoints)
		{
			
			// Place a lizard
			int xpos = pointLizard.x;
			int ypos = pointLizard.y;
			thisLizardConflicts = false;

			int xlos, ylos;

			// Propogate the lizard's LOS and check for conflicts!
			xlos = xpos; ylos = ypos;
			

			// Left side
			while(--xlos >= 0)
			{		
				if(nursery[xlos][ylos] == 2) // Break if it's a tree
					break;
				NurseryGridPoint pointPotentialConflict = new NurseryGridPoint(xlos, ylos);
				if(node.lizardPoints.contains(pointPotentialConflict)) {
					conflictingLizards++; thisLizardConflicts = true;
					break;
				}
			}
			xlos = xpos; ylos = ypos;
			if(!thisLizardConflicts) {
				
				// Right side
				while(++xlos < n)
				{		
					if(nursery[xlos][ylos] == 2) // Break if it's a tree
						break;
					NurseryGridPoint pointPotentialConflict = new NurseryGridPoint(xlos, ylos);
					if(node.lizardPoints.contains(pointPotentialConflict)) {
						conflictingLizards++; thisLizardConflicts = true;
						break;
					}
				}
				
				xlos = xpos; ylos = ypos;
				if(!thisLizardConflicts) {
					
					// Top side
					while(++ylos < n)
					{		
						if(nursery[xlos][ylos] == 2) // Break if it's a tree
							break;
						NurseryGridPoint pointPotentialConflict = new NurseryGridPoint(xlos, ylos);
						if(node.lizardPoints.contains(pointPotentialConflict)) {
							conflictingLizards++; thisLizardConflicts = true;
							break;
						}
					}
					
					xlos = xpos; ylos = ypos;
					if(!thisLizardConflicts) {
						
						// Bottom side
						while(--ylos >= 0)
						{		
							if(nursery[xlos][ylos] == 2) // Break if it's a tree
								break;
							NurseryGridPoint pointPotentialConflict = new NurseryGridPoint(xlos, ylos);
							if(node.lizardPoints.contains(pointPotentialConflict)) {
								conflictingLizards++; thisLizardConflicts = true;
								break;
							}
						}
						
						xlos = xpos; ylos = ypos;
						if(!thisLizardConflicts) {
							
							// Top-left side
							while(--xlos >= 0 && ++ylos < n)
							{		
								if(nursery[xlos][ylos] == 2) // Break if it's a tree
									break;
								NurseryGridPoint pointPotentialConflict = new NurseryGridPoint(xlos, ylos);
								if(node.lizardPoints.contains(pointPotentialConflict)) {
									conflictingLizards++; thisLizardConflicts = true;
									break;
								}
							}
							
							xlos = xpos; ylos = ypos;
							if(!thisLizardConflicts) {
								
								// Top-right side
								while(++xlos < n && ++ylos < n)
								{		
									if(nursery[xlos][ylos] == 2) // Break if it's a tree
										break;
									NurseryGridPoint pointPotentialConflict = new NurseryGridPoint(xlos, ylos);
									if(node.lizardPoints.contains(pointPotentialConflict)) {
										conflictingLizards++; thisLizardConflicts = true;
										break;
									}
								}
								
								xlos = xpos; ylos = ypos;
								if(!thisLizardConflicts) {
									
									// Bottom-left side
									while(--xlos >= 0 && --ylos >= 0)
									{		
										if(nursery[xlos][ylos] == 2) // Break if it's a tree
											break;
										NurseryGridPoint pointPotentialConflict = new NurseryGridPoint(xlos, ylos);
										if(node.lizardPoints.contains(pointPotentialConflict)) {
											conflictingLizards++; thisLizardConflicts = true;
											break;
										}
									}
									
									xlos = xpos; ylos = ypos;
									if(!thisLizardConflicts) {
										
										// Bottom-right side
										while(++xlos < n && --ylos >= 0)
										{		
											if(nursery[xlos][ylos] == 2) // Break if it's a tree
												break;
											NurseryGridPoint pointPotentialConflict = new NurseryGridPoint(xlos, ylos);
											if(node.lizardPoints.contains(pointPotentialConflict)) {
												conflictingLizards++; thisLizardConflicts = true;
												break;
											}
										}
										
										xlos = xpos; ylos = ypos;
										if(!thisLizardConflicts) {
											// nothing
										}
										
									}
									
								}
								
							}
							
						}
						
					}
					
				}
			
			}
		}

		if (DEBUG_MODE) {
			// updateNurseryMatrix(node);
			// printMatrix(nursery);
			System.out.println(conflictingLizards + " conflicting lizard(s).");
		}

		return conflictingLizards;
	}

	/**
	 * The schedule function for simulated annealing.
	 * Default schedules are usually 1/log(n).
	 * @param time The number of iterations
	 * @return
	 */
	private static double tempSchedule(int time)
	{
		double cParam = 3.5, dParam = 1;
		return cParam/Math.log(time+dParam);
	}

	/**
	 * When a solution is found, update the nursery matrix to contain the solution.
	 * Then, print "OK" and the solution to the output file.
	 */
	private static void finishSuccess(NurseryNode nodeCurrent)
	{
		isSolvable = true;

		// Print solution to console as well as file
		PrintWriter writer = null;
		try{

			writer = new PrintWriter(FILE_OUTPUT, "UTF-8");

			System.out.println("OK");
			writer.println("OK");

			// Reconstruct solution
			updateNurseryMatrix(nodeCurrent);
			
			// Print solution
			printMatrix(nursery);
			writer.print(matrixAsString(nursery));


		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(writer != null)
				writer.close();
		}
	}
	
	/**
	 * Prints fail to OUTPUT.txt
	 */
	private static void finishFailure()
	{
		isSolvable = false;

		// Print solution to console as well as file
		PrintWriter writer = null;
		try{

			writer = new PrintWriter(FILE_OUTPUT, "UTF-8");

			System.out.println("FAIL");
			writer.println("FAIL");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(writer != null)
				writer.close();
		}
	}

	/**
	 * Prints a 2D matrix to console.
	 * @param m
	 */
	public static void printMatrix(int[][] m)
	{
		for(int i = 0; i < m.length; i++)
		{
			for(int j = 0;  j < m[0].length; j++)
				System.out.print(m[i][j]);
			System.out.println();
		}
	}

	/**
	 * Returns a String representation of a 2D matrix.
	 * @param m
	 * @return
	 */
	public static String matrixAsString(int[][] m)
	{
		StringBuffer sb = new StringBuffer();

		for(int i = 0; i < m.length; i++)
		{
			for(int j = 0;  j < m[0].length; j++)
				sb.append(m[i][j]);
			sb.append(System.lineSeparator());
		}

		return sb.toString();
	}
	
	/**
	 * Checks if the program is taking too long to execute
	 * @return true if TIMEOUT_MILLISECONDS has been overshot, false if not.
	 */
	private static boolean timeOut()
	{
		timeCurrent = System.nanoTime();
		
		if(TimeUnit.MILLISECONDS.convert(timeCurrent-timeStart, TimeUnit.NANOSECONDS) >= TIMEOUT_MILLISECONDS)
			return true;
		
		return false;
	}

	public static void main(String[] args) 
	{
		Scanner sc;

		timeStart = System.nanoTime();

		// Read contents of input file
		try
		{
			sc = new Scanner(new File(FILE_INPUT));

			algo = sc.nextLine().trim().toUpperCase();

			n = Integer.parseInt(sc.nextLine());
			nursery = new int[n][n];

			p = Integer.parseInt(sc.nextLine());

			// Read initial nursery layout
			// Add  trees to the tree points list.
			treePoints = new ArrayList<>();
			for(int i = 0; i < n; i++)
			{
				String row = sc.nextLine();
				for(int j = 0; j < n; j++)
				{
					nursery[i][j] = Integer.parseInt(String.valueOf(row.charAt(j)));

					if(nursery[i][j] == 2)
						treePoints.add(new NurseryGridPoint(i, j));
				}
			}		

			System.out.println(algo); // Print everything
			System.out.println(n);
			System.out.println(p);
			printMatrix(nursery);

			// Run the algorithm
			if(algo.equals("BFS"))
			{
				solveBFS();
			}
			else if(algo.equals("DFS"))
			{
				solveDFS();
			}
			else if(algo.equals("SA"))
			{
				solveSA();
			}
			else {
				finishFailure();
			}

			sc.close();

		} catch (FileNotFoundException e) { }

		timeCurrent = System.nanoTime();

		System.out.println("\nCompleted in " +
				(TimeUnit.MILLISECONDS.convert(timeCurrent - timeStart, TimeUnit.NANOSECONDS) / 1000.0) + " seconds.");

	}

}

/**
 * A class to simply denote a point on the nursery grid.
 * Does not contain information about what is in that point.
 */
class NurseryGridPoint
{
	/** The location of this point on the grid. */
	public int x = -1, y = -1;

	public NurseryGridPoint(int x, int y)
	{
		this.x = x;
		this.y = y;
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
 * Defines a node containing a basic state in the search tree.<br>
 * <br>
 * A NurseryNode basically contains a list of all the
 * available 'free' positions where a lizard can be placed.
 */
class NurseryNode
{	
	public List<NurseryGridPoint> availablePoints;

	public List<NurseryGridPoint> lizardPoints; // lmao it's public :(

	/**
	 * Depth of the search tree. Also indicates the number of lizards, and can
	 * be used as a goal test.
	 */
	public int depth = 0;

	/**
	 * Create a blank NurseryState.
	 */
	public NurseryNode()
	{
		// Initialize blank availablePoints
		availablePoints = new ArrayList<>();

		// New node contains no lizards
		lizardPoints = new ArrayList<>();
	}

	/**
	 * Creates a state of available free positions given the input matrix.
	 * 
	 * @param nurseryParam 0 indicates free position, 1 lizard and 2 tree.
	 * Ideally, input should not have anything but 0's and 2's.
	 */
	public NurseryNode(int[][] nurseryParam) {
		this(); // call default constructor

		// this.nursery = nursery;

		for(int i = 0; i < nurseryParam.length; i++)
		{
			for(int j = 0; j < nurseryParam[0].length; j++)
			{
				if(nurseryParam[i][j] == 0) {
					NurseryGridPoint pt = new NurseryGridPoint(i, j);
					availablePoints.add(pt);
				}
				else if(nurseryParam[i][j] == 1) {
					NurseryGridPoint pt = new NurseryGridPoint(i, j);
					lizardPoints.add(pt);
				}
			}
		}
	}

	/**
	 * Copy constructor.
	 */
	public NurseryNode(NurseryNode node)
	{
		this.depth = node.depth;
		this.availablePoints = new LinkedList<>(node.availablePoints);
		this.lizardPoints = new LinkedList<>(node.lizardPoints);
	}
	
	@Override
	public String toString()
	{

		// With spaces
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < depth; i++)
			sb.append(" ");
		sb.append(String.format("%d/%d: %d left, %d available",
				depth, homework.p, (homework.p-lizardPoints.size()), availablePoints.size()));

		return sb.toString();

		// Without spaces

		/*return String.format("depth = %d/%d, %d availablePoints",
				depth, homework.p, availablePoints.size());
		 */
	}
}
