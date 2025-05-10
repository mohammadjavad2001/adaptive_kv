//package de.learnlib.algorithms.kv;
//
//import de.learnlib.acex.AcexAnalyzer;
//import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
//import de.learnlib.api.oracle.MembershipOracle;
//import de.learnlib.api.query.DefaultQuery;
//import de.learnlib.api.query.Query;
//import de.learnlib.oracle.equivalence.WpMethodEQOracle;
//import de.learnlib.oracle.membership.SimulatorOracle;
//import net.automatalib.words.Alphabet;
//import de.learnlib.util.QueryUtil;
//import net.automatalib.words.Word;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//public class QuerySynthesisKV<I, O> extends KearnsVaziraniMealy {
//
//    private int maxQueries;
//    private int batchSize;
//	private List<Word<I>> suffixes;
//	private List<Word<I>> prefixes;
//	private static boolean repeatedCounterexampleEvaluation;
//
//
//    public QuerySynthesisKV(
//    		 Alphabet<I> alphabet,
//    	     MembershipOracle<I, Word<O>> oracle,
//    	     List<Word<I>> initialPrefixes,
//    	     List<Word<I>> initialSuffixes, AcexAnalyzer ceAnalyzer,
//    	     int maxQueries, int batchSize) {
//    	super(alphabet, oracle, repeatedCounterexampleEvaluation, ceAnalyzer);
//    	   this.prefixes = initialPrefixes;
//    	   this.suffixes = initialSuffixes;
//        this.maxQueries = maxQueries;
//        this.batchSize = batchSize;
//    }
//
//    @Override
//    public void startLearning() {
//    	List<DefaultQuery<I, Word<O>>> queries = generateInitialQueries();
//        int numQueries = queries.size();
//        while (!getHypothesisModel().isCompletelyDefined() && numQueries < maxQueries) {
//        	List<DefaultQuery<I, Word<O>>> batchQueries = generateBatchQueries(queries, batchSize);
//            QueryUtil.normalize(batchQueries);
//            queries.addAll(batchQueries);
//            for (DefaultQuery<I, Word<O>> query : batchQueries) {
//                super.refineHypothesis(query);
//            }
//            
//            numQueries = queries.size();
//        }
//    }
//
//    private List<DefaultQuery<I, Word<O>>> generateInitialQueries() {
//        List<DefaultQuery<I, Word<O>>> queries = new ArrayList<>();
//        
//        for (Word<I> prefix : this.prefixes) {
//            for (Word<I> suffix : this.suffixes) {
//              DefaultQuery<I, Word<O>> query = new DefaultQuery<>(prefix.concat(suffix));
//              queries.add(query);
//            }
//          }
//        return queries;
//    }
//
//    private List<DefaultQuery<I, Word<O>>> generateBatchQueries(List<DefaultQuery<I, Word<O>>>  queries, int batchSize) {
//        SimulatorOracle<Integer, Character, Integer> oracle = new SimulatorOracle<>(getHypothesisModel());
//        WpMethodEQOracle<Integer, Character, Integer> eqOracle = new WpMethodEQOracle<>(oracle);
//        List<DefaultQuery<I, Word<O>>> batchQueries = new ArrayList<>();
//        for (int i = 0; i < batchSize; i++) {
//        	Query<I, Word<O>> query = eqOracle.findCounterExample(getHypothesisModel(), queries);
//            if (query == null) {
//                break;
//            }
//            batchQueries.add(query);
//        }
//        return batchQueries;
//    }
//}