/**
 * 
 */
package competition.microsoft;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 */
public class MatrixTranspose {

	private static final String FILE_INPUT = "PracticeInput.txt";

	private static int[][] matrixTranspose(int[][] matrix)
	{

		int[][] solution = null;

		int m = matrix.length;
		int n = matrix[0].length;
		solution = new int[n][m];

		for(int i = 0; i < n; i ++)
		{
			for(int j = 0; j < m; j++)
			{
				solution[i][j] = matrix[j][i];
			}
		}

		return solution;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Scanner sc;
		try {
			sc = new Scanner(new File(FILE_INPUT));

			List<String> lines = new ArrayList<>();

			while(sc.hasNextLine())
			{
				lines.add(sc.nextLine());
			}

			// parse it-
			int m = lines.size(); // no. of rows

			
			int n = lines.get(0).split(" ").length;
			int[][] inputMatrix = new int[m][n];

			for(int i = 0; i < m; i++)
			{
				String[] numbers = lines.get(i).split(" ");
				for(int j = 0; j < n; j++)
				{
					inputMatrix[i][j] = Integer.parseInt(numbers[j]);
				}
				
			}
			
			int[][] transposeMatrix = matrixTranspose(inputMatrix);
			
			for(int i = 0; i < n; i++)
			{
				for(int j = 0; j < m; j++)
				{
					System.out.print(transposeMatrix[i][j]);
					if(j < m-1)
						System.out.print(" ");
				}
				System.out.println();
			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}



	}

}
