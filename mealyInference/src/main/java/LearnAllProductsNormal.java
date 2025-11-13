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
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.function.Function;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStream;
import net.automatalib.commons.util.IOUtil;
import net.automatalib.commons.util.settings.AbstractClassPathFileSource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.Alphabet;

// Apache POI imports for Excel
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;

public class LearnAllProductsNormal {

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
		String learningType;
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

		eqOracle = new RandomWalkEQOracle<String, Word<String>>(eq_sul, // sul
				restartProbability, // reset SUL w/ this probability before a step
				maxSteps, // max steps (overall)
				resetStepCount, // reset step count after counterexample
				rnd_seed // make results reproducible
		);

		return eqOracle;
	}

	// Save metrics to Excel file
	private static void saveMetricsToExcel(String filename) throws IOException {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Normal Learning Metrics");

		// Create header row
		Row headerRow = sheet.createRow(0);
		String[] headers = { "Product", "Rounds", "MQ Resets", "MQ Symbols", "EQ Resets", "EQ Symbols", "States",
				"Alphabet Size", "Learning Type" };
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
			row.createCell(8).setCellValue(metrics.learningType);
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
		System.out.println("║     NORMAL LEARNING - ALL MINEPUMP_SPL PRODUCTS                ║");
		System.out.println("╚════════════════════════════════════════════════════════════════╝");
		System.out.println("\nFound " + productFiles.length + " products to learn\n");
		System.out.println("Each product will be learned from scratch (no tree reuse)\n");

		// Learn each product independently from scratch
		for (int i = 0; i < productFiles.length; i++) {
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
				formatter.printHelp("LearnAllProductsNormal", options);
				return;
			}

			StatisticSUL<String, Word<String>> eq_sym = new SymbolCounterSUL<>("EQ", sulSim);
			StatisticSUL<String, Word<String>> eq_rst = new ResetCounterSUL<>("EQ", eq_sym);

			SUL<String, Word<String>> eq_sul = eq_rst;

			MealySimulatorSUL<String, Word<String>> sul = new MealySimulatorSUL<>(mealyMachine);
			EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle = buildEqOracle(
					rnd_seed, line, mealyMachine, eq_sul);

			SymbolCounterSUL<String, Word<String>> mqSym = new SymbolCounterSUL<>("MQ", sul);
			ResetCounterSUL<String, Word<String>> mqRst = new ResetCounterSUL<>("MQ", mqSym);
			MembershipOracle<String, Word<Word<String>>> mqOracle = new SULOracle<String, Word<String>>(mq_sul);

			KearnsVaziraniMealyBuilder<String, Word<String>> builder = new KearnsVaziraniMealyBuilder<String, Word<String>>();

			builder.setAlphabet(mealyMachine.getInputAlphabet());
			builder.setOracle(mqOracle);

			KearnsVaziraniMealy<String, Word<String>> learner = builder.create();

			Experiment1.MealyExperiment<String, Word<String>> experiment = new Experiment1.MealyExperiment<String, Word<String>>(
					learner, eqOracle, mealyMachine.getInputAlphabet());

			// Run the experiment
			System.out.println("Learning from scratch (no tree reuse)...");
			experiment.run();

			// Collect metrics
			ProductMetrics metrics = new ProductMetrics();
			metrics.productName = productFile.getName();
			metrics.rounds = (int) experiment.getRounds().getCount();
			metrics.mqResets = ExtractValue(mq_rst.getStatisticalData().getSummary());
			metrics.mqSymbols = ExtractValue(mq_sym.getStatisticalData().getSummary());
			metrics.eqResets = ExtractValue(eq_rst.getStatisticalData().getSummary());
			metrics.eqSymbols = ExtractValue(eq_sym.getStatisticalData().getSummary());
			metrics.states = experiment.getFinalHypothesis().getStates().size();
			metrics.alphabetSize = mealyMachine.getInputAlphabet().size();
			metrics.learningType = "Normal (No Reuse)";

			allProductMetrics.add(metrics);

			System.out.println("\n========== PRODUCT " + (i + 1) + " COMPLETED ==========");
			System.out.println("Rounds: " + metrics.rounds);
			System.out.println("MQ Resets: " + metrics.mqResets + ", Symbols: " + metrics.mqSymbols);
			System.out.println("EQ Resets: " + metrics.eqResets + ", Symbols: " + metrics.eqSymbols);
			System.out.println("States: " + metrics.states);
			System.out.println("Alphabet: " + metrics.alphabetSize + " symbols");
			System.out.println("✗ No tree reuse (learned from scratch)");
			System.out.println("====================================================\n");
		}

		// Save to Excel
		String excelFilename = "NormalLearning_Results_" + sdf.format(new Date()) + ".xlsx";
		saveMetricsToExcel(excelFilename);

		// Print summary
		System.out.println("\n\n");
		System.out.println("╔════════════════════════════════════════════════════════════════╗");
		System.out.println("║         NORMAL LEARNING COMPLETED - SUMMARY                    ║");
		System.out.println("╚════════════════════════════════════════════════════════════════╝");
		System.out.println("\nTotal products learned: " + allProductMetrics.size());
		System.out.println("Results saved to: " + excelFilename);
		System.out.println("\nAll products learned independently from scratch!");
		
		// Calculate totals for comparison
		long totalMqResets = 0;
		long totalMqSymbols = 0;
		long totalEqResets = 0;
		long totalEqSymbols = 0;
		int totalRounds = 0;
		
		for (ProductMetrics m : allProductMetrics) {
			totalMqResets += m.mqResets;
			totalMqSymbols += m.mqSymbols;
			totalEqResets += m.eqResets;
			totalEqSymbols += m.eqSymbols;
			totalRounds += m.rounds;
		}
		
		System.out.println("\nTOTAL METRICS ACROSS ALL PRODUCTS:");
		System.out.println("  Total Rounds: " + totalRounds);
		System.out.println("  Total MQ Resets: " + totalMqResets);
		System.out.println("  Total MQ Symbols: " + totalMqSymbols);
		System.out.println("  Total EQ Resets: " + totalEqResets);
		System.out.println("  Total EQ Symbols: " + totalEqSymbols);
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

