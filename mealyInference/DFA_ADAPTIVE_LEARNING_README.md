# Adaptive DFA Learning with IKearnsVaziraniDFA

## Overview

This implementation provides adaptive DFA (Deterministic Finite Automaton) learning across multiple products, similar to the Mealy machine approach in `hi_single.java`, but adapted for DFA learning.

## Files Created

### 1. `hi_single_dfa.java`
Main file for adaptive DFA learning with tree reuse across products.

**Key Features:**
- Learns DFAs instead of Mealy machines (Boolean outputs instead of Word<String>)
- Uses `KearnsVaziraniDFA` for Product 0 (initial learning)
- Uses `IKearnsVaziraniDFA` for subsequent products (adaptive learning with tree reuse)
- Handles alphabet extension when new symbols appear in later products
- Canonicalizes tree discriminators to match new alphabet instances
- Tracks learning statistics for comparison

### 2. `IKearnsVaziraniDFABuilder.java`
Builder pattern class to simplify construction of `IKearnsVaziraniDFA` learners.

**Configuration Options:**
- `setAlphabet()` - Set the learning alphabet
- `setOracle()` - Set the membership oracle
- `setCounterexampleAnalyzer()` - Set CE analysis strategy (default: LINEAR_FWD)
- `setStartingState()` - Set initial state for adaptive learning

## Key Differences from Mealy Machine Learning

| Aspect | Mealy Machine (`hi_single.java`) | DFA (`hi_single_dfa.java`) |
|--------|----------------------------------|----------------------------|
| **Output Type** | `Word<String>` | `Boolean` |
| **Learner (Product 0)** | `KearnsVaziraniMealy` | `KearnsVaziraniDFA` |
| **Learner (Product 1+)** | `IKearnsVaziraniMealy` | `IKearnsVaziraniDFA` |
| **Tree Type** | `MultiDTree<String, Word<Word<String>>, StateInfo>` | `MultiDTree<String, Boolean, StateInfo>` |
| **Hypothesis** | `CompactMealy<String, Word<String>>` | `CompactDFA<String>` |
| **SUL Simulator** | `MealySimulatorSUL` | Custom `DFASimulatorSUL` |

## Adaptive Learning Process

### Product 0 (Initial Learning)
1. Load DFA from DOT file
2. Create `KearnsVaziraniDFA` learner from scratch
3. Run learning experiment
4. **Extract discrimination tree** using reflection
5. Save hypothesis and tree for Product 1

### Product 1+ (Adaptive Learning)
1. Load new DFA from DOT file
2. **Extend alphabet** with new symbols from current product
3. **Canonicalize tree discriminators** to use new alphabet instances
4. Create adapted hypothesis with extended alphabet
5. Extract StateInfo objects from reused tree
6. Create `IKearnsVaziraniDFA` with:
   - Reused discrimination tree
   - Adapted hypothesis
   - Extended alphabet
7. Run learning (benefits from reused knowledge)
8. Update tree and hypothesis for next product

## Alphabet Handling

### Symbol Canonicalization
The implementation carefully handles alphabet symbol instances:

```java
// Product 0 symbols are Java objects with specific identities
String symbol1 = productAlphabet.getSymbol(0); // identity hash: 12345

// Product 1 needs to use the SAME object instances
// Otherwise queries fail with IllegalArgumentException
int idx = extendedAlphabet.getSymbolIndex("symbol");
String canonicalSymbol = extendedAlphabet.getSymbol(idx); // same identity
```

### Self-Loops for Unknown Symbols
Symbols not in current product get self-loop transitions:

```java
// For adaptive learning: symbols from Product 0 not in Product 1
for (String symbol : extendedAlphabet) {
    if (!productAlphabet.containsSymbol(symbol)) {
        // Add self-loop (reject state transitions to itself)
        mqDfa.setTransition(state, symbol, state);
    }
}
```

## Statistics Tracked

