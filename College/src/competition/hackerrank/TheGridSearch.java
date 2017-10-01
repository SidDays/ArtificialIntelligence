package competition.hackerrank;

import java.util.Scanner;

/**
 * Isn't working as of 09/26/2017 8:56 PM.
 */
public class TheGridSearch {


	public static void main(String[] args)
	{

		boolean debug = false;

		Scanner in = new Scanner(System.in);
		int t = in.nextInt();

		for(int a0 = 0; a0 < t; a0++)
		{
			int R = in.nextInt();
			int C = in.nextInt();
			String[] grid = new String[R];
			for(int G_i=0; G_i < R; G_i++){
				grid[G_i] = in.next();
			}
			int r = in.nextInt();
			int c = in.nextInt();
			String[] pattern = new String[r];
			for(int P_i=0; P_i < r; P_i++){
				pattern[P_i] = in.next();
			}

			boolean found = false, 
					possibleSoln = false, 
					impossible = false;

			/** Starting position of a possible solution. */
			int iStart = -1, jStart = -1;


			for(int i = 0; i < R && (!found && !impossible); i++)
			{
				for(int j = 0; j < C && (!found && !impossible); j++)
				{
					if(!possibleSoln)
					{
						// Check if row bound exceeded
						if(i+r-1 > R)
						{
							impossible = true;

							if(debug) System.out.println("Solution not possible: row bound exceeded.");
						}

						// Check if solution can start, and that many columns are available
						else if(grid[i].charAt(j) == pattern[0].charAt(0) && (j+c-1) <= C)
						{
							possibleSoln = true;
							iStart = i;
							jStart = j;

							// You should be able to return to the next iteration!

							if(debug) {
								System.out.format("\nG(%d, %d) = %c, P(%d, %d) = %c | Started checking pattern\n", 
										i, j, grid[i].charAt(j), 
										0, 0, pattern[0].charAt(0));
							}
						}

						else {


							if(debug) System.out.format("G(%d, %d) = %c | No effect\n", i, j, grid[i].charAt(j));
						}
					}

					// If solution checking is in progress
					else
					{
						// Skip the starting columns without the pattern
						if((j >= jStart && j-jStart < c) && (i >= iStart && i-iStart < r))
						{

							// check if pattern is satisfied
							if(grid[i].charAt(j) == pattern[i-iStart].charAt(j-jStart))
							{
								if(debug) {
									System.out.format("G(%d, %d) = %c, P(%d, %d) = %c | Pattern continues...\n", 
											i, j, grid[i].charAt(j), 
											i-iStart, j-jStart, pattern[i-iStart].charAt(j-jStart));
								}
								
								if(i-iStart == r-1 && j-jStart == c-1)
								{
									if(debug) System.out.println("Solution found!");
									found = true;
									break;
								}
							}
							// if pattern is broken
							else {
								possibleSoln = false;

								// BACKTRACK! This works because the for loop will increment its value
								i = iStart;
								j = jStart;
								
								if(debug) {
									System.out.format("G(%d, %d) = %c, P(%d, %d) = %c | Pattern broken!\n", 
											i, j, grid[i].charAt(j), 
											i-iStart, j-jStart, pattern[i-iStart].charAt(j-jStart));
								}
							}

						}
						else {
							if(debug) System.out.format("G(%d, %d) = %c | Skipping this value\n", i, j, grid[i].charAt(j));
						}
					}
				}
			}

			System.out.println(found?"YES":"NO");

		}

		in.close();


	}
}
