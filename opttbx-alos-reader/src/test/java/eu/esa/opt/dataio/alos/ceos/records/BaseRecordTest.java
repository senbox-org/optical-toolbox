/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
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
package eu.esa.opt.dataio.alos.ceos.records;

import eu.esa.opt.dataio.alos.ceos.CeosFileReader;
import eu.esa.opt.dataio.alos.ceos.IllegalCeosFormatException;
import eu.esa.opt.dataio.alos.ceos.records.BaseRecord;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

@SuppressWarnings("OctalInteger")
public class BaseRecordTest {

    static final int RECORD_LENGTH = 4680;
    private ImageOutputStream _ios;
    private String _prefix;
    private CeosFileReader _reader;

    @Before
    public void setUp() throws Exception {
        final ByteArrayOutputStream os = new ByteArrayOutputStream(24);
        _ios = new MemoryCacheImageOutputStream(os);
        _prefix = "BaseRecordTest_prefix";
        _ios.writeBytes(_prefix);
        writeRecordData(_ios);
        _ios.writeBytes("BaseRecordTest_suffix"); // as suffix
        _reader = new CeosFileReader(_ios);
    }

    @Test
    public void testInitBaseRecord() throws IOException,
            IllegalCeosFormatException {
        final BaseRecord record = new BaseRecord(_reader, _prefix.length());

        assertRecord(record);
        assertSame(_reader, record.getReader());
        assertEquals(_prefix.length(), record.getStartPos());
        assertEquals(_prefix.length() + 12, _ios.getStreamPosition());
    }

    @Test
    public void testAssignMetadataTo() throws IOException,
            IllegalCeosFormatException {
        final BaseRecord record = new BaseRecord(_reader, _prefix.length());
        final MetadataElement elem = new MetadataElement("elem");

        record.assignMetadataTo(elem, null);

        assertMetadata(elem);
        assertEquals(0, elem.getNumElements());
        assertEquals(6, elem.getNumAttributes());
    }

    static void assertMetadata(final MetadataElement elem) {
        assertIntAttribute(elem, "Record number", 1);
        assertIntAttribute(elem, "First record subtype", 077);
        assertIntAttribute(elem, "Record type code", 0300);
        assertIntAttribute(elem, "Second record subtype", 022);
        assertIntAttribute(elem, "Third record subtype", 021);
        assertIntAttribute(elem, "Record length", RECORD_LENGTH);
    }

    static void assertIntAttribute(MetadataElement elem, String attributeName, int expectedValue) {
        final MetadataAttribute attribute = elem.getAttribute(attributeName);
        assertNotNull(attribute);
        assertEquals(ProductData.TYPE_INT32, attribute.getDataType());
        assertEquals(1, attribute.getNumDataElems());
        assertEquals(expectedValue, attribute.getData().getElemInt());
    }

    static void assertStringAttribute(MetadataElement elem, String attibuteName, String expectedValue) {
        final MetadataAttribute attribute = elem.getAttribute(attibuteName);
        assertNotNull(attribute);
        assertEquals(ProductData.TYPESTRING_ASCII, attribute.getData().getTypeString());
        assertEquals(expectedValue, attribute.getData().getElemString());
    }

    public static void assertRecord(final BaseRecord record) {
        assertNotNull(record);
        assertEquals(1, record.getRecordNumber());
        assertEquals(077, record.getFirstRecordSubtype());
        assertEquals(0300, record.getRecordTypeCode());
        assertEquals(022, record.getSecondRecordSubtype());
        assertEquals(021, record.getThirdRecordSubtype());
        assertEquals(RECORD_LENGTH, record.getRecordLength());
    }

    public static void writeRecordData(final ImageOutputStream ios) throws IOException {
        ios.writeInt(1); // recordNumber = 1
        ios.write(077); // firstRecordSubtype = 77 octal
        ios.write(0300); // recordTypeCode = 300 octal
        ios.write(022); // secondRecordSubtype = 22 octal
        ios.write(021); // thirdRecordSubtype = 22 octal (21 octal only for test)
        ios.writeInt(RECORD_LENGTH); // recordLength = variable
    }

    @Test
    public void testCreateMetadataElement() {
        MetadataElement elem;
        String suffix;

        suffix = "suffix";
        elem = BaseRecord.createMetadataElement("name", suffix);
        assertNotNull(elem);
        assertEquals("name suffix", elem.getName());

        suffix = "   ";
        elem = BaseRecord.createMetadataElement("name", suffix);
        assertNotNull(elem);
        assertEquals("name", elem.getName());

        suffix = "";
        elem = BaseRecord.createMetadataElement("name", suffix);
        assertNotNull(elem);
        assertEquals("name", elem.getName());

        suffix = null;
        elem = BaseRecord.createMetadataElement("name", suffix);
        assertNotNull(elem);
        assertEquals("name", elem.getName());
    }
}
