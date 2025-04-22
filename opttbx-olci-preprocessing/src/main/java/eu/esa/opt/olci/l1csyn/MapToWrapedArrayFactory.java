package eu.esa.opt.olci.l1csyn;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MapToWrapedArrayFactory {

    public static Map<int[], int[]> createWrappedArray(Map<int[], int[]> map) {
        if (map == null) {
            return null;
        }
        if (map.isEmpty()) {
            return new HashMap<>();
        }

        Iterator<int[]> keyIterator = map.keySet().iterator();
        int[] next = keyIterator.next();
        int[] keyMins = Arrays.copyOf(next, next.length);
        int[] keyMaxs = Arrays.copyOf(next, next.length);

        while (keyIterator.hasNext()) {
            int[] key = keyIterator.next();
            for (int i = 0; i < key.length; i++) {
                int v = key[i];
                keyMins[i] = Math.min(keyMins[i], v);
                keyMaxs[i] = Math.max(keyMaxs[i], v);
            }
        }

        int keyLength = keyMins.length;
        if (keyLength == 2) {
            return new WrappedArray2(map, keyMins, keyMaxs);
        }
        if (keyLength == 3) {
            return new WrappedArray3(map, keyMins, keyMaxs);
        }
        throw new IllegalStateException("Unexpected length of keys. Expected is 2 or 3 but was " + keyLength);
    }

    public static class WrappedArray2 extends AbstractWrappedArray {

        private final int[][][] array;

        public WrappedArray2(Map<int[], int[]> map, int[] keyMins, int[] keyMaxs) {
            super(keyMins, 2);
            if (map == null) {
                throw new IllegalArgumentException("Map must be not null.");
            }
            if (keyMins.length != 2 || keyMaxs.length != 2) {
                throw new IllegalArgumentException("The key min and max arrays must have a length of 2.");
            }
            int[] dimensions = new int[2];
            for (int i = 0; i < dimensions.length; i++) {
                int maxIdx = keyMaxs[i] - keyMins[i];
                maxIndex[i] = maxIdx;
                dimensions[i] = maxIdx + 1;
            }
            array = new int[dimensions[0]][dimensions[1]][];
            copyMapValues(map);
        }

        private void copyMapValues(Map<int[], int[]> map) {
            for (Map.Entry<int[], int[]> entry : map.entrySet()) {
                int[] key = entry.getKey();
                array[key[0] - offsets[0]][key[1] - offsets[1]] = entry.getValue();
            }
        }

        @Override
        public boolean containsKey(Object key) {
            if (key instanceof int[]) {
                int[] keyArr = (int[]) key;
                if (keyArr.length == 2) {
                    if (insideArrayBounds(keyArr)) {
                        return array[keyArr[0] - offsets[0]][keyArr[1] - offsets[1]] != null;
                    }
                }
            }
            return false;
        }

        @Override
        public int[] get(Object key) {
            if (key instanceof int[]) {
                int[] keyArr = (int[]) key;
                if (keyArr.length == 2) {
                    if (insideArrayBounds(keyArr)) {
                        return array[keyArr[0] - offsets[0]][keyArr[1] - offsets[1]];
                    }
                }
            }
            return null;
        }

    }

    public static class WrappedArray3 extends AbstractWrappedArray {

        private final int[][][][] array;

        public WrappedArray3(Map<int[], int[]> map, int[] keyMins, int[] keyMaxs) {
            super(keyMins, 3);
            if (map == null) {
                throw new IllegalArgumentException("Map must be not null.");
            }
            if (keyMins.length != 3 || keyMaxs.length != 3) {
                throw new IllegalArgumentException("The key min and max arrays must have a length of 2.");
            }
            int[] dimensions = new int[3];
            for (int i = 0; i < dimensions.length; i++) {
                int maxIdx = keyMaxs[i] - keyMins[i];
                maxIndex[i] = maxIdx;
                dimensions[i] = maxIdx + 1;
            }
            array = new int[dimensions[0]][dimensions[1]][dimensions[2]][];
            copyMapValues(map);
        }

        private void copyMapValues(Map<int[], int[]> map) {
            for (Map.Entry<int[], int[]> entry : map.entrySet()) {
                int[] key = entry.getKey();
                array[key[0] - offsets[0]][key[1] - offsets[1]][key[2] - offsets[2]] = entry.getValue();
            }
        }

        @Override
        public boolean containsKey(Object key) {
            if (key instanceof int[]) {
                int[] keyArr = (int[]) key;
                if (keyArr.length == 3) {
                    if (insideArrayBounds(keyArr)) {
                        return array[keyArr[0] - offsets[0]][keyArr[1] - offsets[1]][keyArr[2] - offsets[2]] != null;
                    }
                }
            }
            return false;
        }

        @Override
        public int[] get(Object key) {
            if (key instanceof int[]) {
                int[] keyArr = (int[]) key;
                if (keyArr.length == 3) {
                    if (insideArrayBounds(keyArr)) {
                        return array[keyArr[0] - offsets[0]][keyArr[1] - offsets[1]][keyArr[2] - offsets[2]];
                    }
                }
            }
            return null;
        }
    }

    public static abstract class AbstractWrappedArray implements Map<int[], int[]> {
        protected final int[] offsets;
        protected final int[] maxIndex;

        public AbstractWrappedArray(int[] keyMins, int size) {
            offsets = keyMins;
            maxIndex = new int[size];
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        protected boolean insideArrayBounds(int[] keyArr) {
            for (int i = 0; i < keyArr.length; i++) {
                int v = keyArr[i] - offsets[i];
                if (v < 0 || v > maxIndex[i]) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public int[] put(int[] key, int[] value) {
            return new int[0];
        }

        @Override
        public int[] remove(Object key) {
            return new int[0];
        }

        @Override
        public void putAll(Map<? extends int[], ? extends int[]> m) {

        }

        @Override
        public void clear() {

        }

        @Override
        public Set<int[]> keySet() {
            return null;
        }

        @Override
        public Collection<int[]> values() {
            return null;
        }

        @Override
        public Set<Entry<int[], int[]>> entrySet() {
            return null;
        }
    }
}

