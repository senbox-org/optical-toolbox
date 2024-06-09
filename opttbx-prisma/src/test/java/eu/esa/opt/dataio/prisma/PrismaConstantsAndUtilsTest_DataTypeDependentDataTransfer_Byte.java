package eu.esa.opt.dataio.prisma;

import org.esa.snap.core.datamodel.ProductData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class PrismaConstantsAndUtilsTest_DataTypeDependentDataTransfer_Byte {

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
                destBuffer, 0, 0, 3, 3);

        //verification
        assertThat(destBuffer.getElems(), is(new byte[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        }));
    }

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
                destBuffer, 0, 0, 3, 3);

        //verification
        assertThat(destBuffer.getElems(), is(new byte[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        }));
    }

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
                destBuffer, 2, 2, 3, 3);

        //verification
        assertThat(destBuffer.getElems(), is(new byte[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        }));
    }

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
                destBuffer, 2, 2, 3, 3);

        //verification
        assertThat(destBuffer.getElems(), is(new byte[]{
                1, 2, 3,
                4, 5, 6,
                7, 8, 9
        }));
    }
}