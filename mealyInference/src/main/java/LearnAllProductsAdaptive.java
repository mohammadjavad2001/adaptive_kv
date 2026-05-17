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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cached Membership Oracle for reusing query results and reducing duplicate queries.
 * This significantly improves performance in adaptive learning scenarios.
 * 
 * CRITICAL: Cache key uses Pair(prefix, suffix) NOT concat(prefix, suffix)
 * because answerQuery(prefix, suffix) returns different output than answerQuery(concat)
 */
class AdaptiveCachedMembershipOracle<I, O> implements MembershipOracle<I, O> {
    private final MembershipOracle<I, O> delegate;
    // Use Pair<prefix, suffix> as cache key to correctly distinguish queries
    private final Map<Pair<Word<I>, Word<I>>, O> cache;
    private int cacheHits = 0;
    private int cacheMisses = 0;
    
    public AdaptiveCachedMembershipOracle(MembershipOracle<I, O> delegate) {
        this.delegate = delegate;
        this.cache = new ConcurrentHashMap<>();
    }
    
    @Override
    public O answerQuery(Word<I> prefix, Word<I> suffix) {
        // CRITICAL FIX: Use Pair(prefix, suffix) as cache key
        // answerQuery("a", "b") != answerQuery("", "ab") in Mealy machines!
        Pair<Word<I>, Word<I>> cacheKey = Pair.of(prefix, suffix);
        
        // Check cache first
        if (cache.containsKey(cacheKey)) {
            cacheHits++;
            return cache.get(cacheKey);
        }
        
        // Cache miss - query the delegate oracle
        cacheMisses++;
        O result = delegate.answerQuery(prefix, suffix);
        cache.put(cacheKey, result);
        return result;
    }
    
    @Override
    public O answerQuery(Word<I> query) {
        // For single word query, prefix is empty (epsilon)
        return answerQuery(Word.epsilon(), query);
    }
    
    @Override
    public void processQueries(Collection<? extends de.learnlib.api.query.Query<I, O>> queries) {
        for (de.learnlib.api.query.Query<I, O> query : queries) {
            O answer = answerQuery(query.getPrefix(), query.getSuffix());
            query.answer(answer);
        }
    }
    
    public void printStatistics() {
        printStatistics("CACHE");
    }

    public void printStatistics(String label) {
        int totalQueries = cacheHits + cacheMisses;
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║              " + String.format("%-38s", label + " STATISTICS") + "║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.println("║  Total Queries:    " + String.format("%-30d", totalQueries) + "║");
        System.out.println("║  Cache Hits:       " + String.format("%-30d", cacheHits) + "║");
        System.out.println("║  Cache Misses:     " + String.format("%-30d", cacheMisses) + "║");
        System.out.println("║  Cache Size:       " + String.format("%-30d", cache.size()) + "║");
        if (totalQueries > 0) {
            double hitRate = (cacheHits * 100.0) / totalQueries;
            System.out.println("║  Hit Rate:         " + String.format("%-29.2f%%", hitRate) + "║");
            System.out.println("║  Resets Saved:     " + String.format("%-30d", cacheHits) + "║");
        }
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
    }
    
    public int getCacheHits() {
        return cacheHits;
    }
    
    public int getCacheMisses() {
        return cacheMisses;
    }
    
    public int getCacheSize() {
        return cache.size();
    }
    
    public void clearCache() {
        cache.clear();
        cacheHits = 0;
        cacheMisses = 0;
    }
}

public class LearnAllProductsAdaptive {

	// Tree and hypothesis storage for adaptive learning
	static MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree_round2 = null;
	private static ArrayList<String> allInputAlphabets = new ArrayList<>();
	private static Alphabet<String> previousProductAlphabet = null;
	private static CompactMealy<String, Word<String>> previousHypothesis = null;
	// Store cached oracles for statistics
	private static AdaptiveCachedMembershipOracle<String, Word<Word<String>>> cachedOracle = null;
	private static AdaptiveCachedMembershipOracle<String, Word<Word<String>>> cachedEqOracle = null;

	// Statistics storage
	private static List<ProductMetrics> allProductMetrics = new ArrayList<>();

