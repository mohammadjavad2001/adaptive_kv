import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.RandomWMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.WMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.WpMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.Infer_LearnLib;
import br.usp.icmc.labes.mealyInference.utils.LearnLibProperties;
import br.usp.icmc.labes.mealyInference.utils.Utils;
// import de.learnlib.algorithms.kv.KearnsVaziraniMealy;
import de.learnlib.algorithms.kv.IKearnsVaziraniMealy;

// import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;

// import de.learnlib.algorithms.kv.KearnsVaziraniMealyBuilder;
import de.learnlib.algorithms.kv.IKearnsVaziraniMealyBuilder;
import de.learnlib.api.SUL;
// import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealyBuilder;
//import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import de.learnlib.api.logging.LearnLogger;

// import de.learnlib.ds.EquivalenceOracle;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;

// import de.learnlib.ds.MembershipOracle;
import de.learnlib.api.statistic.StatisticSUL;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.ds.MealySimulatorSUL;
import de.learnlib.ds.ResetCounterSUL;
import de.learnlib.ds.SymbolCounterSUL;
import de.learnlib.oracle.equivalence.RandomWMethodEQOracle;
import de.learnlib.oracle.equivalence.RandomWordsEQOracle;
import de.learnlib.oracle.equivalence.WMethodEQOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle;
// import de.learnlib.ds.RandomWalkEQOracle;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;


import de.learnlib.oracle.membership.SULOracle;
// import de.learnlib.ds.SULOracle;

// import de.learnlib.util.Experiment;
import de.learnlib.algorithms.kv.Experiment;


import de.learnlib.util.statistics.SimpleProfiler;

import net.automatalib.automata.transducers.MealyMachine;
// import de.learnlib.ds.MealyMachine;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.equivalence.DeterministicEquivalenceTest;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Word;
import org.apache.commons.cli.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.function.Function;
import java.io.BufferedReader;
import java.io.FileReader;
// import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
// import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealyBuilder;
//If using Apache Commons Graph (though 1named differently)

import java.io.FileWriter;

import net.automatalib.visualization.VisualizationHelper;
import de.learnlib.algorithms.kv.StateInfo;

// import de.learnlib.algorithms.kv.StateInfo;
import de.learnlib.algorithms.kv.GraphUtils;


import net.automatalib.serialization.dot.GraphDOT;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
//import net.automatalib.util.IOUtil;
import net.automatalib.commons.util.IOUtil;
import java.io.InputStream;
import net.automatalib.commons.util.settings.AbstractClassPathFileSource;
import net.automatalib.commons.util.settings.AbstractClassPathFileSource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.HashSet;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.GrowingMapAlphabet;
import java.util.*;
import net.automatalib.words.Alphabet;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.visualization.Visualization;
import net.automatalib.visualization.VisualizationHelper.EdgeAttrs;

// import de.learnlib.ds.MultiDTree;
import de.learnlib.datastructure.discriminationtree.MultiDTree;


import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import de.learnlib.algorithms.kv.StateInfo;
import net.automatalib.graphs.concepts.GraphViewable;
import de.learnlib.ds.JointCounterOracle;
import de.learnlib.filter.statistic.oracle.CounterSymbolQueryOracle;


