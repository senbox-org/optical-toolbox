package eu.esa.opt.dataio.s3;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.RGBImageProfile;
import org.esa.snap.core.datamodel.RGBImageProfileManager;
import org.junit.Test;

import static org.junit.Assert.*;

public class Sentinel3RgbProfilesTest {

    @Test
    @STTM("SNAP-3790")
    public void testCreateSynAodProfile() {
        final RGBImageProfile synAodProfile = Sentinel3RgbProfiles.createSynAodProfile();
        assertNotNull(synAodProfile);
        assertEquals("SYN AOD SLSTR/OLCI- False colour", synAodProfile.getName());
        assertEquals("Surface_reflectance_670", synAodProfile.getRedExpression());
        assertEquals("Surface_reflectance_550", synAodProfile.getGreenExpression());
        assertEquals("Surface_reflectance_440", synAodProfile.getBlueExpression());

        final String[] pattern = synAodProfile.getPattern();
        assertEquals(3, pattern.length);
        final String typePattern = pattern[0];
        final String filePattern = pattern[1];

        assertEquals("SY_2_AOD___", typePattern);
        assertEquals("S3._SY_2_AOD__*", filePattern);
        assertEquals("", pattern[2]);

        assertPatternMatches("SY_2_AOD___", typePattern);

        assertPatternMatches("S3A_SY_2_AOD____20170619T101637_20170619T101937_20190605T093814_0180_019_065______LR1_D_NT_001.SEN3", filePattern);
    }

    @Test
    @STTM("SNAP-3790")
    public void testCreateSynVegProfile() {
        final RGBImageProfile synVegProfile = Sentinel3RgbProfiles.createSynVegProfile();
        assertNotNull(synVegProfile);
        assertEquals("SYN VG1/V10/VGP SLSTR/OLCI - False colour", synVegProfile.getName());
        assertEquals("B3", synVegProfile.getRedExpression());
        assertEquals("B2", synVegProfile.getGreenExpression());
        assertEquals("B0", synVegProfile.getBlueExpression());

        final String[] pattern = synVegProfile.getPattern();
        assertEquals(3, pattern.length);
        final String typePattern = pattern[0];
        final String filePattern = pattern[1];

        assertEquals("SY_2_V*__", typePattern);
        assertEquals("S3._SY_2_V..__*", filePattern);
        assertEquals("", pattern[2]);

        assertPatternMatches("SY_2_VGP___", typePattern);
        assertPatternMatches("SY_2_VG1___", typePattern);
        assertPatternMatches("SY_2_V10___", typePattern);

        assertPatternMatches("S3A_SY_2_VGP____20240313T073520_20240313T081915_20240313T124212_2635_110_092______PS1_O_ST_002.SEN3", filePattern);
        assertPatternMatches("S3A_SY_2_V10____20190410T111721_20190420T111721_20190430T120711_EUROPE____________LN2_O_NT_002.SEN3", filePattern);
        assertPatternMatches("S3A_SY_2_VG1____20130621T100922_20130621T104922_20140527T011902_GLOBAL____________LN2_D_NR____.SEN3", filePattern);
    }

    @Test
    @STTM("SNAP-3790")
    public void testRegisterProfiles() {
        Sentinel3RgbProfiles.registerRGBProfiles();

        RGBImageProfileManager manager = RGBImageProfileManager.getInstance();
        final RGBImageProfile[] allProfiles = manager.getAllProfiles();

        assertProfileExists("SYN AOD SLSTR/OLCI- False colour", allProfiles);
        assertProfileExists("SYN VG1/V10/VGP SLSTR/OLCI - False colour", allProfiles);
    }

    private static void assertProfileExists(String expected, RGBImageProfile[] allProfiles) {
        for (final RGBImageProfile currentProfile : allProfiles) {
            if (currentProfile.getName().equals(expected)) {
                return;
            }
        }

        fail("The requested profile is not registered: " + expected);
    }

    private static void assertPatternMatches(String type, String typePattern) {
        assertTrue(type.matches(typePattern.replace("*", ".*").replace("?", ".")));
    }
}
