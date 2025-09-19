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
        m.onEnter();
        int ans = selectInPlaceQuick(copy, 0, copy.length - 1, k, m); // временный quickselect
        m.onExit();
        m.elapsedNanos = System.nanoTime() - t0;
        return ans;
    }

    private static int selectInPlaceQuick(int[] a, int l, int r, int k, Metrics m) {
        if (l == r) return a[l];
        int pivotValue = a[(l + r) >>> 1];
        int p = partitionAroundValue(a, l, r, pivotValue, m);
        if (k == p) return a[p];
        if (k < p) {
            m.onEnter();
            int res = selectInPlaceQuick(a, l, p - 1, k, m);
            m.onExit();
            return res;
        } else {
            m.onEnter();
            int res = selectInPlaceQuick(a, p + 1, r, k, m);
            m.onExit();
            return res;
        }
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
        int n = 40;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = rnd.nextInt(1000);

        int[] s = Arrays.copyOf(arr, arr.length);
        Arrays.sort(s);
        int k = n / 2;

        Metrics m = new Metrics();
        int got = select(arr, k, m);
        System.out.println("ok=" + (got == s[k]) + " value=" + got + " depth=" + m.maxRecursionDepth);
    }
}
