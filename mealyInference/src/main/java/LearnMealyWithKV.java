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
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Word;
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

public class LearnMealyWithKV {
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
    private static CompactMealy<String, Word<String>> LoadMealy(File fsm_file){
        // TODO Auto-generated method stub
        InputModelDeserializer<String, CompactMealy<String, Word<String>>> parser_1 = DOTParsers
                .mealy(MEALY_EDGE_WORD_STR_PARSER);
        CompactMealy<String, Word<String>> mealy = null;
        String file_name = fsm_file.getName();
        if (file_name.endsWith("txt")) {
            try {
                mealy = Utils.getInstance().loadMealyMachine(fsm_file);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return mealy;
        } else if (file_name.endsWith("dot")) {
            try {
                mealy = parser_1.readModel(fsm_file).model;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return mealy;
        }
        return null;

    }
//$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    private static EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> buildEqOracle(
            Random rnd_seed, CommandLine line, CompactMealy<String, Word<String>> mealyss,
            SUL<String, Word<String>> eq_sul) {
        MembershipOracle<String, Word<Word<String>>> oracleForEQoracle = new SULOracle<>(eq_sul);

        EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle;
        if (!line.hasOption(EQ)) {
//			logger.logEvent("EquivalenceOracle: WpMethodEQOracle(" + 2 + ")");
            return new WpMethodEQOracle<>(oracleForEQoracle, 2);
        }

        double restartProbability;
        int maxSteps, maxTests, maxLength, minLength, maxDepth, minimalSize, rndLength, bound;
        long rnd_long;
        boolean resetStepCount;

        LearnLibProperties learn_props = LearnLibProperties.getInstance();

//        switch (line.getOptionValue(EQ)) {
//            case "rndWalk":
                // create RandomWalkEQOracle
                restartProbability = learn_props.getRndWalk_restartProbability();
                maxSteps = learn_props.getRndWalk_maxSteps();
                resetStepCount = learn_props.getRndWalk_resetStepsCount();

                eqOracle = new RandomWalkEQOracle<String, Word<String>>(eq_sul, // sul
                        restartProbability, // reset SUL w/ this probability before a step
                        maxSteps, // max steps (overall)
                        resetStepCount, // reset step count after counterexample
                        rnd_seed // make results reproducible
                );
////			logger.logEvent("EquivalenceOracle: RandomWalkEQOracle(" + restartProbability + "," + maxSteps + ","
////					+ resetStepCount + ")");
//                break;
//            case "rndWords":
//                // create RandomWordsEQOracle
//                maxTests = learn_props.getRndWords_maxTests();
//                maxLength = learn_props.getRndWords_maxLength();
//                minLength = learn_props.getRndWords_minLength();
//                rnd_long = rnd_seed.nextLong();
//                rnd_seed.setSeed(rnd_long);
//
//                eqOracle = new RandomWordsEQOracle<>(oracleForEQoracle, minLength, maxLength, maxTests, rnd_seed);
////			logger.logEvent("EquivalenceOracle: RandomWordsEQOracle(" + minLength + ", " + maxLength + ", " + maxTests
////					+ ", " + rnd_long + ")");
//                break;
//            case "wp":
//                maxDepth = learn_props.getW_maxDepth();
//                eqOracle = new WpMethodEQOracle<>(oracleForEQoracle, maxDepth);
////			logger.logEvent("EquivalenceOracle: WpMethodEQOracle(" + maxDepth + ")");
//                break;
//            case "wphyp":
//                maxDepth = learn_props.getW_maxDepth();
//                eqOracle = new WpMethodHypEQOracle((MembershipOracle.MealyMembershipOracle) oracleForEQoracle, maxDepth, mealyss);
////			logger.logEvent("EquivalenceOracle: WpMethodHypEQOracle(" + maxDepth + ")");
//                break;
//            case "w":
//                maxDepth = learn_props.getW_maxDepth();
//                eqOracle = new WMethodEQOracle<>(oracleForEQoracle, maxDepth);
////			logger.logEvent("EquivalenceOracle: WMethodQsizeEQOracle(" + maxDepth + ")");
//                break;
//            case "whyp":
//                maxDepth = learn_props.getW_maxDepth();
//                eqOracle = new WMethodHypEQOracle((MembershipOracle.MealyMembershipOracle) oracleForEQoracle, maxDepth, mealyss);
////			logger.logEvent("EquivalenceOracle: WMethodHypEQOracle(" + maxDepth + ")");
//                break;
//            case "wrnd":
//                minimalSize = learn_props.getWhyp_minLen();
//                rndLength = learn_props.getWhyp_rndLen();
//                bound = learn_props.getWhyp_bound();
//                rnd_long = rnd_seed.nextLong();
//                rnd_seed.setSeed(rnd_long);
//
//                eqOracle = new RandomWMethodEQOracle<>(oracleForEQoracle, minimalSize, rndLength, bound, rnd_seed, 1);
////			logger.logEvent("EquivalenceOracle: RandomWMethodEQOracle(" + minimalSize + "," + rndLength + "," + bound
////					+ "," + rnd_long + ")");
//                break;
//            case "wrndhyp":
//                minimalSize = learn_props.getWhyp_minLen();
//                rndLength = learn_props.getWhyp_rndLen();
//                bound = learn_props.getWhyp_bound();
//                rnd_long = rnd_seed.nextLong();
//                rnd_seed.setSeed(rnd_long);
//
//                eqOracle = new RandomWMethodHypEQOracle((MembershipOracle.MealyMembershipOracle) oracleForEQoracle, minimalSize, rndLength,
//                        bound, rnd_seed, 1, mealyss);
////			logger.logEvent("EquivalenceOracle: RandomWMethodHypEQOracle(" + minimalSize + "," + rndLength + "," + bound
////					+ "," + rnd_long + "," + 1 + ")");
//                break;
//            default:
//                maxDepth = 2;
//                eqOracle = new WMethodEQOracle<>(oracleForEQoracle, maxDepth);
////			logger.logEvent("EquivalenceOracle: WMethodEQOracle(" + maxDepth + ")");
//                break;
//        }
        return eqOracle;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: LearnMealyWithKV <dot-file>");
            System.exit(1);
        }

        // Load the Mealy machine from the .dot file
        File dotFile = new File(args[0]);
        Function<Map<String, String>, Word<String>> edgeParser = attr -> {
            String label = attr.get("label");
            if (label == null) return Word.epsilon();
            String[] tokens = label.split("/");
            return Word.fromSymbols(tokens[1]); // Output is after the '/'
        };


//        File productFile_1 = new File(products_dir, productFileName_1);

        File productFile_2 = new File("/home/user/products/", "00001_fsm.dot");

        CompactMealy<String, Word<String>> mealyMachine = LoadMealy(productFile_2);

//        CompactMealy<String, String> mealyMachine = DOTParsers.mealy(edgeParser).readModel(dotFile).model;
        // create the command line parser
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

        CommandLine line = parser.parse(options, args);

        StatisticSUL<String, Word<String>> eq_sym = new SymbolCounterSUL<>("EQ", sulSim);
        StatisticSUL<String, Word<String>> eq_rst = new ResetCounterSUL<>("EQ", eq_sym);

        SUL<String, Word<String>> eq_sul = eq_rst;


//        EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle = buildEqOracle(rnd_seed, line, mealyss, eq_sul);
        // Create a SUL (System Under Learning) from the Mealy machine
        MealySimulatorSUL<String, Word<String>> sul = new MealySimulatorSUL<>(mealyMachine);
        EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle = null;
        eqOracle = buildEqOracle(rnd_seed, line, mealyMachine, eq_sul);
        // Set up membership oracle with counters
        SymbolCounterSUL<String, Word<String>> mqSym = new SymbolCounterSUL<>("MQ", sul);
        ResetCounterSUL<String, Word<String>> mqRst = new ResetCounterSUL<>("MQ", mqSym);
        MembershipOracle<String, Word<Word<String>>> mqOracle = new SULOracle<String, Word<String>>(mq_sul);

        // Set up equivalence oracle (using WpMethod)
//        EquivalenceOracle<CompactMealy<String, String>, String, Word<String>> eqOracle =
//                new WpMethodEQOracle<CompactMealy<String, String>, String, Word<String>>(mqOracle, 2); // Depth = 2
        KearnsVaziraniMealyBuilder<String, Word<String>> builder=new KearnsVaziraniMealyBuilder<String, Word<String>>();

        builder.setAlphabet(mealyMachine.getInputAlphabet());
        builder.setOracle(mqOracle);
        // Set up the Kearns-Vazirani learner
        builder.setAlphabet(mealyMachine.getInputAlphabet());

        KearnsVaziraniMealy<String, Word<String>> learner = builder.create();

        // Set up the learning experiment
//        Experiment<CompactMealy<String, String>> experiment = new Experiment<>(learner, eqOracle, mealyMachine.getInputAlphabet());
        Experiment.MealyExperiment<String, Word<String>> experiment = new Experiment.MealyExperiment<String, Word<String>>(learner, eqOracle,
                mealyMachine.getInputAlphabet());
        // Run the experiment
        experiment.run();

        // Output results
        System.out.println("Learning completed.");
        System.out.println("Final hypothesis states: " + experiment.getFinalHypothesis().getStates().size());
        System.out.println("Membership queries: " + mqRst.getStatisticalData());
        System.out.println("Equivalence queries: " + experiment.getRounds().getCount());

        // Output observation table (optional)
//        System.out.println("Observation Table:");
//        new ObservationTableASCIIWriter<>().write(learner.getObservationTable(), System.out);

        // Profiling results
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