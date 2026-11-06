import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.oracle.equivalence.WMethodEQOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle;
// import de.learnlib.oracle.equivalence.RandomWalkEQOracle;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.*;

/**
 * Hybrid Adaptive Equivalence Oracle that combines smart adaptive testing
 * with fallback to exhaustive methods for completeness.
 * 
 * Strategy:
 * 1. Try SmartAdaptiveEQOracle first (fast, targeted)
 * 2. If no counterexample found, fallback to WpMethod (exhaustive)
 */
public class HybridAdaptiveEQOracle<I, O> implements EquivalenceOracle<MealyMachine<?, I, ?, O>, I, Word<O>> {

    private final SmartAdaptiveEQOracle<I, O> smartOracle;
    private final WpMethodEQOracle<MealyMachine<?, I, ?, O>, I, Word<O>> wpMethodOracle;
    private final MembershipOracle<I, Word<O>> membershipOracle;
    
    private int smartOracleHits = 0;
    private int fallbackHits = 0;
    private int replayHits = 0;
    // Replay is scoped to the current product and checked against each new hypothesis.
    private final Set<Word<I>> counterexamples = new LinkedHashSet<>();

    /**
     * Constructor for Hybrid Adaptive EQ Oracle
     * 
     * @param membershipOracle The membership oracle
     * @param previousProducts List of previously learned product models
     * @param currentAlphabet The alphabet for the current product
     * @param newSymbols Set of symbols that are new in this product
     * @param smartMaxTests Maximum tests for smart oracle phase
     * @param smartMinLength Min length for smart oracle
     * @param smartMaxLength Max length for smart oracle
     * @param wpLookahead Lookahead depth for WpMethod fallback
     * @param random Random generator
     */
    public HybridAdaptiveEQOracle(
            MembershipOracle<I, Word<O>> membershipOracle,
            List<MealyMachine<?, I, ?, O>> previousProducts,
            Alphabet<I> currentAlphabet,
            Set<I> newSymbols,
            int smartMaxTests,
            int smartMinLength,
            int smartMaxLength,
            int wpLookahead,
            Random random) {
        
        this.membershipOracle = membershipOracle;
        
        // Create smart oracle
        this.smartOracle = new SmartAdaptiveEQOracle<>(
            membershipOracle,
            previousProducts,
            currentAlphabet,
            newSymbols,
            smartMaxTests,
            smartMinLength,
            smartMaxLength,
            random
        );
        
        // Create WpMethod oracle as fallback
        this.wpMethodOracle = new WpMethodEQOracle<>(membershipOracle, wpLookahead);
        
        System.out.println("╔═══════════════════════════════════════════════════════╗");
        System.out.println("║     HYBRID ADAPTIVE EQ ORACLE INITIALIZED             ║");
        System.out.println("╠═══════════════════════════════════════════════════════╣");
        System.out.println("║  Phase 1: Smart Adaptive Testing                      ║");
        System.out.println("║           - Max tests: " + smartMaxTests + "                             ║");
        System.out.println("║           - Max length: " + smartMaxLength + "                             ║");
        System.out.println("║  Phase 2: WpMethod (lookahead=" + wpLookahead + ")                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
    }

    @Override
    public DefaultQuery<I, Word<O>> findCounterExample(
            MealyMachine<?, I, ?, O> hypothesis,
            Collection<? extends I> inputs) {
        for (Word<I> input : counterexamples) {
            if (!inputs.containsAll(input.asList())) {
                continue;
            }
            DefaultQuery<I, Word<O>> replay = new DefaultQuery<>(input);
            membershipOracle.processQuery(replay);
            if (!Objects.equals(hypothesis.computeOutput(input), replay.getOutput())) {
                replayHits++;
                System.out.println("Counterexample replay hit: " + input);
                return replay;
            }
        }
        
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║         HYBRID EQ ORACLE - PHASE 1: SMART             ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        
        // Phase 1: Try smart oracle first
        DefaultQuery<I, Word<O>> counterExample = smartOracle.findCounterExample(hypothesis, inputs);
        
        if (counterExample != null) {
            counterExample = minimizeCounterexample(hypothesis, counterExample);
            counterexamples.add(counterExample.getInput());
            smartOracleHits++;
            System.out.println("\n✓✓✓ SMART ORACLE SUCCESS ✓✓✓");
            System.out.println("Counterexample found: " + counterExample.getInput());
            System.out.println("Counterexample length: " + counterExample.getInput().length());
            System.out.println("Stats: Smart hits=" + smartOracleHits + ", Fallback hits=" + fallbackHits);
            System.out.println("════════════════════════════════════════════════════════\n");
            return counterExample;
        }
        
        // Phase 2: Fall back to WpMethod for exhaustive search
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║       HYBRID EQ ORACLE - PHASE 2: WpMETHOD            ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        System.out.println("Smart oracle found no counterexample.");
        System.out.println("Falling back to exhaustive WpMethod...");
        
        counterExample = wpMethodOracle.findCounterExample(hypothesis, inputs);
        
        if (counterExample != null) {
            counterExample = minimizeCounterexample(hypothesis, counterExample);
            counterexamples.add(counterExample.getInput());
            fallbackHits++;
            System.out.println("\n✓✓✓ WpMETHOD FALLBACK SUCCESS ✓✓✓");
            System.out.println("Counterexample found: " + counterExample.getInput());
            System.out.println("Counterexample length: " + counterExample.getInput().length());
            System.out.println("Stats: Smart hits=" + smartOracleHits + ", Fallback hits=" + fallbackHits);
            System.out.println("════════════════════════════════════════════════════════\n");
        } else {
            System.out.println("\n✓ NO COUNTEREXAMPLE FOUND - HYPOTHESIS CORRECT ✓");
            System.out.println("Stats: Smart hits=" + smartOracleHits + ", Fallback hits=" + fallbackHits);
            System.out.println("════════════════════════════════════════════════════════\n");
        }
        
        return counterExample;
    }

    private DefaultQuery<I, Word<O>> minimizeCounterexample(
            MealyMachine<?, I, ?, O> hypothesis, DefaultQuery<I, Word<O>> query) {
        Word<I> input = query.getInput();
        Word<O> expected = hypothesis.computeOutput(input);
        Word<O> actual = query.getOutput();
        for (int i = 0; i < Math.min(expected.length(), actual.length()); i++) {
            if (!Objects.equals(expected.getSymbol(i), actual.getSymbol(i))) {
                input = input.prefix(i + 1);
                query = new DefaultQuery<>(input, actual.prefix(i + 1));
                break;
            }
        }
        // Every accepted deletion is verified against the current product through its EQ cache.
        for (int i = 0; i < input.length() && input.length() > 1;) {
            Word<I> candidate = input.prefix(i).concat(input.subWord(i + 1));
            DefaultQuery<I, Word<O>> shorter = new DefaultQuery<>(candidate);
            membershipOracle.processQuery(shorter);
            if (!Objects.equals(hypothesis.computeOutput(candidate), shorter.getOutput())) {
                query = shorter;
                input = candidate;
                i = 0;
            } else {
                i++;
            }
        }
        return query;
    }

    /**
     * Get statistics about oracle usage
     */
    public String getStatistics() {
        int total = smartOracleHits + fallbackHits + replayHits;
        if (total == 0) return "No counterexamples found yet";
        
        double smartPercent = (smartOracleHits * 100.0) / total;
        double fallbackPercent = (fallbackHits * 100.0) / total;
        
        return String.format(
            "Smart: %d (%.1f%%), Fallback: %d (%.1f%%), Replay: %d, Total: %d",
            smartOracleHits, smartPercent,
            fallbackHits, fallbackPercent,
            replayHits, total
        );
    }
}


