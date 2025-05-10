package br.usp.icmc.labes.mealyInference.utils;
/**
 *
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.checkerframework.checker.nullness.qual.Nullable;

import br.usp.icmc.labes.mealyInference.utils.ExperimentAndLearner;
import br.usp.icmc.labes.mealyInference.utils.LearnLibProperties;
import br.usp.icmc.labes.mealyInference.utils.MyObservationTable;
import br.usp.icmc.labes.mealyInference.utils.OTUtils;
import br.usp.icmc.labes.mealyInference.utils.Utils;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.RandomWMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.WMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.WpMethodHypEQOracle;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealy;
import de.learnlib.algorithms.lstar.mealy.ExtensibleLStarMealyBuilder;

import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealy;
import de.learnlib.algorithms.ttt.mealy.TTTLearnerMealyBuilder;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.algorithms.kv.AKVMBuilder;
import de.learnlib.algorithms.kv.AdaptiveKearnsVaziraniMealy;
import de.learnlib.algorithms.kv.AdaptiveKearnsVaziraniMealyBuilder;
import de.learnlib.algorithms.kv.StateInfo;
import de.learnlib.algorithms.kv.mealy.AdaptiveKVM;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealyBuilder;
import de.learnlib.algorithms.dlstar.mealy.ExtensibleDLStarMealy;

import de.learnlib.api.SUL;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle.MealyMembershipOracle;
import de.learnlib.api.statistic.StatisticSUL;

import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.driver.util.MealySimulatorSUL;
//import de.learnlib.examples.mealy.ExampleCoffeeMachine.Input;
import de.learnlib.filter.cache.sul.SULCache;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.filter.statistic.sul.SymbolCounterSUL;
import de.learnlib.oracle.equivalence.RandomWMethodEQOracle;
import de.learnlib.oracle.equivalence.RandomWordsEQOracle;
import de.learnlib.oracle.equivalence.WMethodEQOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.util.ExperimentDebug.MealyExperiment;
import de.learnlib.util.MQUtil;
import de.learnlib.util.statistics.SimpleProfiler;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import de.learnlib.datastructure.discriminationtree.model.*;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.visualization.VisualizationHelper.EdgeAttrs;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.visualization.Visualization;
//import de.learnlib.algorithms.adt.util.MDTUtil;



/**
 * @author damasceno
 *
 */
public class Infer_LearnLib {

	public static final String CONFIG = "config";
	public static final String SOT = "sot";
	public static final String SUL = "sul";
	public static final String DOT = "dot";
	public static final String HELP = "help";
	public static final String HELP_SHORT = "h";
	public static final String OT = "ot";
	public static final String CEXH = "cexh";
	public static final String CLOS = "clos";
	public static final String EQ = "eq";
	public static final String CACHE = "cache";
	public static final String SEED = "seed";
	public static final String OUT = "out";
	public static final String LEARN = "learn";
	public static final String INFO = "info";

	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	public static final String[] eqMethodsAvailable = {"rndWalk" , "rndWords", "wp", "wphyp", "w", "whyp","wrnd","wrndhyp"};
	public static final String[] closingStrategiesAvailable = {"CloseFirst" , "CloseShortest"};
	private static final String RIVEST_SCHAPIRE_ALLSUFFIXES = "RivestSchapireAllSuffixes";
	public static final String[] cexHandlersAvailable = {"ClassicLStar" , "MalerPnueli", "RivestSchapire", RIVEST_SCHAPIRE_ALLSUFFIXES, "Shahbaz", "Suffix1by1"};
	public static final String[] learningMethodsAvailable = {"lstar" , "l1","adaptive", "dlstar_v4", "dlstar_v3", "dlstar_v2", "dlstar_v1","dlstar_v0","ttt","kv","adaptive_kv"};


	public static final Function<Map<String, String>, Pair<@Nullable String, @Nullable Word<String>>>
	MEALY_EDGE_WORD_STR_PARSER = attr -> {
		final String label = attr.get(EdgeAttrs.LABEL);
		if (label == null) {
			return Pair.of(null, null);
		}

		final String[] tokens = label.split("/");

		if (tokens.length != 2) {
			return Pair.of(null, null);
		}

		Word<String> token2 = Word.epsilon();
		token2=token2.append(tokens[1]);
		return Pair.of(tokens[0], token2);
	};


