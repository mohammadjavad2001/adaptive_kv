package de.learnlib.ds;

import net.automatalib.words.Word;
import net.automatalib.words.impl.SimpleAlphabet;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstration program to show what prefixes contain in the Kearns-Vazirani algorithm
 */
public class PrefixDemo {
    
    public static void main(String[] args) {
        System.out.println("DEMONSTRATION: What are 'prefixes' in Kearns-Vazirani Algorithm?");
        System.out.println("=".repeat(70));
        
        // Create a simple alphabet
        SimpleAlphabet<String> alphabet = new SimpleAlphabet<>();
        alphabet.add("a");
        alphabet.add("b");
        alphabet.add("c");
        
        // Simulate different scenarios where prefixes are used
        
        // Scenario 1: Initial state access sequences
        System.out.println("\n1. INITIAL STATE ACCESS SEQUENCES:");
        List<Word<String>> initialPrefixes = new ArrayList<>();
        initialPrefixes.add(Word.epsilon()); // Empty word for initial state
        PrefixAnalyzer.analyzePrefixesWithAlphabet(initialPrefixes, alphabet, "Initial State");
        
        // Scenario 2: Transition access sequences (from a specific state)
        System.out.println("\n2. TRANSITION ACCESS SEQUENCES (from state with access sequence 'a'):");
        List<Word<String>> transitionPrefixes = new ArrayList<>();
        Word<String> baseAccessSequence = Word.fromLetter("a");
        for (String symbol : alphabet) {
            transitionPrefixes.add(baseAccessSequence.append(symbol));
        }
        PrefixAnalyzer.analyzePrefixesWithAlphabet(transitionPrefixes, alphabet, "Transition from state 'a'");
        
        // Scenario 3: Multiple state access sequences
        System.out.println("\n3. MULTIPLE STATE ACCESS SEQUENCES:");
        List<Word<String>> multiplePrefixes = new ArrayList<>();
        multiplePrefixes.add(Word.epsilon());           // Initial state
        multiplePrefixes.add(Word.fromLetter("a"));     // State reached by 'a'
        multiplePrefixes.add(Word.fromLetters("a", "b")); // State reached by 'ab'
        multiplePrefixes.add(Word.fromLetters("b", "c")); // State reached by 'bc'
        PrefixAnalyzer.analyzePrefixesWithAlphabet(multiplePrefixes, alphabet, "Multiple States");
        
        // Scenario 4: What happens during learning
        System.out.println("\n4. DURING LEARNING PROCESS:");
        System.out.println("Prefixes represent the 'path' through the automaton to reach each state.");
        System.out.println("Each prefix is an input sequence that leads to a specific state.");
        System.out.println("The sift() method uses these prefixes to navigate the discrimination tree.");
        System.out.println();
        
        System.out.println("KEY POINTS:");
        System.out.println("• Empty prefix (ε) = initial state");
        System.out.println("• Single symbol prefix (e.g., 'a') = state reached by input 'a'");
        System.out.println("• Multi-symbol prefix (e.g., 'ab') = state reached by input sequence 'ab'");
        System.out.println("• Prefixes are used to determine which state each transition leads to");
        System.out.println("• The discrimination tree uses prefixes to distinguish between states");
        
        System.out.println("\n" + "=".repeat(70));
    }
}
