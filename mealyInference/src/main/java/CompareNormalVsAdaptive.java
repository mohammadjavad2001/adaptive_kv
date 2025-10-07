import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.RandomWMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.WMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.WpMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.Infer_LearnLib;
import br.usp.icmc.labes.mealyInference.utils.LearnLibProperties;
import br.usp.icmc.labes.mealyInference.utils.Utils;
import de.learnlib.algorithms.kv.KearnsVaziraniMealy;
import de.learnlib.algorithms.kv.KearnsVaziraniMealyBuilder;
import de.learnlib.api.SUL;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.statistic.StatisticSUL;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.ds.MealySimulatorSUL;
import de.learnlib.ds.ResetCounterSUL;
import de.learnlib.ds.SymbolCounterSUL;
import de.learnlib.oracle.equivalence.RandomWMethodEQOracle;
import de.learnlib.oracle.equivalence.RandomWordsEQOracle;
import de.learnlib.oracle.equivalence.WMethodEQOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.algorithms.kv.Experiment;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.transducers.MealyMachine;
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
import net.automatalib.commons.util.IOUtil;
import java.io.InputStream;
import net.automatalib.commons.util.settings.AbstractClassPathFileSource;
import net.automatalib.commons.util.settings.AbstractClassPathFileSource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.GrowingMapAlphabet;
import java.util.*;
import net.automatalib.words.Alphabet;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.visualization.Visualization;
import net.automatalib.visualization.VisualizationHelper.EdgeAttrs;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import de.learnlib.ds.AbstractWordBasedDTNode;
import de.learnlib.algorithms.kv.StateInfo;
import net.automatalib.graphs.concepts.GraphViewable;
import de.learnlib.ds.JointCounterOracle;
import de.learnlib.filter.statistic.oracle.CounterSymbolQueryOracle;


public class CompareNormalVsAdaptive {

	// Tree ذخیره شده از محصول اول
	static MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> savedTree = null;
	static GrowingAlphabet<String> savedAlphabet = null;

	private static int ExtractValue(String string_1) {
		int value_1 = 0;
		int j = string_1.lastIndexOf(" ");
		String string_2 = "";
		if (j >= 0) {
			string_2 = string_1.substring(j + 1);
		}
		value_1 = Integer.parseInt(string_2);
		return value_1;
	}