	public static <I, O> void main(String[] args) throws Exception {



		// create the command line parser
		CommandLineParser parser = new BasicParser();
		// create the Options
		Options options = createOptions();
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();


		long tstamp = System.currentTimeMillis();
		// random seed
		Random rnd_seed = new Random(tstamp);

		// timestamp
		Timestamp timestamp = new Timestamp(tstamp);

		try {

			// parse the command line arguments
			CommandLine line = parser.parse( options, args);

			if(line.hasOption(HELP)){
				formatter.printHelp( "Infer_LearnLib", options );
				System.exit(0);
			}

			if(!line.hasOption(SUL)){
				throw new IllegalArgumentException("must provide a SUL");
			}


			// set SUL path
			File sul = new File(line.getOptionValue(SUL));

			// if passed as argument, set OT path
			File obsTable = null;
			if( line.hasOption(OT)){
				obsTable = new File(line.getOptionValue(OT));
			}

			// set output dir
			File out_dir = sul.getParentFile();
			if( line.hasOption(OUT)){
				out_dir = new File(line.getOptionValue(OUT));
			}
			if(!out_dir.exists()) {
				out_dir.mkdirs();
			}

			LearnLibProperties learn_props = LearnLibProperties.getInstance();
			if(line.hasOption(CONFIG)) {
				String pathname = line.getOptionValue(CONFIG);
				learn_props.loadProperties(new File(pathname));
			}

			// create log
			System.setProperty("logdir", out_dir.getAbsolutePath());
			LearnLogger logger = LearnLogger.getLogger(Infer_LearnLib.class);

			// set closing strategy
			ClosingStrategy<Object, Object> strategy 			= getClosingStrategy(line.getOptionValue(CLOS));

			// set CE processing approach
			ObservationTableCEXHandler<Object, Object> handler 	= getCEXHandler(line.getOptionValue(CEXH));

			// load mealy machine
			InputModelDeserializer<String, CompactMealy<String, Word<String>>> mealy_parser = DOTParsers.mealy(MEALY_EDGE_WORD_STR_PARSER);

			CompactMealy<String, Word<String>> mealyss = null;

			if(line.hasOption(DOT)) {
				mealyss = mealy_parser.readModel(sul).model;
			}else {
				mealyss = Utils.getInstance().loadMealyMachine(sul);
			}
			logger.logEvent("SUL name: "+sul.getName());
			logger.logEvent("SUL dir: "+sul.getAbsolutePath());
			logger.logEvent("Output dir: "+out_dir);

			if( line.hasOption( SEED ) )  tstamp = Long.valueOf(line.getOptionValue(SEED));
			rnd_seed.setSeed(tstamp);
			logger.logEvent("Seed: "+Long.toString(tstamp));


			Utils.getInstance();
			// SUL simulator
			SUL<String,Word<String>> sulSim = new MealySimulatorSUL<>(mealyss, Utils.OMEGA_SYMBOL);

			//////////////////////////////////
			// Setup objects related to MQs	//
			//////////////////////////////////

			// Counters for MQs
			StatisticSUL<String, Word<String>>  mq_sym = new SymbolCounterSUL<>("MQ", sulSim);
			StatisticSUL<String, Word<String>>  mq_rst = new ResetCounterSUL <>("MQ", mq_sym);

			// SUL for counting queries wraps sul
			SUL<String, Word<String>> mq_sul = mq_rst;

			// use caching to avoid duplicate queries
			if(line.hasOption(CACHE))  {
				// SULs for associating the IncrementalMealyBuilder 'mq_cbuilder' to MQs
				mq_sul = SULCache.createDAGCache(mealyss.getInputAlphabet(), mq_rst);
			}
			MembershipOracle<String, Word<Word<String>>> mqOracle = new SULOracle<String, Word<String>>(mq_sul);

			logger.logEvent("Cache: "+(line.hasOption(CACHE)?"Y":"N"));

			//////////////////////////////////
			// Setup objects related to EQs	//
			//////////////////////////////////


			logger.logEvent("ClosingStrategy: "+strategy.toString());
			logger.logEvent("ObservationTableCEXHandler: "+handler.toString());

			// Counters for EQs
			StatisticSUL<String, Word<String>>  eq_sym = new SymbolCounterSUL<>("EQ", sulSim);
			StatisticSUL<String, Word<String>>  eq_rst = new ResetCounterSUL <>("EQ", eq_sym);

			// SUL for counting queries wraps sul
			SUL<String, Word<String>> eq_sul = eq_rst;

			// use caching to avoid duplicate queries
			if(line.hasOption(CACHE))  {
				// SULs for associating the IncrementalMealyBuilder 'cbuilder' to EQs
				eq_sul = SULCache.createDAGCache(mealyss.getInputAlphabet(), eq_rst);
			}



			EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle = null;
			eqOracle = buildEqOracle(rnd_seed, line, logger, mealyss, eq_sul);

			/////////////////////////////
			// Setup experiment object //
			/////////////////////////////

			String learnAlgorithm = "lstar";
			ExperimentAndLearner experiment_pair = null;

			if(line.hasOption(LEARN)) learnAlgorithm = line.getOptionValue(LEARN).toLowerCase();
			System.out.print(learnAlgorithm);
			switch (learnAlgorithm) {
			case "l1":
				logger.logConfig("Method: L1");
				experiment_pair = learningL1(mealyss, mqOracle, eqOracle, handler, strategy);
				break;
			case "dlstar_v1":
				if(handler == ObservationTableCEXHandlers.CLASSIC_LSTAR)  throw new Exception("DL*M requires "+ObservationTableCEXHandlers.RIVEST_SCHAPIRE+" CexH");
				logger.logConfig("Method: DL*M_v1");
				logger.logEvent("Revalidate OT");
				experiment_pair = learningDLStarM_v1(mealyss, mqOracle, eqOracle, handler, strategy,obsTable);
				// learning statistics
				logger.logEvent("Reused queries [resets]: " +((ResetCounterSUL)mq_rst).getStatisticalData().getCount());
				logger.logEvent("Reused queries [symbols]: "+((SymbolCounterSUL)mq_sym).getStatisticalData().getCount());
				break;
			case "dlstar_v0":
				if(handler == ObservationTableCEXHandlers.CLASSIC_LSTAR)  throw new Exception("DL*M requires "+ObservationTableCEXHandlers.RIVEST_SCHAPIRE+" CexH");
				logger.logConfig("Method: DL*M_v0");
				logger.logEvent("Revalidate OT");
				experiment_pair = learningDLStarM_v0(mealyss, mqOracle, eqOracle, handler, strategy,obsTable);
				// learning statistics
				logger.logEvent("Reused queries [resets]: " +((ResetCounterSUL)mq_rst).getStatisticalData().getCount());
				logger.logEvent("Reused queries [symbols]: "+((SymbolCounterSUL)mq_sym).getStatisticalData().getCount());
				break;
			case "dlstar_v2":
				if(handler == ObservationTableCEXHandlers.CLASSIC_LSTAR)  throw new Exception("DL*M requires "+ObservationTableCEXHandlers.RIVEST_SCHAPIRE+" CexH");
				logger.logConfig("Method: DL*M_v2");
				experiment_pair = learningDLStarM_v2(mealyss, mqOracle, eqOracle, handler, strategy,obsTable);
				break;
			case "dlstar_v3":
				if(handler == ObservationTableCEXHandlers.CLASSIC_LSTAR)  throw new Exception("DL*M requires "+ObservationTableCEXHandlers.RIVEST_SCHAPIRE+" CexH");
				logger.logConfig("Method: DL*M_v3");
				experiment_pair = learningDLStarM_v3(mealyss, mqOracle, eqOracle, handler, strategy,obsTable);
				break;
			case "dlstar_v4":
				if(handler == ObservationTableCEXHandlers.CLASSIC_LSTAR)  throw new Exception("DL*M requires "+ObservationTableCEXHandlers.RIVEST_SCHAPIRE+" CexH");
				logger.logConfig("Method: DL*M_v4");
				experiment_pair = learningDLStarM_v4(mealyss, mqOracle, eqOracle, handler, strategy,obsTable);
				break;
			case "adaptive":
				logger.logConfig("Method: Adaptive");
				if(handler == ObservationTableCEXHandlers.CLASSIC_LSTAR)  throw new Exception("Adaptive requires "+ObservationTableCEXHandlers.RIVEST_SCHAPIRE+" CexH");
				experiment_pair = learningAdaptive(mealyss, mqOracle, eqOracle, handler, strategy,obsTable);
				break;
			case "ttt":
				logger.logConfig("Method: TTT");
				experiment_pair = learningTTT(mealyss, mqOracle, eqOracle, handler, strategy);
				break;
			case "lstar":
				logger.logConfig("Method: L*M");
				experiment_pair = learningLStarM(mealyss, mqOracle, eqOracle, handler, strategy);
				break;
			case "kv":
				logger.logConfig("Method: Kearns&Vazirani");
				experiment_pair = learningKV(mealyss, mqOracle, eqOracle, handler, strategy);
				break;
			case "adaptive_kv":
				logger.logConfig("Method: Adaptive Kearns&Vazirani");
				experiment_pair = learningAdaptiveKV(mealyss, mqOracle, eqOracle, handler, strategy, obsTable);
				break;
			default:
				throw new Exception("Invalid learning method selected: "+learnAlgorithm);
			}

			MealyExperiment experiment = experiment_pair.getExperiment();

			// turn on time profiling
			experiment.setProfile(true);



			// enable logging of models whether it has OT or is a tree-based one
			if (learnAlgorithm.equals("kv") || learnAlgorithm.equals("ttt") ||
					learnAlgorithm.equals("adaptive_kv")) {
				experiment.setLogOT(false);
//				Visualization.visualize(mealyss.graphView(), true);
			}
			else {
				experiment.setLogOT(true);
			}


			// run experiment
			experiment.run();

			// learning statistics
			logger.logConfig("Rounds: "+experiment.getRounds().getCount());
			logger.logStatistic(mq_rst.getStatisticalData());
			logger.logStatistic(mq_sym.getStatisticalData());
			logger.logStatistic(eq_rst.getStatisticalData());
			logger.logStatistic(eq_sym.getStatisticalData());

			// profiling
			SimpleProfiler.logResults();

			MealyMachine finalHyp = (MealyMachine) experiment.getFinalHypothesis();




			logger.logConfig("Qsize: "+mealyss.getStates().size());
			logger.logConfig("Isize: "+mealyss.getInputAlphabet().size());

			//boolean isEquiv = Automata.testEquivalence(mealyss,finalHyp, mealyss.getInputAlphabet());
			boolean isEquiv = mealyss.getStates().size()==finalHyp.getStates().size();
			if(isEquiv){
				logger.logConfig("Equivalent: OK");
			}else{
				logger.logConfig("Equivalent: NOK");
			}

			if(line.hasOption(INFO))  {
				logger.logConfig("Info: "+line.getOptionValue(INFO));
			}else{
				logger.logConfig("Info: N/A");
			}

			if(line.hasOption(SOT) && experiment_pair.getLearner() != null)  {
				StringBuffer sb = new StringBuffer();
				sb.append("Observation Table (Final Round):\n");
				new ObservationTableASCIIWriter<>().write(experiment_pair.getLearner().getObservationTable(), sb);
				logger.logEvent(sb.toString());
			}

		}
		catch( Exception exp ) {
			// automatically generate the help statement
			formatter.printHelp( "Infer_LearnLib", options );
			System.err.println( "Unexpected Exception");
			exp.printStackTrace();
		}

	}


