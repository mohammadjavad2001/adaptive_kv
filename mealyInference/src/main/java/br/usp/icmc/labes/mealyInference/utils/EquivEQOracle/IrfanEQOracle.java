/* Copyright (C) 2018 Carlos Diego N Damasceno
 */

package br.usp.icmc.labes.mealyInference.utils.EquivEQOracle;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import de.learnlib.api.SUL;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This EQ oracle sets two bounds for counterexample search. 
 * (1) the maximum length for a CE is set as five times the number of states in a SUL
 *     if this limit is reached without finding a CE, then the machine is reset, and 
 *     reinitialize the search with a new sequence. 
 * (2) the learning process terminates as soon as the number of states in a hypothesis
 *     becomes equivalent to the SUL. 
 *     
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author damascenodiego
 */


public class IrfanEQOracle<I, O> implements MealyEquivalenceOracle<I, O> {

    private final Logger LOGGER = LoggerFactory.getLogger(IrfanEQOracle.class);

    /**
     * maximum length of the CE (default: set as 2x|Q|).
     */
    private long maxLengthCE;

    /**
     * RNG.
     */
    private Random random;
    /**
     * System under learning.
     */
    private final SUL<I, O> sul;
    /**
     * Max. number of symbols (default: set as 100x|Q|).
     */
//	private long maxResets;

	private CompactMealy<I, O> sul_model;

    public IrfanEQOracle(SUL<I, O> sul,
                              CompactMealy<I, O> sulMealy,
                              Random random) {
        this.sul = sul;
        this.sul_model = sulMealy;
        this.maxLengthCE = 100*sulMealy.getStates().size();
        this.random = random;
    }

    public IrfanEQOracle(SUL<I, O> sul,
            CompactMealy<I, O> sulMealy) {
        this(sul,sulMealy,new Random());
    }
    
    @Override
    public DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis,
                                                       Collection<? extends I> inputs) {
        return doFindCounterExample(hypothesis, inputs);
    }

    private <S, T> DefaultQuery<I, Word<O>> doFindCounterExample(MealyMachine<S, I, T, O> hypothesis,
                                                                 Collection<? extends I> inputs) {
        if (inputs.isEmpty()) {
            LOGGER.warn("Passed empty set of inputs to equivalence oracle; no counterexample can be found!");
            return null;
        }

        List<? extends I> choices = CollectionsUtil.randomAccessList(inputs);
        int bound = choices.size();
        S cur = hypothesis.getInitialState();
        WordBuilder<I> wbIn = new WordBuilder<>();
        WordBuilder<O> wbOut = new WordBuilder<>();

        long steps = 0;
        sul.pre();
        try {
            while (!Automata.testEquivalence(hypothesis, sul_model, sul_model.getInputAlphabet())) {

                // restart!
            	if (steps == maxLengthCE){
            		sul.post();
            		sul.pre();
            		cur = hypothesis.getInitialState();
            		wbIn.clear();
            		wbOut.clear();
            		steps = 0;
            	}

                // step
                steps++;
                I in = choices.get(random.nextInt(bound));
                O outSul;

                outSul = sul.step(in);

                T hypTrans = hypothesis.getTransition(cur, in);
                O outHyp = hypothesis.getTransitionOutput(hypTrans);
                wbIn.add(in);
                wbOut.add(outSul);

                // ce?
                if (!outSul.equals(outHyp)) {
                    DefaultQuery<I, Word<O>> ce = new DefaultQuery<>(wbIn.toWord());
                    ce.answer(wbOut.toWord());
                    return ce;
                }
                cur = hypothesis.getSuccessor(cur, in);
            }
            return null;
        } finally {
            sul.post();
        }
    }
    public void set_maxLengthIsMult(int val) {
  		this.maxLengthCE = val*this.sul_model.getStates().size();
  	}
    
    public void set_maxLength(int val) {
  		this.maxLengthCE = val;
  	}
    
    
    public long getMaxLengthCE() {
		return maxLengthCE;
	}
    
}
