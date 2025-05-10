import java.io.File;
import java.io.IOException;

import de.learnlib.algorithms.kv.mealy.KearnsVaziraniMealy;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.query.DefaultQuery;
import de.learnlib.drivers.reflect.SimplePOJOTestDriver;
import de.learnlib.filter.cache.mealy.MealyCacheOracle;
import de.learnlib.filter.statistic.oracle.JointCounterOracle;
import de.learnlib.oracle.equivalence.MealySimulatorEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle;
import de.learnlib.util.Experiment;
import de.learnlib.util.statistics.SimpleProfiler;

import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.serialization.dot.DOTParsers;
import net.automatalib.serialization.InputModelDeserializer;
import net.automatalib.util.automata.Automata;
import net.automatalib.util.automata.equivalence.DeterministicEquivalenceTest;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

public class hi2 {

    // Parser for the edge labels in DOT file - adjust if your format is different
    private static final Function<String, Word<String>> MEALY_EDGE_WORD_STR_PARSER = s -> Word.fromString(s, " ");

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java LearnMealyFromDOT <dot-file>");
            System.exit(1);
        }

        File dotFile = new File(args[0]);
        CompactMealy<String, Word<String>> originalMealy = loadMealy(dotFile);
        
        if (originalMealy == null) {
            System.err.println("Failed to load Mealy machine from file.");
            System.exit(1);
        }

        System.out.println("Original automaton loaded. States: " + originalMealy.size());
        
        // Let's extract the input alphabet
        Alphabet<String> alphabet = originalMealy.getInputAlphabet();
        
        // Create a membership oracle based on the loaded model
        MembershipOracle<String, Word<String>> membershipOracle = 
                new SimulatorOracle<>(originalMealy);
        
        // Wrap in a cache for performance
        MembershipOracle<String, Word<String>> cachedOracle = 
                MealyCacheOracle.createDAGCacheOracle(alphabet, membershipOracle);
        
        // Add statistics counter
        JointCounterOracle<String, Word<String>> counterOracle = 
                new JointCounterOracle<>(cachedOracle);
        
        // Create the learning algorithm - Kearns-Vazirani
        KearnsVaziraniMealy<String, Word<String>> learner = 
                new KearnsVaziraniMealy<>(alphabet, counterOracle);
        
        // Create an equivalence oracle for testing equivalence
        MealySimulatorEQOracle<String, Word<String>> eqOracle = 
                new MealySimulatorEQOracle<>(originalMealy);
        
        // Setup the experiment
        Experiment.MealyExperiment<String, Word<String>> experiment = 
                new Experiment.MealyExperiment<>(learner, eqOracle, alphabet);
        
        // Add profiling information
        experiment.setProfile(true);
        
        // Run the experiment
        System.out.println("Starting learning...");
        experiment.run();
        
        // Get the learned model
        CompactMealy<String, Word<String>> learnedModel = 
                new CompactMealy<>(experiment.getFinalHypothesis());
        
        // Output the results
        System.out.println("Finished learning");
        System.out.println("Learning statistics:");
        System.out.println("  Membership queries: " + counterOracle.getCount());
        System.out.println("  Equivalence queries: " + experiment.getRounds());
        System.out.println("  States in learned model: " + learnedModel.size());
        
        // Check if the models are equivalent
        boolean equivalent = DeterministicEquivalenceTest.findSeparatingWord(
                originalMealy, learnedModel, alphabet) == null;
        System.out.println("Models are equivalent: " + equivalent);
        
        // Visualize the learned model (optional)
        System.out.println("Visualizing learned model...");
        Visualization.visualize(learnedModel, alphabet);
        
        // Visualize the original model for comparison (optional)
        System.out.println("Visualizing original model...");
        Visualization.visualize(originalMealy, alphabet);
    }
    
    private static CompactMealy<String, Word<String>> loadMealy(File fsm_file) {
        // Parser for DOT files
        InputModelDeserializer<String, CompactMealy<String, Word<String>>> parser = 
                DOTParsers.mealy(MEALY_EDGE_WORD_STR_PARSER);
        
        CompactMealy<String, Word<String>> mealy = null;
        String file_name = fsm_file.getName();
        
        if (file_name.endsWith("txt")) {
            try {
                // Assuming you have a Utils class with this method
                mealy = Utils.getInstance().loadMealyMachine(fsm_file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mealy;
        } else if (file_name.endsWith("dot")) {
            try {
                mealy = parser.readModel(fsm_file).model;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return mealy;
        }
        return null;
    }
}