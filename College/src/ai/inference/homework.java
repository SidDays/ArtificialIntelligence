package ai.inference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class homework 
{

	/** If true, prints information to the console. */
	public static final boolean DEBUG = true;

	/** If true, prints a lot more information to the console. */
	public static final boolean VERBOSE = true & DEBUG;
	
	/** Should optimization strategies, like removal of duplicates be used? */
	public static final boolean OPTIMIZE = true;

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

	/**
	 * Stores the negated query predicates (which later become the first new
	 * sentence to add to the KB, I guess)
	 */
	private static List<Predicate> queriesNegated = new ArrayList<>();

	/**
	 * <p>
	 * UNIT PREFERENCE resolution strategy: AIMA 9.5.6
	 * <p>
	 * Currently, compares the sizes of the predicate clauses so that we can
	 * prefer those that are smaller.
	 */
	private static final Comparator<Entry<Sentence, Sentence>> unitPreference = 
			new Comparator<Entry<Sentence, Sentence>>() 
	{
		@Override
		public int compare(Entry<Sentence, Sentence> o1, Entry<Sentence, Sentence> o2) {

			int size1 = o1.getKey().predicates.size() + o1.getValue().predicates.size();
			int size2 = o2.getKey().predicates.size() + o2.getValue().predicates.size();

			return Integer.compare(size1, size2);
		}
	};

	public static String inputFileName = "input.txt";
	public static String outputFileName = "output.txt";

	// Time variables

	/** System.nanoTime() values. */
	private static long timeStart, timeCurrent;

	private static void initialize()
	{
		sKB = 0;
		v = 0;
		steps = 0;
		queriesNegated = new ArrayList<>();
	}

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
			System.out.println("\nParsing sentences into knowledgebase...");

		kb = new HashMap<>();

		// kbSentences = new ArrayList<>();

		for(int i = 0; i < sIP; i++)
		{
			String sentenceStr = in.nextLine().trim();

			Sentence sentence = new Sentence(sentenceStr);

			if(DEBUG)
				System.out.printf("Current sentence: %s\n", sentence);

			// Handle sentences with two contradicting predicates -
			// Not required, because KB is guaranteed to be consistent.

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
		if(DEBUG) System.out.printf("Adding sentence [%s] to KB.\n", sentence);

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

	private static Map<Variable, Argument> unify(Predicate p1, Predicate p2)
	{
		if(VERBOSE)
		{
			System.out.printf("Attempting to unify %s and %s.\n", p1, p2);
		}

		if(!p1.name.equals(p2.name))
		{
			System.err.printf("Attempted to unify unidentical predicates: %s vs %s\n", p1.name, p2.name);
			return null;
		}

		Map<Variable, Argument> substitution = new HashMap<>();

		boolean valid = true;

		if(!p1.args.equals(p2.args))
		{
			List<Argument> p1_args = new ArrayList<>(p1.args);
			List<Argument> p2_args = new ArrayList<>(p2.args);

			Set<Set<Variable>> commonSubVariableSets = new HashSet<>();

			for(int i = 0; i < p1_args.size() && valid; i++)
			{
				Argument p1_i = p1_args.get(i);
				Argument p2_i = p2_args.get(i);

				// If both are Constants
				if(p1_i instanceof Constant && p2_i instanceof Constant)
				{
					Constant p1_c = (Constant) p1_i;
					Constant p2_c = (Constant) p2_i;

					if(!p1_c.equals(p2_c))
					{
						if(DEBUG)
						{
							System.out.printf("Substitution invalid: Constants %s, %s can't be unified.\n", p1_c, p2_c);
						}
						valid = false;
					}
				}

				// If one is a Variable and the other is a constant
				if((p1_i instanceof Variable && p2_i instanceof Constant) || 
						(p2_i instanceof Variable && p1_i instanceof Constant))
				{
					Variable p_v = null;
					Constant p_c = null;

					// Check WHICH one is Variable
					if(p1_i instanceof Variable) {
						p_v = (Variable) p1_i;
						p_c = (Constant) p2_i;
					}
					else {
						p_v = (Variable) p2_i;
						p_c = (Constant) p1_i;
					}

					// [?] Add substitution to the substitution list
					if(substitution.get(p_v) != null && !substitution.get(p_v).equals(p_c))
					{
						if(DEBUG)
						{
							System.out.printf("Substitution invalid: %s already unified with different constant.\n",
									p_v);
						}
						valid = false;
					} 
					else {
						substitution.put(p_v, p_c);
					}
				}

				// If both are variables
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

					if(VERBOSE) System.out.printf("Will attempt to unify variables %s and %s.\n", p1_v, p2_v);

					Set<Variable> currentSubSet = null;

					if(VERBOSE) System.out.printf("All groups so far sharing a substitution: %s.\n", commonSubVariableSets);

					// Check if any of the common substitution sets already contain either variable
					for(Set<Variable> possibleSubSet : commonSubVariableSets)
					{
						if(possibleSubSet.contains(p1_v) || possibleSubSet.contains(p2_v))
						{
							currentSubSet = possibleSubSet;

							if(VERBOSE) System.out.printf("Can reuse the substitution %s.\n", currentSubSet);

							break;
						}
					}

					if(currentSubSet != null) // If it exists, reuse that subset
					{
						// Either way, add these two variables to it.
						currentSubSet.add(p1_v);
						currentSubSet.add(p2_v);
					}
					else // If not, make a new set of common substitutions
					{ 
						currentSubSet = new HashSet<>();

						// Add these two variables to it.
						currentSubSet.add(p1_v);
						currentSubSet.add(p2_v);

						// Now add this list to the common ones
						commonSubVariableSets.add(currentSubSet);
					}
				}

				if(VERBOSE) System.out.printf("Substitution after arg%d, (%s and %s) is %s.\n", (i), p1_i, p2_i, substitution);

			}

			// if(VERBOSE) System.out.println("Finished processing arguments.");

			// Check if all the common substitutions are satisfied
			for(Set<Variable> currentSubSet : commonSubVariableSets)
			{
				// Resue the same list of common substitutions
				Set<Argument> commonSubs = new HashSet<>();

				if(VERBOSE) System.out.printf("Checking the combined mappings of the Variable-Set %s.\n", currentSubSet);
				for(Variable var : currentSubSet)
				{
					Argument argSub = substitution.get(var);
					if(argSub != null)
						commonSubs.add(argSub);
				}

				// If none of these variables have a mapping, assign a temp variable to map them to it
				if(commonSubs.isEmpty())
				{
					Variable varSub = new Variable(Variable.VAR_TEMP_PREFIX + steps);

					for(Variable var : currentSubSet)
					{
						substitution.put(var, varSub);

						// if(VERBOSE) System.out.printf("Adding var-var substitution {%s/%s}\n", var, varSub);
					}

					if (VERBOSE)
						System.out.printf("Thus, the variables %s were unified by substituting them to a new variable %s.\n",
								currentSubSet, varSub);

				}

				// If all of them combined have only one mapping, assign all variables to it
				else if(commonSubs.size() == 1)
				{
					Argument argSub = commonSubs.iterator().next();

					for(Variable var : currentSubSet)
					{
						substitution.put(var, argSub);

						// if(VERBOSE) System.out.printf("Adding var-var substitution {%s/%s}\n", var, argSub);
					}

					if (VERBOSE)
						System.out.printf("Thus, the variables %s were unified to the common substitution %s.\n",
								currentSubSet, argSub);
				}

				// Since a Set is used, the variables supposed to have a common substitution already don't have a common substitution
				else
				{
					if(VERBOSE) System.out.printf("The combined common subtitutions, %s, are non-identical and clash.\n"
							+ "The variables %s cannot be unified.\n", commonSubs, currentSubSet);
					valid = false;
				}
			}

			// If invalid, clear the substitution - these cannot be unified.
			if(!valid) 
			{
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
	private static Sentence substitute(Sentence sent, Map<Variable, Argument> substitution)
	{
		Sentence sentNew = new Sentence(sent, false);

		for(Entry<Variable, Argument> e : substitution.entrySet())
		{
			sentNew.substituteSingle(e.getKey(), e.getValue());
		}

		return sentNew;
	}

	/**
	 * <p>
	 * Tries to resolve two sentences, s1 and s2. Compares every predicate in
	 * both of those sentences, and tries to unify them in all possible ways.
	 * 
	 * @return a Set of all possible unifications, null if unable to unify.
	 * 
	 */
	private static Set<Sentence> resolve(Sentence s1, Sentence s2)
	{
		if (DEBUG) 
		{
			System.out.printf("\nSTEP %d - Resolve [%s] vs [%s]\n"
					+ "-----------------------------------------------------------------------\n", steps, s1, s2);
		}

		// Store the combined results of all possible ways these two sentences can be resolved
		Set<Sentence> resolutionResult = null;

		// Check all possible ways these two sentences can be resolved
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
					if(DEBUG)
						System.out.printf("-> %d/%d: %s might unify with %s.\n",i, j, p1, p2);

					Map<Variable, Argument> substitution = unify(p1, p2);

					Sentence s1_New = s1, s2_New = s2;

					if(DEBUG) System.out.println();

					/*
					 * Substitution might be empty even after a valid resolution
					 * ~A(Lion) vs A(Lion), so check for nullness instead.
					 */
					if(substitution != null)
					{
						if(DEBUG)
							System.out.printf("Substitution, finally = %s\n", substitution);
						
						s1_New = substitute(s1, substitution);
						// if(DEBUG) System.out.printf("Substitution: [%s] -> [%s]\n", s1, s1_New);

						s2_New = substitute(s2, substitution); 
						// if(DEBUG) System.out.printf("Substitution: [%s] -> [%s]\n", s2, s2_New);

						Sentence sResolved = new Sentence(s1_New, s2_New);

						// if(DEBUG) System.out.println("Resolved to: "+sResolved);

						if(sResolved.predicates.isEmpty())
						{
							if(DEBUG) System.out.printf("Empty clause formed after resolving [%s] vs [%s].\n", s1, s2);

							emptyClause = true;
						}
						else 
						{
							// NOT JUST YET: putInKB(sResolved);

							if(DEBUG) 
							{ 
								System.out.printf(
										"Valid sentence formed after resolving \n\t   [%s] \n\tvs [%s] \n\t = [%s]\n\n",
										s1, s2, sResolved);
							}
							
							if(OPTIMIZE) sResolved.refactor();
						}

						// The set remains null if it is empty
						if(resolutionResult == null)
						{
							resolutionResult = new LinkedHashSet<>();
						}
						resolutionResult.add(sResolved);
						// return sResolved;
					}

					else // Don't add anything to the set
					{

						if(DEBUG)
							System.out.printf("Can't resolve [%s] vs [%s]; no valid substitution.\n", s1, s2);
						// return null;

					}
				}
			}
		}

		// If both for loops are exited, no predicates might unify
		if(resolutionResult == null) {
			if(DEBUG) System.out.printf("Can't resolve [%s] vs [%s]; Possibly incompatible predicates.\n", s1, s2);
		}

		return resolutionResult;
	}


	/**
	 * <p>
	 * Runs FOL-Resolution for the query with given query number.
	 * <p>
	 * The way the function is currently designed, there MUST be a path to
	 * the bottom using the query itself (without requiring additional things
	 * like AND introduction.)
	 * <p>
	 * Currently does not check for loops by checking if the resolution pairs
	 * already exist.
	 * 
	 * @param q_i The query to try to refute.
	 */
	private static boolean resolveQueryNegated(int q_i) 
	{
		Predicate queryNegated = queriesNegated.get(q_i);
		if(DEBUG) 
			System.out.printf("\nQuery %d: %s\n===== == ======================\n",
					q_i+1, queryNegated);

		emptyClause = false;

		/*
		 * Restore the KB backup; delete all sentences with steps > 0 (1 is the
		 * old query, > 1 the derived ones)
		 */
		if(steps > 0) // Only if you need to reset it - just optimizing ;)
		{
			resetKB();
		}

		Sentence queryNegatedSentence = new Sentence(queryNegated);
		putInKB(queryNegatedSentence);

		List<Entry<Sentence, Sentence>> pairsToResolve = new ArrayList<>();
		List<Entry<Sentence, Sentence>> pairsResolved = new ArrayList<>();

		// Get all the sentences with new sentence query's negation
		List<Sentence> resolvers = kb.get(queryNegated.signedName(true));


		// Initial resolver database, NullPointerException if no resolvers
		if(resolvers == null)
		{
			if(DEBUG) System.out.println("There's nothing to unify the query with at all.");
			return false;
		}
		else 
		{
			// resolvers.sort(null);

			if(DEBUG) System.out.printf("%s can resolve with: %s\n", queryNegated, resolvers);
		}

		// I got something to resolve with!
		for(Sentence s : resolvers)
		{
			Entry<Sentence, Sentence> pairWithQuery = new AbstractMap.SimpleEntry<>(queryNegatedSentence, s);
			pairsToResolve.add(pairWithQuery);
		}

		while(!pairsToResolve.isEmpty() && !emptyClause)
		{
			// Attempt to reduce resolution steps
			Collections.sort(pairsToResolve, unitPreference);

			// Attempt to resolve one pair, then add its result to the KB
			Entry<Sentence, Sentence> pair = pairsToResolve.remove(0);
			Sentence s1 = pair.getKey();
			Sentence s2 = pair.getValue();

			// Only increment counter if the new step worked
			steps++;

			// The set of all possible ways a pair of Sentences can resolve
			Set<Sentence> sNewSet = resolve(s1, s2);
			
			if(DEBUG) System.out.printf("Set of all resolved results: %s\n", sNewSet);

			pairsResolved.add(pair);

			// If it was possible to actually resolve them
			if(sNewSet != null)
			{
				if(emptyClause) 
				{
					break;
				}
				else // Process every new sentence
				{
					for(Sentence sNew : sNewSet) 
					{

						// Check if sentence already in KB - it should be present in the list for even its first predicate
						Predicate p0 = sNew.predicates.get(0);

						boolean inKB = kb.get(p0.signedName(false)).contains(sNew);

						if(inKB)
						{
							if(DEBUG) System.out.printf("Sentence [%s] already in KB!\n", sNew);
						}

						else 
						{
							/* Create new resolvers using the newly generated sentence.
							 * Later checks if this sentence/pair repeats
							 */
							if(DEBUG)
								System.out.printf("\nCreating new pairs for [%s].\n", sNew);

							// TODO Does checking this condition cause problems?
							boolean atLeastOnePair = false;

							for(Predicate p : sNew.predicates)
							{
								resolvers = kb.get(p.signedName(true));

								if(resolvers != null)
								{
									if(VERBOSE) System.out.printf("From [%s], predicate %s can resolve with: %s\n", sNew, p, resolvers);

									// Make this pair only if not already made
									for(Sentence s : resolvers)
									{
										boolean alreadyExists = false;

										// The pair can be in the pairsToResolve...
										for(Entry<Sentence, Sentence> pairExisting : pairsToResolve)
										{
											Sentence ps1 = pairExisting.getKey();
											Sentence ps2 = pairExisting.getValue();

											if((ps1.equals(sNew) && ps2.equals(s)) ||
													(ps2.equals(sNew) && ps1.equals(s)))
											{
												alreadyExists = true;

												if(DEBUG)
													System.out.printf(
															"The pair [%s; %s] already exists in the pairs to resolve.\n", 
															s, sNew);

												break;
											}
										}

										// or can be in pairsResolved.
										if(!alreadyExists)
										{
											for(Entry<Sentence, Sentence> pairExisting : pairsResolved)
											{
												Sentence ps1 = pairExisting.getKey();
												Sentence ps2 = pairExisting.getValue();

												if((ps1.equals(sNew) && ps2.equals(s)) ||
														(ps2.equals(sNew) && ps1.equals(s)))
												{
													alreadyExists = true;

													if(DEBUG)
														System.out.printf(
																"The pair [%s; %s] already exists in the resolved pairs.\n", 
																s, sNew);

													break;
												}
											}
										}

										// If it's in neither, you're good to go!
										if(!alreadyExists)
										{
											Entry<Sentence, Sentence> pair2 = new AbstractMap.SimpleEntry<>(sNew, s);
											pairsToResolve.add(0, pair2);

											if(DEBUG)
												System.out.printf(
														"Added pair [%s; %s] to pairsToResolve!\n", 
														s, sNew);

											// TODO
											atLeastOnePair = true;
										}
									}
								}

								// TODO
								if(atLeastOnePair) break;
							}

							// Add this new sentence to the KB
							putInKB(sNew);

						}

					}
				}

			}

		}
		
		if(pairsToResolve.isEmpty() && !emptyClause)
		{
			if(DEBUG) System.out.printf("\n...tried all possible combinations and failed :(\n");
		}

		return emptyClause;
	}

	/**
	 * Restore the KB backup; delete all sentences with steps > 0 (1 is the old
	 * query, > 1 the derived ones)
	 */
	private static void resetKB() 
	{
		if(DEBUG) System.out.println("Restoring KB to pre-query state...");

		for(Entry<String, List<Sentence>> e : kb.entrySet())
		{
			List<Sentence> l = e.getValue();

			for(int i = 0; i < l.size(); i++)
			{
				Sentence s = l.get(i);
				if(s.step > 0)
				{
					// if(DEBUG) System.out.printf("Removing [%s].\n", l.get(i));

					l.remove(i);

					i--;
				}
			}
		}

		// Start from step 1 again
		steps = 1;
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

			initialize();

			parseInput(in);

			PrintWriter writerOutput = null;
			try 
			{

				writerOutput = new PrintWriter(outputFileName, "UTF-8");
				
				if(DEBUG) System.out.printf("Starting inference %s optimization.\n", ((OPTIMIZE)?"with":"without"));

				// For each query
				for(int q_i = 0; q_i < q; q_i++)
				{
					boolean result = resolveQueryNegated(q_i);

					String resultString = (result)?"TRUE":"FALSE";

					System.out.printf("\n%s\n", resultString);
					writerOutput.println(resultString);
				}

				// Before hardcode testing, make sure to disable emptyClause
				/*emptyClause = false;
				System.out.println(resolve(
						new Sentence("FA(x1,y2,y2)"), 
						new Sentence("~FA(y3,y3,x4)")));*/


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
			System.out.println("Input file '"+inputFileName+"' not found.\n");
		}

		timeCurrent = System.nanoTime();

		System.out.println("Completed in " +
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
	public static final String VAR_TEMP_PREFIX = "t";

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
		return String.format("%s%03d", Variable.VAR_PREFIX, id);
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
		this(p, false);
	}

	/**
	 * Copy constructor
	 * @param p
	 * @param flip Whether the sign should be flipped or not.
	 */
	public Predicate(Predicate p, boolean flip)
	{
		this.name = p.name;
		this.args = new ArrayList<>(p.args);
		this.negative = p.negative^flip;
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

	public boolean isAllVariable()
	{
		boolean allVars = true;

		for(int j = 0; j < args.size(); j++)
		{
			Argument arg1 = args.get(j);
			if(!(arg1 instanceof Variable))
			{
				allVars = false;
				break;
			}
		}

		return allVars;
	}

	/**
	 * Returns a negated version of this predicate.
	 * @return
	 */
	public Predicate negate()
	{
		Predicate pNeg = null;

		pNeg = new Predicate(this, true);

		return pNeg;
	}

	/**
	 * @param p2
	 * @return
	 */
	public boolean matchesSignature(Predicate p2) 
	{
		return (this.name.equals(p2.name) && this.negative == p2.negative);
	}

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
	 * Counts the types of variables in this predicate.
	 * e.g. A(x, x, y) has 2 types of variables.
	 * @return
	 */
	public Integer numberOfVarTypes() 
	{
		Set<Variable> vars = new HashSet<>();
		for(Argument arg : this.args)
			if(arg instanceof Variable)
				vars.add((Variable)arg);
		
		return vars.size();
	}

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
	 * For two Predicates to be identical, they must share the same name, the
	 * same sign, and the exact same list of arguments, right down to the
	 * variable number.
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
		
		// Check all arguments. if a single one doesn't match, return false.
		for(int i = 0; i < this.args.size(); i++)
		{
			Argument arg = this.args.get(i);
			Argument argCheck = other.args.get(i);
			
			if((arg instanceof Constant && argCheck instanceof Constant) ||
					(arg instanceof Variable && argCheck instanceof Variable))
			{
				if(arg instanceof Constant)
				{
					if(!((Constant)arg).equals((Constant)argCheck))
						return false;
				}
				else // It must be the EXACT SAME VARIABLE
				{
					if(!((Variable)arg).equals((Variable)argCheck))
						return false;
				}
			}
		}

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
	
	/**
	 * Count the number of times an argument appears in a predicate
	 * @param argCheck The argument to count.
	 * @return
	 */
	public int countOcurrences(Argument argCheck)
	{
		int count = 0;		
		for(Argument arg : this.args)
		{
			if((arg instanceof Constant && argCheck instanceof Constant) ||
					(arg instanceof Variable && argCheck instanceof Variable))
			{
				if(arg instanceof Constant)
				{
					if(((Constant)arg).equals((Constant)argCheck))
						count++;
				}
				else 
				{
					if(((Variable)arg).equals((Variable)argCheck))
						count++;
				}
			}
		}
		
		return count;
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
class Sentence // implements Comparable<Sentence>
{
	public final int id;

	public final int step;

	public final List<Predicate> predicates;

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

						if(homework.DEBUG) System.out.printf("Resolved and removed; %s vs %s.\n", p1, p2);

						// Removing two elements from a list
						if(i > j)
						{
							predicates.remove(i);
							predicates.remove(j);
							i--;
						}
						else {
							predicates.remove(j);
							predicates.remove(i);
							j--;
						}
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
	 * @param var Variable to find
	 * @param argSub Constant to replace it with
	 */
	public void substituteSingle(Variable var, Argument argSub)
	{
		for(Predicate p : this.predicates)
		{
			for(int i = 0; i < p.args.size(); i++)
			{
				Argument argPred = p.args.get(i);

				// if(homework.DEBUG) System.out.printf("Trying to substitute %s {%s/%s}.\n", argPred, var, argSub);

				if(argPred instanceof Variable && ((Variable) argPred).equals(var))
				{
					// Substitute

					// if(homework.DEBUG) System.out.printf("Substituting %s by %s.\n", argPred, argSub);

					p.args.set(i, argSub);
				}
			}
		}
	}

	/**
	 * An attempt to optimize sentences to prevent looping.
	 */
	public void refactor()
	{
		/*
		 * TODO check if the sentence is always TRUE
		if(homework.DEBUG) System.out.printf("Checking for complementary predicates...\n");

		// List<Predicate> not predicate
		Map<String, Predicate> predicateMap = new HashMap<>();
		for(Predicate p : this.predicates)
		{
			predicateMap.put(p.signedName(false), p);
		}
		
		for(Entry<String, Predicate> e : predicateMap.entrySet())
		{
			String name1 = e.getKey();
			String name2 = e.getValue().signedName(true);
			System.out.println(name1 + " x okurr x "+name2);
			if(predicateMap.get(name2) != null)
			{
				if(homework.DEBUG) 
					System.out.printf("Possible optimization: %s vs %s.\n", e.getValue(), predicateMap.get(name2));
			}
		}*/

		int count = 0;
		
		if(homework.DEBUG) System.out.printf("Checking for repeated predicates...\n");
		
		// Separate the predicates into 'buckets' sorted by signed name, like the kb
		Map<String, List<Predicate>> buckets = new HashMap<>();
		for(Predicate p : this.predicates)
		{
			if(p.isAllVariable()) 
			{
				List<Predicate> bucket = buckets.get(p.signedName(false));

				if(bucket == null) bucket = new ArrayList<>();	

				bucket.add(p);

				buckets.put(p.signedName(false), bucket);
			}
		}
		if(homework.VERBOSE) System.out.printf("The predicate buckets are %s.\n", buckets);
		
		// Now, we can check all predicates of a certain bucket and try to combine
		for(Entry<String, List<Predicate>> e : buckets.entrySet())
		{
			List<Predicate> bucket = e.getValue();
			if(bucket.size() > 1) // Only then can you combine
			{
				if(homework.VERBOSE) System.out.println("There's a shot at optimization for the bucket "+bucket);
				
				// This list can be more than 2 predicates, e.g. A(Liz, x), A(y, Jon) which can't be combined.
				
				// find the (one) predicate with maximum variable-types, make that the most general
				// preferably which doesn't appear in other predicates of the sentences
				// this is all literally so arbit
				
				// List<Integer> varTypes = new ArrayList<>();
				int maxVarTypes = -1;
				Predicate mostGeneralPredicate = null;
				
				for(int i = 0; i < bucket.size(); i++)
				{
					Predicate currentPredicate = bucket.get(i);
					int currentVarTypes = currentPredicate.numberOfVarTypes();
					
					if(currentVarTypes > maxVarTypes)
					{
						mostGeneralPredicate = currentPredicate;
						maxVarTypes = currentVarTypes;
					}
				}
				
				if(homework.VERBOSE) System.out.printf("Most general predicate is %s (%d types). Will remove others.\n",
						mostGeneralPredicate, maxVarTypes, bucket);
				
				// Now, remove all other all-variable predicates in this bucket from the sentence
				for(int i = 0; i < this.predicates.size(); i++)
				{
					Predicate currentPredicate = this.predicates.get(i);
					
					if(!currentPredicate.equals(mostGeneralPredicate) && bucket.contains(currentPredicate))
					{
						if(homework.VERBOSE) System.out.printf("Removing predicate %s.\n", currentPredicate);
						
						this.predicates.remove(i--);
						
						count++;
					}
				}
				
			}
		}

		if(homework.DEBUG && count > 0)
			System.out.printf("%d duplicate(s) removed.\n\n", count);
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
}
