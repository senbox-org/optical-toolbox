package eu.esa.opt.dataio.s3.olci;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import javax.annotation.Tainted;

import static org.junit.Assert.assertArrayEquals;

public class DataMapperTest {

    @Test
    @STTM("SNAP-3978")
    public void testMapData() {
        final short[] sensorIndices = {-1, -1, 3, 2, 4, -1};
        final float[] instrumentData = {11.1f, 12.12f, 13.13f, 14.14f, 15.15f, 16.16f};
        final float[] targetBuffer = new float[instrumentData.length];

        final DataMapper dataMapper = new DataMapper();
        dataMapper.mapData(instrumentData, targetBuffer, sensorIndices);

        final float[] expected = {-1.f, -1.f, 14.14f, 13.13f, 15.15f, -1.f};
        assertArrayEquals(expected, targetBuffer, 1e-8F);
    }
}
