/*
 * Copyright (C) 2014 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package eu.esa.opt.dataio.avhrr.noaa.pod;

import com.bc.ceres.binio.IOHandler;
import com.bc.ceres.binio.util.FileChannelIOHandler;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.SnapFileFilter;

import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

/**
 * A reader plugin for NOAA Polar Orbiter Data (POD) products (currently AVHRR HRPT only).
 *
 * @author Ralf Quast
 * @see <a href="http://www.ncdc.noaa.gov/oa/pod-guide/ncdc/docs/podug/index.htm">NOAA Polar Orbiter Data User's Guide</a>
 */
public final class PodAvhrrReaderPlugIn implements ProductReaderPlugIn {

    private static final String DESCRIPTION = "NOAA Polar Orbiter Data products (AVHRR HRPT)";
    private static final String[] FILE_EXTENSIONS = new String[]{".l1b"};
    private static final String FORMAT_NAME = "NOAA_POD_AVHRR_HRPT";
    private static final String[] FORMAT_NAMES = new String[]{FORMAT_NAME};
    private static final Class[] INPUT_TYPES = new Class[]{
            //ImageInputStream.class,
            String.class, Path.class};

    public PodAvhrrReaderPlugIn() {
    }

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        final Path file = getInputFile(input);
        if (file != null) {
            try (FileChannel channel = FileChannel.open(
                    file,
                    StandardOpenOption.READ)) {
                IOHandler ioHandler = new FileChannelIOHandler(channel);
                if (PodAvhrrFile.canDecode(ioHandler)) {
                    return DecodeQualification.INTENDED;
                }
            } catch (IOException e) {
                return DecodeQualification.UNABLE;
            }
        }
//        if (input instanceof ImageInputStream) {
//            ImageInputStream iis = (ImageInputStream) input;
//            IOHandler ioHandler = new ImageIOHandler(iis);
//            if (PodAvhrrFile.canDecode(ioHandler)) {
//                return DecodeQualification.INTENDED;
//            }
//        }
        return DecodeQualification.UNABLE;
    }

    public static Path getInputFile(Object input) {
        Path file = null;
        if (input instanceof String inputString) {
            file = Path.of(inputString);
        } else if (input instanceof Path inputPath) {
            file = inputPath;
        }
        return file;
    }

    @Override
    public Class[] getInputTypes() {
        return INPUT_TYPES;
    }

    @Override
    public ProductReader createReaderInstance() {
        return new PodAvhrrReader(this);
    }

    @Override
    public SnapFileFilter getProductFileFilter() {
        return new SnapFileFilter(getFormatNames()[0], getDefaultFileExtensions(), getDescription(null));
    }

    @Override
    public String[] getFormatNames() {
        return FORMAT_NAMES;
    }

    @Override
    public String[] getDefaultFileExtensions() {
        return FILE_EXTENSIONS;
    }

    @Override
    public String getDescription(Locale locale) {
        return DESCRIPTION;
    }
}
