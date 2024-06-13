package eu.esa.opt.dataio.prisma;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class PrismaConstantsAndUtilsTest_DataTypeDependentDataTransfer_Byte {

    @STTM("SNAP-3445")
    @Test
    public void datatypeDependentDataTransfer_noShift_noSubsampling() {
        //preparation
        final ByteBuffer srcBuffer = ByteBuffer.wrap(new byte[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        });
        final ProductData.Byte destBuffer = new ProductData.Byte(9);

        //execution
        PrismaConstantsAndUtils.datatypeDependentDataTransfer(
                srcBuffer, 3, 3, 1, 1,
                destBuffer, 3, 3);

        //verification
        assertThat(destBuffer.getElems(), is(new byte[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        }));
    }

    @STTM("SNAP-3445")
    @Test
    public void datatypeDependentDataTransfer_noShift_Subsampling() {
        //preparation
        final ByteBuffer srcBuffer = ByteBuffer.wrap(new byte[]{
                1, 0, 2, 0, 3,
                0, 0, 0, 0, 0,
                4, 0, 5, 0, 6,
                0, 0, 0, 0, 0,
                7, 0, 8, 0, 9
        });
        final ProductData.Byte destBuffer = new ProductData.Byte(9);

        //execution
        PrismaConstantsAndUtils.datatypeDependentDataTransfer(
                srcBuffer, 5, 5, 2, 2,
                destBuffer, 3, 3);

        //verification
        assertThat(destBuffer.getElems(), is(new byte[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        }));
    }

    @STTM("SNAP-3445")
    @Test
    public void datatypeDependentDataTransfer_Shift_noSubsampling() {
        //preparation
        final ByteBuffer srcBuffer = ByteBuffer.wrap(new byte[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        });
        final ProductData.Byte destBuffer = new ProductData.Byte(9);

        //execution
        PrismaConstantsAndUtils.datatypeDependentDataTransfer(
                srcBuffer, 3, 3, 1, 1,
                destBuffer, 3, 3);

        //verification
        assertThat(destBuffer.getElems(), is(new byte[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        }));
    }

    @STTM("SNAP-3445")
    @Test
    public void datatypeDependentDataTransfer_Shift_AND_Subsampling() {
        //preparation
        final ByteBuffer srcBuffer = ByteBuffer.wrap(new byte[]{
                1, 0, 2, 0, 3,
                0, 0, 0, 0, 0,
                4, 0, 5, 0, 6,
                0, 0, 0, 0, 0,
                7, 0, 8, 0, 9
        });
        final ProductData.Byte destBuffer = new ProductData.Byte(9);

        //execution
        PrismaConstantsAndUtils.datatypeDependentDataTransfer(
                srcBuffer, 5, 5, 2, 2,
                destBuffer, 3, 3);

        //verification
        assertThat(destBuffer.getElems(), is(new byte[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        }));
    }
}