import de.learnlib.acex.AcexAnalyzer;
// import de.learnlib.acex.AcexAnalyzers;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.incremental.KearnsVaziraniDFAState;
import net.automatalib.words.Alphabet;

/**
 * Builder class for IKearnsVaziraniDFA to facilitate easy construction
 * Similar to KearnsVaziraniMealyBuilder pattern
 */
public class IKearnsVaziraniDFABuilder {
    
    private Alphabet<String> alphabet;
    private MembershipOracle<String, Boolean> oracle;
    private AcexAnalyzer counterexampleAnalyzer = AcexAnalyzer.LINEAR_FWD;
    private KearnsVaziraniDFAState<String> startingState;
    
    /**
     * Set the alphabet for learning
     */
    public IKearnsVaziraniDFABuilder setAlphabet(Alphabet<String> alphabet) {
        this.alphabet = alphabet;
        return this;
    }
    
    /**
     * Set the membership oracle
     */
    public IKearnsVaziraniDFABuilder setOracle(MembershipOracle<String, Boolean> oracle) {
        this.oracle = oracle;
        return this;
    }
    
    /**
     * Set the counterexample analyzer (optional)
     */
    public IKearnsVaziraniDFABuilder setCounterexampleAnalyzer(AcexAnalyzer analyzer) {
        this.counterexampleAnalyzer = analyzer;
        return this;
    }
    
    /**
     * Set the starting state for adaptive learning (optional)
     */
    public IKearnsVaziraniDFABuilder setStartingState(KearnsVaziraniDFAState<String> startingState) {
        this.startingState = startingState;
        return this;
    }
    
    /**
     * Create the IKearnsVaziraniDFA learner
     * If no starting state is provided, returns null (caller should handle standard initialization)
     */
    public IKearnsVaziraniDFA<String> create() {
        if (alphabet == null) {
            throw new IllegalStateException("Alphabet must be set");
        }
        if (oracle == null) {
            throw new IllegalStateException("Oracle must be set");
        }
        
        // If no starting state, we need to use the standard KearnsVaziraniDFA
        // This is a limitation - IKearnsVaziraniDFA requires a starting state
        if (startingState == null) {
            // For initial learning, we need to create an empty starting state
            // or use standard KearnsVaziraniDFA
            // Here we'll throw an exception to indicate this needs special handling
            throw new IllegalStateException("IKearnsVaziraniDFA requires a starting state. " +
                "For initial learning, use standard KearnsVaziraniDFA.");
        }
        
        return new IKearnsVaziraniDFA<>(alphabet, oracle, counterexampleAnalyzer, startingState);
    }
}

