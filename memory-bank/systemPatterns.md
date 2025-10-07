# System Patterns

## Architecture Overview
```
┌─────────────┐
│   hi.java   │ ← Main orchestration
│  (Product   │
│   Loader)   │
└──────┬──────┘
       │
       ├─── Static State ────┐
       │  • tree_round2      │
       │  • product1Alphabet │
       │  • allInputAlphabets│
       └─────────────────────┘
       │
       ▼
┌──────────────────────────┐
│  KearnsVaziraniMealy     │ ← Learning algorithm
│  • getDiscriminationTree()│
│  • addAlphabetSymbol()   │
│  • getHypothesisModel()  │
└──────────────────────────┘
       │
       ├─── Uses ────┐
       │             │
       ▼             ▼
┌─────────────┐  ┌──────────────┐
│ MultiDTree  │  │GrowingMap    │
│             │  │Alphabet      │
└─────────────┘  └──────────────┘
       │
       ▼
┌──────────────────────────┐
│   Experiment.java        │ ← Experiment runner
│   • run()                │
│   • getDiscrtree()       │
└──────────────────────────┘
```

## Key Design Patterns

### 1. Incremental State Pattern
```java
// Static state persists across product learning iterations
static MultiDTree tree_round2 = null;
static GrowingAlphabet product1Alphabet = null;

// Each product updates state for next
tree_round2 = experiment.getDiscrtree();
product1Alphabet = learner.get_alphabet_symbol();
```

### 2. Alphabet Extension Pattern
```java
// Step 1: Create learner with existing alphabet
learner = builder.withAlphabet(product1Alphabet).create(tree_round2);

// Step 2: Safely add new symbols
for (String symbol : newProductAlphabet) {
    if (!product1Alphabet.containsSymbol(symbol)) {
        learner.addAlphabetSymbol(symbol);  // Extends internal structures
    }
}
```

### 3. Conditional Initialization Pattern
```java
if (i == 0) {
    // First product: from scratch
    product1Alphabet = new GrowingMapAlphabet<>(productAlphabet);
    learner = builder.withAlphabet(product1Alphabet).create(null);
} else {
    // Subsequent: reuse and extend
    learner = builder.withAlphabet(product1Alphabet).create(tree_round2);
    // Add new symbols...
}
```

## Component Relationships

### Core Components
1. **hi.java**: Orchestration layer
   - Loads products sequentially
   - Manages static state (tree, alphabet)
   - Coordinates learning flow
   - Collects and displays metrics

2. **KearnsVaziraniMealy**: Learning engine
   - Implements Kearns-Vazirani algorithm
   - Manages discrimination tree
   - Supports alphabet extension
   - Returns learned hypothesis

3. **MultiDTree**: Knowledge structure
   - Discrimination tree implementation
   - Stores learning decisions
   - Reused across products
   - Grows incrementally

4. **GrowingMapAlphabet**: Symbol management
   - Dynamic alphabet that supports growth
   - Thread-safe symbol addition
   - Used for incremental learning

5. **Experiment**: Learning coordinator
   - Runs learning rounds
   - Manages oracles (MQ, EQ)
   - Collects statistics
   - Returns final tree

## Critical Implementation Paths

### Path 1: First Product Learning
```
Load Product 1 FSM
    → Create GrowingMapAlphabet
    → Initialize Learner (null tree)
    → Run Experiment
    → Save tree_round2
    → Save product1Alphabet
```

### Path 2: Adaptive Learning (Product 2+)
```
Load Product N FSM
    → Load previous tree_round2
    → Load previous product1Alphabet
    → Create Learner (with tree)
    → Add new symbols dynamically
    → Run Experiment  (reuses tree!)
    → Update tree_round2
    → Update product1Alphabet
```

## Data Flow
```
Product FSM File
    ↓
Load Alphabet (symbols)
    ↓
[If i==0]  Create GrowingMapAlphabet
[If i>0]   Reuse + Extend Alphabet
    ↓
Create Learner
    ↓
Run Learning (with/without tree)
    ↓
Collect Metrics
    ↓
Save State for Next Product
```

## Type System Handling

### Wildcard Type Challenge
```java
// Interface expects: MealyMachine<?, I, ?, O>
// Implementation has: CompactMealy<I, O>
//   which is: MealyMachine<Integer, I, CompactMealyTransition<O>, O>

// Solution: Double cast
@SuppressWarnings("unchecked")
public MealyMachine<?, I, ?, O> getHypothesisModel() {
    return (MealyMachine<?, I, ?, O>) (MealyMachine<?, ?, ?, ?>) hypothesis;
}
```

### Alphabet Type Management
```java
// Declaration must support growth
private static GrowingAlphabet<String> product1Alphabet = null;

// Initialization
product1Alphabet = new GrowingMapAlphabet<>(productAlphabet);

// Extension
learner.addAlphabetSymbol(symbol);  // Requires GrowingAlphabet
```

