package eu.esa.opt.olci.l1csyn;

import org.junit.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class SlstrMisrTransformTest {

    @Test
    public void testIntArrayComparatorUsedByTreeMap() {
        Map<int[], Integer> integerTreeMap = new TreeMap<>(new SlstrMisrTransform.ComparatorIntArray());
        Map<String, Integer> stringTreeMap = new TreeMap<>();

        int value = 0;
        for (int x = 0; x < 1000; x++) {
            for (int y = 0; y < 1000; y++) {
                integerTreeMap.put(new int[]{x, y}, value);
                stringTreeMap.put("" + x + "_" + y, value);
                value++;
            }
        }
        for (String key : stringTreeMap.keySet()) {
            String[] split = key.split("_");
            int[] iKey = {Integer.parseInt(split[0]), Integer.parseInt(split[1])};
            assertEquals("Wrong value for key: " + key, stringTreeMap.get(key), integerTreeMap.get(iKey));
        }
    }

    @Test
    public void testGetColRow() {
        int[] colRow = SlstrMisrTransform.getColRow(0, 1, 2);
        assertEquals(1, colRow[0]);
        assertEquals(2, colRow[1]);

        colRow = SlstrMisrTransform.getColRow(1, 2, 3);
        assertEquals(2, colRow[0]);
        assertEquals(7, colRow[1]);

        colRow = SlstrMisrTransform.getColRow(2, 3, 4);
        assertEquals(3, colRow[0]);
        assertEquals(12, colRow[1]);
    }
}