import br.usp.icmc.labes.mealyInference.utils.LearnLibProperties;
import br.usp.icmc.labes.mealyInference.utils.Utils;
import de.learnlib.acex.AcexAnalyzer;
// import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.api.SUL;
import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.statistic.StatisticSUL;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import de.learnlib.datastructure.discriminationtree.iterators.DiscriminationTreeIterators;
import de.learnlib.ds.ResetCounterSUL;
import de.learnlib.ds.SymbolCounterSUL;
// import de.learnlib.oracle.equivalence.RandomWalkEQOracle;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;

import de.learnlib.oracle.equivalence.WMethodEQOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.util.Experiment;
import de.learnlib.util.statistics.SimpleProfiler;
import de.learnlib.incremental.KearnsVaziraniDFAState;
import de.learnlib.incremental.KearnsVaziraniDFA;
import de.learnlib.algorithms.kv.StateInfo;

import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.commons.util.Pair;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.equivalence.DeterministicEquivalenceTest;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import net.automatalib.words.impl.GrowingMapAlphabet;
import net.automatalib.words.GrowingAlphabet;

import org.apache.commons.cli.*;

import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adaptive DFA learning using IKearnsVaziraniDFA algorithm
 * Similar to hi_single.java but for DFA instead of Mealy machines
 */
public class hi_single_dfa<I, O> {

    // Store tree between learning rounds for adaptive learning
    static MultiDTree<String, Boolean, StateInfo<String, Boolean>> tree_round2 = null;
    
    // Collect all unique input symbols seen so far
    private static ArrayList<String> allInputAlphabets = new ArrayList<>();
    
    // Store Product 0's alphabet for adaptive learning
    private static Alphabet<String> product1Alphabet = null;
    
    // Store previous product's hypothesis
    private static CompactDFA<String> previousHypothesis = null;

    // Statistics arrays
    private static long[][] productStats = new long[10][6];
    private static int[] productStates = new int[10];
    private static int[] productAlphabetSizes = new int[10];
    private static int[] newSymbolsAdded = new int[10];

    // Constants
    public static final String EQ = "eq";
    public static final String HELP = "help";
    public static final String HELP_SHORT = "h";
    public static final String SEED = "seed";
    public static final String OUT = "out";
    public static final String DIR = "dir";
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * Extract numeric value from statistic string
     */
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

    /**
     * Traverse and print the discrimination tree structure
     */
    private static <I, O, D> void traverseAndPrintTree(
            AbstractWordBasedDTNode<I, O, D> node,
            String indent,
            boolean isLast) {

        System.out.print(indent);

        if (isLast) {
            System.out.print("└─ ");
            indent += "   ";
        } else {
            System.out.print("├─ ");
            indent += "│  ";
        }

        if (node.isLeaf()) {
            System.out.println("State: " + node.getData());
        } else {
            System.out.println("Test: " + node.getDiscriminator());

            Collection<Map.Entry<O, AbstractWordBasedDTNode<I, O, D>>> children = node.getChildEntries();
            int count = 0;
            int total = children.size();

            for (Map.Entry<O, AbstractWordBasedDTNode<I, O, D>> entry : children) {
                count++;

                System.out.print(indent);
                if (count == total) {
                    System.out.print("└─ ");
                } else {
                    System.out.print("├─ ");
                }
                System.out.println("Edge: " + entry.getKey());

                traverseAndPrintTree(entry.getValue(), indent + (count == total ? "   " : "│  "), count == total);
            }
        }
    }

