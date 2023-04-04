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
package eu.esa.opt.dataio.alos.ceos.prism.records;

import eu.esa.opt.dataio.alos.ceos.CeosFileReader;
import eu.esa.opt.dataio.alos.ceos.IllegalCeosFormatException;
import eu.esa.opt.dataio.alos.ceos.records.BaseLeaderFileDescriptorRecord;

import java.io.IOException;

// todo - this class can be deleted
// todo - use BaseLeaderFileDescriptorRecord instead

public class LeaderFileDescriptorRecord extends BaseLeaderFileDescriptorRecord {


    public LeaderFileDescriptorRecord(final CeosFileReader reader) throws IOException, IllegalCeosFormatException {
        this(reader, -1);
    }

    public LeaderFileDescriptorRecord(final CeosFileReader reader, final long startPos) throws IOException,
                                                                                               IllegalCeosFormatException {
        super(reader, startPos);

//        reader.skipBytes(16 + 16 + 16 + 16 + 4256); // skip blanks
    }
}
