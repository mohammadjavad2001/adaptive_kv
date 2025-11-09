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


import java.util.ArrayList;
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
import de.learnlib.incremental.KearnsVaziraniDFA;
import de.learnlib.api.oracle.MembershipOracle;




import de.learnlib.api.query.DefaultQuery;
import de.learnlib.datastructure.discriminationtree.iterators.DiscriminationTreeIterators;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import de.learnlib.datastructure.discriminationtree.model.LCAInfo;
import de.learnlib.incremental.KearnsVaziraniDFAState;
// import net.automatalib.alphabets.Alphabet;
import net.automatalib.words.Alphabet;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;

import de.learnlib.algorithms.kv.StateInfo;
import net.automatalib.commons.smartcollections.ArrayStorage;

import net.automatalib.commons.util.Pair;


import net.automatalib.words.Word;

/**
 * The Kearns/Vazirani algorithm for learning DFA, as described in the book "An
 * Introduction to Computational Learning Theory" by Michael Kearns and Umesh
 * Vazirani.
 *
 * @param <I> input symbol type
 *
 * @author Malte Isberner
 */

 import de.learnlib.algorithms.kv.GenerateBuilder;

public class IKearnsVaziraniDFA<I> extends KearnsVaziraniDFA<I> {

    /**
     * Constructor.
     *
     * @param alphabet the learning alphabet
     * @param oracle   the membership oracle
     */
    @GenerateBuilder
    public IKearnsVaziraniDFA(Alphabet<I> alphabet, MembershipOracle<I, Boolean> oracle,
            AcexAnalyzer counterexampleAnalyzer, KearnsVaziraniDFAState<I> startingState) {
                
        super(alphabet, oracle, true, counterexampleAnalyzer);
        super.discriminationTree = startingState.getDiscriminationTree();
        super.discriminationTree.setOracle(oracle);
        super.hypothesis = startingState.getHypothesis();
        super.stateInfos = startingState.getStateInfos();
          
    }

    @Override
    public void startLearning() {
        if (discriminationTree.getNodes().size() == 1) {
            super.startLearning();
        } else {
            initialize();
        }
    }

    private void initialize() {
        // Minimising the tree at the start allows us the make the tree smaller,
        // limiting sift depth.
        minimiseTree();
        DiscriminationTreeIterators.leafIterator(discriminationTree.getRoot()).forEachRemaining(l -> {
            assert l.getData().dtNode.equals(l);
            assert stateInfos.contains(l.getData());
        });
    }

    @Override
    public boolean refineHypothesis(DefaultQuery<I, Boolean> ceQuery) {
        if (hypothesis.size() == 0) {
            throw new IllegalStateException("Not initialized");
        }
        Word<I> input = ceQuery.getInput();
        boolean output = ceQuery.getOutput();
        if (!refineHypothesisSingle(input, output)) {
            return false;
        }
        if (repeatedCounterexampleEvaluation) {
            while (refineHypothesisSingle(input, output)) {
                
            }
        }

        return true;
    }

    private boolean refineHypothesisSingle(Word<I> input, boolean output) {
        int inputLen = input.length();

        if (hypothesis.accepts(input) == output) {
            return false;
        }

        if (inputLen < 2) {
            StateInfo<I, Boolean> startState = sift(Collections.singletonList(Word.epsilon()), hypothesis).get(0);
            StateInfo<I, Boolean> newDest = sift(Collections.singletonList(input), hypothesis).get(0);
            StateInfo<I, Boolean> startStateInfo = sift(Collections.singletonList(Word.epsilon()), hypothesis).get(0);
            newDest.addIncoming(startStateInfo.id, alphabet.getSymbolIndex(input.getSymbol(0)));
            hypothesis.removeAllTransitions(startState.id, input.getSymbol(0));
            hypothesis.addTransition(startState.id, input.getSymbol(0), newDest.id);
            assert hypothesis.computeOutput(input) == output;
            return true;
        }

        KVAbstractCounterexample acex = new KVAbstractCounterexample(input, output, getOracle());
        int idx = ceAnalyzer.analyzeAbstractCounterexample(acex, 1);

        Word<I> prefix = input.prefix(idx);
        StateInfo<I, Boolean> srcStateInfo = acex.getStateInfo(idx);
        I sym = input.getSymbol(idx);
        LCAInfo<Boolean, AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>> lca = acex.getLCA(idx + 1);
        assert lca != null;

        splitState(srcStateInfo, prefix, sym, lca);

        return true;
    }

