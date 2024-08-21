package gov.nasa.gsfc.seadas.dataio;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.RGBImageProfile;
import org.junit.Test;

import static org.junit.Assert.*;

public class L1BPaceRgbProfilesTest {

    @Test
    @STTM("SNAP-3810")
    public void testCreateL1BProfileTrue() {
        final RGBImageProfile l1BProfile = L1BPaceRgbProfiles.createL1BProfileTrueColor();
        assertNotNull(l1BProfile);
        assertEquals("PACE L1B - True Color", l1BProfile.getName());
        assertEquals("rhot_red_654.602", l1BProfile.getRedExpression());
        assertEquals("rhot_blue_554.9926", l1BProfile.getGreenExpression());
        assertEquals("rhot_blue_442.28088", l1BProfile.getBlueExpression());

        final String[] pattern = l1BProfile.getPattern();
        assertEquals(3, pattern.length);
        final String typePattern = pattern[0];
        final String filePattern = pattern[1];

        assertEquals("PaceOCI_L1B", typePattern);
        assertEquals("PACE_OCI*L1B*", filePattern);
        assertEquals("", pattern[2]);

        assertPatternMatches("PaceOCI_L1B", typePattern);

        assertPatternMatches("PACE_OCI.20240514T094709.L1B.nc", filePattern);
    }

    @Test
    @STTM("SNAP-3810")
    public void testCreateL1BProfileTrueLog() {
        final RGBImageProfile l1BProfile = L1BPaceRgbProfiles.createL1BProfileLogTrueColor();
        assertNotNull(l1BProfile);
        assertEquals("PACE L1B - True Color (log)", l1BProfile.getName());
        assertEquals("log(rhot_red_654.602/0.01)/log(1/0.01)", l1BProfile.getRedExpression());
        assertEquals("log(rhot_blue_554.9926/0.01)/log(1/0.01)", l1BProfile.getGreenExpression());
        assertEquals("log(rhot_blue_442.28088/0.01)/log(1/0.01)", l1BProfile.getBlueExpression());

        final String[] pattern = l1BProfile.getPattern();
        assertEquals(3, pattern.length);
        final String typePattern = pattern[0];
        final String filePattern = pattern[1];

        assertEquals("PaceOCI_L1B", typePattern);
        assertEquals("PACE_OCI*L1B*", filePattern);
        assertEquals("", pattern[2]);

        assertPatternMatches("PaceOCI_L1B", typePattern);

        assertPatternMatches("PACE_OCI.20240514T094709.L1B.nc", filePattern);
    }

    @Test
    @STTM("SNAP-3810")
    public void testCreateL1BProfileFalse() {
        final RGBImageProfile l1BProfile = L1BPaceRgbProfiles.createL1BProfileFalseColor();
        assertNotNull(l1BProfile);
        assertEquals("PACE L1B - False Color", l1BProfile.getName());
        assertEquals("rhot_red_864.59247", l1BProfile.getRedExpression());
        assertEquals("rhot_red_654.602", l1BProfile.getGreenExpression());
        assertEquals("rhot_blue_554.9926", l1BProfile.getBlueExpression());

        final String[] pattern = l1BProfile.getPattern();
        assertEquals(3, pattern.length);
        final String typePattern = pattern[0];
        final String filePattern = pattern[1];

        assertEquals("PaceOCI_L1B", typePattern);
        assertEquals("PACE_OCI*L1B*", filePattern);
        assertEquals("", pattern[2]);

        assertPatternMatches("PaceOCI_L1B", typePattern);

        assertPatternMatches("PACE_OCI.20240514T094709.L1B.nc", filePattern);
    }

    @Test
    @STTM("SNAP-3810")
    public void testCreateL1BProfileFalseLog() {
        final RGBImageProfile l1BProfile = L1BPaceRgbProfiles.createL1BProfileLogFalseColor();
        assertNotNull(l1BProfile);
        assertEquals("PACE L1B - False Color (log)", l1BProfile.getName());
        assertEquals("log(rhot_red_864.59247/0.01)/log(1/0.01)", l1BProfile.getRedExpression());
        assertEquals("log(rhot_red_654.602/0.01)/log(1/0.01)", l1BProfile.getGreenExpression());
        assertEquals("log(rhot_blue_554.9926/0.01)/log(1/0.01)", l1BProfile.getBlueExpression());

        final String[] pattern = l1BProfile.getPattern();
        assertEquals(3, pattern.length);
        final String typePattern = pattern[0];
        final String filePattern = pattern[1];

        assertEquals("PaceOCI_L1B", typePattern);
        assertEquals("PACE_OCI*L1B*", filePattern);
        assertEquals("", pattern[2]);

        assertPatternMatches("PaceOCI_L1B", typePattern);

        assertPatternMatches("PACE_OCI.20240514T094709.L1B.nc", filePattern);
    }


    private static void assertPatternMatches(String type, String typePattern) {
        assertTrue(type.matches(typePattern.replace("*", ".*").replace("?", ".")));
    }
}