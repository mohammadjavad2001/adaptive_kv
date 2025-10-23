# Summary: Prefix Analysis and Error Diagnosis

## What Was Done

### 1. Added Prefix Analysis to Kearns-Vazirani Algorithm

Modified `mealyInference/src/main/java/de/learnlib/algorithms/kv/KearnsVaziraniMealy.java` to include detailed analysis of what "prefixes" contain during the learning process.

**Location**: Lines 333-403 in the `sift()` method

**What it shows**:
- Total number of prefixes
- Detailed breakdown of each prefix:
  - Length
  - Content (the actual input sequence)
  - Individual symbols
- Summary statistics:
  - Total symbols across all prefixes
  - Empty prefixes count
  - Maximum/minimum/average prefix length
- Explanation of what prefixes represent in the algorithm

### 2. Created Comprehensive Documentation

Created two documentation files:

#### `UNDERSTANDING_PREFIXES.md`
- Explains what prefixes are in the Kearns-Vazirani algorithm
- Provides examples of different types of prefixes
- Shows how prefixes are used in the sift() method
- Describes the role of prefixes in state identification

#### `PREFIX_AND_ERROR_ANALYSIS.md`
- Complete analysis of the error that occurred
- Root cause diagnosis
- Call stack breakdown
- Multiple solution approaches
- Action items for fixing the issue

## What Are Prefixes?

**Simple Answer**: Prefixes are input sequences (words) that represent paths through the hypothesis automaton from the initial state to specific states.

**Examples**:
- Empty prefix `ε` = initial state
- Single symbol `a` = state reached by input 'a'
- Multi-symbol `ab` = state reached by input sequence 'ab'

**How They're Used**:
1. The `sift()` method receives a list of prefixes (access sequences)
2. For each prefix, it navigates the discrimination tree
3. At each tree node, it builds a query: `query = prefix + discriminator`
4. The query is executed on the MQ oracle
5. Based on the output, it moves to a child node
6. This continues until a leaf is reached
7. The leaf identifies which state the prefix leads to

## Error Analysis

### The Error
```
Exception in thread "main" java.lang.IllegalArgumentException: Symbol 'lowLevel' not contained in alphabet
```

### Root Cause
The error occurs due to **symbol instance mismatch**. The automatalib library uses object identity (not `equals()`) for symbol comparison.

**The Problem Flow**:
1. Product 0 creates discrimination tree with discriminators containing symbols (e.g., `'lowLevel'`)
2. These symbol instances are stored in the tree
3. Product 1 loads the tree and tries to reuse it
4. When `sift()` is called, it queries the MQ oracle using discriminators from the tree
5. The discriminator contains a symbol instance from Product 0
6. The MQ oracle's Mealy machine expects symbol instances from Product 1's alphabet
7. **Mismatch**: Different object instances, even if string value is the same
8. **Result**: IllegalArgumentException

### Where It Happens

```
hi.java:830 (experiment.run)
  → Experiment.java:158 (learner.startLearning)
  → KearnsVaziraniMealy.java:124 (startLearning → initState)
  → KearnsVaziraniMealy.java:313 (initState → sift)
  → KearnsVaziraniMealy.java:405 (discriminationTree.sift)
  → AbstractDiscriminationTree.java:90 (buildQuery + processQuery)
  → MealySimulatorSUL.step
  → ERROR: Symbol not in alphabet!
```

### Why It's Complex

The issue is that:
1. The tree discriminators reference symbol instances from Product 0
2. Product 1 creates new symbol instances (even with same string values)
3. The MQ oracle's Mealy machine uses Product 1's symbol instances
4. When tree discriminator (Product 0 instance) is used to query MQ oracle (Product 1 instance), they don't match

### Specific Problem in Code

In `hi.java`:
- Line 582: Creates `extendedAlphabet` from `product1Alphabet` (good - reuses instances)
- Line 600: Creates `mqMealy` with `extendedAlphabet` (should be good)
- Line 617-638: Copies transitions using symbols from `productAlphabet`
- **Issue**: The code tries to get canonical symbols (line 628), but...
- **Problem**: When the tree discriminator contains `'lowLevel'` (from Product 0), and `'lowLevel'` is NOT in Product 1's `productAlphabet`, the symbol instance in the tree might not match the instance in `extendedAlphabet`

## Solutions

### Immediate Fix Needed

The `mqMealy` construction needs to ensure ALL symbols (including those only in Product 0) use the exact same instances as in `extendedAlphabet`:

```java
// After line 600 in hi.java
CompactMealy<String, Word<String>> mqMealy = new CompactMealy<>(extendedAlphabet);

// Verify alphabet identity
System.out.println("mqMealy alphabet == extendedAlphabet: " + 
    (mqMealy.getInputAlphabet() == extendedAlphabet));
```

### Long-term Solution

Consider implementing symbol remapping when loading a tree:
1. When tree is loaded, scan all discriminators
2. For each symbol in discriminators, look it up in the new alphabet
3. Replace symbol instances with canonical instances from new alphabet
4. This ensures tree discriminators always use current alphabet instances

## Files Modified

1. **mealyInference/src/main/java/de/learnlib/algorithms/kv/KearnsVaziraniMealy.java**
   - Added prefix analysis in `sift()` method (lines 333-403)
   - Added `repeatString()` helper method (lines 326-335)

## Files Created

1. **mealyInference/UNDERSTANDING_PREFIXES.md**
   - Comprehensive explanation of prefixes in Kearns-Vazirani

2. **mealyInference/PREFIX_AND_ERROR_ANALYSIS.md**
   - Detailed error analysis and solutions

3. **mealyInference/SUMMARY_PREFIX_ANALYSIS.md** (this file)
   - Summary of all work done

## Next Steps

1. **Fix the symbol instance mismatch issue** in hi.java:
   - Verify mqMealy uses the exact same alphabet instance
   - Ensure all symbol references come from a single canonical source
   - Add debug output to track symbol instance identity

2. **Test the fix**:
   - Run Product 0 learning
   - Run Product 1 learning with tree reuse
   - Verify no symbol mismatch errors

3. **Enhance debugging**:
   - Add symbol identity hash printing
   - Track symbol instances through the entire flow
   - Verify tree discriminators use correct instances

## How to Use the Prefix Analysis

When you run the learning algorithm now, you'll see detailed output like:

```
================================================================================
PREFIX ANALYSIS - SIFT METHOD
================================================================================
Total number of prefixes: 3

Prefix 1:
  - Length: 0
  - Content: ε
  - Is empty: true
  - String representation: ''

Prefix 2:
  - Length: 1
  - Content: [startCmd]
  - Is empty: false
  - String representation: 'startCmd'
  - Individual symbols:
    [0] = startCmd (type: String)

...

SUMMARY STATISTICS:
  - Total symbols across all prefixes: 5
  - Empty prefixes: 1
  - Maximum prefix length: 2
  - Minimum prefix length: 0
  - Average prefix length: 1.67

WHAT ARE PREFIXES?
In the Kearns-Vazirani algorithm, 'prefixes' represent:
1. Access sequences to states in the hypothesis automaton
2. Input words that lead to specific states
3. The 'path' through the automaton to reach each state
4. Used by sift() method to navigate the discrimination tree
================================================================================
```

This helps you understand exactly what input sequences are being processed during the learning process.