    private void minimiseTree() {
        boolean hasRemovedLeaf = true;
        while (hasRemovedLeaf) {
            hasRemovedLeaf = false;
            Iterator<AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>> nodeIt = DiscriminationTreeIterators
                    .leafIterator(discriminationTree.getRoot());

            while (nodeIt.hasNext()) {
                AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> currentNode = nodeIt.next();
                if (currentNode.getData() != null) {
                    AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> siftedNode = discriminationTree
                            .sift(currentNode.getData().accessSequence);
                    if (!currentNode.getData().dtNode.equals(siftedNode)) {
                        hasRemovedLeaf = true;
                        if (siftedNode.getData() == null) {
                            siftedNode.setData(currentNode.getData());
                            siftedNode.getData().dtNode = siftedNode;
                            currentNode.setData(null);
                        } else {
                            removeLeaf(currentNode);
                        }
                        break;
                    }
                }
            }
        }
        
        // To maintain Isberner 2014, I3, we need to make sure transitions to remaining
        // states
        // are correct from the beginning w.r.t the new target. As such, recompute every
        // transition.

        rebuildHypothesis(new HashSet<>(stateInfos.stream().map(si -> si.id).collect(Collectors.toSet())));
    }

    private void removeLeaf(AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> leaf) {
        AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> parentNode = leaf.getParent();
        AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> siblingNode = parentNode
                .getChild(!leaf.getParentOutcome());

        if (parentNode.getDiscriminator().getClass() != Word.epsilon().getClass()) {
            Map<Boolean, AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>> childMap = 
                siblingNode.getChildEntries().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            parentNode.replaceChildren(childMap);

            parentNode.setDiscriminator(siblingNode.getDiscriminator());
            if (siblingNode.isLeaf()) {
                parentNode.setData(siblingNode.getData());
            } else {
                parentNode.setData(null);
            }
            if (parentNode.isLeaf() && parentNode.getData() != null) {
                parentNode.getData().dtNode = parentNode;
            }
        } else {
            leaf.setData(null);
        }
    }

