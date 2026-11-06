import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import java.util.*;

/**
 * Smart Adaptive Equivalence Oracle that uses previously learned products as guides
 * to generate more effective test cases, focusing on differences and new symbols.
 */
public class SmartAdaptiveEQOracle<I, O> implements EquivalenceOracle<MealyMachine<?, I, ?, O>, I, Word<O>> {

    private final MembershipOracle<I, Word<O>> membershipOracle;
    private final List<MealyMachine<?, I, ?, O>> previousProducts;
    private final Random random;
    private final int maxTests;
    private final int minLength;
    private final int maxLength;
    private final Set<I> newSymbols;
    private final Alphabet<I> currentAlphabet;

    /**
     * Constructor for Smart Adaptive EQ Oracle
     * 
     * @param membershipOracle The membership oracle to test sequences
     * @param previousProducts List of previously learned product models
     * @param currentAlphabet The alphabet for the current product
     * @param newSymbols Set of symbols that are new in this product
     * @param maxTests Maximum number of test sequences to generate
     * @param minLength Minimum length of test sequences
     * @param maxLength Maximum length of test sequences
     * @param random Random generator for randomization
     */
    public SmartAdaptiveEQOracle(
            MembershipOracle<I, Word<O>> membershipOracle,
            List<MealyMachine<?, I, ?, O>> previousProducts,
            Alphabet<I> currentAlphabet,
            Set<I> newSymbols,
            int maxTests,
            int minLength,
            int maxLength,
            Random random) {
        this.membershipOracle = membershipOracle;
        this.previousProducts = new ArrayList<>(previousProducts);
        this.currentAlphabet = currentAlphabet;
        this.newSymbols = new HashSet<>(newSymbols);
        this.maxTests = maxTests;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.random = random;
    }

    @Override
    public DefaultQuery<I, Word<O>> findCounterExample(
            MealyMachine<?, I, ?, O> hypothesis,
            Collection<? extends I> inputs) {
        
        System.out.println("\n========== SMART ADAPTIVE EQ ORACLE ==========");
        System.out.println("Using " + previousProducts.size() + " previous products as guides");
        System.out.println("New symbols to focus on: " + newSymbols.size());
        System.out.println("Generating up to " + maxTests + " smart test sequences");
        
        // Strategy 1: Test sequences with NEW symbols (40% of tests)
        int newSymbolTests = (int) (maxTests * 0.4);
        for (int i = 0; i < newSymbolTests; i++) {
            Word<I> testSeq = generateSequenceWithNewSymbols();
            DefaultQuery<I, Word<O>> counterExample = testSequence(hypothesis, testSeq);
            if (counterExample != null) {
                System.out.println("✓ Counterexample found using NEW SYMBOL strategy");
                return counterExample;
            }
        }
        
        // Strategy 2: Test sequences that differ between previous products (30% of tests)
        int differenceTests = (int) (maxTests * 0.3);
        for (int i = 0; i < differenceTests; i++) {
            Word<I> testSeq = generateSequenceFromPreviousProductDifferences();
            DefaultQuery<I, Word<O>> counterExample = testSequence(hypothesis, testSeq);
            if (counterExample != null) {
                System.out.println("✓ Counterexample found using DIFFERENCE strategy");
                return counterExample;
            }
        }
        
        // Strategy 3: Random sequences with bias towards new symbols (30% of tests)
        int randomTests = (int) (maxTests * 0.3);
        for (int i = 0; i < randomTests; i++) {
            Word<I> testSeq = generateBiasedRandomSequence();
            DefaultQuery<I, Word<O>> counterExample = testSequence(hypothesis, testSeq);
            if (counterExample != null) {
                System.out.println("✓ Counterexample found using BIASED RANDOM strategy");
                return counterExample;
            }
        }
        
        System.out.println("✗ No counterexample found after " + maxTests + " tests");
        System.out.println("==============================================\n");
        return null;
    }

    /**
     * Generate a sequence that heavily uses new symbols
     */
    private Word<I> generateSequenceWithNewSymbols() {
        if (newSymbols.isEmpty() || currentAlphabet.size() == 0) {
            return generateRandomSequence();
        }

        WordBuilder<I> builder = new WordBuilder<>();
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        
        List<I> newSymbolList = new ArrayList<>(newSymbols);
        List<I> allSymbolList = new ArrayList<>();
        for (I symbol : currentAlphabet) {
            allSymbolList.add(symbol);
        }
        
        for (int i = 0; i < length; i++) {
            // 70% chance to use a new symbol if available
            if (!newSymbolList.isEmpty() && random.nextDouble() < 0.7) {
                builder.add(newSymbolList.get(random.nextInt(newSymbolList.size())));
            } else {
                builder.add(allSymbolList.get(random.nextInt(allSymbolList.size())));
            }
        }
        
        return builder.toWord();
    }