For each product, the implementation tracks:
- **Rounds**: Number of equivalence queries
- **MQ Resets**: Membership query resets
- **MQ Symbols**: Total symbols queried (MQ)
- **EQ Resets**: Equivalence query resets
- **EQ Symbols**: Total symbols queried (EQ)
- **States**: Final hypothesis size
- **Alphabet Size**: Number of input symbols
- **New Symbols**: Symbols added in current product

## Usage Example

```java
// Compile
javac -cp "lib/*:." hi_single_dfa.java IKearnsVaziraniDFABuilder.java

// Run
java -cp "lib/*:." hi_single_dfa

// The program will:
// 1. Learn Product 0 (00001_fsm.dot) from scratch
// 2. Learn Product 1 (00002_fsm.dot) reusing Product 0's tree
// 3. Print comparison statistics
```

## Tree Reuse Benefits

Adaptive learning with tree reuse provides:
- **Reduced membership queries** - Knowledge from Product 0 is reused
- **Faster convergence** - Discriminators already learned
- **Shared structure** - Common states identified immediately
- **Incremental refinement** - Only new distinctions need learning

## Technical Implementation Details

### Tree Extraction (Product 0)
```java
// Extract tree using reflection (not exposed in API)
Field treeField = KearnsVaziraniDFA.class.getDeclaredField("discriminationTree");
treeField.setAccessible(true);
MultiDTree<String, Boolean, StateInfo<String, Boolean>> tree = 
    (MultiDTree<String, Boolean, StateInfo<String, Boolean>>) treeField.get(learner);
```

### Starting State Creation (Product 1+)
```java
// Collect StateInfo objects from reused tree
List<StateInfo<String, Boolean>> stateInfosList = new ArrayList<>();
extractStateInfosFromTree(tree_round2.getRoot(), stateInfosList);

// Create starting state with tree, hypothesis, and state infos
KearnsVaziraniDFAState<String> startingState = new KearnsVaziraniDFAState<>(
    tree_round2,           // Reused discrimination tree
    adaptedHypothesis,     // Hypothesis with extended alphabet
    stateInfosList         // State information from tree
);

// Create learner with starting state
IKearnsVaziraniDFA<String> learner = new IKearnsVaziraniDFA<>(
    extendedAlphabet, mqOracle, AcexAnalyzers.LINEAR_FWD, startingState
);
```

### Discriminator Canonicalization
```java
// Replace old symbol instances with new alphabet instances
String[] canonicalSymbols = new String[oldDiscriminator.length()];
for (int i = 0; i < oldDiscriminator.length(); i++) {
    String oldSymbol = oldDiscriminator.getSymbol(i);
    int symbolIdx = newAlphabet.getSymbolIndex(oldSymbol);
    canonicalSymbols[i] = newAlphabet.getSymbol(symbolIdx); // Canonical instance
}
Word<String> newDiscriminator = Word.fromList(Arrays.asList(canonicalSymbols));

// Update discriminator using reflection
Field discriminatorField = node.getClass().getDeclaredField("discriminator");
discriminatorField.setAccessible(true);
discriminatorField.set(node, newDiscriminator);
```

## Limitations and Considerations

1. **Tree Extraction**: Uses reflection to access private fields
2. **Alphabet Matching**: Requires careful symbol identity management
3. **Product Order**: Products must be learned sequentially
4. **DFA Parsing**: Currently expects standard DOT format for DFAs
5. **Boolean Outputs**: Only suitable for DFA learning (accept/reject)

## Future Enhancements

- [ ] Support for more than 2 products
- [ ] Parallel product learning (where tree reuse allows)
- [ ] Better tree visualization and analysis
- [ ] Automatic alphabet inference
- [ ] Support for different DFA input formats
- [ ] Performance benchmarking framework

## References

- Original Mealy implementation: `hi_single.java`
- IKearnsVaziraniDFA: `IKearnsVaziraniDFA.java`
- LearnLib Documentation: http://learnlib.de/
- "An Introduction to Computational Learning Theory" by Kearns & Vazirani

## Contact

For questions or issues with this adaptive DFA learning implementation, refer to the original research paper on tree-based adaptive model learning.

