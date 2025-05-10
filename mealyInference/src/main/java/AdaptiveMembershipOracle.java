import de.learnlib.api.oracle.MembershipOracle;
//import de.learnlib.oracle.membership.SimulatorOracle.MealySimulatorOracle;
//import net.automatalib.automata.transout.MealyMachine;
//import net.automatalib.words.Alphabet;
//import net.automatalib.words.Word;
//import net.automatalib.words.WordBuilder;
//import net.automatalib.words.WordBuilderFactory;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//private class AdaptiveMembershipOracle<I, O> implements MembershipOracle<I, O> {
//
//
//        private final MealySimulatorOracle<I, O> simulator;
//        private final Alphabet<I> alphabet;
//        private final WordBuilderFactory<I> wordBuilderFactory = new WordBuilderFactory<>();
//
//        public AdaptiveOracle(MealySimulatorOracle<I, O> simulator, Alphabet<I> alphabet) {
//            this.simulator = simulator;
//            this.alphabet = alphabet;
//        }
//
//        @Override
//        public Word<O> answerQuery(Word<I> query) {
//            return simulator.answerQuery(query);
//        }
//
//        @Override
//        public void processQueries(Iterable<? extends Query<I, Word<O>>> queries) {
//            List<I> informativeInputs = new ArrayList<>();
//
//            for (I input : alphabet) {
//                DFAMealySimulatorOracle<I, O> dfaSimulator = new DFAMealySimulatorOracle<>(simulator.getSimulator(), wordBuilderFactory.create().append(input).toWord());
//                Word<O> output = answerQuery(dfaSimulator.getPrefix());
//
//                if (output == null) {
//                    informativeInputs.add(input);
//                }
//            }
//
//            if (!informativeInputs.isEmpty()) {
//                I selectedInput = informativeInputs.get(random.nextInt(informativeInputs.size()));
//                Word<O> output = answerQuery(wordBuilderFactory.create().append(selectedInput).toWord());
//
//                for (Query<I, Word<O>> query : queries) {
//                    query.answer(output);
//                }
//            } else {
//                // If all inputs are informative, select a random input
//                I selectedInput = alphabet.get(random.nextInt(alphabet.size()));
//                Word<O> output = answerQuery(wordBuilderFactory.create().append(selectedInput).toWord());
//
//                for (Query<I, Word<O>> query : queries) {
//                    query.answer(output);
//                }
//            }
//        }
//    }
//}
