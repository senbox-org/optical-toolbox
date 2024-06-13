package eu.esa.opt.dataio.prisma;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.TypedGetter;
import eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.TypedSetter;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PrismaConstantsAndUtilsTest {

    @STTM("SNAP-3445")
    @Test
    public void convertToPath() {
        Path path;
        Object o;

        o = "Test";
        path = PrismaConstantsAndUtils.convertToPath(o);
        assertThat(path, is(Paths.get("test")));

        o = new File("test");
        path = PrismaConstantsAndUtils.convertToPath(o);
        assertThat(path, is(Paths.get("test")));

        o = Paths.get("test");
        path = PrismaConstantsAndUtils.convertToPath(o);
        assertThat(path, is(Paths.get("test")));
    }

    @STTM("SNAP-3445")
    @Test
    public void test_GetDataSubSampled_JustSubsampling_TypeIndependent() {
        //preparation
        final int[] src = {
                2, 0, 3, 0, 4, 0, 5,
                0, 0, 0, 0, 0, 0, 0,
                6, 0, 7, 0, 8, 0, 9,
                0, 0, 0, 0, 0, 0, 0,
                10, 0, 11, 0, 12, 0, 13};
        final int srcWidth = 7;
        final int srcStepX = 2;
        final int srcStepY = 2;

        final int[] target = new int[12];
        final int destWidth = 4;
        final int destHeight = 3;

        final TypedGetter<Integer> typedGetter = index -> src[index];
        final TypedSetter<Integer> typedSetter = (index, value) -> target[index] = value;

        //execution
        PrismaConstantsAndUtils.getDataSubSampled(
                typedGetter, srcWidth, srcStepX, srcStepY,
                typedSetter, destWidth, destHeight);

        //verification
        assertThat(target, is(new int[]{
                2, 3, 4, 5,
                6, 7, 8, 9,
                10, 11, 12, 13}));
    }

    @STTM("SNAP-3445")
    @Test
    public void test_GetDataSubSampledAndShifted_SubSampled_TypeIndependent() {
        //preparation
        final int[] src = {
                2, 0, 3, 0, 4, 0, 5,
                0, 0, 0, 0, 0, 0, 0,
                6, 0, 7, 0, 8, 0, 9,
                0, 0, 0, 0, 0, 0, 0,
                10, 0, 11, 0, 12, 0, 13};
        final int srcWidth = 7;
        final int srcStepX = 2;
        final int srcStepY = 2;

        final int[] target = new int[12];
        final int destWidth = 4;
        final int destHeight = 3;

        final TypedGetter<Integer> typedGetter = index -> src[index];
        final TypedSetter<Integer> typedSetter = (index, value) -> target[index] = value;

        //execution
        PrismaConstantsAndUtils.getDataSubSampled(
                typedGetter, srcWidth, srcStepX, srcStepY,
                typedSetter, destWidth, destHeight);

        //verification
        assertThat(target, is(new int[]{
                2, 3, 4, 5,
                6, 7, 8, 9,
                10, 11, 12, 13,
        }));
    }

    @STTM("SNAP-3445")
    @Test
    public void test_GetDataSubSampled_TypeIndependent() {
        //preparation
        final int[] src = {
                2, 3, 4, 5,
                6, 7, 8, 9,
                10, 11, 12, 13};
        final int srcWidth = 4;
        final int srcStepX = 1;
        final int srcStepY = 1;

        final int[] target = new int[12];
        final int destWidth = 4;
        final int destHeight = 3;

        final TypedGetter<Integer> typedGetter = index -> src[index];
        final TypedSetter<Integer> typedSetter = (index, value) -> target[index] = value;

        //execution
        PrismaConstantsAndUtils.getDataSubSampled(
                typedGetter, srcWidth, srcStepX, srcStepY,
                typedSetter, destWidth, destHeight);

        //verification
        assertThat(target, is(new int[]{
                2, 3, 4, 5,
                6, 7, 8, 9,
                10, 11, 12, 13,
        }));
    }

    @STTM("SNAP-3445")
    @Test
    public void isSubSampling() {
        assertThat(PrismaConstantsAndUtils.isSubSampling(1, 1), is(false));

        assertThat(PrismaConstantsAndUtils.isSubSampling(2, 1), is(true));
        assertThat(PrismaConstantsAndUtils.isSubSampling(1, 2), is(true));
        assertThat(PrismaConstantsAndUtils.isSubSampling(2, 2), is(true));
    }
}