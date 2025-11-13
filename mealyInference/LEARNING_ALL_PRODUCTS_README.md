# Learning All Minepump_SPL Products

This directory contains two programs for learning all Minepump_SPL products:

## Files Created

1. **LearnAllProductsAdaptive.java** - Adaptive learning with tree reuse
2. **LearnAllProductsNormal.java** - Normal learning (each product from scratch)
3. **run_adaptive_learning.bat** - Batch script to run adaptive learning
4. **run_normal_learning.bat** - Batch script to run normal learning

## Programs

### 1. Adaptive Learning (LearnAllProductsAdaptive.java)

This program learns all products with **fresh Product 1 tree reuse**:
- **Product 1**: Learned from scratch and stored
- **Product 2**: Uses Product 1's tree/hypothesis (from first learning)
- **Product 3**: Product 1 is re-learned fresh, then Product 3 uses the fresh tree
- **Product 4**: Product 1 is re-learned fresh, then Product 4 uses the fresh tree
- ...and so on for all products

**Strategy:** Before learning each product (2-15), Product 1 is learned fresh to get a clean tree and hypothesis. This ensures each product starts with the same baseline.

**Learning Flow:**
```
Product 1 → [save tree/hyp] → Product 2
Learn Product 1 (fresh) → [save tree/hyp] → Product 3
Learn Product 1 (fresh) → [save tree/hyp] → Product 4
Learn Product 1 (fresh) → [save tree/hyp] → Product 5
...
Learn Product 1 (fresh) → [save tree/hyp] → Product 15
```

**Features:**
- Fresh Product 1 learning before each subsequent product
- Tree canonicalization for alphabet symbol matching
- Hypothesis adaptation with extended alphabet
- OMEGA self-loops for symbols not in current product
- Tracks new symbols added per product

### 2. Normal Learning (LearnAllProductsNormal.java)

This program learns all products **independently from scratch**:
- Each product is learned without any knowledge from previous products
- No tree reuse
- Standard Kearns-Vazirani learning algorithm

## Running the Programs

### Option 1: Using Batch Files (Easiest)

```batch
# For adaptive learning:
run_adaptive_learning.bat

# For normal learning:
run_normal_learning.bat
```

### Option 2: Using Maven Directly

```batch
# For adaptive learning:
mvn clean compile
mvn exec:java -Dexec.mainClass="LearnAllProductsAdaptive"

# For normal learning:
mvn clean compile
mvn exec:java -Dexec.mainClass="LearnAllProductsNormal"
```

## Output

Both programs generate **Excel files** with learning metrics:

### Adaptive Learning Output
- **File**: `AdaptiveLearning_Results_[timestamp].xlsx`
- **Columns**:
  - Product name
  - Rounds (EQ queries)
  - MQ Resets/Symbols
  - EQ Resets/Symbols
  - Number of states
  - Alphabet size
  - New symbols added
  - Learning type (Adaptive/Normal)

### Normal Learning Output
- **File**: `NormalLearning_Results_[timestamp].xlsx`
- **Columns**:
  - Product name
  - Rounds (EQ queries)
  - MQ Resets/Symbols
  - EQ Resets/Symbols
  - Number of states
  - Alphabet size
  - Learning type

## Comparing Results

After running both programs, you can:
1. Open both Excel files
2. Compare metrics side-by-side
3. Calculate savings from adaptive learning:
   - Total MQ/EQ reductions
   - Per-product improvements
   - Cumulative benefits

### Expected Results

**Adaptive learning should show:**
- ✓ Reduced queries for products 2-15 (tree reuse benefit)
- ✓ Faster learning for subsequent products
- ✓ Each product benefits from Product 1's learned structure
- ⚠ Note: Product 1 is re-learned 14 times (for products 2-15), so total includes these

**Normal learning shows:**
- Each product learned independently
- No efficiency gains from previous products
- Baseline for comparison

## Why Re-learn Product 1 Each Time?

This approach ensures:
1. **Consistent Baseline**: Each product starts with the same Product 1 tree/hypothesis
2. **Clean State**: No accumulated changes from previous products
3. **Fair Comparison**: Each product gets the same "starting knowledge"
4. **Isolation**: Issues in one product don't affect others

This is the strategy used in the research paper for adaptive SPL learning.

## Key Differences Between Programs

| Feature | Adaptive | Normal |
|---------|----------|--------|
| Tree reuse | ✓ Yes (from Product 1) | ✗ No |
| Hypothesis reuse | ✓ Yes | ✗ No |
| Alphabet extension | ✓ Yes | ✗ N/A |
| Learning approach | Incremental | Independent |
| Algorithm | IKearnsVaziraniMealy | KearnsVaziraniMealy |
| Experiment class | Experiment | Experiment1 |

## Troubleshooting

### NullPointerException on tree_round2
- Fixed with fallback to `learner.getDiscriminationTree()`
- Safety checks added before tree reuse

### FileOutputStream not found
- Fixed with import: `import java.io.FileOutputStream;`

### Excel file not generated
- Ensure Apache POI dependencies are in pom.xml (already added)
- Check console for errors during file write

### Compilation errors
- Run `mvn clean compile` first
- Check Java version (requires Java 11+)

## Notes

1. **Product 1 Learning**: Product 1 (00001_fsm.dot) is always learned from scratch in both approaches
2. **Tree Canonicalization**: Critical for adaptive learning - ensures alphabet symbol instances match
3. **OMEGA Symbols**: Used for undefined transitions in extended alphabets
4. **Performance**: Adaptive learning should be significantly faster for products 2-15

## Dependencies

Required in pom.xml (already configured):
- Apache POI 5.2.3 (for Excel export)
- LearnLib 0.16.0
- AutomataLib 0.10.0
- Apache Commons CLI 1.4

All dependencies are already configured in the pom.xml file.

