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
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.io.FileUtils;
import org.esa.snap.core.util.io.SnapFileFilter;

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
                        "SL_2_(LST|WCT|WST|FRP)|SY_1_SYN|SY_2_(AOD|VGP|SYN|V10)|SY_[23]_VG1)_.*(.SEN3)?(.zip)?",
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
        if (isManifestFile(name)) {
            return true;
        }
        final String extension = FileUtils.getExtension(name);
        if (".zip".equalsIgnoreCase(extension)) {
            final String nameWoExtension = FileUtils.getFilenameWithoutExtension(name);
            return isValidSourceName(nameWoExtension);
        } else if (".SEN3".equalsIgnoreCase(extension) || StringUtils.isNullOrBlank(extension)) {
            return isValidSourceName(name);
        }

        return false;
    }

    private boolean isManifestFile(String name) {
        final String manifestFileName = manifestFileBasename + ".xml";
        final String alternativeManifestFileName = alternativeManifestFileBasename + ".xml";
        return (manifestFileName.equalsIgnoreCase(name) || alternativeManifestFileName.equalsIgnoreCase(name));
    }

    // public access for testing only 2024-05-28
    public boolean isInputValid(Object input) {
        String inputString = input.toString();
        final String filename = FileUtils.getFilenameFromPath(inputString);

        // check if we have a directory name (not ending on xml)
        final String fileExtension = FileUtils.getExtension(filename);
        if (isDirectory(fileExtension)) {
            inputString = inputString + File.separator + manifestFileBasename + ".xml";
        }

        if (!isValidInputFileName(filename)) {
            return false;
        }

        if (".zip".equalsIgnoreCase(fileExtension)) {
            String zipName = FileUtils.getFilenameWithoutExtension(filename);
            return isValidSourceName(zipName);
        }

        final Path path = Paths.get(inputString);
        final String parentFileName;
        if (path.getParent() == null) {
            parentFileName = path.getFileName().toString();
        } else {
            int nameCount = path.getNameCount();
            parentFileName = path.getName(nameCount - 2).toString();
        }

        return isValidSourceName(parentFileName);
    }

    private static boolean isDirectory(String extension) {
        return !(".zip".equalsIgnoreCase(extension) || ".ZIP".equalsIgnoreCase(extension) || ".xml".equalsIgnoreCase(extension));
    }

    // public access for testing only 2025-05-27
    public boolean isValidSourceName(String name) {
        return sourceNamePattern.matcher(name).matches();
    }
}
