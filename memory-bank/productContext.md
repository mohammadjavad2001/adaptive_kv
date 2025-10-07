# Product Context

## Problem Statement
Learning Mealy machine models for Software Product Lines is expensive:
- Each product requires complete learning from scratch
- Discrimination trees are rebuilt for every product
- Many redundant membership queries across similar products
- High computational cost when learning product families

## Solution Approach
**Incremental Adaptive Learning**: Reuse knowledge from previously learned products

### How It Works
1. **Product 1 (Baseline)**
   - Learn completely using Kearns-Vazirani
   - Build discrimination tree from scratch
   - Save tree + alphabet for reuse

2. **Product 2+ (Adaptive)**
   - Load tree + alphabet from previous product
   - Add any new symbols to alphabet
   - Continue learning with existing tree structure
   - Save updated tree + alphabet for next product

### Expected Benefits
- **Reduced Queries**: Fewer membership/equivalence queries needed
- **Faster Learning**: Reuse of tree structure accelerates convergence
- **Incremental Knowledge**: Each product builds on accumulated knowledge
- **Scalability**: Benefits increase with more similar products

## User Experience
Users provide multiple product FSM files and observe:
- Detailed learning statistics per product
- Clear visualization of query reduction
- Adaptive learning benefit metrics
- Comparison between baseline and adaptive approaches

