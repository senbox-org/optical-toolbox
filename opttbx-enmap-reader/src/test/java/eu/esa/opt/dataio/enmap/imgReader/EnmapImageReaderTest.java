package eu.esa.opt.dataio.enmap.imgReader;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EnmapImageReaderTest {

    @Test
    @STTM("SNAP-4123")
    public void testIsJaiGeoTiffPreferredDefaultsToFalse() throws Exception {
        Preferences preferences = createIsolatedPreferences();
        try {
            assertFalse(EnmapImageReader.isJaiGeoTiffPreferred(preferences));
        } finally {
            removePreferences(preferences);
        }
    }

    @Test
    @STTM("SNAP-4123")
    public void testIsJaiGeoTiffPreferredWhenPropertyIsSet() throws Exception {
        Preferences preferences = createIsolatedPreferences();
        try {
            preferences.putBoolean(EnmapImageReader.ENMAP_GEOTIFF_USE_JAI, true);
            assertTrue(EnmapImageReader.isJaiGeoTiffPreferred(preferences));
        } finally {
            removePreferences(preferences);
        }
    }

    private static Preferences createIsolatedPreferences() {
        return Preferences.userRoot().node("/eu/esa/opt/dataio/enmap/test/" + UUID.randomUUID());
    }

    private static void removePreferences(Preferences preferences) throws BackingStoreException {
        preferences.removeNode();
    }
}