	private static EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> buildEqOracle(
			Random rnd_seed, CommandLine line, LearnLogger logger, CompactMealy<String, Word<String>> mealyss,
			SUL<String, Word<String>> eq_sul) {
		MembershipOracle<String,Word<Word<String>>> oracleForEQoracle = new SULOracle<>(eq_sul);

		EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle;
		if(!line.hasOption(EQ)){
			logger.logEvent("EquivalenceOracle: WpMethodEQOracle("+2+")");
			return new WpMethodEQOracle<>(oracleForEQoracle, 2);
		}

		double restartProbability;
		int maxSteps, maxTests, maxLength, minLength, maxDepth, minimalSize, rndLength, bound;
		long rnd_long;
		boolean resetStepCount;

		LearnLibProperties learn_props = LearnLibProperties.getInstance();

		switch (line.getOptionValue(EQ)) {
		case "rndWalk":
			// create RandomWalkEQOracle
			restartProbability = learn_props.getRndWalk_restartProbability();
			maxSteps = learn_props.getRndWalk_maxSteps();
			resetStepCount = learn_props.getRndWalk_resetStepsCount();

			eqOracle = new RandomWalkEQOracle<String, Word<String>>(
					eq_sul, // sul
					restartProbability,// reset SUL w/ this probability before a step
					maxSteps, // max steps (overall)
					resetStepCount, // reset step count after counterexample
					rnd_seed // make results reproducible
					);
			logger.logEvent("EquivalenceOracle: RandomWalkEQOracle("+restartProbability+","+maxSteps+","+resetStepCount+")");
			break;
		case "rndWords":
			// create RandomWordsEQOracle
			maxTests = learn_props.getRndWords_maxTests();
			maxLength = learn_props.getRndWords_maxLength();
			minLength = learn_props.getRndWords_minLength();
			rnd_long = rnd_seed.nextLong();
			rnd_seed.setSeed(rnd_long);

			eqOracle = new RandomWordsEQOracle<>(oracleForEQoracle, minLength, maxLength, maxTests,rnd_seed);
			logger.logEvent("EquivalenceOracle: RandomWordsEQOracle("+minLength+", "+maxLength+", "+maxTests+", "+rnd_long+")");
			break;
		case "wp":
			maxDepth = learn_props.getW_maxDepth();
			eqOracle = new WpMethodEQOracle<>(oracleForEQoracle, maxDepth);
			logger.logEvent("EquivalenceOracle: WpMethodEQOracle("+maxDepth+")");
			break;
		case "wphyp":
			maxDepth = learn_props.getW_maxDepth();
			eqOracle = new WpMethodHypEQOracle((MealyMembershipOracle) oracleForEQoracle, maxDepth, mealyss);
			logger.logEvent("EquivalenceOracle: WpMethodHypEQOracle("+maxDepth+")");
			break;
		case "w":
			maxDepth = learn_props.getW_maxDepth();
			eqOracle = new WMethodEQOracle<>(oracleForEQoracle, maxDepth);
			logger.logEvent("EquivalenceOracle: WMethodQsizeEQOracle("+maxDepth+")");
			break;
		case "whyp":
			maxDepth = learn_props.getW_maxDepth();
			eqOracle = new WMethodHypEQOracle((MealyMembershipOracle) oracleForEQoracle, maxDepth, mealyss);
			logger.logEvent("EquivalenceOracle: WMethodHypEQOracle("+maxDepth+")");
			break;
		case "wrnd":
			minimalSize = learn_props.getWhyp_minLen();
			rndLength = learn_props.getWhyp_rndLen();
			bound = learn_props.getWhyp_bound();
			rnd_long = rnd_seed.nextLong();
			rnd_seed.setSeed(rnd_long);

			eqOracle = new RandomWMethodEQOracle<>(oracleForEQoracle, minimalSize, rndLength, bound, rnd_seed,1);
			logger.logEvent("EquivalenceOracle: RandomWMethodEQOracle("+minimalSize+","+rndLength+","+bound+","+rnd_long+")");
			break;
		case "wrndhyp":
			minimalSize = learn_props.getWhyp_minLen();
			rndLength = learn_props.getWhyp_rndLen();
			bound = learn_props.getWhyp_bound();
			rnd_long = rnd_seed.nextLong();
			rnd_seed.setSeed(rnd_long);

			eqOracle = new RandomWMethodHypEQOracle((MealyMembershipOracle) oracleForEQoracle, minimalSize, rndLength, bound, rnd_seed, 1, mealyss);
			logger.logEvent("EquivalenceOracle: RandomWMethodHypEQOracle("+minimalSize+","+rndLength+","+bound+","+rnd_long+","+1+")");
			break;
		default:
			maxDepth = 2;
			eqOracle = new WMethodEQOracle<>(oracleForEQoracle,maxDepth);
			logger.logEvent("EquivalenceOracle: WMethodEQOracle("+maxDepth+")");
			break;
		}
		return eqOracle;
	}


