# Technical Context

## Technology Stack

### Core Technologies
- **Language**: Java
- **Build Tool**: Maven (pom.xml)
- **IDE**: Eclipse

### Key Libraries

#### Learning Library (LearnLib)
- **Purpose**: Automata learning framework
- **Key Classes**:
  - `LearningAlgorithm.MealyLearner<I, O>`
  - `MembershipOracle<I, Word<O>>`
  - `EquivalenceOracle<...>`
  - `Experiment.MealyExperiment<I, O>`

#### AutomataLib
- **Purpose**: Automata data structures
- **Key Classes**:
  - `MealyMachine<S, I, T, O>`
  - `CompactMealy<I, O>`
  - `Alphabet<I>`
  - `GrowingAlphabet<I>`
  - `GrowingMapAlphabet<I>`
  - `Word<T>`

#### Custom Implementations
- **Package**: `de.learnlib.algorithms.kv`
  - `KearnsVaziraniMealy<I, O>` - Custom K-V implementation
  - `KearnsVaziraniMealyBuilder<I, O>` - Builder pattern
  - `KearnsVaziraniMealyState<I, O>` - State management
  - `Experiment` - Custom experiment runner

- **Package**: `de.learnlib.ds`
  - `MultiDTree<I, O, D>` - Discrimination tree
  - `AbstractWordBasedDTNode<I, O, D>` - Tree nodes
  - `StateInfo<I, O>` - State information
  - `CompactMealy<I, O>` - Custom Mealy implementation

## Development Setup

### Project Structure
```
Adaptive-Learning-master/
├── mealyInference/
│   ├── src/main/java/
│   │   ├── hi.java              ← Main implementation
│   │   ├── de/learnlib/
│   │   │   ├── algorithms/kv/   ← K-V algorithm
│   │   │   ├── ds/              ← Data structures
│   │   └── br/usp/icmc/...      ← Utilities
│   ├── lib/                     ← External JARs
│   ├── pom.xml                  ← Maven config
│   └── target/                  ← Compiled classes
├── alternative_experiments/
├── experiments/
└── memory-bank/                 ← This documentation
```

### Build Commands
```bash
# Clean build
cd mealyInference
mvn clean compile

# Run (from Eclipse or command line)
java -cp ... hi <args>
```

## Technical Constraints

### Java Version
- Requires Java 8+ (uses generics, lambdas)
- Type system challenges with wildcards

### Memory Constraints
- Discrimination trees grow with learning
- Static variables persist across products
- May need heap adjustment for many products

### Alphabet Requirements
- Must use `GrowingAlphabet` for adaptive learning
- Regular `Alphabet` doesn't support `addAlphabetSymbol()`
- `GrowingMapAlphabet` is concrete implementation

## Dependencies

### Critical Dependencies
```xml
<!-- LearnLib -->
<dependency>
    <groupId>de.learnlib</groupId>
    <artifactId>learnlib-...</artifactId>
</dependency>

<!-- AutomataLib -->
<dependency>
    <groupId>net.automatalib</groupId>
    <artifactId>automata-...</artifactId>
</dependency>
```

### Custom Libraries
- Located in `mealyInference/lib/`
- May include modified LearnLib/AutomataLib versions
- Check for custom implementations in `de.learnlib.ds` package

## Tool Usage Patterns

### Compilation
- Use Eclipse: "Project → Clean → Build"
- Or Maven: `mvn clean compile`
- Always clean when changing interfaces

### Debugging
- Enable verbose output in learning algorithm
- Use `Visualization.visualize(tree, true)` for tree inspection
- Check console for detailed metrics

### Testing
- Sequential product loading via loop
- Metrics comparison between products
- Visual tree comparison

## Import Patterns

### Standard Imports
```java
// LearnLib API
import de.learnlib.api.algorithm.LearningAlgorithm.MealyLearner;
import de.learnlib.api.oracle.MembershipOracle;
import de.learnlib.api.oracle.EquivalenceOracle;

// AutomataLib
import net.automatalib.words.Alphabet;
import net.automatalib.words.GrowingAlphabet;
import net.automatalib.words.impl.GrowingMapAlphabet;
import net.automatalib.words.Word;

// Custom
import de.learnlib.algorithms.kv.KearnsVaziraniMealy;
import de.learnlib.ds.MultiDTree;
import de.learnlib.ds.StateInfo;
```

### Critical for Adaptive Learning
```java
import net.automatalib.words.impl.GrowingMapAlphabet;  // Essential!
import de.learnlib.algorithms.kv.StateInfo;            // For tree typing
```

## Known Technical Issues

### Issue 1: Return Type Compatibility
**Problem**: `CompactMealy<I,O>` vs `MealyMachine<?,I,?,O>`  
**Solution**: Double cast with `@SuppressWarnings("unchecked")`

### Issue 2: Alphabet Growth
**Problem**: Regular alphabet doesn't support `addAlphabetSymbol()`  
**Solution**: Use `GrowingMapAlphabet` from start

### Issue 3: Tree-Alphabet Sync
**Problem**: Tree expects original alphabet symbols  
**Solution**: Load tree with old alphabet, then extend

## Environment Variables
- `JAVA_HOME`: Required for Maven build
- Heap size: May need `-Xmx` for large trees

