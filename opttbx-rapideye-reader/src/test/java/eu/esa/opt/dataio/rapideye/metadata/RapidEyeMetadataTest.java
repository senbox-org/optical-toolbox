/*
 * Copyright (C) 2014-2015 CS-SI (foss-contact@thor.si.c-s.fr)
 * Copyright (C) 2014-2015 CS-Romania (office@c-s.ro)
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

package eu.esa.opt.dataio.rapideye.metadata;

import org.esa.snap.engine_utilities.utils.TestUtil;
import org.apache.commons.lang3.SystemUtils;
import org.esa.snap.core.metadata.GenericXmlMetadata;
import org.esa.snap.core.metadata.XmlMetadataParser;
import org.esa.snap.core.metadata.XmlMetadataParserFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * @author Ramona Manda
 */
public class RapidEyeMetadataTest {
    private RapidEyeMetadata metadata;
    private final String productsFolder = "_rapideye" + File.separator;

    @Before
    public void setUp() {
        assumeTrue(TestUtil.testdataAvailable());

        XmlMetadataParserFactory.registerParser(RapidEyeMetadata.class, new XmlMetadataParser<RapidEyeMetadata>(RapidEyeMetadata.class));
        metadata = GenericXmlMetadata.create(RapidEyeMetadata.class, TestUtil.getTestFile(productsFolder + "2009-04-16T104920_RE4_1B-NAC_3436599_84303_metadata.xml"));
    }

    @After
    public void tearDown(){
        metadata = null;
        System.gc();
    }

    @Test
    public void testGetBrowseFileName() {
        assertEquals("2009-04-16T104920_RE4_1B-NAC_3436599_84303_browse.tif", metadata.getBrowseFileName());
    }

    @Test
    public void testGetMaskFileName() {
        assertEquals("2009-04-16T104920_RE4_1B-NAC_3436599_84303_udm.tif", metadata.getMaskFileName());
    }

    @Test
    public void testGetRasterFileNames() {
        assertEquals(5, metadata.getRasterFileNames().length);
        assertEquals("2009-04-16T104920_RE4_1B-NAC_3436599_84303_band1.ntf", metadata.getRasterFileNames()[0]);
        assertEquals("2009-04-16T104920_RE4_1B-NAC_3436599_84303_band2.ntf", metadata.getRasterFileNames()[1]);
        assertEquals("2009-04-16T104920_RE4_1B-NAC_3436599_84303_band3.ntf", metadata.getRasterFileNames()[2]);
        assertEquals("2009-04-16T104920_RE4_1B-NAC_3436599_84303_band4.ntf", metadata.getRasterFileNames()[3]);
        assertEquals("2009-04-16T104920_RE4_1B-NAC_3436599_84303_band5.ntf", metadata.getRasterFileNames()[4]);
    }

    @Test
    public void testGetReferenceSystem() {
        assertNotNull(metadata.getReferenceSystem());
        assertEquals("4326", metadata.getReferenceSystem().epsgCode);
        assertEquals("N/A", metadata.getReferenceSystem().geodeticDatum);
        assertEquals("N/A", metadata.getReferenceSystem().projectionCode);
        assertEquals("N/A", metadata.getReferenceSystem().projectionZone);
    }

    @Test
    public void testGetProductStartTime() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");//the SimplaDateFormat knows only miliseconds, not microseconds!
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date expectedDate = sdf.parse("2009-04-16 10:49:15.290");
        assertEquals(expectedDate.getTime(), metadata.getProductStartTime().getAsDate().getTime());
        assertEquals(290423, metadata.getProductStartTime().getMicroSecondsFraction());//the SimplaDateFormat knows only miliseconds, not microseconds!
    }

    @Test
    public void testGetProductEndTime() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");//the SimplaDateFormat knows only miliseconds, not microseconds!
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date expectedDate = sdf.parse("2009-04-16 10:49:22.244");
        assertEquals(expectedDate.getTime(), metadata.getProductEndTime().getAsDate().getTime());
        assertEquals(244296, metadata.getProductEndTime().getMicroSecondsFraction());//the SimplaDateFormat knows only miliseconds, not microseconds!
    }

    @Test
    public void testGetScaleFactor() {
        assertEquals(0.01, metadata.getScaleFactor(0), 0.001);
        assertEquals(0.01, metadata.getScaleFactor(1), 0.001);
        assertEquals(0.01, metadata.getScaleFactor(2), 0.001);
        assertEquals(0.01, metadata.getScaleFactor(3), 0.001);
    }

    @Test
    public void testGetPixelFormat() {
        assertEquals(21, metadata.getPixelFormat());
    }

    @Test
    public void testGetCornersLatitudes() {
        assertEquals(53.088257, metadata.getCornersLatitudes()[0], 0.001);
        assertEquals(52.924603, metadata.getCornersLatitudes()[1], 0.001);
        assertEquals(52.670235, metadata.getCornersLatitudes()[2], 0.001);
        assertEquals(52.50815, metadata.getCornersLatitudes()[3], 0.001);
    }

    @Test
    public void testGetCornersLongitudes() {
        assertEquals(15.082264, metadata.getCornersLongitudes()[0], 0.001);
        assertEquals(16.193775, metadata.getCornersLongitudes()[1], 0.001);
        assertEquals(14.893127, metadata.getCornersLongitudes()[2], 0.001);
        assertEquals(15.994719, metadata.getCornersLongitudes()[3], 0.001);
    }

    @Test
    public void testGetRationalPolinomialCoefficients() {
        assertNull(metadata.getRationalPolinomialCoefficients());
    }

    @Test
    public void testGetRootElement() {
        assertNotNull(metadata.getRootElement());
    }

    @Test
    public void testGetFileName() {
        assertEquals("L1B", metadata.getFileName());
    }

    @Test
    public void testSetFileName() {
        metadata.setFileName("testname");
        assertEquals("testname", metadata.getFileName());
    }

    @Test
    public void testGetNumBands() {
        assertEquals(5, metadata.getNumBands());
    }

    @Test
    public void testGetProductName() {
        assertEquals("2009-04-16T104920_RE4_1B-NAC_3436599_84303", metadata.getProductName());
    }

    @Test
    public void testGetFormatName() {
        assertEquals("NITF", metadata.getFormatName());
    }

    @Test
    public void testGetMetadataProfile() {
        assertEquals("L1B", metadata.getMetadataProfile());
    }

    @Test
    public void testGetRasterWidth() {
        assertEquals(11829, metadata.getRasterWidth());
    }

    @Test
    public void testGetRasterHeight() {
        assertEquals(7422, metadata.getRasterHeight());
    }

    @Test
    public void testGetPath() {
        String root = System.getProperty(TestUtil.PROPERTYNAME_DATA_DIR);
        String partialPath = root + File.separator + productsFolder + "2009-04-16T104920_RE4_1B-NAC_3436599_84303_metadata.xml";
        String metadataPath = metadata.getPath().toString();
        if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            partialPath = partialPath.replaceAll("\\\\", "/");
        } else if (SystemUtils.IS_OS_WINDOWS) {
            partialPath = partialPath.replace("\\", "/");
            metadataPath = metadataPath.replace("\\", "/");
        }

        assertEquals(partialPath, metadataPath);
    }

    @Test
    public void testSetName() {
        metadata.setName("testname");
        assertEquals("testname", metadata.getFileName());
    }
}
