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
        long t0 = System.nanoTime();
        int ans = selectInPlace(copy, 0, copy.length - 1, k, m);
        m.elapsedNanos = System.nanoTime() - t0;
        return ans;
    }

    private static int selectInPlace(int[] a, int l, int r, int k, Metrics m) {
        while (true) {
            if (l == r) return a[l];

            int pivotVal = medianOfMedians(a, l, r, m);
            int p = partitionAroundValue(a, l, r, pivotVal, m);

            if (k == p) return a[p];

            int leftSize = p - l;
            int rightSize = r - p;

            if (k < p) {
                if (leftSize <= rightSize) { // рекурсируемся в меньшую
                    m.onEnter();
                    int res = selectInPlace(a, l, p - 1, k, m);
                    m.onExit();
                    return res;
                } else {
                    r = p - 1;               // большую сторону обрабатываем хвостом (цикл)
                }
            } else {
                if (rightSize <= leftSize) {
                    m.onEnter();
                    int res = selectInPlace(a, p + 1, r, k, m);
                    m.onExit();
                    return res;
                } else {
                    l = p + 1;
                }
            }
        }
    }


    private static int medianOfMedians(int[] a, int l, int r, Metrics m) {
        int n = r - l + 1;
        if (n <= 5) {
            insertionSort(a, l, r, m);
            return a[l + n / 2];
        }
        int write = l;
        for (int i = l; i <= r; i += 5) {
            int gL = i, gR = Math.min(i + 4, r);
            insertionSort(a, gL, gR, m);
            int med = gL + (gR - gL) / 2;
            swap(a, write++, med, m);
        }
        int midIndex = l + (write - l - 1) / 2;
        m.onEnter();
        int mom = selectInPlace(a, l, write - 1, midIndex, m);
        m.onExit();
        return mom;
    }

    private static int partitionAroundValue(int[] a, int l, int r, int pivotValue, Metrics m) {
        int pivotIdx = l;
        while (pivotIdx <= r) {
            m.comparisons++;
            if (a[pivotIdx] == pivotValue) break;
            pivotIdx++;
        }
        swap(a, pivotIdx, r, m);

        int store = l;
        for (int i = l; i < r; i++) {
            m.comparisons++;
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
        m.swaps++;
    }
    private static void insertionSort(int[] a, int l, int r, Metrics m) {
        for (int i = l + 1; i <= r; i++) {
            int key = a[i], j = i - 1;
            while (j >= l) {
                m.comparisons++;
                if (a[j] <= key) break;
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    public static void main(String[] args) {
        Random rnd = new Random(42);
        int n = 20000;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = rnd.nextInt();

        int[] s = Arrays.copyOf(arr, arr.length);
        Arrays.sort(s);
        int[] ks = {0, n/4, n/2, 3*n/4, n-1};
        for (int k : ks) {
            Metrics m = new Metrics();
            int got = select(arr, k, m);
            System.out.printf("k=%-7d ok=%s time=%.3f ms comps=%d swaps=%d depth=%d%n",
                    k, (got == s[k]), m.elapsedNanos/1e6, m.comparisons, m.swaps, m.maxRecursionDepth);
        }
    }
}
