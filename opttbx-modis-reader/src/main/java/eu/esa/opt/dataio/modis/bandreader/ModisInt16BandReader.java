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

package eu.esa.opt.dataio.modis.bandreader;

import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Variable;

import java.io.IOException;

public class ModisInt16BandReader extends ModisBandReader {

    private short[] line;
    private short min;
    private short max;
    private short fill;
    private short[] targetData;
    private int targetIdx;

    public ModisInt16BandReader(Variable variable, final int layer, final boolean is3d) {
        super(variable, layer, is3d);
    }

    /**
     * Retrieves the data type of the band
     *
     * @return always {@link ProductData#TYPE_INT8}
     */
    @Override
    public int getDataType() {
        return ProductData.TYPE_INT16;
    }

    @Override
    protected void prepareForReading(final int sourceOffsetX, final int sourceOffsetY, final int sourceWidth,
                                     final int sourceHeight, final int sourceStepX, final int sourceStepY,
                                     final ProductData destBuffer) {
        fill = (short) Math.round(fillValue);
        if (validRange == null) {
            min = Short.MIN_VALUE;
            max = Short.MAX_VALUE;
        } else {
            min = (short) Math.round(validRange.getMin());
            max = (short) Math.round(validRange.getMax());
        }
        targetData = (short[]) destBuffer.getElems();
        targetIdx = 0;
        ensureLineWidth(sourceWidth);
    }

    @Override
    protected void readLine() throws IOException {
        try {
            final Section section = new Section(start, count, stride);
            final Array array = variable.read(section);
            for (int i = 0; i < line.length; i++) {
                line[i] = array.getShort(i);
            }
        } catch (InvalidRangeException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    protected void validate(final int x) {
        final short value = line[x];
        if (value < min || value > max) {
            line[x] = fill;
        }
    }

    @Override
    protected void assign(final int x) {
        targetData[targetIdx++] = line[x];
    }

    private void ensureLineWidth(final int sourceWidth) {
        if ((line == null) || (line.length != sourceWidth)) {
            line = new short[sourceWidth];
        }
    }
}
