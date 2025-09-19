package algo.dac;

import java.util.Arrays;
import java.util.Random;

/**
 * Deterministic Select — Median-of-Medians (MoM5) с гарантией O(n).
 * - Группировка по 5, медиана медиан как опорный элемент.
 * - In-place partition (без доп. массивов).
 * - Рекурсия только в нужную сторону + предпочтение меньшей стороны.
 * - Хвостовая рекурсия устранена (while-цикл).
 *
 * Метрики: comparisons, swaps, allocations(≈0), maxRecursionDepth, elapsedNanos.
 */
public final class DeterministicSelect {

    public static final class Metrics {
        public long comparisons;
        public long swaps;
        public long allocations;
        public int recursionDepth;
        public int maxRecursionDepth;
        public long elapsedNanos;

        void onEnter() {
            recursionDepth++;
            if (recursionDepth > maxRecursionDepth) maxRecursionDepth = recursionDepth;
        }
        void onExit() { recursionDepth--; }

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

    /** Публичное API: k-ый минимум (0-based). Входной массив НЕ портим (работаем с копией). */
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
                // ищем слева
                if (leftSize <= rightSize) { // рекурсируемся в меньшую сторону
                    m.onEnter();
                    int res = selectInPlace(a, l, p - 1, k, m);
                    m.onExit();
                    return res;
                } else {
                    r = p - 1; // хвост: продолжаем циклом
                }
            } else {
                // ищем справа
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

    /** Median-of-Medians (группы по 5): возвращает ЗНАЧЕНИЕ опорного элемента. */
    private static int medianOfMedians(int[] a, int l, int r, Metrics m) {
        int n = r - l + 1;
        if (n <= 5) {
            insertionSort(a, l, r, m);
            return a[l + n / 2];
        }
        int write = l;
        for (int i = l; i <= r; i += 5) {
            int gL = i;
            int gR = Math.min(i + 4, r);
            insertionSort(a, gL, gR, m);
            int medianIndex = gL + (gR - gL) / 2;
            swap(a, write, medianIndex, m); // перенос медианы группы в «фронт»
            write++;
        }
        int midIndex = l + (write - l - 1) / 2;
        m.onEnter();
        int mom = selectInPlace(a, l, write - 1, midIndex, m);
        m.onExit();
        return mom;
    }

    /** Разбиение вокруг значения pivotValue (Ломуто), возвращает финальный индекс pivot. */
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

    /** Вставками на отрезке [l..r] (малоэлем. группы по 5). */
    private static void insertionSort(int[] a, int l, int r, Metrics m) {
        for (int i = l + 1; i <= r; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= l) {
                m.comparisons++;
                if (a[j] <= key) break;
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    private static void swap(int[] a, int i, int j, Metrics m) {
        if (i == j) return;
        int t = a[i]; a[i] = a[j]; a[j] = t;
        m.swaps++;
    }

    /* ---------- Мини-демо ---------- */
    public static void main(String[] args) {
        Random rnd = new Random(42);
        int n = 20_000;
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = rnd.nextInt();

        int[] sorted = Arrays.copyOf(arr, arr.length);
        Arrays.sort(sorted);

        int[] ks = {0, n/4, n/2, 3*n/4, n-1};
        for (int k : ks) {
            Metrics m = new Metrics();
            int got = select(arr, k, m);
            System.out.printf("k=%-7d ok=%s  time=%.3f ms  comps=%d  swaps=%d  depth=%d%n",
                    k, (got == sorted[k]), m.elapsedNanos/1e6, m.comparisons, m.swaps, m.maxRecursionDepth);
        }
    }
}