	private static ExperimentAndLearner learningTTT(CompactMealy<String, Word<String>> mealyss,
			MembershipOracle<String, Word<Word<String>>> mqOracle,
			EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle,
			ObservationTableCEXHandler<Object, Object> handler, ClosingStrategy<Object, Object> strategy) {

		// construct TTT instance
		TTTLearnerMealyBuilder<String,Word<String>> builder = new TTTLearnerMealyBuilder<>();
		builder.setAlphabet(mealyss.getInputAlphabet());
		builder.setAnalyzer(AcexAnalyzers.LINEAR_FWD);
		builder.setOracle(mqOracle);

		TTTLearnerMealy<String,Word<String>> learner = builder.create();

		// The experiment will execute the main loop of active learning
		MealyExperiment<String, Word<String>> experiment = new MealyExperiment<String, Word<String>> (learner, eqOracle, mealyss.getInputAlphabet());

		ExperimentAndLearner pair = new ExperimentAndLearner(learner, experiment);
		return pair;
	}

	private static ExperimentAndLearner learningKV(CompactMealy<String, Word<String>> mealyss,
			MembershipOracle<String, Word<Word<String>>> mqOracle,
			EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle,
			ObservationTableCEXHandler<Object, Object> handler, ClosingStrategy<Object, Object> strategy) {

		// construct K&V instance
		KearnsVaziraniMealyBuilder<String,Word<String>> builder = new KearnsVaziraniMealyBuilder<>();

		builder.setAlphabet(mealyss.getInputAlphabet());

		builder.setOracle(mqOracle);

		KearnsVaziraniMealy<String,Word<String>> learner = builder.create();




		// The experiment will execute the main loop of active learning
		MealyExperiment<String, Word<String>> experiment = new MealyExperiment<String, Word<String>> (learner, eqOracle, mealyss.getInputAlphabet());

		ExperimentAndLearner pair = new ExperimentAndLearner(learner, experiment);
		return pair;
	}

