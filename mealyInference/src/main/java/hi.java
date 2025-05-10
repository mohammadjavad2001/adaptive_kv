import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.RandomWMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.WMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.WpMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.Infer_LearnLib;
import br.usp.icmc.labes.mealyInference.utils.LearnLibProperties;
import br.usp.icmc.labes.mealyInference.utils.Utils;
// import de.learnlib.algorithms.kv.KearnsVaziraniMealy;
// import de.learnlib.algorithms.kv.KearnsVaziraniMealyBuilder;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealyBuilder;

import de.learnlib.api.SUL;
// import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealyBuilder;
//import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.statistic.StatisticSUL;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.driver.util.MealySimulatorSUL;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.filter.statistic.sul.SymbolCounterSUL;
import de.learnlib.oracle.equivalence.RandomWMethodEQOracle;
import de.learnlib.oracle.equivalence.RandomWordsEQOracle;
import de.learnlib.oracle.equivalence.WMethodEQOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.util.Experiment;
// import de.learnlib.algorithms.kv.Experiment;
// import de.learnlib.algorithms.kv.KearnsVaziraniMealyBuilder;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.MealyMachine;
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

import de.learnlib.datastructure.discriminationtree.MultiDTree;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import de.learnlib.algorithms.kv.StateInfo;
import net.automatalib.graphs.concepts.GraphViewable;
import de.learnlib.filter.statistic.oracle.JointCounterOracle;

import de.learnlib.filter.statistic.oracle.CounterSymbolQueryOracle;


public class hi {


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

		System.out.println("VVVVVVVVVVVVVVVVVVV");

		Pattern kissLine = Pattern
				.compile("\\s*([a-zA-Z0-9]+)\\s+->\\s+([a-zA-Z0-9]+)\\s*\\[label=[\"<](.+)[\">]\\];?");
		BufferedReader br = new BufferedReader(new FileReader(f));

		List<String[]> trs = new ArrayList<String[]>();
		System.out.println("CCCCCCCCCC");
		HashSet<String> abcSet = new HashSet<>();

		// int count = 0;
		System.out.println("WWWWWWWW");	
		while (br.ready()) {
			String line = br.readLine();
			Matcher m = kissLine.matcher(line);
			if (m.matches()) {
				// System.out.println(m.group(0));
				// System.out.println(m.group(1));
				// System.out.println(m.group(2));
				// System.out.println(m.group(3));
				// System.out.println(m.group(4));

				String[] tr = new String[4];
				tr[0] = m.group(1);
				tr[1] = m.group(3);
//    				if(!abc.contains(tr[1])){
//    					abc.add(tr[1]);
//    				}
//    				tr[2] = m.group(4);
				tr[3] = m.group(2);
				if (tr[1].contains("<br />")) {
					String trr[] = tr[1].split("<br />");
					tr[1] = trr[0];
					tr[2] = trr[1];
					trr = tr[1].split(" \\| ");
					for (String string : trr) {
						String trrr[] = new String[4];
						trrr[0] = tr[0];
						trrr[1] = string;
						trrr[2] = tr[2];
						trrr[3] = tr[3];
						trs.add(trrr);
						abcSet.add(trrr[1]);
					}
				} else {
					String trr[] = tr[1].split("\\s*/\\s*");
					tr[1] = trr[0];
					tr[2] = trr[1];
					trs.add(tr);
					abcSet.add(tr[1]);
				}

			}
			// count++;
		}

		br.close();

		List abc = new ArrayList<>(abcSet);
		Collections.sort(abc);
		Alphabet<String> alphabet = Alphabets.fromCollection(abc);
		CompactMealy<String, Word<String>> mealym = new CompactMealy<String, Word<String>>(alphabet);

		Map<String, Integer> states = new HashMap<String, Integer>();
		Integer si = null, sf = null;

		Map<String, Word<String>> words = new HashMap<String, Word<String>>();

		WordBuilder<String> aux = new WordBuilder<>();

		aux.clear();
		aux.append(OMEGA_SYMBOL);
		words.put(OMEGA_SYMBOL.toString(), aux.toWord());

		for (String[] tr : trs) {
			if (!states.containsKey(tr[0]))
				states.put(tr[0], mealym.addState());
			if (!states.containsKey(tr[3]))
				states.put(tr[3], mealym.addState());

			si = states.get(tr[0]);
			sf = states.get(tr[3]);
			
		
			if (!words.containsKey(tr[1])) {
				aux.clear();
				aux.add(tr[1]);
				words.put(tr[1], aux.toWord());
			}
			if (!words.containsKey(tr[2])) {
				aux.clear();
				aux.add(tr[2]);
				words.put(tr[2], aux.toWord());
			}
			mealym.addTransition(si, words.get(tr[1]).toString(), sf, words.get(tr[2]));
		}

		for (Integer st : mealym.getStates()) {
			for (String in : alphabet) {
				// System.out.println(mealym.getTransition(st, in));
				if (mealym.getTransition(st, in) == null) {
					mealym.addTransition(st, in, st, OMEGA_SYMBOL);
				}
			}
		}

		mealym.setInitialState(states.get("s0"));

