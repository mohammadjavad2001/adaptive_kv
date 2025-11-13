import br.usp.icmc.labes.mealyInference.utils.LearnLibProperties;
import br.usp.icmc.labes.mealyInference.utils.Utils;
import de.learnlib.algorithms.kv.IKearnsVaziraniMealy;
import de.learnlib.algorithms.kv.IKearnsVaziraniMealyBuilder;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealyBuilder;
import de.learnlib.api.SUL;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.statistic.StatisticSUL;
import de.learnlib.algorithms.kv.Experiment;
import de.learnlib.algorithms.kv.Experiment1;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import de.learnlib.algorithms.kv.StateInfo;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import de.learnlib.driver.util.MealySimulatorSUL;
import de.learnlib.filter.statistic.sul.ResetCounterSUL;
import de.learnlib.filter.statistic.sul.SymbolCounterSUL;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.Pair;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.visualization.VisualizationHelper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.GrowingMapAlphabet;
import org.apache.commons.cli.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Comprehensive comparison between Adaptive Learning and Normal Learning for all Mine pump products.
 * This class learns all 15 Mine pump products using:
 * 1. Adaptive Learning: Uses product 1's tree from round 4 for subsequent products
 * 2. Normal Learning: Learns each product from scratch
 * 
 * Results are saved to an Excel file for easy comparison.
 */
public class CompareAdaptiveVsNormal {

    // Statistics storage for both approaches
    private static class ProductStats {
        String productName;
        int rounds;
        long mqResets;
        long mqSymbols;
        long eqResets;
        long eqSymbols;
        int finalStates;
        int alphabetSize;
        long totalTime; // milliseconds
        
        public ProductStats(String productName) {
            this.productName = productName;
        }
    }

    // Adaptive learning state
    private static MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> sharedTree = null;
    private static GrowingAlphabet<String> sharedAlphabet = null;
    
    // Strategy: Re-learn Product 1 before each product 2-15 to get fresh tree/hypothesis
    // This solves the Java reference problem - each product gets identical baseline
    private static MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> product1TreeRound4 = null;
    private static GrowingAlphabet<String> product1AlphabetRound4 = null;
    
    private static CompactMealy<String, Word<String>> previousHypothesis = null;
    private static ArrayList<String> allInputAlphabets = new ArrayList<>();
    
    // Configuration
    private static final String PRODUCTS_DIR = ".\\alternative_experiments\\Minepump_SPL\\products_3wise";
    private static final int TOTAL_PRODUCTS = 15;
    private static final int TREE_CAPTURE_ROUND = 4; // Capture tree at round 4 of product 1
    
    public static final Word<String> OMEGA_SYMBOL = Word.fromLetter("Ω");

    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║  COMPREHENSIVE COMPARISON: ADAPTIVE vs NORMAL LEARNING           ║");
        System.out.println("║  Mine Pump Software Product Line - All Products                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝\n");

        // Storage for results
        List<ProductStats> adaptiveResults = new ArrayList<>();
        List<ProductStats> normalResults = new ArrayList<>();

        // Phase 1: Adaptive Learning
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PHASE 1: ADAPTIVE LEARNING (with tree reuse from product 1, round 4)");
        System.out.println("=".repeat(70) + "\n");
        adaptiveResults = learnAllProductsAdaptive();

        // Phase 2: Normal Learning
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PHASE 2: NORMAL LEARNING (each product from scratch)");
        System.out.println("=".repeat(70) + "\n");
        normalResults = learnAllProductsNormal();

        // Phase 3: Generate Excel Report
        System.out.println("\n" + "=".repeat(70));
        System.out.println("PHASE 3: GENERATING EXCEL REPORT");
        System.out.println("=".repeat(70) + "\n");
        generateExcelReport(adaptiveResults, normalResults);

        // Phase 4: Print Summary
        printComparisonSummary(adaptiveResults, normalResults);

