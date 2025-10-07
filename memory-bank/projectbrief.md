# Project Brief: Incremental Adaptive Model Learning for SPL

## Overview
Implementation of **Incremental Adaptive Model Learning** for Software Product Lines (SPL) using the **Kearns-Vazirani algorithm** to accelerate learning of similar products by reusing discrimination trees.

## Core Objective
Reduce learning time and query count when learning multiple related products by:
1. Learning Product 1 completely and saving its discrimination tree
2. Reusing and extending the tree for each subsequent product
3. Building incremental knowledge: P1→tree1→P2→tree2→P3→tree3...

## Key Requirements
- **Algorithm**: Kearns-Vazirani (not L*)
- **Learning Type**: Incremental (each product builds on previous, not just first)
- **Alphabet Handling**: Dynamic symbol addition via `addAlphabetSymbol()`
- **Metrics**: Track all learning costs (Rounds, MQ/EQ queries, symbols)

## Success Criteria
- Successfully learn multiple SPL products sequentially
- Demonstrate query reduction in later products
- Measure improvement across all metrics:
  - Number of Rounds (EQ queries)
  - Membership Query counts (Resets + Symbols)
  - Equivalence Query costs (Resets + Symbols)
  - Learning time

## Technical Constraints
- Must use custom Kearns-Vazirani implementation
- Products may have different alphabets (symbol sets)
- Tree and alphabet must be saved and restored between products
- Support for 2+ products in sequence

