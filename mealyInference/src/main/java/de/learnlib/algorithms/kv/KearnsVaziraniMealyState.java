package de.learnlib.algorithms.kv;


import de.learnlib.ds.StateInfo;
import de.learnlib.ds.MultiDTree;
// import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import de.learnlib.ds.CompactMealy;
import net.automatalib.words.Word;
import java.io.Serializable;
import java.util.List;

public class KearnsVaziraniMealyState<I, O> implements Serializable {
    
    private final CompactMealy<I, O> hypothesis;
    private final MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> discriminationTree;
    private final List<StateInfo<I, Word<O>>> stateInfos;
    
    public KearnsVaziraniMealyState(CompactMealy<I, O> hypothesis, 
                                     MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> discriminationTree,
                                     List<StateInfo<I, Word<O>>> stateInfos) {
        this.hypothesis = hypothesis;
        this.discriminationTree = discriminationTree;
        this.stateInfos = stateInfos;
    }
    
    public CompactMealy<I, O> getHypothesis() {
        return hypothesis;
    }
    
    public MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> getDiscriminationTree() {
        return discriminationTree;
    }
    
    public List<StateInfo<I, Word<O>>> getStateInfos() {
        return stateInfos;
    }
}