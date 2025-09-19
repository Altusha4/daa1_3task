package algo.dac;

import java.util.Arrays;
import java.util.Random;

public final class DeterministicSelect {

    public static final class Metrics {
        public long comparisons;
        public long swaps;
        public long allocations;
        public int recursionDepth;
        public int maxRecursionDepth;
        public long elapsedNanos;

        public void onEnter() {
            recursionDepth++;
            if (recursionDepth > maxRecursionDepth) maxRecursionDepth = recursionDepth;
        }
        public void onExit() { recursionDepth--; }

        @Override public String toString() {
            return "Metrics{" +
                    "comparisons=" + comparisons +
                    ", swaps=" + swaps +
                    ", allocations=" + allocations +
                    ", maxRecursionDepth=" + maxRecursionDepth +
                    ", elapsedNanos=" + elapsedNanos +
                    '}';
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
