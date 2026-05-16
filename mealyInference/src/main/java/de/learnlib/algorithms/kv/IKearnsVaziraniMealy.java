/* Copyright (C) 2013-2021 TU Dortmund
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import de.learnlib.datastructure.discriminationtree.iterators.DiscriminationTreeIterators;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import de.learnlib.datastructure.discriminationtree.model.LCAInfo;
import de.learnlib.util.mealy.MealyUtil;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.automata.transducers.impl.compact.CompactMealyTransition;
import net.automatalib.commons.smartcollections.ArrayStorage;
//  import de.learnlib.ds.StateInfo;
 import de.learnlib.algorithms.kv.StateInfo;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Incremental Kearns/Vazirani algorithm for learning Mealy machines with tree minimization
 * and hypothesis rebuilding capabilities for adaptive learning across product variants.
 * 
 * Based on the ideas from "Tree-Based Adaptive Model Learning" and adapted from IKearnsVaziraniDFA.
 *
 * @param <I> input symbol type
 * @param <O> output symbol type
 *
 * @author Adapted from IKearnsVaziraniDFA
 */
public class IKearnsVaziraniMealy<I, O> extends KearnsVaziraniMealy<I, O> {

    /**
     * Constructor for incremental learning with starting state.
     *
     * @param alphabet the learning alphabet
     * @param oracle   the membership oracle
     * @param counterexampleAnalyzer the counterexample analyzer
     * @param startingTree the discrimination tree from previous product
     * @param startingHypothesis the hypothesis from previous product
     */
    public IKearnsVaziraniMealy(Alphabet<I> alphabet, 
                                MembershipOracle<I, Word<O>> oracle,
                                boolean repeatedCounterexampleEvaluation,
                                AcexAnalyzer counterexampleAnalyzer, 
                                MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> startingTree,
                                CompactMealy<I, O> startingHypothesis) {
        super(alphabet, oracle, repeatedCounterexampleEvaluation, counterexampleAnalyzer,startingTree, startingHypothesis);
     
        if (startingTree != null) {
            super.discriminationTree = startingTree;
            super.discriminationTree.setOracle(oracle);
        }
        
        if (startingHypothesis != null) {
            super.hypothesis = startingHypothesis;
            // Populate stateInfos from tree if needed
            if (super.stateInfos == null || super.stateInfos.isEmpty()) {
                populateStateInfosFromTree();
            }
        }
    }

    /**
     * Populate stateInfos list from discrimination tree leaves
     */
    public void populateStateInfosFromTree() {
        if (super.stateInfos == null) {
            super.stateInfos = new ArrayList<>();
        }
        super.stateInfos.clear();
        
        Iterator<AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> leafIt = 
            DiscriminationTreeIterators.leafIterator(discriminationTree.getRoot());
        
        while (leafIt.hasNext()) {
            AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> leaf = leafIt.next();
            StateInfo<I, Word<O>> stateInfo = leaf.getData();
            if (stateInfo != null) {
                super.stateInfos.add(stateInfo);
            }
        }
    }

    @Override
    public void startLearning() {
        // Check if we're starting from scratch or incrementally
        if (discriminationTree.getRoot().isLeaf() || 
            (hypothesis != null && hypothesis.size() == 0)) {
            // Start from scratch - no previous knowledge
            super.startLearning();
        } else {
            // Incremental learning - optimize existing tree
            initialize();
        }
    }

