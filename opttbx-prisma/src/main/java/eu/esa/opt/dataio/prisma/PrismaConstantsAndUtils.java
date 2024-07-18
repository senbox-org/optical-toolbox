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

import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.IndexCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.SampleCoding;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

final class PrismaConstantsAndUtils {


    static final String FORMAT_NAME = "PRISMA";
    static final String DESCRIPTION = "Prisma ASI (Agenzia Spaziale Italiana)";
    static final String PRISMA_HDF_EXTENSION = ".he5";
    static final String PRISMA_FILENAME_REGEX
            = "PRS_L(1|2B|2C|2D)_.*_\\d{14}_\\d{14}_\\d+\\.he5";
    static final Pattern PRISMA_FILENAME_PATTERN
            = Pattern.compile(PRISMA_FILENAME_REGEX, Pattern.CASE_INSENSITIVE);
    static final Map<String, String> LEVEL_DEPENDENT_CUBE_MEASUREMENT_NAME = new HashMap<>() {{
        put("1", "_Ltoa");
        put("2B", "_Lboa");
        put("2C", "_Rrs");
        put("2D", "_Rrs");
    }};
    static final Map<String, String> LEVEL_DEPENDENT_CUBE_UNIT = new HashMap<>() {{
        put("1", "W/(str*um*m2)");
        put("2B", "W/(str*um*m2)");
        put("2C", "1");
        put("2D", "1");
    }};
    static final Map<String, String> VARIABLE_UNIT = new HashMap<>() {{
        put("AOT_Map", "1");
        put("AEX_Map", "1");
        put("WVM_Map", "g/cm2");
        put("COT_Map", "1");
    }};
    static final Map<String, String> VARIABLE_DESCRIPTION = new HashMap<>() {{
        put("AOT_Map", "Aerosol optical thickness");
        put("AEX_Map", "Angstrom exponent of the aerosol");
        put("WVM_Map", "Water Vapour columnar amount");
        put("COT_Map", "Clouds optical thickness");
    }};

    static final Map<String, String> LEVEL_DEPENDENT_AUTO_GROUPING = new HashMap<>() {{
        put("1",
            "HCO_Vnir:HCO_Swir:HCO:HCO_VNIR_PIXEL_SAT_ERR_MATRIX:HCO_SWIR_PIXEL_SAT_ERR_MATRIX:PCO:" +
            "HRC_Vnir:HRC_Swir:HRC:HRC_VNIR_PIXEL_SAT_ERR_MATRIX:HRC_SWIR_PIXEL_SAT_ERR_MATRIX:PRC");
        put("2B", "HCO_Vnir:HCO_Swir:HCO:HCO_VNIR_PIXEL_L2_ERR_MATRIX:HCO_SWIR_PIXEL_L2_ERR_MATRIX:PCO");
        put("2C", "HCO_Vnir:HCO_Swir:HCO:HCO_VNIR_PIXEL_L2_ERR_MATRIX:HCO_SWIR_PIXEL_L2_ERR_MATRIX:PCO");
        put("2D", "HCO_Vnir:HCO_Swir:HCO:HCO_VNIR_PIXEL_L2_ERR_MATRIX:HCO_SWIR_PIXEL_L2_ERR_MATRIX:PCO");
    }};

    static final Class<?>[] IO_TYPES = new Class[]{
            File.class,
            String.class
    };

    private static final InputConverter[] INPUT_CONVERTERS = new InputConverter[]{
            output -> (File) output,
            output -> Paths.get((String) output).toFile()
    };

    static File convertToFile(final Object object) {
        for (int i = 0; i < IO_TYPES.length; i++) {
            if (IO_TYPES[i].isInstance(object)) {
                return INPUT_CONVERTERS[i].convertInput(object);
            }
        }
        return null;
    }

    static <T> void getDataSubSampled(TypedGetter<T> src, int srcWidth, int srcStepX, int srcStepY, TypedSetter<T> dest, int destWidth, int destHeight) {
        for (int y = 0; y < destHeight; y++) {
            final int srcY = y * srcStepY;
            for (int x = 0; x < destWidth; x++) {
                final int srcX = x * srcStepX;
                final int srcIndex = srcY * srcWidth + srcX;
                final int destIndex = y * destWidth + x;
                dest.set(destIndex, src.get(srcIndex));
            }
        }
    }

