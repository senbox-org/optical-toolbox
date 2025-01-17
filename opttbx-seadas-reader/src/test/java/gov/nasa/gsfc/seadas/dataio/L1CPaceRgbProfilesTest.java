package gov.nasa.gsfc.seadas.dataio;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.RGBImageProfile;
import org.junit.Test;

import static org.junit.Assert.*;

public class L1CPaceRgbProfilesTest {

    @Test
    @STTM("SNAP-3810")
    public void testCreateL1CProfileTrue() {
        final RGBImageProfile l1CProfile = L1CPaceRgbProfiles.createL1CProfileTrueColor();
        assertNotNull(l1CProfile);
        assertEquals("PACE L1C - True Color", l1CProfile.getName());
        assertEquals("i_20_654", l1CProfile.getRedExpression());
        assertEquals("i_20_555", l1CProfile.getGreenExpression());
        assertEquals("i_20_442", l1CProfile.getBlueExpression());

        final String[] pattern = l1CProfile.getPattern();
        assertEquals(3, pattern.length);
        final String typePattern = pattern[0];
        final String filePattern = pattern[1];

        assertEquals("Pace_L1C", typePattern);
        assertEquals("PACE_OCI*L1C*", filePattern);
        assertEquals("", pattern[2]);

        assertPatternMatches("Pace_L1C", typePattern);

        assertPatternMatches("PACE_OCI.20240514T080849.L1C.5km.nc", filePattern);
    }

    @Test
    @STTM("SNAP-3810")
    public void testCreateL1CProfileTrueLog() {
        final RGBImageProfile l1CProfile = L1CPaceRgbProfiles.createL1CProfileLogTrueColor();
        assertNotNull(l1CProfile);
        assertEquals("PACE L1C - True Color (log)", l1CProfile.getName());
        assertEquals("log(i_20_654/0.01)/log(1/0.01)", l1CProfile.getRedExpression());
        assertEquals("log(i_20_555/0.01)/log(1/0.01)", l1CProfile.getGreenExpression());
        assertEquals("log(i_20_442/0.01)/log(1/0.01)", l1CProfile.getBlueExpression());

        final String[] pattern = l1CProfile.getPattern();
        assertEquals(3, pattern.length);
        final String typePattern = pattern[0];
        final String filePattern = pattern[1];

        assertEquals("Pace_L1C", typePattern);
        assertEquals("PACE_OCI*L1C*", filePattern);
        assertEquals("", pattern[2]);

        assertPatternMatches("Pace_L1C", typePattern);

        assertPatternMatches("PACE_OCI.20240514T080849.L1C.5km.nc", filePattern);
    }

    @Test
    @STTM("SNAP-3810")
    public void testCreateL1CProfileFalse() {
        final RGBImageProfile l1CProfile = L1CPaceRgbProfiles.createL1CProfileFalseColor();
        assertNotNull(l1CProfile);
        assertEquals("PACE L1C - False Color", l1CProfile.getName());
        assertEquals("i_20_864", l1CProfile.getRedExpression());
        assertEquals("i_20_654", l1CProfile.getGreenExpression());
        assertEquals("i_20_555", l1CProfile.getBlueExpression());

        final String[] pattern = l1CProfile.getPattern();
        assertEquals(3, pattern.length);
        final String typePattern = pattern[0];
        final String filePattern = pattern[1];

        assertEquals("Pace_L1C", typePattern);
        assertEquals("PACE_OCI*L1C*", filePattern);
        assertEquals("", pattern[2]);

        assertPatternMatches("Pace_L1C", typePattern);

        assertPatternMatches("PACE_OCI.20240514T080849.L1C.5km.nc", filePattern);
    }

    @Test
    @STTM("SNAP-3810")
    public void testCreateL1CProfileFalseLog() {
        final RGBImageProfile l1CProfile = L1CPaceRgbProfiles.createL1CProfileLogFalseColor();
        assertNotNull(l1CProfile);
        assertEquals("PACE L1C - False Color (log)", l1CProfile.getName());
        assertEquals("log(i_20_864/0.01)/log(1/0.01)", l1CProfile.getRedExpression());
        assertEquals("log(i_20_654/0.01)/log(1/0.01)", l1CProfile.getGreenExpression());
        assertEquals("log(i_20_555/0.01)/log(1/0.01)", l1CProfile.getBlueExpression());

        final String[] pattern = l1CProfile.getPattern();
        assertEquals(3, pattern.length);
        final String typePattern = pattern[0];
        final String filePattern = pattern[1];

        assertEquals("Pace_L1C", typePattern);
        assertEquals("PACE_OCI*L1C*", filePattern);
        assertEquals("", pattern[2]);

        assertPatternMatches("Pace_L1C", typePattern);

        assertPatternMatches("PACE_OCI.20240514T080849.L1C.5km.nc", filePattern);
    }

    private static void assertPatternMatches(String type, String typePattern) {
        assertTrue(type.matches(typePattern.replace("*", ".*").replace("?", ".")));
    }
}