    private void rebuildHypothesis(Set<Integer> idsRemoved) {
        Map<Integer, StateInfo<I, Boolean>> oldIds = stateInfos.stream()
            .collect(Collectors.toMap(si -> si.id, si -> si));
        Map<StateInfo<I, Boolean>, Integer> oldStateInfos = oldIds.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        stateInfos.clear();

        CompactDFA<I> newhyp = new CompactDFA<>(alphabet);
        Iterator<AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>> acceptingIt = DiscriminationTreeIterators
                .leafIterator(discriminationTree.getRoot().getChild(true));
        while (acceptingIt.hasNext()) {
            AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> node = acceptingIt.next();
            if (node.getData() != null) {
                StateInfo<I, Boolean> oldInfo = node.getData();
                int newId = newhyp.addIntState(true);
                StateInfo<I, Boolean> newInfo = new StateInfo<>(newId, oldInfo.accessSequence);
                newInfo.dtNode = node;
                node.setData(newInfo);
                stateInfos.add(newInfo);
                if (oldInfo.accessSequence.getClass() == Word.epsilon().getClass()) {
                    newhyp.setInitialState(newId);
                }
            }
        }

        Iterator<AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>> rejectingIt = DiscriminationTreeIterators
                .leafIterator(discriminationTree.getRoot().getChild(false));
        while (rejectingIt.hasNext()) {
            AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> node = rejectingIt.next();
            if (node.getData() != null) {
                StateInfo<I, Boolean> oldInfo = node.getData();
                int newId = newhyp.addIntState(false);
                StateInfo<I, Boolean> newInfo = new StateInfo<>(newId, oldInfo.accessSequence);
                newInfo.dtNode = node;
                node.setData(newInfo);
                stateInfos.add(newInfo);
                if (oldInfo.accessSequence.getClass() == Word.epsilon().getClass()) {
                    newhyp.setInitialState(newId);
                }
            }
        }

        List<StateInfo<I, Boolean>> statesList = new LinkedList<>(stateInfos);
        for (int i = 0; i < statesList.size(); i++) {
            StateInfo<I, Boolean> newState = statesList.get(i);
            for (I sym : alphabet) {
                if (oldStateInfos.containsKey(newState)) {
                    Integer transState = hypothesis.getTransition(oldStateInfos.get(newState), sym);
                    if (transState != null) {
                        if (!idsRemoved.contains(transState)) {
                            newhyp.addTransition(newState.id, sym, oldIds.get(transState).id);
                        } else {
                            Word<I> transAS = newState.accessSequence.append(sym);
                            int oldStateCount = stateInfos.size();
                            StateInfo<I, Boolean> newTransState = sift(Collections.singletonList(transAS), newhyp)
                                    .get(0);
                            if (stateInfos.size() != oldStateCount) {
                                statesList.add(newTransState);
                            }
                            newhyp.addTransition(newState.id, sym, newTransState.id);
                        }
                    }
                } else {
                    // This code is messy and not DRY.
                    Word<I> transAS = newState.accessSequence.append(sym);
                    // TODO: Sifting can get expensive quite quickly.
                    int oldStateCount = stateInfos.size();
                    StateInfo<I, Boolean> newTransState = sift(Collections.singletonList(transAS), newhyp).get(0);
                    if (stateInfos.size() != oldStateCount) {
                        statesList.add(newTransState);
                    }
                    newhyp.addTransition(newState.id, sym, newTransState.id);
                }

            }

        }

        hypothesis = newhyp;

        for (StateInfo<I, Boolean> state : stateInfos) {
            state.fetchIncoming(); // Clears incoming list as side effect
        }

        for (StateInfo<I, Boolean> state : stateInfos) {
            for (I sym : alphabet) {
                int symIdx = alphabet.getSymbolIndex(sym);
                stateInfos.get(hypothesis.getTransition(state.id, sym)).addIncoming(state.id, symIdx);
            }
        }
    }

    private void splitState(StateInfo<I, Boolean> stateInfo, Word<I> newPrefix, I sym,
            LCAInfo<Boolean, AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>> separatorInfo) {
        int state = stateInfo.id;
        boolean oldAccepting = hypothesis.isAccepting(state);
        List<Long> oldIncoming = stateInfo.fetchIncoming();

        StateInfo<I, Boolean> newStateInfo = createState(newPrefix, oldAccepting, hypothesis);

        AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> stateLeaf = stateInfo.dtNode;

        AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> separator = separatorInfo.leastCommonAncestor;
        Word<I> newDiscriminator = newDiscriminator(sym, separator.getDiscriminator());

        AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>.SplitResult sr = stateLeaf.split(newDiscriminator,
                separatorInfo.subtree1Label, separatorInfo.subtree2Label, newStateInfo);

        stateInfo.dtNode = sr.nodeOld;
        newStateInfo.dtNode = sr.nodeNew;

        initState(newStateInfo);

        updateTransitions(oldIncoming, stateLeaf);
    }

