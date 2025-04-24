package eu.esa.opt.slstr.pdu.stitching;

import org.esa.snap.dataio.netcdf.NetCdfActivator;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Tonio Fincke
 */
public class NcFileStitcherLongTest {
    static {
        NetCdfActivator.activate();
    }

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testStitchF1_BT_io() throws Exception {
        File targetDirectory = tempFolder.newFolder("StitchF1_BT_io");

        final String ncFileName = "F1_BT_io.nc";
        final ImageSize targetImageSize = new ImageSize("io", 21687, 450, 6000, 900);
        final ImageSize[] imageSizes = new ImageSize[1];
        imageSizes[0] = new ImageSize("io", 23687, 450, 2000, 900);
        final Date now = Calendar.getInstance().getTime();
        final File[] ncFiles = new File[]{NcFileStitcherTest.getSecondNcFile(ncFileName)};

        final File stitchedFile = NcFileStitcher.stitchNcFiles(ncFileName, targetDirectory, now, ncFiles,
                targetImageSize, imageSizes);

        assertNotNull(stitchedFile);
        assertTrue(stitchedFile.exists());
        assertEquals(ncFileName, stitchedFile.getName());
        try (NetcdfFile netcdfFile = NetcdfFileOpener.open(stitchedFile)) {
            assertNotNull(netcdfFile);
            final List<Variable> variables = netcdfFile.getVariables();
            assertEquals(4, variables.size());
            Variable varF1_BT = variables.get(0);
            assertEquals("F1_BT_io", varF1_BT.getFullName());
            assertEquals(DataType.SHORT, varF1_BT.getDataType());
            assertEquals("rows columns", varF1_BT.getDimensionsString());
            assertTrue(varF1_BT.hasAttribute("_ChunkSize"));
            Array chunkLengths = varF1_BT.findAttribute("_ChunkSize").getValues();
            assertEquals(1, chunkLengths.getShape().length);
            assertEquals(2, chunkLengths.getShape()[0]);
            assertEquals(600, chunkLengths.getInt(0));
            assertEquals(450, chunkLengths.getInt(1));
            assertEquals("toa_brightness_temperature", varF1_BT.findAttribute("standard_name").getStringValue());
            assertEquals("Gridded pixel brightness temperature for channel F1 (1km TIR grid, oblique view)",
                    varF1_BT.findAttribute("long_name").getStringValue());
            assertEquals("K", varF1_BT.findAttribute("units").getStringValue());
            assertEquals((short) -32768, varF1_BT.findAttribute("_FillValue").getNumericValue());
            assertEquals(0.01, varF1_BT.findAttribute("scale_factor").getNumericValue());
            assertEquals(283.73, varF1_BT.findAttribute("add_offset").getNumericValue());

            assertEquals("F1_exception_io", variables.get(1).getFullName());
            assertEquals("F1_BT_orphan_io", variables.get(2).getFullName());

            Variable varF1_exceptions = variables.get(3);
            assertEquals("F1_exception_orphan_io", varF1_exceptions.getFullName());
            assertEquals(DataType.UBYTE, varF1_exceptions.getDataType());
            assertTrue(varF1_exceptions.getDataType().isUnsigned());
            assertEquals("rows orphan_pixels", varF1_exceptions.getDimensionsString());
            chunkLengths = varF1_exceptions.findAttribute("_ChunkSize").getValues();
            assertEquals(1, chunkLengths.getShape().length);
            assertEquals(2, chunkLengths.getShape()[0]);
            assertEquals(600, chunkLengths.getInt(0));
            assertEquals(112, chunkLengths.getInt(1));
            assertEquals("toa_brightness_temperature_status_flag", varF1_exceptions.findAttribute("standard_name").getStringValue());
            assertTrue(varF1_exceptions.findAttribute("flag_masks").isArray());
            final Array F1_exception_orphan_io_values = varF1_exceptions.findAttribute("flag_masks").getValues();
            assertEquals(8, F1_exception_orphan_io_values.getSize());
            assertTrue(F1_exception_orphan_io_values.isUnsigned());
            assertEquals((byte) 1, F1_exception_orphan_io_values.getByte(0));
            assertEquals((byte) 2, F1_exception_orphan_io_values.getByte(1));
            assertEquals((byte) 4, F1_exception_orphan_io_values.getByte(2));
            assertEquals((byte) 8, F1_exception_orphan_io_values.getByte(3));
            assertEquals((byte) 16, F1_exception_orphan_io_values.getByte(4));
            assertEquals((byte) 32, F1_exception_orphan_io_values.getByte(5));
            assertEquals((byte) 64, F1_exception_orphan_io_values.getByte(6));
            assertEquals((byte) 128, F1_exception_orphan_io_values.getByte(7));
            assertEquals("ISP_absent pixel_absent not_decompressed no_signal saturation invalid_radiance no_parameters unfilled_pixel",
                    varF1_exceptions.findAttribute("flag_meanings").getStringValue());
            assertEquals((byte) -128, varF1_exceptions.findAttribute("_FillValue").getNumericValue());

            List<Variable> inputFileVariables = new ArrayList<>();
            for (int i = 0; i < 1; i++) {
                final NetcdfFile ncfile = NetcdfFileOpener.open(ncFiles[i]);
                assertNotNull(ncfile);
                inputFileVariables = ncfile.getVariables();
            }
            for (int i = 0; i < variables.size(); i++) {
                Variable variable = variables.get(i);
                int offset = 2000;
                final Section section = new Section(new int[]{offset, 0}, new int[]{offset, variable.getDimension(1).getLength()});
                final Array stitchedArrayPart = variable.read(section);
                final Array fileArray = inputFileVariables.get(i).read();
                if (variable.getDataType() == DataType.SHORT) {
                    final short[] expectedArray = (short[]) fileArray.copyTo1DJavaArray();
                    final short[] actualArray = (short[]) stitchedArrayPart.copyTo1DJavaArray();
                    assertArrayEquals(expectedArray, actualArray);
                } else {
                    final byte[] expectedArray = (byte[]) fileArray.copyTo1DJavaArray();
                    final byte[] actualArray = (byte[]) stitchedArrayPart.copyTo1DJavaArray();
                    assertArrayEquals(expectedArray, actualArray);
                }
            }
        }
    }

