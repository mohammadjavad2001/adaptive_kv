/* Copyright (C) 2013-2020 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.algorithms.kv;
import net.automatalib.graphs.concepts.GraphViewable;

import de.learnlib.api.algorithm.LearningAlgorithm;
import de.learnlib.api.logging.LearnLogger;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import de.learnlib.filter.statistic.Counter;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;
import de.learnlib.algorithms.kv.KearnsVaziraniMealy;

/**
 * runs a learning experiment.
 *
 * @param <A> the automaton type
 *
 * @author falkhowar
 */
public class Experiment<A extends Object> {

    public static final String LEARNING_PROFILE_KEY = "Learning";
    public static final String COUNTEREXAMPLE_PROFILE_KEY = "Searching for counterexample";

    private static final LearnLogger LOGGER = LearnLogger.getLogger(Experiment.class);
    private final ExperimentImpl<?, ?> impl;
    private boolean logModels;
    private boolean profile;
    private final Counter rounds = new Counter("learning rounds", "#");
    private @Nullable A finalHypothesis;
    private MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> discrtree;

    public <I, D> Experiment(LearningAlgorithm<? extends A, I, D> learningAlgorithm,
                             EquivalenceOracle<? super A, I, D> equivalenceAlgorithm,
                             Alphabet<I> inputs) {
        this.impl = new ExperimentImpl<>(learningAlgorithm, equivalenceAlgorithm, inputs);
    }
    public MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> getDiscrtree() {
        return discrtree;
    }
    public void setDiscrtree(MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> discrtree) {
        this.discrtree = discrtree;
    }
    public A run(boolean first) {
        
        if (this.finalHypothesis != null) {
            throw new IllegalStateException("Experiment has already been run");
        }

        finalHypothesis = impl.run(first);
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
    public void setLogModels(boolean logModels) {
        this.logModels = logModels;
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

        public A run(boolean first) {
            rounds.increment();
            LOGGER.logPhase("Starting round " + rounds.getCount());
            System.out.println("Starting round " + rounds.getCount());
            LOGGER.logPhase("Learning");
            System.out.println("Learning");

            profileStart(LEARNING_PROFILE_KEY);
            learningAlgorithm.startLearning();
            profileStop(LEARNING_PROFILE_KEY);

            while (true) {
                final A hyp = learningAlgorithm.getHypothesisModel();
		        //visualize fsm 
                // Visualization.visualize(((GraphViewable) hyp).graphView(), true);
                KearnsVaziraniMealy<String, Word<String>> kvLearner = (KearnsVaziraniMealy<String, Word<String>>) learningAlgorithm;
                //visualize discrimintation tree
                MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree = kvLearner.getDiscriminationTree();
                
                Visualization.visualize(tree, true);
                if (logModels) {
                    LOGGER.logModel(hyp);
                }

                LOGGER.logPhase("Searching for counterexample");
                System.out.println("Searching for counterexample");
                profileStart(COUNTEREXAMPLE_PROFILE_KEY);
                DefaultQuery<I, D> ce = equivalenceAlgorithm.findCounterExample(hyp, inputs);
                profileStop(COUNTEREXAMPLE_PROFILE_KEY);

                if (ce == null) {
                    return hyp;
                }

                LOGGER.logCounterexample(ce.getInput().toString());

                // next round ...
                rounds.increment();
                LOGGER.logPhase("Starting round " + rounds.getCount());
                System.out.println("Starting round " + rounds.getCount());
                if(rounds.getCount()==4 && first){
                    MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> tree_round2 = kvLearner.getDiscriminationTree();
                    setDiscrtree(tree_round2);
                    return hyp;

                    // tree_round2.getEdgesBetween(null, null);
                    // MultiDTree<String, Word<Word<String>>, StateInfo<String, Word<Word<String>>>> temp_tree= new MultiDTree<>(null);
                    // kvLearner.getDiscriminationTree();
                    // System.out.println("UUUUUUUUUUUUUUUUUUU");
                    // Visualization.visualize((tree_round2), true);
                    // System.out.println("MJEJEJEJEJEEJEJ");
                }
                LOGGER.logPhase("Learning");

                profileStart(LEARNING_PROFILE_KEY);
                final boolean refined = learningAlgorithm.refineHypothesis(ce);
                profileStop(LEARNING_PROFILE_KEY);

                assert refined;
            }
        }
    }

    public static class DFAExperiment<I> extends Experiment<DFA<?, I>> {
        public DFAExperiment(LearningAlgorithm<? extends DFA<?, I>, I, Boolean> learningAlgorithm,
                             EquivalenceOracle<? super DFA<?, I>, I, Boolean> equivalenceAlgorithm,
                             Alphabet<I> inputs) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs);
        }

    }

    public static class MealyExperiment<I, O> extends Experiment<MealyMachine<?, I, ?, O>> {

        public MealyExperiment(LearningAlgorithm<? extends MealyMachine<?, I, ?, O>, I, Word<O>> learningAlgorithm,
                               EquivalenceOracle<? super MealyMachine<?, I, ?, O>, I, Word<O>> equivalenceAlgorithm,
                               Alphabet<I> inputs) {
            super(learningAlgorithm, equivalenceAlgorithm, inputs);
        }

    }
}