    /**
     * Analyze discrimination tree structure in detail
     */
    private static void analyzeTreeStructureDetailed(
            MultiDTree<String, Boolean, StateInfo<String, Boolean>> tree,
            int productNumber,
            Alphabet<String> productAlphabet) {

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  DETAILED TREE ANALYSIS - PRODUCT " + productNumber + "                             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        if (tree == null) {
            System.out.println("⚠ Tree is null - no analysis possible");
            return;
        }

        AbstractWordBasedDTNode<String, Boolean, StateInfo<String, Boolean>> root = tree.getRoot();

        List<StateInfo<String, Boolean>> allStates = new ArrayList<>();
        List<Word<String>> allDiscriminators = new ArrayList<>();
        int treeDepth = collectTreeInfo(root, allStates, allDiscriminators, 0);

        System.out.println("TREE STATISTICS:");
        System.out.println("  Total states (leaf nodes): " + allStates.size());
        System.out.println("  Total discriminators (internal nodes): " + allDiscriminators.size());
        System.out.println("  Maximum tree depth: " + treeDepth);
        System.out.println("  Product alphabet size: " + productAlphabet.size());
        System.out.println();

        System.out.println("DISCRIMINATORS IN TREE:");
        for (int i = 0; i < allDiscriminators.size(); i++) {
            Word<String> disc = allDiscriminators.get(i);
            System.out.println("  [" + i + "] " + disc + " (length: " + disc.length() + ")");
        }
        System.out.println();

        System.out.println("STATE INFO OBJECTS:");
        for (StateInfo<String, Boolean> stateInfo : allStates) {
            System.out.println("  State " + stateInfo.id + ":");
            System.out.println("    Access Sequence: " + stateInfo.accessSequence);
            System.out.println("    Access Seq Length: " + stateInfo.accessSequence.length());

            boolean usesCurrentSymbols = false;
            for (String symbol : productAlphabet) {
                if (stateInfo.accessSequence.toString().contains(symbol)) {
                    usesCurrentSymbols = true;
                    break;
                }
            }
            System.out.println("    Uses current product symbols: " + usesCurrentSymbols);
        }
        System.out.println();

        System.out.println("═══════════════════════════════════════════════════════════════\n");
    }

