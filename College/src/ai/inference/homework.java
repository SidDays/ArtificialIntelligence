/**
 * 
 */
package ai.inference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class homework 
{

	/** If true, prints information to the console. */
	public static final boolean DEBUG = true;
	
	/** No. of queries */
	private static int q;
	
	/** No. of sentences in input */
	private static int sIP;
	
	/** No. of sentences in KB */
	public static int sKB = 0;
	
	/** No. of variables - only for standardization */
	public static int v = 0;

	/**
	 * The step number of resolution. A possible optimization includes not
	 * attempting to resolve any statements that have been processed earlier,
	 * i.e. only resolving all sentences in the latest iteration 'n' with
	 * previous ones.
	 */
	public static int steps = 0;
	
	/**
	 * Initialized to false at the beginning of every query. Make this true if
	 * the query is proved by resolution (i.e., when the empty clause is
	 * generated.)
	 */
	private static boolean emptyClause;
	
	/**
	 * <p>
	 * The knowledgebase is stored as key-value pair:
	 * <ul>
	 * <li>key: Predicate names
	 * <li>val: Sentences in which this predicate appears.
	 * </ul>
	 */
	private static Map<String, List<Sentence>> kb;
	
	// private static List<Sentence> kbSentences;
	
	private static final List<Predicate> queriesNegated = new ArrayList<>();
	
	public static String inputFileName = "input.txt";
	public static String outputFileName = "output.txt";

	// Time variables

	/** System.nanoTime() values. */
	private static long timeStart, timeCurrent;

	/**
	 * @param in
	 */
	private static void parseInput(Scanner in) {
		
		q = Integer.parseInt(in.nextLine().trim());
		
		// Parse the queries
		if(DEBUG) System.out.println("Parsing query predicates (flipping them automatically)...");
		for(int i = 0; i < q; i++)
		{
			String queryStr = in.nextLine().trim();
			
			Predicate queryNegated = new Predicate(queryStr, new HashMap<String, Variable>(), true);
			queriesNegated.add(queryNegated);
			
			if(DEBUG)
				System.out.printf("%2d: %s\n", i, queryNegated);
		}
		
		sIP = Integer.parseInt(in.nextLine().trim());
		
		// Parse the sentences in KB
		
		if(DEBUG)
			System.out.println("Parsing sentences into knowledgebase...");
		
		kb = new HashMap<>();
		
		// kbSentences = new ArrayList<>();
		
		for(int i = 0; i < sIP; i++)
		{
			String sentenceStr = in.nextLine().trim();
			
			Sentence sentence = new Sentence(sentenceStr);
			
			if(DEBUG)
				System.out.printf("Current sentence: %s\n", sentence);
			
			// TODO [?] Discard sentences with two contradicting predicates
			
			putInKB(sentence);

		}
		
		if(DEBUG) 
		{
			System.out.println("\nKnowledgebase:");
			int p = 1;
			for(Entry<String, List<Sentence>> e : kb.entrySet())
			{
				System.out.printf("%2d: Sentences with [%s]\n", p++, e.getKey());
				List<Sentence> sentences = e.getValue();
				
				for(int s_i = 0; s_i < sentences.size()-1; s_i++)
				{
					System.out.println(sentences.get(s_i) + "; ");
				}
				System.out.println(sentences.get(sentences.size()-1));
				
				System.out.println();
			}
		}
	}


	/**
	 * Adding sentence to KB, i.e. adding sentence to the sentence-list of every
	 * predicate it contains
	 * 
	 * @param sentence
	 */
	private static void putInKB(Sentence sentence) 
	{
		for (Predicate p : sentence.predicates)
		{
			String signedName = p.signedName(false);
			
			/*if(DEBUG)
				System.out.println("Signed Predicate: "+signedName);*/
			
			if(kb.containsKey(signedName))
			{
				List<Sentence> sentencesWithP = kb.get(signedName);
				
				// System.out.println("Hash return: "+sentencesWithP);
				
				if(!sentencesWithP.contains(sentence))
					sentencesWithP.add(sentence);
			}
			else {
				List<Sentence> sentencesWithP = new ArrayList<>();
				sentencesWithP.add(sentence);
				kb.put(signedName, sentencesWithP);
			}
		}
	}
	

	private static Map<Variable, Constant> unify(Predicate p1, Predicate p2)
	{
		if(DEBUG)
		{
			System.out.printf("Attempting to unify %s and %s.\n", p1, p2);
		}
		
		if(!p1.name.equals(p2.name))
		{
			System.err.printf("Attempted to unify unidentical predicates: %s vs %s\n", p1.name, p2.name);
			return null;
		}
		
		Map<Variable, Constant> substitution = new HashMap<>();
		
		boolean valid = true;
		
		if(!p1.args.equals(p2.args))
		{
			List<Argument> p1_args = new ArrayList<>(p1.args);
			List<Argument> p2_args = new ArrayList<>(p2.args);
			
			List<List<Variable>> commonSubstitutedVariables = new ArrayList<>();
			
			for(int i = 0; i < p1_args.size() && valid; i++)
			{
				Argument p1_i = p1_args.get(i);
				Argument p2_i = p2_args.get(i);
				
				if(p1_i instanceof Constant && p2_i instanceof Constant)
				{
					Constant p1_c = (Constant) p1_i;
					Constant p2_c = (Constant) p2_i;
					
					if(!p1_c.equals(p2_c))
					{
						if(DEBUG)
						{
							System.out.printf("Substitution invalid: %s != %s\n", p1_c, p2_c);
						}
						valid = false;
					}
				}
				
				if(p1_i instanceof Variable && p2_i instanceof Constant)
				{
					Variable p1_v = (Variable) p1_i;
					Constant p2_c = (Constant) p2_i;
					
					// [?] Add substitution to the substitution list
					if(substitution.get(p1_v) != null && !substitution.get(p1_v).equals(p2_c))
					{
						if(DEBUG)
						{
							System.out.printf("Substitution invalid: %s already unified with different constant\n",
									p1_v);
						}
						valid = false;
					} 
					else {
						substitution.put(p1_v, p2_c);
					}
				}
				
				// COMBINE THESE
				else if (p2_i instanceof Variable && p1_i instanceof Constant)
				{
					Variable p2_v = (Variable) p2_i;
					Constant p1_c = (Constant) p1_i;
					
					// [?] Add substitution to the substitution list
					if(substitution.get(p2_v) != null && !substitution.get(p2_v).equals(p1_c))
					{
						if(DEBUG)
						{
							System.out.printf("Substitution invalid: %s already unified with different constant\n",
									p2_v);
						}
						valid = false;
					} 
					else {
						substitution.put(p2_v, p1_c);
					}

				}
				
				else if (p1_i instanceof Variable && p2_i instanceof Variable)
				{
					/*
					 * if both of the unification thingies are variables, you
					 * need to keep track of them - at the end, check whether
					 * they were properly unified to the same value. If not, the
					 * substitution is invalid.
					 */
					
					Variable p1_v = (Variable)(p1_i);
					Variable p2_v = (Variable)(p2_i);
					
					if(DEBUG) System.out.format("Will attempt to unify variables %s and %s.\n", p1_v, p2_v);

					List<Variable> thisOne = null;
					
					// If either of the variables already shares a substitution
					for(List<Variable> l : commonSubstitutedVariables)
					{
						if(l.contains(p1_v) || l.contains(p2_v))
						{
							thisOne = l;
						}
					}
					
					// If not, make a new set of common substitutions
					if(thisOne == null)
						thisOne = new ArrayList<>();
					
					// Either way, add these two variables to it.
					thisOne.add(p1_v);
					thisOne.add(p2_v);
					
					// Now add this list to the common ones
					commonSubstitutedVariables.add(thisOne);
					
				}
				
				if(DEBUG)
				{
					System.out.printf("Substitution so far: %s\n", substitution);
				}

			}
			
			// Check if the variable common substitutions are satisfied
			for(List<Variable> l : commonSubstitutedVariables)
			{
				Variable var0 = l.get(0);
				
				Constant commonValue = substitution.get(var0);
				
				// If all of these variables do NOT share the same mapping - invalid
				if(commonValue == null)
				{
					valid = false;
				}
				else
				{
					for(int i = 1; i < l.size(); i++)
					{
						Constant otherValue = substitution.get(l.get(i));
						if(otherValue == null || !otherValue.equals(commonValue))
						{
							valid = false;
							break;
						}
					}
					
					// Thus, they do share the same value.
					// It might be a single value, OR no value at all!
					if(DEBUG)
					{
						System.out.printf("Thus, the variables ");
						for(int i = 0; i < l.size(); i++)
						{
							System.out.printf("%s", l.get(i));
							if(i < l.size()-1)
								System.out.printf(", ");
						}
						System.out.println(" can be unified.");
					}
				}
			}
			
			// If invalid, clear the substitution - these cannot be unified.
			if(!valid) {
				substitution.clear();
				substitution = null;
				
				if(DEBUG) System.out.println("Substitution is nulled, was invalid.");
			}
		}
		
		return substitution;
	}

	/**
	 * <p>
	 * Creates a new sentence by substituting variables with constants as
	 * provided.
	 * <p>
	 * The substitution should already be valid.
	 * 
	 * @param sent Old sentence that won't be modified.
	 * @param substitution
	 * @return Substituted sentence
	 */
	private static Sentence substitute(Sentence sent, Map<Variable, Constant> substitution)
	{
		Sentence sentNew = new Sentence(sent, false);
		
		for(Entry<Variable, Constant> e : substitution.entrySet())
		{
			sentNew.substituteSingle(e.getKey(), e.getValue());
		}
		
		return sentNew;
	}


	/**
	 * <p>
	 * Tries to resolve two sentences, s1 and s2. Compares every predicate in
	 * both of those sentences, and tries to unify them.
	 * <p>
	 * A valid result will be <b>put into the KB.</b>
	 * 
	 * @param s1
	 *            First sentence
	 * @param s2
	 *            Second sentence
	 * @return
	 *         <p>
	 * 		<code>substitution</code> Map if the sentences can be unified,
	 *         <code>null</code> if the sentences cannot.
	 *         <p>
	 * 		An empty <code>substitution</code> Map indicates unification is
	 *         possible without substitution (i.e. variable-variable
	 *         unification).
	 */
	private static Sentence resolve(Sentence s1, Sentence s2)
	{
		if (DEBUG) 
		{
			System.out.printf("\nSTEP %d - Resolve [%s] vs [%s]\n"
					+ "-----------------------------------------------------------------------\n", steps, s1, s2);
		}
		
		for(int i = 0; i < s1.predicates.size() && !emptyClause; i++)
		{
			Predicate p1 = s1.predicates.get(i);
			// if (DEBUG) System.out.printf("s1 = [%s], p1.%c = %s\n", s1, ('a'+i), p1);
			
			for(int j = 0; j < s2.predicates.size() && !emptyClause; j++)
			{
				Predicate p2 = s2.predicates.get(j);
				// if (DEBUG) System.out.printf("s2 = [%s], p2.%c = %s\n", s2, ('a'+j), p2);

				if(p1.mightUnify(p2))
				{
					// TODO Unify variables
					
					if(DEBUG)
						System.out.printf("%s might unify with %s.\n", p1, p2);

					Map<Variable, Constant> substitution = unify(p1, p2);

					Sentence s1_New = s1, s2_New = s2;

					/*
					 * Substitution might be empty even after a valid resolution
					 * ~A(Lion) vs A(Lion), so check for nullness instead.
					 */
					if(substitution != null)
					{
						s1_New = substitute(s1, substitution);
						s2_New = substitute(s2, substitution); 
						
						Sentence sResolved = new Sentence(s1_New, s2_New);
						
						// if(DEBUG) System.out.println("Resolved to: "+sResolved);

						if(sResolved.predicates.isEmpty())
						{
							if(DEBUG) System.out.printf("Empty clause formed after resolving [%s] vs [%s].\n", s1, s2);

							emptyClause = true;
						}
						else 
						{
							putInKB(sResolved);

							if(DEBUG) { 
								System.out.printf(
										"Valid sentence formed after resolving \n\t   [%s] \n\tvs [%s] \n\t = [%s]\n\n",
										s1, s2, sResolved);
							}
						}

						return sResolved;
					}
					
					else {
						
						if(DEBUG)
							System.out.printf("Can't resolve [%s] vs [%s]; no valid substitution.\n", s1, s2);
						return null;
						
					}
				}
			}
		}

		/* If both for loops are exited, no predicates might unify */
		
		if(DEBUG)
			System.out.printf("Can't resolve [%s] vs [%s]; Possibly incompatible predicates.\n", s1, s2);
		
		return null;
	}


	/**
	 * <p>
	 * TODO The way the function is currently designed, there MUST be a path to
	 * the bottom using the query itself (without requiring additional things
	 * like AND introduction.)
	 * <p>
	 * Currently does not check for loops by checking if the resolution pairs
	 * already exist.
	 * 
	 * @param q_i
	 */
	private static boolean resolveQueryNegated(int q_i) 
	{
		Predicate queryNegated = queriesNegated.get(q_i);
		if(DEBUG) System.out.printf("Query %d: %s\n===== == ===============\n", q_i+1, queryNegated);
		
		emptyClause = false;

		/*
		 * TODO Restore the KB backup!
		 * 
		 * Maybe just delete all sentences with steps > 0? (1 is the old query,
		 * > 1 the derived ones)
		 */
		steps = 1;

		Sentence queryNegatedSentence = new Sentence(queryNegated);
		putInKB(queryNegatedSentence);
		
		List<Entry<Sentence, Sentence>> pairsToResolve = new ArrayList<>();
		List<Entry<Sentence, Sentence>> pairsResolved = new ArrayList<>();
		
		// Get all the sentences with new sentence query's negation
		List<Sentence> resolvents = kb.get(queryNegated.signedName(true));
		
		if(DEBUG) System.out.println("Initial sentences to consider: "+resolvents);

		// Initial resolvent database, NullPointerException if no resolvents
		if(resolvents == null)
		{
			System.out.println("There's nothing to unify the query with at all.");
			return false;
		}
		
		// I got something to resolve with!
		for(Sentence s : resolvents)
		{
			Entry<Sentence, Sentence> pairWithQuery = new AbstractMap.SimpleEntry<>(queryNegatedSentence, s);
			pairsToResolve.add(pairWithQuery);
		}
		
		while(!pairsToResolve.isEmpty() && !emptyClause)
		{
			// Attempt to resolve one pair, then add its result to the KB
			
			Entry<Sentence, Sentence> pair = pairsToResolve.remove(0);
			Sentence s1 = pair.getKey();
			Sentence s2 = pair.getValue();
			
			steps++;
			
			// Resolve automatically adds new valid sentences to the KB
			Sentence sNew = resolve(s1, s2);
			
			pairsResolved.add(pair);

			// If it was possible to actually resolve them
			if(sNew != null)
			{

				if(emptyClause) 
				{
					break;
				}
				else {

					// TODO Create new resolvents using the newly generated sentence
					// CHECK if this sentence/pair repeats

					if(DEBUG)
						System.out.println("Creating new resolvents using the newly generated sentence.");

					for(Predicate p : sNew.predicates)
					{
						resolvents = kb.get(p.signedName(true));
						if(DEBUG) System.out.printf("From [%s], %s can resolve with: %s\n", sNew, p, resolvents);

						for(Sentence s : resolvents)
						{
							Entry<Sentence, Sentence> pair2 = new AbstractMap.SimpleEntry<>(sNew, s);
							pairsToResolve.add(pair2);
						}
					}

				}

			}

			// System.out.println();


			
		}
		
		return emptyClause;
	}


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
	
			parseInput(in);
			
			PrintWriter writerOutput = null;
			try 
			{

				writerOutput = new PrintWriter(outputFileName, "UTF-8");

				// For each query
				for(int q_i = 0; q_i < q; q_i++)
				{
					boolean result = resolveQueryNegated(q_i);
					
					String resultString = (result)?"TRUE":"FALSE";
					
					System.out.printf("\nWriting to %s: %s\n", outputFileName, resultString);
					writerOutput.println(resultString);
				}
				
				/*Predicate p1a = new Predicate("mother(x, x, Alice)");
				Predicate p2a = new Predicate("mother(Alice, y, y)");
				
				Map<Variable, Constant> mappy = unify(p1a, p2a);
				
				System.out.println("Substitution is: "+mappy);
				
				Sentence senti = new Sentence(p1a);
				System.out.println(senti);
				System.out.println(substitute(senti, mappy));*/
				
				// Before hardcode testing, make sure to disable emptyClause
				emptyClause = false;
				System.out.println(resolve(
						new Sentence("mother(x, x, Alice) | dad(x)"), 
						new Sentence("~mother(Alice, y, Alice)")));
				
				/*Sentence s5 = new Sentence("Mellow(a, b, Popat) | WTF(John, gorg, b) | Mellow(Acid, e, a, e, b)");
				System.out.println(s5.toCommonString());
				Sentence s6a = new Sentence("WTF(John, gorg, x) | Mellow(a, x, Popat) | Mellow(Acid, e, a, e, x)");
				System.out.println(s6a.toCommonString());
				Sentence s6b = new Sentence("WTF(John, gorg, b) | Mellow(a, b, Carnegie) | Mellow(Acid, e, a, e, b)");
				System.out.println(s6b.toCommonString());
				
				System.out.printf("s5 == s6? %s,\ns5 == s6b? %s,\ns6a == s6b? %s\n", s5.equals(s6a), s5.equals(s6b), s6a.equals(s6b));
				 */
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
			finally {
				if (writerOutput != null)
					writerOutput.close();
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

/**
 * A superclass to allow arguments and predicates (to unify)? together
 */
interface Argument
{
	
}

/**
 * e.g. West, Nono, M1
 */
class Constant implements Argument
{
	public final String name;
	Constant(String name)
	{
		this.name = name;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) {
	        return false;
	    }
		
	    if (!Constant.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
	    
	    final Constant other = (Constant) obj;
	    
	    if(this.name == null || other.name == null || !this.name.equals(other.name))
	    	return false;
	    
	    return true;
	}
	
	@Override
	public int hashCode()
	{
		return this.toString().hashCode();
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}

class Variable implements Argument, Comparable<Variable>
{
	public static final String VAR_PREFIX = "x";
	
	public final int id;
	public final String commonName;
	
	public Variable(String common)
	{
		this.id = (homework.v)++;
		this.commonName = common;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) {
	        return false;
	    }
		
	    if (!Variable.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
	    
	    final Variable other = (Variable) obj;
	    
	    if(this.id != other.id)
	    	return false;
	    
	    return true;
	}
	
	@Override
	public int hashCode()
	{
		return this.toString().hashCode();
	}
	
	@Override
	public String toString()
	{
		return Variable.VAR_PREFIX + id;
	}

	/**
	 * <p>
	 * A variable is alphabetically compared to another variable using its
	 * common name. e.g. x < y
	 * <p>
	 * Note that this is only valid for a single sentence - comparing Variables
	 * from different sentences may be erroneous
	 */
	@Override
	public int compareTo(Variable o) {

		return this.commonName.compareTo(o.commonName);
	}
}

/**
 * A predicate is identified by its name. It has a sign and a list of arguments.
 */
class Predicate implements Comparable<Predicate>
{
	public final String name;
	public final List<Argument> args;
	public final boolean negative;
	
	public Predicate(String natural)
	{
		this(natural, new HashMap<String, Variable>(), false);
	}
	
	/**
	 * Copy constructor
	 * @param p
	 */
	public Predicate(Predicate p)
	{
		this(p.toString());
	}
	
	public Predicate(String natural, Map<String, Variable> vars)
	{
		this(natural, vars, false);
	}
	
	public Predicate(String natural, Map<String, Variable> vars, boolean flip)
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
			else 
			{
				// If that variable name has already been assigned a Variable id
				if (vars.containsKey(arg)) 
				{
					args.add(vars.get(arg));
				}
				else 
				{
					Variable var = new Variable(arg);
					
					vars.put(arg, var);
					
					args.add(var);
				}
			}
			
		}
	}
	
	/**
	 * Returns a negated version of this predicate.
	 * @return
	 */
	/*public Predicate negate()
	{
		Predicate p2 = new Predicate(this.toString(), new HashMap<String, Variable>(), true);
		return p2;
	}*/
	
	/**
	 * <p>
	 * Checks if a predicate can unify with another predicate.
	 * <p>
	 * A predicate can unify with another if:
	 * <ol>
	 * <li>It has the same name</li>
	 * <li>It has the opposite sign</li>
	 * </ol>
	 * 
	 * @param other
	 *            The predicate to try to unify this one with.
	 * @return
	 */
	public boolean mightUnify(Predicate other)
	{
		if(this.name.equals(other.name) && this.negative == !(other.negative))
			return true;
		
		return false;
	}
	
	/**
	 * 
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) {
	        return false;
	    }
		
	    if (!Predicate.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
	    
	    final Predicate other = (Predicate) obj;
	    
	    if(this.name == null || other.name == null)
	    	return false;
	    
	    if (!this.name.equals(other.name))
	    	return false;
	    
	    return true;
	}
	
	@Override
	public int hashCode()
	{
		return this.name.hashCode();
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
	
	/*public String toCommonString()
	{		
		StringBuilder sbPredicate = new StringBuilder();
		if(negative)
			sbPredicate.append("~");
		sbPredicate.append(name);
		sbPredicate.append("(");
		
		Integer firstVarId = null;
				
		for(int i = 0; i < args.size(); i++)
		{
			Argument arg = args.get(i);
			
			// un-standardize variables
			if(arg instanceof Variable)
			{
				Variable var = (Variable)arg;
				if(firstVarId == null)
				{
					firstVarId = var.id;
				}
				sbPredicate.append(Variable.VAR_PREFIX + (var.id - firstVarId));
			}
			else { // Constant
				sbPredicate.append(args.get(i));
			}
			
			if(i < args.size()-1)
				sbPredicate.append(", ");
			else
				sbPredicate.append(")");
		}
		
		return sbPredicate.toString();
	}*/
	
	/**
	 * Returns the name of the predicate with the ~ sign in front (if negative)
	 * 
	 * @param flip
	 *            Allows you to reverse the sign
	 * @return
	 */
	public String signedName(boolean flip)
	{
		return ((this.negative ^ flip)?"~":"").concat(this.name);
	}

	/**
	 * <p>
	 * Allow Predicates to be sorted in alphabetical order.
	 * <p>
	 * This allows Sentences to become 'equal' to each other if they contain the
	 * same sentences, just in a different order.
	 */
	@Override
	public int compareTo(Predicate arg0) {
		
		String thisName = this.name + ((this.negative)?"~":"") + this.args.toString();
		// System.out.println(thisName);
		
		String otherName = arg0.name + ((arg0.negative)?"~":"") + arg0.args.toString();
		// System.out.println(otherName);
		
		return thisName.compareTo(otherName);
	}
}

/**
 * A sentence is a disjunction (OR) of several predicates.
 * Sentences are identified by their sentence id.
 */
class Sentence implements Comparable<Sentence>
{
	public final int id;
	
	public final int step;
	
	public final List<Predicate> predicates;
	

	/*public boolean containsPredicate(String predicateName, boolean negated)
	{
		for(Predicate p2 : predicates)
		{
			if(predicateName.equals(p2.name) && negated == p2.negative)
			{
				return true;
			}
		}
		
		return false;
	}*/
	
	/*public boolean containsPredicate(Predicate p1)
	{
		return this.predicates.contains(p1);
	}*/
	
	
	public Sentence(String natural)
	{
		this.id = (homework.sKB)++;
		this.step = homework.steps;
		
		String[] predStrings = natural.split("\\|");
		this.predicates = new ArrayList<>();

		// Handle variables with the same name
		Map<String, Variable> vars = new HashMap<>();
		
		for(String predString : predStrings)
		{
			this.predicates.add(new Predicate(predString.trim(), vars));
		}
		
		// Sort them - helps for checking equal sentences
		Collections.sort(this.predicates);
	}

	
	/**
	 * Duplicate a predefined sentence WITHOUT re-standardizing the variables.
	 * 
	 * @param s
	 * @updateCounter If true, a new sentence number is created
	 */
	public Sentence(Sentence s, boolean updateCounter) 
	{
		this.id = homework.sKB;
		if(updateCounter)
			homework.sKB++;

		this.step = homework.steps;
		this.predicates = new ArrayList<>();
		
		// COPY the predicate objects over - NEW PREDICATES ARE CREATED
		for(Predicate p : s.predicates)
		{
			this.predicates.add(new Predicate(p));
		}
		
		// Should already be sorted
	}
	
	/**
	 * Create a new sentence with the specified predicates
	 * @param p
	 */
	public Sentence(Predicate ... predicates)
	{
		this.id = (homework.sKB)++;
		this.step = homework.steps;
		this.predicates = new ArrayList<>();
		
		// USE the specified predicate objects
		for(Predicate p : predicates)
			this.predicates.add(p);

		// Sort them - helps for checking equal sentences
		Collections.sort(this.predicates);
	}
	
	public Sentence(Sentence s1, Sentence s2)
	{
		this.id = (homework.sKB)++;
		this.step = homework.steps;
		this.predicates = new ArrayList<>();

		// COPY the predicate objects over
		for(Predicate p : s1.predicates)
			this.predicates.add(p);
		for(Predicate p : s2.predicates)
			this.predicates.add(p);
		
		for(int i = 0; i < predicates.size()-1; i++)
		{
			for(int j = 1; j < predicates.size(); j++)
			{
				// Attempt to eliminate duplicates only for different indices
				if(i != j)
				{
					Predicate p1 = predicates.get(i);
					Predicate p2 = predicates.get(j);
					
					// System.out.printf("%s x %s CHECKIN'\n", p1, p2);
					
					if(p1.name.equals(p2.name) && p1.negative == !p2.negative)
					{
						// can remove these
						
						// System.out.printf("%s x %s CONTRADICTION LMAO.\n", p1, p2);
						
						predicates.remove(i);
						predicates.remove(j-1);
						i--;
						j--;
					}
				}
			}
		}

		// Sort them - helps for checking equal sentences
		Collections.sort(this.predicates);
	}

	/**
	 * Replaces all occurences of a variable in this sentence 
	 * with the specified constant.
	 * 
	 * @param v Variable to find
	 * @param c Constant to replace it with
	 */
	public void substituteSingle(Variable v, Constant c)
	{
		for(Predicate p : this.predicates)
		{
			for(int i = 0; i < p.args.size(); i++)
			{
				Argument arg_i = p.args.get(i);
		
				if(arg_i instanceof Variable && ((Variable) arg_i).equals(v))
				{
					// Substitute
					p.args.set(i, c);
				}
			}
		}
	}
	
	/**
	 * Two sentences need not have the same ID and step to be equal. They must
	 * have the same predicates (in any order, because sentence predicates are
	 * sorted), each having the same assignment of variables.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) {
	        return false;
	    }
		
	    if (!Sentence.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
	    
	    final Sentence other = (Sentence) obj;
	    
	    // Is there a better approach?
	    if(!this.toCommonString().equals(other.toCommonString()))
	    	return false;
		
		return true;
	}
	
	@Override
	public int hashCode()
	{
		return this.toCommonString().hashCode();
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("s%d/", step));
		sb.append(String.format("%d: ", id));
		
		if(predicates.size() > 0) 
		{
			for(int i = 0; i < predicates.size()-1; i++)
			{
				sb.append(predicates.get(i));
				sb.append(" | ");
			}
			sb.append(predicates.get(predicates.size()-1));

		}
		else sb.append("-");
		
		return sb.toString();
	}
	
	/**
	 * <p>
	 * Returns a String representation of a Sentence that is <i>equal<i> for all
	 * equivalent Sentences.
	 * <p>
	 * Thus, Sentences with the same predicates in any order, even if the
	 * sentence id, step numbers or variable ids are different, return the same
	 * value.
	 * 
	 * @return
	 */
	public String toCommonString()
	{
		StringBuilder sb = new StringBuilder();
				
		// Find the lowest variable id to destandardize
		// Integer lowestVarId = null;
		List<Variable> varsSentence = new ArrayList<>();
		for(Predicate p : predicates)
		{
			for(Argument a : p.args)
			{
				if(a instanceof Variable)
				{
					Variable v = ((Variable)a);
					varsSentence.add(v);
					
					/*if(lowestVarId == null)
						lowestVarId = v.id;
					else
					{
						if(v.id < lowestVarId)
							lowestVarId = v.id;
					}*/
				}
			}
		}
		
		if(predicates.size() > 0) 
		{
			for(int j = 0; j < predicates.size(); j++)
			{
				Predicate p = predicates.get(j);
				
				StringBuilder sbPredicate = new StringBuilder();
				if(p.negative)
					sbPredicate.append("~");
				sbPredicate.append(p.name);
				sbPredicate.append("(");
					
				for(int i = 0; i < p.args.size(); i++)
				{
					Argument arg = p.args.get(i);
					
					// un-standardize variables
					if(arg instanceof Variable)
					{
						Variable var = (Variable)arg;
						sbPredicate.append(Variable.VAR_PREFIX + (varsSentence.indexOf(var)));
					}
					else { // Constant
						sbPredicate.append(p.args.get(i));
					}
					
					if(i < p.args.size()-1)
						sbPredicate.append(", ");
					else
						sbPredicate.append(")");
				}
				
				sb.append(sbPredicate.toString());
				
				if(j < predicates.size()-1)
					sb.append(" | ");
			}

		}
		else sb.append("-");
		
		return sb.toString();
	}

	/**
	 * TODO The resolution strategy: AIMA 9.5.6<br>
	 * <br>
	 * Currently, compares the sizes of the predicate clauses so that we can
	 * prefer those that are smaller.
	 */
	@Override
	public int compareTo(Sentence arg0) {

		return Integer.compare(
				this.predicates.size(), 
				arg0.predicates.size());
	}
	
	/**
	 * Check if a sentence has at least one predicate that can unify with the
	 * sentence passed as input.
	 * 
	 * @param other
	 * @return
	 */
	/*public boolean canUnify(Sentence other)
	{
		for(Predicate p1 : this.predicates)
		{
			for(Predicate p2 : other.predicates)
			{
				if(p1.canUnify(p2))
					return true;
			}
		}
		
		return false;
	}*/

}