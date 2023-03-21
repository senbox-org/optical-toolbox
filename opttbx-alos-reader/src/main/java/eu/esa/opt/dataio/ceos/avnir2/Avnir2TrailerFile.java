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

package eu.esa.opt.dataio.ceos.avnir2;

import eu.esa.opt.dataio.ceos.records.TrailerFileDescriptorRecord;
import eu.esa.opt.dataio.ceos.CeosFileReader;
import eu.esa.opt.dataio.ceos.IllegalCeosFormatException;
import eu.esa.opt.dataio.ceos.avnir2.records.Avnir2TrailerRecord;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

/**
 * * This class represents a trailer file of an Avnir-2 product.
 *
 * @author Marco Peters
 * @version $Revision$ $Date$
 */
class Avnir2TrailerFile {

    private Avnir2TrailerRecord _trailerRecord;
    private CeosFileReader _ceosReader;

    public Avnir2TrailerFile(final ImageInputStream trailerStream) throws IOException,
                                                                          IllegalCeosFormatException {
        _ceosReader = new CeosFileReader(trailerStream);
        // must be created even it is not (yet) used
        // it is needed for positioning the reader correctly
        new TrailerFileDescriptorRecord(_ceosReader);
        _trailerRecord = new Avnir2TrailerRecord(_ceosReader);
    }

    public int[] getHistogramBinsForBand(final int index) throws IOException,
                                                                 IllegalCeosFormatException {
        return _trailerRecord.getHistogramFor(index);
    }

    public void close() throws IOException {
        _ceosReader.close();
        _ceosReader = null;
        _trailerRecord = null;
    }
}
