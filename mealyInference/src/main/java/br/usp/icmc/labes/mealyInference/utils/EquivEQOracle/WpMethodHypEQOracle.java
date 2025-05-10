/* Copyright (C) 2018 Carlos Diego N Damasceno
 */

package br.usp.icmc.labes.mealyInference.utils.EquivEQOracle;

import java.util.Collection;

import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.oracle.equivalence.MealyWpMethodEQOracle;
import de.learnlib.oracle.equivalence.WpMethodEQOracle;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Word;
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


public class WpMethodHypEQOracle<I, O> extends MealyWpMethodEQOracle<I, O>{

    private final LearnLogger LOGGER = LearnLogger.getLogger(WpMethodHypEQOracle.class);

    private CompactMealy<I, O> sul_fsm;

    public WpMethodHypEQOracle(MembershipOracle.MealyMembershipOracle<I, O> sulOracle,
        int lookahead, CompactMealy<I, O> fsm) {
      super(sulOracle, lookahead);
      this.sul_fsm = fsm;
    }

    /**
     * This is an auto-generated constructor. See the {@link WpMethodEQOracle#WpMethodEQOracle(MembershipOracle, int, int) original constructor}.
     */
    public WpMethodHypEQOracle(MembershipOracle.MealyMembershipOracle<I, O> sulOracle,
        int lookahead, int expectedSize, CompactMealy<I, O> fsm) {
      super(sulOracle, lookahead, expectedSize);
      this.sul_fsm = fsm;
    }

    /**
     * This is an auto-generated constructor. See the {@link WpMethodEQOracle#WpMethodEQOracle(MembershipOracle, int, int, int) original constructor}.
     */
    public WpMethodHypEQOracle(MembershipOracle.MealyMembershipOracle<I, O> sulOracle,
        int lookahead, int expectedSize, int batchSize, CompactMealy<I, O> fsm) {
      super(sulOracle, lookahead, expectedSize, batchSize);
      this.sul_fsm = fsm;
    }

    @Override
    public DefaultQuery<I, Word<O>> findCounterExample(MealyMachine<?, I, ?, O> hypothesis,
                                                       Collection<? extends I> inputs) {
//    	if(sul_fsm.getStates().size()==hypothesis.getStates().size()) {
//        	return null;
//        }
    	LOGGER.logEvent("EquivalenceOracle: WpMethodHypEQOracle: {HypothesisSize="+hypothesis.getStates().size()+";SULSize="+sul_fsm.getStates().size()+";}");
        return super.findCounterExample(hypothesis, inputs);
    }
    
}