        System.out.println("\n✓ All learning completed successfully!");
        System.out.println("✓ Results saved to: learning_comparison_results.xlsx");
    }

    /**
     * Learn all products using adaptive learning (tree reuse from product 1, round 4)
     * Strategy: For each target product, run a 2-product loop exactly like hi_single.java:
     *   - i=0: Learn Product 1 from scratch
     *   - i=1: Learn target product using Product 1's tree
     */
    private static List<ProductStats> learnAllProductsAdaptive() throws Exception {
        List<ProductStats> results = new ArrayList<>();
        File product1File = new File(PRODUCTS_DIR, "00001_fsm.dot");
        
        // First, learn Product 1 standalone
        System.out.println("\n" + "─".repeat(70));
        System.out.println("ADAPTIVE LEARNING - Product 1: 00001_fsm.dot");
        System.out.println("─".repeat(70));
        
        ProductStats product1Stats = learnTwoProductsLikeHiSingle(product1File, product1File, true);
        results.add(product1Stats);
        
        System.out.println("✓ Product 1 completed (Adaptive - Baseline)");
        printProductStats(product1Stats);

        // For products 2-15: Run 2-product loop (Product 1 -> Target Product)
        for (int i = 1; i < TOTAL_PRODUCTS; i++) {
            String productFileName = String.format("%05d_fsm.dot", i + 1);
            File targetProductFile = new File(PRODUCTS_DIR, productFileName);
            
            System.out.println("\n" + "═".repeat(70));
            System.out.println("PREPARING FOR PRODUCT " + (i + 1) + " - Learning Product 1 + Product " + (i + 1));
            System.out.println("═".repeat(70));
            
            // Run 2-product loop exactly like hi_single.java
            ProductStats stats = learnTwoProductsLikeHiSingle(product1File, targetProductFile, false);
            results.add(stats);
            
            System.out.println("✓ Product " + (i + 1) + " completed (Adaptive)");
            printProductStats(stats);
        }
        
        return results;
    }
    
    /**
     * Learn two products exactly like hi_single.java's main loop
     * Simulates: for(int i=0; i<2; i++) where i=0 is product1, i=1 is product2
     */
    private static ProductStats learnTwoProductsLikeHiSingle(File product1File, File product2File, boolean isStandaloneProduct1) throws Exception {
        // Reset state for fresh 2-product learning session
        sharedTree = null;
        product1TreeRound4 = null;
        product1AlphabetRound4 = null;
        previousHypothesis = null;
        allInputAlphabets.clear();
        
        // Learn Product 1 (i=0)
        System.out.println("  → Learning Product 1 (i=0)...");
        ProductStats unusedStats = learnProductAdaptive(product1File, 0);
        
        // Learn Product 2 (i=1) or return Product 1 stats if standalone
        if (isStandaloneProduct1) {
            return unusedStats;
        } else {
            System.out.println("  → Learning target product (i=1) using Product 1's tree...");
            return learnProductAdaptive(product2File, 1);
        }
    }

    /**
     * Learn all products using normal learning (from scratch)
     */
    private static List<ProductStats> learnAllProductsNormal() throws Exception {
        List<ProductStats> results = new ArrayList<>();

        for (int i = 0; i < TOTAL_PRODUCTS; i++) {
            String productFileName = String.format("%05d_fsm.dot", i + 1);
            File productFile = new File(PRODUCTS_DIR, productFileName);
            
            System.out.println("\n" + "─".repeat(70));
            System.out.println("NORMAL LEARNING - Product " + (i + 1) + ": " + productFileName);
            System.out.println("─".repeat(70));
            
            ProductStats stats = learnProductNormal(productFile);
            results.add(stats);
            
            System.out.println("✓ Product " + (i + 1) + " completed (Normal)");
            printProductStats(stats);
        }
        
        return results;
    }

    /**
     * Learn a single product using adaptive learning
     */
    private static ProductStats learnProductAdaptive(File productFile, int productIndex) throws Exception {
        long startTime = System.currentTimeMillis();
        ProductStats stats = new ProductStats(productFile.getName());

        // Load the Mealy machine
        CompactMealy<String, Word<String>> mealyMachine = loadMealyMachineFromDot(productFile);
        Alphabet<String> productAlphabet = mealyMachine.getInputAlphabet();
        stats.alphabetSize = productAlphabet.size();

        // Setup SUL and oracles
        CommandLine line = createDummyCommandLine();
        Random rndSeed = new Random(System.currentTimeMillis());

        // Update combined alphabet
        if (productIndex == 0) {
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

        // Create learner
        IKearnsVaziraniMealy<String, Word<String>> learner;
        StatisticSUL<String, Word<String>> mqRst;
        StatisticSUL<String, Word<String>> mqSym;

        if (productIndex == 0) {
            // Product 1: Learn from scratch
            sharedAlphabet = new GrowingMapAlphabet<>(combinedAlphabet);
            
            SUL<String, Word<String>> sulSim = new MealySimulatorSUL<>(mealyMachine, Utils.OMEGA_SYMBOL);
            mqSym = new SymbolCounterSUL<>("MQ", sulSim);
            mqRst = new ResetCounterSUL<>("MQ", mqSym);
            MembershipOracle<String, Word<Word<String>>> mqOracle = new SULOracle<>(mqRst);

            IKearnsVaziraniMealyBuilder<Object, String, Word<String>> builder = new IKearnsVaziraniMealyBuilder<>();
            builder.setOracle(mqOracle);
            builder.setAlphabet(combinedAlphabet);
            learner = (IKearnsVaziraniMealy<String, Word<String>>) builder.withAlphabet(sharedAlphabet).create(null, null);
            
            System.out.println("  Learning Product 1 from scratch");
        } else {
            // Products 2-15: Use fresh tree from Product 1 (round 4)
            System.out.println("  Product (i=" + productIndex + "): Adaptive learning with Product 1 tree");
            
            if (product1TreeRound4 == null) {
                throw new IllegalStateException("Product 1's tree is null! Cannot perform adaptive learning.");
            }
            
            System.out.println("    → Using fresh tree from Product 1 (round 4)");
            sharedTree = product1TreeRound4;
            sharedAlphabet = product1AlphabetRound4;
            
            // Extend alphabet from Product 1's alphabet
            GrowingAlphabet<String> extendedAlphabet = new GrowingMapAlphabet<>(sharedAlphabet);
            for (String symbol : productAlphabet) {
                if (!extendedAlphabet.containsSymbol(symbol)) {
                    extendedAlphabet.addSymbol(symbol);
                }
            }

            // Create MQ mealy with extended alphabet
            CompactMealy<String, Word<String>> mqMealy = new CompactMealy<>(extendedAlphabet);
            Map<Integer, Integer> stateMap = new HashMap<>();
            for (Integer state : mealyMachine.getStates()) {
                stateMap.put(state, mqMealy.addState());
            }
            mqMealy.setInitialState(stateMap.get(mealyMachine.getInitialState()));

            // Copy transitions for current product symbols
            for (Integer state : mealyMachine.getStates()) {
                for (String input : productAlphabet) {
                    Integer succ = mealyMachine.getSuccessor(state, input);
                    Word<String> output = mealyMachine.getOutput(state, input);
                    if (succ != null) {
                        int symbolIdx = extendedAlphabet.getSymbolIndex(input);
                        String canonicalSymbol = extendedAlphabet.getSymbol(symbolIdx);
                        mqMealy.addTransition(stateMap.get(state), canonicalSymbol, stateMap.get(succ), output);
                    }
                }
            }

            // Add OMEGA self-loops for other symbols
            for (String symbol : extendedAlphabet) {
                if (!productAlphabet.containsSymbol(symbol)) {
                    for (Integer state : mqMealy.getStates()) {
                        mqMealy.addTransition(state, symbol, state, Utils.OMEGA_SYMBOL);
                    }
                }
            }

            SUL<String, Word<String>> mqSulSim = new MealySimulatorSUL<>(mqMealy, Utils.OMEGA_SYMBOL);
            mqSym = new SymbolCounterSUL<>("MQ", mqSulSim);
            mqRst = new ResetCounterSUL<>("MQ", mqSym);
            MembershipOracle<String, Word<Word<String>>> mqOracle = new SULOracle<>(mqRst);

            // Adapt previous hypothesis to new alphabet
            CompactMealy<String, Word<String>> adaptedHypothesis = new CompactMealy<>(extendedAlphabet);
            Map<Integer, Integer> stateMap2 = new HashMap<>();
            for (Integer oldState : previousHypothesis.getStates()) {
                stateMap2.put(oldState, adaptedHypothesis.addState());
            }
            adaptedHypothesis.setInitialState(stateMap2.get(previousHypothesis.getInitialState()));

            for (Integer oldState : previousHypothesis.getStates()) {
                Integer newState = stateMap2.get(oldState);
                for (String symbol : sharedAlphabet) {
                    Integer oldSucc = previousHypothesis.getSuccessor(oldState, symbol);
                    Word<String> output = previousHypothesis.getOutput(oldState, symbol);
                    if (oldSucc != null) {
                        adaptedHypothesis.addTransition(newState, symbol, stateMap2.get(oldSucc), output);
                    }
                }
            }

            for (String symbol : extendedAlphabet) {
                if (!sharedAlphabet.containsSymbol(symbol)) {
                    for (Integer newState : adaptedHypothesis.getStates()) {
                        adaptedHypothesis.addTransition(newState, symbol, newState, Utils.OMEGA_SYMBOL);
                    }
                }
            }

            // Canonicalize discriminators to match the extended alphabet
            canonicalizeTreeDiscriminators(sharedTree.getRoot(), extendedAlphabet);

            IKearnsVaziraniMealyBuilder<Object, String, Word<String>> builder = new IKearnsVaziraniMealyBuilder<>();
            builder.setOracle(mqOracle);
            builder.setAlphabet(combinedAlphabet);
            
            // Use the shared tree (will be modified during learning)
            learner = (IKearnsVaziraniMealy<String, Word<String>>) builder.withAlphabet(extendedAlphabet).create(sharedTree, adaptedHypothesis);
            
            System.out.println("    ✓ Tree loaded (nodes: " + countTreeNodes(sharedTree.getRoot()) + ")");
        }

        // Setup EQ oracle
        CompactMealy<String, Word<String>> updatedMealy;
        if (productIndex > 0) {
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
        StatisticSUL<String, Word<String>> eqSym = new SymbolCounterSUL<>("EQ", eqSulSim);
        StatisticSUL<String, Word<String>> eqRst = new ResetCounterSUL<>("EQ", eqSym);
        EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle = 
            buildEqOracle(rndSeed, line, updatedMealy, eqRst);

        // Run experiment
        Experiment.MealyExperiment<String, Word<String>> experiment = 
            new Experiment.MealyExperiment<>(learner, eqOracle, learner.get_alphabet_symbol());

        if (productIndex == 0) {
            experiment.run(true, null); // Capture tree at round 4
        } else {
            experiment.run(false, null);
        }

        // Collect statistics
        stats.rounds = (int) experiment.getRounds().getCount();
        stats.mqResets = extractValue(mqRst.getStatisticalData().getSummary());
        stats.mqSymbols = extractValue(mqSym.getStatisticalData().getSummary());
        stats.eqResets = extractValue(eqRst.getStatisticalData().getSummary());
        stats.eqSymbols = extractValue(eqSym.getStatisticalData().getSummary());
        stats.finalStates = experiment.getFinalHypothesis().getStates().size();
        stats.totalTime = System.currentTimeMillis() - startTime;

        // Save tree and hypothesis for next product
        MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> currentTree = learner.getDiscriminationTree();
        
        if (productIndex == 0) {
            // Product 1: Save tree from round 4 for Product 2
            MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> capturedTree = experiment.getDiscrtree();
            product1TreeRound4 = (capturedTree != null) ? capturedTree : currentTree;
            product1AlphabetRound4 = (GrowingAlphabet<String>) learner.get_alphabet_symbol();
            
            System.out.println("  ✓ Tree from Product 1 (round 4) saved");
            System.out.println("  ✓ Tree has " + countTreeNodes(product1TreeRound4.getRoot()) + " nodes");
        }
        // Note: For products 2-15, we don't save the tree because we'll re-learn Product 1 next time
        
        // Always update the hypothesis
        previousHypothesis = (CompactMealy<String, Word<String>>) experiment.getFinalHypothesis();

        return stats;
    }

    /**
     * Learn a single product using normal learning (from scratch)
     */
    private static ProductStats learnProductNormal(File productFile) throws Exception {
        long startTime = System.currentTimeMillis();
        ProductStats stats = new ProductStats(productFile.getName());

        // Load the Mealy machine
        CompactMealy<String, Word<String>> mealyMachine = loadMealyMachineFromDot(productFile);
        Alphabet<String> productAlphabet = mealyMachine.getInputAlphabet();
        stats.alphabetSize = productAlphabet.size();

        // Setup SUL and oracles
        CommandLine line = createDummyCommandLine();
        Random rndSeed = new Random(System.currentTimeMillis());

        SUL<String, Word<String>> sulSim = new MealySimulatorSUL<>(mealyMachine, Utils.OMEGA_SYMBOL);
        StatisticSUL<String, Word<String>> mqSym = new SymbolCounterSUL<>("MQ", sulSim);
        StatisticSUL<String, Word<String>> mqRst = new ResetCounterSUL<>("MQ", mqSym);
        MembershipOracle<String, Word<Word<String>>> mqOracle = new SULOracle<>(mqRst);

        // Create learner (normal KV algorithm)
        KearnsVaziraniMealyBuilder<String, Word<String>> builder = new KearnsVaziraniMealyBuilder<>();
        builder.setAlphabet(productAlphabet);
        builder.setOracle(mqOracle);
        KearnsVaziraniMealy<String, Word<String>> learner = builder.create();

        // Setup EQ oracle
        StatisticSUL<String, Word<String>> eqSym = new SymbolCounterSUL<>("EQ", sulSim);
        StatisticSUL<String, Word<String>> eqRst = new ResetCounterSUL<>("EQ", eqSym);
        EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> eqOracle = 
            buildEqOracle(rndSeed, line, mealyMachine, eqRst);

        // Run experiment
        Experiment1.MealyExperiment<String, Word<String>> experiment = 
            new Experiment1.MealyExperiment<>(learner, eqOracle, productAlphabet);
        experiment.run();

        // Collect statistics
        stats.rounds = (int) experiment.getRounds().getCount();
        stats.mqResets = extractValue(mqRst.getStatisticalData().getSummary());
        stats.mqSymbols = extractValue(mqSym.getStatisticalData().getSummary());
        stats.eqResets = extractValue(eqRst.getStatisticalData().getSummary());
        stats.eqSymbols = extractValue(eqSym.getStatisticalData().getSummary());
        stats.finalStates = experiment.getFinalHypothesis().getStates().size();
        stats.totalTime = System.currentTimeMillis() - startTime;

        return stats;
    }

    /**
     * Generate Excel report comparing adaptive vs normal learning
     */
    private static void generateExcelReport(List<ProductStats> adaptiveResults, List<ProductStats> normalResults) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        
        // Sheet 1: Detailed Comparison
        Sheet detailSheet = workbook.createSheet("Detailed Comparison");
        createDetailedComparisonSheet(detailSheet, adaptiveResults, normalResults);
        
        // Sheet 2: Summary Statistics
        Sheet summarySheet = workbook.createSheet("Summary");
        createSummarySheet(summarySheet, adaptiveResults, normalResults);
        
        // Sheet 3: Improvement Percentages
        Sheet improvementSheet = workbook.createSheet("Improvement Analysis");
        createImprovementSheet(improvementSheet, adaptiveResults, normalResults);

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream("learning_comparison_results.xlsx")) {
            workbook.write(fileOut);
        }
        workbook.close();
        
        System.out.println("✓ Excel report generated: learning_comparison_results.xlsx");
    }

    private static void createDetailedComparisonSheet(Sheet sheet, List<ProductStats> adaptive, List<ProductStats> normal) {
        // Create header style
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Product", "Approach", "Rounds", "MQ Resets", "MQ Symbols", 
                           "EQ Resets", "EQ Symbols", "States", "Alphabet Size", "Time (ms)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Fill data
        int rowNum = 1;
        for (int i = 0; i < adaptive.size(); i++) {
            // Adaptive row
            Row adaptiveRow = sheet.createRow(rowNum++);
            fillDataRow(adaptiveRow, adaptive.get(i), i + 1, "Adaptive");
            
            // Normal row
            Row normalRow = sheet.createRow(rowNum++);
            fillDataRow(normalRow, normal.get(i), i + 1, "Normal");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void fillDataRow(Row row, ProductStats stats, int productNum, String approach) {
        row.createCell(0).setCellValue(productNum);
        row.createCell(1).setCellValue(approach);
        row.createCell(2).setCellValue(stats.rounds);
        row.createCell(3).setCellValue(stats.mqResets);
        row.createCell(4).setCellValue(stats.mqSymbols);
        row.createCell(5).setCellValue(stats.eqResets);
        row.createCell(6).setCellValue(stats.eqSymbols);
        row.createCell(7).setCellValue(stats.finalStates);
        row.createCell(8).setCellValue(stats.alphabetSize);
        row.createCell(9).setCellValue(stats.totalTime);
    }

    private static void createSummarySheet(Sheet sheet, List<ProductStats> adaptive, List<ProductStats> normal) {
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Headers
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Metric");
        headerRow.createCell(1).setCellValue("Adaptive Total");
        headerRow.createCell(2).setCellValue("Normal Total");
        headerRow.createCell(3).setCellValue("Difference");
        headerRow.createCell(4).setCellValue("Improvement %");
        
        for (int i = 0; i < 5; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }

        // Calculate totals
        long[] adaptiveTotals = calculateTotals(adaptive);
        long[] normalTotals = calculateTotals(normal);

        String[] metrics = {"Rounds", "MQ Resets", "MQ Symbols", "EQ Resets", "EQ Symbols", "Total Time (ms)"};
        for (int i = 0; i < metrics.length; i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(metrics[i]);
            row.createCell(1).setCellValue(adaptiveTotals[i]);
            row.createCell(2).setCellValue(normalTotals[i]);
            row.createCell(3).setCellValue(normalTotals[i] - adaptiveTotals[i]);
            
            double improvement = 100.0 * (normalTotals[i] - adaptiveTotals[i]) / (double) normalTotals[i];
            row.createCell(4).setCellValue(String.format("%.2f%%", improvement));
        }

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void createImprovementSheet(Sheet sheet, List<ProductStats> adaptive, List<ProductStats> normal) {
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Product", "Rounds Imp%", "MQ Resets Imp%", "MQ Symbols Imp%", 
                           "EQ Resets Imp%", "EQ Symbols Imp%", "Time Imp%"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (int i = 0; i < adaptive.size(); i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(i + 1);
            row.createCell(1).setCellValue(calculateImprovement(normal.get(i).rounds, adaptive.get(i).rounds));
            row.createCell(2).setCellValue(calculateImprovement(normal.get(i).mqResets, adaptive.get(i).mqResets));
            row.createCell(3).setCellValue(calculateImprovement(normal.get(i).mqSymbols, adaptive.get(i).mqSymbols));
            row.createCell(4).setCellValue(calculateImprovement(normal.get(i).eqResets, adaptive.get(i).eqResets));
            row.createCell(5).setCellValue(calculateImprovement(normal.get(i).eqSymbols, adaptive.get(i).eqSymbols));
            row.createCell(6).setCellValue(calculateImprovement(normal.get(i).totalTime, adaptive.get(i).totalTime));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static long[] calculateTotals(List<ProductStats> stats) {
        long[] totals = new long[6];
        for (ProductStats s : stats) {
            totals[0] += s.rounds;
            totals[1] += s.mqResets;
            totals[2] += s.mqSymbols;
            totals[3] += s.eqResets;
            totals[4] += s.eqSymbols;
            totals[5] += s.totalTime;
        }
        return totals;
    }

    private static String calculateImprovement(long normal, long adaptive) {
        if (normal == 0) return "N/A";
        double improvement = 100.0 * (normal - adaptive) / (double) normal;
        return String.format("%.2f%%", improvement);
    }

    private static void printComparisonSummary(List<ProductStats> adaptive, List<ProductStats> normal) {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("FINAL COMPARISON SUMMARY");
        System.out.println("═".repeat(70));

        long[] adaptiveTotals = calculateTotals(adaptive);
        long[] normalTotals = calculateTotals(normal);

        System.out.println("\nTOTAL METRICS ACROSS ALL 15 PRODUCTS:");
        System.out.println("─".repeat(70));
        System.out.printf("%-20s %15s %15s %15s%n", "Metric", "Adaptive", "Normal", "Improvement");
        System.out.println("─".repeat(70));
        
        String[] metrics = {"Rounds", "MQ Resets", "MQ Symbols", "EQ Resets", "EQ Symbols", "Time (ms)"};
        for (int i = 0; i < metrics.length; i++) {
            String improvement = calculateImprovement(normalTotals[i], adaptiveTotals[i]);
            System.out.printf("%-20s %15d %15d %15s%n", metrics[i], adaptiveTotals[i], normalTotals[i], improvement);
        }
        System.out.println("═".repeat(70));
    }

    // ==================== HELPER METHODS ====================

    /**
     * Print all measurements for a learned product in a formatted way
     */
    private static void printProductStats(ProductStats stats) {
        System.out.println("  ┌─ Measurements:");
        System.out.println("  ├─ Rounds (EQ queries):     " + stats.rounds);
        System.out.println("  ├─ MQ Resets:               " + stats.mqResets);
        System.out.println("  ├─ MQ Symbols:              " + stats.mqSymbols);
        System.out.println("  ├─ EQ Resets:               " + stats.eqResets);
        System.out.println("  ├─ EQ Symbols:              " + stats.eqSymbols);
        System.out.println("  ├─ Final States:            " + stats.finalStates);
        System.out.println("  ├─ Alphabet Size:           " + stats.alphabetSize);
        System.out.println("  └─ Time (ms):               " + stats.totalTime + " ms (" + (stats.totalTime / 1000.0) + " seconds)");
    }

    /**
     * Count the total number of nodes in the discrimination tree (for verification)
     */
    private static int countTreeNodes(AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> node) {
        if (node == null) return 0;
        
        if (node.isLeaf()) {
            return 1;
        }
        
        int count = 1; // Count this internal node
        Collection<Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>>> children = node.getChildEntries();
        for (Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>> entry : children) {
            count += countTreeNodes(entry.getValue());
        }
        return count;
    }

    private static CompactMealy<String, Word<String>> loadMealyMachineFromDot(File f) throws Exception {
        Pattern kissLine = Pattern.compile("\\s*([a-zA-Z0-9]+)\\s+->\\s+([a-zA-Z0-9]+)\\s*\\[label=[\"<](.+)[\">]\\];?");
        BufferedReader br = new BufferedReader(new FileReader(f));
        
        List<String[]> trs = new ArrayList<>();
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
        CompactMealy<String, Word<String>> mealym = new CompactMealy<>(alphabet);

        Map<String, Integer> states = new HashMap<>();
        Map<String, Word<String>> words = new HashMap<>();
        WordBuilder<String> aux = new WordBuilder<>();

        aux.clear();
        aux.append(OMEGA_SYMBOL);
        words.put(OMEGA_SYMBOL.toString(), aux.toWord());

        for (String[] tr : trs) {
            if (!states.containsKey(tr[0])) states.put(tr[0], mealym.addState());
            if (!states.containsKey(tr[3])) states.put(tr[3], mealym.addState());

            Integer si = states.get(tr[0]);
            Integer sf = states.get(tr[3]);

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
                Word<String> newAccessSeq = Word.fromList(Arrays.asList(canonicalSymbols));
                try {
                    java.lang.reflect.Field accessSeqField = StateInfo.class.getDeclaredField("accessSequence");
                    accessSeqField.setAccessible(true);
                    accessSeqField.set(stateInfo, newAccessSeq);
                } catch (Exception e) {
                    // Silently continue
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
            Word<String> newDiscriminator = Word.fromList(Arrays.asList(canonicalSymbols));
            try {
                java.lang.reflect.Field discriminatorField = node.getClass().getDeclaredField("discriminator");
                discriminatorField.setAccessible(true);
                discriminatorField.set(node, newDiscriminator);
            } catch (Exception e) {
                // Silently continue
            }
        }
        
        Collection<Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>>> children = node.getChildEntries();
        for (Map.Entry<Word<Word<String>>, AbstractWordBasedDTNode<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>>> entry : children) {
            canonicalizeTreeDiscriminators(entry.getValue(), newAlphabet);
        }
    }

    private static EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<Word<String>>> buildEqOracle(
            Random rndSeed, CommandLine line, CompactMealy<String, Word<String>> mealy,
            SUL<String, Word<String>> eqSul) {
        MembershipOracle<String, Word<Word<String>>> oracleForEQoracle = new SULOracle<>(eqSul);
        if (!line.hasOption("eq")) {
            return new WpMethodEQOracle<>(oracleForEQoracle, 2);
        }
        LearnLibProperties learnProps = LearnLibProperties.getInstance();
        double restartProbability = learnProps.getRndWalk_restartProbability();
        int maxSteps = learnProps.getRndWalk_maxSteps();
        boolean resetStepCount = learnProps.getRndWalk_resetStepsCount();
        return new RandomWalkEQOracle<>(eqSul, restartProbability, maxSteps, resetStepCount, rndSeed);
    }

    private static CommandLine createDummyCommandLine() throws ParseException {
        Options options = new Options();
        options.addOption("eq", false, "Equivalence oracle");
        CommandLineParser parser = new BasicParser();
        return parser.parse(options, new String[]{});
    }

    private static long extractValue(String statisticalSummary) {
        int j = statisticalSummary.lastIndexOf(" ");
        if (j >= 0) {
            String valueStr = statisticalSummary.substring(j + 1);
            return Long.parseLong(valueStr);
        }
        return 0;
    }
}

