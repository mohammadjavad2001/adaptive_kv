package de.learnlib.ds;

import de.learnlib.api.query.DefaultQuery;
import net.automatalib.words.Word;
import net.automatalib.words.Alphabet;
import java.util.List;
import java.util.ArrayList;

/**
 * Debugging tool to analyze what prefixes contain in the Kearns-Vazirani algorithm
 */
public class PrefixAnalyzer<I, O> {
    
    /**
     * Analyzes a list of prefixes and provides detailed information about their content
     */
    public static <I, O> void analyzePrefixes(List<Word<I>> prefixes, String context) {
        System.out.println("\n" + repeatString("=", 80));
        System.out.println("PREFIX ANALYSIS - " + context);
        System.out.println(repeatString("=", 80));
        
        System.out.println("Total number of prefixes: " + prefixes.size());
        System.out.println();
        
        for (int i = 0; i < prefixes.size(); i++) {
            Word<I> prefix = prefixes.get(i);
            System.out.println("Prefix " + (i + 1) + ":");
            System.out.println("  - Length: " + prefix.length());
            System.out.println("  - Content: " + prefix);
            System.out.println("  - Is empty: " + prefix.isEmpty());
            System.out.println("  - String representation: '" + prefix.toString() + "'");
            
            // Show individual symbols
            if (!prefix.isEmpty()) {
                System.out.println("  - Individual symbols:");
                for (int j = 0; j < prefix.length(); j++) {
                    I symbol = prefix.getSymbol(j);
                    System.out.println("    [" + j + "] = " + symbol + " (type: " + symbol.getClass().getSimpleName() + ")");
                }
            }
            System.out.println();
        }
        
        // Summary statistics
        int totalSymbols = prefixes.stream().mapToInt(Word::length).sum();
        int emptyPrefixes = (int) prefixes.stream().mapToInt(p -> p.isEmpty() ? 1 : 0).sum();
        int maxLength = prefixes.stream().mapToInt(Word::length).max().orElse(0);
        int minLength = prefixes.stream().mapToInt(Word::length).min().orElse(0);
        
        System.out.println("SUMMARY STATISTICS:");
        System.out.println("  - Total symbols across all prefixes: " + totalSymbols);
        System.out.println("  - Empty prefixes: " + emptyPrefixes);
        System.out.println("  - Maximum prefix length: " + maxLength);
        System.out.println("  - Minimum prefix length: " + minLength);
        System.out.println("  - Average prefix length: " + (prefixes.isEmpty() ? 0 : (double) totalSymbols / prefixes.size()));
        
        System.out.println(repeatString("=", 80) + "\n");
    }
    
    /**
     * Analyzes prefixes with alphabet context for better understanding
     */
    public static <I, O> void analyzePrefixesWithAlphabet(List<Word<I>> prefixes, Alphabet<I> alphabet, String context) {
        analyzePrefixes(prefixes, context);
        
        System.out.println("ALPHABET CONTEXT:");
        System.out.println("  - Alphabet size: " + alphabet.size());
        System.out.println("  - Alphabet symbols: " + alphabet);
        System.out.println();
        
        // Check which symbols from the alphabet appear in the prefixes
        System.out.println("SYMBOL USAGE IN PREFIXES:");
        for (I symbol : alphabet) {
            int count = 0;
            for (Word<I> prefix : prefixes) {
                for (int i = 0; i < prefix.length(); i++) {
                    if (symbol.equals(prefix.getSymbol(i))) {
                        count++;
                    }
                }
            }
            System.out.println("  - Symbol '" + symbol + "': appears " + count + " times");
        }
        System.out.println();
    }
    
    /**
     * Creates a detailed report of what prefixes represent in the learning context
     */
    public static <I, O> void createPrefixReport(List<Word<I>> prefixes, String methodName) {
        System.out.println("\n" + "PREFIX REPORT FOR: " + methodName);
        System.out.println(repeatString("=", 60));
        
        System.out.println("WHAT ARE PREFIXES?");
        System.out.println("In the Kearns-Vazirani algorithm, 'prefixes' represent:");
        System.out.println("1. Access sequences to states in the hypothesis automaton");
        System.out.println("2. Input words that lead to specific states");
        System.out.println("3. The 'path' through the automaton to reach each state");
        System.out.println();
        
        System.out.println("CURRENT PREFIXES ANALYSIS:");
        if (prefixes.isEmpty()) {
            System.out.println("  WARNING: No prefixes provided - this might indicate an issue");
        } else {
            System.out.println("  SUCCESS: " + prefixes.size() + " prefixes found");
            
            // Categorize prefixes by length
            System.out.println("\nPREFIX LENGTH DISTRIBUTION:");
            int[] lengthCounts = new int[10]; // Assuming max length of 10 for display
            for (Word<I> prefix : prefixes) {
                int len = Math.min(prefix.length(), lengthCounts.length - 1);
                lengthCounts[len]++;
            }
            
            for (int i = 0; i < lengthCounts.length; i++) {
                if (lengthCounts[i] > 0) {
                    System.out.println("  - Length " + i + ": " + lengthCounts[i] + " prefixes");
                }
            }
            
            // Show examples
            System.out.println("\nEXAMPLE PREFIXES:");
            int examplesToShow = Math.min(5, prefixes.size());
            for (int i = 0; i < examplesToShow; i++) {
                Word<I> prefix = prefixes.get(i);
                System.out.println("  " + (i + 1) + ". " + prefix + " (length: " + prefix.length() + ")");
            }
            
            if (prefixes.size() > examplesToShow) {
                System.out.println("  ... and " + (prefixes.size() - examplesToShow) + " more");
            }
        }
        
        System.out.println("\n" + repeatString("=", 60) + "\n");
    }
    
    /**
     * Helper method to repeat a string (Java 8 compatibility)
     */
    private static String repeatString(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
