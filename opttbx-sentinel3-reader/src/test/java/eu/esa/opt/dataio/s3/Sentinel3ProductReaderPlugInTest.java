package eu.esa.opt.dataio.s3;/*
 * Copyright (C) 2012 Brockmann Consult GmbH (info@brockmann-consult.de)
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
 */

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductIOPlugInManager;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;

import static org.junit.Assert.*;

public class Sentinel3ProductReaderPlugInTest {

    private Sentinel3ProductReaderPlugIn plugIn;

    @Before
    public void setup() {
        plugIn = new Sentinel3ProductReaderPlugIn();
    }

    @Test
    public void testIfPlugInIsLoaded() {
        final ProductIOPlugInManager ioPlugInManager = ProductIOPlugInManager.getInstance();
        final Iterator<ProductReaderPlugIn> readerPlugIns = ioPlugInManager.getReaderPlugIns("Sen3");
        assertTrue(readerPlugIns.hasNext());
        assertTrue(readerPlugIns.next() instanceof Sentinel3ProductReaderPlugIn);
    }

    @Test
    public void testDecodeQualification_OlciLevel1b() {
        String path;

        path = createManifestFilePath("OL", "1", "ERR", "");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));

        path = createManifestFilePath("OL", "1", "EFR", "");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));
    }

    @Test
    public void testDecodeQualification_OlciLevel2L() {
        final String path = createManifestFilePath("OL", "2", "LFR", ".SEN3");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));
    }

    @Test
    public void testDecodeQualification_OlciLevel2W() {
        final String path = createManifestFilePath("OL", "2", "WFR", ".SEN3");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));
    }

    @Test
    public void testDecodeQualification_SlstrLevel1b() {
        final String path = createManifestFilePath("SL", "1", "RBT", ".SEN3");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));
    }

    @Test
    public void testDecodeQualification_SlstrWct() {
        final String path = createManifestFilePath("SL", "2", "WCT", ".SEN3");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));
    }

    @Test
    public void testDecodeQualification_SlstrWst() {
        final String path = createManifestFilePath("SL", "2", "WST", ".SEN3");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));
    }

    @Test
    public void testDecodeQualification_SlstrLst() {
        final String path = createManifestFilePath("SL", "2", "LST", ".SEN3");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));
    }

    @Test
    public void testDecodeQualification_SynergyLevel2() {
        final String path = createManifestFilePath("SY", "2", "SYN", ".SEN3");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));
    }

    @Test
    public void testDecodeQualification_VgtP() {
        final String path = createManifestFilePath("SY", "2", "VGP", ".SEN3");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));
    }

    @Test
    public void testDecodeQualification_VgtS() {
        final String path = createManifestFilePath("SY", "3", "VG1", ".SEN3");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));
    }

    @Test
    public void testDecodeQualification_WithInvalidDataSource() {
        String invalidPath = createManifestFilePath("SL", "1", "XXX", "");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(invalidPath));
    }

    @Test
    public void testDecodeQualificationWith_WrongFile() {
        final String invalidPath = "/S3_SY_2_ERR_TTTTTTTTTTTT_instanceID_GGG_CCCC_VV/someFile.doc";
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(invalidPath));
    }

    @Test
    public void testDecodeQualification_WithoutFile() {
        final String invalidPath = "/SY_1_ERR_TTTTTTTTTTTT_instanceID_GGG_CCCC_VV";
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(invalidPath));
    }

    @Test
    public void testDecodeQualificationFromXfduManifestOnly_Nonsense() {
        final String path = Sentinel3ProductReaderPlugInTest.class.getResource("nonsense/xfdumanifest.xml").getFile();
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(path));
    }

    @Test
    @STTM("SNAP-3666")
    public void testGetInputTypes() {
        final Class[] inputTypes = plugIn.getInputTypes();
        assertEquals(2, inputTypes.length);
        assertEquals(String.class, inputTypes[0]);
        assertEquals(File.class, inputTypes[1]);
    }

    @Test
    @STTM("SNAP-3666")
    public void testGetFormatNames() {
        final String[] formatNames = plugIn.getFormatNames();
        assertEquals(1, formatNames.length);
        assertEquals("Sen3", formatNames[0]);
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
    public void testGetDescription() {
        final String description = plugIn.getDescription(null);
        assertEquals("Sentinel-3 products", description);
    }

    @Test
    @STTM("SNAP-3666")
    public void testGetProductFileFilter() {
        final SnapFileFilter productFileFilter = plugIn.getProductFileFilter();
        assertNotNull(productFileFilter);

        assertEquals("Sen3", productFileFilter.getFormatName());
        final String[] extensions = productFileFilter.getExtensions();
        assertEquals(2, extensions.length);
        assertEquals(".xml", extensions[0]);
        assertEquals(".zip", extensions[1]);
        assertEquals("Sentinel-3 products (*.xml,*.zip)", productFileFilter.getDescription());
    }

    @Test
    @STTM("SNAP-3666")
    public void testIsValidSourceName() {
        assertTrue(plugIn.isValidSourceName("S3B_OL_1_EFR____20231214T092214_20231214T092514_20231214T204604_0179_087_207_2340_PS2_O_NT_003.SEN3"));
        assertTrue(plugIn.isValidSourceName("S3A_OL_1_EFR____20240526T155849_20240526T160149_20240526T174356_0179_112_382_2700_PS1_O_NR_004.SEN3.zip"));
        assertTrue(plugIn.isValidSourceName("S3A_OL_2_LFR____20240526T204947_20240526T205247_20240526T225409_0179_112_385_1980_PS1_O_NR_002.SEN3.zip"));

        assertFalse(plugIn.isValidSourceName("S2A_OPER_PRD_MSIL1C_PDMC_20160918T063540_R022_V20160916T101022_20160916T101045.SAFE"));
        assertFalse(plugIn.isValidSourceName("SM_OPER_MIR_SCLF1C_20221224T220123_20221224T225442_724_001_1.zip"));
    }

    @Test
    public void testCreateReaderInstanceReturnsNewInstanceEachTime() {
        final ProductReader firstInstance = plugIn.createReaderInstance();
        assertNotNull(firstInstance);
        final ProductReader secondInstance = plugIn.createReaderInstance();
        assertNotSame(secondInstance, firstInstance);
    }

    private static String createManifestFilePath(String sensorId, String levelId, String productId, String suffix) {
        String validParentDirectory = String.format("/S3_%s_%s_%s_TTTTTTTTTTTT_.*%s/", sensorId,
                levelId, productId, suffix);
        String manifestFile = "xfdumanifest.xml";
        return validParentDirectory + manifestFile;
    }

}
