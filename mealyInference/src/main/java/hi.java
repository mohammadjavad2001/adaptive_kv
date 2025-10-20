import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.RandomWMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.WMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.WpMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.Infer_LearnLib;
import br.usp.icmc.labes.mealyInference.utils.LearnLibProperties;
import br.usp.icmc.labes.mealyInference.utils.Utils;
import de.learnlib.algorithms.kv.KearnsVaziraniMealy;


// import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;

import de.learnlib.algorithms.kv.KearnsVaziraniMealyBuilder;
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
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import java.util.*;
import net.automatalib.words.Alphabet;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.visualization.Visualization;
import net.automatalib.visualization.VisualizationHelper.EdgeAttrs;

// import de.learnlib.ds.MultiDTree;
import de.learnlib.datastructure.discriminationtree.MultiDTree;


import de.learnlib.ds.AbstractWordBasedDTNode;
import de.learnlib.algorithms.kv.StateInfo;
import net.automatalib.graphs.concepts.GraphViewable;
import de.learnlib.ds.JointCounterOracle;
import de.learnlib.filter.statistic.oracle.CounterSymbolQueryOracle;


public class hi<
         I extends java.lang.Object,
         O extends java.lang.Object> {

	// Add static variable to store tree between method calls
	static MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree_round2 = null;
	// MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> tree_round2 = null;
	// Add static ArrayList to store all input alphabets
	private static ArrayList<String> allInputAlphabets = new ArrayList<>();

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
	

	private static CompactMealy<String, Word<String>> LoadMealy(File fsm_file) throws Exception {
		InputModelDeserializer<String, CompactMealy<String, Word<String>>> parser = DOTParsers
				.mealy(MEALY_EDGE_WORD_STR_PARSER);

		CompactMealy<String, Word<String>> mealy = null;
		String fileName = fsm_file.getName();

		if (fileName.endsWith("txt")) {
			try {
				mealy = Utils.getInstance().loadMealyMachine(fsm_file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	 else if (fileName.endsWith("dot")) {
			try {
				mealy = parser.readModel(fsm_file).model;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return mealy;
		}
		return null;
	}

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
	// if (!line.hasOption(EQ)) {
	// 	return new WpMethodEQOracle<>(oracleForEQoracle, 2);
	// }

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
			mealyMachine = loadMealyMachineFromDot3(productFile_2);
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

		StatisticSUL<String, Word<String>> eq_sym = new SymbolCounterSUL<>("EQ", sulSim);
		StatisticSUL<String, Word<String>> eq_rst = new ResetCounterSUL<>("EQ", eq_sym);

		SUL<String, Word<String>> eq_sul = eq_rst;

		// Create a SUL (System Under Learning) from the Mealy machine
		MealySimulatorSUL<String, Word<String>> sul = new MealySimulatorSUL<>(mealyMachine);
		EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle = null;
		eqOracle = buildEqOracle(rnd_seed, line, mealyMachine, eq_sul);

		// Set up membership oracle with counters
		SymbolCounterSUL<String, Word<String>> mqSym = new SymbolCounterSUL<>("MQ", sul);
		ResetCounterSUL<String, Word<String>> mqRst = new ResetCounterSUL<>("MQ", mqSym);
		MembershipOracle<String, Word<Word<String>>> mqOracle = new SULOracle<String, Word<String>>(mq_sul);

		// JointCounterOracle<String, Word<Word<String>>> mqCounter = new JointCounterOracle<>(mqOracle);
		// CounterSymbolQueryOracle<String, Word<Word<String>>> mqCounter = 
		// new CounterSymbolQueryOracle<>(mqOracle);

		//***************************************
//		CounterSymbolQueryOracle<String, Word<Word<String>>> symbolCounter = 
//			    new CounterSymbolQueryOracle<>(membershipOracle);

		//***************************************
		
				// de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealyBuilder<String, Word<String>> builder = new de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealyBuilder<String, Word<String>>();



		// Alphabet<String> alphbe22 = mealyMachine.getInputAlphabet();
		
		// Get the current product's input alphabet
		
		Alphabet<String> productAlphabet = mealyMachine.getInputAlphabet();
		
		// Add all symbols from this product's alphabet to our combined collection
		System.out.println("\nProduct " + i + " alphabet contains " + productAlphabet.size() + " symbols:");
		for (String symbol : productAlphabet) {
			System.out.println("  - " + symbol);
			// Only add if it's not already in our collection (avoid duplicates)
			if (!allInputAlphabets.contains(symbol)) {
				System.out.println("EEE"+symbol);
				allInputAlphabets.add(symbol);
			}
		}
				
		System.out.println("Combined alphabet now has " + allInputAlphabets.size() + " unique symbols");
		Alphabet<String> combinedAlphabet = Alphabets.fromCollection(allInputAlphabets);
		

		for(String w23:combinedAlphabet){
			System.out.println("=== +"+ w23);
		}
		
		KearnsVaziraniMealyBuilder<Object, String, Word<String>> builder = new KearnsVaziraniMealyBuilder<>();
		builder.setOracle(mqOracle);
		builder.setAlphabet(combinedAlphabet); 

		
		Alphabet<String> a413 = mealyMachine.getInputAlphabet();
		System.out.println();

		// KearnsVaziraniMealy<MealyMachine<?, String, ?, Word<String>>, String, Word<String>> learner = null;											
        KearnsVaziraniMealy<String, Word<String>> learner=null;

		if (i==0){	
			learner = builder.withAlphabet(productAlphabet).create(null);
		}
		
		else{
			learner = builder.withAlphabet(combinedAlphabet).create(tree_round2);
			// learner = builder.withAlphabet(combinedAlpha.bet).create(tree_round2);
			// builder.setAlphabet(productAlphabet); 
			System.out.println("Learner Alphabet Symbols++++++++++++");
			System.out.println(learner.get_alphabet_symbol());
			
			System.out.println("ROUND@@@@@@INIT DISCRIMINATION TREE");
			MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> treeinit = learner.getDiscriminationTree();
			Visualization.visualize(treeinit, true);
			System.out.println("RRRRRRRRRRRRRRRRRRRR");
		}
		// if (i == 1 && tree_round2 != null) {
				// System.out.println("BBBBBBBBB"EEE);
			// builder.setDiscriminationTree(tree_round2);
			// builder.setDiscriminationTree(tree_round2);
		// }
		//settttttt
					
		// MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> discriminationTree = new MultiDTree<I, Word<O>, StateInfo<I, Word<O>>>;
		// MealyMachine<?, String, ?, Word<String>> ghooz = null;
					
		Experiment.MealyExperiment<String, Word<String>> experiment = 
		new Experiment.MealyExperiment<String, Word<String>>(learner, eqOracle, combinedAlphabet);
		// (de.learnlib.api.oracle.EquivalenceOracle<? super net.automatalib.automata.transducers.MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>>)
		// Experiment.MealyExperiment<String, Word<String>> experiment = 
		// new Experiment.MealyExperiment<String, Word<String>>(eqOracle);
		
		int[] statistics_array=new int[6];
		
		if (i==0){

			experiment.run(true);

		}
		else{
			experiment.run(false);
		}
	
			// statistics array
		MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree = learner.getDiscriminationTree();
		// MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> 
		tree_round2 = experiment.getDiscrtree();
		
		statistics_array[0] += experiment.getRounds().getCount();
		statistics_array[1] += ExtractValue(mq_rst.getStatisticalData().getSummary());
		statistics_array[2] += ExtractValue(mq_sym.getStatisticalData().getSummary());
		statistics_array[3] += ExtractValue(eq_rst.getStatisticalData().getSummary());
		statistics_array[4] += ExtractValue(eq_sym.getStatisticalData().getSummary());
		System.out.println(mq_rst.getStatisticalData());
		
		for (int j=0;j<5;j++){
			System.out.println("vaa"+statistics_array[j]);
		}
		
		// Output results
		System.out.println("Learning completed.");
		System.out.println("Final hypothesis states: " + experiment.getFinalHypothesis().getStates().size());
		System.out.println("Membership queries: " + mqRst.getStatisticalData());
		System.out.println("Equivalence queries: " + experiment.getRounds().getCount());
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