    @Test
    public void testStitchMet_tx() throws IOException, PDUStitchingException, InvalidRangeException, URISyntaxException {
        File targetDirectory = tempFolder.newFolder("StitchMet_tx");

        final String ncFileName = "met_tx.nc";
        final ImageSize targetImageSize = new ImageSize("in", 21687, 64, 6000, 130);
        final ImageSize[] imageSizes = new ImageSize[3];
        imageSizes[0] = new ImageSize("in", 21687, 64, 2000, 130);
        imageSizes[1] = new ImageSize("in", 23687, 64, 2000, 130);
        imageSizes[2] = new ImageSize("in", 25687, 64, 2000, 130);
        final Date now = Calendar.getInstance().getTime();
        final File[] ncFiles = NcFileStitcherTest.getNcFiles(ncFileName);

        final File stitchedFile = NcFileStitcher.stitchNcFiles(ncFileName, targetDirectory, now, ncFiles,
                targetImageSize, imageSizes);

        assertNotNull(stitchedFile);
        assertTrue(stitchedFile.exists());
        assertEquals(ncFileName, stitchedFile.getName());
        try (NetcdfFile netcdfFile = NetcdfFileOpener.open(stitchedFile)) {
            assertNotNull(netcdfFile);
            final List<Variable> variables = netcdfFile.getVariables();
            assertEquals(28, variables.size());
            assertEquals("t_single", variables.get(0).getFullName());
            assertEquals(DataType.SHORT, variables.get(0).getDataType());
            assertEquals("sea_ice_fraction_tx", variables.get(9).getFullName());
            assertEquals(DataType.FLOAT, variables.get(9).getDataType());
            assertEquals("u_wind_tx", variables.get(10).getFullName());
            assertEquals(DataType.FLOAT, variables.get(10).getDataType());
            Variable varSnow_depth = variables.get(27);
            assertEquals("snow_depth_tx", varSnow_depth.getFullName());
            assertEquals(DataType.FLOAT, varSnow_depth.getDataType());
            assertEquals("1 600 130 ", varSnow_depth.findAttribute("_ChunkSize").getValues().toString());
            assertEquals("Snow liquid water equivalent depth", varSnow_depth.findAttribute("long_name").getStringValue());
            assertEquals("lwe_thickness_of_surface_snow_amount", varSnow_depth.findAttribute("standard_name").getStringValue());
            assertEquals("metre", varSnow_depth.findAttribute("units").getStringValue());
            assertEquals("ECMWF_F", varSnow_depth.findAttribute("model").getStringValue());
            assertEquals("141", varSnow_depth.findAttribute("parameter").getStringValue());

            final List<Variable>[] inputFileVariables = new ArrayList[3];
            for (int i = 0; i < inputFileVariables.length; i++) {
                final NetcdfFile ncFile = NetcdfFileOpener.open(ncFiles[i]);
                assertNotNull(ncFile);
                inputFileVariables[i] = new ArrayList<>(ncFile.getVariables());
            }
            for (int i = 0; i < variables.size(); i++) {
                Variable variable = variables.get(i);
                for (int j = 0; j < inputFileVariables.length; j++) {
                    int rowOffset = j * 2000;
                    final Variable inputFileVariable = inputFileVariables[j].get(i + 1);

                    final List<Dimension> inputFileVariableDimensions = inputFileVariable.getDimensions();
                    int[] origin = new int[inputFileVariableDimensions.size()];
                    int[] shape = new int[inputFileVariableDimensions.size()];
                    for (int k = 0; k < inputFileVariableDimensions.size(); k++) {
                        if (inputFileVariableDimensions.get(k).getFullName().equals("rows")) {
                            origin[k] = rowOffset;
                            shape[k] = 2000;
                        } else {
                            origin[k] = 0;
                            shape[k] = inputFileVariableDimensions.get(k).getLength();
                        }
                    }
                    final Section section = new Section(origin, shape);

                    final Array stitchedArrayPart = variable.read(section);
                    final Array fileArray = inputFileVariable.read();
                    if (variable.getDataType() == DataType.SHORT) {
                        final short[] expectedArray = (short[]) fileArray.copyTo1DJavaArray();
                        final short[] actualArray = (short[]) stitchedArrayPart.copyTo1DJavaArray();
                        assertArrayEquals(expectedArray, actualArray);
                    } else {
                        final float[] expectedArray = (float[]) fileArray.copyTo1DJavaArray();
                        final float[] actualArray = (float[]) stitchedArrayPart.copyTo1DJavaArray();
                        assertArrayEquals(expectedArray, actualArray, 1e-8f);
                    }
                }
            }
        }
    }


}