	private static ExperimentAndLearner learningAdaptiveKV_v1(CompactMealy<String, Word<String>> mealyss,
			MembershipOracle<String, Word<Word<String>>> mqOracle,
			EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle,
			ObservationTableCEXHandler<Object, Object> handler, ClosingStrategy<Object, Object> strategy, File reused_file)
					throws IOException {


		MyObservationTable my_ot = OTUtils.getInstance().readOT(reused_file, mealyss.getInputAlphabet());

		List<Word<String>> prefixes = my_ot.getPrefixes();
		List<Word<String>> suffixes = my_ot.getSuffixes();

		System.out.println("given Prefixes: ");
		System.out.println(prefixes);


		System.out.println("given Suffixes: ");
		System.out.println(suffixes);


		System.out.println("given Alphabet: ");
		System.out.print(mealyss.getInputAlphabet());

		AKVMBuilder AKVMbuilder = new AKVMBuilder<>();


		// construct K&V instance
		AKVMbuilder.setAlphabet(mealyss.getInputAlphabet());
		AKVMbuilder.setOracle(mqOracle);
		AKVMbuilder.setInitialPrefixes(prefixes);
		AKVMbuilder.setInitialSuffixes(suffixes);



		AdaptiveKVM<String,Word<String>> learner = AKVMbuilder.create();



		// The experiment will execute the main loop of active learning
		MealyExperiment<String, Word<String>> experiment = new MealyExperiment<String, Word<String>> (learner, eqOracle, mealyss.getInputAlphabet());

		ExperimentAndLearner pair = new ExperimentAndLearner(learner, experiment);
		return pair;
	}

	private static ExperimentAndLearner learningAdaptiveKV(CompactMealy<String, Word<String>> mealyss,
			MembershipOracle<String, Word<Word<String>>> mqOracle,
			EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle,
			ObservationTableCEXHandler<Object, Object> handler, ClosingStrategy<Object, Object> strategy, File reused_file)
					throws IOException {


		MyObservationTable my_ot = OTUtils.getInstance().readOT(reused_file, mealyss.getInputAlphabet());

		List<Word<String>> prefixes = my_ot.getPrefixes();
		List<Word<String>> suffixes = my_ot.getSuffixes();

		System.out.println("given Prefixes: ");
		System.out.println(prefixes);


		System.out.println("given Suffixes: ");
		System.out.println(suffixes);


		System.out.println("given Alphabet: ");
		System.out.print(mealyss.getInputAlphabet());

		AdaptiveKearnsVaziraniMealyBuilder AKVMbuilder = new AdaptiveKearnsVaziraniMealyBuilder<>();


		// construct K&V instance
		AKVMbuilder.setAlphabet(mealyss.getInputAlphabet());
		AKVMbuilder.setOracle(mqOracle);
		AKVMbuilder.setInitialPrefixes(prefixes);
		AKVMbuilder.setInitialSuffixes(suffixes);



		AdaptiveKearnsVaziraniMealy<String,Word<String>> learner = AKVMbuilder.create();



		// The experiment will execute the main loop of active learning
		MealyExperiment<String, Word<String>> experiment = new MealyExperiment<String, Word<String>> (learner, eqOracle, mealyss.getInputAlphabet());

		ExperimentAndLearner pair = new ExperimentAndLearner(learner, experiment);
		return pair;
	}

	protected static MyObservationTable loadObservationTable(CompactMealy<String, Word<String>> mealyss, File the_ot) throws IOException {
		// create log
		LearnLogger logger = LearnLogger.getLogger(Infer_LearnLib.class);
		logger.logEvent("Reading OT: "+the_ot.getName());

		MyObservationTable my_ot = OTUtils.getInstance().readOT(the_ot, mealyss.getInputAlphabet());

		return my_ot;

	}

	private static ExperimentAndLearner learningDLStarM_v0(
			CompactMealy<String, Word<String>> mealyss,
			MembershipOracle<String, Word<Word<String>>> mqOracle,
			EquivalenceOracle<? super MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle,
			ObservationTableCEXHandler<Object,Object> handler,
			ClosingStrategy<Object,Object> strategy,
			File ot_file) throws IOException {
		// create log
		LearnLogger logger = LearnLogger.getLogger(Infer_LearnLib.class);

		MyObservationTable my_ot = loadObservationTable(mealyss, ot_file);

		int tpi = my_ot.getPrefixes().size();
		int tsi = my_ot.getSuffixes().size();

		logger.logEvent("Revalidate OT");
		ObservationTable<String, Word<Word<String>>> reval_ot = OTUtils.getInstance().revalidateObservationTable(my_ot, mqOracle,mealyss,false);

		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		new ObservationTableASCIIWriter<>().write(reval_ot, sb);
		logger.logEvent(sb.toString());

		List<Word<String>> initPrefixes = new ArrayList<>();
		List<Word<String>> initSuffixes = new ArrayList<>();

		initPrefixes.addAll(my_ot.getPrefixes());
		initSuffixes.addAll(my_ot.getSuffixes());

		int tpf = my_ot.getPrefixes().size();
		int tsf = my_ot.getSuffixes().size();

		logger.logEvent("Reused prefixes: "+tpf+"/"+tpi);
		logger.logEvent("Reused suffixes: "+tsf+"/"+tsi);

		// construct DL*M v0 instance
		ExtensibleLStarMealyBuilder<String, Word<String>> builder = new ExtensibleLStarMealyBuilder<String, Word<String>>();
		builder.setAlphabet(mealyss.getInputAlphabet());
		builder.setOracle(mqOracle);
		builder.setInitialPrefixes(initPrefixes);
		builder.setInitialSuffixes(initSuffixes);
		builder.setCexHandler(handler);
		builder.setClosingStrategy(strategy);
		ExtensibleLStarMealy<String, Word<String>> learner = builder.create();

		// The experiment will execute the main loop of active learning
		MealyExperiment<String, Word<String>> experiment = new MealyExperiment<String, Word<String>> (learner, eqOracle, mealyss.getInputAlphabet());

		ExperimentAndLearner pair = new ExperimentAndLearner(learner, experiment);
		return pair;
	}