		return mealym;
	}

	// dddddddddddddddddddddddddddddddd

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
				System.out.println("jjjjjjjjjj");
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

	public static <I, O> void main(String[] args) throws Exception {

//         if (args.length < 1) {
//             System.err.println("Usage: LearnMealyWithKV <dot-file>");
//             System.exit(1);
//         }
		
		System.out.println("eeeeeeeeee");
		// File productFile_2 = new File(
		// 		"E:\\learning\\Projectpayan\\software\\SPL_Learning\\experiments\\BCS_SPL\\products_3wise",
		// 		"00001_fsm.dot");
		File productFile_2 = new File(
				"E:\\learning\\Projectpayan\\software\\SPL_Learning\\experiments\\Minepump_SPL\\products_3wise",
				"00001_fsm.dot");
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

		JointCounterOracle<String, Word<Word<String>>> mqCounter = new JointCounterOracle<>(mqOracle);
		// CounterSymbolQueryOracle<String, Word<Word<String>>> mqCounter = 
		// new CounterSymbolQueryOracle<>(mqOracle);

		//***************************************
//		CounterSymbolQueryOracle<String, Word<Word<String>>> symbolCounter = 
//			    new CounterSymbolQueryOracle<>(membershipOracle);

		//***************************************
		
				// de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealyBuilder<String, Word<String>> builder = new de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealyBuilder<String, Word<String>>();

		
		KearnsVaziraniMealyBuilder<String, Word<String>> builder = new KearnsVaziraniMealyBuilder<String, Word<String>>();
		builder.setAlphabet(mealyMachine.getInputAlphabet());
		builder.setOracle(mqOracle);

		// 333333333333333333333333333333333333333333333333333333333333
		// builder.setDiscriminationTree(null);
		// 333333333333333333333333333333333333333333333333333333333333
		// Set up the Kearns-Vazirani learner
		// new MultiDTree<>(oracle);
		builder.setAlphabet(mealyMachine.getInputAlphabet()); 
		KearnsVaziraniMealy<String, Word<String>> learner = builder.create();
		// MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> discriminationTree = new MultiDTree<I, Word<O>, StateInfo<I, Word<O>>>;
		Experiment.MealyExperiment<String, Word<String>> experiment = new Experiment.MealyExperiment<String, Word<String>>(
				learner, eqOracle, mealyMachine.getInputAlphabet());
		// Run the experiment
		int[] statistics_array=new int[6];
		experiment.run();
		// MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> a2 = experiment.getDiscrtree();
				// statistics array
		
		statistics_array[0] += experiment.getRounds().getCount();
		statistics_array[1] += ExtractValue(mq_rst.getStatisticalData().getSummary());
		statistics_array[2] += ExtractValue(mq_sym.getStatisticalData().getSummary());
		statistics_array[3] += ExtractValue(eq_rst.getStatisticalData().getSummary());
		statistics_array[4] += ExtractValue(eq_sym.getStatisticalData().getSummary());
		System.out.println(mq_rst.getStatisticalData());
		System.out.println(mq_sym.getStatisticalData());
		System.out.println(eq_rst.getStatisticalData());
		System.out.println(eq_sym.getStatisticalData());

		for (int i=0;i<5;i++){
			System.out.println("vaa"+statistics_array[i]);
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
		MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree = learner.getDiscriminationTree();

		// builder.setDiscriminationTree(tree);

		//		MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree = learner.getDiscriminationTree().asNormalGraph();
		// Visualization.visualize(tree, new VisualizationHelper<AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>, Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>>> () {
		//     @Override
		//     public boolean getNodeProperties(AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> node, Map<String, String> properties) {
		//         if (node.isLeaf()) {
		//             StateInfo<String, Word<Word<String>>> state = node.getData();
		//             // Use toString() instead of getHypothesisState()
		//             properties.put(NodeAttrs.LABEL, "State: " + state.toString());
		//             properties.put(NodeAttrs.SHAPE, NodeShapes.BOX);
		//         } else {
		//             properties.put(NodeAttrs.LABEL, "Test: " + node.getDiscriminator());
		//             properties.put(NodeAttrs.SHAPE, NodeShapes.OVAL);
		//         }
		//         return true;
		//     }
		    
		//     @Override
		//     public boolean getEdgeProperties(AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> src, 
		//                                     Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>>> edge, 
		//                                     AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tgt, 
		//                                     Map<String, String> properties) {
		//         properties.put(EdgeAttrs.LABEL, edge.getKey().toString());
		//         return true;
		//     }
		// });
//		Visualization.visualize(((GraphViewable) tree).graphView(), true);
		// Visualization.visualize(, true);

		// Get the discrimination tree

		// The AbstractDiscriminationTree class already implements Graph interface
		// So we can directly visualize it
		System.out.println("********************Visualizing discrimination tree********************");
		Visualization.visualize(tree, true);
		// Print the tree structure
		System.out.println("********************Discrimination Tree Structure********************");
		traverseAndPrintTree(tree.getRoot(), "", true);
		
		

		VisualizationHelper a=tree.getVisualizationHelper();
		
		System.out.print("ddddddddddddddd"+a);
		tree.getVisualizationHelper();

		
		// System.out.print(tree.);
		// dotgen.visualizeTree();
		
		System.out.println();
		Alphabet<String> alphabet = mealyMachine.getInputAlphabet();
		MealyMachine<?, String, ?, Word<String>> hypothesis = experiment.getFinalHypothesis();
		CompactMealy<String, Word<String>> learnedModel = new CompactMealy<>(alphabet);
		MealyMachine<?, String, ?, Word<String>> finalHyp = experiment.getFinalHypothesis();
		
		// Output statistics
		System.out.println("Learning completed.");
		System.out.println("Statistics:");
		System.out.println(" Membership queries: " + mqCounter.getQueryCount());
		System.out.println(" MQ resets: " + experiment.getRounds().getCount());
		System.out.println(" Symbol count (MQ): " + mqCounter.getSymbolCount());
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
		return options;
	}
}
