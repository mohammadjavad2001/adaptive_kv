import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * Visualizes and compares two product FSMs from DOT files
 * Shows transitions without self-loops and displays the alphabet/symbols of each product
 */
public class VisualizeProductComparison {
    
    static class Transition {
        String source;
        String target;
        String input;
        String output;
        
        public Transition(String source, String target, String input, String output) {
            this.source = source;
            this.target = target;
            this.input = input;
            this.output = output;
        }
        
        public boolean isSelfLoop() {
            return source.equals(target);
        }
        
        @Override
        public String toString() {
            return String.format("%s -> %s [%s / %s]", source, target, input, output);
        }
    }
    
    static class ProductModel {
        String name;
        Set<String> states = new TreeSet<>();
        Set<String> alphabet = new TreeSet<>();
        List<Transition> transitions = new ArrayList<>();
        List<Transition> nonSelfLoopTransitions = new ArrayList<>();
        
        public ProductModel(String name) {
            this.name = name;
        }
        
        public void addTransition(Transition t) {
            transitions.add(t);
            states.add(t.source);
            states.add(t.target);
            alphabet.add(t.input);
            
            if (!t.isSelfLoop()) {
                nonSelfLoopTransitions.add(t);
            }
        }
    }
    
    public static ProductModel parseDotFile(String filepath, String productName) throws IOException {
        ProductModel model = new ProductModel(productName);
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        String line;
        
        // Pattern to match transitions: s0 -> s1 [label="input / output"];
        Pattern transitionPattern = Pattern.compile("\\s*(s\\d+)\\s*->\\s*(s\\d+)\\s*\\[label=\"([^/]+)\\s*/\\s*([^\"]+)\"\\]");
        
        while ((line = reader.readLine()) != null) {
            Matcher matcher = transitionPattern.matcher(line);
            if (matcher.find()) {
                String source = matcher.group(1);
                String target = matcher.group(2);
                String input = matcher.group(3).trim();
                String output = matcher.group(4).trim();
                
                model.addTransition(new Transition(source, target, input, output));
            }
        }
        reader.close();
        
        return model;
    }
    