	private static ExperimentAndLearner learningDLStarM_v1(
			CompactMealy<String, Word<String>> mealyss,
			MembershipOracle<String, Word<Word<String>>> mqOracle,
			EquivalenceOracle<? super MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle,
			ObservationTableCEXHandler<Object,Object> handler,
			ClosingStrategy<Object,Object> strategy,
			File ot_file) throws IOException {
		// create log
		LearnLogger logger = LearnLogger.getLogger(Infer_LearnLib.class);

		MyObservationTable my_ot = loadObservationTable(mealyss, ot_file);

		int tpi = my_ot.getPrefixes().size();
		int tsi = my_ot.getSuffixes().size();

		logger.logEvent("Revalidate OT");
		ObservationTable<String, Word<Word<String>>> reval_ot = OTUtils.getInstance().revalidateObservationTable(my_ot, mqOracle,mealyss,true);

		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		new ObservationTableASCIIWriter<>().write(reval_ot, sb);
		logger.logEvent(sb.toString());

		List<Word<String>> initPrefixes = new ArrayList<>();
		List<Word<String>> initSuffixes = new ArrayList<>();

		initPrefixes.addAll(my_ot.getPrefixes());
		initSuffixes.addAll(my_ot.getSuffixes());

		int tpf = my_ot.getPrefixes().size();
		int tsf = my_ot.getSuffixes().size();

		logger.logEvent("Reused prefixes: "+tpf+"/"+tpi);
		logger.logEvent("Reused suffixes: "+tsf+"/"+tsi);

		// construct DL*M v1 instance
		ExtensibleLStarMealyBuilder<String, Word<String>> builder = new ExtensibleLStarMealyBuilder<String, Word<String>>();
		builder.setAlphabet(mealyss.getInputAlphabet());
		builder.setOracle(mqOracle);
		builder.setInitialPrefixes(initPrefixes);
		builder.setInitialSuffixes(initSuffixes);
		builder.setCexHandler(handler);
		builder.setClosingStrategy(strategy);
		ExtensibleLStarMealy<String, Word<String>> learner = builder.create();

		// The experiment will execute the main loop of active learning
		MealyExperiment<String, Word<String>> experiment = new MealyExperiment<String, Word<String>> (learner, eqOracle, mealyss.getInputAlphabet());

		ExperimentAndLearner pair = new ExperimentAndLearner(learner, experiment);
		return pair;
	}


	private static ExperimentAndLearner learningDLStarM_v2(
			CompactMealy<String, Word<String>> mealyss,
			MembershipOracle<String, Word<Word<String>>> mqOracle,
			EquivalenceOracle<? super MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle,
			ObservationTableCEXHandler<Object,Object> handler,
			ClosingStrategy<Object,Object> strategy,
			File ot_file) throws IOException {

		MyObservationTable my_ot = loadObservationTable(mealyss, ot_file);

		List<Word<String>> initPrefixes = new ArrayList<>(my_ot.getPrefixes());
		List<Word<String>> initSuffixes = new ArrayList<>(my_ot.getSuffixes());

		// construct DL*M v2 instance
		ExtensibleDLStarMealy<String, Word<String>> learner = new ExtensibleDLStarMealy<String, Word<String>>(
				mealyss.getInputAlphabet(),
				mqOracle,
				initPrefixes,
				initSuffixes,
				handler,
				strategy);

		// The experiment will execute the main loop of active learning
		MealyExperiment<String, Word<String>> experiment = new MealyExperiment<String, Word<String>> (learner, eqOracle, mealyss.getInputAlphabet());

		ExperimentAndLearner pair = new ExperimentAndLearner(learner, experiment);
		return pair;
	}

