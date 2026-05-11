package gov.nasa.gsfc.seadas.dataio;

import com.bc.ceres.annotation.STTM;
import eu.esa.snap.core.dataio.cache.DataBuffer;
import eu.esa.snap.core.dataio.cache.VariableDescriptor;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SeadasCacheUtilsTest {


    @Test
    @STTM("SNAP-4122")
    public void test_getDefaultVariableDescriptor_2DVariableWithoutChunkSizes() throws IOException {
        final Variable variable = mock(Variable.class);
        when(variable.getDataType()).thenReturn(DataType.FLOAT);
        when(variable.getShape()).thenReturn(new int[]{17, 23});

        final VariableDescriptor descriptor = SeadasCacheUtils.getDefaultVariableDescriptor(variable, "radiance");

        assertEquals("radiance", descriptor.name);
        assertEquals(ProductData.TYPE_FLOAT32, descriptor.dataType);
        assertEquals(23, descriptor.width);
        assertEquals(17, descriptor.height);
        assertEquals(-1, descriptor.layers);
        assertEquals(23, descriptor.tileWidth);
        assertEquals(17, descriptor.tileHeight);
        assertEquals(-1, descriptor.tileLayers);
    }

    @Test
    @STTM("SNAP-4122")
    public void test_getDefaultVariableDescriptor_3DVariableWithChunkSizes() throws IOException {
        final Variable variable = mock(Variable.class);
        final Attribute chunkSizes = mock(Attribute.class);
        when(chunkSizes.getValues()).thenReturn(Array.factory(DataType.INT, new int[]{3}, new int[]{2, 5, 7}));
        when(variable.getDataType()).thenReturn(DataType.SHORT);
        when(variable.getShape()).thenReturn(new int[]{11, 13, 19});
        when(variable.findAttribute("_ChunkSizes")).thenReturn(chunkSizes);

        final VariableDescriptor descriptor = SeadasCacheUtils.getDefaultVariableDescriptor(variable, "rhot_blue");

        assertEquals("rhot_blue", descriptor.name);
        assertEquals(ProductData.TYPE_INT16, descriptor.dataType);
        assertEquals(19, descriptor.width);
        assertEquals(13, descriptor.height);
        assertEquals(11, descriptor.layers);
        assertEquals(7, descriptor.tileWidth);
        assertEquals(5, descriptor.tileHeight);
        assertEquals(2, descriptor.tileLayers);
    }

    @Test
    @STTM("SNAP-4122")
    public void test_getDefaultVariableDescriptor_nullVariableThrowsIOException() {
        try {
            SeadasCacheUtils.getDefaultVariableDescriptor(null, "missing");
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals("Variable not known: missing", e.getMessage());
        }
    }

    @Test
    @STTM("SNAP-4122")
    public void test_getDefaultVariableDescriptor_UnsupportedRankThrowsIOException() {
        final Variable variable = mock(Variable.class);
        when(variable.getDataType()).thenReturn(DataType.FLOAT);
        when(variable.getShape()).thenReturn(new int[]{3, 5, 7, 11});

        try {
            SeadasCacheUtils.getDefaultVariableDescriptor(variable, "too_many_dims");
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals("Unsupported variable rank for caching: 4", e.getMessage());
        }
    }

    @Test
    @STTM("SNAP-4122")
    public void test_readArrayReadsRequestedSection() throws IOException {
        final Variable variable = mock(Variable.class);
        final int[] offsets = {3, 4};
        final int[] shapes = {2, 3};
        final Array expected = Array.factory(DataType.INT, shapes, new int[]{1, 2, 3, 4, 5, 6});
        try {
            when(variable.read(offsets, shapes)).thenReturn(expected);
        } catch (InvalidRangeException e) {
            fail(e.getMessage());
        }

        final Array actual = SeadasCacheUtils.readArray(variable, offsets, shapes);

        assertSame(expected, actual);
    }

    @Test
    @STTM("SNAP-4122")
    public void test_readArrayWrapsInvalidRangeException() throws InvalidRangeException, IOException {
        final Variable variable = mock(Variable.class);
        final int[] offsets = {3, 4};
        final int[] shapes = {2, 3};
        final InvalidRangeException invalidRangeException = new InvalidRangeException("bad range");
        when(variable.read(offsets, shapes)).thenThrow(invalidRangeException);

        try {
            SeadasCacheUtils.readArray(variable, offsets, shapes);
            fail("Expected IOException");
        } catch (IOException e) {
            assertSame(invalidRangeException, e.getCause());
        }
    }

    @Test
    @STTM("SNAP-4122")
    public void test_ConstructDataBufferCreatesFloat32TargetFor2DShape() throws IOException {
        final int[] offsets = {4, 6};
        final int[] shapes = {2, 3};
        final float[] values = {1.5f, 2.5f, 3.5f, 4.5f, 5.5f, 6.5f};
        final Array rawBuffer = Array.factory(DataType.FLOAT, shapes, values);

        final DataBuffer dataBuffer = SeadasCacheUtils.constructDataBuffer(rawBuffer, offsets, shapes, null, ProductData.TYPE_FLOAT32);

        assertArrayEquals(values, (float[]) dataBuffer.getData().getElems(), 1e-6f);
        assertEquals(ProductData.TYPE_FLOAT32, dataBuffer.getData().getType());
        assertArrayEquals(new int[]{-1, 4, 6}, dataBuffer.getOffsets());
        assertArrayEquals(new int[]{-1, 2, 3}, dataBuffer.getShapes());
        assertEquals(6, dataBuffer.getSizeInBytes() / ProductData.getElemSize(ProductData.TYPE_FLOAT32));
    }

    @Test
    @STTM("SNAP-4122")
    public void test_constructDataBufferReusesProvidedInt16TargetFor3DShape() throws IOException {
        final int[] offsets = {1, 4, 6};
        final int[] shapes = {1, 2, 3};
        final short[] values = {1, 2, 3, 4, 5, 6};
        final ProductData targetData = ProductData.createInstance(ProductData.TYPE_INT16, values.length);
        final Array rawBuffer = Array.factory(DataType.SHORT, shapes, values);

        final DataBuffer dataBuffer = SeadasCacheUtils.constructDataBuffer(rawBuffer, offsets, shapes, targetData, ProductData.TYPE_INT16);

        assertSame(targetData, dataBuffer.getData());
        assertArrayEquals(values, (short[]) dataBuffer.getData().getElems());
        assertArrayEquals(offsets, dataBuffer.getOffsets());
        assertArrayEquals(shapes, dataBuffer.getShapes());
    }

    @Test
    @STTM("SNAP-4122")
    public void test_constructDataBufferRejectsUnknownDataType() {
        final int[] offsets = {0, 0};
        final int[] shapes = {1, 2};
        final Array rawBuffer = Array.factory(DataType.FLOAT, shapes, new float[]{1.0f, 2.0f});
        final ProductData targetData = ProductData.createInstance(ProductData.TYPE_FLOAT32, 2);

        try {
            SeadasCacheUtils.constructDataBuffer(rawBuffer, offsets, shapes, targetData, -999);
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals("Unknown data type: -999", e.getMessage());
        }
    }

    @Test
    @STTM("SNAP-4122")
    public void test_constructDataBufferRejectsUnsupportedShapeWhenTargetMustBeCreated() {
        final int[] offsets = {0, 0, 0, 0};
        final int[] shapes = {1, 2, 3, 4};
        final Array rawBuffer = Array.factory(DataType.FLOAT, shapes, new float[24]);

        try {
            SeadasCacheUtils.constructDataBuffer(rawBuffer, offsets, shapes, null, ProductData.TYPE_FLOAT32);
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals("Illegally shaped variable", e.getMessage());
        }
    }
}