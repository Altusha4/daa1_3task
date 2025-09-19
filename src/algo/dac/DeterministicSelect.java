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
        Arrays.sort(copy);
        int ans = copy[k];
        m.elapsedNanos = System.nanoTime() - t0;
        return ans;
    }

    private static void swap(int[] a, int i, int j, Metrics m) {
        if (i == j) return;
        int t = a[i]; a[i] = a[j]; a[j] = t;
        if (m != null) m.swaps++;
    }
    private static void insertionSort(int[] a, int l, int r, Metrics m) {
        for (int i = l + 1; i <= r; i++) {
            int key = a[i], j = i - 1;
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
        Random rnd = new Random(42);
        int[] arr = new int[20];
        for (int i = 0; i < arr.length; i++) arr[i] = rnd.nextInt(100);
        Metrics m = new Metrics();
        int k = arr.length / 2;
        int val = select(arr, k, m);
        System.out.println("k=" + k + " value=" + val + " metrics=" + m);
    }
}