public class hi_single<
         I extends java.lang.Object,
         O extends java.lang.Object> {

	// Add static variable to store tree between method calls
	static MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree_round2 = null;
	// MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> tree_round2 = null;
	// Collect all unique input symbols seen so far (for building combined alphabet)
	private static ArrayList<String> allInputAlphabets = new ArrayList<>();
	// Store Product 0's alphabet for adaptive learning (reused in Product 1+)
	private static Alphabet<String> product1Alphabet = null;
	// Store previous product's hypothesis
	private static CompactMealy<String, Word<String>> previousHypothesis = null;

	private static int ExtractValue(String string_1) {
		// TODO Auto-generated method stub
		int value_1 = 0;
		int j = string_1.lastIndexOf(" ");
		String string_2 = "";
		if (j >= 0) {
			string_2 = string_1.substring(j + 1);
		}
		value_1 = Integer.parseInt(string_2);
		return value_1;
	}
	// Method to traverse and print the tree
	private static <I, O, D> void traverseAndPrintTree(
        AbstractWordBasedDTNode<I, O, D> node, 
        String indent, 
        boolean isLast) {
    
    // Print the current node
    System.out.print(indent);
    
    if (isLast) {
        System.out.print("└─ ");
        indent += "   ";
    } else {
        System.out.print("├─ ");
        indent += "│  ";
    }
    
    // Print node information
    if (node.isLeaf()) {
        System.out.println("State: " + node.getData());
    } 
	else {
        System.out.println("Test: " + node.getDiscriminator());
        
        // Get children and print them
        Collection<Map.Entry<O, AbstractWordBasedDTNode<I, O, D>>> children = node.getChildEntries();
        int count = 0;
        int total = children.size();
        
        for (Map.Entry<O, AbstractWordBasedDTNode<I, O, D>> entry : children) {
            count++;
            
            // Print edge
            System.out.print(indent);
            if (count == total) {
                System.out.print("└─ ");
            } else {
                System.out.print("├─ ");
            }
            System.out.println("Edge: " + entry.getKey());
            
            // Recursively print the child node
            traverseAndPrintTree(entry.getValue(), indent + (count == total ? "   " : "│  "), count == total);
        }
    }
}

	// ============================================================================
	// COMPREHENSIVE TREE ANALYSIS METHOD - Analyzes StateInfo and Tree Structure
	// ============================================================================
	private static void analyzeTreeStructureDetailed(
			MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree,
			int productNumber,
			Alphabet<String> productAlphabet) {
		
		System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
		System.out.println("║  DETAILED TREE ANALYSIS - PRODUCT " + productNumber + "                             ║");
		System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
		
		if (tree == null) {
			System.out.println("⚠ Tree is null - no analysis possible");
			return;
		}
		
		// Get root node
		AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> root = tree.getRoot();
		
		// Collect all StateInfo objects and discriminators
		List<StateInfo<String, Word<Word<String>>>> allStates = new ArrayList<>();
		List<Word<String>> allDiscriminators = new ArrayList<>();
		int treeDepth = collectTreeInfo(root, allStates, allDiscriminators, 0);
		
		// Print tree statistics
		System.out.println("TREE STATISTICS:");
		System.out.println("  Total states (leaf nodes): " + allStates.size());
		System.out.println("  Total discriminators (internal nodes): " + allDiscriminators.size());
		System.out.println("  Maximum tree depth: " + treeDepth);
		System.out.println("  Product alphabet size: " + productAlphabet.size());
		System.out.println();
		
		// Print all discriminators
		System.out.println("DISCRIMINATORS IN TREE:");
		for (int i = 0; i < allDiscriminators.size(); i++) {
			Word<String> disc = allDiscriminators.get(i);
			System.out.println("  [" + i + "] " + disc + " (length: " + disc.length() + ")");
		}
		System.out.println();
		
		// Print all StateInfo objects
		System.out.println("STATE INFO OBJECTS:");
		for (StateInfo<String, Word<Word<String>>> stateInfo : allStates) {
			System.out.println("  State " + stateInfo.id + ":");
			System.out.println("    Access Sequence: " + stateInfo.accessSequence);
			System.out.println("    Access Seq Length: " + stateInfo.accessSequence.length());
			
			// Check if access sequence uses symbols from current product
			boolean usesCurrentSymbols = false;
			for (String symbol : productAlphabet) {
				if (stateInfo.accessSequence.toString().contains(symbol)) {
					usesCurrentSymbols = true;
					break;
				}
			}
			System.out.println("    Uses current product symbols: " + usesCurrentSymbols);
		}
		System.out.println();
		
	System.out.println("═══════════════════════════════════════════════════════════════\n");
}

// Helper method to update StateInfo IDs in tree to match new hypothesis state mapping
private static void updateStateInfoInTree(
		AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> node,
		Map<Integer, Integer> stateMap) {
	if (node == null) {
		return;
	}
	
	if (node.isLeaf()) {
		// Update StateInfo ID if this is a leaf node
		StateInfo<String, Word<Word<String>>> stateInfo = node.getData();
		if (stateInfo != null && stateMap != null) {
			Integer oldId = stateInfo.id;
			Integer newId = stateMap.get(oldId);
			if (newId != null && !newId.equals(oldId)) {
				// Update ID using reflection (since 'id' is a final field)
				try {
					java.lang.reflect.Field idField = StateInfo.class.getDeclaredField("id");
					idField.setAccessible(true);
					idField.set(stateInfo, newId);
					System.out.println("    Updated StateInfo ID: " + oldId + " → " + newId);
				} catch (Exception e) {
					System.err.println("    ERROR: Could not update StateInfo ID from " + oldId + " to " + newId + ": " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return;
	}
	
	// Recursively update StateInfo in all child nodes
	Collection<Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>>> children = node.getChildEntries();
	for (Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>> entry : children) {
		updateStateInfoInTree(entry.getValue(), stateMap);
	}
}

// Helper method to canonicalize all discriminators in tree to use new alphabet's symbol instances
// This is CRITICAL when reusing a tree from product1 for product2, because:
// - Discriminators contain Word<String> where each String symbol is an object from product1's alphabet
// - Java uses object identity (==) not equality (.equals()) for alphabet lookups
// - Product2's alphabet has different String instances even if the values are the same
// - Without canonicalization, oracle queries will fail with IllegalArgumentException
private static void canonicalizeTreeDiscriminators(
		AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> node,
		Alphabet<String> newAlphabet) {
	if (node == null) {
		return;
	}
	
	// Canonicalize StateInfo access sequence if this is a leaf node
	if (node.isLeaf()) {
		StateInfo<String, Word<Word<String>>> stateInfo = node.getData();
		if (stateInfo != null && stateInfo.accessSequence != null && stateInfo.accessSequence.length() > 0) {
			Word<String> oldAccessSeq = stateInfo.accessSequence;
			System.out.println("  Canonicalizing StateInfo access sequence for state " + stateInfo.id + ": " + oldAccessSeq);
			
			// Build new access sequence with canonical symbols
			String[] canonicalSymbols = new String[oldAccessSeq.length()];
			for (int i = 0; i < oldAccessSeq.length(); i++) {
				String oldSymbol = oldAccessSeq.getSymbol(i);
				if (newAlphabet.containsSymbol(oldSymbol)) {
					int symbolIdx = newAlphabet.getSymbolIndex(oldSymbol);
					canonicalSymbols[i] = newAlphabet.getSymbol(symbolIdx);
				} else {
					System.err.println("    WARNING: Symbol '" + oldSymbol + "' from StateInfo not in new alphabet!");
					canonicalSymbols[i] = oldSymbol;
				}
			}
			
			Word<String> newAccessSeq = Word.fromList(java.util.Arrays.asList(canonicalSymbols));
			
			// Replace access sequence using reflection
			try {
				java.lang.reflect.Field accessSeqField = StateInfo.class.getDeclaredField("accessSequence");
				accessSeqField.setAccessible(true);
				accessSeqField.set(stateInfo, newAccessSeq);
				System.out.println("    ✓ StateInfo access sequence canonicalized");
			} catch (Exception e) {
				System.err.println("    ERROR: Could not update StateInfo access sequence: " + e.getMessage());
				e.printStackTrace();
			}
		}
		return;
	}
	
	// Canonicalize the discriminator at this internal node
	Word<String> oldDiscriminator = node.getDiscriminator();
	if (oldDiscriminator != null && oldDiscriminator.length() > 0) {
		System.out.println("  Canonicalizing discriminator: " + oldDiscriminator);
		System.out.println("    Old symbols (identity hashes):");
		
		// Build new discriminator with canonical symbols from newAlphabet
		String[] canonicalSymbols = new String[oldDiscriminator.length()];
		for (int i = 0; i < oldDiscriminator.length(); i++) {
			String oldSymbol = oldDiscriminator.getSymbol(i);
			System.out.println("      [" + i + "] '" + oldSymbol + "' (hash=" + System.identityHashCode(oldSymbol) + ")");
			
			// Look up the canonical symbol in the new alphabet
			if (newAlphabet.containsSymbol(oldSymbol)) {
				int symbolIdx = newAlphabet.getSymbolIndex(oldSymbol);
				canonicalSymbols[i] = newAlphabet.getSymbol(symbolIdx);
			} else {
				// Symbol not in new alphabet - this shouldn't happen if alphabet extension was done correctly
				System.err.println("      WARNING: Symbol '" + oldSymbol + "' from tree not in new alphabet!");
				canonicalSymbols[i] = oldSymbol; // Keep old symbol as fallback
			}
		}
		
		// Create new Word with canonical symbols
		Word<String> newDiscriminator = Word.fromList(java.util.Arrays.asList(canonicalSymbols));
		System.out.println("    New symbols (identity hashes):");
		for (int i = 0; i < newDiscriminator.length(); i++) {
			String newSymbol = newDiscriminator.getSymbol(i);
			System.out.println("      [" + i + "] '" + newSymbol + "' (hash=" + System.identityHashCode(newSymbol) + ")");
		}
		
		// Replace discriminator in node using reflection (since it's typically final)
		try {
			java.lang.reflect.Field discriminatorField = node.getClass().getDeclaredField("discriminator");
			discriminatorField.setAccessible(true);
			discriminatorField.set(node, newDiscriminator);
			System.out.println("    ✓ Discriminator canonicalized successfully");
		} catch (Exception e) {
			System.err.println("    ERROR: Could not update discriminator using reflection: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	// Recursively canonicalize discriminators and StateInfo in all child nodes
	Collection<Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>>> children = node.getChildEntries();
	for (Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>> entry : children) {
		canonicalizeTreeDiscriminators(entry.getValue(), newAlphabet);
	}
}

// Helper method to recursively collect tree information
private static int collectTreeInfo(
		AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> node,
		List<StateInfo<String, Word<Word<String>>>> states,
		List<Word<String>> discriminators,
		int currentDepth) {
		
		if (node.isLeaf()) {
			// Leaf node - contains StateInfo
			StateInfo<String, Word<Word<String>>> stateInfo = node.getData();
			if (stateInfo != null) {
				states.add(stateInfo);
			}
			return currentDepth;
		} else {
			// Internal node - contains discriminator
			Word<String> discriminator = node.getDiscriminator();
			if (discriminator != null) {
				discriminators.add(discriminator);
			}
			
			// Recursively process children
			int maxDepth = currentDepth;
			Collection<Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>>> children = node.getChildEntries();
			for (Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>> entry : children) {
				int childDepth = collectTreeInfo(entry.getValue(), states, discriminators, currentDepth + 1);
				maxDepth = Math.max(maxDepth, childDepth);
			}
			
			return maxDepth;
		}
	}
	
	// ============================================================================
	// COMPARE TWO TREES - Shows differences in StateInfo between products
	// ============================================================================
	private static void compareTreesBetweenProducts(
			MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree1,
			MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree2,
			int product1Num,
			int product2Num) {
		
		System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
		System.out.println("║  TREE COMPARISON: Product " + product1Num + " vs Product " + product2Num + "                      ║");
		System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
		
		if (tree1 == null || tree2 == null) {
			System.out.println("⚠ Cannot compare - one or both trees are null");
			return;
		}
		
		// Collect info from both trees
		List<StateInfo<String, Word<Word<String>>>> states1 = new ArrayList<>();
		List<Word<String>> disc1 = new ArrayList<>();
		int depth1 = collectTreeInfo(tree1.getRoot(), states1, disc1, 0);
		
		List<StateInfo<String, Word<Word<String>>>> states2 = new ArrayList<>();
		List<Word<String>> disc2 = new ArrayList<>();
		int depth2 = collectTreeInfo(tree2.getRoot(), states2, disc2, 0);
		
		System.out.println("COMPARISON SUMMARY:");
		System.out.println("  Product " + product1Num + " - States: " + states1.size() + ", Discriminators: " + disc1.size() + ", Depth: " + depth1);
		System.out.println("  Product " + product2Num + " - States: " + states2.size() + ", Discriminators: " + disc2.size() + ", Depth: " + depth2);
		System.out.println();
		
		// Find common and different discriminators
		Set<String> disc1Set = new HashSet<>();
		Set<String> disc2Set = new HashSet<>();
		for (Word<String> d : disc1) disc1Set.add(d.toString());
		for (Word<String> d : disc2) disc2Set.add(d.toString());
		
		Set<String> commonDisc = new HashSet<>(disc1Set);
		commonDisc.retainAll(disc2Set);
		
		Set<String> onlyInProduct1 = new HashSet<>(disc1Set);
		onlyInProduct1.removeAll(disc2Set);
		
		Set<String> onlyInProduct2 = new HashSet<>(disc2Set);
		onlyInProduct2.removeAll(disc1Set);
		
		System.out.println("DISCRIMINATOR COMPARISON:");
		System.out.println("  Common discriminators: " + commonDisc.size());
		System.out.println("  Only in Product " + product1Num + ": " + onlyInProduct1.size());
		System.out.println("  Only in Product " + product2Num + ": " + onlyInProduct2.size());
		
		if (!onlyInProduct1.isEmpty()) {
			System.out.println("\n  Discriminators only in Product " + product1Num + ":");
			for (String d : onlyInProduct1) {
				System.out.println("    - " + d);
			}
		}
		
		if (!onlyInProduct2.isEmpty()) {
			System.out.println("\n  Discriminators only in Product " + product2Num + ":");
			for (String d : onlyInProduct2) {
				System.out.println("    - " + d);
			}
		}
		
		System.out.println("\n═══════════════════════════════════════════════════════════════\n");
	}

	public static final String EQ = "eq";
	public static final String SOT = "sot";
	public static final String SOT2 = "sot2";
	public static final String HELP = "help";
	public static final String HELP_SHORT = "h";
	public static final String OT = "ot";
	public static final String CEXH = "cexh";
	public static final String CLOS = "clos";
	public static final String CACHE = "cache";
	public static final String SEED = "seed";
	public static final String OUT = "out";
	public static final String LEARN = "learn";
	public static final String INFO = "info";
	public static final String DIR = "dir";
	private static final String FM = "fm";

	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	public static final String[] eqMethodsAvailable = { "rndWalk", "rndWords", "wp", "wphyp", "w", "whyp", "wrnd",
			"wrndhyp" };
	public static final String[] closingStrategiesAvailable = { "CloseFirst", "CloseShortest" };
	private static final String RIVEST_SCHAPIRE_ALLSUFFIXES = "RivestSchapireAllSuffixes";
	public static final String[] cexHandlersAvailable = { "ClassicLStar", "MalerPnueli", "RivestSchapire",
			RIVEST_SCHAPIRE_ALLSUFFIXES, "Shahbaz", "Suffix1by1" };
	public static final String[] learningMethodsAvailable = { "lstar", "l1", "adaptive", "dlstar_v2", "dlstar_v1",
			"dlstar_v0", "ttt" };

	public static final Function<Map<String, String>, Pair<@Nullable String, @Nullable Word<String>>> MEALY_EDGE_WORD_STR_PARSER = attr -> {
		final String label = attr.get(VisualizationHelper.EdgeAttrs.LABEL);
		if (label == null) {
			return Pair.of(null, null);
		}

		final String[] tokens = label.split("/");

		if (tokens.length != 2) {
			return Pair.of(null, null);
		}

		Word<String> token2 = Word.epsilon();
		token2 = token2.append(tokens[1]);
		return Pair.of(tokens[0], token2);
	};

	public static final Word<String> OMEGA_SYMBOL = Word.fromLetter("Ω");


	public static CompactMealy<String, Word<String>> loadMealyMachineFromDot3(File f) throws Exception {

		Pattern kissLine = Pattern.compile("\\s*([a-zA-Z0-9]+)\\s+->\\s+([a-zA-Z0-9]+)\\s*\\[label=[\"<](.+)[\">]\\];?");
		System.out.println("VVVVVVVVVVVVVVVVVVV");
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		List<String[]> trs = new ArrayList<String[]>();
		System.out.println("CCCCCCCCCC");
		HashSet<String> abcSet = new HashSet<>();
		
		//		int count = 0;
		System.out.println("WWWWWWWW");
		while(br.ready()){
			String line = br.readLine();
			Matcher m = kissLine.matcher(line);
			if(m.matches()){
				//				System.out.println(m.group(0));
				//				System.out.println(m.group(1));
				//				System.out.println(m.group(2));
				//				System.out.println(m.group(3));
				//				System.out.println(m.group(4));

				String[] tr = new String[4];
				tr[0] = m.group(1);
				tr[1] = m.group(3); 
//    				if(!abc.contains(tr[1])){
//    					abc.add(tr[1]);
//    				}
//    				tr[2] = m.group(4);
				tr[3] = m.group(2);
				if(tr[1].contains("<br />")){
					String trr[] = tr[1].split("<br />");
					tr[1]=trr[0];
					tr[2]=trr[1];
					trr = tr[1].split(" \\| ");
					for (String string : trr) {
						String trrr[] = new String[4];
						trrr[0]= tr[0];
						trrr[1]= string;
						trrr[2]= tr[2];
						trrr[3]= tr[3];
						trs.add(trrr);
						abcSet.add(trrr[1]);
					}
				}else{
					String trr[] = tr[1].split("\\s*/\\s*");
					tr[1]=trr[0];
					tr[2]=trr[1];
					trs.add(tr);
					abcSet.add(tr[1]); 
				}
				
				
			}
			//			count++;
		}

		br.close();

		List abc = new ArrayList<>(abcSet);
		Collections.sort(abc);
		Alphabet<String> alphabet = Alphabets.fromCollection(abc);
		CompactMealy<String, Word<String>> mealym = new CompactMealy<String, Word<String>>(alphabet);

		Map<String,Integer> states = new HashMap<String,Integer>();
		Integer si=null,sf=null;

		Map<String,Word<String>> words = new HashMap<String,Word<String>>();		


		WordBuilder<String> aux = new WordBuilder<>();

		aux.clear();
		aux.append(OMEGA_SYMBOL);
		words.put(OMEGA_SYMBOL.toString(), aux.toWord());


		for (String[] tr : trs) {
			if(!states.containsKey(tr[0])) states.put(tr[0], mealym.addState());
			if(!states.containsKey(tr[3])) states.put(tr[3], mealym.addState());

			si = states.get(tr[0]);
			sf = states.get(tr[3]);

			if(!words.containsKey(tr[1])){
				aux.clear();
				aux.add(tr[1]);
				words.put(tr[1], aux.toWord());
			}
			if(!words.containsKey(tr[2])){
				aux.clear();
				aux.add(tr[2]);
				words.put(tr[2], aux.toWord());
			}
			mealym.addTransition(si, words.get(tr[1]).toString(), sf, words.get(tr[2]));
		}

		for (Integer st : mealym.getStates()) {
			for (String in : alphabet) {
				//				System.out.println(mealym.getTransition(st, in));
				if(mealym.getTransition(st, in)==null){
					mealym.addTransition(st, in, st, OMEGA_SYMBOL);
				}
			}
		}


		mealym.setInitialState(states.get("s0"));

		return mealym;
	}
	
	
	
        
        //dddddddddddddddddddddddddddddddd
    	
        private static CompactMealy<String, Word<String>> LoadMealy(File fsm_file) throws Exception {
            InputModelDeserializer<String, CompactMealy<String, Word<String>>> parser = 
                DOTParsers.mealy(MEALY_EDGE_WORD_STR_PARSER);

            CompactMealy<String, Word<String>> mealy = null;
            String fileName = fsm_file.getName();

            if (fileName.endsWith("txt")) {
                try {
                    mealy = Utils.getInstance().loadMealyMachine(fsm_file);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (fileName.endsWith("dot")) {
                try (InputStream is = new FileInputStream(fsm_file)) {
                	mealy = loadMealyMachineFromDot3(fsm_file);
                    mealy = parser.readModel(is).model;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return mealy;
        }
	// private static CompactMealy<String, Word<String>> LoadMealy(File fsm_file) throws Exception {
	// 	InputModelDeserializer<String, CompactMealy<String, Word<String>>> parser = DOTParsers
	// 			.mealy(MEALY_EDGE_WORD_STR_PARSER);

	// 	CompactMealy<String, Word<String>> mealy = null;
	// 	String fileName = fsm_file.getName();

	// 	if (fileName.endsWith("txt")) {
	// 		try {
	// 			mealy = Utils.getInstance().loadMealyMachine(fsm_file);
	// 		} catch (Exception e) {
	// 			e.printStackTrace();
	// 		}
	// 	}	 else if (fileName.endsWith("dot")) {
	// 		try {
	// 			mealy = parser.readModel(fsm_file).model;
	// 		} catch (IOException e) {
	// 			// TODO Auto-generated catch block
	// 			e.printStackTrace();
	// 		}
	// 		return mealy;
	// 	}
	// 	return null;
	// }

//            else if (fileName.endsWith("dot")) {
//                try (InputStream is = new FileInputStream(fsm_file)) {
//                	mealy = loadMealyMachineFromDot3(fsm_file);
//                    mealy = parser.readModel(is).model;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            return mealy;
//        }

	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@2


    private static EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> buildEqOracle(
            Random rnd_seed, CommandLine line, CompactMealy<String, Word<String>> mealyss,
            SUL<String, Word<String>> eq_sul) {
        MembershipOracle<String, Word<Word<String>>> oracleForEQoracle = new SULOracle<>(eq_sul);

        EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle;
        if (!line.hasOption(EQ)) {
            return new WpMethodEQOracle<>(oracleForEQoracle, 2);
        }

        double restartProbability;
        int maxSteps, maxTests, maxLength, minLength, maxDepth, minimalSize, rndLength, bound;
        long rnd_long;
        boolean resetStepCount;

        LearnLibProperties learn_props = LearnLibProperties.getInstance();

        restartProbability = learn_props.getRndWalk_restartProbability();
        maxSteps = learn_props.getRndWalk_maxSteps();
        resetStepCount = learn_props.getRndWalk_resetStepsCount();

        eqOracle = new RandomWalkEQOracle<String, Word<String>>(eq_sul, // sul
                restartProbability, // reset SUL w/ this probability before a step
                maxSteps, // max steps (overall)
                resetStepCount, // reset step count after counterexample
                rnd_seed // make results reproducible
        );

        return eqOracle;
    }

	
	public static void learnalgo(File productFile_2, String[] args,int product) throws Exception {

	}
	// Static arrays to store statistics for comparison
	private static long[][] productStats = new long[10][6]; // [product][metric]
	private static int[] productStates = new int[10];
	private static int[] productAlphabetSizes = new int[10];
	private static int[] newSymbolsAdded = new int[10];
	
	public static <I, O> void main(String[] args) throws Exception {

//         if (args.length < 1) {
//             System.err.println("Usage: LearnMealyWithKV <dot-file>");
//             System.exit(1);
//         }
		String[] a54= {".\\alternative_experiments\\Minepump_SPL\\products_3wise"
			,".\\alternative_experiments\\Minepump_SPL\\products_3wise"};
		String[] a213={"00001_fsm.dot","00002_fsm.dot"};
		
	for(int i=0;i<2;i++){
		File productFile_2 = new File(a54[i],a213[i]);
		System.out.println(productFile_2);
		System.out.print("Fvvvvvv");
		CompactMealy<String, Word<String>> mealyMachine;
		mealyMachine = LoadMealy(productFile_2);
		System.out.print(mealyMachine);

		// try {
		// mealyMachine = loadMealyMachineFromDot3(productFile_2);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		
		CommandLineParser parser = new BasicParser();
		// create the Options
		Options options = createOptions();
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		// logger.logEvent("\nSUL name: "+sul_file.getName());
		SUL<String, Word<String>> sulSim = new MealySimulatorSUL<>(mealyMachine, Utils.OMEGA_SYMBOL);

		StatisticSUL<String, Word<String>> mq_sym = new SymbolCounterSUL<>("MQ", sulSim);

		StatisticSUL<String, Word<String>> mq_rst = new ResetCounterSUL<>("MQ", mq_sym);

		long tstamp = System.currentTimeMillis();

		SUL<String, Word<String>> mq_sul = mq_rst;
		// random seed
		Random rnd_seed = new Random(tstamp);
		
		// timestamp
		Timestamp timestamp = new Timestamp(tstamp);

		CommandLine line;
		try {
			line = parser.parse(options, args);
		} catch (ParseException e) {
			formatter.printHelp("LearnMealyWithKV", options);
			return;
		}

	// Get the current product's input alphabet
	Alphabet<String> productAlphabet = mealyMachine.getInputAlphabet();
	
	System.out.println("\nProduct " + i + " alphabet contains " + productAlphabet.size() + " symbols:");
	for (String symbol : productAlphabet) {
		System.out.println("  - " + symbol);
	}
	
	// CRITICAL FIX: For Product 0, use ONLY current product's symbols
	// For Product 1+, combine with previous product's symbols for adaptive learning
	if (i == 0) {
		// Product 0: Start fresh with only its own symbols
		allInputAlphabets.clear();
		for (String symbol : productAlphabet) {
			System.out.println("EEE"+symbol);
			allInputAlphabets.add(symbol);
		}
	} else {
		// Product 1+: Add new symbols from current product to existing collection
		for (String symbol : productAlphabet) {
			if (!allInputAlphabets.contains(symbol)) {
				System.out.println("EEE"+symbol);
				allInputAlphabets.add(symbol);
			}
		}
	}
			
	System.out.println("Combined alphabet now has " + allInputAlphabets.size() + " unique symbols");
	Alphabet<String> combinedAlphabet = Alphabets.fromCollection(allInputAlphabets);
	
	for(String w23:combinedAlphabet){
		System.out.println("=== +"+ w23);
	}
	
	System.out.println();

	// KearnsVaziraniMealy<MealyMachine<?, String, ?, Word<String>>, String, Word<String>> learner = null;											
    IKearnsVaziraniMealy<String, Word<String>> learner=null;
	
	// Declare adaptive statistics variables outside if/else so they're accessible later
	StatisticSUL<String, Word<String>> mq_sym_adaptive = null;
	StatisticSUL<String, Word<String>> mq_rst_adaptive = null;

if (i==0){	
	// ========== PRODUCT 0: Initialize from scratch ==========
	// CRITICAL FIX: Create GrowingMapAlphabet with TRIMMED symbols to avoid whitespace issues
	// List<String> trimmedSymbols = new ArrayList<>();
	// for (String symbol : productAlphabet) {
	// 	String cleaned = symbol.trim();
	// 	trimmedSymbols.add(cleaned);
	// 	System.out.println("  Product 0 symbol: '" + cleaned + "' (length=" + cleaned.length() + ")");
	// }
	product1Alphabet = new GrowingMapAlphabet<>(Alphabets.fromCollection(allInputAlphabets));
	
	// Setup membership oracle 
	MembershipOracle<String, Word<Word<String>>> mqOracle = new SULOracle<String, Word<String>>(mq_sul);
	
	IKearnsVaziraniMealyBuilder<Object, String, Word<String>> builder = new IKearnsVaziraniMealyBuilder<>();
	builder.setOracle(mqOracle);
	builder.setAlphabet(combinedAlphabet);
	
	learner = (IKearnsVaziraniMealy<String, Word<String>>) builder.withAlphabet(product1Alphabet).create(null,null);
	System.out.println("Product " + i + ": Learning from scratch");
	System.out.println("  Initial alphabet size = " + product1Alphabet.size());
}

else{
	// ========== PRODUCT i: Incremental Adaptive Learning ==========
	System.out.println("Product " + i + ": adaptive learning");
	System.out.println("  Previous alphabet size: " + product1Alphabet.size());
	System.out.println("  Current product alphabet size: " + productAlphabet.size());
	
	// FIX 5: MQ oracle must handle ALL symbols from extended alphabet
	// The tree from Product 0 contains discriminators with Product 0's symbols
	// Product 1's MQ oracle must respond to BOTH Product 0 and Product 1 symbols
	
	// CRITICAL FIX: Extend product1Alphabet (reuse its symbol instances) then add new symbols
	// We MUST reuse Product 0's symbol instances so the tree's discriminators match!
	GrowingAlphabet<String> extendedAlphabet = new GrowingMapAlphabet<>(product1Alphabet);
	for (String symbol : productAlphabet) {
		// CRITICAL: Trim whitespace to ensure clean symbol matching
		// String cleanSymbol = symbol.trim();
		if (!extendedAlphabet.containsSymbol(symbol)) {
			extendedAlphabet.addSymbol(symbol);
			System.out.println("  DEBUG: Added new symbol '" + symbol + "' (length=" + symbol.length() + ")");
		}
	}
	
	// Create mealy with FULL extended alphabet for MQ oracle
	System.out.println("\n===== DEBUG: Creating mqMealy =====");
	System.out.println("extendedAlphabet size: " + extendedAlphabet.size());
	System.out.println("extendedAlphabet symbols (with identity hash):");
	for (String s : extendedAlphabet) {
		System.out.println("  Symbol: '" + s + "' | Length: " + s.length() + " | Hash: " + System.identityHashCode(s) + " | Bytes: " + java.util.Arrays.toString(s.getBytes()));
	}
	System.out.println("==================================\n");
	CompactMealy<String, Word<String>> mqMealy = new CompactMealy<>(extendedAlphabet);
	System.out.println("mqMealy alphabet size: " + mqMealy.getInputAlphabet().size());
	System.out.println("mqMealy alphabet == extendedAlphabet: " + (mqMealy.getInputAlphabet() == extendedAlphabet));
	System.out.println("mqMealy alphabet symbols (with identity hash):");
	for (String s : mqMealy.getInputAlphabet()) {
		System.out.println("  Symbol: '" + s + "' | Hash: " + System.identityHashCode(s));
	}
	System.out.println("==================================\n");
	
	// Copy structure from original mealy (current product)
	Map<Integer, Integer> stateMap = new HashMap<>();
	for (Integer state : mealyMachine.getStates()) {
		stateMap.put(state, mqMealy.addState());
	}
	mqMealy.setInitialState(stateMap.get(mealyMachine.getInitialState()));
	
	// Copy transitions ONLY for symbols that exist in CURRENT product
	System.out.println("\n===== DEBUG: Adding transitions from productAlphabet =====");
	int transitionCount = 0;
	for (Integer state : mealyMachine.getStates()) {
		for (String input : productAlphabet) {
			Integer succ = mealyMachine.getSuccessor(state, input);
			Word<String> output = mealyMachine.getOutput(state, input);
			if (succ != null) {
				try {
					// CRITICAL FIX: Trim and look up the symbol in extendedAlphabet to get the canonical instance
					// Alphabets use object identity, not string equality, for symbol lookups
					// String cleanInput = input.trim();
//****************************************************************************** */
					
					System.out.println("  - Heyyyyyyyyyyyyyyyy: '" + input + "'");
									
					String cleanInput = input;
					int symbolIdx = extendedAlphabet.getSymbolIndex(cleanInput);
					String canonicalSymbol = extendedAlphabet.getSymbol(symbolIdx);
					mqMealy.addTransition(stateMap.get(state), canonicalSymbol, stateMap.get(succ), output);
					transitionCount++;
					if (input.contains("lowLevel")) {
						System.out.println("  Added transition for 'lowLevel': state=" + state + ", input='" + input + "', canonical='" + canonicalSymbol + "', inputHash=" + System.identityHashCode(input) + ", canonicalHash=" + System.identityHashCode(canonicalSymbol));
					}
				} catch (IllegalArgumentException e) {
					System.out.println("ERROR: Symbol '" + input + "' (trimmed: '" + input.trim() + "') from productAlphabet not in extendedAlphabet!");
					System.out.println("  input identity hash: " + System.identityHashCode(input));
					System.out.println("  productAlphabet size: " + ((Alphabet<?>)productAlphabet).size());
					System.out.println("  extendedAlphabet size: " + extendedAlphabet.size());
					System.out.println("  extendedAlphabet symbols:");
					for (String s : extendedAlphabet) {
						System.out.println("    '" + s + "' (hash=" + System.identityHashCode(s) + ")");
					}
					throw e;
				}
			}
		}
	}
	System.out.println("Total transitions added from productAlphabet: " + transitionCount);
	System.out.println("==================================\n");
	
	// Add self-loops with OMEGA for ALL symbols NOT in current product
	// This includes symbols from previous products (e.g., startCmd, stopCmd from Product 0)
	// AND symbols that will be in future products
	System.out.println("\n===== DEBUG: Adding OMEGA self-loops for symbols not in productAlphabet =====");
	int omegaSymbolCount = 0;
	for (String symbol : extendedAlphabet) {
		if (!productAlphabet.containsSymbol(symbol)) {
			System.out.println("  Adding OMEGA self-loops for symbol: '" + symbol + "' (hash=" + System.identityHashCode(symbol) + ")");
			for (Integer state : mqMealy.getStates()) {
				mqMealy.addTransition(state, symbol, state, Utils.OMEGA_SYMBOL);
			}
			omegaSymbolCount++;
		}
	}
	System.out.println("Total symbols with OMEGA self-loops: " + omegaSymbolCount);
	System.out.println("==================================\n");
	
	// Create MQ SUL and oracle with extended mealy
	SUL<String, Word<String>> mqSulSim = new MealySimulatorSUL<>(mqMealy, Utils.OMEGA_SYMBOL);
	mq_sym_adaptive = new SymbolCounterSUL<>("MQ", mqSulSim);
	mq_rst_adaptive = new ResetCounterSUL<>("MQ", mq_sym_adaptive);
	SUL<String, Word<String>> mq_sul_adaptive = mq_rst_adaptive;
	MembershipOracle<String, Word<Word<String>>> mqOracle = new SULOracle<String, Word<String>>(mq_sul_adaptive);
	
	IKearnsVaziraniMealyBuilder<Object, String, Word<String>> builder = new IKearnsVaziraniMealyBuilder<>();
	builder.setOracle(mqOracle);
	builder.setAlphabet(combinedAlphabet);
	
	// Reuse tree AND hypothesis from previous product
	// But first, we need to update the hypothesis to use the extended alphabet
	// Create a new hypothesis with extended alphabet and copy structure from previous
	CompactMealy<String, Word<String>> adaptedHypothesis = new CompactMealy<>(extendedAlphabet);
	
	// Copy all states from previous hypothesis
	Map<Integer, Integer> stateMap2 = new HashMap<>();
	for (Integer oldState : previousHypothesis.getStates()) {
		Integer newState = adaptedHypothesis.addState();
		stateMap2.put(oldState, newState);
	}
	adaptedHypothesis.setInitialState(stateMap2.get(previousHypothesis.getInitialState()));
	
	// Copy transitions for symbols that exist in previous product
	for (Integer oldState : previousHypothesis.getStates()) {
		Integer newState = stateMap2.get(oldState);
		for (String symbol : product1Alphabet) {  // product1Alphabet is from Product 0
			Integer oldSucc = previousHypothesis.getSuccessor(oldState, symbol);
			Word<String> output = previousHypothesis.getOutput(oldState, symbol);
			if (oldSucc != null) {
				adaptedHypothesis.addTransition(newState, symbol, stateMap2.get(oldSucc), output);
			}
		}
	}
	
	// Add placeholder transitions for NEW symbols (will be refined during learning)
	// These are symbols in extendedAlphabet but not in product1Alphabet
	for (String symbol : extendedAlphabet) {
		if (!product1Alphabet.containsSymbol(symbol)) {
			// Add self-loops with OMEGA for new symbols as placeholders
			for (Integer newState : adaptedHypothesis.getStates()) {
				adaptedHypothesis.addTransition(newState, symbol, newState, Utils.OMEGA_SYMBOL);
			}
		}
	}
	
	// Update StateInfo IDs in the tree to match the new state mapping
	
	updateStateInfoInTree(tree_round2.getRoot(), stateMap);
	
	// CRITICAL: Canonicalize discriminators in tree to use new alphabet's symbol instances
	// Without this, oracle queries will fail with IllegalArgumentException due to symbol identity mismatch
	System.out.println("\n========== CANONICALIZING TREE DISCRIMINATORS ==========");
	System.out.println("Replacing discriminator symbols from old alphabet with new alphabet instances");
	canonicalizeTreeDiscriminators(tree_round2.getRoot(), extendedAlphabet);
	System.out.println("========================================================\n");
	
	System.out.println("  Adapted hypothesis: " + previousHypothesis.size() + " states → " + adaptedHypothesis.size() + " states");
	System.out.println("  Alphabet extended: " + product1Alphabet.size() + " → " + extendedAlphabet.size() + " symbols");
	System.out.println("  Tree StateInfo updated to match new hypothesis");
	System.out.println("  Tree discriminators canonicalized to new alphabet");
	
	learner = (IKearnsVaziraniMealy<String, Word<String>>) builder.withAlphabet(extendedAlphabet).create(tree_round2, adaptedHypothesis);
	
	System.out.println("  Learner created with alphabet size: " + learner.get_alphabet_symbol().size());
	System.out.println("==================================================\n");
	
	// Visualize reused tree (only if tree was actually reused)
	if (tree_round2 != null) {
		System.out.println("DISCRIMINATION TREE (reused from product " + (i-1) + "):");
		// MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> treeinit = learner.getDiscriminationTree();
		Visualization.visualize(tree_round2, true);
	} 
	else {
		System.out.println("Learning from scratch (no tree reuse)");
	}
	
	// ========== ANALYZE LOADED TREE FOR PRODUCT i ==========
	System.out.println("\n========== LOADED TREE ANALYSIS (Product " + i + ") ==========");
	System.out.println("Loaded tree_round2 from Product " + (i-1));
	System.out.println("Current product alphabet has " + productAlphabet.size() + " symbols:");
	for (String s : productAlphabet) {
		System.out.print(s + " ");
	}
	
	System.out.println("\nLearner's alphabet after loading tree has " + learner.get_alphabet_symbol().size() + " symbols:");
	for (String s : learner.get_alphabet_symbol()) {
		System.out.print(s + " ");
	}
	System.out.println();
	
	System.out.println("\nSymbols in learner alphabet but NOT in current product:");
	for (String s : learner.get_alphabet_symbol()) {
		if (!productAlphabet.containsSymbol(s)) {
			System.out.print("  '" + s + "' ");
		}
	}
	System.out.println();
	
	System.out.println("\nSymbols in current product but NOT in learner alphabet:");
	for (String s : productAlphabet) {
		if (!learner.get_alphabet_symbol().containsSymbol(s)) {
			System.out.print("  '" + s + "' ");
		}
	}
	System.out.println();
	System.out.println("========================================================\n");
}

	// Create a new Mealy machine with updated alphabet for equivalence oracle
	CompactMealy<String, Word<String>> updatedMealy;
	if (i > 0) {
		// For adaptive learning, create mealy with learner's alphabet
		Alphabet<String> learnerAlphabet = learner.get_alphabet_symbol();
		updatedMealy = new CompactMealy<>(learnerAlphabet);
		
		// Copy structure from original mealy
		Map<Integer, Integer> stateMap = new HashMap<>();
		for (Integer state : mealyMachine.getStates()) {
			stateMap.put(state, updatedMealy.addState());
		}
		updatedMealy.setInitialState(stateMap.get(mealyMachine.getInitialState()));
		
		// Copy transitions for symbols that exist in product
		for (Integer state : mealyMachine.getStates()) {
			for (String input : productAlphabet) {
				Integer succ = mealyMachine.getSuccessor(state, input);
				Word<String> output = mealyMachine.getOutput(state, input);
				if (succ != null) {
					updatedMealy.addTransition(stateMap.get(state), input, stateMap.get(succ), output);
				}
			}
		}
		
		// Add self-loops for ALL symbols not in current product
		for (String symbol : learnerAlphabet) {
			if (!productAlphabet.containsSymbol(symbol)) {
				for (Integer state : updatedMealy.getStates()) {
					updatedMealy.addTransition(state, symbol, state, Utils.OMEGA_SYMBOL);
				}
			}
		}
	} else {
		updatedMealy = mealyMachine;
	}
	
	// Create SUL and EQ oracle with updated mealy
	SUL<String, Word<String>> eqSulSim = new MealySimulatorSUL<>(updatedMealy, Utils.OMEGA_SYMBOL);
	StatisticSUL<String, Word<String>> eq_sym = new SymbolCounterSUL<>("EQ", eqSulSim);
	StatisticSUL<String, Word<String>> eq_rst = new ResetCounterSUL<>("EQ", eq_sym);
	SUL<String, Word<String>> eq_sul = eq_rst;
	
	EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle = null;
	eqOracle = buildEqOracle(rnd_seed, line, updatedMealy, eq_sul);
	// Use the learner's alphabet for the experiment (already normalized and extended if needed)
	Experiment.MealyExperiment<String, Word<String>> experiment = 
	new Experiment.MealyExperiment<String, Word<String>>(learner, eqOracle, learner.get_alphabet_symbol());
		// (de.learnlib.api.oracle.EquivalenceOracle<? super net.automatalib.automata.transducers.MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>>)
		// Experiment.MealyExperiment<String, Word<String>> experiment = 
		// new Experiment.MealyExperiment<String, Word<String>>(eqOracle);
		
		int[] statistics_array=new int[6];
	// ========== ANALYZE TREE_ROUND2 STRUCTURE ==========
	if (tree_round2 != null) {
		System.out.println("\n========== TREE_ROUND2 ANALYSIS ==========");
		System.out.println("Tree class: " + tree_round2.getClass().getName());
		System.out.println("Tree root: " + tree_round2.getRoot());
		
		// Print alphabet that was used to build this tree
		System.out.println("\nAlphabet used for tree_round2:");
		System.out.println("  Alphabet class: " + product1Alphabet.getClass().getName());
		System.out.println("  Alphabet size: " + product1Alphabet.size());
		System.out.println("  Symbols in alphabet:");
		int idx = 0;
		for (String symbol : product1Alphabet) {
			System.out.println("    [" + idx + "] " + symbol);
			idx++;
		}
		
		// Try to analyze discriminators in the tree
		System.out.println("\nTree structure analysis:");
		try {
			// Get the root node and analyze
			var root = tree_round2.getRoot();
			System.out.println("  Root node type: " + root.getClass().getName());
			System.out.println("  Root is leaf: " + (root.isLeaf()));
			
			if (!root.isLeaf()) {
				var discriminator = root.getDiscriminator();
				System.out.println("  Root discriminator: " + discriminator);
				System.out.println("  Root discriminator class: " + discriminator.getClass().getName());
				
				// Try to get children entries
				try {
					var childEntries = root.getChildEntries();
					System.out.println("  Number of children: " + childEntries.size());
					
					// Analyze each child
					int childIdx = 0;
					for (var childEntry : childEntries) {
						System.out.println("\n  Child " + childIdx + ":");
						System.out.println("    Output key: " + childEntry.getKey());
						System.out.println("    Child node: " + childEntry.getValue());
						childIdx++;
					}
				} catch (Exception e2) {
					System.out.println("  Could not access children: " + e2.getMessage());
				}
			} else {
				System.out.println("  Root is a leaf node (contains state info)");
				System.out.println("  State info: " + root.getData());
			}
		} catch (Exception e) {
			System.out.println("  Error analyzing tree: " + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("==========================================\n");
	}	
	// FIX 4: Always run with false to avoid premature tree capture at round 4
	// We need the COMPLETE tree from Product 0, not the incomplete round-4 tree
	// The round-4 tree captured at round 4 only has Product 0's alphabet
	// and doesn't have proper structure for alphabet extension in Product 1
	
	if(i==0){
		experiment.run(true, null);
	}
	else{
		System.out.println("\n======= Starting Product " + i + " with previous hypothesis =======");
		System.out.println("Previous hypothesis states: " + (previousHypothesis != null ? previousHypothesis.size() : "null"));
		System.out.println("==============================================================\n");
		// FIX: Don't pass previousHypothesis to experiment.run()
		// The learner has already been initialized with the tree and will build its own hypothesis
		// with the extended alphabet. Passing previousHypothesis causes a mismatch between
		// the hypothesis used for EQ checking and the learner's internal hypothesis.
		experiment.run(false, null);
	}
	
		// ========== SAVE FOR NEXT PRODUCT: Update tree AND alphabet ==========
	MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree = learner.getDiscriminationTree();
	MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree_before_save = tree_round2; // Save old tree for comparison
	tree_round2 = experiment.getDiscrtree();  // Tree for next product
	product1Alphabet = (GrowingAlphabet<String>) learner.get_alphabet_symbol();  // Updated alphabet
	
	// Save hypothesis for next product
	previousHypothesis = (CompactMealy<String, Word<String>>) experiment.getFinalHypothesis();
	
	System.out.println("Saved for next product:");
	// System.out.println("  Tree depth/size: " + tree.getRoot().subtreeSize());
	System.out.println("  Alphabet size: " + product1Alphabet.size());
	System.out.println("  Hypothesis saved with " + previousHypothesis.size() + " states");
	
	// ========== CALL TREE ANALYSIS ==========
	System.out.println("\n");
	System.out.println("█████████████████████████████████████████████████████████████████");
	System.out.println("█         ANALYZING DISCRIMINATION TREE FOR PRODUCT " + i + "          █");
	System.out.println("█████████████████████████████████████████████████████████████████");
	analyzeTreeStructureDetailed(tree_round2, i, productAlphabet);
	
	// If this is Product 1, compare with Product 0's tree
	if (i == 1 && tree_before_save != null) {
		compareTreesBetweenProducts(tree_before_save, tree_round2, 0, 1);
	}
	

		
		StatisticSUL<String, Word<String>> currentMqRst = (i == 0) ? mq_rst : mq_rst_adaptive;
		StatisticSUL<String, Word<String>> currentMqSym = (i == 0) ? mq_sym : mq_sym_adaptive;
		
		statistics_array[0] += experiment.getRounds().getCount();
		statistics_array[1] += ExtractValue(currentMqRst.getStatisticalData().getSummary());
		statistics_array[2] += ExtractValue(currentMqSym.getStatisticalData().getSummary());
		statistics_array[3] += ExtractValue(eq_rst.getStatisticalData().getSummary());
		statistics_array[4] += ExtractValue(eq_sym.getStatisticalData().getSummary());
		System.out.println(currentMqRst.getStatisticalData());
		
		for (int j=0;j<5;j++){
			System.out.println("vaa"+statistics_array[j]);
		}
		
	// ========== METRICS FOR ADAPTIVE LEARNING EVALUATION ==========
	System.out.println("\n========== PRODUCT " + i + " LEARNING COMPLETED ==========");
	System.out.println("Final hypothesis states: " + experiment.getFinalHypothesis().getStates().size());
	System.out.println("Rounds (EQ queries): " + experiment.getRounds().getCount());
	System.out.println("Membership queries - Resets: " + ExtractValue(currentMqRst.getStatisticalData().getSummary()));
	System.out.println("Membership queries - Symbols: " + ExtractValue(currentMqSym.getStatisticalData().getSummary()));
	System.out.println("Equivalence queries - Resets: " + ExtractValue(eq_rst.getStatisticalData().getSummary()));
	System.out.println("Equivalence queries - Symbols: " + ExtractValue(eq_sym.getStatisticalData().getSummary()));
	if (i > 0) {
		System.out.println("*** ADAPTIVE LEARNING BENEFIT ***");
		System.out.println("  Reused tree from product " + (i-1));
		System.out.println("  Alphabet extended: " + (learner.get_alphabet_symbol().size() - productAlphabet.size()) + " symbols");
	}
	System.out.println("====================================================\n");
	
	// Store statistics for final comparison
	productStats[i][0] = experiment.getRounds().getCount(); // Rounds
	productStats[i][1] = ExtractValue(currentMqRst.getStatisticalData().getSummary()); // MQ Resets
	productStats[i][2] = ExtractValue(currentMqSym.getStatisticalData().getSummary()); // MQ Symbols
	productStats[i][3] = ExtractValue(eq_rst.getStatisticalData().getSummary()); // EQ Resets
	productStats[i][4] = ExtractValue(eq_sym.getStatisticalData().getSummary()); // EQ Symbols
	productStates[i] = experiment.getFinalHypothesis().getStates().size();
	productAlphabetSizes[i] = learner.get_alphabet_symbol().size();
	if (i > 0) {
		newSymbolsAdded[i] = learner.get_alphabet_symbol().size() - productAlphabetSizes[i-1];
	}
		LearnLogger logger = LearnLogger.getLogger(Infer_LearnLib.class);

		SimpleProfiler.logResults();
		
		
		// learning statistics
		logger.logConfig("Rounds: "+experiment.getRounds().getCount());
		logger.logStatistic(mq_rst.getStatisticalData());
		logger.logStatistic(mq_sym.getStatisticalData());
		logger.logStatistic(eq_rst.getStatisticalData());
		logger.logStatistic(eq_sym.getStatisticalData());
		
		
		
		SimpleProfiler.logResults();

//		MealyMachine finalHyp = (MealyMachine) experiment.getFinalHypothesis();
//		Visualization.visualize(((GraphViewable) experiment.getFinalHypothesis()).graphView(), true);
////		777777777777777777777777777777777777
//		GraphViewable looplessGraph = GraphUtils.createSpanningTree((GraphViewable) experiment.getFinalHypothesis());
//		Visualization.visualize(looplessGraph.graphView(), true);
//
//		// Option 2: Pass a parameter to exclude loops
//		Visualization.visualize(((GraphViewable) experiment.getFinalHypothesis()).graphView(), true, false); // Last param indicates "no loops"
//
//		// Option 3: Filter loops explicitly
//		Graph originalGraph = ((GraphViewable) experiment.getFinalHypothesis()).graphView();
//		Graph filteredGraph = GraphUtils.removeLoops(originalGraph);
//		Visualization.visualize(filteredGraph, true);
		
//		*/777777777777777777777777777777777777
		System.out.println("********************Visualizing discrimination tree********************");
		Visualization.visualize(tree, true);
		// Print the tree structure
		System.out.println("********************Discrimination Tree Structure********************");
		
		// ################################################################
		// traverseAndPrintTree(tree.getRoot(), "", true);
		// ################################################################		
		
		
		VisualizationHelper a=tree.getVisualizationHelper();
		
		System.out.print("ddddddddddddddd"+a);
		tree.getVisualizationHelper();

		
		// System.out.print(tree.);
		// dotgen.visualizeTree();
		
		System.out.println();
		Alphabet<String> alphabet = mealyMachine.getInputAlphabet();

		CompactMealy<String, Word<String>> learnedModel = new CompactMealy<>(alphabet);
		MealyMachine<?, String, ?, Word<String>> finalHyp = (MealyMachine<?, String, ?, Word<String>>) experiment.getFinalHypothesis();


	
		// Output statistics
		System.out.println("Learning completed.");
		System.out.println("Statistics:");
		// System.out.println(" Membership queries: " + mqCounter.getQueryCount());
		System.out.println(" MQ resets: " + experiment.getRounds().getCount());
		// System.out.println(" Symbol count (MQ): " + mqCounter.getSymbolCount());
//		System.out.println("  Equivalence queries: " + experiment.getRounds().getCount());
//		System.out.println("  EQ resets: " + eqResetCounter.getCount());
//		System.out.println("  Total resets: " + (mqResetCounter.getCount() + eqResetCounter.getCount()));
		System.out.println("Final hypothesis states: " + finalHyp.size());


// Get the learned model - correct approach

// Copy the hypothesis structure into the compact representation
     // Get the discrimination tree

        // Traverse and print the discrimination tree
        System.out.println("Discrimination Tree Structure:");

        boolean equivalent = DeterministicEquivalenceTest.findSeparatingWord(
			mealyMachine, finalHyp, alphabet) == null;
        System.out.println("Models are equivalent: " + equivalent);
        
        // Visualize the learned model (optional)
        System.out.println("Visualizing learned model...");
        Visualization.visualize(finalHyp, alphabet);
//		Visualization.visualize(((GraphViewable) experiment.getFinalHypothesis()).graphView(), true);

        // Visualize the original model for comparison (optional)
        System.out.println("Visualizing original model...");
        Visualization.visualize(mealyMachine, alphabet);
		
	}
	//lllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll
	
	// Print comprehensive comparison summary
	System.out.println("\n\n");
	System.out.println("╔════════════════════════════════════════════════════════════════╗");
	System.out.println("║         ADAPTIVE LEARNING RESULTS SUMMARY                      ║");
	System.out.println("╚════════════════════════════════════════════════════════════════╝");
	System.out.println();
	
	// Product 0 Results
	System.out.println("Product 0:"+a213[0]);
	System.out.println("  Rounds: " + productStats[0][0]);
	System.out.println("  MQ Resets: " + productStats[0][1] + ", Symbols: " + productStats[0][2]);
	System.out.println("  EQ Resets: " + productStats[0][3] + ", Symbols: " + productStats[0][4]);
	System.out.println("  States: " + productStates[0]);
	System.out.println("  Alphabet: " + productAlphabetSizes[0] + " symbols");
	System.out.println();
	
	// Product 1 Results
	System.out.println("Product 1 (Adaptive):"+a213[1]);
	System.out.println("  Rounds: " + productStats[1][0]);
	System.out.println("  MQ Resets: " + productStats[1][1] + ", Symbols: " + productStats[1][2]);
	System.out.println("  EQ Resets: " + productStats[1][3] + ", Symbols: " + productStats[1][4]);
	System.out.println("  States: " + productStates[1]);
	System.out.println("  Alphabet: " + productAlphabetSizes[1] + " symbols (" + productAlphabetSizes[0] + " + " + newSymbolsAdded[1] + " new)");
	System.out.println("  ✓ Tree reused from product 0");
	System.out.println("  ✓ " + newSymbolsAdded[1] + " new symbols added");
	System.out.println();
	
	System.out.println("════════════════════════════════════════════════════════════════");
	System.out.println();
	
	System.out.println("YYYYYYYYYYYYYYYYYYYYYYYY");
	for (String symbol : allInputAlphabets) {
		System.out.println(symbol);
	}
	
	// Create a combined Alphabet<String> with all collected symbols
	System.out.println("\n========================================================");
	System.out.println("CREATING COMBINED ALPHABET FROM ALL PRODUCTS");
	System.out.println("========================================================");
	
	// Convert our ArrayList of collected symbols to an Alphabet<String>
	Alphabet<String> combinedAlphabet2 = Alphabets.fromCollection(allInputAlphabets);
	
	System.out.println("Combined alphabet created successfully!");
	System.out.println("It contains " + combinedAlphabet2.size() + " unique symbols:");
	
	// Print all symbols in the combined alphabet
	int count = 1;
	System.out.println("VVVVVVVVVVVVVVV");
	for (String symbol : combinedAlphabet2) {
		System.out.println(count + ". " + symbol);
		count++;
	}
	
	// Create a Mealy machine with the combined alphabet using our helper method
	// CompactMealy<String, Word<String>> combinedMealyMachine = createMealyWithCombinedAlphabet();
	
	// System.out.println("\nCreated a new Mealy machine with the combined alphabet");
	// System.out.println("Initial state: " + combinedMealyMachine.getInitialState());
	// System.out.println("Alphabet size: " + combinedMealyMachine.getInputAlphabet().size() + " symbols");
	
		// File productFile_2 = new File(
				// "E:\\learning\\Projectpayan\\software\\SPL_Learning\\experiments\\BCS_SPL\\products_3wise",
				// "00001_fsm.dot");
	
		
		// Create file and call learnalgo
		
	}
	
	private static Options createOptions() {
		// create the Options 
		Options options = new Options();
		options.addOption(SOT, false, "Save observation table (OT)");
		options.addOption(SOT2, true, "Save observation table (OT) to the disk");
		options.addOption(HELP, false, "Shows help");
		options.addOption(OT, true, "Load observation table (OT)");
		options.addOption(OUT, true, "Set output directory");
		options.addOption(CLOS, true,
				"Set closing strategy." + "\nOptions: {" + String.join(", ", closingStrategiesAvailable) + "}");
		options.addOption(EQ, true,
				"Set equivalence query generator." + "\nOptions: {" + String.join(", ", eqMethodsAvailable) + "}");
		options.addOption(CEXH, true, "Set counter example (CE) processing method." + "\nOptions: {"
				+ String.join(", ", cexHandlersAvailable) + "}");
		options.addOption(CACHE, false, "Use caching.");
		options.addOption(LEARN, true,
				"Model learning algorithm." + "\nOptions: {" + String.join(", ", learningMethodsAvailable) + "}");
		options.addOption(SEED, true, "Seed used by the random generator");
		options.addOption(INFO, true, "Add extra information as string");
		options.addOption(DIR, true, "Directory of the SPL products");
		options.addOption(FM, true, "Feature model");
		// File productFile_2 = new File(
				// "E:\\learning\\Projectpayan\\software\\SPL_Learning\\experiments\\Minepump_SPL\\products_3wise",
				// "00001_fsm.dot");
		return options;
	}

	/**
	 * Creates an Alphabet<String> from the collected input symbols and builds a Mealy machine with it
	 * 
	//  * @return A new Mealy machine with the combined alphabet
	//  */
	// private static CompactMealy<String, Word<String>> createMealyWithCombinedAlphabet() {
	// 	// Convert our ArrayList of collected symbols to an Alphabet<String>
	// 	Alphabet<String> combinedAlphabet = Alphabets.fromCollection(allInputAlphabets);
		
	// 	// Create a Mealy machine with this combined alphabet
	// 	CompactMealy<String, Word<String>> combinedMealyMachine = new CompactMealy<>(combinedAlphabet);
		
	// 	// Add an initial state
	// 	int initialState = combinedMealyMachine.addInitialState();
		
	// 	return combinedMealyMachine;
	// }
}