	// Inner class to store metrics for each product
	static class ProductMetrics {
		String productName;
		int rounds;
		long mqResets;
		long mqSymbols;
		/** Count of {@code pre()} on the EQ SUL (one increment per EQ-side membership query reset). */
		long eqResets;
		/** Count of {@code step()} on the EQ SUL (total input symbols applied during equivalence testing). */
		long eqSymbols;
		int states;
		int alphabetSize;
		int newSymbolsAdded;
		boolean isAdaptive;
		// MQ cache statistics
		int cacheHits;
		int cacheMisses;
		int cacheSize;
		double cacheHitRate;
		// EQ cache statistics
		int eqCacheHits;
		int eqCacheMisses;
		int eqCacheSize;
		double eqCacheHitRate;
		int originalStates;
		boolean equivalent;
	}

	/** Post-learning check: learned hypothesis vs original product model on product alphabet. */
	private static void checkEquivalence(
			String productName,
			CompactMealy<String, Word<String>> mealyMachine,
			CompactMealy<String, Word<String>> finalHyp,
			Alphabet<String> productAlphabet,
			ProductMetrics metrics) {
		int expectedStates = mealyMachine.size();
		int learnedStates = finalHyp.getStates().size();
		Word<String> separatingWord = DeterministicEquivalenceTest.findSeparatingWord(
				mealyMachine, finalHyp, productAlphabet);
		boolean isEquivalent = (separatingWord == null);

		metrics.originalStates = expectedStates;
		metrics.equivalent = isEquivalent;

		System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
		System.out.println("║  EQUIVALENCE CHECK: " + productName);
		System.out.println("╠═══════════════════════════════════════════════════════════╣");
		System.out.println("║  Original model states:  " + expectedStates);
		System.out.println("║  Learned model states:   " + learnedStates);
		if (isEquivalent) {
			System.out.println("║  ✓ EQUIVALENT - Model learned correctly!");
		} else {
			System.out.println("║  ✗ NOT EQUIVALENT - Model incomplete!");
			System.out.println("║  Separating word: " + separatingWord);
			System.out.println("║  Expected output: " + mealyMachine.computeOutput(separatingWord));
			System.out.println("║  Learned output:  " + finalHyp.computeOutput(separatingWord));
		}
		System.out.println("╚═══════════════════════════════════════════════════════════╝");
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
	/** WpMethod lookahead for EQ when {@code -eq} is not set (default 2). Lower values may reduce EQ work but can break completeness for some models. */
	public static final String EQWP = "eqwp";
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
			MembershipOracle<String, Word<Word<String>>> oracleForEQoracle,
			SUL<String, Word<String>> eq_sul) {
		EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle;
		if (!line.hasOption(EQ)) {
			int wpLookahead = 2;
			if (line.hasOption(EQWP)) {
				try {
					wpLookahead = Integer.parseInt(line.getOptionValue(EQWP));
				} catch (NumberFormatException e) {
					System.err.println("LearnAllProductsAdaptive: invalid -eqwp, using 2");
					wpLookahead = 2;
				}
				if (wpLookahead < 1) {
					wpLookahead = 1;
				}
			}
			return new WpMethodEQOracle<>(oracleForEQoracle, wpLookahead);
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

	private static class ProductInfo {
		int index;
		File file;
		Set<String> alphabet;
		double dPrime;

		ProductInfo(int index, File file, Set<String> alphabet) {
			this.index = index;
			this.file = file;
			this.alphabet = alphabet;
			this.dPrime = 0.0;
		}
	}

	private static CompactMealy<String, Word<String>> loadMealyForOrder(File productFile) throws Exception {
		return LoadMealy(productFile);
	}

	static List<ProductInfo> calculateOptimalOrder(File[] productFiles) throws Exception {
		System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
		System.out.println("║ CALCULATING OPTIMAL PRODUCT LEARNING ORDER (D')               ║");
		System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

		List<ProductInfo> products = new ArrayList<>();
		for (int i = 0; i < productFiles.length; i++) {
			CompactMealy<String, Word<String>> mealy = loadMealyForOrder(productFiles[i]);
			Set<String> alphabet = new HashSet<>();
			for (String symbol : mealy.getInputAlphabet()) {
				alphabet.add(symbol);
			}
			products.add(new ProductInfo(i, productFiles[i], alphabet));
		}

		ProductInfo firstProduct = products.stream()
			.min(Comparator.comparingInt(p -> p.alphabet.size()))
			.orElse(products.get(0));
		System.out.println("✓ First product (smallest alphabet): " + firstProduct.file.getName() + " with " + firstProduct.alphabet.size() + " symbols");

		List<ProductInfo> orderedProducts = new ArrayList<>();
		orderedProducts.add(firstProduct);
		Set<String> learnedSymbols = new HashSet<>(firstProduct.alphabet);
		List<ProductInfo> remaining = new ArrayList<>(products);
		remaining.remove(firstProduct);

		while (!remaining.isEmpty()) {
			for (ProductInfo product : remaining) {
				product.dPrime = 0;
				for (String symbol : product.alphabet) {
					if (!learnedSymbols.contains(symbol)) {
						product.dPrime++;
					}
				}
			}

			remaining.sort(Comparator.comparingDouble(p -> p.dPrime));
			ProductInfo nextProduct = remaining.remove(0);
			orderedProducts.add(nextProduct);
			learnedSymbols.addAll(nextProduct.alphabet);
			System.out.println(" Product " + orderedProducts.size() + ": " + nextProduct.file.getName() + " (D'=" + String.format("%.0f", nextProduct.dPrime) + ", alphabet=" + nextProduct.alphabet.size() + ", new symbols=" + (int) nextProduct.dPrime + ")");
		}

		System.out.println("\n✓ Optimal learning order calculated using D' heuristic");
		System.out.println(" Strategy: Learn products with fewer NEW symbols first\n");
		return orderedProducts;
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
		
		// EQ: current product alphabet only, with cached membership oracle
		SUL<String, Word<String>> eqSulSim = new MealySimulatorSUL<>(mealyMachine, Utils.OMEGA_SYMBOL);
		StatisticSUL<String, Word<String>> eq_sym = new SymbolCounterSUL<>("EQ", eqSulSim);
		StatisticSUL<String, Word<String>> eq_rst = new ResetCounterSUL<>("EQ", eq_sym);
		SUL<String, Word<String>> eq_sul = eq_rst;
		cachedEqOracle = new AdaptiveCachedMembershipOracle<>(new SULOracle<>(eq_sul));

		EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle = buildEqOracle(
				rnd_seed, line, mealyMachine, cachedEqOracle, eq_sul);

		Experiment.MealyExperiment<String, Word<String>> experiment = new Experiment.MealyExperiment<String, Word<String>>(
				learner, eqOracle, productAlphabet);
		
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
				"Original States", "Equivalent", "Alphabet Size", "New Symbols Added", "Learning Type", "MQ Cache Hits",
				"MQ Cache Misses", "MQ Cache Size", "MQ Cache Hit Rate %", "EQ Cache Hits", "EQ Cache Misses",
				"EQ Cache Size", "EQ Cache Hit Rate %" };
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
			row.createCell(7).setCellValue(metrics.originalStates);
			row.createCell(8).setCellValue(metrics.equivalent ? "Yes" : "No");
			row.createCell(9).setCellValue(metrics.alphabetSize);
			row.createCell(10).setCellValue(metrics.newSymbolsAdded);
			row.createCell(11).setCellValue(metrics.isAdaptive ? "Adaptive" : "Normal");
			row.createCell(12).setCellValue(metrics.cacheHits);
			row.createCell(13).setCellValue(metrics.cacheMisses);
			row.createCell(14).setCellValue(metrics.cacheSize);
			row.createCell(15).setCellValue(metrics.cacheHitRate);
			row.createCell(16).setCellValue(metrics.eqCacheHits);
			row.createCell(17).setCellValue(metrics.eqCacheMisses);
			row.createCell(18).setCellValue(metrics.eqCacheSize);
			row.createCell(19).setCellValue(metrics.eqCacheHitRate);
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
		if (productFiles == null || productFiles.length == 0) {
			System.err.println("No product files found in " + productsDir.getAbsolutePath());
			return;
		}
		Arrays.sort(productFiles);

		List<ProductInfo> orderedProducts = calculateOptimalOrder(productFiles);

		System.out.println("╔════════════════════════════════════════════════════════════════╗");
		System.out.println("║     ADAPTIVE LEARNING - ALL MINEPUMP_SPL PRODUCTS              ║");
		System.out.println("╚════════════════════════════════════════════════════════════════╝");
		System.out.println("\nFound " + orderedProducts.size() + " products to learn\n");
		System.out.println("Strategy: Learn in optimal D' order");
		System.out.println("          First learn the smallest alphabet product, then products that add fewer new symbols\n");

		// Learn each product using the previous product's tree/hypothesis chain
		for (int i = 0; i < orderedProducts.size(); i++) {
			if (i > 0) {
				System.out.println("\n" + "▼".repeat(70));
				System.out.println("  PREPARING TO LEARN PRODUCT " + (i + 1));
				System.out.println("  Step 1: Use the previous product's tree and hypothesis");
				System.out.println("▼".repeat(70));
			}
			File productFile = orderedProducts.get(i).file;
			System.out.println("\n" + "=".repeat(70));
			System.out.println("LEARNING PRODUCT " + (i + 1) + "/" + orderedProducts.size() + ": " + productFile.getName());
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
			// First ordered product: initialize from scratch and learn to round 7
			previousProductAlphabet = new GrowingMapAlphabet<>(Alphabets.fromCollection(allInputAlphabets));
			MembershipOracle<String, Word<Word<String>>> mqOracle = new SULOracle<String, Word<String>>(mq_sul);
			IKearnsVaziraniMealyBuilder<Object, String, Word<String>> builder = new IKearnsVaziraniMealyBuilder<>();
			builder.setOracle(mqOracle);
			builder.setAlphabet(combinedAlphabet);
			learner = (IKearnsVaziraniMealy<String, Word<String>>) builder.withAlphabet(previousProductAlphabet)
					.create(null, null);
			System.out.println("Learning first ordered product from scratch");
		} else {
			// Subsequent products: reuse tree/hypothesis from the previous ordered product
			System.out.println("Adaptive learning (reusing previous product's tree)");
				// Safety check: ensure tree exists
				if (tree_round2 == null || previousHypothesis == null) {
					System.err.println("ERROR: Tree or previous hypothesis is null! Cannot perform adaptive learning.");
					System.err.println("  tree_round2 is null: " + (tree_round2 == null));
					System.err.println("  previousHypothesis is null: " + (previousHypothesis == null));
					throw new IllegalStateException("Cannot perform adaptive learning without previous tree and hypothesis");
				}

				GrowingAlphabet<String> extendedAlphabet = new GrowingMapAlphabet<>(previousProductAlphabet);
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

				// 🔥 Enable Cache Layer for Adaptive Learning to reduce duplicate queries
				cachedOracle = new AdaptiveCachedMembershipOracle<>(mqOracle);
				mqOracle = cachedOracle;
				System.out.println("✓ Cache layer enabled for Product " + (i + 1) + " - duplicate queries will be avoided");

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
					for (String symbol : previousProductAlphabet) {
						Integer oldSucc = previousHypothesis.getSuccessor(oldState, symbol);
						Word<String> output = previousHypothesis.getOutput(oldState, symbol);
						if (oldSucc != null) {
							adaptedHypothesis.addTransition(newState, symbol, stateMap2.get(oldSucc), output);
						}
					}
				}

				for (String symbol : extendedAlphabet) {
					if (!previousProductAlphabet.containsSymbol(symbol)) {
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

			// EQ: current product alphabet only (not extended learner alphabet), with cache
			SUL<String, Word<String>> eqSulSim = new MealySimulatorSUL<>(mealyMachine, Utils.OMEGA_SYMBOL);
			StatisticSUL<String, Word<String>> eq_sym = new SymbolCounterSUL<>("EQ", eqSulSim);
			StatisticSUL<String, Word<String>> eq_rst = new ResetCounterSUL<>("EQ", eq_sym);
			SUL<String, Word<String>> eq_sul = eq_rst;
			cachedEqOracle = new AdaptiveCachedMembershipOracle<>(new SULOracle<>(eq_sul));

			EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle = buildEqOracle(
					rnd_seed, line, mealyMachine, cachedEqOracle, eq_sul);
			Experiment.MealyExperiment<String, Word<String>> experiment = new Experiment.MealyExperiment<String, Word<String>>(
					learner, eqOracle, productAlphabet);

			// Run the product with the current adaptive seed (or scratch for the first product).
			// Use the learner's internal adapted hypothesis/tree, not the previous product's raw hypothesis.
			experiment.run(false, null, (i == 0 ? null : tree_round2));

			// Collect the tree/hypothesis produced by the completed product learning.
			tree_round2 = experiment.getDiscrtree();
			if (tree_round2 == null) {
				tree_round2 = learner.getDiscriminationTree();
				System.out.println("WARNING: Got tree from learner instead of experiment");
			}

			// Second pass: learn the current product again from scratch to produce the round-7 seed for the next product.
			GrowingAlphabet<String> round7Alphabet = new GrowingMapAlphabet<>(productAlphabet);
			MembershipOracle<String, Word<Word<String>>> round7MqOracle = new SULOracle<String, Word<String>>(mq_sul);
			IKearnsVaziraniMealyBuilder<Object, String, Word<String>> round7Builder = new IKearnsVaziraniMealyBuilder<>();
			round7Builder.setOracle(round7MqOracle);
			round7Builder.setAlphabet(combinedAlphabet);
			IKearnsVaziraniMealy<String, Word<String>> round7Learner = (IKearnsVaziraniMealy<String, Word<String>>) round7Builder
					.withAlphabet(round7Alphabet)
					.create(null, null);

			Experiment.MealyExperiment<String, Word<String>> round7Experiment = new Experiment.MealyExperiment<String, Word<String>>(
					round7Learner, eqOracle, productAlphabet);
			round7Experiment.run(true, null, null);

			tree_round2 = round7Experiment.getDiscrtree();
			if (tree_round2 == null) {
				tree_round2 = round7Learner.getDiscriminationTree();
				System.out.println("WARNING: Got tree from round-7 experiment instead of experiment");
			}

			GrowingAlphabet<String> nextProductAlphabet = round7Alphabet;
			CompactMealy<String, Word<String>> nextProductHypothesis = (CompactMealy<String, Word<String>>) round7Experiment
					.getFinalHypothesis();
			if (nextProductHypothesis == null) {
				throw new IllegalStateException("Round-7 experiment failed to return a hypothesis for " + productFile.getName());
			}

			previousProductAlphabet = nextProductAlphabet;
			previousHypothesis = nextProductHypothesis;
			System.out.println("✓ Saved " + productFile.getName() + " round-7 tree and hypothesis for the next product");

			StatisticSUL<String, Word<String>> currentMqRst = (mq_rst_adaptive != null) ? mq_rst_adaptive : mq_rst;
			StatisticSUL<String, Word<String>> currentMqSym = (mq_sym_adaptive != null) ? mq_sym_adaptive : mq_sym;

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
			
			// MQ cache statistics (adaptive products only)
			if (i > 0 && cachedOracle != null) {
				metrics.cacheHits = cachedOracle.getCacheHits();
				metrics.cacheMisses = cachedOracle.getCacheMisses();
				metrics.cacheSize = cachedOracle.getCacheSize();
				int totalMqCache = metrics.cacheHits + metrics.cacheMisses;
				metrics.cacheHitRate = (totalMqCache > 0) ? (metrics.cacheHits * 100.0 / totalMqCache) : 0.0;
			} else {
				metrics.cacheHits = 0;
				metrics.cacheMisses = 0;
				metrics.cacheSize = 0;
				metrics.cacheHitRate = 0.0;
			}
			// EQ cache statistics (all products)
			if (cachedEqOracle != null) {
				metrics.eqCacheHits = cachedEqOracle.getCacheHits();
				metrics.eqCacheMisses = cachedEqOracle.getCacheMisses();
				metrics.eqCacheSize = cachedEqOracle.getCacheSize();
				int totalEqCache = metrics.eqCacheHits + metrics.eqCacheMisses;
				metrics.eqCacheHitRate = (totalEqCache > 0) ? (metrics.eqCacheHits * 100.0 / totalEqCache) : 0.0;
			} else {
				metrics.eqCacheHits = 0;
				metrics.eqCacheMisses = 0;
				metrics.eqCacheSize = 0;
				metrics.eqCacheHitRate = 0.0;
			}

			CompactMealy<String, Word<String>> finalHyp = (CompactMealy<String, Word<String>>) experiment.getFinalHypothesis();
			checkEquivalence(productFile.getName(), mealyMachine, finalHyp, productAlphabet, metrics);

			allProductMetrics.add(metrics);

			System.out.println("\n========== PRODUCT " + (i + 1) + " COMPLETED ==========");
			System.out.println("Rounds: " + metrics.rounds);
			System.out.println("MQ Resets: " + metrics.mqResets + ", Symbols: " + metrics.mqSymbols);
			System.out.println("EQ Resets: " + metrics.eqResets + ", Symbols: " + metrics.eqSymbols);
			System.out.println("States: " + metrics.states + " (original: " + metrics.originalStates + ")");
			System.out.println("Equivalent: " + (metrics.equivalent ? "Yes" : "No"));
			System.out.println("Alphabet: " + metrics.alphabetSize + " symbols");
			if (i > 0) {
				System.out.println("✓ Tree reused from the previous ordered product");
				if (cachedOracle != null) {
					cachedOracle.printStatistics("MQ CACHE");
					System.out.println("MQ cache: saved " + metrics.cacheHits + " queries ("
							+ String.format("%.1f%%", metrics.cacheHitRate) + " hit rate)");
				}
			}
			if (cachedEqOracle != null) {
				cachedEqOracle.printStatistics("EQ CACHE");
				System.out.println("EQ cache: saved " + metrics.eqCacheHits + " queries ("
						+ String.format("%.1f%%", metrics.eqCacheHitRate) + " hit rate)");
			}
			System.out.println("EQ alphabet: " + productAlphabet.size() + " symbols (product-only)");
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
		System.out.println("  • First product in the D' order: Learned from scratch");
		System.out.println("  • Remaining products: Each reused the previous ordered product's tree");
		System.out.println("  • First ordered product was re-learned " + (orderedProducts.size() - 1) + " times");
		System.out.println("  • MQ cache enabled for adaptive products (2-N)");
		System.out.println("  • EQ cache + product-only alphabet for all products");
		
		// Calculate total cache benefits
		int totalMqCacheHits = 0;
		int totalMqCacheMisses = 0;
		int totalEqCacheHits = 0;
		int totalEqCacheMisses = 0;
		for (ProductMetrics m : allProductMetrics) {
			if (m.isAdaptive) {
				totalMqCacheHits += m.cacheHits;
				totalMqCacheMisses += m.cacheMisses;
			}
			totalEqCacheHits += m.eqCacheHits;
			totalEqCacheMisses += m.eqCacheMisses;
		}
		
		if (totalMqCacheHits + totalMqCacheMisses > 0) {
			double mqHitRate = (totalMqCacheHits * 100.0) / (totalMqCacheHits + totalMqCacheMisses);
			System.out.println("\nMQ Cache Summary (adaptive products):");
			System.out.println("  • Hits: " + totalMqCacheHits + ", hit rate: " + String.format("%.2f%%", mqHitRate));
		}
		if (totalEqCacheHits + totalEqCacheMisses > 0) {
			double eqHitRate = (totalEqCacheHits * 100.0) / (totalEqCacheHits + totalEqCacheMisses);
			System.out.println("\nEQ Cache Summary (all products):");
			System.out.println("  • Hits: " + totalEqCacheHits + ", hit rate: " + String.format("%.2f%%", eqHitRate));
		}
		
		System.out.println("\nAll products learned successfully with tree reuse and caching!");
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
		options.addOption(EQWP, true,
				"WpMethod lookahead depth when -eq is omitted (default 2). Tune EQ symbol/reset counts; values < 2 may be incomplete for some Mealy machines.");
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

