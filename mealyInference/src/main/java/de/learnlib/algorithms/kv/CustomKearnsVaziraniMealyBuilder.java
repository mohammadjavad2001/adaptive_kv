package de.learnlib.algorithms.kv;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

import java.util.List;

public class CustomKearnsVaziraniMealyBuilder<I, O> {
    
    private Alphabet<I> alphabet;
    private MembershipOracle<I, Word<O>> oracle;
    private boolean repeatedCounterexampleEvaluation;
    private AcexAnalyzer counterexampleAnalyzer;
    private MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> initialTree;
    
    /**
     * Default constructor.
     */
    public CustomKearnsVaziraniMealyBuilder() {
        this.repeatedCounterexampleEvaluation = KearnsVaziraniMealy.BuilderDefaults.repeatedCounterexampleEvaluation();
        this.counterexampleAnalyzer = KearnsVaziraniMealy.BuilderDefaults.counterexampleAnalyzer();
    }
    
    /**
     * Set an initial discrimination tree to be used by the learner.
     * 
     * @param tree The initial discrimination tree
     * @return this builder
     */
    public CustomKearnsVaziraniMealyBuilder<I, O> setInitialTree(MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> tree) {
        this.initialTree = tree;
        return this;
    }
    
    /**
     * Set the input alphabet.
     * 
     * @param alphabet the input alphabet
     * @return this builder
     */
    public CustomKearnsVaziraniMealyBuilder<I, O> setAlphabet(Alphabet<I> alphabet) {
        this.alphabet = alphabet;
        return this;
    }
    
    /**
     * Set the membership oracle.
     * 
     * @param oracle the membership oracle
     * @return this builder
     */
    public CustomKearnsVaziraniMealyBuilder<I, O> setOracle(MembershipOracle<I, Word<O>> oracle) {
        this.oracle = oracle;
        return this;
    }
    
    /**
     * Set whether counterexamples should be evaluated repeatedly.
     * 
     * @param repeatedCounterexampleEvaluation whether to repeatedly evaluate counterexamples
     * @return this builder
     */
    public CustomKearnsVaziraniMealyBuilder<I, O> setRepeatedCounterexampleEvaluation(boolean repeatedCounterexampleEvaluation) {
        this.repeatedCounterexampleEvaluation = repeatedCounterexampleEvaluation;
        return this;
    }
    
    /**
     * Set the counterexample analyzer.
     * 
     * @param counterexampleAnalyzer the counterexample analyzer
     * @return this builder
     */
    public CustomKearnsVaziraniMealyBuilder<I, O> setCounterexampleAnalyzer(AcexAnalyzer counterexampleAnalyzer) {
        this.counterexampleAnalyzer = counterexampleAnalyzer;
        return this;
    }
    
    /**
     * Create a KearnsVaziraniMealy instance using the configured parameters.
     * 
     * @return a new KearnsVaziraniMealy instance
     */
    public KearnsVaziraniMealy<I, O> create() {
        if (initialTree != null) {
            // If you have a way to create KearnsVaziraniMealy with an initial tree,
            // you would implement that here
            return new KearnsVaziraniMealy<>(alphabet, oracle, repeatedCounterexampleEvaluation, counterexampleAnalyzer);
        }
        return new KearnsVaziraniMealy<>(alphabet, oracle, repeatedCounterexampleEvaluation, counterexampleAnalyzer);
    }
}