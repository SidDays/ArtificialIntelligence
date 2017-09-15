package competition.microsoft;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class Template {

	private static final String FILE_INPUT = "Template.txt";
	
	public static void main(String[] args) {

		Scanner sc;
		try {
			sc = new Scanner(new File(FILE_INPUT));

			List<String> lines = new ArrayList<>();

			while(sc.hasNextLine())
			{
				lines.add(sc.nextLine());
			}


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