    public static void printProductInfo(ProductModel model) {
        System.out.println("╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println(String.format("║ %-77s ║", "PRODUCT: " + model.name));
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        
        // States
        System.out.println(String.format("║ %-77s ║", "States: " + model.states.size() + " total"));
        System.out.println(String.format("║ %-77s ║", model.states.toString()));
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        
        // Alphabet/Symbols
        System.out.println(String.format("║ %-77s ║", "Input Alphabet: " + model.alphabet.size() + " symbols"));
        System.out.println("║ Symbols:                                                                      ║");
        int count = 0;
        StringBuilder symbolLine = new StringBuilder("║   ");
        for (String symbol : model.alphabet) {
            if (symbolLine.length() + symbol.length() + 2 > 78) {
                // Pad the rest and print
                while (symbolLine.length() < 79) symbolLine.append(" ");
                System.out.println(symbolLine + "║");
                symbolLine = new StringBuilder("║   ");
            }
            symbolLine.append(symbol).append(", ");
            count++;
        }
        if (symbolLine.length() > 4) {
            // Remove last comma
            symbolLine.setLength(symbolLine.length() - 2);
            while (symbolLine.length() < 79) symbolLine.append(" ");
            System.out.println(symbolLine + "║");
        }
        
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        
        // Transition statistics
        int selfLoops = model.transitions.size() - model.nonSelfLoopTransitions.size();
        System.out.println(String.format("║ %-77s ║", "Total Transitions: " + model.transitions.size()));
        System.out.println(String.format("║ %-77s ║", "  - Self-loops: " + selfLoops));
        System.out.println(String.format("║ %-77s ║", "  - Non-self-loop transitions: " + model.nonSelfLoopTransitions.size()));
        
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    public static void printTransitionsWithoutSelfLoops(ProductModel model) {
        System.out.println("┌───────────────────────────────────────────────────────────────────────────────┐");
        System.out.println(String.format("│ %-77s │", "NON-SELF-LOOP TRANSITIONS: " + model.name));
        System.out.println("├───────────────────────────────────────────────────────────────────────────────┤");
        System.out.println(String.format("│ %-10s │ %-10s │ %-30s │ %-10s │", "From", "To", "Input", "Output"));
        System.out.println("├────────────┼────────────┼────────────────────────────────────┼────────────┤");
        
        for (Transition t : model.nonSelfLoopTransitions) {
            String inputStr = t.input.length() > 30 ? t.input.substring(0, 27) + "..." : t.input;
            System.out.println(String.format("│ %-10s │ %-10s │ %-30s │ %-10s │", 
                t.source, t.target, inputStr, t.output));
        }
        
        System.out.println("└────────────┴────────────┴────────────────────────────────────┴────────────┘");
        System.out.println();
    }
    
    public static void generateDotFileWithoutSelfLoops(ProductModel model, String outputPath) throws IOException {
        PrintWriter writer = new PrintWriter(new FileWriter(outputPath));
        
        writer.println("digraph \"" + model.name + "_NoSelfLoops\" {");
        writer.println("    rankdir=LR;");
        writer.println("    node [shape=circle];");
        writer.println("    ");
        writer.println("    // Initial state");
        writer.println("    \"\" [shape=none];");
        writer.println("    \"\" -> s0;");
        writer.println("    ");
        
        // Write state declarations
        for (String state : model.states) {
            writer.println("    " + state + " [label=\"" + state.substring(1) + "\"];");
        }
        writer.println("    ");
        
        // Write non-self-loop transitions
        writer.println("    // Non-self-loop transitions");
        for (Transition t : model.nonSelfLoopTransitions) {
            writer.println(String.format("    %s -> %s [label=\"%s / %s\"];", 
                t.source, t.target, t.input, t.output));
        }
        
        writer.println("}");
        writer.close();
        
        System.out.println("✓ Generated DOT file (without self-loops): " + outputPath);
    }
    
    public static void compareProducts(ProductModel product1, ProductModel product2) {
        System.out.println("╔═══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                        PRODUCT COMPARISON ANALYSIS                            ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        
        // State comparison
        System.out.println(String.format("║ %-77s ║", 
            String.format("States: %s has %d | %s has %d", 
                product1.name, product1.states.size(),
                product2.name, product2.states.size())));
        
        // Alphabet comparison
        Set<String> commonSymbols = new TreeSet<>(product1.alphabet);
        commonSymbols.retainAll(product2.alphabet);
        
        Set<String> onlyInProduct1 = new TreeSet<>(product1.alphabet);
        onlyInProduct1.removeAll(product2.alphabet);
        
        Set<String> onlyInProduct2 = new TreeSet<>(product2.alphabet);
        onlyInProduct2.removeAll(product1.alphabet);
        
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        System.out.println(String.format("║ %-77s ║", "Alphabet Analysis:"));
        System.out.println(String.format("║ %-77s ║", 
            String.format("  %s: %d symbols | %s: %d symbols", 
                product1.name, product1.alphabet.size(),
                product2.name, product2.alphabet.size())));
        System.out.println(String.format("║ %-77s ║", "  Common symbols: " + commonSymbols.size()));
        System.out.println(String.format("║ %-77s ║", "  Only in " + product1.name + ": " + onlyInProduct1.size()));
        System.out.println(String.format("║ %-77s ║", "  Only in " + product2.name + ": " + onlyInProduct2.size()));
        
        if (!onlyInProduct1.isEmpty()) {
            System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
            System.out.println(String.format("║ %-77s ║", "Symbols ONLY in " + product1.name + ":"));
            printSymbolSet(onlyInProduct1);
        }
        
        if (!onlyInProduct2.isEmpty()) {
            System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
            System.out.println(String.format("║ %-77s ║", "Symbols ONLY in " + product2.name + ":"));
            printSymbolSet(onlyInProduct2);
        }
        
        // Transition comparison
        System.out.println("╠═══════════════════════════════════════════════════════════════════════════════╣");
        System.out.println(String.format("║ %-77s ║", "Transition Analysis:"));
        System.out.println(String.format("║ %-77s ║", 
            String.format("  %s: %d total (%d non-self-loop)", 
                product1.name, product1.transitions.size(), product1.nonSelfLoopTransitions.size())));
        System.out.println(String.format("║ %-77s ║", 
            String.format("  %s: %d total (%d non-self-loop)", 
                product2.name, product2.transitions.size(), product2.nonSelfLoopTransitions.size())));
        
        System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();
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
            String product1Path = "..\\alternative_experiments\\Minepump_SPL\\products_3wise\\00001_fsm.dot";
            String product2Path = "..\\alternative_experiments\\Minepump_SPL\\products_3wise\\00002_fsm.dot";
            String product1Name = "Product 00001";
            String product2Name = "Product 00002";
            
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
            System.out.println("║                    PRODUCT FSM VISUALIZATION & COMPARISON                     ║");
            System.out.println("║                         (Without Self-Loops)                                  ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════════════════════╝");
            System.out.println();
            
            // Parse both products
            System.out.println("Loading products...");
            ProductModel product1 = parseDotFile(product1Path, product1Name);
            System.out.println("✓ Loaded " + product1Name + " from: " + product1Path);
            
            ProductModel product2 = parseDotFile(product2Path, product2Name);
            System.out.println("✓ Loaded " + product2Name + " from: " + product2Path);
            System.out.println();
            
            // Display Product 1
            printProductInfo(product1);
            printTransitionsWithoutSelfLoops(product1);
            
            // Display Product 2
            printProductInfo(product2);
            printTransitionsWithoutSelfLoops(product2);
            
            // Compare products
            compareProducts(product1, product2);
            
            // Generate cleaned DOT files (without self-loops)
            System.out.println("Generating visualization files...");
            generateDotFileWithoutSelfLoops(product1, product1Name.replace(" ", "_") + "_NoSelfLoops.dot");
            generateDotFileWithoutSelfLoops(product2, product2Name.replace(" ", "_") + "_NoSelfLoops.dot");
            
            System.out.println();
            System.out.println("═══════════════════════════════════════════════════════════════════════════════");
            System.out.println("SUMMARY:");
            System.out.println("  - Parsed " + product1.transitions.size() + " transitions from " + product1Name);
            System.out.println("  - Parsed " + product2.transitions.size() + " transitions from " + product2Name);
            System.out.println("  - Extracted " + product1.alphabet.size() + " symbols from " + product1Name);
            System.out.println("  - Extracted " + product2.alphabet.size() + " symbols from " + product2Name);
            System.out.println("  - Generated cleaned DOT files without self-loops");
            System.out.println("═══════════════════════════════════════════════════════════════════════════════");
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

