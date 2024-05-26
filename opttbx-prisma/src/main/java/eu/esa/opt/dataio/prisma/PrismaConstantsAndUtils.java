/*
 *
 * Copyright (c) 2021.  Brockmann Consult GmbH (info@brockmann-consult.de)
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
 */

package eu.esa.opt.dataio.prisma;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

final class PrismaConstantsAndUtils {


    static final String FORMAT_NAME = "PRISMA";
    static final String DESCRIPTION = "Prisma ASI (Agenzia Spaziale Italiana)";
    static final String PRISMA_HDF_EXTENSION = ".he5";
    static final String PRISMA_ZIP_CONTAINER_EXTENSION = ".zip";
    static final String PRISMA_FILENAME_REGEX
            = "PRS_L2D_\\w+_\\d{14}_\\d{14}_\\d+\\.(he5|zip)";
    static final Pattern PRISMA_FILENAME_PATTERN
            = Pattern.compile(PRISMA_FILENAME_REGEX, Pattern.CASE_INSENSITIVE);
    static final String GROUP_NAME_PRS_L2D_HCO = "PRS_L2D_HCO";

    static final Class<?>[] IO_TYPES = new Class[]{
            Path.class,
            File.class,
            String.class
    };

    private static final OutputConverter[] IO_CONVERTERS = new OutputConverter[]{
            output -> (Path) output,
            output -> ((File) output).toPath(),
            output -> Paths.get((String) output)
    };
    static final String DATASET_NAME_LONGITUDE = "Longitude";

    static Path convertToPath(final Object object) {
        for (int i = 0; i < IO_TYPES.length; i++) {
            if (IO_TYPES[i].isInstance(object)) {
                return IO_CONVERTERS[i].convertOutput(object);
            }
        }
        return null;
    }

    private interface OutputConverter {

        Path convertOutput(Object output);
    }
}