    /**
     * Canonicalize tree discriminators to use new alphabet's symbol instances
     */
    private static void canonicalizeTreeDiscriminators(
            AbstractWordBasedDTNode<String, Boolean, StateInfo<String, Boolean>> node,
            Alphabet<String> newAlphabet) {
        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            StateInfo<String, Boolean> stateInfo = node.getData();
            if (stateInfo != null && stateInfo.accessSequence != null && stateInfo.accessSequence.length() > 0) {
                Word<String> oldAccessSeq = stateInfo.accessSequence;
                System.out.println("  Canonicalizing StateInfo access sequence for state " + stateInfo.id + ": " + oldAccessSeq);

                String[] canonicalSymbols = new String[oldAccessSeq.length()];
                for (int i = 0; i < oldAccessSeq.length(); i++) {
                    String oldSymbol = oldAccessSeq.getSymbol(i);
                    if (newAlphabet.containsSymbol(oldSymbol)) {
                        int symbolIdx = newAlphabet.getSymbolIndex(oldSymbol);
                        canonicalSymbols[i] = newAlphabet.getSymbol(symbolIdx);
                    } else {
                        System.err.println("    WARNING: Symbol '" + oldSymbol + "' from StateInfo not in new alphabet!");
                        canonicalSymbols[i] = oldSymbol;
                    }
                }

                Word<String> newAccessSeq = Word.fromList(Arrays.asList(canonicalSymbols));

                try {
                    java.lang.reflect.Field accessSeqField = StateInfo.class.getDeclaredField("accessSequence");
                    accessSeqField.setAccessible(true);
                    accessSeqField.set(stateInfo, newAccessSeq);
                    System.out.println("    ✓ StateInfo access sequence canonicalized");
                } catch (Exception e) {
                    System.err.println("    ERROR: Could not update StateInfo access sequence: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            return;
        }

        Word<String> oldDiscriminator = node.getDiscriminator();
        if (oldDiscriminator != null && oldDiscriminator.length() > 0) {
            System.out.println("  Canonicalizing discriminator: " + oldDiscriminator);

            String[] canonicalSymbols = new String[oldDiscriminator.length()];
            for (int i = 0; i < oldDiscriminator.length(); i++) {
                String oldSymbol = oldDiscriminator.getSymbol(i);
                if (newAlphabet.containsSymbol(oldSymbol)) {
                    int symbolIdx = newAlphabet.getSymbolIndex(oldSymbol);
                    canonicalSymbols[i] = newAlphabet.getSymbol(symbolIdx);
                } else {
                    System.err.println("      WARNING: Symbol '" + oldSymbol + "' from tree not in new alphabet!");
                    canonicalSymbols[i] = oldSymbol;
                }
            }

            Word<String> newDiscriminator = Word.fromList(Arrays.asList(canonicalSymbols));

            try {
                java.lang.reflect.Field discriminatorField = node.getClass().getDeclaredField("discriminator");
                discriminatorField.setAccessible(true);
                discriminatorField.set(node, newDiscriminator);
                System.out.println("    ✓ Discriminator canonicalized successfully");
            } catch (Exception e) {
                System.err.println("    ERROR: Could not update discriminator: " + e.getMessage());
                e.printStackTrace();
            }
        }

        Collection<Map.Entry<Boolean, AbstractWordBasedDTNode<String, Boolean, StateInfo<String, Boolean>>>> children = node.getChildEntries();
        for (Map.Entry<Boolean, AbstractWordBasedDTNode<String, Boolean, StateInfo<String, Boolean>>> entry : children) {
            canonicalizeTreeDiscriminators(entry.getValue(), newAlphabet);
        }
    }

    /**
     * Recursively collect tree information
     */
    private static int collectTreeInfo(
            AbstractWordBasedDTNode<String, Boolean, StateInfo<String, Boolean>> node,
            List<StateInfo<String, Boolean>> states,
            List<Word<String>> discriminators,
            int currentDepth) {

        if (node.isLeaf()) {
            StateInfo<String, Boolean> stateInfo = node.getData();
            if (stateInfo != null) {
                states.add(stateInfo);
            }
            return currentDepth;
        } else {
            Word<String> discriminator = node.getDiscriminator();
            if (discriminator != null) {
                discriminators.add(discriminator);
            }

            int maxDepth = currentDepth;
            Collection<Map.Entry<Boolean, AbstractWordBasedDTNode<String, Boolean, StateInfo<String, Boolean>>>> children = node.getChildEntries();
            for (Map.Entry<Boolean, AbstractWordBasedDTNode<String, Boolean, StateInfo<String, Boolean>>> entry : children) {
                int childDepth = collectTreeInfo(entry.getValue(), states, discriminators, currentDepth + 1);
                maxDepth = Math.max(maxDepth, childDepth);
            }

            return maxDepth;
        }
    }

    /**
     * Extract all StateInfo objects from tree leaves
     */
    private static void extractStateInfosFromTree(
            AbstractWordBasedDTNode<String, Boolean, StateInfo<String, Boolean>> node,
            List<StateInfo<String, Boolean>> stateInfosList) {
        if (node == null) {
            return;
        }

        if (node.isLeaf()) {
            StateInfo<String, Boolean> stateInfo = node.getData();
            if (stateInfo != null) {
                stateInfosList.add(stateInfo);
            }
        } else {
            Collection<Map.Entry<Boolean, AbstractWordBasedDTNode<String, Boolean, StateInfo<String, Boolean>>>> children = node.getChildEntries();
            for (Map.Entry<Boolean, AbstractWordBasedDTNode<String, Boolean, StateInfo<String, Boolean>>> entry : children) {
                extractStateInfosFromTree(entry.getValue(), stateInfosList);
            }
        }
    }

    /**
     * Load DFA from DOT file
     */
    private static CompactDFA<String> loadDFA(File dotFile) throws Exception {
        InputModelDeserializer<String, CompactDFA<String>> parser = DOTParsers.dfa();
        
        try (InputStream is = new FileInputStream(dotFile)) {
            CompactDFA<String> dfa = parser.readModel(is).model;
            return dfa;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Build equivalence oracle for DFA learning
     */
    private static EquivalenceOracle<DFA<?, String>, String, Boolean> buildEqOracle(
            Random rnd_seed,
            CommandLine line,
            CompactDFA<String> dfa,
            SUL<String, Boolean> eq_sul) {

        MembershipOracle<String, Word<Boolean>> oracleForEQoracle = new SULOracle<>(eq_sul);

        // if (!line.hasOption(EQ)) {
        //     return new WpMethodEQOracle<>(oracleForEQoracle, 2);
        // }

        LearnLibProperties learn_props = LearnLibProperties.getInstance();
        double restartProbability = learn_props.getRndWalk_restartProbability();
        int maxSteps = learn_props.getRndWalk_maxSteps();
        boolean resetStepCount = learn_props.getRndWalk_resetStepsCount();

        return new RandomWalkEQOracle<String, Word<Boolean>>(eq_sul, restartProbability, maxSteps, resetStepCount, rnd_seed);
    }

    /**
     * Create a simple SUL that simulates DFA behavior
     */
    private static class DFASimulatorSUL implements SUL<String, Boolean> {
        private final CompactDFA<String> dfa;
        private Integer currentState;

        public DFASimulatorSUL(CompactDFA<String> dfa) {
            this.dfa = dfa;
        }

        @Override
        public void pre() {
            currentState = dfa.getInitialState();
        }

        @Override
        public Boolean step(String input) {
            if (currentState == null) {
                return false;
            }
            currentState = dfa.getSuccessor(currentState, input);
            if (currentState == null) {
                return false;
            }
            return dfa.isAccepting(currentState);
        }

        @Override
        public void post() {
            currentState = null;
        }
    }

    public static void main(String[] args) throws Exception {

        // Product file paths
        String[] directories = {
            ".\\alternative_experiments\\Minepump_SPL\\products_3wise",
            ".\\alternative_experiments\\Minepump_SPL\\products_3wise"
        };
        String[] fileNames = {"00001_fsm.dot", "00002_fsm.dot"};

        for (int i = 0; i < 2; i++) {
            File productFile = new File(directories[i], fileNames[i]);
            System.out.println("\n========== Processing Product " + i + ": " + productFile + " ==========");

            // Load DFA
            CompactDFA<String> dfaMachine = loadDFA(productFile);
            System.out.println("Loaded DFA with " + dfaMachine.size() + " states");

            // Parse command line
            CommandLineParser parser = new BasicParser();
            Options options = createOptions();
            CommandLine line;
            try {
                line = parser.parse(options, args);
            } catch (ParseException e) {
                new HelpFormatter().printHelp("hi_single_dfa", options);
                return;
            }

            // Setup SUL with statistics counters
            SUL<String, Boolean> sulSim = new DFASimulatorSUL(dfaMachine);
            StatisticSUL<String, Boolean> mq_sym = new SymbolCounterSUL<>("MQ", sulSim);
            StatisticSUL<String, Boolean> mq_rst = new ResetCounterSUL<>("MQ", mq_sym);
            SUL<String, Boolean> mq_sul = mq_rst;

            long tstamp = System.currentTimeMillis();
            Random rnd_seed = new Random(tstamp);

            // Get current product's alphabet
            Alphabet<String> productAlphabet = dfaMachine.getInputAlphabet();
            System.out.println("\nProduct " + i + " alphabet contains " + productAlphabet.size() + " symbols:");
            for (String symbol : productAlphabet) {
                System.out.println("  - " + symbol);
            }

            // Build combined alphabet
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
            System.out.println("Combined alphabet now has " + combinedAlphabet.size() + " unique symbols");

            // Create learner
            de.learnlib.api.algorithm.LearningAlgorithm.DFALearner<String> learner;
            StatisticSUL<String, Boolean> mq_sym_adaptive = null;
            StatisticSUL<String, Boolean> mq_rst_adaptive = null;

            if (i == 0) {
                // Product 0: Initialize from scratch using standard KearnsVaziraniDFA
                product1Alphabet = new GrowingMapAlphabet<>(Alphabets.fromCollection(allInputAlphabets));
                MembershipOracle<String, Boolean> mqOracle = new SULOracle<>(mq_sul);
                
                learner = new KearnsVaziraniDFA<>(product1Alphabet, mqOracle, true, AcexAnalyzers.LINEAR_FWD);
                
                System.out.println("Product " + i + ": Learning from scratch with standard KearnsVaziraniDFA");
                System.out.println("  Initial alphabet size = " + product1Alphabet.size());
            } else {
                // Product i: Adaptive learning with tree reuse
                System.out.println("Product " + i + ": Incremental adaptive learning");
                System.out.println("  Previous alphabet size: " + product1Alphabet.size());
                System.out.println("  Current product alphabet size: " + productAlphabet.size());

                // Extend alphabet
                GrowingAlphabet<String> extendedAlphabet = new GrowingMapAlphabet<>(product1Alphabet);
                for (String symbol : productAlphabet) {
                    if (!extendedAlphabet.containsSymbol(symbol)) {
                        extendedAlphabet.addSymbol(symbol);
                        System.out.println("  Added new symbol: '" + symbol + "'");
                    }
                }

                // Create extended DFA for MQ oracle
                CompactDFA<String> mqDfa = new CompactDFA<>(extendedAlphabet);
                Map<Integer, Integer> stateMap = new HashMap<>();
                for (Integer state : dfaMachine.getStates()) {
                    stateMap.put(state, mqDfa.addIntState(dfaMachine.isAccepting(state)));
                }
                mqDfa.setInitialState(stateMap.get(dfaMachine.getInitialState()));

                // Copy transitions for current product symbols
                for (Integer state : dfaMachine.getStates()) {
                    for (String input : productAlphabet) {
                        Integer succ = dfaMachine.getSuccessor(state, input);
                        if (succ != null) {
                            int symbolIdx = extendedAlphabet.getSymbolIndex(input);
                            String canonicalSymbol = extendedAlphabet.getSymbol(symbolIdx);
                            mqDfa.setTransition(stateMap.get(state), canonicalSymbol, stateMap.get(succ));
                        }
                    }
                }

                // Add self-loops for symbols not in current product (reject)
                for (String symbol : extendedAlphabet) {
                    if (!productAlphabet.containsSymbol(symbol)) {
                        for (Integer state : mqDfa.getStates()) {
                            mqDfa.setTransition(state, symbol, state);
                        }
                    }
                }

                // Create adaptive SUL and oracle
                SUL<String, Boolean> mqSulSim = new DFASimulatorSUL(mqDfa);
                mq_sym_adaptive = new SymbolCounterSUL<>("MQ", mqSulSim);
                mq_rst_adaptive = new ResetCounterSUL<>("MQ", mq_sym_adaptive);
                MembershipOracle<String, Boolean> mqOracle = new SULOracle<>(mq_rst_adaptive);

                // Adapt previous hypothesis to extended alphabet
                CompactDFA<String> adaptedHypothesis = new CompactDFA<>(extendedAlphabet);
                Map<Integer, Integer> stateMap2 = new HashMap<>();
                for (Integer oldState : previousHypothesis.getStates()) {
                    Integer newState = adaptedHypothesis.addIntState(previousHypothesis.isAccepting(oldState));
                    stateMap2.put(oldState, newState);
                }
                adaptedHypothesis.setInitialState(stateMap2.get(previousHypothesis.getInitialState()));

                // Copy transitions
                for (Integer oldState : previousHypothesis.getStates()) {
                    Integer newState = stateMap2.get(oldState);
                    for (String symbol : product1Alphabet) {
                        Integer oldSucc = previousHypothesis.getSuccessor(oldState, symbol);
                        if (oldSucc != null) {
                            adaptedHypothesis.setTransition(newState, symbol, stateMap2.get(oldSucc));
                        }
                    }
                }

                // Canonicalize tree discriminators
                System.out.println("\n========== CANONICALIZING TREE DISCRIMINATORS ==========");
                canonicalizeTreeDiscriminators(tree_round2.getRoot(), extendedAlphabet);
                System.out.println("========================================================\n");

                // Extract stateInfos from tree for starting state
                List<StateInfo<String, Boolean>> stateInfosList = new ArrayList<>();
                extractStateInfosFromTree(tree_round2.getRoot(), stateInfosList);
                
                // Create learner with reused tree
                KearnsVaziraniDFAState<String> startingState = new KearnsVaziraniDFAState<>(
                    tree_round2, adaptedHypothesis, stateInfosList
                );
                
                learner = new IKearnsVaziraniDFA<>(extendedAlphabet, mqOracle, 
                    AcexAnalyzers.LINEAR_FWD, startingState);

                product1Alphabet = extendedAlphabet;
                System.out.println("  Learner created with extended alphabet size: " + extendedAlphabet.size());
                System.out.println("  Reused tree with " + stateInfosList.size() + " state infos");
            }

            // Setup equivalence oracle
            CompactDFA<String> updatedDfa;
            if (i > 0) {
                Alphabet<String> learnerAlphabet = product1Alphabet;
                updatedDfa = new CompactDFA<>(learnerAlphabet);

                Map<Integer, Integer> stateMap = new HashMap<>();
                for (Integer state : dfaMachine.getStates()) {
                    stateMap.put(state, updatedDfa.addIntState(dfaMachine.isAccepting(state)));
                }
                updatedDfa.setInitialState(stateMap.get(dfaMachine.getInitialState()));

                for (Integer state : dfaMachine.getStates()) {
                    for (String input : productAlphabet) {
                        Integer succ = dfaMachine.getSuccessor(state, input);
                        if (succ != null) {
                            updatedDfa.setTransition(stateMap.get(state), input, stateMap.get(succ));
                        }
                    }
                }

                for (String symbol : learnerAlphabet) {
                    if (!productAlphabet.containsSymbol(symbol)) {
                        for (Integer state : updatedDfa.getStates()) {
                            updatedDfa.setTransition(state, symbol, state);
                        }
                    }
                }
            } else {
                updatedDfa = dfaMachine;
            }

            SUL<String, Boolean> eqSulSim = new DFASimulatorSUL(updatedDfa);
            StatisticSUL<String, Boolean> eq_sym = new SymbolCounterSUL<>("EQ", eqSulSim);
            StatisticSUL<String, Boolean> eq_rst = new ResetCounterSUL<>("EQ", eq_sym);
            SUL<String, Boolean> eq_sul = eq_rst;

            EquivalenceOracle<DFA<?, String>, String, Boolean> eqOracle = buildEqOracle(rnd_seed, line, updatedDfa, eq_sul);

            // Run experiment
            Experiment.DFAExperiment<String> experiment = new Experiment.DFAExperiment<>(
                learner, eqOracle, product1Alphabet
            );

            experiment.run();

            // Save for next product
            previousHypothesis = (CompactDFA<String>) experiment.getFinalHypothesis();
            
            // Extract discrimination tree from learner using reflection
            if (i == 0) {
                try {
                    // For KearnsVaziraniDFA (Product 0)
                    java.lang.reflect.Field treeField = KearnsVaziraniDFA.class.getDeclaredField("discriminationTree");
                    treeField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    MultiDTree<String, Boolean, StateInfo<String, Boolean>> extractedTree = 
                        (MultiDTree<String, Boolean, StateInfo<String, Boolean>>) treeField.get(learner);
                    tree_round2 = extractedTree;
                    System.out.println("  ✓ Extracted discrimination tree from Product 0 for reuse");
                } catch (Exception e) {
                    System.err.println("  ✗ Failed to extract discrimination tree: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                // For IKearnsVaziraniDFA (Product 1+), tree is already updated
                try {
                    java.lang.reflect.Field treeField = KearnsVaziraniDFA.class.getDeclaredField("discriminationTree");
                    treeField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    MultiDTree<String, Boolean, StateInfo<String, Boolean>> extractedTree = 
                        (MultiDTree<String, Boolean, StateInfo<String, Boolean>>) treeField.get(learner);
                    tree_round2 = extractedTree;
                    System.out.println("  ✓ Updated discrimination tree from Product " + i);
                } catch (Exception e) {
                    System.err.println("  ✗ Failed to extract discrimination tree: " + e.getMessage());
                }
            }

            // Statistics
            StatisticSUL<String, Boolean> currentMqRst = (i == 0) ? mq_rst : mq_rst_adaptive;
            StatisticSUL<String, Boolean> currentMqSym = (i == 0) ? mq_sym : mq_sym_adaptive;

            System.out.println("\n========== PRODUCT " + i + " LEARNING COMPLETED ==========");
            System.out.println("Final hypothesis states: " + experiment.getFinalHypothesis().size());
            System.out.println("Rounds (EQ queries): " + experiment.getRounds().getCount());
            System.out.println("Membership queries - Resets: " + ExtractValue(currentMqRst.getStatisticalData().getSummary()));
            System.out.println("Membership queries - Symbols: " + ExtractValue(currentMqSym.getStatisticalData().getSummary()));
            System.out.println("Equivalence queries - Resets: " + ExtractValue(eq_rst.getStatisticalData().getSummary()));
            System.out.println("Equivalence queries - Symbols: " + ExtractValue(eq_sym.getStatisticalData().getSummary()));
            System.out.println("====================================================\n");

            // Visualize
            System.out.println("Visualizing learned DFA...");
            Visualization.visualize(experiment.getFinalHypothesis(), product1Alphabet);
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║         ADAPTIVE DFA LEARNING COMPLETED                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption(HELP, false, "Shows help");
        options.addOption(EQ, true, "Set equivalence query generator");
        options.addOption(SEED, true, "Seed used by the random generator");
        options.addOption(OUT, true, "Set output directory");
        options.addOption(DIR, true, "Directory of the SPL products");
        return options;
    }
}

