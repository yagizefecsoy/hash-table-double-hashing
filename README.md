# Hash Table: Double Hashing vs. Linear Probing

An open-addressing hash table written from scratch in Java (no library hash maps), comparing **double hashing** with a **linear probing** baseline under different load factors and workloads.

Built as the course project for **CMPE 211 – Data Structures and Algorithms**.

## Features

- Two generic `HashTable<K, V>` implementations:
  - `LinearProbingHashTable`: baseline, probes the next slot (`index + 1`) on a collision
  - `DoubleHashingHashTable`: uses a second hash function to choose a key-dependent step size
- Standard operations: `put`, `get`, `remove`, `contains`, `size`
- Lazy deletion with `EMPTY` / `OCCUPIED` / `DELETED` slot states, so removals don't break probe chains
- Probe counting, timing and approximate memory measurement for every experiment

## How it works

| | Linear probing | Double hashing |
|---|---|---|
| Probe sequence | `h1(k), h1(k)+1, h1(k)+2, …` | `h1(k) + i · h2(k)` |
| Step size | always 1 | `h2(k) = 1 + (k mod (m − 1))`, never 0 |
| Clustering | prone to primary clustering | keys spread out across the table |

The table capacity is a prime number (`10007`), so every step size is coprime with it. This means a probe sequence visits every slot before repeating.

## Experiments

`main` runs three workloads at load factor 0.9, then a load-factor sweep:

- **Workload A, random keys:** uniformly random integers
- **Workload B, clustered keys:** `123 + i · capacity`, so every key has the same `h1`. This is the worst case for linear probing.
- **Workload C, mixed:** insert, delete half of the keys, then search

### Results (from the project report)

Total probes when inserting and then searching random keys:

| Load factor | Linear probing | Double hashing |
|---:|---:|---:|
| 0.5 | 20,144 | 17,086 |
| 0.8 | 123,231 | 55,557 |
| 0.9 | 459,926 | 112,181 |

Average probes per operation:

| Workload | Linear probing | Double hashing |
|---|---:|---:|
| A: Random | 5 | 2 |
| B: Clustered | 4,503 | ~2 |
| C: Mixed insert/delete/search | 1,127 | 7 |

Double hashing stays stable as the table fills up. Linear probing gets much slower under clustering-heavy workloads. The full analysis is in [`docs/CMPE211_ProjectReport.pdf`](docs/CMPE211_ProjectReport.pdf).

## Running

Requires JDK 8 or newer.

```bash
javac -d out src/Project.java
java -cp out HashTableProject
```

> The entry point is the `HashTableProject` class inside `Project.java`.

## Project structure

```
.
├── src/
│   └── Project.java              # Both hash tables + experiment runner
└── docs/
    └── CMPE211_ProjectReport.pdf # Report with analysis and results
```

## Authors

- Yağız Efe Çiçekçisoy
- Batuhan Türkkan

## License

[MIT](LICENSE)
