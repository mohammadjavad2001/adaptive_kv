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
        
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║         HYBRID EQ ORACLE - PHASE 1: SMART             ║");
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        
        // Phase 1: Try smart oracle first
        DefaultQuery<I, Word<O>> counterExample = smartOracle.findCounterExample(hypothesis, inputs);
        
        if (counterExample != null) {
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

    /**
     * Get statistics about oracle usage
     */
    public String getStatistics() {
        int total = smartOracleHits + fallbackHits;
        if (total == 0) return "No counterexamples found yet";
        
        double smartPercent = (smartOracleHits * 100.0) / total;
        double fallbackPercent = (fallbackHits * 100.0) / total;
        
        return String.format(
            "Smart: %d (%.1f%%), Fallback: %d (%.1f%%), Total: %d",
            smartOracleHits, smartPercent,
            fallbackHits, fallbackPercent,
            total
        );
    }
}

