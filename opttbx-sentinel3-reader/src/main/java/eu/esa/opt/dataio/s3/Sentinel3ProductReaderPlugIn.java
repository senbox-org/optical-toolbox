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

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.FileUtils;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.joda.time.field.FieldUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.regex.Pattern;

public class Sentinel3ProductReaderPlugIn implements ProductReaderPlugIn {

    private static final Class[] SUPPORTED_INPUT_TYPES = {String.class, File.class};
    private static final String FORMAT_NAME = "Sen3";

    private final String formatName;
    private final String manifestFileBasename;
    private final String alternativeManifestFileBasename;
    private final String[] fileExtensions;
    private final Pattern sourceNamePattern;
    private final String description;
    private final String[] formatNames;

    static {
        Sentinel3RgbProfiles.registerRGBProfiles();
    }

    public Sentinel3ProductReaderPlugIn() {
        this(FORMAT_NAME, "Sentinel-3 products",
                "S3.?_(OL_1_E[FR]R|OL_2_(L[FR]R|W[FR]R)|ER1_AT_1_RBT|ER2_AT_1_RBT|ENV_AT_1_RBT|SL_1_RBT|" +
                        "SL_2_(LST|WCT|WST|FRP)|SY_1_SYN|SY_2_(AOD|VGP|SYN|V10)|SY_[23]_VG1)_.*(.SEN3)?",
                "xfdumanifest", "L1c_Manifest", ".xml", ".zip");
    }

    protected Sentinel3ProductReaderPlugIn(String formatName,
                                           String description,
                                           String sourceNamePattern,
                                           String manifestFileBasename,
                                           String alternativeManifestFileBasename,
                                           String... fileExtensions) {
        this.formatName = formatName;
        this.fileExtensions = fileExtensions;
        this.sourceNamePattern = Pattern.compile(sourceNamePattern);
        this.description = description;
        formatNames = new String[]{formatName};
        this.manifestFileBasename = manifestFileBasename;
        this.alternativeManifestFileBasename = alternativeManifestFileBasename;
    }

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        if (isInputValid(input)) {
            return DecodeQualification.INTENDED;
        } else {
            return DecodeQualification.UNABLE;
        }
    }

    @Override
    public Class[] getInputTypes() {
        return SUPPORTED_INPUT_TYPES;
    }

    @Override
    public ProductReader createReaderInstance() {
        return new Sentinel3ProductReader(this);
    }

    @Override
    public String[] getFormatNames() {
        return formatNames;
    }

    @Override
    public String[] getDefaultFileExtensions() {
        return fileExtensions;
    }

    @Override
    public String getDescription(Locale locale) {
        return description;
    }

    @Override
    public SnapFileFilter getProductFileFilter() {
        return new SnapFileFilter(formatName, fileExtensions, description);
    }

    // public access for testing only 2024-05-28
    public boolean isValidInputFileName(String name) {
        for (final String fileExtension : fileExtensions) {
            final String manifestFileName = manifestFileBasename + fileExtension;
            if (".xml".equalsIgnoreCase(fileExtension)) {
                final String alternativeManifestFileName = alternativeManifestFileBasename + fileExtension;
                if (manifestFileName.equalsIgnoreCase(name) || alternativeManifestFileName.equalsIgnoreCase(name)) {
                    return true;
                }
            } else if (".zip".equalsIgnoreCase(fileExtension)) {
                // we assume that a zip with a matching name pattern is a valid product 2024-05-28 tb
                return ".zip".equalsIgnoreCase(FileUtils.getExtension(name));
            } else {
                return false;
            }
        }
        return false;
    }

    // public access for testing only 2024-05-28
    public boolean isInputValid(Object input) {
        final String inputString = input.toString();
        final String filename = FileUtils.getFilenameFromPath(inputString);

        if (!isValidInputFileName(filename)) {
            return false;
        }

        final Path path = Paths.get(inputString);
        Path parentPath = path.getParent();
        final String parentFileName;
        if (parentPath == null) {
            parentFileName = path.getFileName().toString();
        } else {
            parentFileName = parentPath.getName(0).toString();
        }
        final String extension = FileUtils.getExtension(parentFileName);
        if (".zip".equalsIgnoreCase(extension)) {
            String zipName = FileUtils.getFilenameWithoutExtension(parentFileName);
            return isValidSourceName(zipName);
        }

        // the manifest in directory case 2024-05-28 tb


        return isValidSourceName(parentFileName);
    }

    // public access for testing only 2025-05-27
    public boolean isValidSourceName(String name) {
        return sourceNamePattern.matcher(name).matches();
    }
}