	private static ExperimentAndLearner learningDLStarM_v3(
			CompactMealy<String, Word<String>> mealyss,
			MembershipOracle<String, Word<Word<String>>> mqOracle,
			EquivalenceOracle<? super MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle,
			ObservationTableCEXHandler<Object,Object> handler,
			ClosingStrategy<Object,Object> strategy,
			File ot_file) throws IOException {

		MyObservationTable my_ot = loadObservationTable(mealyss, ot_file);

		// get alphabet of updated version
		Set<String> abc_updt = new HashSet<>(mealyss.getInputAlphabet());

		// get reused alphabet symbols (check first symbol of each S_ref word)
		Set<String> abc_reused = new HashSet<>();
		my_ot.getSuffixes().forEach(suff -> abc_reused.add(suff.getSymbol(0)));

		// find I_updt symbols that have been unreused
		boolean abc_unreused = abc_updt.removeAll(abc_reused);

		// if there is any unreused symbol from I_updt
		if(abc_unreused) {
			// then add unreused I_updt symbols to my_ot
			for (String symb : abc_updt) {
				Word<String> symb_w = Word.epsilon(); symb_w=symb_w.append(symb);
				my_ot.getSuffixes().add(symb_w);
			}
		}

		List<Word<String>> initPrefixes = new ArrayList<>(my_ot.getPrefixes());
		List<Word<String>> initSuffixes = new ArrayList<>(my_ot.getSuffixes());

		// construct DL*M v3 instance
		ExtensibleDLStarMealy<String, Word<String>> learner = new ExtensibleDLStarMealy<String, Word<String>>(
				mealyss.getInputAlphabet(),
				mqOracle,
				initPrefixes,
				initSuffixes,
				handler,
				strategy);

		// The experiment will execute the main loop of active learning
		MealyExperiment<String, Word<String>> experiment = new MealyExperiment<String, Word<String>> (learner, eqOracle, mealyss.getInputAlphabet());

		ExperimentAndLearner pair = new ExperimentAndLearner(learner, experiment);
		return pair;
	}


	private static ExperimentAndLearner learningDLStarM_v4(
			CompactMealy<String, Word<String>> mealyss,
			MembershipOracle<String, Word<Word<String>>> mqOracle,
			EquivalenceOracle<? super MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle,
			ObservationTableCEXHandler<Object,Object> handler,
			ClosingStrategy<Object,Object> strategy,
			File ot_file) throws IOException {

		MyObservationTable my_ot = loadObservationTable(mealyss, ot_file);

		// get alphabet of updated version
		Set<String> abc_updt = new HashSet<>(mealyss.getInputAlphabet());

		// get reused alphabet symbols (check each S_ref member)
		Set<String> abc_reused = new HashSet<>();
		my_ot.getSuffixes().forEach(suff -> suff.forEach(symbol -> abc_reused.add(symbol)));

		// find I_updt symbols that have been unreused
		boolean abc_unreused = abc_updt.removeAll(abc_reused);

		// if there is any unreused symbol from I_updt
		if(abc_unreused) {
			// then add unreused I_updt symbols to my_ot
			for (String symb : abc_updt) {
				Word<String> symb_w = Word.epsilon(); symb_w=symb_w.append(symb);
				my_ot.getSuffixes().add(symb_w);
			}
		}

		List<Word<String>> initPrefixes = new ArrayList<>(my_ot.getPrefixes());
		List<Word<String>> initSuffixes = new ArrayList<>(my_ot.getSuffixes());

		// construct DL*M v4 instance
		ExtensibleDLStarMealy<String, Word<String>> learner = new ExtensibleDLStarMealy<String, Word<String>>(
				mealyss.getInputAlphabet(),
				mqOracle,
				initPrefixes,
				initSuffixes,
				handler,
				strategy);

		// The experiment will execute the main loop of active learning
		MealyExperiment<String, Word<String>> experiment = new MealyExperiment<String, Word<String>> (learner, eqOracle, mealyss.getInputAlphabet());

		ExperimentAndLearner pair = new ExperimentAndLearner(learner, experiment);
		return pair;
	}


	private static ExperimentAndLearner learningAdaptive(
			CompactMealy<String, Word<String>> mealyss,
			MembershipOracle<String, Word<Word<String>>> mqOracle,
			EquivalenceOracle<? super MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle,
			ObservationTableCEXHandler<Object,Object> handler,
			ClosingStrategy<Object,Object> strategy,
			File ot_file) throws IOException {

		MyObservationTable my_ot = loadObservationTable(mealyss, ot_file);

		List<Word<String>> initPrefixes = new ArrayList<>();
		initPrefixes.add(Word.epsilon());
		List<Word<String>> initSuffixes = new ArrayList<>(my_ot.getSuffixes());

		// construct Adaptive Learning instance
		ExtensibleLStarMealyBuilder<String, Word<String>> builder = new ExtensibleLStarMealyBuilder<String, Word<String>>();
		builder.setAlphabet(mealyss.getInputAlphabet());
		builder.setOracle(mqOracle);
		builder.setInitialPrefixes(initPrefixes );
		builder.setInitialSuffixes(initSuffixes);
		builder.setCexHandler(handler);
		builder.setClosingStrategy(strategy);

		ExtensibleLStarMealy<String, Word<String>> learner = builder.create();

		// The experiment will execute the main loop of active learning
		MealyExperiment<String, Word<String>> experiment = new MealyExperiment<String, Word<String>> (learner, eqOracle, mealyss.getInputAlphabet());

		ExperimentAndLearner pair = new ExperimentAndLearner(learner, experiment);
		return pair;
	}


	private static ExperimentAndLearner learningL1(
			CompactMealy<String, Word<String>> mealyss,
			MembershipOracle<String, Word<Word<String>>> mqOracle,
			EquivalenceOracle<? super MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle,
			ObservationTableCEXHandler<Object,Object> handler,
			ClosingStrategy<Object,Object> strategy) {
		List<Word<String>> initPrefixes = new ArrayList<>();
		initPrefixes.add(Word.epsilon());
		List<Word<String>> initSuffixes = new ArrayList<>();

		// construct L1 instance
		ExtensibleLStarMealyBuilder<String, Word<String>> builder = new ExtensibleLStarMealyBuilder<String, Word<String>>();
		builder.setAlphabet(mealyss.getInputAlphabet());
		builder.setOracle(mqOracle);
		builder.setInitialPrefixes(initPrefixes );
		builder.setInitialSuffixes(initSuffixes);
		builder.setCexHandler(handler);
		builder.setClosingStrategy(strategy);

		ExtensibleLStarMealy<String, Word<String>> learner = builder.create();

		// The experiment will execute the main loop of active learning
		MealyExperiment<String, Word<String>> experiment = new MealyExperiment<String, Word<String>> (learner, eqOracle, mealyss.getInputAlphabet());

		ExperimentAndLearner pair = new ExperimentAndLearner(learner, experiment);
		return pair;
	}


