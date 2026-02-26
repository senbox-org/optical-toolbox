/*
 * Copyright (C) 2013 Brockmann Consult GmbH (info@brockmann-consult.de)
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
package eu.esa.opt.dataio.landsat.geotiff;

import eu.esa.opt.dataio.landsat.geotiff.c2.Landsat8C2Metadata;
import eu.esa.opt.dataio.landsat.geotiff.c2.LandsatC2ReprocessedMetadata;
import org.esa.snap.core.dataio.ProductIOException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Thomas Storm
 */
class LandsatMetadataFactory {

    static LandsatMetadata create(File mtlFile) throws IOException {
        LandsatLegacyMetadata landsatMetadata = new LandsatLegacyMetadata(Files.newBufferedReader(mtlFile.toPath()));
        if (landsatMetadata.isLegacyFormat()) {
            // legacy format case
            if (landsatMetadata.isLandsatTM() || landsatMetadata.isLandsatETM_Plus()) {
                return landsatMetadata;
            } else {
                throw new ProductIOException("Product is of a legacy landsat format, not a legacy Landsat5 or Landsat7 ETM+ product.");
            }
        } else {
            // new format case
            try (BufferedReader reader = Files.newBufferedReader(mtlFile.toPath())) {
                String line = reader.readLine();
                int collection = 1;
                while (line != null) {
                    if (line.contains("COLLECTION_NUMBER")) {
                        collection = Integer.parseInt(getValue(line));
                    } else if (line.contains("SPACECRAFT_ID")) {
                        if (line.contains("LANDSAT_8") || line.contains("LANDSAT_9")) {
                            return collection == 1 ?
                                    new Landsat8Metadata(Files.newBufferedReader(mtlFile.toPath())) :
                                    new Landsat8C2Metadata(Files.newBufferedReader(mtlFile.toPath()));
                        } else {
                            return collection == 1 ?
                                    new LandsatReprocessedMetadata(Files.newBufferedReader(mtlFile.toPath())) :
                                    new LandsatC2ReprocessedMetadata(Files.newBufferedReader(mtlFile.toPath()));
                        }
                    }
                    line = reader.readLine();
                }
            }
            throw new IllegalStateException(
                    "File '" + mtlFile + "' does not contain spacecraft information. (Field 'SPACECRAFT_ID' missing)");
        }
    }

    private static String getValue(String line) {
        return line.substring(line.indexOf('=') + 1).trim();
    }
}