    /**
     * Initialize for incremental learning by minimizing the tree
     * and rebuilding the hypothesis to match the new product's alphabet.
     */
    private void initialize() {
        System.out.println("\n========== IKearnsVaziraniMealy: Initializing Incremental Learning ==========");
        // System.out.println("Discrimination tree nodes before minimization: " + 
        //     DiscriminationTreeIterators.nodeIterator(discriminationTree.getRoot()).toStream().count());
        System.out.println("Hypothesis states before minimization: " + (hypothesis != null ? hypothesis.size() : 0));
        
        // Minimize the tree to optimize for the new product
        minimiseTree();
        
        System.out.println("\nAfter minimization:");
        // System.out.println("Discrimination tree nodes: " + 
        //     DiscriminationTreeIterators.nodeIterator(discriminationTree.getRoot()).toStream().count());
        System.out.println("Hypothesis states: " + hypothesis.size());
        
        // Verify tree consistency
        DiscriminationTreeIterators.leafIterator(discriminationTree.getRoot()).forEachRemaining(leaf -> {
            StateInfo<I, Word<O>> data = leaf.getData();
            if (data != null) {
                assert data.dtNode.equals(leaf) : "StateInfo dtNode mismatch!";
                assert stateInfos.contains(data) : "StateInfo not in stateInfos list!";
            }
        });
        
        System.out.println("========== Initialization Complete ==========\n");
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
        
        // Note: repeatedCounterexampleEvaluation is handled by parent's field
        // We use reflection or direct access through protected field
        return true;
    }

    /**
     * Refine hypothesis with a single counterexample.
     * Adapted from IKearnsVaziraniDFA for Mealy machines.
     */
    private boolean refineHypothesisSingle(Word<I> input, Word<O> output) {
        int inputLen = input.length();

        if (inputLen < 2) {
            // Handle short counterexamples specially
            if (inputLen == 1) {
                handleShortCounterexample(input, output);
                return true;
            }
            return false;
        }

        int mismatchIdx = MealyUtil.findMismatch(hypothesis, input, output);
        
        if (mismatchIdx == MealyUtil.NO_MISMATCH) {
            return false;
        }

        Word<I> effInput = input.prefix(mismatchIdx + 1);
        Word<O> effOutput = output.prefix(mismatchIdx + 1);

        // Canonicalize symbols to match alphabet instances
        @SuppressWarnings("unchecked")
        I[] canonicalSymbols = (I[]) new Object[effInput.length()];
        for (int i = 0; i < effInput.length(); i++) {
            I symbol = effInput.getSymbol(i);
            int symbolIdx = alphabet.getSymbolIndex(symbol);
            canonicalSymbols[i] = alphabet.getSymbol(symbolIdx);
        }
        Word<I> canonicalEffInput = Word.fromList(java.util.Arrays.asList(canonicalSymbols));

        // Create abstract counterexample using parent's inner class
        KVAbstractCounterexample acex = new KVAbstractCounterexample(canonicalEffInput, effOutput, getOracle());
        int idx = getCeAnalyzer().analyzeAbstractCounterexample(acex, 0);

        Word<I> prefix = canonicalEffInput.prefix(idx);
        StateInfo<I, Word<O>> srcStateInfo = acex.getStateInfo(idx);
        I sym = canonicalEffInput.getSymbol(idx);
        LCAInfo<Word<O>, @Nullable AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> lca = acex.getLCA(idx + 1);
        
        assert lca != null : "LCA should not be null";

        splitState(srcStateInfo, prefix, sym, lca);

        return true;
    }

