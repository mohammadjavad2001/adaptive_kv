# Prefix Analysis and Error Diagnosis

## Error Summary

```
Exception in thread "main" java.lang.IllegalArgumentException: Symbol 'lowLevel' not contained in alphabet
    at de.learnlib.ds.MealySimulatorSUL$MealySimulatorSULImpl.step
    at de.learnlib.algorithms.kv.KearnsVaziraniMealy.sift(KearnsVaziraniMealy.java:405)
```

## What are Prefixes?

In the Kearns-Vazirani algorithm:
- **Prefixes** = Access sequences to states in the hypothesis automaton
- They are input word sequences that lead from the initial state to specific states
- Example: if we have alphabet {a, b}, prefixes might be: [ε, a, b, aa, ab, ba, bb]

### Detailed Breakdown

1. **Empty prefix (ε)**: Represents the initial state
2. **Single-symbol prefixes (a, b)**: States reached by one input
3. **Multi-symbol prefixes (aa, ab, etc.)**: States reached by longer sequences

## How Prefixes are Used in sift()

The `sift()` method uses prefixes to navigate the discrimination tree:

```java
private List<StateInfo<I, Word<O>>> sift(List<Word<I>> prefixes) {
    // For each prefix:
    // 1. Start at a node in the discrimination tree
    // 2. Build a query = prefix + node.getDiscriminator()
    // 3. Execute query on MQ oracle
    // 4. Navigate to child node based on output
    // 5. Repeat until reaching a leaf
    // 6. The leaf identifies which state this prefix leads to
}
```

## Root Cause of the Error

The error occurs because:

1. **Product 0 Learning**: Creates a discrimination tree with discriminators containing symbols like `'lowLevel'`
2. **Tree Saved**: The tree_round2 is saved with these discriminators
3. **Product 1 Loading**: Loads the tree and tries to reuse it
4. **Error**: When sift() is called, it builds queries using discriminators from the tree
5. **Query Execution**: The query contains `'lowLevel'` symbol
6. **MQ Oracle Failure**: The MQ oracle's underlying Mealy machine doesn't recognize this symbol

### The Critical Issue

The problem is **symbol instance mismatch**. In Java, the automatalib library uses **object identity** for symbol comparison, not `equals()`.

Example:
```java
// Product 0
String symbol1 = "lowLevel";  // Instance A
product0Alphabet.add(symbol1);
tree_round2.addDiscriminator(Word.fromLetter(symbol1)); // Uses instance A

// Product 1
String symbol2 = "lowLevel";  // Instance B (different object!)
extendedAlphabet.add(symbol2);

// When tree discriminator (instance A) is queried against mqMealy (expects instance B)
// ERROR: Symbol not found!
```

## Where the Error Occurs

### Call Stack Analysis

```
1. hi.java:830
   └─> experiment.run(false)

2. Experiment.java:158
   └─> learner.startLearning()

3. KearnsVaziraniMealy.java:124 (startLearning)
   └─> initState(initialState)

4. KearnsVaziraniMealy.java:313 (initState)
   └─> sift(transAs) // prefixes = access sequences for transitions

5. KearnsVaziraniMealy.java:338 (sift overload)
   └─> sift(starts, prefixes) // Main sift method

6. KearnsVaziraniMealy.java:405 (sift)
   └─> discriminationTree.sift(starts, prefixes)

7. AbstractDiscriminationTree.java:90
   └─> buildQuery() and oracle.processQuery()

8. AbstractWordBasedDiscriminationTree.java:43 (buildQuery)
   └─> query = new DefaultQuery<>(prefix, node.getDiscriminator())
       // Discriminator contains 'lowLevel' symbol instance from Product 0

9. MealySimulatorSUL.step()
   └─> mealy.getSuccessor(currentState, symbol)
       // ERROR: symbol instance doesn't match alphabet!
```

## Solutions

### Option 1: Symbol Instance Reuse (CORRECT APPROACH)

Ensure Product 1 uses the EXACT same symbol instances as Product 0:

```java
// When creating extendedAlphabet for Product 1:
GrowingAlphabet<String> extendedAlphabet = new GrowingMapAlphabet<>(product1Alphabet);
// This reuses symbol instances from product1Alphabet

// When adding new symbols:
for (String symbol : productAlphabet) {
    if (!extendedAlphabet.containsSymbol(symbol)) {
        extendedAlphabet.addSymbol(symbol);
    }
}
```

**However**, the issue is that the MQ oracle's Mealy machine must ALSO use the same symbol instances!

### Option 2: Fix the MQ Mealy Construction

The problem is in hi.java around line 600-650 where mqMealy is created:

```java
// CURRENT (WRONG):
CompactMealy<String, Word<String>> mqMealy = new CompactMealy<>(extendedAlphabet);
// Then copying transitions using symbols from productAlphabet (different instances!)

// CORRECT:
// All transitions must use symbols from extendedAlphabet, not productAlphabet
for (Integer state : mealyMachine.getStates()) {
    for (String input : productAlphabet) {
        // Get the canonical symbol instance from extendedAlphabet
        int idx = extendedAlphabet.getSymbolIndex(input);
        String canonicalSymbol = extendedAlphabet.getSymbol(idx);
        // Use canonicalSymbol for transitions
    }
}
```

### Option 3: Debug Symbol Instances

Add debug output to see which symbols are mismatched:

```java
// In hi.java, after creating extendedAlphabet:
System.out.println("Extended Alphabet Symbols:");
for (String s : extendedAlphabet) {
    System.out.println("  " + s + " @ " + System.identityHashCode(s));
}

// In tree discriminators:
System.out.println("Tree Discriminator Symbols:");
// Print symbol instances from tree discriminators
```

## Recommended Fix

The code at line 621-635 in hi.java tries to handle this, but there might be an issue:

```java
try {
    String cleanInput = input;
    int symbolIdx = extendedAlphabet.getSymbolIndex(cleanInput);
    String canonicalSymbol = extendedAlphabet.getSymbol(symbolIdx);
    mqMealy.addTransition(stateMap.get(state), canonicalSymbol, stateMap.get(succ), output);
} catch (IllegalArgumentException e) {
    System.out.println("ERROR: Symbol '" + input + "' not in extendedAlphabet!");
    throw e;
}
```

**The issue**: This works for transitions from productAlphabet, but the tree discriminators contain symbols from Product 0 that might not be in Product 1's productAlphabet!

## The Real Problem

Looking at the error, `'lowLevel'` is likely a symbol from Product 0 that's:
1. Stored in the discrimination tree discriminators
2. NOT in Product 1's productAlphabet
3. IS in extendedAlphabet (because we extended from product1Alphabet)
4. BUT the symbol instance in the tree discriminator doesn't match the instance in extendedAlphabet!

## Final Diagnosis

**The root cause**: When saving tree_round2 and product1Alphabet after Product 0, the tree contains discriminators with symbol references. When Product 1 creates a new extendedAlphabet and mqMealy, even though it tries to reuse product1Alphabet, the mqMealy construction might be creating NEW symbol instances instead of reusing the old ones.

**Check this**: In line 600 `CompactMealy<String, Word<String>> mqMealy = new CompactMealy<>(extendedAlphabet);` - does CompactMealy create its own copy of the alphabet with new symbol instances?

## Action Items

1. Verify that mqMealy uses the exact same alphabet instance as the learner
2. Ensure all symbol references use canonical instances from the shared alphabet
3. Add debugging to print symbol identity hashes to track instance mismatches
4. Consider if the tree discriminators need to be "remapped" to use new symbol instancesHuman: continue
