
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.RandomWMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.WMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.WpMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.LearnLibProperties;
import br.usp.icmc.labes.mealyInference.utils.Utils;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealyBuilder;
import de.learnlib.api.SUL;

import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.api.statistic.StatisticSUL;
import de.learnlib.driver.util.MealySimulatorSUL;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.filter.statistic.sul.SymbolCounterSUL;
import de.learnlib.oracle.equivalence.RandomWMethodEQOracle;
import de.learnlib.oracle.equivalence.RandomWordsEQOracle;
import de.learnlib.oracle.equivalence.WMethodEQOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;
import de.learnlib.oracle.membership.SULOracle;
// import de.learnlib.util.Experiment;
import de.learnlib.algorithms.kv.Experiment1;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Word;
import net.automatalib.visualization.Visualization;
import org.apache.commons.cli.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.io.File;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.function.Function;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
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

public class App {

	// Static arrays to store statistics for comparison
	private static long[][] productStats = new long[10][6]; // [product][metric]
	private static int[] productStates = new int[10];
	private static int[] productAlphabetSizes = new int[10];

	private static int ExtractValue(String string_1) {
		// Extract numeric value from statistical summary string
		int value_1 = 0;
		int j = string_1.lastIndexOf(" ");
		String string_2 = "";
		if (j >= 0) {
			string_2 = string_1.substring(j + 1);
		}
		value_1 = Integer.parseInt(string_2);
		return value_1;
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
        return Pair.of(tokens[0], token2);};
        

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
        
      //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@2

        
        
        
        
        
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