    /**
     * Handle counterexamples of length 1 (single symbol).
     */
    private void handleShortCounterexample(Word<I> input, Word<O> output) {
        // For short counterexamples, we need to update the transition from initial state
        StateInfo<I, Word<O>> initState = stateInfos.get(hypothesis.getIntInitialState());
        I sym = input.getSymbol(0);
        
        // Sift to find the correct target state
        List<StateInfo<I, Word<O>>> newDests = sift(Collections.singletonList(input));
        StateInfo<I, Word<O>> newDest = newDests.get(0);
        
        // Get old transition info (for reference only)
        int symIdx = alphabet.getSymbolIndex(sym);
        
        // Set new transition (this will automatically update incoming/outgoing edges)
        setTransition(initState.id, symIdx, newDest, output.firstSymbol());
        
        assert hypothesis.computeOutput(input).equals(output) : "Output mismatch after short CE handling";
    }
    /**
     * Minimize the discrimination tree by removing redundant leaves and rebuilding hypothesis.
     * This is the core optimization for adaptive learning.
     */
    private void minimiseTree() {
        System.out.println("\n>>> Starting Tree Minimization...");
        int iterations = 0;
        boolean hasRemovedLeaf = true;
        
        while (hasRemovedLeaf) {
            iterations++;
            hasRemovedLeaf = false;
            Iterator<AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> nodeIt = 
                DiscriminationTreeIterators.leafIterator(discriminationTree.getRoot());

            while (nodeIt.hasNext()) {
                AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> currentNode = nodeIt.next();
                StateInfo<I, Word<O>> currentData = currentNode.getData();
                
                if (currentData != null) {
                    // Sift the access sequence to see where it should go
                    AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> siftedNode = 
                        discriminationTree.sift(currentData.accessSequence);
                    
                    if (!currentData.dtNode.equals(siftedNode)) {
                        hasRemovedLeaf = true;
                        System.out.println("  Iteration " + iterations + ": Found misplaced leaf, relocating...");
                        
                        if (siftedNode.getData() == null) {
                            // Target leaf is empty - move data there
                            siftedNode.setData(currentData);
                            currentData.dtNode = siftedNode;
                            currentNode.setData(null);
                        } else {
                            // Target leaf already has data - merge states
                            // Keep epsilon access sequence if present
                            // if (currentData.accessSequence.isEmpty()) {
                            //     siftedNode.getData().accessSequence = Word.epsilon();
                            // }
                            removeLeaf(currentNode);
                        }
                        break; // Restart iteration after modification
                    }
                }
            }
        }
        
        System.out.println("Tree minimization completed in " + iterations + " iterations");
        
        // Rebuild hypothesis to match the minimized tree and new alphabet
        Set<Integer> removedStates = new HashSet<>();
        // Collect all state IDs that are no longer in the tree
        for (int i = 0; i < stateInfos.size(); i++) {
            StateInfo<I, Word<O>> info = stateInfos.get(i);
            if (info != null && !isStateInTree(info)) {
                removedStates.add(info.id);
            }
        }
        
        rebuildHypothesis(removedStates);
    }

