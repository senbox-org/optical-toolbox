package eu.esa.opt.dataio.atsr;

import org.esa.snap.core.datamodel.FlagCoding;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AtsrGSSTFileTest {

    @Test
    public void testAddConfidenceFlagsToCoding() {
        final FlagCoding flagCoding = new FlagCoding("testMe");

        AtsrGSSTFile.addConfidenceFlagsToCoding(flagCoding);

        final String[] flagNames = flagCoding.getFlagNames();
        assertEquals(11, flagNames.length);

        assertEquals(1, flagCoding.getFlagMask(flagNames[0]));
        assertEquals(128, flagCoding.getFlagMask(flagNames[7]));
        assertEquals(1024, flagCoding.getFlagMask(flagNames[10]));
    }
}
