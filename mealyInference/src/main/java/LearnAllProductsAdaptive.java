import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.RandomWMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.WMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.EquivEQOracle.WpMethodHypEQOracle;
import br.usp.icmc.labes.mealyInference.utils.Infer_LearnLib;
import br.usp.icmc.labes.mealyInference.utils.LearnLibProperties;
import br.usp.icmc.labes.mealyInference.utils.Utils;
import de.learnlib.algorithms.kv.IKearnsVaziraniMealy;
import de.learnlib.algorithms.kv.IKearnsVaziraniMealyBuilder;
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
import java.io.FileOutputStream;
import net.automatalib.visualization.VisualizationHelper;
import de.learnlib.algorithms.kv.StateInfo;
import de.learnlib.algorithms.kv.GraphUtils;
import net.automatalib.serialization.dot.GraphDOT;
import java.io.FileInputStream;
import java.io.InputStream;
import net.automatalib.commons.util.IOUtil;
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
import net.automatalib.words.Alphabet;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.visualization.Visualization;
import net.automatalib.visualization.VisualizationHelper.EdgeAttrs;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import de.learnlib.ds.JointCounterOracle;
import de.learnlib.filter.statistic.oracle.CounterSymbolQueryOracle;

// Apache POI imports for Excel
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class LearnAllProductsAdaptive {

	// Tree and hypothesis storage for adaptive learning
	static MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree_round2 = null;
	private static ArrayList<String> allInputAlphabets = new ArrayList<>();
	private static Alphabet<String> product1Alphabet = null;
	private static CompactMealy<String, Word<String>> previousHypothesis = null;

	// Statistics storage
	private static List<ProductMetrics> allProductMetrics = new ArrayList<>();

	// Inner class to store metrics for each product
	static class ProductMetrics {
		String productName;
		int rounds;
		long mqResets;
		long mqSymbols;
		long eqResets;
		long eqSymbols;
		int states;
		int alphabetSize;
		int newSymbolsAdded;
		boolean isAdaptive;
	}

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

	// Canonicalize tree discriminators
	private static void canonicalizeTreeDiscriminators(
			AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> node,
			Alphabet<String> newAlphabet) {
		if (node == null) return;

		if (node.isLeaf()) {
			StateInfo<String, Word<Word<String>>> stateInfo = node.getData();
			if (stateInfo != null && stateInfo.accessSequence != null && stateInfo.accessSequence.length() > 0) {
				Word<String> oldAccessSeq = stateInfo.accessSequence;
				String[] canonicalSymbols = new String[oldAccessSeq.length()];
				for (int i = 0; i < oldAccessSeq.length(); i++) {
					String oldSymbol = oldAccessSeq.getSymbol(i);
					if (newAlphabet.containsSymbol(oldSymbol)) {
						int symbolIdx = newAlphabet.getSymbolIndex(oldSymbol);
						canonicalSymbols[i] = newAlphabet.getSymbol(symbolIdx);
					} else {
						canonicalSymbols[i] = oldSymbol;
					}
				}
				Word<String> newAccessSeq = Word.fromList(java.util.Arrays.asList(canonicalSymbols));
				try {
					java.lang.reflect.Field accessSeqField = StateInfo.class.getDeclaredField("accessSequence");
					accessSeqField.setAccessible(true);
					accessSeqField.set(stateInfo, newAccessSeq);
				} catch (Exception e) {
					System.err.println("ERROR: Could not update StateInfo access sequence: " + e.getMessage());
				}
			}
			return;
		}

		Word<String> oldDiscriminator = node.getDiscriminator();
		if (oldDiscriminator != null && oldDiscriminator.length() > 0) {
			String[] canonicalSymbols = new String[oldDiscriminator.length()];
			for (int i = 0; i < oldDiscriminator.length(); i++) {
				String oldSymbol = oldDiscriminator.getSymbol(i);
				if (newAlphabet.containsSymbol(oldSymbol)) {
					int symbolIdx = newAlphabet.getSymbolIndex(oldSymbol);
					canonicalSymbols[i] = newAlphabet.getSymbol(symbolIdx);
				} else {
					canonicalSymbols[i] = oldSymbol;
				}
			}
			Word<String> newDiscriminator = Word.fromList(java.util.Arrays.asList(canonicalSymbols));
			try {
				java.lang.reflect.Field discriminatorField = node.getClass().getDeclaredField("discriminator");
				discriminatorField.setAccessible(true);
				discriminatorField.set(node, newDiscriminator);
			} catch (Exception e) {
				System.err.println("ERROR: Could not update discriminator: " + e.getMessage());
			}
		}

		Collection<Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>>> children = node.getChildEntries();
		for (Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>> entry : children) {
			canonicalizeTreeDiscriminators(entry.getValue(), newAlphabet);
		}
	}

	private static void updateStateInfoInTree(
			AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> node,
			Map<Integer, Integer> stateMap) {
		if (node == null) return;
		if (!node.isLeaf()) {
			Collection<Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>>> children = node.getChildEntries();
			for (Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>> entry : children) {
				updateStateInfoInTree(entry.getValue(), stateMap);
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
		BufferedReader br = new BufferedReader(new FileReader(f));
		List<String[]> trs = new ArrayList<String[]>();
		HashSet<String> abcSet = new HashSet<>();

		while (br.ready()) {
			String line = br.readLine();
			Matcher m = kissLine.matcher(line);
			if (m.matches()) {
				String[] tr = new String[4];
				tr[0] = m.group(1);
				tr[1] = m.group(3);
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
				if (mealym.getTransition(st, in) == null) {
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

	private static EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> buildEqOracle(
			Random rnd_seed, CommandLine line, CompactMealy<String, Word<String>> mealyss,
			SUL<String, Word<String>> eq_sul) {
		MembershipOracle<String, Word<Word<String>>> oracleForEQoracle = new SULOracle<>(eq_sul);

		EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle;
		if (!line.hasOption(EQ)) {
			return new WpMethodEQOracle<>(oracleForEQoracle, 2);
		}

		double restartProbability;
		int maxSteps;
		boolean resetStepCount;

		LearnLibProperties learn_props = LearnLibProperties.getInstance();

		restartProbability = learn_props.getRndWalk_restartProbability();
		maxSteps = learn_props.getRndWalk_maxSteps();
		resetStepCount = learn_props.getRndWalk_resetStepsCount();

		eqOracle = new RandomWalkEQOracle<String, Word<String>>(eq_sul,
				restartProbability,
				maxSteps,
				resetStepCount,
				rnd_seed);

		return eqOracle;
	}

	// Helper method to learn Product 1 and return its tree and hypothesis
	private static class Product1Result {
		MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree;
		CompactMealy<String, Word<String>> hypothesis;
		GrowingAlphabet<String> alphabet;
	}
	
	private static Product1Result learnProduct1Fresh(File product1File, String[] args) throws Exception {
		System.out.println("\n" + "█".repeat(70));
		System.out.println("  LEARNING PRODUCT 1 (FRESH) FOR TREE/HYPOTHESIS EXTRACTION");
		System.out.println("█".repeat(70));
		
		CompactMealy<String, Word<String>> mealyMachine = LoadMealy(product1File);
		
		CommandLineParser parser = new BasicParser();
		Options options = createOptions();
		SUL<String, Word<String>> sulSim = new MealySimulatorSUL<>(mealyMachine, Utils.OMEGA_SYMBOL);
		
		StatisticSUL<String, Word<String>> mq_sym = new SymbolCounterSUL<>("MQ", sulSim);
		StatisticSUL<String, Word<String>> mq_rst = new ResetCounterSUL<>("MQ", mq_sym);
		
		long tstamp = System.currentTimeMillis();
		SUL<String, Word<String>> mq_sul = mq_rst;
		Random rnd_seed = new Random(tstamp);
		
		CommandLine line;
		try {
			line = parser.parse(options, args);
		} catch (ParseException e) {
			throw new Exception("Failed to parse command line options");
		}
		
		Alphabet<String> productAlphabet = mealyMachine.getInputAlphabet();
		GrowingAlphabet<String> growingAlphabet = new GrowingMapAlphabet<>(productAlphabet);
		
		MembershipOracle<String, Word<Word<String>>> mqOracle = new SULOracle<String, Word<String>>(mq_sul);
		IKearnsVaziraniMealyBuilder<Object, String, Word<String>> builder = new IKearnsVaziraniMealyBuilder<>();
		builder.setOracle(mqOracle);
		builder.setAlphabet(productAlphabet);
		IKearnsVaziraniMealy<String, Word<String>> learner = (IKearnsVaziraniMealy<String, Word<String>>) builder
				.withAlphabet(growingAlphabet).create(null, null);
		
		// Create EQ oracle
		SUL<String, Word<String>> eqSulSim = new MealySimulatorSUL<>(mealyMachine, Utils.OMEGA_SYMBOL);
		StatisticSUL<String, Word<String>> eq_sym = new SymbolCounterSUL<>("EQ", eqSulSim);
		StatisticSUL<String, Word<String>> eq_rst = new ResetCounterSUL<>("EQ", eq_sym);
		SUL<String, Word<String>> eq_sul = eq_rst;
		
		EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle = buildEqOracle(
				rnd_seed, line, mealyMachine, eq_sul);
		
		Experiment.MealyExperiment<String, Word<String>> experiment = new Experiment.MealyExperiment<String, Word<String>>(
				learner, eqOracle, learner.get_alphabet_symbol());
		
		// Run experiment
		System.out.println("Learning Product 1 from scratch...");
		experiment.run(true, null);
		
		// Extract results
		Product1Result result = new Product1Result();
		result.tree = experiment.getDiscrtree();
		if (result.tree == null) {
			result.tree = learner.getDiscriminationTree();
		}
		result.hypothesis = (CompactMealy<String, Word<String>>) experiment.getFinalHypothesis();
		result.alphabet = (GrowingAlphabet<String>) learner.get_alphabet_symbol();
		
		System.out.println("✓ Product 1 learned: " + result.hypothesis.size() + " states, alphabet size: " + result.alphabet.size());
		System.out.println("█".repeat(70) + "\n");
		
		return result;
	}

	// Save metrics to Excel file
	private static void saveMetricsToExcel(String filename) throws IOException {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Adaptive Learning Metrics");

		// Create header row
		Row headerRow = sheet.createRow(0);
		String[] headers = { "Product", "Rounds", "MQ Resets", "MQ Symbols", "EQ Resets", "EQ Symbols", "States",
				"Alphabet Size", "New Symbols Added", "Learning Type" };
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			CellStyle style = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setBold(true);
			style.setFont(font);
			cell.setCellStyle(style);
		}

		// Fill data rows
		int rowNum = 1;
		for (ProductMetrics metrics : allProductMetrics) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(metrics.productName);
			row.createCell(1).setCellValue(metrics.rounds);
			row.createCell(2).setCellValue(metrics.mqResets);
			row.createCell(3).setCellValue(metrics.mqSymbols);
			row.createCell(4).setCellValue(metrics.eqResets);
			row.createCell(5).setCellValue(metrics.eqSymbols);
			row.createCell(6).setCellValue(metrics.states);
			row.createCell(7).setCellValue(metrics.alphabetSize);
			row.createCell(8).setCellValue(metrics.newSymbolsAdded);
			row.createCell(9).setCellValue(metrics.isAdaptive ? "Adaptive" : "Normal");
		}

		// Auto-size columns
		for (int i = 0; i < headers.length; i++) {
			sheet.autoSizeColumn(i);
		}

		// Write to file
		try (FileOutputStream fileOut = new FileOutputStream(filename)) {
			workbook.write(fileOut);
		}
		workbook.close();

		System.out.println("\n✓ Metrics saved to: " + filename);
	}

	public static void main(String[] args) throws Exception {
		// Get all product files
		File productsDir = new File(".\\alternative_experiments\\Minepump_SPL\\products_3wise");
		File[] productFiles = productsDir.listFiles((dir, name) -> name.matches("\\d{5}_fsm\\.dot"));
		Arrays.sort(productFiles);

		System.out.println("╔════════════════════════════════════════════════════════════════╗");
		System.out.println("║     ADAPTIVE LEARNING - ALL MINEPUMP_SPL PRODUCTS              ║");
		System.out.println("╚════════════════════════════════════════════════════════════════╝");
		System.out.println("\nFound " + productFiles.length + " products to learn\n");
		System.out.println("Strategy: Learn Product 1 fresh before each new product");
		System.out.println("          Then reuse Product 1's tree/hypothesis for that product\n");

		// Learn each product with fresh Product 1 tree reuse
		for (int i = 0; i < productFiles.length; i++) {
			
			// For products 2-15: Learn Product 1 fresh first to get clean tree/hypothesis
			if (i > 0) {
				System.out.println("\n" + "▼".repeat(70));
				System.out.println("  PREPARING TO LEARN PRODUCT " + (i + 1));
				System.out.println("  Step 1: Learn Product 1 fresh to get tree/hypothesis");
				System.out.println("▼".repeat(70));
				
				Product1Result product1Result = learnProduct1Fresh(productFiles[0], args);
				tree_round2 = product1Result.tree;
				previousHypothesis = product1Result.hypothesis;
				product1Alphabet = product1Result.alphabet;
				
				// Reset alphabet collection to Product 1's alphabet
				allInputAlphabets.clear();
				for (String symbol : product1Alphabet) {
					allInputAlphabets.add(symbol);
				}
				
				System.out.println("  Step 2: Now learn Product " + (i + 1) + " using Product 1's tree");
			}
			File productFile = productFiles[i];
			System.out.println("\n" + "=".repeat(70));
			System.out.println("LEARNING PRODUCT " + (i + 1) + "/" + productFiles.length + ": " + productFile.getName());
			System.out.println("=".repeat(70));

			CompactMealy<String, Word<String>> mealyMachine = LoadMealy(productFile);

			CommandLineParser parser = new BasicParser();
			Options options = createOptions();
			HelpFormatter formatter = new HelpFormatter();
			SUL<String, Word<String>> sulSim = new MealySimulatorSUL<>(mealyMachine, Utils.OMEGA_SYMBOL);

			StatisticSUL<String, Word<String>> mq_sym = new SymbolCounterSUL<>("MQ", sulSim);
			StatisticSUL<String, Word<String>> mq_rst = new ResetCounterSUL<>("MQ", mq_sym);

			long tstamp = System.currentTimeMillis();
			SUL<String, Word<String>> mq_sul = mq_rst;
			Random rnd_seed = new Random(tstamp);
			Timestamp timestamp = new Timestamp(tstamp);

			CommandLine line;
			try {
				line = parser.parse(options, args);
			} catch (ParseException e) {
				formatter.printHelp("LearnAllProductsAdaptive", options);
				return;
			}

			Alphabet<String> productAlphabet = mealyMachine.getInputAlphabet();
			System.out.println("\nProduct alphabet contains " + productAlphabet.size() + " symbols");

			// Manage alphabet collection
			if (i == 0) {
				allInputAlphabets.clear();
				for (String symbol : productAlphabet) {
					allInputAlphabets.add(symbol);
				}
			} else {
				for (String symbol : productAlphabet) {
					if (!allInputAlphabets.contains(symbol)) {
						allInputAlphabets.add(symbol);
					}
				}
			}

			Alphabet<String> combinedAlphabet = Alphabets.fromCollection(allInputAlphabets);
			IKearnsVaziraniMealy<String, Word<String>> learner = null;

			StatisticSUL<String, Word<String>> mq_sym_adaptive = null;
			StatisticSUL<String, Word<String>> mq_rst_adaptive = null;

			if (i == 0) {
				// Product 0: Initialize from scratch
				product1Alphabet = new GrowingMapAlphabet<>(Alphabets.fromCollection(allInputAlphabets));
				MembershipOracle<String, Word<Word<String>>> mqOracle = new SULOracle<String, Word<String>>(mq_sul);
				IKearnsVaziraniMealyBuilder<Object, String, Word<String>> builder = new IKearnsVaziraniMealyBuilder<>();
				builder.setOracle(mqOracle);
				builder.setAlphabet(combinedAlphabet);
				learner = (IKearnsVaziraniMealy<String, Word<String>>) builder.withAlphabet(product1Alphabet)
						.create(null, null);
				System.out.println("Learning from scratch (Product 1)");
			} else {
				// Products 2+: Adaptive learning with tree reuse
				System.out.println("Adaptive learning (reusing Product 1's FRESH tree)");
				
				// Safety check: ensure tree exists
				if (tree_round2 == null || previousHypothesis == null) {
					System.err.println("ERROR: Tree or previous hypothesis is null! Cannot perform adaptive learning.");
					System.err.println("  tree_round2 is null: " + (tree_round2 == null));
					System.err.println("  previousHypothesis is null: " + (previousHypothesis == null));
					throw new IllegalStateException("Cannot perform adaptive learning without previous tree and hypothesis");
				}

				GrowingAlphabet<String> extendedAlphabet = new GrowingMapAlphabet<>(product1Alphabet);
				for (String symbol : productAlphabet) {
					if (!extendedAlphabet.containsSymbol(symbol)) {
						extendedAlphabet.addSymbol(symbol);
					}
				}

				CompactMealy<String, Word<String>> mqMealy = new CompactMealy<>(extendedAlphabet);
				Map<Integer, Integer> stateMap = new HashMap<>();
				for (Integer state : mealyMachine.getStates()) {
					stateMap.put(state, mqMealy.addState());
				}
				mqMealy.setInitialState(stateMap.get(mealyMachine.getInitialState()));

				for (Integer state : mealyMachine.getStates()) {
					for (String input : productAlphabet) {
						Integer succ = mealyMachine.getSuccessor(state, input);
						Word<String> output = mealyMachine.getOutput(state, input);
						if (succ != null) {
							String cleanInput = input;
							int symbolIdx = extendedAlphabet.getSymbolIndex(cleanInput);
							String canonicalSymbol = extendedAlphabet.getSymbol(symbolIdx);
							mqMealy.addTransition(stateMap.get(state), canonicalSymbol, stateMap.get(succ), output);
						}
					}
				}

				for (String symbol : extendedAlphabet) {
					if (!productAlphabet.containsSymbol(symbol)) {
						for (Integer state : mqMealy.getStates()) {
							mqMealy.addTransition(state, symbol, state, Utils.OMEGA_SYMBOL);
						}
					}
				}

				SUL<String, Word<String>> mqSulSim = new MealySimulatorSUL<>(mqMealy, Utils.OMEGA_SYMBOL);
				mq_sym_adaptive = new SymbolCounterSUL<>("MQ", mqSulSim);
				mq_rst_adaptive = new ResetCounterSUL<>("MQ", mq_sym_adaptive);
				SUL<String, Word<String>> mq_sul_adaptive = mq_rst_adaptive;
				MembershipOracle<String, Word<Word<String>>> mqOracle = new SULOracle<String, Word<String>>(
						mq_sul_adaptive);

				IKearnsVaziraniMealyBuilder<Object, String, Word<String>> builder = new IKearnsVaziraniMealyBuilder<>();
				builder.setOracle(mqOracle);
				builder.setAlphabet(combinedAlphabet);

				CompactMealy<String, Word<String>> adaptedHypothesis = new CompactMealy<>(extendedAlphabet);
				Map<Integer, Integer> stateMap2 = new HashMap<>();
				for (Integer oldState : previousHypothesis.getStates()) {
					Integer newState = adaptedHypothesis.addState();
					stateMap2.put(oldState, newState);
				}
				adaptedHypothesis.setInitialState(stateMap2.get(previousHypothesis.getInitialState()));

				for (Integer oldState : previousHypothesis.getStates()) {
					Integer newState = stateMap2.get(oldState);
					for (String symbol : product1Alphabet) {
						Integer oldSucc = previousHypothesis.getSuccessor(oldState, symbol);
						Word<String> output = previousHypothesis.getOutput(oldState, symbol);
						if (oldSucc != null) {
							adaptedHypothesis.addTransition(newState, symbol, stateMap2.get(oldSucc), output);
						}
					}
				}

				for (String symbol : extendedAlphabet) {
					if (!product1Alphabet.containsSymbol(symbol)) {
						for (Integer newState : adaptedHypothesis.getStates()) {
							adaptedHypothesis.addTransition(newState, symbol, newState, Utils.OMEGA_SYMBOL);
						}
					}
				}

				updateStateInfoInTree(tree_round2.getRoot(), stateMap);
				canonicalizeTreeDiscriminators(tree_round2.getRoot(), extendedAlphabet);

				learner = (IKearnsVaziraniMealy<String, Word<String>>) builder.withAlphabet(extendedAlphabet)
						.create(tree_round2, adaptedHypothesis);
			}

			// Create EQ oracle
			CompactMealy<String, Word<String>> updatedMealy;
			if (i > 0) {
				Alphabet<String> learnerAlphabet = learner.get_alphabet_symbol();
				updatedMealy = new CompactMealy<>(learnerAlphabet);
				Map<Integer, Integer> stateMap = new HashMap<>();
				for (Integer state : mealyMachine.getStates()) {
					stateMap.put(state, updatedMealy.addState());
				}
				updatedMealy.setInitialState(stateMap.get(mealyMachine.getInitialState()));

				for (Integer state : mealyMachine.getStates()) {
					for (String input : productAlphabet) {
						Integer succ = mealyMachine.getSuccessor(state, input);
						Word<String> output = mealyMachine.getOutput(state, input);
						if (succ != null) {
							updatedMealy.addTransition(stateMap.get(state), input, stateMap.get(succ), output);
						}
					}
				}

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

			SUL<String, Word<String>> eqSulSim = new MealySimulatorSUL<>(updatedMealy, Utils.OMEGA_SYMBOL);
			StatisticSUL<String, Word<String>> eq_sym = new SymbolCounterSUL<>("EQ", eqSulSim);
			StatisticSUL<String, Word<String>> eq_rst = new ResetCounterSUL<>("EQ", eq_sym);
			SUL<String, Word<String>> eq_sul = eq_rst;

			EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle = buildEqOracle(
					rnd_seed, line, updatedMealy, eq_sul);
			Experiment.MealyExperiment<String, Word<String>> experiment = new Experiment.MealyExperiment<String, Word<String>>(
					learner, eqOracle, learner.get_alphabet_symbol());

			// Run experiment
			if (i == 0) {
				experiment.run(true, null);
			} else {
				experiment.run(false, null);
			}

		// For Product 1, save tree and hypothesis for immediate next product
		// For other products, we'll re-learn Product 1 fresh before the next one
		if (i == 0) {
			tree_round2 = experiment.getDiscrtree();
			if (tree_round2 == null) {
				tree_round2 = learner.getDiscriminationTree();
				System.out.println("WARNING: Got tree from learner instead of experiment");
			}
			product1Alphabet = (GrowingAlphabet<String>) learner.get_alphabet_symbol();
			previousHypothesis = (CompactMealy<String, Word<String>>) experiment.getFinalHypothesis();
			System.out.println("✓ Saved Product 1's tree and hypothesis for Product 2");
		}

			// Collect metrics
			StatisticSUL<String, Word<String>> currentMqRst = (i == 0) ? mq_rst : mq_rst_adaptive;
			StatisticSUL<String, Word<String>> currentMqSym = (i == 0) ? mq_sym : mq_sym_adaptive;

			ProductMetrics metrics = new ProductMetrics();
			metrics.productName = productFile.getName();
			metrics.rounds = (int) experiment.getRounds().getCount();
			metrics.mqResets = ExtractValue(currentMqRst.getStatisticalData().getSummary());
			metrics.mqSymbols = ExtractValue(currentMqSym.getStatisticalData().getSummary());
			metrics.eqResets = ExtractValue(eq_rst.getStatisticalData().getSummary());
			metrics.eqSymbols = ExtractValue(eq_sym.getStatisticalData().getSummary());
			metrics.states = experiment.getFinalHypothesis().getStates().size();
			metrics.alphabetSize = learner.get_alphabet_symbol().size();
			metrics.newSymbolsAdded = (i == 0) ? 0 : (learner.get_alphabet_symbol().size() - productAlphabet.size());
			metrics.isAdaptive = (i > 0);

			allProductMetrics.add(metrics);

			System.out.println("\n========== PRODUCT " + (i + 1) + " COMPLETED ==========");
			System.out.println("Rounds: " + metrics.rounds);
			System.out.println("MQ Resets: " + metrics.mqResets + ", Symbols: " + metrics.mqSymbols);
			System.out.println("EQ Resets: " + metrics.eqResets + ", Symbols: " + metrics.eqSymbols);
			System.out.println("States: " + metrics.states);
			System.out.println("Alphabet: " + metrics.alphabetSize + " symbols");
			if (i > 0) {
				System.out.println("✓ Tree reused from previous product");
			}
			System.out.println("====================================================\n");
		}

		// Save to Excel
		String excelFilename = "AdaptiveLearning_Results_" + sdf.format(new Date()) + ".xlsx";
		saveMetricsToExcel(excelFilename);

		// Print summary
		System.out.println("\n\n");
		System.out.println("╔════════════════════════════════════════════════════════════════╗");
		System.out.println("║         ADAPTIVE LEARNING COMPLETED - SUMMARY                  ║");
		System.out.println("╚════════════════════════════════════════════════════════════════╝");
		System.out.println("\nTotal products learned: " + allProductMetrics.size());
		System.out.println("Results saved to: " + excelFilename);
		System.out.println("\nStrategy Used:");
		System.out.println("  • Product 1: Learned from scratch");
		System.out.println("  • Products 2-15: Each used a FRESH Product 1 tree");
		System.out.println("  • Product 1 was re-learned " + (productFiles.length - 1) + " times");
		System.out.println("\nAll products learned successfully with tree reuse!");
	}

	private static Options createOptions() {
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

