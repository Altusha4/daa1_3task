package algo.dac;

import java.util.Arrays;
import java.util.Random;

public final class DeterministicSelect {

    public static final class Metrics {
        public long comparisons, swaps, allocations;
        public int recursionDepth, maxRecursionDepth;
        public long elapsedNanos;
        public void onEnter() { if (++recursionDepth > maxRecursionDepth) maxRecursionDepth = recursionDepth; }
        public void onExit()  { --recursionDepth; }
        @Override public String toString() {
            return "Metrics{comparisons=" + comparisons + ", swaps=" + swaps +
                    ", allocations=" + allocations + ", maxRecursionDepth=" + maxRecursionDepth +
                    ", elapsedNanos=" + elapsedNanos + '}';
        }
    }

    private DeterministicSelect() {}

    public static int select(int[] a, int k, Metrics m) {
        if (a == null || a.length == 0) throw new IllegalArgumentException("array is empty");
        if (k < 0 || k >= a.length) throw new IllegalArgumentException("k out of range");
        int[] copy = Arrays.copyOf(a, a.length);
        if (m != null) m.allocations += copy.length;
        long t0 = System.nanoTime();
        int ans = selectInPlace(copy, 0, copy.length - 1, k, m);
        if (m != null) m.elapsedNanos = System.nanoTime() - t0;
        return ans;
    }

    private static int selectInPlace(int[] a, int l, int r, int k, Metrics m) {
        while (true) {
            if (l == r) return a[l];

            int pivotIdx = medianOfMediansIndex(a, l, r, m);
            int pivotVal = a[pivotIdx];
            int p = partitionAroundValue(a, l, r, pivotVal, m);

            if (k == p) return a[p];

            if (k < p) {
                r = p - 1;
            } else {
                l = p + 1;
            }
        }
    }

    private static int medianOfMediansIndex(int[] a, int l, int r, Metrics m) {
        int n = r - l + 1;
        if (n <= 5) {
            insertionSort(a, l, r, m);
            return l + n / 2;
        }

        int write = l;
        for (int i = l; i <= r; i += 5) {
            int gL = i;
            int gR = Math.min(i + 4, r);
            insertionSort(a, gL, gR, m);
            int medIdx = gL + (gR - gL) / 2;
            swap(a, write++, medIdx, m);
        }

        int numMedians = write - l;
        int midIndex = l + (numMedians - 1) / 2;

        if (m != null) m.onEnter();
        int momValue = selectInPlace(a, l, write - 1, midIndex, m);
        if (m != null) m.onExit();

        return findValueIndex(a, l, r, momValue, m);
    }

    private static int findValueIndex(int[] a, int l, int r, int value, Metrics m) {
        for (int i = l; i <= r; i++) {
            if (m != null) m.comparisons++;
            if (a[i] == value) {
                return i;
            }
        }
        return l; // fallback
    }

    private static int partitionAroundValue(int[] a, int l, int r, int pivotValue, Metrics m) {
        int pivotIdx = findValueIndex(a, l, r, pivotValue, m);
        swap(a, pivotIdx, r, m);

        int store = l;
        for (int i = l; i < r; i++) {
            if (m != null) m.comparisons++;
            if (a[i] < pivotValue) {
                swap(a, store, i, m);
                store++;
            }
        }
        swap(a, store, r, m);
        return store;
    }

    private static void swap(int[] a, int i, int j, Metrics m) {
        if (i == j) return;
        int t = a[i]; a[i] = a[j]; a[j] = t;
        if (m != null) m.swaps++;
    }

    private static void insertionSort(int[] a, int l, int r, Metrics m) {
        for (int i = l + 1; i <= r; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= l) {
                if (m != null) m.comparisons++;
                if (a[j] <= key) break;
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    public static void main(String[] args) {
        testCorrectness();
        testPerformance();
    }

    private static void testCorrectness() {
        System.out.println("=== Testing Correctness ===");
        Random rnd = new Random(42);
        int numTests = 100;
        int arraySize = 100;

        for (int test = 0; test < numTests; test++) {
            int[] arr = new int[arraySize];
            for (int i = 0; i < arraySize; i++) arr[i] = rnd.nextInt(1000);

            int[] sorted = arr.clone();
            Arrays.sort(sorted);

            for (int k = 0; k < arraySize; k++) {
                Metrics m = new Metrics();
                int result = select(arr.clone(), k, m);
                if (result != sorted[k]) {
                    System.out.printf("Error: test=%d, k=%d, expected=%d, got=%d%n",
                            test, k, sorted[k], result);
                    return;
                }
            }
        }
        System.out.println("All correctness tests passed!");
    }

    private static void testPerformance() {
        System.out.println("\n=== Testing Performance ===");
        Random rnd = new Random(42);
        int[] sizes = {1000, 5000, 10000};

        for (int n : sizes) {
            int[] arr = new int[n];
            for (int i = 0; i < n; i++) arr[i] = rnd.nextInt();

            int k = n / 2;
            Metrics m = new Metrics();

            long startTime = System.nanoTime();
            int result = select(arr, k, m);
            long time = System.nanoTime() - startTime;

            int[] sorted = arr.clone();
            Arrays.sort(sorted);
            boolean correct = (result == sorted[k]);

            System.out.printf("n=%-6d | time=%-8.3fms | comps=%-10d | swaps=%-10d | depth=%-3d | correct=%b%n",
                    n, time/1e6, m.comparisons, m.swaps, m.maxRecursionDepth, correct);
        }
    }
}