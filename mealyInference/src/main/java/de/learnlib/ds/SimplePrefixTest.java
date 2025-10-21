package de.learnlib.ds;

import net.automatalib.words.Word;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple test to demonstrate what prefixes contain
 */
public class SimplePrefixTest {
    
    public static void main(String[] args) {
        System.out.println("SIMPLE PREFIX TEST");
        System.out.println("==================");
        
        // Create some example prefixes
        List<Word<String>> prefixes = new ArrayList<>();
        
        // Empty prefix (initial state)
        prefixes.add(Word.epsilon());
        
        // Single symbol prefixes
        prefixes.add(Word.fromLetter("a"));
        prefixes.add(Word.fromLetter("b"));
        
        // Multi-symbol prefixes
        prefixes.add(Word.fromLetters("a", "b"));
        prefixes.add(Word.fromLetters("b", "a"));
        prefixes.add(Word.fromLetters("a", "a", "b"));
        
        // Analyze the prefixes
        PrefixAnalyzer.analyzePrefixes(prefixes, "Simple Test");
        
        System.out.println("WHAT THESE PREFIXES REPRESENT:");
        System.out.println("1. Empty prefix (ε) = initial state");
        System.out.println("2. Single symbol (a, b) = states reached by one input");
        System.out.println("3. Multi-symbol (ab, ba, aab) = states reached by input sequences");
        System.out.println();
        System.out.println("In the Kearns-Vazirani algorithm:");
        System.out.println("- These prefixes are used to navigate the discrimination tree");
        System.out.println("- Each prefix helps determine which state a transition leads to");
        System.out.println("- They represent the 'path' through the automaton");
    }
}
