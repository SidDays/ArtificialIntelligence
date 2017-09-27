package competition.hackerrank;

import java.util.Scanner;

/**
 * Isn't working as of 09/26/2017 8:56 PM.
 */
public class TheGridSearch {

	
    public static void main(String[] args)
    {
    	
    	boolean cm = false;
    	
        Scanner in = new Scanner(System.in);
        int t = in.nextInt();
        
        for(int a0 = 0; a0 < t; a0++)
        {
            int R = in.nextInt();
            int C = in.nextInt();
            String[] G = new String[R];
            for(int G_i=0; G_i < R; G_i++){
                G[G_i] = in.next();
            }
            int r = in.nextInt();
            int c = in.nextInt();
            String[] P = new String[r];
            for(int P_i=0; P_i < r; P_i++){
                P[P_i] = in.next();
            }
            
            boolean found = false, finding = false, impossible = false;
            
            int iStart = -1, jStart = -1;
            
            for(int i = 0; i < R && !found && !impossible; i++)
            {
            	for(int j = 0; j < C && !found && !impossible; j++)
            	{
            		if(cm) System.out.format("\nFinding = %c | ",(finding)?'T':'F');
            		if(!finding)
            		{
            			if(cm) System.out.format("%c (%d, %d) vs %c (%d, %d)\n", G[i].charAt(j), i, j,
            					P[0].charAt(0), 0, 0);
            		}
            		
            		if(!finding && ((i > R-r+1) ))
            			impossible = true;
            		
            		if(!finding && G[i].charAt(j) == P[0].charAt(0))
            		{
            			if(j+c-1 <= C) {
            				if(cm) System.out.format("START FINDING!\n", G[i].charAt(j), i, j,
            					P[0].charAt(0), 0, 0);
            			
            			finding = true;
            			iStart = i;
            			jStart = j;
            			}
            			else {
            				if(cm) System.out.println("can't start cehcking : too few columns...");
            			}
            		}
            		
            		if(finding)
            		{
            			if((j >= jStart && j-jStart < c) && (i >= iStart && i-iStart <r ))
            			{
            				
            				if(cm) System.out.format("%c (%d, %d) vs %c (%d, %d)\n", G[i].charAt(j), i, j, 
            						P[i-iStart].charAt(j-jStart), i-iStart, j-jStart);

            				if(G[i].charAt(j) == P[i-iStart].charAt(j-jStart))
            				{
            					//

            					// if last node
            					if(i-iStart == r-1 && j-jStart == c-1)
            					{
            						if(cm) System.out.println("SOLUTION FOUND!");
            						found = true;
            						
            					}
            				}
            				else
            				{
            					iStart = jStart = -1;
            					if(cm) System.out.println("lost track!!");
            					finding = false;
            				}
            			}
            		}
            	}
            	
            	if(cm) System.out.println("----");
            }
            
            System.out.println(found?"YES":"NO");
            
        }
        
        in.close();
        
        
    }
}