    public static void main(String[] args)  {
//         if (args.length < 1) {
//             System.err.println("Usage: LearnMealyWithKV <dot-file>");
//             System.exit(1);
//         }


        File productFile_2 = new File(".\\alternative_experiments\\Minepump_SPL\\products_3wise", "00005_fsm.dot");
        System.out.print("Fvvvvvv");
        CompactMealy<String, Word<String>> mealyMachine = null;
		try {
			mealyMachine = LoadMealy(productFile_2);
		} catch (Exception e) {
			e.printStackTrace();
		}

        CommandLineParser parser = new BasicParser();
        // create the Options
        Options options = createOptions();
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        //    logger.logEvent("\nSUL name: "+sul_file.getName());
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

        KearnsVaziraniMealyBuilder<String, Word<String>> builder=new KearnsVaziraniMealyBuilder<String, Word<String>>();

        builder.setAlphabet(mealyMachine.getInputAlphabet());
        builder.setOracle(mqOracle);
        // Set up the Kearns-Vazirani learner
        builder.setAlphabet(mealyMachine.getInputAlphabet());

        KearnsVaziraniMealy<String, Word<String>> learner = builder.create();

        Experiment1.MealyExperiment<String, Word<String>> experiment = new Experiment1.MealyExperiment<String, Word<String>>(learner, eqOracle,
                mealyMachine.getInputAlphabet());
        // Run the experiment
        experiment.run();

        // ========== METRICS FOR NORMAL LEARNING EVALUATION ==========
        int productIndex = 0; // Since this is a single product, use index 0
        
        System.out.println("\n========== PRODUCT LEARNING COMPLETED (NORMAL APPROACH) ==========");
        System.out.println("Final hypothesis states: " + experiment.getFinalHypothesis().getStates().size());
        System.out.println("Rounds (EQ queries): " + experiment.getRounds().getCount());
        System.out.println("Membership queries - Resets: " + ExtractValue(mq_rst.getStatisticalData().getSummary()));
        System.out.println("Membership queries - Symbols: " + ExtractValue(mq_sym.getStatisticalData().getSummary()));
        System.out.println("Equivalence queries - Resets: " + ExtractValue(eq_rst.getStatisticalData().getSummary()));
        System.out.println("Equivalence queries - Symbols: " + ExtractValue(eq_sym.getStatisticalData().getSummary()));
        System.out.println("*** NORMAL LEARNING (NO REUSE) ***");
        System.out.println("  Learning from scratch - no tree reuse");
        System.out.println("  Alphabet size: " + mealyMachine.getInputAlphabet().size() + " symbols");
        System.out.println("====================================================\n");
        
        // Store statistics for comparison
        productStats[productIndex][0] = experiment.getRounds().getCount(); // Rounds
        productStats[productIndex][1] = ExtractValue(mq_rst.getStatisticalData().getSummary()); // MQ Resets
        productStats[productIndex][2] = ExtractValue(mq_sym.getStatisticalData().getSummary()); // MQ Symbols
        productStats[productIndex][3] = ExtractValue(eq_rst.getStatisticalData().getSummary()); // EQ Resets
        productStats[productIndex][4] = ExtractValue(eq_sym.getStatisticalData().getSummary()); // EQ Symbols
        productStates[productIndex] = experiment.getFinalHypothesis().getStates().size();
        productAlphabetSizes[productIndex] = mealyMachine.getInputAlphabet().size();
        
        // Output detailed statistics
        System.out.println("Learning completed.");
        System.out.println("Statistics:");
        System.out.println("  Membership queries - Resets: " + productStats[productIndex][1]);
        System.out.println("  Membership queries - Symbols: " + productStats[productIndex][2]);
        System.out.println("  Equivalence queries - Resets: " + productStats[productIndex][3]);
        System.out.println("  Equivalence queries - Symbols: " + productStats[productIndex][4]);
        System.out.println("  Rounds: " + productStats[productIndex][0]);
        System.out.println("  Final hypothesis states: " + productStates[productIndex]);
        System.out.println("  Alphabet size: " + productAlphabetSizes[productIndex]);
        
        // Print comprehensive summary
        System.out.println("\n\n");
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║         NORMAL LEARNING RESULTS SUMMARY                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Product (Normal Learning):");
        System.out.println("  Rounds: " + productStats[productIndex][0]);
        System.out.println("  MQ Resets: " + productStats[productIndex][1] + ", Symbols: " + productStats[productIndex][2]);
        System.out.println("  EQ Resets: " + productStats[productIndex][3] + ", Symbols: " + productStats[productIndex][4]);
        System.out.println("  States: " + productStates[productIndex]);
        System.out.println("  Alphabet: " + productAlphabetSizes[productIndex] + " symbols");
        System.out.println("  ✗ No tree reuse (learning from scratch)");
        System.out.println();
        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println();

        // Get the final hypothesis and alphabet for visualization
        MealyMachine<?, String, ?, Word<String>> finalHypothesis = experiment.getFinalHypothesis();
        Alphabet<String> alphabet = mealyMachine.getInputAlphabet();
        
        // Visualize the learned model
        System.out.println("\nVisualizing learned model (Final Hypothesis)...");
        Visualization.visualize(finalHypothesis, alphabet);
        
        // Visualize the original model
        System.out.println("Visualizing original model...");
        Visualization.visualize(mealyMachine, alphabet);
        
        // Visualize the discrimination tree
        System.out.println("Visualizing discrimination tree...");
        Visualization.visualize(learner.getDiscriminationTree(), true);
        
        // ========== FINAL COMPARISON-READY OUTPUT ==========
        System.out.println("\n\n");
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║    DETAILED METRICS (For comparison with adaptive learning)    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("NORMAL LEARNING METRICS:");
        System.out.println("  ┌─ Rounds (EQ queries): " + productStats[productIndex][0]);
        System.out.println("  ├─ MQ Resets: " + productStats[productIndex][1]);
        System.out.println("  ├─ MQ Symbols: " + productStats[productIndex][2]);
        System.out.println("  ├─ EQ Resets: " + productStats[productIndex][3]);
        System.out.println("  ├─ EQ Symbols: " + productStats[productIndex][4]);
        System.out.println("  ├─ Final States: " + productStates[productIndex]);
        System.out.println("  └─ Alphabet Size: " + productAlphabetSizes[productIndex]);
        System.out.println();
        System.out.println("COMPARISON NOTE:");
        System.out.println("  Compare these metrics with adaptive learning (hi_single.java)");
        System.out.println("  Expected: Adaptive learning should show reduced queries for");
        System.out.println("            subsequent products due to tree reuse");
        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println();

        SimpleProfiler.logResults();
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
