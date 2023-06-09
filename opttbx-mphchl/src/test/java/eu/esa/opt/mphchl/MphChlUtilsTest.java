package eu.esa.opt.mphchl;

import org.esa.snap.core.gpf.Operator;
import org.junit.Test;

import static org.junit.Assert.*;

public class MphChlUtilsTest {

    @Test
    public void testSetToInvalid() {
        final TestSample[] samples = createSampleArray(6);

        MphChlUtils.setToInvalid(samples, false);

        assertEquals(Double.NaN, samples[0].getDouble(), 1e-8);
        assertEquals(0.0, samples[1].getDouble(), 1e-8);
        assertEquals(0.0, samples[2].getDouble(), 1e-8);
        assertEquals(0.0, samples[3].getDouble(), 1e-8);
        assertEquals(0.0, samples[4].getDouble(), 1e-8);
        assertEquals(0.0, samples[4].getDouble(), 1e-8);
    }

    @Test
    public void testSetToInvalid_withMph() {
        final TestSample[] samples = createSampleArray(7);

        MphChlUtils.setToInvalid(samples, true);

        assertEquals(Double.NaN, samples[0].getDouble(), 1e-8);
        assertEquals(0.0, samples[1].getDouble(), 1e-8);
        assertEquals(0.0, samples[2].getDouble(), 1e-8);
        assertEquals(0.0, samples[3].getDouble(), 1e-8);
        assertEquals(0.0, samples[4].getDouble(), 1e-8);
        assertEquals(Double.NaN, samples[5].getDouble(), 1e-8);
    }

    @Test
    public void testComputeMph() {
        double mph = MphChlUtils.computeMph(1, 2, 3, 6, 4, 5);
        assertEquals(-3, mph, 1e-8);

        mph = MphChlUtils.computeMph(0, 2, 3, 6, 4, 5);
        assertEquals(-4, mph, 1e-8);

        mph = MphChlUtils.computeMph(1, 0, 3, 6, 4, 5);
        assertEquals(-5, mph, 1e-8);

        mph = MphChlUtils.computeMph(1, 2, 0, 6, 4, 5);
        assertEquals(3, mph, 1e-8);

        mph = MphChlUtils.computeMph(1, 2, 3, 6, 0, 5);
        assertEquals(-2.2, mph, 1e-8);

        mph = MphChlUtils.computeMph(1, 2, 3, 6, 4, 0);
        assertEquals(-0.5, mph, 1e-8);

        mph = MphChlUtils.computeMph(1, 2, 3, 0, 4, 5);
        assertEquals(3.0, mph, 1e-8);
    }

    @Test
    public void testIsCyano_threeArgs() {
        assertFalse(MphChlUtils.isCyano(0.0, 1.0, 1.0));
        assertFalse(MphChlUtils.isCyano(0.5, 1.0, 1.0));
        assertTrue(MphChlUtils.isCyano(-0.1, 1.0, 1.0));

        assertFalse(MphChlUtils.isCyano(-1.0, 0.0, 1.0));
        assertFalse(MphChlUtils.isCyano(-1.0, -0.1, 1.0));
        assertTrue(MphChlUtils.isCyano(-1.0, 0.5, 1.0));

        assertFalse(MphChlUtils.isCyano(-1.0, 1.0, 0.0));
        assertFalse(MphChlUtils.isCyano(-1.0, 1.0, 0.0019));
        assertTrue(MphChlUtils.isCyano(-1.0, 1.0, 0.0021));
    }

    @Test
    public void testIsCyano_twoArgs() {
        assertFalse(MphChlUtils.isCyano(1.0, -1.0));
        assertFalse(MphChlUtils.isCyano(-0.1, -1.0));
        assertFalse(MphChlUtils.isCyano(1.0, 0.1));
        assertTrue(MphChlUtils.isCyano(-0.1, 0.1));
    }

    @Test
    public void testComputeChlPolynomial() {
        assertEquals(353732.6926, MphChlUtils.computeChlPolynomial(0.1), 1e-8);
        assertEquals(8.2646992, MphChlUtils.computeChlPolynomial(0.001), 1e-8);
        assertEquals(1.9726, MphChlUtils.computeChlPolynomial(0.0), 1e-8);
    }

    @Test
    public void testComputeChlExponential() {
        assertEquals(22.5204566512951240, MphChlUtils.computeChlExponential(0.0001), 1e-8);
        assertEquals(23.25767257114881, MphChlUtils.computeChlExponential(0.001), 1e-8);
        assertEquals(22.44, MphChlUtils.computeChlExponential(0.0), 1e-8);
    }

    @Test
    public void testEncodeFlags() {
        assertEquals(0, MphChlUtils.encodeFlags(false, false, false));
        assertEquals(1, MphChlUtils.encodeFlags(true, false, false));
        assertEquals(2, MphChlUtils.encodeFlags(false, true, false));
        assertEquals(4, MphChlUtils.encodeFlags(false, false, true));
        assertEquals(3, MphChlUtils.encodeFlags(true, true, false));
        assertEquals(5, MphChlUtils.encodeFlags(true, false, true));
    }

    @Test
    public void testSpi() {
        final MphChlOlciOp.Spi spi = new MphChlOlciOp.Spi();
        final Class<? extends Operator> operatorClass = spi.getOperatorClass();
        assertTrue(operatorClass.isAssignableFrom(MphChlOlciOp.class));
    }

    private static TestSample[] createSampleArray(int numSamples) {
        final TestSample[] samples = new TestSample[numSamples];
        for (int i = 0; i < numSamples; i++) {
            samples[i] = new TestSample();
        }
        return samples;
    }
}
