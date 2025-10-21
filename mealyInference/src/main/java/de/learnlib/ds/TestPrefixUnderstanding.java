package de.learnlib.ds;

import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;
import java.util.ArrayList;
import java.util.List;

/**
 * Test program to understand prefixes in the context of the actual learning algorithm
 */
public class TestPrefixUnderstanding {
    
    public static void main(String[] args) {
        System.out.println("TESTING PREFIX UNDERSTANDING");
        System.out.println("=".repeat(50));
        
        // Create alphabet similar to what might be used in the actual algorithm
        SimpleAlphabet<String> alphabet = new SimpleAlphabet<>();
        alphabet.add("input1");
        alphabet.add("input2");
        alphabet.add("input3");
        
        // Simulate what happens in the actual Kearns-Vazirani algorithm
        System.out.println("\nSIMULATING KEARNS-VAZIRANI PREFIX GENERATION:");
        
        // This simulates the code: transAs.add(accessSequence.append(symbol));
        List<Word<String>> prefixes = new ArrayList<>();
        
        // Simulate different access sequences (states in the hypothesis)
        String[] accessSequences = {"", "input1", "input1input2", "input2"};
        
        for (String accessSeq : accessSequences) {
            Word<String> accessSequence = Word.fromString(accessSeq);
            System.out.println("\nProcessing state with access sequence: '" + accessSeq + "'");
            
            // For each symbol in alphabet, create a prefix
            for (String symbol : alphabet) {
                Word<String> prefix = accessSequence.append(symbol);
                prefixes.add(prefix);
                System.out.println("  Created prefix: " + prefix + " (from '" + accessSeq + "' + '" + symbol + "')");
            }
        }
        
        // Analyze the resulting prefixes
        PrefixAnalyzer.analyzePrefixesWithAlphabet(prefixes, alphabet, "Generated Prefixes");
        
        // Show what these prefixes represent
        System.out.println("WHAT THESE PREFIXES REPRESENT:");
        System.out.println("Each prefix is a potential 'path' through the automaton:");
        System.out.println("• It starts from a known state (the access sequence)");
        System.out.println("• It adds one more input symbol");
        System.out.println("• The result is a path that might lead to a new state");
        System.out.println("• The sift() method determines which state this path leads to");
        System.out.println("• This is used to build/update the hypothesis automaton");
        
        System.out.println("\n" + "=".repeat(50));
    }
    
    /**
     * Helper method to create Word from string representation
     */
    private static Word<String> fromString(String str) {
        if (str.isEmpty()) {
            return Word.epsilon();
        }
        
        // Simple parsing - in real code this would be more sophisticated
        String[] parts = str.split("input");
        List<String> symbols = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty()) {
                symbols.add("input" + part);
            }
        }
        return Word.fromList(symbols);
    }
}
