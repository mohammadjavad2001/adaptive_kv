import br.usp.icmc.labes.mealyInference.utils.Utils;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;
import net.automatalib.words.impl.Alphabets;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compare and Visualize Two Product FSMs
 * Similar to App.java but focuses on comparing two products and showing their structure without self-loops
 */
public class CompareAndVisualizeProducts {

    public static final Word<String> OMEGA_SYMBOL = Word.fromLetter("Ω");

    /**
     * Load Mealy Machine from DOT file (similar to App.java's loadMealyMachineFromDot3)
     */
    public static CompactMealy<String, Word<String>> loadMealyMachineFromDot(File f) throws Exception {
        Pattern kissLine = Pattern.compile("\\s*([a-zA-Z0-9]+)\\s+->\\s+([a-zA-Z0-9]+)\\s*\\[label=[\"<](.+)[\">]\\];?");
        
        System.out.println("Loading DOT file: " + f.getName());
        BufferedReader br = new BufferedReader(new FileReader(f));
        
        List<String[]> trs = new ArrayList<String[]>();
        HashSet<String> abcSet = new HashSet<>();
        
        while(br.ready()){
            String line = br.readLine();
            Matcher m = kissLine.matcher(line);
            if(m.matches()){
                String[] tr = new String[4];
                tr[0] = m.group(1);  // source state
                tr[1] = m.group(3);  // label (input/output)
                tr[3] = m.group(2);  // target state
                
                if(tr[1].contains("<br />")){
                    String trr[] = tr[1].split("<br />");
                    tr[1] = trr[0];
                    tr[2] = trr[1];
                    trr = tr[1].split(" \\| ");
                    for (String string : trr) {
                        String trrr[] = new String[4];
                        trrr[0] = tr[0];
                        trrr[1] = string;
                        trrr[2] = tr[2];
                        trrr[3] = tr[3];
                        trs.add(trrr);
                        abcSet.add(trrr[1]);
                    }
                } else {
                    String trr[] = tr[1].split("\\s*/\\s*");
                    tr[1] = trr[0];  // input
                    tr[2] = trr[1];  // output
                    trs.add(tr);
                    abcSet.add(tr[1]); 
                }
            }
        }
        br.close();

        List abc = new ArrayList<>(abcSet);
        Collections.sort(abc);
        Alphabet<String> alphabet = Alphabets.fromCollection(abc);
        CompactMealy<String, Word<String>> mealym = new CompactMealy<String, Word<String>>(alphabet);

        Map<String,Integer> states = new HashMap<String,Integer>();
        Integer si = null, sf = null;
        Map<String,Word<String>> words = new HashMap<String,Word<String>>();

        WordBuilder<String> aux = new WordBuilder<>();
        aux.clear();
        aux.append(OMEGA_SYMBOL);
        words.put(OMEGA_SYMBOL.toString(), aux.toWord());

        for (String[] tr : trs) {
            if(!states.containsKey(tr[0])) states.put(tr[0], mealym.addState());
            if(!states.containsKey(tr[3])) states.put(tr[3], mealym.addState());

            si = states.get(tr[0]);
            sf = states.get(tr[3]);

            if(!words.containsKey(tr[1])){
                aux.clear();
                aux.add(tr[1]);
                words.put(tr[1], aux.toWord());
            }
            if(!words.containsKey(tr[2])){
                aux.clear();
                aux.add(tr[2]);
                words.put(tr[2], aux.toWord());
            }
            mealym.addTransition(si, words.get(tr[1]).toString(), sf, words.get(tr[2]));
        }

        // Complete the machine with omega transitions
        for (Integer st : mealym.getStates()) {
            for (String in : alphabet) {
                if(mealym.getTransition(st, in) == null){
                    mealym.addTransition(st, in, st, OMEGA_SYMBOL);
                }
            }
        }

        mealym.setInitialState(states.get("s0"));
        
        System.out.println("✓ Loaded: " + mealym.getStates().size() + " states, " + 
                         alphabet.size() + " input symbols");
        
        return mealym;
    }

    /**
     * Create a version of the Mealy machine without self-loops for visualization
     */
    public static CompactMealy<String, Word<String>> createMealyWithoutSelfLoops(
            CompactMealy<String, Word<String>> original) {
        
        Alphabet<String> alphabet = original.getInputAlphabet();
        CompactMealy<String, Word<String>> filtered = new CompactMealy<>(alphabet);
        
        // Map old states to new states
        Map<Integer, Integer> stateMap = new HashMap<>();
        for (Integer state : original.getStates()) {
            stateMap.put(state, filtered.addState());
        }
        
        // Set initial state
        filtered.setInitialState(stateMap.get(original.getInitialState()));
        
        // Copy only non-self-loop transitions
        int selfLoopCount = 0;
        int normalCount = 0;
        
        for (Integer state : original.getStates()) {
            for (String input : alphabet) {
                Integer targetState = original.getSuccessor(state, input);
                Word<String> output = original.getOutput(state, input);
                
                if (targetState != null && output != null) {
                    // Only add if it's NOT a self-loop
                    if (!state.equals(targetState)) {
                        filtered.addTransition(
                            stateMap.get(state), 
                            input, 
                            stateMap.get(targetState), 
                            output
                        );
                        normalCount++;
                    } else {
                        selfLoopCount++;
                    }
                }
            }
        }
        
        System.out.println("  - Filtered out " + selfLoopCount + " self-loops");
        System.out.println("  - Kept " + normalCount + " non-self-loop transitions");
        
        return filtered;
    }

