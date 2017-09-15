// TODO This program is INCOMPLETE

package competition.microsoft;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Node {
	List<String> slots;
	Node()
	{
		slots = new ArrayList<>();
	}
	
	public void add(String s)
	{
		slots.add(s);
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append("[\n");
		for(String line : slots)
		{
			sb.append(line+"\n");
		}
		sb.append("]");
		
		return sb.toString();
	}
}

public class CapGown {

	private static final String FILE_INPUT = "capgown.txt";
	
	
	
	@SuppressWarnings("unused") // TODO remove
	public static void main(String[] args) {

		Scanner sc;
		try {
			sc = new Scanner(new File(FILE_INPUT));

			List<String> lines = new ArrayList<>();

			while(sc.hasNextLine())
			{
				lines.add(sc.nextLine());
			}
			
			
			
			
			// TODO
			/////////////////////////////////////////
			
			String header = lines.get(0);
			
			/**
			 * Number of output node
			 */
			int noOfSlotsPerNode = Integer.parseInt(header.substring(0, header.indexOf(":")));
			int noOfInputNodes = 0;
			boolean preserveEmpty;
			
			String preserveEmptyString = header.substring(header.indexOf(":")+1);
			if(preserveEmptyString.equalsIgnoreCase("false"))
				preserveEmpty = true;
			else
				preserveEmpty = false;
			
			// for the rest of the lines
			List<Node> nodes = new ArrayList<>();
			
			boolean outsideList = false;
			int currentLineIndex = 2;
			
			boolean insideNode = false;
			Node currentNode = null;
			
			int emptySlotInputCount = 0;
			int emptySlotOutputCount = 0;
			
			List<String> slots = new ArrayList<>();
			while(!outsideList)
			{
				String currentLine = lines.get(currentLineIndex).trim();
				//System.out.println(currentLine);
				
				if(!insideNode && currentLine.equals("["))
				{
					// new node has started
					insideNode = true;
					currentNode = new Node();
				}
				else if(insideNode && currentLine.equals("]"))
				{
					insideNode = false;
					// System.out.println("Added node:\n"+currentNode);
				}
				else if(!insideNode && currentLine.equals("]"))
				{
					// finished
					outsideList = true;
					// break;
				}
				else if(insideNode)
				{
					// its a slot inside a node!!
					if(currentLine.isEmpty()) {
						emptySlotInputCount++;
						
						if(preserveEmpty)
						{
							slots.add(currentLine);
						}
					}
					else
						slots.add(currentLine);
					
				}
				currentLineIndex++;
			}
			
			// done?!?
			System.out.println("all slots:\n"+slots);
			
			// Node newNode = new Node();
			for(int i = 0; i < slots.size(); i++)
			{
				if(i%3 == 0)
				{
					// newNode = new Node(); TODO
					
				}
				
			}
			
			int noOfOutputNodes = nodes.size();
			
			System.out.println(noOfOutputNodes);
			System.out.println(noOfOutputNodes - noOfInputNodes);
			System.out.println(emptySlotOutputCount - emptySlotInputCount);
			System.out.println(emptySlotOutputCount);
			
			// print list of nodes
			System.out.println("[");
			for(Node n : nodes)
				System.out.println(n);
			System.out.println("]");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
