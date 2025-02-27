/*
 * Copyright (C) 2014-2015 CS-SI (foss-contact@thor.si.c-s.fr)
 * Copyright (C) 2014-2015 CS-Romania (office@c-s.ro)
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

package eu.esa.opt.dataio.nitf;

import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.DateTimeUtils;

/**
 * Contains parsed metadata from a NITF file.
 *
 * @author Cosmin Cara
 */
public class NITFMetadata {

    final int FIRST_IMAGE = 0;
    MetadataElement root;

    NITFMetadata() {
    }

    public MetadataElement getMetadataRoot() {
        return root;
    }

    void setRootElement(MetadataElement newRoot) {
        root = newRoot;
    }

    public ProductData.UTC getFileDate() {
        ProductData.UTC fileDate = null;
        MetadataElement currentElement = root.getElement(NITFFields.TAG_IMAGE_INFO);
        if (currentElement != null) {
            try {
                fileDate = DateTimeUtils.parseDate(currentElement.getAttributeString(NITFFields.FDT, ""), "ddHHmmss'Z'MMMyy");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fileDate;
    }

    public String getFileTitle() {
        String ret = "";
        MetadataElement currentElement = root.getElement(NITFFields.TAG_IMAGE_INFO);
        if (currentElement != null) {
            ret = currentElement.getAttributeString(NITFFields.FTITLE, "");
        }
        return ret;
    }

    public boolean isEncrypted() {
        boolean ret = false;
        MetadataElement currentElement = root.getElement(NITFFields.TAG_IMAGE_INFO);
        if (currentElement != null) {
            int val = Integer.parseInt(currentElement.getAttributeString(NITFFields.ENCRYP, "0"));
            ret = (val == 1);
        }
        return ret;
    }

    /**
     * Returns the width, in pixels, of the first image.
     *
     * @return the number of pixels, or 0 if read fails.
     */
    public int getWidth() {
        return getWidth(FIRST_IMAGE);
    }

    /**
     * Returns the width, in pixels, of the imageIndex-th image.
     *
     * @param imageIndex the image index (0-based)
     * @return the number of pixels, or 0 if read fails
     */
    public int getWidth(int imageIndex) {
        int ret = 0;
        if (imageIndex < 0)
            throw new IllegalArgumentException("Invalid image index");
        MetadataElement currentElement = root.getElement(NITFFields.TAG_IMAGE_INFO);
        if (currentElement != null) {
            ret = Integer.parseInt(currentElement.getAttributeString(NITFFields.WIDTH, "0"));
        }
        return ret;
    }

    /**
     * Returns the height, in pixels, of the first image.
     *
     * @return the number of pixels, or 0 if read fails.
     */
    public int getHeight() {
        return getHeight(FIRST_IMAGE);
    }

    /**
     * Returns the height, in pixels, of the imageIndex-th image.
     *
     * @param imageIndex The image index (0-based)
     * @return the number of pixels, or 0 if read fails.
     */
    public int getHeight(int imageIndex) {
        int ret = 0;
        if (imageIndex < 0)
            throw new IllegalArgumentException("Invalid image index");
        MetadataElement currentElement = root.getElement(NITFFields.TAG_IMAGE_INFO);
        if (currentElement != null) {
            ret = Integer.parseInt(currentElement.getAttributeString(NITFFields.HEIGHT, "0"));
        }
        return ret;
    }

    /**
     * Returns the data type value of a pixel of the first image.
     *
     * @return One of the ProductData.TYPE_* values.
     * @see ProductData
     */
    public int getDataType() {
        return getDataType(FIRST_IMAGE);
    }

    /**
     * Returns the data type value of a pixel of the imageIndex-th image.
     *
     * @param imageIndex the image index (0-based)
     * @return the data type value, or <code>ProductData.TYPE_UNDEFINED</code> if read fails
     */
    public int getDataType(int imageIndex) {
        int ret = ProductData.TYPE_UNDEFINED;
        if (imageIndex < 0)
            throw new IllegalArgumentException("Invalid image index");
        MetadataElement currentElement = root.getElement(NITFFields.TAG_IMAGE_INFO);
        if (currentElement != null) {
            String valString = currentElement.getAttributeString(NITFFields.PVTYPE);
            int abpp = Integer.parseInt(currentElement.getAttributeString(NITFFields.ABPP));
           // int nbpp = Integer.parseInt(currentElement.getAttributeString(NITFFields.NBPP)); //TODO -> find a solution to get NBPP attribute which is not returned by GDAL
           // if (abpp <= nbpp) {
           //     switch (nbpp) {
                 switch (abpp) {
                    case 1:
                        if ("B".equals(valString)) {
                            ret = ProductData.TYPE_UNDEFINED; // support for bit type ??
                        }
                        break;
                    case 8:
                        if ("INT".equals(valString)) {
                            ret = ProductData.TYPE_UINT8;
                        } else if ("SI".equals(valString)) {
                            ret = ProductData.TYPE_INT8;
                        }
                        break;
                    case 12:
                        if ("INT".equals(valString)) {
                            ret = ProductData.TYPE_UINT16;
                        } else if ("SI".equals(valString)) {
                            ret = ProductData.TYPE_INT16;
                        }
                        break;
                    case 16:
                        if ("INT".equals(valString)) {
                            ret = ProductData.TYPE_UINT16;
                        } else if ("SI".equals(valString)) {
                            ret = ProductData.TYPE_INT16;
                        }
                        break;
                    case 32:
                        if ("INT".equals(valString)) {
                            ret = ProductData.TYPE_UINT32;
                        } else if ("SI".equals(valString)) {
                            ret = ProductData.TYPE_INT32;
                        } else if ("R".equals(valString)) {
                            ret = ProductData.TYPE_FLOAT32;
                        }
                        break;
                    case 64:
                        if ("INT".equals(valString)) {
                            ret = ProductData.TYPE_UNDEFINED;   // support for 64-bit integers ??
                        } else if ("SI".equals(valString)) {
                            ret = ProductData.TYPE_UNDEFINED;
                        } else if ("R".equals(valString)) {
                            ret = ProductData.TYPE_FLOAT64;
                        }
                        break;
                    default:
                        ret = ProductData.TYPE_UNDEFINED;
                        break;
                }
            //}
        }
        return ret;
    }

    public String getUnit() {
        return getUnit(FIRST_IMAGE);
    }

    public String getUnit(int imageIndex) {
        String ret = null;
        if (imageIndex < 0)
            throw new IllegalArgumentException("Invalid image index");
        MetadataElement currentElement = root.getElement(NITFFields.TAG_IMAGE_INFO);
        if (currentElement != null) {
            String value = currentElement.getAttributeString(NITFFields.ICAT, null);
            if (value != null) {
                if ("MS".equals(value) || "HS".equals(value) || "IR".equals(value)) {
                    ret = "nm";
                }
            }
        }
        return ret;
    }
}
