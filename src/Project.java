import java.util.Random;

/* =========================================================
   CELL STATE
   ========================================================= */
enum State {
    EMPTY,
    OCCUPIED,
    DELETED
}

/* =========================================================
   ENTRY (Key–Value Pair)
   ========================================================= */
class Entry<K, V> {
    K key;
    V value;

    Entry(K key, V value) {
        this.key = key;
        this.value = value;
    }
}

/* =========================================================
   BASELINE: LINEAR PROBING HASH TABLE
   ========================================================= */
class LinearProbingHashTable<K, V> {

    private Entry<K, V>[] table;
    private State[] states;
    private int capacity;
    private int size;

    public long probeCount = 0;

    @SuppressWarnings("unchecked")
    LinearProbingHashTable(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        table = (Entry<K, V>[]) new Entry[capacity];
        states = new State[capacity];

        for (int i = 0; i < capacity; i++)
            states[i] = State.EMPTY;
    }

    private int hash(K key) {
        return (key.hashCode() & 0x7fffffff) % capacity;
    }

    void put(K key, V value) {
        int index = hash(key);

        for (int i = 0; i < capacity; i++) {
            probeCount++;

            if (states[index] == State.EMPTY || states[index] == State.DELETED) {
                table[index] = new Entry<>(key, value);
                states[index] = State.OCCUPIED;
                size++;
                return;
            }

            if (states[index] == State.OCCUPIED &&
                    table[index].key.equals(key)) {
                table[index].value = value;
                return;
            }

            index = (index + 1) % capacity;
        }
    }

    V get(K key) {
        int index = hash(key);

        for (int i = 0; i < capacity; i++) {
            probeCount++;

            if (states[index] == State.EMPTY)
                return null;

            if (states[index] == State.OCCUPIED &&
                    table[index].key.equals(key))
                return table[index].value;

            index = (index + 1) % capacity;
        }
        return null;
    }

    void remove(K key) {
        int index = hash(key);

        for (int i = 0; i < capacity; i++) {
            probeCount++;

            if (states[index] == State.EMPTY)
                return;

            if (states[index] == State.OCCUPIED &&
                    table[index].key.equals(key)) {
                states[index] = State.DELETED;
                table[index] = null;
                size--;
                return;
            }

            index = (index + 1) % capacity;
        }
    }

    boolean contains(K key) {
        return get(key) != null;
    }

    int size() {
        return size;
    }
}

/* =========================================================
   ADVANCED TECHNIQUE: DOUBLE HASHING
   ========================================================= */
class DoubleHashingHashTable<K, V> {

    private Entry<K, V>[] table;
    private State[] states;
    private int capacity;
    private int size;

    public long probeCount = 0;

    @SuppressWarnings("unchecked")
    DoubleHashingHashTable(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        table = (Entry<K, V>[]) new Entry[capacity];
        states = new State[capacity];

        for (int i = 0; i < capacity; i++)
            states[i] = State.EMPTY;
    }

    private int h1(K key) {
        return (key.hashCode() & 0x7fffffff) % capacity;
    }

    private int h2(K key) {
        return 1 + ((key.hashCode() & 0x7fffffff) % (capacity - 1));
    }

    void put(K key, V value) {
        int base = h1(key);
        int step = h2(key);

        for (int i = 0; i < capacity; i++) {
            int index = (base + i * step) % capacity;
            probeCount++;

            if (states[index] == State.EMPTY || states[index] == State.DELETED) {
                table[index] = new Entry<>(key, value);
                states[index] = State.OCCUPIED;
                size++;
                return;
            }

            if (states[index] == State.OCCUPIED &&
                    table[index].key.equals(key)) {
                table[index].value = value;
                return;
            }
        }
    }

    V get(K key) {
        int base = h1(key);
        int step = h2(key);

        for (int i = 0; i < capacity; i++) {
            int index = (base + i * step) % capacity;
            probeCount++;

            if (states[index] == State.EMPTY)
                return null;

            if (states[index] == State.OCCUPIED &&
                    table[index].key.equals(key))
                return table[index].value;
        }
        return null;
    }

    void remove(K key) {
        int base = h1(key);
        int step = h2(key);

        for (int i = 0; i < capacity; i++) {
            int index = (base + i * step) % capacity;
            probeCount++;

            if (states[index] == State.EMPTY)
                return;

            if (states[index] == State.OCCUPIED &&
                    table[index].key.equals(key)) {
                states[index] = State.DELETED;
                table[index] = null;
                size--;
                return;
            }
        }
    }

    boolean contains(K key) {
        return get(key) != null;
    }

    int size() {
        return size;
    }
}

/* =========================================================
   EXPERIMENTS
   ========================================================= */
class HashTableProject {

