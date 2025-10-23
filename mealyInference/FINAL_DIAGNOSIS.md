# Final Diagnosis: Symbol Mismatch Error

## The Paradox

From the debug output, we see:
1. ✅ `extendedAlphabet` contains `'lowLevel '` (hash: 749981943)
2. ✅ `mqMealy.getInputAlphabet() == extendedAlphabet` returns `true`  
3. ✅ Transitions added for `'lowLevel '` using canonical symbol (hash: 749981943)
4. ❌ **ERROR**: `Symbol 'lowLevel ' is not contained in the alphabet`

## The Real Problem

The error occurs at `MealySimulatorSUL$MealySimulatorSULImpl.step()`, which calls:
```java
mealy.getTransition(currentState, symbol)
```

This internally calls `getSymbolIndex(symbol)` which fails.

**Key Insight**: Even though `mqMealy.getInputAlphabet() == extendedAlphabet` is true, the symbol lookup inside `MealySimulatorSUL` is using a DIFFERENT symbol instance!

## Where the Different Instance Comes From

The flow is:
1. Tree discriminator contains symbol instance from Product 0
2. Discriminator is used in a query: `prefix + discriminator`
3. Query is executed on MQ oracle
4. MQ oracle tries to step through mqMealy with discriminator symbol
5. **Discriminator symbol instance ≠ extendedAlphabet symbol instance**
6. ERROR!

## The Evidence

Looking at line 800 of terminal output:
```
Root discriminator: levelMsg
```

This `levelMsg` symbol is from Product 0's alphabet. When the tree uses this discriminator to query the MQ oracle, it passes this EXACT symbol instance.

But Product 1's `extendedAlphabet` has a DIFFERENT instance of `levelMsg ` (even if the string value matches).

## The Solution

We need to ensure that when creating `extendedAlphabet` for Product 1, we use the EXACT SAME symbol instances as Product 0's `product1Alphabet`.

The code does this:
```java
GrowingAlphabet<String> extendedAlphabet = new GrowingMapAlphabet<>(product1Alphabet);
```

This SHOULD reuse the symbol instances. Let me verify if `product1Alphabet` is the same instance that was saved from Product 0.

## Hypothesis

The problem might be that `product1Alphabet` isn't being properly saved/restored, OR the tree discriminators are using different symbol instances than `product1Alphabet`.

## Next Step

We need to verify the symbol instance identity hashes:
1. Print hash of symbols in `product1Alphabet` (Product 0)
2. Print hash of symbols in tree discriminators
3. Print hash of symbols in `extendedAlphabet` (Product 1)
4. Confirm they all match

