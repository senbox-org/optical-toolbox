package eu.esa.opt.dataio.s3.olci;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DataMapperTest {

    @Test
    @STTM("SNAP-3978")
    public void testMapSingleLine() {
        final short[] detectorIndices = {-1, -1, 3, 2, 4, -1};
        final float[] instrumentData = {11.1f, 12.12f, 13.13f, 14.14f, 15.15f, 16.16f};
        final float[] targetBuffer = new float[instrumentData.length];

        final DataMapper dataMapper = new DataMapper();
        dataMapper.mapData(instrumentData, targetBuffer, detectorIndices);

        final float[] expected = {-1.f, -1.f, 14.14f, 13.13f, 15.15f, -1.f};
        assertArrayEquals(expected, targetBuffer, 1e-8F);
    }

    @Test
    @STTM("SNAP-3978")
    public void testMapDataArrays() {
        final short[] detectorIndices = {-1, 0, 1, 1, 3, 4, -1,
                -1, 0, 1, 2, 3, 4, -1,
                -1, 0, 1, 2, 2, 4, -1};

        final float[] targetBuffer = new float[21];
        final float[] instrumentData = {100.f, 101.f, 102.f, 103.f, 104.f, 105.f, 106.f};

        final DataMapper dataMapper = new DataMapper();
        dataMapper.mapData(instrumentData, targetBuffer, detectorIndices);

        assertEquals(-1.f, targetBuffer[0], 1e-8f);
        assertEquals(100.f, targetBuffer[1], 1e-8f);
        assertEquals(101.f, targetBuffer[2], 1e-8f);

        assertEquals(101.f, targetBuffer[3], 1e-8f);
        assertEquals(103.f, targetBuffer[4], 1e-8f);
        assertEquals(104.f, targetBuffer[5], 1e-8f);
    }
}
