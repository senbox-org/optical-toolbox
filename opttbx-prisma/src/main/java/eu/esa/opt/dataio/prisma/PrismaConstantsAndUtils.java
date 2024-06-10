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

import org.esa.snap.core.datamodel.ProductData;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

final class PrismaConstantsAndUtils {


    static final String FORMAT_NAME = "PRISMA";
    static final String DESCRIPTION = "Prisma ASI (Agenzia Spaziale Italiana)";
    static final String PRISMA_HDF_EXTENSION = ".he5";
    static final String PRISMA_ZIP_CONTAINER_EXTENSION = ".zip";
    static final String PRISMA_FILENAME_REGEX
            = "PRS_L(1|2B|2C|2D)_.*_\\d{14}_\\d{14}_\\d+\\.(he5|zip)";
    static final Pattern PRISMA_FILENAME_PATTERN
            = Pattern.compile(PRISMA_FILENAME_REGEX, Pattern.CASE_INSENSITIVE);
    static final String GROUP_NAME_PATTERN_HCO = "PRS_.*_HCO";
    static final Map<String, String> LEVEL_DEPENDENT_CUBE_KONTEXT_IDENTIFIER = new HashMap<>(){{
        put("1", "_Ltoa");
        put("2B", "_Lboa");
        put("2C", "_Rrs");
        put("2D", "_Rrs");
    }};
    static final Map<String, String> LEVEL_DEPENDENT_CUBE_UNIT = new HashMap<>(){{
        put("1", "_Ltoa");
        put("2B", "_Lboa");
        put("2C", "1");
        put("2D", "1");
    }};

    static final Class<?>[] IO_TYPES = new Class[]{
            Path.class,
            File.class,
            String.class
    };

    private static final InputConverter[] INPUT_CONVERTERS = new InputConverter[]{
            output -> (Path) output,
            output -> ((File) output).toPath(),
            output -> Paths.get((String) output)
    };
    static final String DATASET_NAME_LONGITUDE = "Longitude";

    static Path convertToPath(final Object object) {
        for (int i = 0; i < IO_TYPES.length; i++) {
            if (IO_TYPES[i].isInstance(object)) {
                return INPUT_CONVERTERS[i].convertInput(object);
            }
        }
        return null;
    }

    static void getDataSubSampled(Object src, int srcWidth, int srcHeight, int srcStepX, int srcStepY, Object dest, int destWidth, int destHeight) {
        for (int y = 0; y < destHeight; y++) {
            final int srcY = y * srcStepY;
//            if (srcY >= srcHeight) {
//                break;
//            }
            for (int x = 0; x < destWidth; x++) {
                final int srcX = x * srcStepX;
//                if (srcX >= srcWidth) {
//                    break;
//                }
                final int srcIndex = srcY * srcWidth + srcX;
                final int destIndex = y * destWidth + x;
                System.arraycopy(src, srcIndex, dest, destIndex, 1);
            }
        }
    }

    static void datatypeDependentDataTransfer(ByteBuffer sliceByteBuffer, int srcWidth, int srcHeight, int srcStepX, int srcStepY, ProductData destBuffer, int destOffsetX, int destOffsetY, int destWidth, int destHeight) {
        final int destBufferType = destBuffer.getType();
        final boolean subSampling = isSubSampling(srcStepX, srcStepY);
        if (destBufferType == ProductData.TYPE_INT8 || destBufferType == ProductData.TYPE_UINT8) {
            if (subSampling) {
                getDataSubSampled(
                        sliceByteBuffer.array(), srcWidth, srcHeight, srcStepX, srcStepY,
                        destBuffer.getElems(), destWidth, destHeight);
            } else {
                final byte[] destBytes = (byte[]) destBuffer.getElems();
                sliceByteBuffer.get(destBytes);
            }
        } else if (destBufferType == ProductData.TYPE_INT16 || destBufferType == ProductData.TYPE_UINT16) {
            if (subSampling) {
                final short[] shorts = new short[srcWidth * srcHeight];
                sliceByteBuffer.asShortBuffer().get(shorts);
                getDataSubSampled(
                        shorts, srcWidth, srcHeight, srcStepX, srcStepY,
                        destBuffer.getElems(), destWidth, destHeight);
            } else {
                final short[] shorts = (short[]) destBuffer.getElems();
                sliceByteBuffer.asShortBuffer().get(shorts);
            }
        } else if (destBufferType == ProductData.TYPE_INT32 || destBufferType == ProductData.TYPE_UINT32) {
            if (subSampling) {
                final int[] ints = new int[srcWidth * srcHeight];
                sliceByteBuffer.asIntBuffer().get(ints);
                getDataSubSampled(
                        ints, srcWidth, srcHeight, srcStepX, srcStepY,
                        destBuffer.getElems(), destWidth, destHeight);
            } else {
                final int[] ints = (int[]) destBuffer.getElems();
                sliceByteBuffer.asIntBuffer().get(ints);
            }
        } else if (destBufferType == ProductData.TYPE_INT64 || destBufferType == ProductData.TYPE_UINT64) {
            if (subSampling) {
                final long[] longs = new long[srcWidth * srcHeight];
                sliceByteBuffer.asLongBuffer().get(longs);
                getDataSubSampled(
                        longs, srcWidth, srcHeight, srcStepX, srcStepY,
                        destBuffer.getElems(), destWidth, destHeight);
            } else {
                final long[] longs = (long[]) destBuffer.getElems();
                sliceByteBuffer.asLongBuffer().get(longs);
            }
        } else if (destBufferType == ProductData.TYPE_FLOAT32) {
            if (subSampling) {
                final float[] floats = new float[srcWidth * srcHeight];
                sliceByteBuffer.asFloatBuffer().get(floats);
                getDataSubSampled(
                        floats, srcWidth, srcHeight, srcStepX, srcStepY,
                        destBuffer.getElems(), destWidth, destHeight);
            } else {
                final float[] floats = (float[]) destBuffer.getElems();
                sliceByteBuffer.asFloatBuffer().get(floats);
            }
        } else if (destBufferType == ProductData.TYPE_FLOAT64) {
            if (subSampling) {
                final double[] doubles = new double[srcWidth * srcHeight];
                sliceByteBuffer.asDoubleBuffer().get(doubles);
                getDataSubSampled(
                        doubles, srcWidth, srcHeight, srcStepX, srcStepY,
                        destBuffer.getElems(), destWidth, destHeight);
            } else {
                final double[] doubles = (double[]) destBuffer.getElems();
                sliceByteBuffer.asDoubleBuffer().get(doubles);
            }
        } else {
            throw new IllegalArgumentException("Unsupported data type: " + destBufferType);
        }
    }

    static boolean isSubSampling(int srcStepX, int srcStepY) {
        return srcStepX != 1 || srcStepY != 1;
    }

    private interface InputConverter {

        Path convertInput(Object input);
    }
}
