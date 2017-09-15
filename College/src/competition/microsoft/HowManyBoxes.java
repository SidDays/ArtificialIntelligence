package competition.microsoft;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class HowManyBoxes {

	private static final String FILE_INPUT = "HowManyBoxes.txt";

	public static void main(String[] args) {

		Scanner sc;
		try {
			sc = new Scanner(new File(FILE_INPUT));

			List<String> lines = new ArrayList<>();

			while(sc.hasNextLine())
			{
				lines.add(sc.nextLine());
			}

			/**
			 * X = The number of packages the company needs to attempt to fit and ship.
			 */
			int x = Integer.parseInt(lines.get(0).replace(";",""));

			/**
			 * Radius of the cylindrical box
			 */
			int r[] = new int[x];

			/**
			 * N = 3 for a triangular prism box and 4 for a rectangular prism box
			 */
			int n[] = new int[x];

			/**
			 * Length of each side of the base of the prism box
			 */
			int l[] = new int[x];
			
			// DO THE STUFF!! xoxo
						int boxesThatFit = 0;

			// Parse r, n, l input
			for(int i = 0; i < x; i ++)
			{
				String curLine = lines.get(i+1).replace(";","").trim(); // skip x wali line
				String rnl[] = curLine.split(",");
				r[i] = Integer.parseInt(rnl[0]);
				n[i] = Integer.parseInt(rnl[1]);
				l[i] = Integer.parseInt(rnl[2]);
				
				if(n[i] == 3) {
					// triangular prism
					
					if((double)r[i] >= l[i]*1.0/Math.sqrt(3))
					{
						boxesThatFit++;
					}
						
				}
				else if(n[i] == 4) {
					if((double)r[i] >= l[i]*1.0/Math.sqrt(2))
					{
						boxesThatFit++;
					}
				}
				else {
					
				}
			}
			
			System.out.print(boxesThatFit);


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
