package eu.esa.opt.dataio.prisma;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PrismaConstantsAndUtilsTest_DataTypeDependentDataTransfer_Integer {

    @STTM("SNAP-3445")
    @Test
    public void datatypeDependentDataTransfer_noShift_noSubsampling() {
        //preparation
        final IntBuffer srcBuffer = IntBuffer.wrap(new int[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        });
        final ByteBuffer sliceBuffer = Mockito.mock(ByteBuffer.class);
        Mockito.when(sliceBuffer.asIntBuffer()).thenReturn(srcBuffer);
        final ProductData.Int destBuffer = new ProductData.Int(9);

        //execution
        PrismaConstantsAndUtils.datatypeDependentDataTransfer(
                sliceBuffer, 3, 3, 1, 1,
                destBuffer, 3, 3);

        //verification
        assertThat(destBuffer.getElems(), is(new int[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        }));
    }

    @STTM("SNAP-3445")
    @Test
    public void datatypeDependentDataTransfer_noShift_Subsampling() {
        //preparation
        final IntBuffer srcBuffer = IntBuffer.wrap(new int[]{
                1, 0, 2, 0, 3,
                0, 0, 0, 0, 0,
                4, 0, 5, 0, 6,
                0, 0, 0, 0, 0,
                7, 0, 8, 0, 9
        });
        ByteBuffer sliceBuffer = Mockito.mock(ByteBuffer.class);
        Mockito.when(sliceBuffer.asIntBuffer()).thenReturn(srcBuffer);
        final ProductData.Int destBuffer = new ProductData.Int(9);

        //execution
        PrismaConstantsAndUtils.datatypeDependentDataTransfer(
                sliceBuffer, 5, 5, 2, 2,
                destBuffer, 3, 3);

        //verification
        assertThat(destBuffer.getElems(), is(new int[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        }));
    }
}