    static SampleCoding createSampleCoding(Product product, String datasetName) {
        if ("SWIR_PIXEL_L2_ERR_MATRIX".equalsIgnoreCase(datasetName)
            || "VNIR_PIXEL_L2_ERR_MATRIX".equalsIgnoreCase(datasetName)) {
            final IndexCoding coding = new IndexCoding(datasetName);
            coding.addIndex("ok", 0, "pixel ok");
            coding.addIndex("inv", 1, "Invalid pixel from L1 product");
            coding.addIndex("neg", 2, "Negative value after atmospheric correction");
            coding.addIndex("sat", 3, "Saturated value after atmospheric correction");
            product.getIndexCodingGroup().add(coding);
            return coding;
        } else if ("MAPS_PIXEL_L2_ERR_MATRIX".equalsIgnoreCase(datasetName)) {
            final FlagCoding coding = new FlagCoding(datasetName);
//            coding.addFlag("ok", 0, "pixel ok");
            coding.addFlag("WVM_inv", 1, "Invalid pixel in WVM evaluation");
            coding.addFlag("WVM>max", 2, "Full - scale pixel in WVM evaluation (> max)");
            coding.addFlag("WVM<min", 4, "Full - scale pixel in WVM evaluation (< min)");
            coding.addFlag("AOD_!ev", 8, "AOD map not evaluated (not Dark-Dense Vegetation pixel or invalid pixel)");
            coding.addFlag("AOD>max", 16, "Full - scale pixel in AOD evaluation (> max)");
            coding.addFlag("AOD<min", 32, "Full - scale pixel in AOD evaluation (< min)");
            coding.addFlag("AEX_inv", 64, "Invalid pixel in AEX evaluation");
            coding.addFlag("COT_inv", 128, "Invalid pixel in COT evaluation");
            product.getFlagCodingGroup().add(coding);
            return coding;
        }
        return null;
    }

    interface TypedGetter<T> {
        T get(int index);
    }

    interface TypedSetter<T> {
        void set(int index, T value);
    }

    static void datatypeDependentDataTransfer(ByteBuffer sliceByteBuffer, int srcWidth, int srcHeight, int srcStepX, int srcStepY, ProductData destBuffer, int destWidth, int destHeight) {
        final int destBufferType = destBuffer.getType();
        final boolean subSampling = isSubSampling(srcStepX, srcStepY);
        if (destBufferType == ProductData.TYPE_INT8 || destBufferType == ProductData.TYPE_UINT8) {
            if (subSampling) {
                final byte[] src = sliceByteBuffer.array();
                final byte[] dest = (byte[]) destBuffer.getElems();
                getDataSubSampled(
                        index -> src[index], srcWidth, srcStepX, srcStepY,
                        (index1, value) -> dest[index1] = value, destWidth, destHeight);
            } else {
                final byte[] destBytes = (byte[]) destBuffer.getElems();
                sliceByteBuffer.get(destBytes);
            }
        } else if (destBufferType == ProductData.TYPE_INT16 || destBufferType == ProductData.TYPE_UINT16) {
            if (subSampling) {
                final short[] src = new short[srcWidth * srcHeight];
                sliceByteBuffer.asShortBuffer().get(src);
                final short[] dest = (short[]) destBuffer.getElems();
                getDataSubSampled(
                        index -> src[index], srcWidth, srcStepX, srcStepY,
                        (index1, value) -> dest[index1] = value, destWidth, destHeight);
            } else {
                final short[] shorts = (short[]) destBuffer.getElems();
                sliceByteBuffer.asShortBuffer().get(shorts);
            }
        } else if (destBufferType == ProductData.TYPE_INT32 || destBufferType == ProductData.TYPE_UINT32) {
            if (subSampling) {
                final int[] src = new int[srcWidth * srcHeight];
                sliceByteBuffer.asIntBuffer().get(src);
                final int[] dest = (int[]) destBuffer.getElems();
                getDataSubSampled(
                        index -> src[index], srcWidth, srcStepX, srcStepY,
                        (index1, value) -> dest[index1] = value, destWidth, destHeight);
            } else {
                final int[] ints = (int[]) destBuffer.getElems();
                sliceByteBuffer.asIntBuffer().get(ints);
            }
        } else if (destBufferType == ProductData.TYPE_INT64 || destBufferType == ProductData.TYPE_UINT64) {
            if (subSampling) {
                final long[] src = new long[srcWidth * srcHeight];
                sliceByteBuffer.asLongBuffer().get(src);
                final long[] dest = (long[]) destBuffer.getElems();
                getDataSubSampled(
                        index -> src[index], srcWidth, srcStepX, srcStepY,
                        (index1, value) -> dest[index1] = value, destWidth, destHeight);
            } else {
                final long[] longs = (long[]) destBuffer.getElems();
                sliceByteBuffer.asLongBuffer().get(longs);
            }
        } else if (destBufferType == ProductData.TYPE_FLOAT32) {
            if (subSampling) {
                final float[] floats = new float[srcWidth * srcHeight];
                sliceByteBuffer.asFloatBuffer().get(floats);
                final float[] dest = (float[]) destBuffer.getElems();
                getDataSubSampled(
                        index -> floats[index], srcWidth, srcStepX, srcStepY,
                        (index1, value) -> dest[index1] = value, destWidth, destHeight);
            } else {
                final float[] floats = (float[]) destBuffer.getElems();
                sliceByteBuffer.asFloatBuffer().get(floats);
            }
        } else if (destBufferType == ProductData.TYPE_FLOAT64) {
            if (subSampling) {
                final double[] src = new double[srcWidth * srcHeight];
                sliceByteBuffer.asDoubleBuffer().get(src);
                final double[] dest = (double[]) destBuffer.getElems();
                getDataSubSampled(
                        index -> src[index], srcWidth, srcStepX, srcStepY,
                        (index1, value) -> dest[index1] = value, destWidth, destHeight);
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

        File convertInput(Object input);
    }
}
