# Active Context

## Current Focus
Implementing and testing **incremental adaptive learning** for multiple SPL products using Kearns-Vazirani algorithm.

## Recent Changes

### Implemented Features
1. **GrowingMapAlphabet Integration**
   - Created Product 1 alphabet as `GrowingMapAlphabet` to support dynamic symbol addition
   - Enables `addAlphabetSymbol()` for subsequent products

2. **Incremental Tree Reuse**
   - Products 2+ load tree from PREVIOUS product (not just first)
   - Both `tree_round2` AND `product1Alphabet` updated after each product
   - Creates chain: P1→tree1→P2→tree2→P3→tree3

3. **Dynamic Symbol Addition**
   - Loop through current product alphabet
   - Add new symbols not in previous alphabet
   - Use `learner.addAlphabetSymbol(symbol)` for safe extension

4. **Comprehensive Metrics**
   - Rounds (EQ queries) count
   - Membership queries (Resets + Symbols)
   - Equivalence queries (Resets + Symbols)
   - States count, alphabet size changes
   - Special "ADAPTIVE LEARNING BENEFIT" output for products 2+

### Bug Fixes
1. **Alphabet Compatibility Error** ✓
   - Problem: "Symbol 'X' is not contained in alphabet"
   - Solution: Create learner with previous alphabet, then add symbols

2. **GrowingAlphabetNotSupportedException** ✓
   - Problem: Regular alphabet doesn't support growth
   - Solution: Use `GrowingMapAlphabet` from Product 1

3. **Return Type Incompatibility** ✓
   - Problem: `getHypothesisModel()` return type mismatch
   - Solution: Double cast with `@SuppressWarnings`

## Next Steps
1. **Testing**: Run multi-product learning experiment
2. **Validation**: Verify query reduction across products
3. **Metrics Analysis**: Compare all metrics between products
4. **Optimization**: Fine-tune tree reuse strategy if needed

## Active Decisions
- Using **incremental** approach (not just reusing first tree)
- Updating both tree AND alphabet after each product
- Supporting arbitrary number of products (2+)
- Tracking all metrics for comprehensive evaluation

## Important Patterns
```java
// Product 0
product1Alphabet = new GrowingMapAlphabet<>(productAlphabet);
learner = builder.withAlphabet(product1Alphabet).create(null);
// ... learn ...
tree_round2 = experiment.getDiscrtree();
product1Alphabet = learner.get_alphabet_symbol();  // Update

// Product 1+
learner = builder.withAlphabet(product1Alphabet).create(tree_round2);
for (symbol : productAlphabet) {
    if (!product1Alphabet.containsSymbol(symbol)) {
        learner.addAlphabetSymbol(symbol);
    }
}
// ... learn ...
tree_round2 = experiment.getDiscrtree();           // Update
product1Alphabet = learner.get_alphabet_symbol();  // Update
```

## Known Issues
- None currently - all major issues resolved

## Learnings
- Java's wildcard types require explicit casting for compatibility
- Alphabet must be growable BEFORE creating learner
- Both tree and alphabet must be updated, not just tree
- Symbol addition must happen AFTER tree loading

