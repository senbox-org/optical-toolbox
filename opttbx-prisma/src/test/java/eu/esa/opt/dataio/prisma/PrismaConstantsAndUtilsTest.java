package eu.esa.opt.dataio.prisma;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PrismaConstantsAndUtilsTest {

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
        final int srcHeight = 5;
        final int srcStepX = 2;
        final int srcStepY = 2;

        final int[] target = new int[12];
        final int destOffsetX = 0;
        final int destOffsetY = 0;
        final int destWidth = 4;
        final int destHeight = 3;

        //execution
        PrismaConstantsAndUtils.getDataSubSampled(
                src, srcWidth, srcHeight, srcStepX,
                srcStepY, target, destWidth, destHeight);

        //verification
        assertThat(target, is(new int[]{
                2, 3, 4, 5,
                6, 7, 8, 9,
                10, 11, 12, 13}));
    }

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
        final int srcHeight = 5;
        final int srcStepX = 2;
        final int srcStepY = 2;

        final int[] target = new int[12];
//        final int[] target = new int[70];
        final int destOffsetX = 3;
        final int destOffsetY = 2;
        final int destWidth = 4;
        final int destHeight = 3;
//        final int destWidth = 10;
//        final int destHeight = 7;

        //execution
        PrismaConstantsAndUtils.getDataSubSampled(
                src, srcWidth, srcHeight, srcStepX,
                srcStepY, target, destWidth, destHeight);

        //verification
        assertThat(target, is(new int[]{
                2, 3, 4, 5,
                6, 7, 8, 9,
                10, 11, 12, 13,
        }));
//        assertThat(target, is(new int[]{
//                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                0, 0, 0, 2, 3, 4, 5, 0, 0, 0,
//                0, 0, 0, 6, 7, 8, 9, 0, 0, 0,
//                0, 0, 0, 10, 11, 12, 13, 0, 0, 0,
//                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
    }

    @Test
    public void test_GetDataSubSampled_TypeIndependent() {
        //preparation
        final int[] src = {
                2, 3, 4, 5,
                6, 7, 8, 9,
                10, 11, 12, 13};
        final int srcWidth = 4;
        final int srcHeight = 3;
        final int srcStepX = 1;
        final int srcStepY = 1;

        final int[] target = new int[12];
//        final int[] target = new int[70];
        final int destOffsetX = 3;
        final int destOffsetY = 2;
        final int destWidth = 4;
        final int destHeight = 3;
//        final int destWidth = 10;
//        final int destHeight = 7;

        //execution
        PrismaConstantsAndUtils.getDataSubSampled(
                src, srcWidth, srcHeight, srcStepX,
                srcStepY, target, destWidth, destHeight);

        //verification
        assertThat(target, is(new int[]{
                2, 3, 4, 5,
                6, 7, 8, 9,
                10, 11, 12, 13,
        }));
//        assertThat(target, is(new int[]{
//                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                0, 0, 0, 2, 3, 4, 5, 0, 0, 0,
//                0, 0, 0, 6, 7, 8, 9, 0, 0, 0,
//                0, 0, 0, 10, 11, 12, 13, 0, 0, 0,
//                0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//                0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
    }

    @Test
    public void isSubSampling() {
        assertThat(PrismaConstantsAndUtils.isSubSampling(1, 1), is(false));

        assertThat(PrismaConstantsAndUtils.isSubSampling(2, 1), is(true));
        assertThat(PrismaConstantsAndUtils.isSubSampling(1, 2), is(true));
        assertThat(PrismaConstantsAndUtils.isSubSampling(2, 2), is(true));
    }
}