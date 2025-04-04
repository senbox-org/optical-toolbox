package eu.esa.opt.dataio.s3;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AbstractProductFactoryTest {

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testRemoveLeadingSlash() {
        final ArrayList<String> fileNames = new ArrayList<>();
        fileNames.add("radiance.nc");
        fileNames.add("view_angle.nc");
        fileNames.add("./instrument_data.nc");

        final List<String> processedNames = AbstractProductFactory.removeLeadingSlash(fileNames);
        assertEquals(fileNames.size(), processedNames.size());
        assertEquals("radiance.nc", processedNames.get(0));
        assertEquals("view_angle.nc", processedNames.get(1));
        assertEquals("instrument_data.nc", processedNames.get(2));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testRemoveLeadingSlash_emptyInput() {
        final ArrayList<String> fileNames = new ArrayList<>();

        final List<String> processedNames = AbstractProductFactory.removeLeadingSlash(fileNames);
        assertEquals(0, processedNames.size());
    }
}