    /**
     * Generate sequences that explore areas where previous products had differences
     */
    private Word<I> generateSequenceFromPreviousProductDifferences() {
        if (previousProducts.isEmpty() || currentAlphabet.size() == 0) {
            return generateRandomSequence();
        }

        // Find sequences that produce different outputs in previous products
        WordBuilder<I> builder = new WordBuilder<>();
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        
        List<I> symbolList = new ArrayList<>();
        for (I symbol : currentAlphabet) {
            symbolList.add(symbol);
        }
        
        // Build sequence incrementally, checking for differences at each step
        Word<I> currentSeq = Word.epsilon();
        for (int i = 0; i < length && !symbolList.isEmpty(); i++) {
            I nextSymbol = symbolList.get(random.nextInt(symbolList.size()));
            Word<I> testSeq = currentSeq.append(nextSymbol);
            
            // Check if this sequence produces different outputs in previous products
            if (hasDifferentOutputsInPreviousProducts(testSeq)) {
                // Good! This area has differences, continue from here
                builder.append(testSeq);
                currentSeq = testSeq;
            } else {
                // Try a different symbol or just add it anyway
                builder.add(nextSymbol);
                currentSeq = currentSeq.append(nextSymbol);
            }
        }
        
        if (builder.size() == 0) {
            return generateRandomSequence();
        }
        
        return builder.toWord();
    }

    /**
     * Check if a sequence produces different outputs across previous products
     */
    private boolean hasDifferentOutputsInPreviousProducts(Word<I> sequence) {
        if (previousProducts.size() < 2) {
            return false;
        }

        Set<Word<O>> outputs = new HashSet<>();
        for (MealyMachine<?, I, ?, O> product : previousProducts) {
            try {
                Word<O> output = product.computeOutput(sequence);
                if (output != null) {
                    outputs.add(output);
                }
            } catch (Exception e) {
                // Symbol might not exist in this product's alphabet
                continue;
            }
        }
        
        // Return true if we got different outputs
        return outputs.size() > 1;
    }

    /**
     * Generate random sequence with bias towards new symbols
     */
    private Word<I> generateBiasedRandomSequence() {
        WordBuilder<I> builder = new WordBuilder<>();
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        
        List<I> newSymbolList = new ArrayList<>(newSymbols);
        List<I> allSymbolList = new ArrayList<>();
        for (I symbol : currentAlphabet) {
            allSymbolList.add(symbol);
        }
        
        if (allSymbolList.isEmpty()) {
            return Word.epsilon();
        }
        
        for (int i = 0; i < length; i++) {
            // 30% chance to use a new symbol if available
            if (!newSymbolList.isEmpty() && random.nextDouble() < 0.3) {
                builder.add(newSymbolList.get(random.nextInt(newSymbolList.size())));
            } else {
                builder.add(allSymbolList.get(random.nextInt(allSymbolList.size())));
            }
        }
        
        return builder.toWord();
    }

    /**
     * Generate a completely random sequence as fallback
     */
    private Word<I> generateRandomSequence() {
        List<I> symbolList = new ArrayList<>();
        for (I symbol : currentAlphabet) {
            symbolList.add(symbol);
        }
        
        if (symbolList.isEmpty()) {
            return Word.epsilon();
        }
        
        WordBuilder<I> builder = new WordBuilder<>();
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        
        for (int i = 0; i < length; i++) {
            builder.add(symbolList.get(random.nextInt(symbolList.size())));
        }
        
        return builder.toWord();
    }

    /**
     * Test a sequence against the hypothesis using the membership oracle
     */
    private DefaultQuery<I, Word<O>> testSequence(MealyMachine<?, I, ?, O> hypothesis, Word<I> input) {
        if (input.isEmpty()) {
            return null;
        }

        // Get expected output from hypothesis
        Word<O> hypothesisOutput;
        try {
            hypothesisOutput = hypothesis.computeOutput(input);
        } catch (Exception e) {
            // Hypothesis might not handle this input
            return null;
        }

        // Get actual output from membership oracle
        DefaultQuery<I, Word<O>> query = new DefaultQuery<>(input);
        membershipOracle.processQuery(query);
        Word<O> actualOutput = query.getOutput();

        // Check if outputs match
        if (actualOutput != null && !actualOutput.equals(hypothesisOutput)) {
            // Found a counterexample!
            return query;
        }

        return null;
    }
}


