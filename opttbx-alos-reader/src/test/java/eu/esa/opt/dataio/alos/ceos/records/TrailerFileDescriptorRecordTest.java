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
import eu.esa.opt.dataio.alos.ceos.CeosTestHelper;
import eu.esa.opt.dataio.alos.ceos.IllegalCeosFormatException;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TrailerFileDescriptorRecordTest {

    private String _prefix;
    private CeosFileReader _reader;

    @Before
    public void setUp() throws Exception {
        final ByteArrayOutputStream os = new ByteArrayOutputStream(24);
        MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(os);
        _prefix = "TrailerFileDescriptorRecordTest_prefix";
        ios.writeBytes(_prefix);
        writeRecordData(ios);
        ios.writeBytes("TrailerFileDescriptorRecordTest_suffix"); // as suffix
        _reader = new CeosFileReader(ios);
    }

    @Test
    public void testInit_SimpleConstructor() throws IOException,
            IllegalCeosFormatException {
        _reader.seek(_prefix.length());

        final TrailerFileDescriptorRecord record = new TrailerFileDescriptorRecord(_reader);

        assertRecord(record);
    }

    @Test
    public void testInit() throws IOException,
            IllegalCeosFormatException {
        final TrailerFileDescriptorRecord record = new TrailerFileDescriptorRecord(_reader, _prefix.length());

        assertRecord(record);
    }

    private void writeRecordData(final ImageOutputStream ios) throws IOException {
        CommonFileDescriptorRecordTest.writeRecordData(ios);

        ios.writeBytes("     1"); // number of trailer records // I6
        ios.writeBytes("  8460"); // trailer record length // I6
        CeosTestHelper.writeBlanks(ios, 24);
        CeosTestHelper.writeBlanks(ios, 8244);
    }

    private void assertRecord(final TrailerFileDescriptorRecord record) {
        CommonFileDescriptorRecordTest.assertRecord(record);

        assertEquals(1, record.getNumTrailerRecords());
        assertEquals(8460, record.getTrailerRecordLength());
    }
}
