# Product Visualization and Comparison Guide

## Overview

`CompareAndVisualizeProducts.java` is a tool that loads, analyzes, and visualizes two product FSMs (Finite State Machines) from DOT files, showing their structure and alphabet without self-loops.

## Features

✓ **Load Mealy Machines** - Parses DOT files using the same method as App.java  
✓ **Extract Alphabets** - Shows all input and output symbols for each product  
✓ **Filter Self-Loops** - Creates clean visualizations without self-loop transitions  
✓ **Compare Products** - Analyzes differences in states, symbols, and complexity  
✓ **Visual Output** - Opens 4 visualization windows using LearnLib's Visualization API  

## Quick Start

### Option 1: Using Default Products (00001 vs 00002)

```batch
cd mealyInference
run_visualize_comparison.bat
```

### Option 2: Compare Specific Products

```batch
run_visualize_comparison.bat ^
  "..\alternative_experiments\Minepump_SPL\products_3wise\00001_fsm.dot" ^
  "..\alternative_experiments\Minepump_SPL\products_3wise\00004_fsm.dot" ^
  "Product 00001" ^
  "Product 00004"
```

### Option 3: Using Maven Directly

```batch
cd mealyInference
mvn compile
mvn exec:java -Dexec.mainClass="CompareAndVisualizeProducts"
```

## Command Line Arguments

```
CompareAndVisualizeProducts [product1_path] [product2_path] [name1] [name2]
```

**Arguments:**
- `product1_path` - Path to first product's DOT file
- `product2_path` - Path to second product's DOT file  
- `name1` - Display name for first product (optional)
- `name2` - Display name for second product (optional)

## Output

### Console Output

The tool provides detailed textual analysis:

1. **Loading Information** - States and symbols count for each product
2. **Product Details** - Full breakdown of states, alphabet, transitions
3. **Comparison Analysis** - Side-by-side comparison of both products
4. **Filtering Statistics** - How many self-loops were removed

### Visualization Windows

Four graphical windows will open:

1. **Product 1 (Original)** - With all transitions including self-loops
2. **Product 2 (Original)** - With all transitions including self-loops
3. **Product 1 (Filtered)** - Only non-self-loop transitions (cleaner view)
4. **Product 2 (Filtered)** - Only non-self-loop transitions (cleaner view)

## Example Output

```
╔═══════════════════════════════════════════════════════════════════════════════╗
║ PRODUCT: Product 00001                                                        ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║ States: 9                                                                     ║
║   Initial State: s0                                                           ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║ Input Alphabet: 17 symbols                                                    ║
║ Symbols:                                                                      ║
║   commandMsg, end, highLevel, isNotReady, isNotRunning, isReady, isRunning,  ║
║   isStopped, levelMsg, pumpStart, pumpStop, receiveMsg, setReady, setRunning,║
║   setStop, startCmd, stopCmd                                                  ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║ Transitions:                                                                  ║
║   Total: 153                                                                  ║
║   Self-loops: 144                                                             ║
║   Non-self-loop: 9                                                            ║
╚═══════════════════════════════════════════════════════════════════════════════╝
```

## Comparison Analysis Features

### Alphabet Analysis
- **Common symbols** - Symbols shared by both products
- **Unique symbols** - Symbols that appear in only one product
- **Symbol counts** - Total input/output alphabet sizes

### State Analysis
- **State count comparison** - Number of states in each product
- **Initial states** - Starting states for each FSM

### Complexity Metrics
- **Transition space** - States × Inputs (possible transition count)
- **Actual transitions** - How many are defined vs. possible
- **Self-loop ratio** - Percentage of self-loop transitions

## Understanding the Visualization

### With Self-Loops (Original)
Shows the complete FSM including:
- All state transitions
- Self-loops (state to itself)
- Complete behavior model

**Use case:** Full system understanding, complete specification

### Without Self-Loops (Filtered)
Shows only state-changing transitions:
- Inter-state transitions only
- Cleaner, more readable graph
- Focus on state evolution

