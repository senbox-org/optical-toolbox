package eu.esa.opt.dataio.prisma;

import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PrismaConstantsAndUtilsTest_DataTypeDependentDataTransfer_Double {

    @Test
    public void datatypeDependentDataTransfer_noSubsampling() {
        //preparation
        final DoubleBuffer srcBuffer = DoubleBuffer.wrap(new double[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        });
        final ByteBuffer sliceBuffer = Mockito.mock(ByteBuffer.class);
        Mockito.when(sliceBuffer.asDoubleBuffer()).thenReturn(srcBuffer);
        final ProductData.Double destBuffer = new ProductData.Double(9);

        //execution
        PrismaConstantsAndUtils.datatypeDependentDataTransfer(
                sliceBuffer, 3, 3, 1, 1,
                destBuffer, 0, 0, 3, 3);

        //verification
        assertThat(destBuffer.getElems(), is(new double[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        }));
    }

    @Test
    public void datatypeDependentDataTransfer_Subsampling() {
        //preparation
        final DoubleBuffer srcBuffer = DoubleBuffer.wrap(new double[]{
                1, 0, 2, 0, 3,
                0, 0, 0, 0, 0,
                4, 0, 5, 0, 6,
                0, 0, 0, 0, 0,
                7, 0, 8, 0, 9
        });
        ByteBuffer sliceBuffer = Mockito.mock(ByteBuffer.class);
        Mockito.when(sliceBuffer.asDoubleBuffer()).thenReturn(srcBuffer);
        final ProductData.Double destBuffer = new ProductData.Double(9);

        //execution
        PrismaConstantsAndUtils.datatypeDependentDataTransfer(
                sliceBuffer, 5, 5, 2, 2,
                destBuffer, 0, 0, 3, 3);

        //verification
        assertThat(destBuffer.getElems(), is(new double[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        }));
    }
}