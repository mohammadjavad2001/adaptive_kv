package de.learnlib.util;

/* Copyright (C) 2021 CDN (Diego) Damasceno
 */

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.observationtable.OTLearner.OTLearnerMealy;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.datastructure.observationtable.ObservationTable;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.graphs.Graph;
import net.automatalib.graphs.concepts.GraphViewable;
import net.automatalib.words.Alphabet;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Word;


import java.io.IOException;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * runs a learning experiment while debugs the observation table.
 *
 * @param <A> the automaton type
 *
 * @author damascenodiego
 */
public class ExperimentDebug<A extends Object> extends Experiment<A>{

    public static final String LEARNING_PROFILE_KEY = "Learning";
    public static final String COUNTEREXAMPLE_PROFILE_KEY = "Searching for counterexample";

    private static final LearnLogger LOGGER = LearnLogger.getLogger(ExperimentDebug.class);
    private final ExperimentImpl<?, ?> impl;
    private boolean logOT;
    private boolean profile;
    private final Counter rounds = new Counter("learning rounds", "#");
    private @Nullable A finalHypothesis;

    public <I, D> ExperimentDebug(LearningAlgorithm<? extends A, I, D> learningAlgorithm,
                             EquivalenceOracle<? super A, I, D> equivalenceAlgorithm,
                             Alphabet<I> inputs) {
    	super(learningAlgorithm, equivalenceAlgorithm, inputs);
        this.impl = new ExperimentImpl<>(learningAlgorithm, equivalenceAlgorithm, inputs);
    }

    public A run() {
        if (this.finalHypothesis != null) {
            throw new IllegalStateException("Experiment has already been run");
        }

        finalHypothesis = impl.run();
        return finalHypothesis;
    }

    public A getFinalHypothesis() {
        if (finalHypothesis == null) {
            throw new IllegalStateException("Experiment has not yet been run");
        }

        return finalHypothesis;
    }

    private void profileStart(String taskname) {
        if (profile) {
            SimpleProfiler.start(taskname);
        }
    }

    private void profileStop(String taskname) {
        if (profile) {
            SimpleProfiler.stop(taskname);
        }
    }

    /**
     * @param logModels
     *         flag whether models should be logged
     */
    public void setLogOT(boolean logModels) {
        this.logOT = logModels;
    }

    /**
     * @param profile
     *         flag whether learning process should be profiled
     */
    public void setProfile(boolean profile) {
        this.profile = profile;
    }

    /**
     * @return the rounds
     */
    public Counter getRounds() {
        return rounds;
    }

    private final class ExperimentImpl<I, D> {

        private final LearningAlgorithm<? extends A, I, D> learningAlgorithm;
        private final EquivalenceOracle<? super A, I, D> equivalenceAlgorithm;
        private final Alphabet<I> inputs;

        ExperimentImpl(LearningAlgorithm<? extends A, I, D> learningAlgorithm,
                       EquivalenceOracle<? super A, I, D> equivalenceAlgorithm,
                       Alphabet<I> inputs) {
            this.learningAlgorithm = learningAlgorithm;
            this.equivalenceAlgorithm = equivalenceAlgorithm;
            this.inputs = inputs;
        }

        public A run() {
            rounds.increment();
            LOGGER.logPhase("Starting round " + rounds.getCount());
            LOGGER.logPhase("Learning");

            profileStart(LEARNING_PROFILE_KEY);
            learningAlgorithm.startLearning();
            profileStop(LEARNING_PROFILE_KEY);

            while (true) {
                final A hyp = learningAlgorithm.getHypothesisModel();
                

                if (logOT) {
                    ObservationTable<I, Word<D>> ot = ((OTLearnerMealy<I, D>)learningAlgorithm).getObservationTable();
                    StringBuffer sb = new StringBuffer();
    				sb.append("Observation Table (Round "+rounds.getCount()+"):\n");
    				try {
						new ObservationTableASCIIWriter<>().write(ot, sb);
					} catch (IOException e) {
						e.printStackTrace();
					}
    				LOGGER.logEvent(sb.toString());
                }
                else {
//    				Visualization.visualize(((GraphViewable) hyp).graphView(), true);
//                	Visualization.visualizeGraph(learningAlgorithm.getHypothesisModel().getDiscriminationTree().graphView(), true);
                }

                LOGGER.logPhase("Searching for counterexample");

                profileStart(COUNTEREXAMPLE_PROFILE_KEY);
                DefaultQuery<I, D> ce = equivalenceAlgorithm.findCounterExample(hyp, inputs);
                profileStop(COUNTEREXAMPLE_PROFILE_KEY);


                if (ce == null) {
                    return hyp;
                }




                System.out.println("Counterexample: " + ce.getInput() + " --> " + ce.getOutput());
                
                LOGGER.logCounterexample(ce.getInput().toString());

                // next round ...
                rounds.increment();
                LOGGER.logPhase("Starting round " + rounds.getCount());
                LOGGER.logPhase("Learning");

                profileStart(LEARNING_PROFILE_KEY);
                final boolean refined = learningAlgorithm.refineHypothesis(ce);
                profileStop(LEARNING_PROFILE_KEY);

                
//                Visualization.visualizeAutomaton(hyp, inputs, true);
                assert refined;
            }
        }
    }

    public static class MealyExperiment<I, O> extends ExperimentDebug<MealyMachine<?, I, ?, O>> {

        public MealyExperiment(LearningAlgorithm<? extends MealyMachine<?, I, ?, O>, I, Word<O>> learningAlgorithm,
                               EquivalenceOracle<? super MealyMachine<?, I, ?, O>, I, Word<O>> equivalenceAlgorithm,
                               Alphabet<I> inputs) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs);
        }

    }
}
