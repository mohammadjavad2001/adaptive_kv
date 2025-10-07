import java.io.File;
import java.io.IOException;
import java.util.Random;

import de.learnlib.algorithms.kv.KearnsVaziraniMealy;
import de.learnlib.algorithms.kv.KearnsVaziraniMealyBuilder;
import de.learnlib.algorithms.kv.Experiment;
import de.learnlib.api.oracle.EquivalenceOracle;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.ds.MealySimulatorSUL;
import de.learnlib.oracle.equivalence.mealy.RandomWalkEQOracle;
import de.learnlib.oracle.membership.SULOracle;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.serialization.dot.GraphDOT;
import net.automatalib.util.automata.Automata;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

public class SingleProductLearning {

    public static void main(String[] args) throws IOException {
        
        // تنظیمات
        String productPath = "./alternative_experiments/Minepump_SPL/products_3wise/00001_fsm.dot";
        
        System.out.println("================================================================");
        System.out.println("           SINGLE PRODUCT LEARNING");
        System.out.println("================================================================");
        System.out.println("Product: " + productPath);
        System.out.println("================================================================\n");
        
        // بارگذاری مدل اصلی (Original Model)
        CompactMealy<String, String> originalModel = loadMealyMachine(productPath);
        Alphabet<String> alphabet = originalModel.getInputAlphabet();
        
        System.out.println("Original Model loaded:");
        System.out.println("  States: " + originalModel.size());
        System.out.println("  Alphabet size: " + alphabet.size());
        System.out.println("  Alphabet: " + alphabet);
        System.out.println();
        
        // ایجاد SUL و Membership Oracle
        MealySimulatorSUL<String, Word<String>> sul = new MealySimulatorSUL<>(originalModel);
        MembershipOracle.MealyMembershipOracle<String, Word<String>> mqOracle = new SULOracle<>(sul);
        
        // ایجاد Equivalence Oracle
        EquivalenceOracle<MealyMachine<?, String, ?, Word<String>>, String, Word<String>> eqOracle;
        eqOracle = new RandomWalkEQOracle<>(
            sul,
            0.05,  // Reset probability
            10000, // Max steps
            true,  // Reset after each EQ
            new Random(46346293)
        );
        
        // ایجاد Learner با استفاده از Builder
        KearnsVaziraniMealyBuilder<Object, String, Word<String>> builder = new KearnsVaziraniMealyBuilder<>();
        builder.setOracle(mqOracle);
        builder.setAlphabet(alphabet);
        KearnsVaziraniMealy<Object, String, Word<String>> learner = builder.withAlphabet(alphabet).create(null);
        
        // ایجاد Experiment
        Experiment.MealyExperiment<String, Word<String>> experiment = 
            new Experiment.MealyExperiment<>(learner, eqOracle, alphabet);
        
        // شروع profiling
        SimpleProfiler.start("Learning Process");
        
        // اجرای یادگیری
        System.out.println("Starting learning process...\n");
        
        // اجرای یادگیری تا زمانی که مدل نهایی با مدل اصلی یکسان شود
        experiment.run();
        
        // پایان profiling
        SimpleProfiler.stop("Learning Process");
        
        // دریافت مدل نهایی
        MealyMachine<?, String, ?, Word<String>> finalHypothesis = experiment.getFinalHypothesis();
        
        System.out.println("\n================================================================");
        System.out.println("           LEARNING COMPLETED");
        System.out.println("================================================================");
        
        // چاپ آمار
        System.out.println("\nLearning Statistics:");
        System.out.println("  Rounds (EQ queries): " + experiment.getRounds());
        System.out.println();
        
        // بررسی تعداد state ها
        System.out.println("Final Hypothesis:");
        System.out.println("  States: " + finalHypothesis.size());
        System.out.println("  Alphabet size: " + alphabet.size());
        System.out.println();
        
        // بررسی یکسان بودن مدل‌ها
        System.out.println("Checking equivalence...");
        Word<String> counterExample = Automata.findSeparatingWord(
            originalModel, 
            finalHypothesis, 
            alphabet
        );
        
        if (counterExample == null) {
            System.out.println("✓ SUCCESS: Final Hypothesis is equivalent to Original Model!");
        } else {
            System.out.println("✗ WARNING: Models are NOT equivalent!");
            System.out.println("  Counter-example found: " + counterExample);
            System.out.println("  Original output: " + originalModel.computeOutput(counterExample));
            System.out.println("  Hypothesis output: " + finalHypothesis.computeOutput(counterExample));
        }
        
        System.out.println("\n================================================================");
    }
    
    /**
     * بارگذاری Mealy Machine از فایل DOT
     */
    private static CompactMealy<String, String> loadMealyMachine(String filePath) throws IOException {
        CompactMealy<String, String> mealy = new CompactMealy<>(Alphabets.fromArray());
        GraphDOT.read(new File(filePath).toPath(), mealy);
        return mealy;
    }
}