    private void updateTransitions(List<Long> transList,
            AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> oldDtTarget) {
        int numTrans = transList.size();
        final List<Word<I>> transAs = new ArrayList<>(numTrans);

        for (int i = 0; i < numTrans; i++) {
            long encodedTrans = transList.get(i);
            int sourceState = (int) (encodedTrans >> Integer.SIZE);
            int transIdx = (int) (encodedTrans);

            StateInfo<I, Boolean> sourceInfo = stateInfos.get(sourceState);
            I symbol = alphabet.getSymbol(transIdx);
            transAs.add(sourceInfo.accessSequence.append(symbol));
        }

        final List<StateInfo<I, Boolean>> succs = sift(Collections.nCopies(numTrans, oldDtTarget), transAs,
                hypothesis);

        for (int i = 0; i < numTrans; i++) {
            long encodedTrans = transList.get(i);
            int sourceState = (int) (encodedTrans >> Integer.SIZE);
            int transIdx = (int) (encodedTrans);

            setTransition(sourceState, transIdx, succs.get(i));
        }
    }

    private Word<I> newDiscriminator(I symbol, Word<I> succDiscriminator) {
        return succDiscriminator.prepend(symbol);
    }

    private StateInfo<I, Boolean> createState(Word<I> accessSequence, boolean accepting, CompactDFA<I> currentHyp) {
        int state = currentHyp.addIntState(accepting);
        StateInfo<I, Boolean> si = new StateInfo<>(state, accessSequence);
        // No longer true as old IDs may be removed.
        // assert stateInfos.size() == state;
        stateInfos.add(si);

        return si;
    }

    private void initState(StateInfo<I, Boolean> stateInfo) {
        int alphabetSize = alphabet.size();

        Word<I> accessSequence = stateInfo.accessSequence;

        final ArrayStorage<Word<I>> transAs = new ArrayStorage<>(alphabetSize);

        for (int i = 0; i < alphabetSize; i++) {
            I sym = alphabet.getSymbol(i);
            transAs.set(i, accessSequence.append(sym));
        }

        final List<StateInfo<I, Boolean>> succs = sift(transAs, hypothesis);

        for (int i = 0; i < alphabetSize; i++) {
            setTransition(stateInfo.id, i, succs.get(i));
        }
    }

    private void setTransition(int state, int symIdx, StateInfo<I, Boolean> succInfo) {
        succInfo.addIncoming(state, symIdx);
        hypothesis.setTransition(state, symIdx, succInfo.id);
    }

    private List<StateInfo<I, Boolean>> sift(List<Word<I>> prefixes, CompactDFA<I> currentHyp) {
        return sift(Collections.nCopies(prefixes.size(), discriminationTree.getRoot()), prefixes, currentHyp);
    }

    private List<StateInfo<I, Boolean>> sift(List<AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>> starts,
            List<Word<I>> prefixes, CompactDFA<I> currentHyp) {

        final List<AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>> leaves = discriminationTree.sift(starts,
                prefixes);
        final ArrayStorage<StateInfo<I, Boolean>> result = new ArrayStorage<>(leaves.size());

        for (int i = 0; i < leaves.size(); i++) {
            final AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>> leaf = leaves.get(i);

            StateInfo<I, Boolean> succStateInfo = leaf.getData();
            if (succStateInfo == null) {
                // Special case: this is the *first* state of a different
                // acceptance than the initial state
                Iterator<AbstractWordBasedDTNode<I, Boolean, StateInfo<I, Boolean>>> trueIt = DiscriminationTreeIterators
                        .leafIterator(discriminationTree.getRoot().child(true));
                boolean isAccepting = false;
                while (trueIt.hasNext()) {
                    if (trueIt.next().equals(leaf)) {
                        isAccepting = true;
                        break;
                    }
                }

                succStateInfo = createState(prefixes.get(i), isAccepting, currentHyp);
                leaf.setData(succStateInfo);
                succStateInfo.dtNode = leaf;

                initState(succStateInfo);
            }

            result.set(i, succStateInfo);
        }

        return result;
    }

    protected MembershipOracle<I, Boolean> getOracle() {
        try {
            java.lang.reflect.Field field = KearnsVaziraniDFA.class.getDeclaredField("oracle");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            MembershipOracle<I, Boolean> oracle = (MembershipOracle<I, Boolean>) field.get(this);
            return oracle;
        } catch (Exception e) {
            throw new RuntimeException("Failed to access oracle field", e);
        }
    }
}
