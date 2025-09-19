# Deterministic Select Algorithm

Java implementation of the deterministic selection algorithm using Median-of-Medians approach.

## Features
- O(n) worst-case time complexity
- In-place partitioning
- Detailed metrics tracking (comparisons, swaps, recursion depth)
- Tail recursion optimization

## Usage
See main method in DeterministicSelect.java for example usage.

## Algorithm
- Groups elements by 5
- Finds median of medians as pivot
- Recurses into the smaller partition