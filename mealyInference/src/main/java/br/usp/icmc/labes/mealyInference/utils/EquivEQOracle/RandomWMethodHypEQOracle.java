/* Copyright (C) 2018 Carlos Diego N Damasceno
 */

package br.usp.icmc.labes.mealyInference.utils.EquivEQOracle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import de.learnlib.api.SUL;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.EquivalenceOracle.MealyEquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.oracle.equivalence.MealyRandomWMethodEQOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.commons.util.collections.CollectionsUtil;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.cover.Covers;
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


public class RandomWMethodHypEQOracle<I, O> extends MealyRandomWMethodEQOracle<I, O> {

    private final Logger LOGGER = LoggerFactory.getLogger(RandomWMethodHypEQOracle.class);

    private CompactMealy<I, O> sul_fsm;

    
    public RandomWMethodHypEQOracle(MembershipOracle.MealyMembershipOracle<I, O> mqOracle,
    		int minimalSize,
    		int rndLength,
    		CompactMealy<I, O> fsm) {
    	super(mqOracle, minimalSize, rndLength);
    	this.sul_fsm = fsm;
    }

    public RandomWMethodHypEQOracle(MembershipOracle.MealyMembershipOracle<I, O> mqOracle,
    		int minimalSize,
    		int rndLength,
    		int bound,
    		CompactMealy<I, O> fsm) {
    	super(mqOracle, minimalSize, rndLength, bound);
    	this.sul_fsm = fsm;
    }
    
    public RandomWMethodHypEQOracle(MembershipOracle.MealyMembershipOracle<I, O> mqOracle,
    		int minimalSize,
    		int rndLength,
    		int bound,
    		int batchSize,
    		CompactMealy<I, O> fsm) {
    	super(mqOracle, minimalSize, rndLength, bound, batchSize);
    	this.sul_fsm = fsm;
    }
    
    public RandomWMethodHypEQOracle(MembershipOracle.MealyMembershipOracle<I, O> mqOracle,
    		int minimalSize,
    		int rndLength,
    		int bound,
    		Random random,
    		int batchSize,
    		CompactMealy<I, O> fsm) {
    	super(mqOracle, minimalSize, rndLength, bound, random, batchSize);
    	this.sul_fsm = fsm;
    }
    
    @Override
    public DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis,
                                                       Collection<? extends I> inputs) {
//    	if(sul_fsm.getStates().size()==hypothesis.getStates().size()) {
//    		return null;
//    	}
        return super.findCounterExample(hypothesis, inputs);
    }
}
