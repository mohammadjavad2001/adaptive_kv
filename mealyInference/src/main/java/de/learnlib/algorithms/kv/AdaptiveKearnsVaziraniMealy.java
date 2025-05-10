package de.learnlib.algorithms.kv;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import de.learnlib.acex.AcexAnalyzer;
import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandler;
import de.learnlib.algorithms.lstar.ce.ObservationTableCEXHandlers;
import de.learnlib.algorithms.lstar.closing.ClosingStrategies;
import de.learnlib.algorithms.lstar.closing.ClosingStrategy;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.datastructure.discriminationtree.MultiDTree;
import de.learnlib.datastructure.discriminationtree.model.AbstractWordBasedDTNode;
import de.learnlib.api.query.DefaultQuery;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import java.util.*;
import java.util.Map.Entry;

// import br.usp.icmc.labes.mealyInference.utils.DotFileGen;

public class AdaptiveKearnsVaziraniMealy <I, O> extends KearnsVaziraniMealy<I, O> {

	private List<Word<I>> suffixes;
	private List<Word<I>> prefixes;

	  
//	private final Alphabet<I> alphabet;
//	private final MembershipOracle<I, Word<O>> oracle;
	private static boolean repeatedCounterexampleEvaluation;
	private static AcexAnalyzer ceAnalyzer;
	protected MultiDTree<I, Word<O>, StateInfo<I, Word<O>>> discriminationTree;
	protected List<StateInfo<I, Word<O>>> stateInfos = new ArrayList<>();
	private CompactMealy<I, O> hypothesis;
    private final MembershipOracle<I, Word<O>> oracle;

  public AdaptiveKearnsVaziraniMealy(
      Alphabet<I> alphabet,
      MembershipOracle<I, Word<O>> oracle,
      List<Word<I>> initialPrefixes,
      List<Word<I>> initialSuffixes, AcexAnalyzer ceAnalyzer){ 
    super(alphabet, oracle, repeatedCounterexampleEvaluation, ceAnalyzer);
    this.prefixes = initialPrefixes;
    this.suffixes = initialSuffixes;
    this.oracle = oracle;
//    initializeDTree(oracle);
  }

  private void initializeDTree(MembershipOracle<I, Word<O>> oracle) {
	  
	  Word<String> query = Word.fromSymbols("a", "b", "c");
	  Word<O> output;
    // Traverse the set of prefixes and suffixes to add them to the DTree
    for (Word<I> prefix : this.prefixes) {
//      for (Word<I> suffix : this.suffixes) {
//        DefaultQuery<I, Word<O>> query = new DefaultQuery<>(prefix);
//        	oracle.processQuery(query);
    	output =oracle.answerQuery(prefix);
    	System.out.println("MEMBER Counterexample: " + prefix + " --> " + output);
//        	oracle.addMembershipQuery(query, output);
//      }
    }

//    Counterexample: switchIntv rain --> 1 1
//    Counterexample: switchIntv rain rain rain --> 1 1 0 1
//    Counterexample: switchPerm switchPerm --> 1 0;
//    oracle.addMembershipQuery("switchIntv rain", "1 1");
    
    for (Word<I> suffix : this.suffixes) {
//      for (Word<I> suffix : this.suffixes) {
//        DefaultQuery<I, Word<O>> query = new DefaultQuery<>(suffix);
//        	oracle.processQuery(query);
    	    oracle.answerQuery(suffix);
//      }
    }
    this.discriminationTree = new MultiDTree<>(oracle);
    this.discriminationTree.setOracle(oracle);
	// DotFileGen<I, O> dotgen = new DotFileGen(this.discriminationTree);
	// dotgen.visualizeTree();
    
  }
  
  
  
  public net.automatalib.words.Alphabet<I> getAlphabet() {
		return this.getAlphabet();
	}
  

  
  public static final class BuilderDefaults {

      private BuilderDefaults() {
          // prevent instantiation
      }

      public static <I> List<Word<I>> initialPrefixes() {
          return Collections.singletonList(Word.epsilon());
      }

      public static <I> List<Word<I>> initialSuffixes() {
          return Collections.emptyList();
      }

  }
}

