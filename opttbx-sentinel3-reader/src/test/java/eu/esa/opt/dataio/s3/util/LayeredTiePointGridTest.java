package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LayeredTiePointGridTest {

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testSetGetVariableName() {
        final LayeredTiePointGrid tiePointGrid = new LayeredTiePointGrid("was_n_los_11", 4091, 77, 0, 0, 1, 64);
        assertTrue(StringUtils.isNullOrEmpty(tiePointGrid.getVariableName()));

        tiePointGrid.setVariableName("Heffalump");
        assertEquals("Heffalump", tiePointGrid.getVariableName());
    }
}