    /**
     * Print detailed product information
     */
    public static void printProductInfo(String productName, CompactMealy<String, Word<String>> mealy) {
        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println(String.format("║ %-77s ║", "PRODUCT: " + productName));
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        
        // States
        System.out.println(String.format("║ %-77s ║", "States: " + mealy.getStates().size()));
        System.out.println(String.format("║ %-77s ║", "  Initial State: s" + mealy.getInitialState()));
        
        // Alphabet
        Alphabet<String> alphabet = mealy.getInputAlphabet();
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        System.out.println(String.format("║ %-77s ║", "Input Alphabet: " + alphabet.size() + " symbols"));
        System.out.println("║ Symbols:                                                                      ║");
        
        List<String> sortedSymbols = new ArrayList<>();
        for (String symbol : alphabet) {
            sortedSymbols.add(symbol);
        }
        Collections.sort(sortedSymbols);
        
        StringBuilder symbolLine = new StringBuilder("║   ");
        for (String symbol : sortedSymbols) {
            if (symbolLine.length() + symbol.length() + 2 > 78) {
                while (symbolLine.length() < 79) symbolLine.append(" ");
                System.out.println(symbolLine + "║");
                symbolLine = new StringBuilder("║   ");
            }
            symbolLine.append(symbol).append(", ");
        }
        if (symbolLine.length() > 4) {
            symbolLine.setLength(symbolLine.length() - 2);
            while (symbolLine.length() < 79) symbolLine.append(" ");
            System.out.println(symbolLine + "║");
        }
        
        // Count transitions
        int totalTransitions = 0;
        int selfLoops = 0;
        Set<String> outputSymbols = new TreeSet<>();
        
        for (Integer state : mealy.getStates()) {
            for (String input : alphabet) {
                Integer target = mealy.getSuccessor(state, input);
                Word<String> output = mealy.getOutput(state, input);
                
                if (target != null && output != null) {
                    totalTransitions++;
                    if (state.equals(target)) {
                        selfLoops++;
                    }
                    for (String outSym : output) {
                        outputSymbols.add(outSym);
                    }
                }
            }
        }
        
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        System.out.println(String.format("║ %-77s ║", "Output Symbols: " + outputSymbols.size()));
        System.out.println(String.format("║ %-77s ║", "  " + outputSymbols.toString()));
        
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        System.out.println(String.format("║ %-77s ║", "Transitions:"));
        System.out.println(String.format("║ %-77s ║", "  Total: " + totalTransitions));
        System.out.println(String.format("║ %-77s ║", "  Self-loops: " + selfLoops));
        System.out.println(String.format("║ %-77s ║", "  Non-self-loop: " + (totalTransitions - selfLoops)));
        
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Compare two products
     */
    public static void compareProducts(String name1, CompactMealy<String, Word<String>> mealy1,
                                      String name2, CompactMealy<String, Word<String>> mealy2) {
        
        System.out.println("\n╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                        PRODUCT COMPARISON ANALYSIS                            ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        
        // State comparison
        System.out.println(String.format("║ %-77s ║", 
            String.format("States: %s has %d | %s has %d", 
                name1, mealy1.getStates().size(),
                name2, mealy2.getStates().size())));
        
        // Alphabet comparison
        Set<String> alphabet1 = new TreeSet<>();
        Set<String> alphabet2 = new TreeSet<>();
        
        for (String s : mealy1.getInputAlphabet()) alphabet1.add(s);
        for (String s : mealy2.getInputAlphabet()) alphabet2.add(s);
        
        Set<String> commonSymbols = new TreeSet<>(alphabet1);
        commonSymbols.retainAll(alphabet2);
        
        Set<String> onlyIn1 = new TreeSet<>(alphabet1);
        onlyIn1.removeAll(alphabet2);
        
        Set<String> onlyIn2 = new TreeSet<>(alphabet2);
        onlyIn2.removeAll(alphabet1);
        
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        System.out.println(String.format("║ %-77s ║", "Alphabet Analysis:"));
        System.out.println(String.format("║ %-77s ║", 
            String.format("  %s: %d symbols | %s: %d symbols", 
                name1, alphabet1.size(),
                name2, alphabet2.size())));
        System.out.println(String.format("║ %-77s ║", "  Common symbols: " + commonSymbols.size()));
        System.out.println(String.format("║ %-77s ║", "  Only in " + name1 + ": " + onlyIn1.size()));
        System.out.println(String.format("║ %-77s ║", "  Only in " + name2 + ": " + onlyIn2.size()));
        
        if (!onlyIn1.isEmpty()) {
            System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
            System.out.println(String.format("║ %-77s ║", "Symbols ONLY in " + name1 + ":"));
            printSymbolSet(onlyIn1);
        }
        
        if (!onlyIn2.isEmpty()) {
            System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
            System.out.println(String.format("║ %-77s ║", "Symbols ONLY in " + name2 + ":"));
            printSymbolSet(onlyIn2);
        }
        
        // Complexity comparison
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        System.out.println(String.format("║ %-77s ║", "Complexity:"));
        System.out.println(String.format("║ %-77s ║", 
            String.format("  %s: %d states × %d inputs = %d possible transitions", 
                name1, mealy1.getStates().size(), alphabet1.size(),
                mealy1.getStates().size() * alphabet1.size())));
        System.out.println(String.format("║ %-77s ║", 
            String.format("  %s: %d states × %d inputs = %d possible transitions", 
                name2, mealy2.getStates().size(), alphabet2.size(),
                mealy2.getStates().size() * alphabet2.size())));
        
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
    }

    private static void printSymbolSet(Set<String> symbols) {
        StringBuilder line = new StringBuilder("║   ");
        for (String symbol : symbols) {
            if (line.length() + symbol.length() + 2 > 78) {
                while (line.length() < 79) line.append(" ");
                System.out.println(line + "║");
                line = new StringBuilder("║   ");
            }
            line.append(symbol).append(", ");
        }
        if (line.length() > 4) {
            line.setLength(line.length() - 2);
            while (line.length() < 79) line.append(" ");
            System.out.println(line + "║");
        }
    }

    public static void main(String[] args) {
        try {
            // Default products to compare
            String product1Path = ".\\alternative_experiments\\Minepump_SPL\\products_3wise\\00001_fsm.dot";
            String product2Path = ".\\alternative_experiments\\Minepump_SPL\\products_3wise\\00004_fsm.dot";
            String product1Name = product1Path;
            String product2Name = product2Path;
            
            // Allow command line arguments
            if (args.length >= 2) {
                product1Path = args[0];
                product2Path = args[1];
            }
            if (args.length >= 4) {
                product1Name = args[2];
                product2Name = args[3];
            }
            
            System.out.println("╔═══════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║               PRODUCT FSM COMPARISON & VISUALIZATION TOOL                     ║");
            System.out.println("║                      (Mealy Machine Analysis)                                 ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
            System.out.println();
            
            // Load Product 1
            File file1 = new File(product1Path);
            System.out.println("Loading " + product1Name + "...");
            CompactMealy<String, Word<String>> mealy1 = loadMealyMachineFromDot(file1);
            
            // Load Product 2
            File file2 = new File(product2Path);
            System.out.println("\nLoading " + product2Name + "...");
            CompactMealy<String, Word<String>> mealy2 = loadMealyMachineFromDot(file2);
            
            // Print detailed information
            printProductInfo(product1Name, mealy1);
            printProductInfo(product2Name, mealy2);
            
            // Compare products
            compareProducts(product1Name, mealy1, product2Name, mealy2);
            
            // Create versions without self-loops
            System.out.println("\n════════════════════════════════════════════════════════════════");
            System.out.println("Creating filtered versions (without self-loops)...");
            System.out.println("════════════════════════════════════════════════════════════════");
            
            System.out.println("\nFiltering " + product1Name + ":");
            CompactMealy<String, Word<String>> mealy1NoSelfLoops = createMealyWithoutSelfLoops(mealy1);
            
            System.out.println("\nFiltering " + product2Name + ":");
            CompactMealy<String, Word<String>> mealy2NoSelfLoops = createMealyWithoutSelfLoops(mealy2);
            
            // Visualize original models
            System.out.println("\n════════════════════════════════════════════════════════════════");
            System.out.println("Visualization Windows Opening...");
            System.out.println("════════════════════════════════════════════════════════════════");
            
            System.out.println("\n[1] Visualizing " + product1Name + " (ORIGINAL with self-loops)...");
            Visualization.visualize(mealy1, mealy1.getInputAlphabet());
            
            System.out.println("[2] Visualizing " + product2Name + " (ORIGINAL with self-loops)...");
            Visualization.visualize(mealy2, mealy2.getInputAlphabet());
            
            System.out.println("[3] Visualizing " + product1Name + " (WITHOUT self-loops)...");
            Visualization.visualize(mealy1NoSelfLoops, mealy1NoSelfLoops.getInputAlphabet());
            
            System.out.println("[4] Visualizing " + product2Name + " (WITHOUT self-loops)...");
            Visualization.visualize(mealy2NoSelfLoops, mealy2NoSelfLoops.getInputAlphabet());
            
            System.out.println("\n════════════════════════════════════════════════════════════════");
            System.out.println("✓ All visualizations opened successfully!");
            System.out.println("  - 4 visualization windows created");
            System.out.println("  - 2 original models (with self-loops)");
            System.out.println("  - 2 filtered models (without self-loops)");
            System.out.println("════════════════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}





