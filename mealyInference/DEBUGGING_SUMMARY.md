# Debugging Summary: Symbol Mismatch Error

## What We Know

### The Error
```
Symbol 'lowLevel ' is not contained in the alphabet
```

### What We've Confirmed

1. ✅ **Prefixes are working correctly**
   - They represent access sequences to states
   - All 23 symbols are included as prefixes
   - Each prefix has length 1 (single symbols from the alphabet)

2. ✅ **Extended Alphabet Creation**
   - `extendedAlphabet` size: 23 symbols
   - Contains `'lowLevel '` with hash `749981943`
   - All symbols have trailing spaces (from DOT parser)

3. ✅ **MQ Mealy Construction**
   - `mqMealy` created with `extendedAlphabet`
   - `mqMealy.getInputAlphabet() == extendedAlphabet` returns `true`
   - Transitions added for all 19 symbols in Product 1
   - `'lowLevel '` transitions added using canonical symbol (hash: 749981943)
   - OMEGA self-loops added for 4 symbols from Product 0 not in Product 1

4. ✅ **Learner Alphabet**
   - Learner alphabet size: 23 symbols
   - Contains all Product 0 symbols + Product 1 new symbols

### The Mystery

Despite all the above being correct, the error still occurs when:
1. `sift()` method queries the discrimination tree
2. Tree uses a discriminator (e.g., `levelMsg`)
3. Query is built: `prefix + discriminator`
4. Query is executed on MQ oracle
5. `MealySimulatorSUL.step()` tries to get transition
6. **ERROR**: Symbol not found

### Key Observations

1. **Tree Alphabet** (Product 0, 17 symbols):
   - Does NOT include Product 1's new symbols (`lowLevel`, etc.)
   - This is expected and correct

2. **Tree Discriminator**:
   - Root discriminator: `levelMsg`
   - This symbol is from Product 0's alphabet

3. **The Paradox**:
   - `mqMealy` alphabet contains `levelMsg ` (with space)
   - Transitions exist for `levelMsg ` 
   - But when queried, symbol is "not found"

## Hypothesis

**The tree discriminator contains a symbol instance from Product 0's alphabet that is DIFFERENT from the symbol instance in Product 1's extendedAlphabet, even though they have the same string value.**

### Why This Happens

When Product 0 learns:
1. Symbols are loaded from DOT file (e.g., `"levelMsg "`)
2. `product1Alphabet` is created with these symbol instances
3. Tree discriminators reference these symbol instances
4. Both tree and `product1Alphabet` are saved

When Product 1 loads:
1. Symbols are loaded from DOT file (NEW instances of `"lowLevel "` etc.)
2. `extendedAlphabet` is created from saved `product1Alphabet`
3. New symbols are added to `extendedAlphabet`
4. Tree is loaded (discriminators still reference OLD Product 0 instances)
5. **Problem**: When tree discriminator queries mqMealy, it uses OLD symbol instance
6. mqMealy expects NEW symbol instances from extendedAlphabet

## The Debug Output We Need

We've added debugging to `MealySimulatorSUL.java` to print:
1. The exact symbol being queried (and its identity hash)
2. All symbols in mqMealy's alphabet (and their identity hashes)
3. Whether the queried symbol `.equals()` any symbol in the alphabet

This will confirm if:
- The queried symbol has a different identity hash than the alphabet symbols
- The queried symbol `.equals()` the alphabet symbol (string equality) but isn't the same instance (object identity)

## Expected Output

We expect to see something like:
```
========== SYMBOL MISMATCH ERROR DETECTED ==========
Querying symbol: 'lowLevel '
Symbol identity hash: 123456789  ← Different hash!
Symbol class: java.lang.String
Current state: 0

Mealy alphabet symbols:
  'lowLevel ' (hash: 749981943, equals: true)  ← Same string, different instance!
  ...
====================================================
```

This would confirm the symbol instance mismatch theory.

## The Solution

Once confirmed, the fix is to ensure that when creating `extendedAlphabet` and `mqMealy` for Product 1, we use the EXACT same symbol instances as the tree discriminators.

Options:
1. **Remap tree discriminators**: Scan tree, replace all symbol instances with canonical instances from extendedAlphabet
2. **Use Product 0's alphabet directly**: Don't create new symbol instances for Product 1
3. **Symbol interning**: Use String interning to ensure same string = same instance