**Use case:** Understanding product flow, comparing products, presentations

## Integration with Adaptive Learning

This tool helps understand why adaptive learning works or fails:

1. **Compare Product 1 with Product N** - See alphabet differences
2. **Analyze symbol overlap** - More overlap = better tree reuse
3. **Check state complexity** - Similar complexity benefits from shared knowledge
4. **Validate product ordering** - Ensure products are incrementally ordered

### Example: Analyzing Adaptive Learning Issues

```batch
REM Compare Product 1 (baseline) with Product 9 (showing issues)
run_visualize_comparison.bat ^
  "..\alternative_experiments\Minepump_SPL\products_3wise\00001_fsm.dot" ^
  "..\alternative_experiments\Minepump_SPL\products_3wise\00009_fsm.dot" ^
  "Product 00001 (Baseline)" ^
  "Product 00009 (Increased Measurements)"
```

Check the output for:
- ❌ Symbols in Product 9 but NOT in Product 1 (tree mismatch)
- ❌ Fewer total symbols in Product 9 (not incremental)
- ✓ All Product 1 symbols present in Product 9 (good ordering)

## Technical Details

### Similar to App.java

This tool uses the same infrastructure as `App.java`:

- **`loadMealyMachineFromDot()`** - Same DOT parsing logic
- **`CompactMealy`** - Same LearnLib Mealy machine representation
- **`Visualization.visualize()`** - Same visualization API
- **`Alphabet`** - Same symbol alphabet handling

### Differences from App.java

| Feature | App.java | CompareAndVisualizeProducts.java |
|---------|----------|----------------------------------|
| Purpose | Learning single product | Comparing two products |
| Visualization | 3 windows (hypothesis, original, tree) | 4 windows (2 products × 2 views) |
| Self-loops | Included | Filtered versions available |
| Comparison | None | Detailed alphabet/state comparison |
| Output | Statistics only | Structured comparison report |

## Use Cases

### 1. Product Ordering Validation
Verify that products are ordered incrementally:
```batch
REM Compare consecutive products
run_visualize_comparison.bat 00001_fsm.dot 00002_fsm.dot "P1" "P2"
run_visualize_comparison.bat 00002_fsm.dot 00003_fsm.dot "P2" "P3"
```

### 2. Debugging Adaptive Learning
When a product shows increased measurements:
```batch
REM Compare baseline with problematic product
run_visualize_comparison.bat 00001_fsm.dot 00015_fsm.dot "Baseline" "Problem"
```

### 3. Feature Analysis
Understand feature combinations across products:
- Check which inputs (features) are unique to each product
- Validate feature model consistency
- Identify missing/extra features

### 4. Presentation/Documentation
Generate clean visualizations without self-loops for:
- Research papers
- Presentations
- Documentation
- Product specifications

## Troubleshooting

### Compilation Errors
```batch
cd mealyInference
mvn clean compile
```

### Visualization Not Opening
- Ensure Java GUI libraries are installed
- Check X11/display settings on Linux
- Try running from IDE instead of command line

### File Not Found
- Use relative path from `mealyInference` directory
- Or use absolute paths
- Check file extension is `.dot`

### Out of Memory
For large products with many states:
```batch
set MAVEN_OPTS=-Xmx2g
run_visualize_comparison.bat
```

## Related Files

- **`App.java`** - Original single-product learning with visualization
- **`CompareAdaptiveVsNormal.java`** - Batch comparison of learning approaches
- **`LearnAllProductsAdaptive.java`** - Adaptive learning for all products
- **`TREE_REUSE_STRATEGY.md`** - Explains tree reuse approach

## Further Reading

- [Adaptive Learning Strategy](ADAPTIVE_LEARNING_STRATEGY.md)
- [Tree Reuse Strategy](TREE_REUSE_STRATEGY.md)
- [Understanding Prefixes](UNDERSTANDING_PREFIXES.md)
- LearnLib Documentation: https://learnlib.de/

