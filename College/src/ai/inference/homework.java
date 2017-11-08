/**
 * 
 */
package ai.inference;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class homework 
{

	/** If true, prints node information to the console. */
	public static final boolean DEBUG_MODE = true;
	
	/** No. of queries */
	private static int q;
	
	/** No. of sentences in input */
	private static int sIP;
	
	/** No. of sentences in KB */
	public static int sKB = 0;
	
	/** No. of variables - only for standardization */
	public static int v = 0;
	
	/**
	 * The knowledgebase is stored as key-value pair:<b>
	 * key: Predicate names<b>
	 * val: Sentences in which this predicate appears.
	 */
	private static Map<String, List<Sentence>> kb;
	
	private static final List<Predicate> queriesNegated = new ArrayList<>();
	
	public static String inputFileName = "input.txt";
	public static String outputFileName = "output.txt";

	// Time variables

	/** System.nanoTime() values. */
	private static long timeStart, timeCurrent;

	public static void main(String[] args)
	{
		Scanner in;

		timeStart = System.nanoTime();

		// Read contents of input file
		try 
		{
			
			// if input file specified
			if (args.length > 0) {
				inputFileName = args[0];

				if (inputFileName.toLowerCase().contains("input")) {
					// Use the same pattern for other files now
					outputFileName = inputFileName.toLowerCase().replace("input", "output");
					// resultFileName =
					// inputFileName.toLowerCase().replace("input", "result");
				}
			}

			in = new Scanner(new File(inputFileName));
			
			
			q = Integer.parseInt(in.nextLine().trim());
			
			// Parse the queries
			for(int i = 0; i < q; i++)
			{
				String queryStr = in.nextLine().trim();
				
				Predicate queryNegated = new Predicate(queryStr, true);
				queriesNegated.add(queryNegated);
				
				if(DEBUG_MODE)
					System.out.println(queryNegated);
			}
			
			sIP = Integer.parseInt(in.nextLine().trim());
	
			// Parse the sentences in KB
			kb = new HashMap<>();
			for(int i = 0; i < sIP; i++)
			{
				String sentenceStr = in.nextLine().trim();
				
				Sentence sentence = new Sentence(sentenceStr);
				
				// TODO add to KB
				for(Predicate p : sentence.predicates)
				{
					if(kb.containsKey(p.name))
					{
						List<Sentence> sentencesWithP = kb.get(p.name);
						sentencesWithP.add(sentence);
					}
					else {
						List<Sentence> sentencesWithP = new ArrayList<>();
						sentencesWithP.add(sentence);
						kb.put(p.name, sentencesWithP);
					}
				}
				
				if(DEBUG_MODE)
					System.out.println(sentence);
			}
			
			if(DEBUG_MODE) 
			{
				System.out.println("\nKnowledgebase:");
				int p = 1;
				for(Entry<String, List<Sentence>> e : kb.entrySet())
				{
					System.out.printf("%2d: %s ->\n", p++, e.getKey());
					List<Sentence> sentences = e.getValue();
					
					for(int s_i = 0; s_i < sentences.size()-1; s_i++)
					{
						System.out.println(sentences.get(s_i) + "; ");
					}
					System.out.println(sentences.get(sentences.size()-1));
					
					System.out.println();
				}
			}

			in.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		timeCurrent = System.nanoTime();

		System.out.println("\nCompleted in " +
				(TimeUnit.MILLISECONDS.convert(timeCurrent - timeStart, TimeUnit.NANOSECONDS) / 1000.0) + " seconds.");
	}

}

interface Argument
{
	
}

class Constant implements Argument
{
	public final String name;
	Constant(String name)
	{
		this.name = name;
	}
	@Override
	public String toString()
	{
		return name;
	}
}

class Variable implements Argument
{
	public final int id;
	
	Variable()
	{
		this.id = (homework.v)++;
	}
	
	@Override
	public String toString()
	{
		return "var"+id;
	}
}

class Predicate 
{
	public final String name;
	public final List<Argument> args;
	public final boolean negative;
	
	Predicate(String natural)
	{
		this(natural, false);
	}
	
	Predicate(String natural, boolean flip)
	{
		// Negative if (~ XOR flip) is true
		negative = (natural.charAt(0)=='~')^flip;

		if(natural.charAt(0)=='~')
		{
			natural = natural.substring(1);
		}
		
		// find bracket
		this.name = natural.substring(0, natural.indexOf("("));
		String argsString = natural.substring(natural.indexOf("(")+1, natural.indexOf(")"));
		
		String[] argString = argsString.split(",");
		
		args = new ArrayList<>();
		for(String arg : argString)
		{
			arg = arg.trim();
			
			// Handle variables, constants and whatnot
			if(Character.isUpperCase(arg.charAt(0)))
			{
				args.add(new Constant(arg));
			}
			else {
				args.add(new Variable());
			}
			
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if(negative)
			sb.append("~");
		sb.append(name);
		sb.append("(");
		for(int i = 0; i < args.size()-1; i++)
		{
			sb.append(args.get(i));
			sb.append(", ");
		}
		sb.append(args.get(args.size()-1));
		sb.append(")");
		
		return sb.toString();
	}
}

class Sentence 
{
	public final int id;
	
	public final List<Predicate> predicates;
	
	Sentence(String natural)
	{
		this.id = (homework.sKB)++;
		
		String[] predStrings = natural.split("\\|");
		predicates = new ArrayList<>();
		for(String predString : predStrings)
		{
			predicates.add(new Predicate(predString.trim()));
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		// sb.append(String.format("%2d: ", id));
		for(int i = 0; i < predicates.size()-1; i++)
		{
			sb.append(predicates.get(i));
			sb.append(" | ");
		}
		sb.append(predicates.get(predicates.size()-1));
		
		return sb.toString();
	}
}
