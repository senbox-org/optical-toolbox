package eu.esa.opt.mphchl;

import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class MphChlOlciOpTest {

    @Test
    public void testOperatorMetadata() {
        final OperatorMetadata operatorMetadata = MphChlOlciOp.class.getAnnotation(OperatorMetadata.class);
        assertNotNull(operatorMetadata);
        assertEquals("MphChlOlci", operatorMetadata.alias());
        assertEquals("1.0", operatorMetadata.version());
        assertEquals("Mark William Matthews, Daniel Odermatt, Tom Block, Olaf Danne", operatorMetadata.authors());
        assertEquals("(c) 2013, 2014, 2017 by Brockmann Consult", operatorMetadata.copyright());
        assertEquals("Computes maximum peak height of chlorophyll for OLCI. Implements OLCI-specific parts.",
                     operatorMetadata.description());
    }

    @Test
    public void testConfigureSourceSample() {
        final TestSourceSampleConfigurer sampleConfigurer = new TestSourceSampleConfigurer();
        MphChlOlciOp mphChlOp = new MphChlOlciOp();
        mphChlOp.configureSourceSamples(sampleConfigurer);

        final HashMap<Integer, String> sampleMap = sampleConfigurer.getSampleMap();
        assertEquals("rBRR_07", sampleMap.get(0));
        assertEquals("rBRR_08", sampleMap.get(1));
        assertEquals("rBRR_10", sampleMap.get(2));
        assertEquals("rBRR_11", sampleMap.get(3));
        assertEquals("rBRR_12", sampleMap.get(4));
        assertEquals("rBRR_18", sampleMap.get(5));
    }

}