    public static void main(String[] args) {

        // ====

        int capacity = 10007; // prime
        double alpha2 = 0.9;
        int operations = (int)(capacity * alpha2);
        Runtime runtime = Runtime.getRuntime();


        // **************************

        System.out.println("\n=== WORKLOAD A: Random Keys ===");

        LinearProbingHashTable<Integer, Integer> lpA =
                new LinearProbingHashTable<>(capacity);
        DoubleHashingHashTable<Integer, Integer> dhA =
                new DoubleHashingHashTable<>(capacity);

        Random rand = new Random(42);
        int[] randomKeys = new int[operations];
        for (int i = 0; i < operations; i++)
            randomKeys[i] = rand.nextInt();

// MEMORY BEFORE
        runtime.gc();
        long memBefore = runtime.totalMemory() - runtime.freeMemory();

// TIME + PROBES
        long start = System.nanoTime();
        for (int k : randomKeys) {
            lpA.put(k, k);
            dhA.put(k, k);
        }
        for (int k : randomKeys) {
            lpA.get(k);
            dhA.get(k);
        }
        long end = System.nanoTime();

// MEMORY AFTER
        runtime.gc();
        long memAfter = runtime.totalMemory() - runtime.freeMemory();

        System.out.println("Linear avg probes: " +
                (int) lpA.probeCount / (2 * operations));
        System.out.println("Double avg probes: " +
                (int) dhA.probeCount / (2 * operations));
        System.out.println("Avg time per op (ns): " +
                (int)((end - start) / (2.0 * operations)));
        System.out.println("Approx memory used (bytes): " +
                (memAfter - memBefore));

        // **************************


        System.out.println("\n=== WORKLOAD B: Clustered Keys ===");

        LinearProbingHashTable<Integer, Integer> lpB =
                new LinearProbingHashTable<>(capacity);
        DoubleHashingHashTable<Integer, Integer> dhB =
                new DoubleHashingHashTable<>(capacity);

        int baseKey = 123;

        runtime.gc();
        memBefore = runtime.totalMemory() - runtime.freeMemory();

        start = System.nanoTime();
        for (int i = 0; i < operations; i++) {
            int key = baseKey + i * capacity;
            lpB.put(key, i);
            dhB.put(key, i);
        }
        for (int i = 0; i < operations; i++) {
            int key = baseKey + i * capacity;
            lpB.get(key);
            dhB.get(key);
        }
        end = System.nanoTime();

        runtime.gc();
        memAfter = runtime.totalMemory() - runtime.freeMemory();

        System.out.println("Linear avg probes: " +
                (int) lpB.probeCount / (2 * operations));
        System.out.println("Double avg probes: " +
                (double) dhB.probeCount / (2 * operations));
        System.out.println("Avg time per op (ns): " +
                (int) ((end - start) / (2.0 * operations)));
        System.out.println("Approx memory used (bytes): " +
                (memAfter - memBefore));

// **************************

        System.out.println("\n=== WORKLOAD C: Mixed Insert/Delete/Search ===");

        LinearProbingHashTable<Integer, Integer> lpC =
                new LinearProbingHashTable<>(capacity);
        DoubleHashingHashTable<Integer, Integer> dhC =
                new DoubleHashingHashTable<>(capacity);

        runtime.gc();
        memBefore = runtime.totalMemory() - runtime.freeMemory();

        start = System.nanoTime();

// INSERT
        for (int i = 0; i < operations; i++) {
            lpC.put(i, i);
            dhC.put(i, i);
        }

// DELETE (half)
        for (int i = 0; i < operations; i += 2) {
            lpC.remove(i);
            dhC.remove(i);
        }

// SEARCH
        for (int i = 0; i < operations; i++) {
            lpC.get(i);
            dhC.get(i);
        }

        end = System.nanoTime();

        runtime.gc();
        memAfter = runtime.totalMemory() - runtime.freeMemory();

        System.out.println("Linear avg probes: " +
                (int) lpC.probeCount / (2 * operations));
        System.out.println("Double avg probes: " +
                (int) dhC.probeCount / (2 * operations));
        System.out.println("Avg time per op (ns): " +
                (int) ((end - start) / (2.0 * operations)));
        System.out.println("Approx memory used (bytes): " +
                (memAfter - memBefore));


        // ====

        
        System.out.println();
        System.out.println();
        double[] loadFactors = {0.5, 0.8, 0.9};

        System.out.println("LoadFactor | LinearProbing | DoubleHashing");
        System.out.println("--------------------------------------------");

        for (double alpha : loadFactors) {

            int inserts = (int) (capacity * alpha);

            LinearProbingHashTable<Integer, Integer> linear =
                    new LinearProbingHashTable<>(capacity);

            DoubleHashingHashTable<Integer, Integer> doubleHash =
                    new DoubleHashingHashTable<>(capacity);

            Random rand2 = new Random(42);

            // INSERT
            for (int i = 0; i < inserts; i++) {
                int key = rand2.nextInt();
                linear.put(key, i);
                doubleHash.put(key, i);
            }

            // SEARCH
            for (int i = 0; i < inserts; i++) {
                int key = rand2.nextInt();
                linear.get(key);
                doubleHash.get(key);
            }

            System.out.printf("%-10.1f | %-14d | %-14d\n",
                    alpha,
                    linear.probeCount,
                    doubleHash.probeCount);
        }






    }

}
