/*
 * Copyright (c) 2022.  Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 *
 */

package eu.esa.opt.dataio.s3.meris;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.meris.MerisLevel1ProductPlugIn;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Marco Peters
 */
public class MerisLevel1ProductPlugInTest {

    private MerisLevel1ProductPlugIn plugIn;

    @Before
    public void setUp()  {
        plugIn = new MerisLevel1ProductPlugIn();
    }

    @Test
    public void testDirNamePatternMatching() {
        assertTrue(plugIn.isValidMerisDirectoryName("ENV_ME_1_FRG____20091208T101751_20091208T102057_________________0186_085_008______DSI_R_NT____.SEN3"));
        assertTrue(plugIn.isValidMerisDirectoryName("ENV_ME_1_RRG____20111230T102706_20111230T111042_________________2616_110_123______DSI_R_NT____.SEN3"));
        assertTrue(plugIn.isValidMerisDirectoryName("EN1_MDSI_MER_RR__1P_20040103T012931_20040103T021310_009633_0060_20180610T222321_0100"));
        assertTrue(plugIn.isValidMerisDirectoryName("EN1_MDSI_MER_FRS_1P_20040103T034519_20040103T034859_009634_0061_20180413T122431_0100"));
    }

    @Test
    @STTM("SNAP-3666")
    public void testGetDefaultFileExtensions() {
        final String[] defaultFileExtensions = plugIn.getDefaultFileExtensions();
        assertEquals(2, defaultFileExtensions.length);
        assertEquals(".xml", defaultFileExtensions[0]);
        assertEquals(".zip", defaultFileExtensions[1]);
    }

    @Test
    @STTM("SNAP-3666")
    public void testGetProductFileFilter() {
        final SnapFileFilter productFileFilter = plugIn.getProductFileFilter();
        assertNotNull(productFileFilter);

        assertEquals("MER_L1_S3", productFileFilter.getFormatName());
        final String[] extensions = productFileFilter.getExtensions();
        assertEquals(2, extensions.length);
        assertEquals(".xml", extensions[0]);
        assertEquals(".zip", extensions[1]);
        assertEquals("MERIS Level 1 in Sentinel-3 product format (*.xml,*.zip)", productFileFilter.getDescription());
    }

    @Test
    @STTM("SNAP-3666")
    public void testIsValidInputFileName() {
        assertTrue(plugIn.isValidInputFileName("xfdumanifest.xml"));
        assertTrue(plugIn.isValidInputFileName("L1c_Manifest.xml"));
        assertTrue(plugIn.isValidInputFileName("EN1_MDSI_MER_FRS_1P_20040103T034519_20040103T034859_009634_0061_20180413T122431_0100.zip"));

        assertFalse(plugIn.isValidInputFileName("manifest.safe"));
        assertFalse(plugIn.isValidInputFileName("S5P_NRTI_L2__SO2____20240219T082248_20240219T082748_32914_03_020601_20240219T090920.nc"));
    }

    @Test
    @STTM("SNAP-3666")
    public void testIsValidInput() {
        final String sep = File.separator;

        assertTrue(plugIn.isInputValid("EN1_MDSI_MER_FRS_1P_20040103T034519_20040103T034859_009634_0061_20180413T122431_0100" + sep + "xfdumanifest.xml"));
        assertTrue(plugIn.isInputValid("EN1_MDSI_MER_FRS_1P_20040103T034519_20040103T034859_009634_0061_20180413T122431_0100.SEN3.ZIP"));

        // no alternative manifest name defined 2024-05-28 tb
        assertFalse(plugIn.isInputValid("ENV_AT_1_RBT____20120316T160758_20120316T175312_20210818T165348_6313_112_370______DSI_R_NT_004.SEN3" + sep + "L1c_Manifest.xml"));
        assertFalse(plugIn.isInputValid("S3A_SL_1_RBT____20180809T035343_20180809T035643_20180810T124116_0179_034_218_2520_MAR_O_NT_002.SEN3" + sep + "manifest.safe"));
        assertFalse(plugIn.isInputValid("S5P_NRTI_L2__SO2____20240219T082248_20240219T082748_32914_03_020601_20240219T090920.nc"));
    }

    @Test
    public void testSourcePattern()  {
        assertEquals("EN(1|V).*_ME(R|_)1?_(F|R)R(G|P|S|_)_(1P)?_.*", plugIn.getSourcePattern());
    }
}