	public static final String HELP = "help";
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

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
		} else if (fileName.endsWith("dot")) {
			try {
				mealy = parser.readModel(fsm_file).model;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return mealy;
		}
		return null;
	}

	private static EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> buildEqOracle(
			Random rnd_seed, CompactMealy<String, Word<String>> mealyss, SUL<String, Word<String>> eq_sul) {
		
		LearnLibProperties learn_props = LearnLibProperties.getInstance();
		double restartProbability = learn_props.getRndWalk_restartProbability();
		int maxSteps = learn_props.getRndWalk_maxSteps();
		boolean resetStepCount = learn_props.getRndWalk_resetStepsCount();

		EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle = 
			new RandomWalkEQOracle<String, Word<String>>(eq_sul, restartProbability, maxSteps, resetStepCount, rnd_seed);

		return eqOracle;
	}

	public static void main(String[] args) throws Exception {

		System.out.println("\n================================================================");
		System.out.println("     COMPARISON: NORMAL vs ADAPTIVE LEARNING");
		System.out.println("================================================================\n");

		// مسیرها
		String baseDir = ".\\alternative_experiments\\Minepump_SPL\\products_3wise";
		String baseProduct = "00001_fsm.dot";  // محصول پایه برای ساخت tree
		String targetProduct = "00004_fsm.dot"; // محصول هدف برای مقایسه

		// ========== مرحله 1: یادگیری محصول پایه (00001) ==========
		System.out.println("════════════════════════════════════════════════════════════════");
		System.out.println("STEP 1: Learning Base Product (00001) to build tree");
		System.out.println("════════════════════════════════════════════════════════════════\n");

		File baseFile = new File(baseDir, baseProduct);
		CompactMealy<String, Word<String>> baseMealy = LoadMealy(baseFile);
		
		Alphabet<String> baseAlphabet = baseMealy.getInputAlphabet();
		savedAlphabet = new GrowingMapAlphabet<>(baseAlphabet);

		SUL<String, Word<String>> baseSulSim = new MealySimulatorSUL<>(baseMealy, Utils.OMEGA_SYMBOL);
		StatisticSUL<String, Word<String>> baseMqSym = new SymbolCounterSUL<>("MQ", baseSulSim);
		StatisticSUL<String, Word<String>> baseMqRst = new ResetCounterSUL<>("MQ", baseMqSym);
		SUL<String, Word<String>> baseMqSul = baseMqRst;

		Random rnd1 = new Random(System.currentTimeMillis());
		StatisticSUL<String, Word<String>> baseEqSym = new SymbolCounterSUL<>("EQ", baseSulSim);
		StatisticSUL<String, Word<String>> baseEqRst = new ResetCounterSUL<>("EQ", baseEqSym);
		SUL<String, Word<String>> baseEqSul = baseEqRst;

		MembershipOracle<String, Word<Word<String>>> baseMqOracle = new SULOracle<>(baseMqSul);
		EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> baseEqOracle = 
			buildEqOracle(rnd1, baseMealy, baseEqSul);

		KearnsVaziraniMealyBuilder<Object, String, Word<String>> baseBuilder = new KearnsVaziraniMealyBuilder<>();
		baseBuilder.setOracle(baseMqOracle);
		baseBuilder.setAlphabet(baseAlphabet);

		KearnsVaziraniMealy<String, Word<String>> baseLearner = baseBuilder.withAlphabet(savedAlphabet).create(null);

		Experiment.MealyExperiment<String, Word<String>> baseExperiment = 
			new Experiment.MealyExperiment<>(baseLearner, baseEqOracle, savedAlphabet);

		baseExperiment.run(true);

		// ذخیره tree و alphabet
		savedTree = baseExperiment.getDiscrtree();
		savedAlphabet = (GrowingAlphabet<String>) baseLearner.get_alphabet_symbol();

		System.out.println("\n✓ Base product learned successfully");
		System.out.println("  States: " + baseExperiment.getFinalHypothesis().getStates().size());
		System.out.println("  Rounds: " + baseExperiment.getRounds().getCount());
		System.out.println("  Tree saved with alphabet size: " + savedAlphabet.size());

		// ========== مرحله 2: یادگیری NORMAL محصول هدف (00004) ==========
		System.out.println("\n════════════════════════════════════════════════════════════════");
		System.out.println("STEP 2: NORMAL Learning of Target Product (00004)");
		System.out.println("════════════════════════════════════════════════════════════════\n");

		File targetFile = new File(baseDir, targetProduct);
		CompactMealy<String, Word<String>> targetMealy = LoadMealy(targetFile);
		Alphabet<String> targetAlphabet = targetMealy.getInputAlphabet();

		// Setup for Normal learning
		SUL<String, Word<String>> normalSulSim = new MealySimulatorSUL<>(targetMealy, Utils.OMEGA_SYMBOL);
		StatisticSUL<String, Word<String>> normalMqSym = new SymbolCounterSUL<>("MQ", normalSulSim);
		StatisticSUL<String, Word<String>> normalMqRst = new ResetCounterSUL<>("MQ", normalMqSym);
		SUL<String, Word<String>> normalMqSul = normalMqRst;

		Random rnd2 = new Random(System.currentTimeMillis());
		StatisticSUL<String, Word<String>> normalEqSym = new SymbolCounterSUL<>("EQ", normalSulSim);
		StatisticSUL<String, Word<String>> normalEqRst = new ResetCounterSUL<>("EQ", normalEqSym);
		SUL<String, Word<String>> normalEqSul = normalEqRst;

		MembershipOracle<String, Word<Word<String>>> normalMqOracle = new SULOracle<>(normalMqSul);
		EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> normalEqOracle = 
			buildEqOracle(rnd2, targetMealy, normalEqSul);

		GrowingAlphabet<String> normalAlphabet = new GrowingMapAlphabet<>(targetAlphabet);
		KearnsVaziraniMealyBuilder<Object, String, Word<String>> normalBuilder = new KearnsVaziraniMealyBuilder<>();
		normalBuilder.setOracle(normalMqOracle);
		normalBuilder.setAlphabet(targetAlphabet);

		// یادگیری از صفر (بدون tree)
		KearnsVaziraniMealy<String, Word<String>> normalLearner = normalBuilder.withAlphabet(normalAlphabet).create(null);

		Experiment.MealyExperiment<String, Word<String>> normalExperiment = 
			new Experiment.MealyExperiment<>(normalLearner, normalEqOracle, normalAlphabet);

		normalExperiment.run(true);

		long normalRounds = normalExperiment.getRounds().getCount();
		long normalMqResets = ExtractValue(normalMqRst.getStatisticalData().getSummary());
		long normalMqSymbols = ExtractValue(normalMqSym.getStatisticalData().getSummary());
		long normalEqResets = ExtractValue(normalEqRst.getStatisticalData().getSummary());
		long normalEqSymbols = ExtractValue(normalEqSym.getStatisticalData().getSummary());
		int normalStates = normalExperiment.getFinalHypothesis().getStates().size();

		System.out.println("✓ Normal learning completed");
		System.out.println("  States: " + normalStates);
		System.out.println("  Rounds: " + normalRounds);
		System.out.println("  MQ Resets: " + normalMqResets + ", Symbols: " + normalMqSymbols);
		System.out.println("  EQ Resets: " + normalEqResets + ", Symbols: " + normalEqSymbols);

		// ========== مرحله 3: یادگیری ADAPTIVE محصول هدف (00004) ==========
		System.out.println("\n════════════════════════════════════════════════════════════════");
		System.out.println("STEP 3: ADAPTIVE Learning of Target Product (00004)");
		System.out.println("════════════════════════════════════════════════════════════════\n");

		// توسعه alphabet
		GrowingAlphabet<String> adaptiveAlphabet = (GrowingAlphabet<String>) savedAlphabet;
		for (String symbol : targetAlphabet) {
			if (!adaptiveAlphabet.containsSymbol(symbol)) {
				adaptiveAlphabet.addSymbol(symbol);
			}
		}

		// ساخت Mealy با alphabet توسعه‌یافته
		CompactMealy<String, Word<String>> adaptiveMealy = new CompactMealy<>(adaptiveAlphabet);
		Map<Integer, Integer> stateMap = new HashMap<>();
		for (Integer state : targetMealy.getStates()) {
			stateMap.put(state, adaptiveMealy.addState());
		}
		adaptiveMealy.setInitialState(stateMap.get(targetMealy.getInitialState()));

		// کپی transitions
		for (Integer state : targetMealy.getStates()) {
			for (String input : targetAlphabet) {
				Integer succ = targetMealy.getSuccessor(state, input);
				Word<String> output = targetMealy.getOutput(state, input);
				if (succ != null) {
					adaptiveMealy.addTransition(stateMap.get(state), input, stateMap.get(succ), output);
				}
			}
		}

		// Self-loops برای سمبول‌های مفقود
		for (String symbol : adaptiveAlphabet) {
			if (!targetAlphabet.containsSymbol(symbol)) {
				for (Integer state : adaptiveMealy.getStates()) {
					adaptiveMealy.addTransition(state, symbol, state, Utils.OMEGA_SYMBOL);
				}
			}
		}

		// Setup for Adaptive learning
		SUL<String, Word<String>> adaptiveMqSulSim = new MealySimulatorSUL<>(adaptiveMealy, Utils.OMEGA_SYMBOL);
		StatisticSUL<String, Word<String>> adaptiveMqSym = new SymbolCounterSUL<>("MQ", adaptiveMqSulSim);
		StatisticSUL<String, Word<String>> adaptiveMqRst = new ResetCounterSUL<>("MQ", adaptiveMqSym);
		SUL<String, Word<String>> adaptiveMqSul = adaptiveMqRst;

		Random rnd3 = new Random(System.currentTimeMillis());
		
		CompactMealy<String, Word<String>> adaptiveEqMealy = new CompactMealy<>(adaptiveAlphabet);
		Map<Integer, Integer> eqStateMap = new HashMap<>();
		for (Integer state : targetMealy.getStates()) {
			eqStateMap.put(state, adaptiveEqMealy.addState());
		}
		adaptiveEqMealy.setInitialState(eqStateMap.get(targetMealy.getInitialState()));
		for (Integer state : targetMealy.getStates()) {
			for (String input : targetAlphabet) {
				Integer succ = targetMealy.getSuccessor(state, input);
				Word<String> output = targetMealy.getOutput(state, input);
				if (succ != null) {
					adaptiveEqMealy.addTransition(eqStateMap.get(state), input, eqStateMap.get(succ), output);
				}
			}
		}
		for (String symbol : adaptiveAlphabet) {
			if (!targetAlphabet.containsSymbol(symbol)) {
				for (Integer state : adaptiveEqMealy.getStates()) {
					adaptiveEqMealy.addTransition(state, symbol, state, Utils.OMEGA_SYMBOL);
				}
			}
		}

		SUL<String, Word<String>> adaptiveEqSulSim = new MealySimulatorSUL<>(adaptiveEqMealy, Utils.OMEGA_SYMBOL);
		StatisticSUL<String, Word<String>> adaptiveEqSym = new SymbolCounterSUL<>("EQ", adaptiveEqSulSim);
		StatisticSUL<String, Word<String>> adaptiveEqRst = new ResetCounterSUL<>("EQ", adaptiveEqSym);
		SUL<String, Word<String>> adaptiveEqSul = adaptiveEqRst;

		MembershipOracle<String, Word<Word<String>>> adaptiveMqOracle = new SULOracle<>(adaptiveMqSul);
		EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> adaptiveEqOracle = 
			buildEqOracle(rnd3, adaptiveEqMealy, adaptiveEqSul);

		KearnsVaziraniMealyBuilder<Object, String, Word<String>> adaptiveBuilder = new KearnsVaziraniMealyBuilder<>();
		adaptiveBuilder.setOracle(adaptiveMqOracle);
		adaptiveBuilder.setAlphabet(adaptiveAlphabet);

		// یادگیری با tree ذخیره شده
		KearnsVaziraniMealy<String, Word<String>> adaptiveLearner = adaptiveBuilder.withAlphabet(adaptiveAlphabet).create(savedTree);

		// اضافه کردن سمبول‌های جدید
		int newSymbolsCount = 0;
		for (String symbol : targetAlphabet) {
			if (!savedAlphabet.containsSymbol(symbol)) {
				adaptiveLearner.addAlphabetSymbol(symbol);
				newSymbolsCount++;
			}
		}

		Experiment.MealyExperiment<String, Word<String>> adaptiveExperiment = 
			new Experiment.MealyExperiment<>(adaptiveLearner, adaptiveEqOracle, adaptiveLearner.get_alphabet_symbol());

		adaptiveExperiment.run(false);

		long adaptiveRounds = adaptiveExperiment.getRounds().getCount();
		long adaptiveMqResets = ExtractValue(adaptiveMqRst.getStatisticalData().getSummary());
		long adaptiveMqSymbols = ExtractValue(adaptiveMqSym.getStatisticalData().getSummary());
		long adaptiveEqResets = ExtractValue(adaptiveEqRst.getStatisticalData().getSummary());
		long adaptiveEqSymbols = ExtractValue(adaptiveEqSym.getStatisticalData().getSummary());
		int adaptiveStates = adaptiveExperiment.getFinalHypothesis().getStates().size();

		System.out.println("✓ Adaptive learning completed");
		System.out.println("  States: " + adaptiveStates);
		System.out.println("  Rounds: " + adaptiveRounds);
		System.out.println("  MQ Resets: " + adaptiveMqResets + ", Symbols: " + adaptiveMqSymbols);
		System.out.println("  EQ Resets: " + adaptiveEqResets + ", Symbols: " + adaptiveEqSymbols);
		System.out.println("  New symbols added: " + newSymbolsCount);

		// ========== مرحله 4: مقایسه نتایج ==========
		System.out.println("\n\n================================================================");
		System.out.println("                 COMPARISON RESULTS");
		System.out.println("================================================================\n");

		System.out.println("Product: " + targetProduct);
		System.out.println("Base Product (for tree): " + baseProduct);
		System.out.println();

		System.out.println("----------------------------------------------------------------");
		System.out.println("                    NORMAL LEARNING");
		System.out.println("----------------------------------------------------------------");
		System.out.println("  States:                 " + String.format("%5d", normalStates));
		System.out.println("  Rounds (EQ queries):    " + String.format("%5d", normalRounds));
		System.out.println("  MQ Resets:              " + String.format("%5d", normalMqResets));
		System.out.println("  MQ Symbols:             " + String.format("%5d", normalMqSymbols));
		System.out.println("  EQ Resets:              " + String.format("%5d", normalEqResets));
		System.out.println("  EQ Symbols:             " + String.format("%5d", normalEqSymbols));
		System.out.println("----------------------------------------------------------------");
		System.out.println();

		System.out.println("----------------------------------------------------------------");
		System.out.println("                   ADAPTIVE LEARNING");
		System.out.println("----------------------------------------------------------------");
		System.out.println("  States:                 " + String.format("%5d", adaptiveStates));
		System.out.println("  Rounds (EQ queries):    " + String.format("%5d", adaptiveRounds));
		System.out.println("  MQ Resets:              " + String.format("%5d", adaptiveMqResets));
		System.out.println("  MQ Symbols:             " + String.format("%5d", adaptiveMqSymbols));
		System.out.println("  EQ Resets:              " + String.format("%5d", adaptiveEqResets));
		System.out.println("  EQ Symbols:             " + String.format("%5d", adaptiveEqSymbols));
		System.out.println("  Tree reused:            YES from " + baseProduct);
		System.out.println("----------------------------------------------------------------");
		System.out.println();

		System.out.println("----------------------------------------------------------------");
		System.out.println("                  IMPROVEMENT (BENEFIT)");
		System.out.println("----------------------------------------------------------------");
		
		double roundsReduction = ((double)(normalRounds - adaptiveRounds) / normalRounds) * 100;
		double mqResetsReduction = ((double)(normalMqResets - adaptiveMqResets) / normalMqResets) * 100;
		double mqSymbolsReduction = ((double)(normalMqSymbols - adaptiveMqSymbols) / normalMqSymbols) * 100;
		double eqResetsReduction = ((double)(normalEqResets - adaptiveEqResets) / normalEqResets) * 100;
		double eqSymbolsReduction = ((double)(normalEqSymbols - adaptiveEqSymbols) / normalEqSymbols) * 100;

		System.out.println("  Rounds reduction:       " + String.format("%6.2f%%", roundsReduction));
		System.out.println("  MQ Resets reduction:    " + String.format("%6.2f%%", mqResetsReduction));
		System.out.println("  MQ Symbols reduction:   " + String.format("%6.2f%%", mqSymbolsReduction));
		System.out.println("  EQ Resets reduction:    " + String.format("%6.2f%%", eqResetsReduction));
		System.out.println("  EQ Symbols reduction:   " + String.format("%6.2f%%", eqSymbolsReduction));
		System.out.println("----------------------------------------------------------------");
		System.out.println();

		System.out.println("================================================================\n");
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption(HELP, false, "Shows help");
		return options;
	}
}

