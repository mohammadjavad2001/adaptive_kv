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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.learnlib.algorithms.kv.GenerateBuilder;



import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.acex.analyzers.AcexAnalyzers;
import de.learnlib.acex.impl.AbstractBaseCounterexample;

import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
// import de.learnlib.ds.AbstractWordBasedDTNode;

// import de.learnlib.ds.StateInfo;
import de.learnlib.algorithms.kv.StateInfo;

import de.learnlib.api.Resumable;
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
// import de.learnlib.ds.MembershipOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
// import de.learnlib.ds.MultiDTree;
import de.learnlib.datastructure.discriminationtree.MultiDTree;

import de.learnlib.datastructure.discriminationtree.model.LCAInfo;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.SupportsGrowingAlphabet;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMealyTransition;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Field;

// import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealyState;
import de.learnlib.algorithms.kv.KearnsVaziraniMealyState;
/**
 * @param <A>
 * @param <I>
 *         input symbol type
 * @param <O>
 *         output symbol type
 *
 * @author Malte Isberner
 */

public class KearnsVaziraniMealy<I, O>
        implements MealyLearner<I, O>, SupportsGrowingAlphabet<I>, Resumable<KearnsVaziraniMealyState<I, O>> {

    private final Alphabet<I> alphabet;
    private final MembershipOracle<I, Word<O>> oracle;
    private final boolean repeatedCounterexampleEvaluation;
    private final AcexAnalyzer ceAnalyzer;
    protected MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> discriminationTree;
    protected List<StateInfo<I, Word<O>>> stateInfos = new ArrayList<>();
    private CompactMealy<I, O> hypothesis;

    @GenerateBuilder
    public KearnsVaziraniMealy(Alphabet<I> alphabet,
                               MembershipOracle<I, Word<O>> oracle,
                               boolean repeatedCounterexampleEvaluation,
                               AcexAnalyzer counterexampleAnalyzer,
                               MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> tree,
                               CompactMealy<I, O> hyp) {
        this.alphabet = alphabet;
        if (hyp==null){
            this.hypothesis = new CompactMealy<>(alphabet);

        }
        else{
            this.hypothesis = hyp;
        }
        this.oracle = oracle;
        this.repeatedCounterexampleEvaluation = repeatedCounterexampleEvaluation;
        this.ceAnalyzer = counterexampleAnalyzer;
        if (tree == null)
            this.discriminationTree = new MultiDTree<>(oracle);
        else{
            // System.out.println("OOOOOOOOOOOOOOOOOOOOOOOOOO");
            // Class<?> clazz = tree.getClass();
            // System.out.println("---- " + clazz.getName() + " ----");
    
            // // Loop over all declared fields (incl. private/protected)
            // for (Field field : clazz.getDeclaredFields()) {
            //     System.out.println("BBBBBBBBBBB"+field);
            //     field.setAccessible(true);        // bypass visibility checks
            //     String name  = field.getName();
            //     Class<?> type = field.getType();
            //     Object value;
            //     try {
            //         value = field.get(tree);       // read the field's value
            //     } catch (IllegalAccessException e) {
            //         value = "<inaccessible>";
            //     }
            //     System.out.println("WWWWWWWWWWWWW");
            //     System.out.printf("%s (%s) = %s%n", name, type.getSimpleName(), value);
            // }
            this.discriminationTree =  tree;
            // If reusing a tree and hypothesis, populate stateInfos from the tree
            if (hyp != null && hyp.size() > 0) {
                populateStateInfosFromTree();
            }
        }
        
    }

    @Override
    public void startLearning() {
        initialize();
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Word<O>> ceQuery) {
        if (hypothesis.size() == 0) {
            throw new IllegalStateException("Not initialized");
        }
        Word<I> input = ceQuery.getInput();
        Word<O> output = ceQuery.getOutput();
        if (!refineHypothesisSingle(input, output)) {
            return false;
        }
        if (repeatedCounterexampleEvaluation) {
            while (refineHypothesisSingle(input, output)) {}
        }
        return true;
    }

    @Override
    public MealyMachine<?, I, ?, O> getHypothesisModel() {
        if (hypothesis.size() == 0) {
            throw new IllegalStateException("Not started");
        }
        return hypothesis;
    }
    public void setDiscriminationTree(MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> discriminationTree) {
        this.discriminationTree = discriminationTree;
    }
    public MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> getDiscriminationTree() {
        return discriminationTree;
    }
    public Alphabet<I> get_alphabet_symbol(){
        return this.alphabet;
    }
    private boolean refineHypothesisSingle(Word<I> input, Word<O> output) {
        int inputLen = input.length();

        if (inputLen < 2) {
            return false;
        }

        int mismatchIdx = MealyUtil.findMismatch(hypothesis, input, output);

        if (mismatchIdx == MealyUtil.NO_MISMATCH) {
            return false;
        }

        Word<I> effInput = input.prefix(mismatchIdx + 1);
        Word<O> effOutput = output.prefix(mismatchIdx + 1);

        // CRITICAL FIX: Canonicalize symbols in counterexample to match alphabet instances
        // Alphabets use object identity, not string equality, so we must use the exact symbol objects
        // from the alphabet to avoid IllegalArgumentException in counterexample analysis
        @SuppressWarnings("unchecked")
        I[] canonicalSymbols = (I[]) new Object[effInput.length()];
        for (int i = 0; i < effInput.length(); i++) {
            I symbol = effInput.getSymbol(i);
            try {
                int symbolIdx = alphabet.getSymbolIndex(symbol);
                canonicalSymbols[i] = alphabet.getSymbol(symbolIdx);
            } catch (IllegalArgumentException e) {
                System.err.println("ERROR: Symbol '" + symbol + "' from counterexample not in alphabet!");
                System.err.println("  Symbol identity hash: " + System.identityHashCode(symbol));
                System.err.println("  Alphabet size: " + alphabet.size());
                System.err.println("  Alphabet symbols:");
                for (I s : alphabet) {
                    System.err.println("    '" + s + "' (hash=" + System.identityHashCode(s) + ")");
                }
                throw e;
            }
        }
        Word<I> canonicalEffInput = Word.fromList(java.util.Arrays.asList(canonicalSymbols));

        KVAbstractCounterexample acex = new KVAbstractCounterexample(canonicalEffInput, effOutput, oracle);
        int idx = ceAnalyzer.analyzeAbstractCounterexample(acex, 0);

        Word<I> prefix = canonicalEffInput.prefix(idx);
        StateInfo<I, Word<O>> srcStateInfo = acex.getStateInfo(idx);
        I sym = canonicalEffInput.getSymbol(idx);
        LCAInfo<Word<O>, @Nullable AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> lca =
                acex.getLCA(idx + 1);
        assert lca != null;

        splitState(srcStateInfo, prefix, sym, lca);

        return true;
    }

    private void splitState(StateInfo<I, Word<O>> stateInfo,
                            Word<I> newPrefix,
                            I sym,
                            LCAInfo<Word<O>, @Nullable AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> separatorInfo) {
        int state = stateInfo.id;

        // TLongList oldIncoming = stateInfo.fetchIncoming();
        List<Long> oldIncoming = stateInfo.fetchIncoming(); // TODO: replace with primitive specialization

        StateInfo<I, Word<O>> newStateInfo = createState(newPrefix);

        AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> stateLeaf = stateInfo.dtNode;

        AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> separator = separatorInfo.leastCommonAncestor;
        Word<I> newDiscriminator;
        Word<O> oldOut, newOut;
        if (separator == null) {
            newDiscriminator = Word.fromLetter(sym);
            oldOut = separatorInfo.subtree1Label;
            newOut = separatorInfo.subtree2Label;
        } else {
            newDiscriminator = newDiscriminator(sym, separator.getDiscriminator());
            CompactMealyTransition<O> transition = hypothesis.getTransition(state, sym);
            assert transition != null;
            O transOut = hypothesis.getTransitionOutput(transition);
            oldOut = newOutcome(transOut, separatorInfo.subtree1Label);
            newOut = newOutcome(transOut, separatorInfo.subtree2Label);
        }

        final AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>.SplitResult sr =
                stateLeaf.split(newDiscriminator, oldOut, newOut, newStateInfo);

        stateInfo.dtNode = sr.nodeOld;
        newStateInfo.dtNode = sr.nodeNew;

        initState(newStateInfo);

        updateTransitions(oldIncoming, stateLeaf);
    }

    private Word<O> newOutcome(O transOutput, Word<O> succOutcome) {
        return succOutcome.prepend(transOutput);
    }

    // private void updateTransitions(TLongList transList, DTNode<I,Word<O>,StateInfo<I,O>> oldDtTarget) {
    private void updateTransitions(List<Long> transList,
                                   AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> oldDtTarget) { // TODO: replace with primitive specialization
        int numTrans = transList.size();

        final List<Word<I>> transAs = new ArrayList<>(numTrans);

        for (int i = 0; i < numTrans; i++) {
            long encodedTrans = transList.get(i);

            int sourceState = (int) (encodedTrans >> Integer.SIZE);
            int transIdx = (int) (encodedTrans);

            StateInfo<I, Word<O>> sourceInfo = stateInfos.get(sourceState);
            I symbol = alphabet.getSymbol(transIdx);

            transAs.add(sourceInfo.accessSequence.append(symbol));
        }

        final List<StateInfo<I, Word<O>>> succs = sift(Collections.nCopies(numTrans, oldDtTarget), transAs);

        for (int i = 0; i < numTrans; i++) {
            long encodedTrans = transList.get(i);

            int sourceState = (int) (encodedTrans >> Integer.SIZE);
            int transIdx = (int) (encodedTrans);

            CompactMealyTransition<O> trans = hypothesis.getTransition(sourceState, transIdx);
            assert trans != null;
            setTransition(sourceState, transIdx, succs.get(i), trans.getOutput());
        }
    }

    private Word<I> newDiscriminator(I symbol, Word<I> succDiscriminator) {
        return succDiscriminator.prepend(symbol);
    }

    private StateInfo<I, Word<O>> createInitialState() {
        int state = hypothesis.addIntInitialState();
        assert state == stateInfos.size();

        StateInfo<I, Word<O>> stateInfo = new StateInfo<>(state, Word.epsilon());
        stateInfos.add(stateInfo);

        return stateInfo;
    }

    private StateInfo<I, Word<O>> createState(Word<I> prefix) {
        int state = hypothesis.addIntState();
        assert state == stateInfos.size();

        StateInfo<I, Word<O>> stateInfo = new StateInfo<>(state, prefix);
        stateInfos.add(stateInfo);

        return stateInfo;
    }

    private void populateStateInfosFromTree() {
        int numStates = hypothesis.size();
        // Initialize the list with the correct size, filled with null values
        stateInfos = new ArrayList<>(Collections.nCopies(numStates, null));
        collectStateInfos(discriminationTree.getRoot());
    }
    
    private void collectStateInfos(AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> node) {
        if (node.isLeaf()) {
            StateInfo<I, Word<O>> stateInfo = node.getData();
            if (stateInfo != null) {
                // Add StateInfo at the index matching its state ID
                // This ensures stateInfos.get(stateId) returns the correct StateInfo
                int stateId = stateInfo.id;
                if (stateId >= 0 && stateId < stateInfos.size()) {
                    stateInfos.set(stateId, stateInfo);
                    // Restore the bidirectional link from StateInfo to tree node
                    stateInfo.dtNode = node;
                }
            }
        } else {
            Collection<Map.Entry<Word<O>, AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>>> children = node.getChildEntries();
            for (Map.Entry<Word<O>, AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> entry : children) {
                collectStateInfos(entry.getValue());
            }
        }
    }

    private void initialize() {
        // If hypothesis already has states, it means we're reusing a previous hypothesis
        // In this case, we should not create a new initial state
        if (hypothesis.size() == 0) {
            StateInfo<I, Word<O>> init = createInitialState();
            discriminationTree.getRoot().setData(init);
            init.dtNode = discriminationTree.getRoot();
            initState(init);
        }
        // If reusing a hypothesis, the discrimination tree already contains the state info
    }

    private void initState(StateInfo<I, Word<O>> stateInfo) {
        int alphabetSize = alphabet.size();

        int state = stateInfo.id;
        Word<I> accessSequence = stateInfo.accessSequence;

        final List<Word<I>> transAs = new ArrayList<>(alphabetSize);
        final List<DefaultQuery<I, Word<O>>> outputQueries = new ArrayList<>(alphabetSize);

        for (int i = 0; i < alphabetSize; i++) {
            I sym = alphabet.getSymbol(i);
            transAs.add(accessSequence.append(sym));
            outputQueries.add(new DefaultQuery<>(accessSequence, Word.fromLetter(sym)));
        }

        final List<StateInfo<I, Word<O>>> succs = sift(transAs);
        this.oracle.processQueries(outputQueries);

        for (int i = 0; i < alphabetSize; i++) {
            setTransition(state, i, succs.get(i), outputQueries.get(i).getOutput().firstSymbol());
        }
    }

    private void setTransition(int state, int symIdx, StateInfo<I, Word<O>> succInfo, O output) {
        succInfo.addIncoming(state, symIdx);
        hypothesis.setTransition(state, symIdx, succInfo.id, output);
    }
    
    /**
     * Helper method to repeat a string (Java 8 compatibility)
     */
    private String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    private List<StateInfo<I, Word<O>>> sift(List<Word<I>> prefixes) {
        return sift(Collections.nCopies(prefixes.size(), discriminationTree.getRoot()), prefixes);
    }

    private List<StateInfo<I, Word<O>>> sift(List<AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> starts,
                                             List<Word<I>> prefixes) {

        // Detailed analysis of what prefixes contain
        System.out.println("\n" + repeatString("=", 80));
        System.out.println("PREFIX ANALYSIS - SIFT METHOD");
        System.out.println(repeatString("=", 80));
        System.out.println("Total number of prefixes: " + prefixes.size());
        System.out.println();
        
        for (int i = 0; i < prefixes.size(); i++) {
            Word<I> prefix = prefixes.get(i);
            System.out.println("Prefix " + (i + 1) + ":");
            System.out.println("  - Length: " + prefix.length());
            System.out.println("  - Content: " + prefix);
            System.out.println("  - Is empty: " + prefix.isEmpty());
            System.out.println("  - String representation: '" + prefix.toString() + "'");
            
            // Show individual symbols
            if (!prefix.isEmpty()) {
                System.out.println("  - Individual symbols:");
                for (int j = 0; j < prefix.length(); j++) {
                    I symbol = prefix.getSymbol(j);
                    System.out.println("    [" + j + "] = " + symbol + " (type: " + symbol.getClass().getSimpleName() + ")");
                }
            }
            System.out.println();
        }
        
        // Summary statistics
        int totalSymbols = 0;
        int emptyPrefixes = 0;
        int maxLength = 0;
        int minLength = Integer.MAX_VALUE;
        
        for (Word<I> prefix : prefixes) {
            int len = prefix.length();
            totalSymbols += len;
            if (len == 0) emptyPrefixes++;
            if (len > maxLength) maxLength = len;
            if (len < minLength) minLength = len;
        }
        
        if (prefixes.isEmpty()) {
            minLength = 0;
        }
        
        System.out.println("SUMMARY STATISTICS:");
        System.out.println("  - Total symbols across all prefixes: " + totalSymbols);
        System.out.println("  - Empty prefixes: " + emptyPrefixes);
        System.out.println("  - Maximum prefix length: " + maxLength);
        System.out.println("  - Minimum prefix length: " + minLength);
        System.out.println("  - Average prefix length: " + (prefixes.isEmpty() ? 0 : (double) totalSymbols / prefixes.size()));
        
        System.out.println("\nWHAT ARE PREFIXES?");
        System.out.println("In the Kearns-Vazirani algorithm, 'prefixes' represent:");
        System.out.println("1. Access sequences to states in the hypothesis automaton");
        System.out.println("2. Input words that lead to specific states");
        System.out.println("3. The 'path' through the automaton to reach each state");
        System.out.println("4. Used by sift() method to navigate the discrimination tree");
        System.out.println(repeatString("=", 80) + "\n");
        
        System.out.println("===================================================>"+prefixes);
        final List<AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> leaves =
                discriminationTree.sift(starts, prefixes);
        final List<StateInfo<I, Word<O>>> result = new ArrayList<>(leaves.size());

        for (int i = 0; i < leaves.size(); i++) {
            final AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> leaf = leaves.get(i);

            StateInfo<I, Word<O>> succStateInfo = leaf.getData();
            if (succStateInfo == null) {
                // Special case: this is the *first* state of a different
                // acceptance than the initial state
                succStateInfo = createState(prefixes.get(i));
                leaf.setData(succStateInfo);
                succStateInfo.dtNode = leaf;

                initState(succStateInfo);
            }

            result.add(succStateInfo);
        }

        return result;
    }

    @Override
    public void addAlphabetSymbol(I symbol) {

        if (!this.alphabet.containsSymbol(symbol)) {
            Alphabets.toGrowingAlphabetOrThrowException(this.alphabet).addSymbol(symbol);
        }

        this.hypothesis.addAlphabetSymbol(symbol);

        // check if we already have information about the symbol (then the transition is defined) so we don't post
        // redundant queries
        if (this.hypothesis.getInitialState() != null &&
            this.hypothesis.getSuccessor(this.hypothesis.getInitialState(), symbol) == null) {
            // use new list to prevent concurrent modification exception
            final List<Word<I>> transAs = new ArrayList<>(this.stateInfos.size());
            final List<DefaultQuery<I, Word<O>>> outputQueries = new ArrayList<>(this.stateInfos.size());

            for (final StateInfo<I, Word<O>> si : this.stateInfos) {
                transAs.add(si.accessSequence.append(symbol));
                outputQueries.add(new DefaultQuery<>(si.accessSequence, Word.fromLetter(symbol)));
            }

            final List<StateInfo<I, Word<O>>> succs = sift(transAs);
            this.oracle.processQueries(outputQueries);

            final Iterator<StateInfo<I, Word<O>>> stateIter = this.stateInfos.iterator();
            final Iterator<StateInfo<I, Word<O>>> leafsIter = succs.iterator();
            final Iterator<DefaultQuery<I, Word<O>>> outputsIter = outputQueries.iterator();
            final int inputIdx = this.alphabet.getSymbolIndex(symbol);

            while (stateIter.hasNext() && leafsIter.hasNext()) {
                setTransition(stateIter.next().id,
                              inputIdx,
                              leafsIter.next(),
                              outputsIter.next().getOutput().firstSymbol());
            }
        }
    }

    @Override
    public KearnsVaziraniMealyState<I, O> suspend() {
        return new KearnsVaziraniMealyState<I, O>(hypothesis, discriminationTree, stateInfos);
    }
    
    @Override
    public void resume(final KearnsVaziraniMealyState<I, O> state) {

        this.hypothesis = state.getHypothesis();
        this.discriminationTree = state.getDiscriminationTree();
        this.discriminationTree.setOracle(oracle);
        this.stateInfos = state.getStateInfos();

    
    }   

    public static final class BuilderDefaults {

        private BuilderDefaults() {
            // prevent instantiation
        }

        public static boolean repeatedCounterexampleEvaluation() {
            return true;
        }

        public static AcexAnalyzer counterexampleAnalyzer() {
            return AcexAnalyzers.LINEAR_FWD;
        }
    }

    protected class KVAbstractCounterexample extends AbstractBaseCounterexample<Boolean> {

        private final Word<I> ceWord;
        private final MembershipOracle<I, Word<O>> oracle;
        private final StateInfo<I, Word<O>>[] states;
        private final LCAInfo<Word<O>, @Nullable AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>>[] lcas;

        @SuppressWarnings("unchecked")
        public KVAbstractCounterexample(Word<I> ceWord, Word<O> output, MembershipOracle<I, Word<O>> oracle) {
            super(ceWord.length() + 1);
            this.ceWord = ceWord;
            this.oracle = oracle;

            int m = ceWord.length();
            this.states = new StateInfo[m + 1];
            this.lcas = new LCAInfo[m + 1];

            int currState = hypothesis.getIntInitialState();
            int i = 0;
            states[i++] = stateInfos.get(currState);
            
            System.out.println("\n=== KVAbstractCounterexample: Processing ceWord ===");
            System.out.println("ceWord length: " + m);
            System.out.println("ceWord: " + ceWord);
            System.out.println("Hypothesis alphabet size: " + alphabet.size());
            System.out.println("Processing symbols:");
            
            for (I sym : ceWord) {
                System.out.println("  Symbol: '" + sym + "' (hash=" + System.identityHashCode(sym) + ")");
                System.out.println("  Current state: " + currState);
                
                try {
                    // Check if symbol is in alphabet
                    int symIdx = alphabet.getSymbolIndex(sym);
                    System.out.println("  Symbol index in alphabet: " + symIdx);
                    
                    currState = hypothesis.getSuccessor(currState, sym);
                    System.out.println("  Next state: " + currState);
                    states[i++] = stateInfos.get(currState);
                } catch (IllegalArgumentException e) {
                    System.err.println("  ERROR: Symbol '" + sym + "' not found in alphabet!");
                    System.err.println("  Alphabet symbols:");
                    for (I s : alphabet) {
                        System.err.println("    '" + s + "' (hash=" + System.identityHashCode(s) + ")");
                    }
                    throw e;
                }
            }
            System.out.println("=== KVAbstractCounterexample: Done ===\n");

            // Output of last transition separates hypothesis from target
            O lastHypOut = hypothesis.getOutput(states[m - 1].id, ceWord.lastSymbol());
            lcas[m] = new LCAInfo<>(null, Word.fromLetter(lastHypOut), Word.fromLetter(output.lastSymbol()));
            super.setEffect(m, false);
        }

        public StateInfo<I, Word<O>> getStateInfo(int idx) {
            return states[idx];
        }

        public LCAInfo<Word<O>, @Nullable AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> getLCA(int idx) {
            return lcas[idx];
        }

        @Override
        protected Boolean computeEffect(int index) {
            System.out.println("\n=== computeEffect: index=" + index + " ===");
            Word<I> prefix = ceWord.prefix(index);
            System.out.println("Prefix: " + prefix + " (length=" + prefix.length() + ")");
            
            StateInfo<I, Word<O>> info = states[index];
            System.out.println("StateInfo: " + (info != null ? info.id : "null"));

            // Save the expected outcomes on the path from the leaf representing the state
            // to the root on a stack
            AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> node = info.dtNode;
            Deque<Word<O>> expect = new ArrayDeque<>();
            while (!node.isRoot()) {
                Word<O> parentOutcome = node.getParentOutcome();
                assert parentOutcome != null;
                expect.push(parentOutcome);
                node = node.getParent();
            }
            System.out.println("Expected outcomes collected: " + expect.size());

            AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> currNode = discriminationTree.getRoot();

            int queryCount = 0;
            while (!expect.isEmpty()) {
                Word<I> suffix = currNode.getDiscriminator();
                System.out.println("  Query " + (++queryCount) + ": prefix=" + prefix + ", suffix=" + suffix);
                System.out.println("    Prefix symbols:");
                for (int i = 0; i < prefix.length(); i++) {
                    I sym = prefix.getSymbol(i);
                    System.out.println("      [" + i + "] '" + sym + "' (hash=" + System.identityHashCode(sym) + ")");
                }
                System.out.println("    Suffix symbols:");
                for (int i = 0; i < suffix.length(); i++) {
                    I sym = suffix.getSymbol(i);
                    System.out.println("      [" + i + "] '" + sym + "' (hash=" + System.identityHashCode(sym) + ")");
                }
                
                try {
                    Word<O> out = oracle.answerQuery(prefix, suffix);
                    System.out.println("    Result: " + out);
                    Word<O> e = expect.pop();
                    if (!Objects.equals(out, e)) {
                        lcas[index] = new LCAInfo<>(currNode, e, out);
                        System.out.println("  Mismatch found, returning false");
                        return false;
                    }
                    currNode = currNode.child(out);
                } catch (Exception ex) {
                    System.err.println("  ERROR during oracle query!");
                    System.err.println("  Exception: " + ex.getClass().getName() + ": " + ex.getMessage());
                    throw ex;
                }
            }

            System.out.println("=== computeEffect: returning true ===\n");
            assert currNode.isLeaf() && expect.isEmpty();
            return true;
        }

        @Override
        public boolean checkEffects(Boolean eff1, Boolean eff2) {
            return !eff1 || eff2;
        }
    }
}