	private static ExperimentAndLearner learningLStarM(
			CompactMealy<String, Word<String>> mealyss,
			MembershipOracle<String, Word<Word<String>>> mqOracle,
			EquivalenceOracle<? super MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle,
			ObservationTableCEXHandler<Object,Object> handler,
			ClosingStrategy<Object,Object> strategy) {
		List<Word<String>> initPrefixes = new ArrayList<>();
		initPrefixes.add(Word.epsilon());
		List<Word<String>> initSuffixes = new ArrayList<>();
		Word<String> word = Word.epsilon();
		for (String symbol : mealyss.getInputAlphabet()) {
			initSuffixes.add(word.append(symbol));
		}

		// construct standard L*M instance
		ExtensibleLStarMealyBuilder<String, Word<String>> builder = new ExtensibleLStarMealyBuilder<String, Word<String>>();
		builder.setAlphabet(mealyss.getInputAlphabet());
		builder.setOracle(mqOracle);
		builder.setInitialPrefixes(initPrefixes );
		builder.setInitialSuffixes(initSuffixes);
		builder.setCexHandler(handler);
		builder.setClosingStrategy(strategy);

		ExtensibleLStarMealy<String, Word<String>> learner = builder.create();

		// The experiment will execute the main loop of active learning
		MealyExperiment<String, Word<String>> experiment = new MealyExperiment<String, Word<String>> (learner, eqOracle, mealyss.getInputAlphabet());

		ExperimentAndLearner pair = new ExperimentAndLearner(learner, experiment);
		return pair;
	}


	private static ClosingStrategy<Object,Object> getClosingStrategy(String optionValue) {
		if(optionValue != null){
			if (optionValue.equals(ClosingStrategies.CLOSE_FIRST.toString())) {
				return ClosingStrategies.CLOSE_FIRST;
			}else if (optionValue.equals(ClosingStrategies.CLOSE_SHORTEST.toString())) {
				return ClosingStrategies.CLOSE_SHORTEST;
			}
		}
		return ClosingStrategies.CLOSE_FIRST;
	}


	private static ObservationTableCEXHandler<Object,Object> getCEXHandler(String optionValue) {
		if(optionValue != null){
			if (optionValue.equals(ObservationTableCEXHandlers.RIVEST_SCHAPIRE.toString())) {
				return ObservationTableCEXHandlers.RIVEST_SCHAPIRE;

			}else if (optionValue.equals(RIVEST_SCHAPIRE_ALLSUFFIXES)) {
				return ObservationTableCEXHandlers.RIVEST_SCHAPIRE_ALLSUFFIXES;
			}else if (optionValue.equals(ObservationTableCEXHandlers.SUFFIX1BY1.toString())) {
				return ObservationTableCEXHandlers.SUFFIX1BY1;
			}else if (optionValue.equals(ObservationTableCEXHandlers.CLASSIC_LSTAR.toString())) {
				return ObservationTableCEXHandlers.CLASSIC_LSTAR;
			}else if (optionValue.equals(ObservationTableCEXHandlers.MALER_PNUELI.toString())) {
				return ObservationTableCEXHandlers.MALER_PNUELI;
			}else if (optionValue.equals(ObservationTableCEXHandlers.SHAHBAZ.toString())) {
				return ObservationTableCEXHandlers.SHAHBAZ;
			}
		}
		return ObservationTableCEXHandlers.RIVEST_SCHAPIRE;
	}


	private static Options createOptions() {
		// create the Options
		Options options = new Options();
		options.addOption( SOT,  false, "Save observation table (OT)" );
		options.addOption( HELP, false, "Shows help" );
		options.addOption( CONFIG, true, "Configuration file");
		options.addOption( SUL,  true, "System Under Learning (SUL)" );
		options.addOption( DOT,  false, "SUL in .dot format" );
		options.addOption( OT,   true, "Load observation table (OT)" );
		options.addOption( OUT,  true, "Set output directory" );
		options.addOption( CLOS, true, "Set closing strategy."
				+ "\nOptions: {"+String.join(", ", closingStrategiesAvailable)+"}");
		options.addOption( EQ, 	 true, "Set equivalence query generator."
				+ "\nOptions: {"+String.join(", ", eqMethodsAvailable)+"}");
		options.addOption( CEXH, true, "Set counter example (CE) processing method."
				+ "\nOptions: {"+String.join(", ", cexHandlersAvailable)+"}");
		options.addOption( CACHE,false,"Use caching.");
		options.addOption( LEARN,true, "Model learning algorithm."
				+"\nOptions: {"+String.join(", ", learningMethodsAvailable)+"}");
		options.addOption( SEED, true, "Seed used by the random generator");
		options.addOption( INFO, true, "Add extra information as string");
		return options;
	}


	private static LearnLogger createLogfile(File out_dir, String filename) throws SecurityException, IOException {
		File filelog = new File(out_dir,filename);
		FileHandler fh = new FileHandler(filelog.getAbsolutePath());
		fh.setFormatter(new SimpleFormatter());
		LearnLogger logger;
		logger = LearnLogger.getLogger(SimpleProfiler.class);
//		logger = LearnLogger.getLogger(Experiment.class);
		return logger;

	}
}