    /**
     * Check if a state is still present in the discrimination tree.
     */
    private boolean isStateInTree(StateInfo<I, Word<O>> stateInfo) {
        Iterator<AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> leafIt = 
            DiscriminationTreeIterators.leafIterator(discriminationTree.getRoot());
        
        while (leafIt.hasNext()) {
            AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> leaf = leafIt.next();
            if (leaf.getData() != null && leaf.getData().equals(stateInfo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove a leaf from the discrimination tree.
     * Adapted from IKearnsVaziraniDFA for Mealy machine discriminators.
     */
    private void removeLeaf(AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> leaf) {
        AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> parentNode = leaf.getParent();
        
        if (parentNode == null) {
            // Cannot remove root
            return;
        }
        
        AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> siblingNode = 
            parentNode.getChild(leaf.getParentOutcome() == null ? null : 
                findSiblingOutcome(parentNode, leaf.getParentOutcome()));

        if (siblingNode == null) {
            // No sibling to promote
            leaf.setData(null);
            return;
        }

        if (!parentNode.isRoot() || !parentNode.getDiscriminator().isEmpty()) {
            if (siblingNode.isLeaf()) {
                // Sibling is a leaf (children == null); promote its state to parent
                StateInfo<I, Word<O>> siblingData = siblingNode.getData();
                leaf.setData(null);
                parentNode.replaceChildren(null);
                parentNode.setDiscriminator(siblingNode.getDiscriminator());
                if (siblingData != null) {
                    parentNode.setData(siblingData);
                    siblingData.dtNode = parentNode;
                }
            } else {
                Map<Word<O>, AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> childMap =
                    siblingNode.getChildEntries().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                parentNode.replaceChildren(childMap);
                parentNode.setDiscriminator(siblingNode.getDiscriminator());
                parentNode.setData(null);
            }
        } else {
            // Root with epsilon discriminator - just clear data
            leaf.setData(null);
        }
    }

    /**
     * Find the sibling outcome (the other outcome that's not the given one).
     */
    private Word<O> findSiblingOutcome(AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> parent, 
                                      Word<O> childOutcome) {
        for (Map.Entry<Word<O>, AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> entry : 
             parent.getChildEntries()) {
            if (!entry.getKey().equals(childOutcome)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Rebuild hypothesis from the minimized discrimination tree.
     * This ensures all transitions are correct for the new alphabet.
     */
    private void rebuildHypothesis(Set<Integer> idsRemoved) {
        System.out.println("\n>>> Rebuilding Hypothesis...");
        System.out.println("States removed: " + idsRemoved.size());
        
        // Save old state mapping
        Map<Integer, StateInfo<I, Word<O>>> oldIds = new HashMap<>();
        for (int i = 0; i < stateInfos.size(); i++) {
            StateInfo<I, Word<O>> info = stateInfos.get(i);
            if (info != null) {
                oldIds.put(info.id, info);
            }
        }
        
        Map<StateInfo<I, Word<O>>, Integer> oldStateInfos = oldIds.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        stateInfos.clear();

        // Create new hypothesis with current alphabet
        CompactMealy<I, O> newhyp = new CompactMealy<>(alphabet);
        
        // Collect all states from tree leaves
        Iterator<AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> leafIt = 
            DiscriminationTreeIterators.leafIterator(discriminationTree.getRoot());
        
        int newStateCount = 0;
        Map<StateInfo<I, Word<O>>, StateInfo<I, Word<O>>> oldToNewStateInfo = new HashMap<>();
        
        while (leafIt.hasNext()) {
            AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> node = leafIt.next();
            StateInfo<I, Word<O>> oldStateInfo = node.getData();
            
            if (oldStateInfo != null) {
                int newId = newhyp.addIntState();
                StateInfo<I, Word<O>> newStateInfo = new StateInfo<>(newId, oldStateInfo.accessSequence);
                newStateInfo.dtNode = node;
                node.setData(newStateInfo);
                stateInfos.add(newStateInfo);
                oldToNewStateInfo.put(oldStateInfo, newStateInfo);
                newStateCount++;
                
                if (newStateInfo.accessSequence.isEmpty()) {
                    newhyp.setInitialState(newId);
                    System.out.println("  Initial state: " + newId);
                }
            }
        }
        
        System.out.println("New hypothesis has " + newStateCount + " states");

        // Build transitions for all states
        List<StateInfo<I, Word<O>>> statesList = new LinkedList<>(stateInfos);
        int transitionsAdded = 0;
        
        for (int i = 0; i < statesList.size(); i++) {
            StateInfo<I, Word<O>> newState = statesList.get(i);
            
            for (int symIdx = 0; symIdx < alphabet.size(); symIdx++) {
                I sym = alphabet.getSymbol(symIdx);
                Word<I> transAS = newState.accessSequence.append(sym);
                
                // Try to reuse old transition if available
                if (oldStateInfos.containsKey(newState) && !idsRemoved.contains(oldStateInfos.get(newState))) {
                    CompactMealyTransition<O> oldTrans = hypothesis.getTransition(oldStateInfos.get(newState), sym);
                    
                    if (oldTrans != null && !idsRemoved.contains(oldTrans.getSuccId())) {
                        StateInfo<I, Word<O>> oldTarget = oldIds.get(oldTrans.getSuccId());
                        if (oldTarget != null && stateInfos.contains(oldTarget)) {
                            // Reuse old transition
                            newhyp.addTransition(newState.id, sym, oldTarget.id, oldTrans.getOutput());
                            transitionsAdded++;
                            continue;
                        }
                    }
                }
                
                // Sift to find new target state
                int oldStateCount = stateInfos.size();
                StateInfo<I, Word<O>> newTransState = sift(Collections.singletonList(transAS)).get(0);
                
                if (stateInfos.size() != oldStateCount) {
                    // New state was created during sift
                    statesList.add(newTransState);
                }
                
                // Query oracle for output
                Word<O> output = getOracle().answerQuery(newState.accessSequence, Word.fromLetter(sym));
                newhyp.addTransition(newState.id, sym, newTransState.id, output.firstSymbol());
                transitionsAdded++;
            }
        }
        
        System.out.println("Added " + transitionsAdded + " transitions");

        // Replace old hypothesis
        hypothesis = newhyp;

        // Rebuild incoming transition tracking
        for (StateInfo<I, Word<O>> state : stateInfos) {
            state.fetchIncoming(); // Clears incoming list as side effect
        }

        for (StateInfo<I, Word<O>> state : stateInfos) {
            for (int symIdx = 0; symIdx < alphabet.size(); symIdx++) {
                CompactMealyTransition<O> trans = hypothesis.getTransition(state.id, symIdx);
                if (trans != null) {
                    stateInfos.get(trans.getSuccId()).addIncoming(state.id, symIdx);
                }
            }
        }
        
        System.out.println(">>> Hypothesis Rebuild Complete\n");
    }

    /**
     * Split a state in the discrimination tree based on a counterexample.
     */
    private void splitState(StateInfo<I, Word<O>> stateInfo, 
                           Word<I> newPrefix, 
                           I sym,
                           LCAInfo<Word<O>, @Nullable AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> separatorInfo) {
        int state = stateInfo.id;
        List<Long> oldIncoming = stateInfo.fetchIncoming();

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

        AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>.SplitResult sr = 
            stateLeaf.split(newDiscriminator, oldOut, newOut, newStateInfo);

        stateInfo.dtNode = sr.nodeOld;
        newStateInfo.dtNode = sr.nodeNew;

        initState(newStateInfo);
        updateTransitions(oldIncoming, stateLeaf);
    }

    /**
     * Update transitions after a state split.
     */
    private void updateTransitions(List<Long> transList,
                                  AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> oldDtTarget) {
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

    private Word<O> newOutcome(O transOutput, Word<O> succOutcome) {
        return succOutcome.prepend(transOutput);
    }

    private StateInfo<I, Word<O>> createState(Word<I> prefix) {
        int state = hypothesis.addIntState();
        StateInfo<I, Word<O>> stateInfo = new StateInfo<>(state, prefix);
        stateInfos.add(stateInfo);
        return stateInfo;
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
        getOracle().processQueries(outputQueries);

        for (int i = 0; i < alphabetSize; i++) {
            setTransition(state, i, succs.get(i), outputQueries.get(i).getOutput().firstSymbol());
        }
    }

    private void setTransition(int state, int symIdx, StateInfo<I, Word<O>> succInfo, O output) {
        succInfo.addIncoming(state, symIdx);
        hypothesis.setTransition(state, symIdx, succInfo.id, output);
    }

    private List<StateInfo<I, Word<O>>> sift(List<Word<I>> prefixes) {
        return sift(Collections.nCopies(prefixes.size(), discriminationTree.getRoot()), prefixes);
    }

    private List<StateInfo<I, Word<O>>> sift(List<AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> starts,
                                            List<Word<I>> prefixes) {
        final List<AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>>> leaves = 
            discriminationTree.sift(starts, prefixes);
        final ArrayStorage<StateInfo<I, Word<O>>> result = new ArrayStorage<>(leaves.size());

        for (int i = 0; i < leaves.size(); i++) {
            final AbstractWordBasedDTNode<I, Word<O>, StateInfo<I, Word<O>>> leaf = leaves.get(i);

            StateInfo<I, Word<O>> succStateInfo = leaf.getData();
            if (succStateInfo == null) {
                succStateInfo = createState(prefixes.get(i));
                leaf.setData(succStateInfo);
                succStateInfo.dtNode = leaf;
                initState(succStateInfo);
            }

            result.set(i, succStateInfo);
        }

        return result;
    }

    // Accessor methods to reach parent's protected fields
    protected MembershipOracle<I, Word<O>> getOracle() {
        try {
            java.lang.reflect.Field field = KearnsVaziraniMealy.class.getDeclaredField("oracle");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            MembershipOracle<I, Word<O>> oracle = (MembershipOracle<I, Word<O>>) field.get(this);
            return oracle;
        } catch (Exception e) {
            throw new RuntimeException("Failed to access oracle field", e);
        }
    }

    protected AcexAnalyzer getCeAnalyzer() {
        try {
            java.lang.reflect.Field field = KearnsVaziraniMealy.class.getDeclaredField("ceAnalyzer");
            field.setAccessible(true);
            return (AcexAnalyzer) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access ceAnalyzer field", e);
        }
    }
}

