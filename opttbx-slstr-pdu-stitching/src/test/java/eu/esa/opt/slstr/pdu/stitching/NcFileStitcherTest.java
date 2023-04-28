package eu.esa.opt.slstr.pdu.stitching;

import com.bc.ceres.binding.converters.DateFormatConverter;
import org.esa.snap.dataio.netcdf.NetCdfActivator;
import org.esa.snap.dataio.netcdf.nc.NFileWriteable;
import org.esa.snap.dataio.netcdf.nc.NVariable;
import org.esa.snap.dataio.netcdf.nc.NWritableFactory;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayLong;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Tonio Fincke
 */
public class NcFileStitcherTest {

    static {
        NetCdfActivator.activate();
    }

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private NetcdfFile netcdfFile;

    @Before
    public void setUp() {
        netcdfFile = null;
    }

    @After
    public void tearDown() throws IOException {
        if (netcdfFile != null) {
            netcdfFile.close();
        }

    }

    @Test
    public void testStitchViscal() throws IOException, PDUStitchingException, URISyntaxException {
        final String ncFileName = "viscal.nc";
        final ImageSize targetImageSize = new ImageSize("xx", 0, 0, 0, 0);
        final ImageSize[] imageSizes = new ImageSize[3];
        imageSizes[0] = new ImageSize("xx", 0, 0, 0, 0);
        imageSizes[1] = new ImageSize("xx", 0, 0, 0, 0);
        imageSizes[2] = new ImageSize("xx", 0, 0, 0, 0);
        final Date now = Calendar.getInstance().getTime();
        final File[] ncFiles = getNcFiles(ncFileName);

        File targetDirectory = tempFolder.newFolder("StitchViscal");
        final File stitchedFile = NcFileStitcher.stitchNcFiles(ncFileName, targetDirectory, now, ncFiles,
                targetImageSize, imageSizes);

        Assert.assertNotNull(stitchedFile);
        assertTrue(stitchedFile.exists());
        assertEquals(ncFileName, stitchedFile.getName());
        netcdfFile = NetcdfFileOpener.open(stitchedFile);
        assertNotNull(netcdfFile);
        final List<Variable> variables = netcdfFile.getVariables();
        assertEquals(40, variables.size());
        assertEquals("ANX_time", variables.get(0).getFullName());
        assertEquals(DataType.STRING, variables.get(0).getDataType());
        assertEquals("calibration_time", variables.get(1).getFullName());
        assertEquals(DataType.STRING, variables.get(1).getDataType());
    }

    @Test
    public void testStitchMet_tx() throws IOException, PDUStitchingException, InvalidRangeException, URISyntaxException {
        final String ncFileName = "met_tx.nc";
        final ImageSize targetImageSize = new ImageSize("in", 21687, 64, 6000, 130);
        final ImageSize[] imageSizes = new ImageSize[3];
        imageSizes[0] = new ImageSize("in", 21687, 64, 2000, 130);
        imageSizes[1] = new ImageSize("in", 23687, 64, 2000, 130);
        imageSizes[2] = new ImageSize("in", 25687, 64, 2000, 130);
        final Date now = Calendar.getInstance().getTime();
        final File[] ncFiles = getNcFiles(ncFileName);

        File targetDirectory = tempFolder.newFolder("StitchMet_tx");
        final File stitchedFile = NcFileStitcher.stitchNcFiles(ncFileName, targetDirectory, now, ncFiles,
                targetImageSize, imageSizes);

        assert (stitchedFile != null);
        assert (stitchedFile.exists());
        assertEquals(ncFileName, stitchedFile.getName());
        netcdfFile = NetcdfFileOpener.open(stitchedFile);
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
        for (int i = 0; i < 3; i++) {
            final NetcdfFile ncFile = NetcdfFileOpener.open(ncFiles[i]);
            assertNotNull(ncFile);
            inputFileVariables[i] = ncFile.getVariables();
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

    @Test
    public void testStitchF1_BT_io() throws Exception {
        final String ncFileName = "F1_BT_io.nc";
        final ImageSize targetImageSize = new ImageSize("io", 21687, 450, 6000, 900);
        final ImageSize[] imageSizes = new ImageSize[1];
        imageSizes[0] = new ImageSize("io", 23687, 450, 2000, 900);
        final Date now = Calendar.getInstance().getTime();
        final File[] ncFiles = new File[]{getSecondNcFile(ncFileName)};

        File targetDirectory = tempFolder.newFolder("StitchF1_BT_io");
        final File stitchedFile = NcFileStitcher.stitchNcFiles(ncFileName, targetDirectory, now, ncFiles,
                targetImageSize, imageSizes);

        assert (stitchedFile != null);
        assert (stitchedFile.exists());
        assertEquals(ncFileName, stitchedFile.getName());
        netcdfFile = NetcdfFileOpener.open(stitchedFile);
        assertNotNull(netcdfFile);
        final List<Variable> variables = netcdfFile.getVariables();
        assertEquals(4, variables.size());
        Variable varF1_BT_io = variables.get(0);
        assertEquals("F1_BT_io", varF1_BT_io.getFullName());
        assertEquals(DataType.SHORT, varF1_BT_io.getDataType());
        assertEquals("rows columns", varF1_BT_io.getDimensionsString());
        Array chunkLengths = varF1_BT_io.findAttribute("_ChunkSize").getValues();
        assertEquals(1, chunkLengths.getShape().length);
        assertEquals(2, chunkLengths.getShape()[0]);
        assertEquals(600, chunkLengths.getInt(0));
        assertEquals(450, chunkLengths.getInt(1));
        assertEquals("toa_brightness_temperature", varF1_BT_io.findAttribute("standard_name").getStringValue());
        assertEquals("Gridded pixel brightness temperature for channel F1 (1km TIR grid, oblique view)",
                varF1_BT_io.findAttribute("long_name").getStringValue());
        assertEquals("K", varF1_BT_io.findAttribute("units").getStringValue());
        assertEquals((short) -32768, varF1_BT_io.findAttribute("_FillValue").getNumericValue());
        assertEquals(0.01, varF1_BT_io.findAttribute("scale_factor").getNumericValue());
        assertEquals(283.73, varF1_BT_io.findAttribute("add_offset").getNumericValue());

        assertEquals("F1_exception_io", variables.get(1).getFullName());
        assertEquals("F1_BT_orphan_io", variables.get(2).getFullName());

        Variable varF1_exception = variables.get(3);
        assertEquals("F1_exception_orphan_io", varF1_exception.getFullName());
        assertEquals(DataType.UBYTE, varF1_exception.getDataType());
        assertTrue(varF1_exception.getDataType().isUnsigned());
        assertEquals("rows orphan_pixels", varF1_exception.getDimensionsString());
        chunkLengths = varF1_exception.findAttribute("_ChunkSize").getValues();
        assertEquals(1, chunkLengths.getShape().length);
        assertEquals(2, chunkLengths.getShape()[0]);
        assertEquals(600, chunkLengths.getInt(0));
        assertEquals(112, chunkLengths.getInt(1));
        assertEquals("toa_brightness_temperature_status_flag", varF1_exception.findAttribute("standard_name").getStringValue());
        assertTrue(varF1_exception.findAttribute("flag_masks").isArray());
        final Array F1_exception_orphan_io_values = varF1_exception.findAttribute("flag_masks").getValues();
        assertEquals(8, F1_exception_orphan_io_values.getSize());
        assertEquals(DataType.UBYTE, F1_exception_orphan_io_values.getDataType());
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
                varF1_exception.findAttribute("flag_meanings").getStringValue());
        assertEquals((byte) -128, varF1_exception.findAttribute("_FillValue").getNumericValue());

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

    @Test
    public void testSetGlobalAttributes() throws IOException {
        File targetDirectory = tempFolder.newFolder("SetGlobalAttributes");
        final File file = new File(targetDirectory, "something.nc");
        SlstrNFileWritable netcdfWriteable = new SlstrNFileWritable(file.getAbsolutePath());
        List<Attribute>[] attributeLists = new List[2];
        attributeLists[0] = new ArrayList<>();
        attributeLists[0].add(new Attribute("xyz", "yz"));
        attributeLists[0].add(new Attribute("abc", "23"));
        final ArrayByte someArray = new ArrayByte(new int[]{2}, true);
        someArray.setByte(0, (byte) 5);
        someArray.setByte(0, (byte) 5);
        attributeLists[0].add(new Attribute("def", someArray));
        attributeLists[1] = new ArrayList<>();
        attributeLists[1].add(new Attribute("xyz", "yz"));
        attributeLists[1].add(new Attribute("abc", "44"));
        attributeLists[1].add(new Attribute("defg", someArray));
        final DateFormatConverter globalAttributesDateFormatConverter =
                new DateFormatConverter(new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'"));
        final String someTimeAsString = globalAttributesDateFormatConverter.format(new GregorianCalendar(2013, 6, 7, 15, 32, 52).getTime());
        attributeLists[1].add(new Attribute("creation_time", someTimeAsString));

        final Date now = Calendar.getInstance().getTime();
        final String nowAsString = globalAttributesDateFormatConverter.format(now);
        NcFileStitcher.setGlobalAttributes(netcdfWriteable, attributeLists, targetDirectory.getName(), now);
        netcdfWriteable.create();
        netcdfWriteable.close();
        netcdfFile = NetcdfFileOpener.open(file);
        assertNotNull(netcdfFile);

        assertEquals("yz", netcdfFile.findGlobalAttribute("xyz").getStringValue());
        assertEquals("44", netcdfFile.findGlobalAttribute("abc_1").getStringValue());
        assertEquals("23", netcdfFile.findGlobalAttribute("abc").getStringValue());
        assertEquals(someArray.toString(), netcdfFile.findGlobalAttribute("def").getStringValue());
        assertEquals(someArray.toString(), netcdfFile.findGlobalAttribute("defg").getStringValue());
        assertEquals(nowAsString, netcdfFile.findGlobalAttribute("creation_time").getStringValue());
    }

    @Test
    public void testSetDimensions() throws IOException, PDUStitchingException {
        List<Dimension>[] dimensionLists = new ArrayList[2];
        List<Variable>[] variableLists = new ArrayList[2];

        File targetDirectory = tempFolder.newFolder("SetDimensions");
        final File inputFile1 = new File(targetDirectory, "input_1.nc");
        SlstrNFileWritable inputWriteable1 = new SlstrNFileWritable(inputFile1.getAbsolutePath());
        inputWriteable1.addDimension("rows", 5);
        inputWriteable1.addDimension("columns", 7);
        inputWriteable1.addDimension("the_twilight_zone", 12);
        final SlstrN4Variable abVariable1 = inputWriteable1.addVariable("ab", DataType.BYTE, true, "rows columns");
        final SlstrN4Variable cdVariable1 = inputWriteable1.addVariable("cd", DataType.LONG, false, "columns the_twilight_zone");
        inputWriteable1.create();
        abVariable1.writeFullyInSections(new ArrayByte(new int[]{5, 7}, false));
        cdVariable1.writeFullyInSections(new ArrayLong(new int[]{7, 12}, false));
        inputWriteable1.close();
        final NetcdfFile inputnc1 = NetcdfFileOpener.open(inputFile1);
        assertNotNull(inputnc1);
        dimensionLists[0] = inputnc1.getDimensions();
        variableLists[0] = inputnc1.getVariables();
        inputnc1.close();

        final File inputFile2 = new File(targetDirectory, "input_2.nc");
        final NFileWriteable inputWriteable2 = NWritableFactory.create(inputFile2.getAbsolutePath(), "netcdf4");
        inputWriteable2.addDimension("rows", 5);
        inputWriteable2.addDimension("columns", 7);
        inputWriteable2.addDimension("outer_limits", 25);
        final NVariable abVariable2 = inputWriteable2.addVariable("ab", DataType.BYTE, true, null, "rows columns");
        final NVariable cdVariable2 = inputWriteable2.addVariable("ef", DataType.LONG, false, null, "columns outer_limits");
        inputWriteable2.create();
        abVariable2.writeFully(new ArrayByte(new int[]{5, 7}, false));
        cdVariable2.writeFully(new ArrayLong(new int[]{7, 25}, false));
        inputWriteable2.close();
        final NetcdfFile inputnc2 = NetcdfFileOpener.open(inputFile2);
        assertNotNull(inputnc2);
        dimensionLists[1] = inputnc2.getDimensions();
        variableLists[1] = inputnc2.getVariables();
        inputnc2.close();

        final File file = new File(targetDirectory, "something.nc");
        SlstrNFileWritable netcdfWriteable = new SlstrNFileWritable(file.getAbsolutePath());
        ImageSize targetImageSize = new ImageSize("id", 10, 20, 10, 20);
        NcFileStitcher.setDimensions(netcdfWriteable, dimensionLists, targetImageSize, variableLists);
        netcdfWriteable.create();
        netcdfWriteable.close();
        netcdfFile = NetcdfFileOpener.open(file);
        assertNotNull(netcdfFile);
        final List<Dimension> dimensions = netcdfFile.getDimensions();
        assertEquals(4, dimensions.size());
        Map<String, Integer> expectedDimensions = new HashMap<>();
        expectedDimensions.put("rows", 10);
        expectedDimensions.put("columns", 20);
        expectedDimensions.put("the_twilight_zone", 12);
        expectedDimensions.put("outer_limits", 25);
        for (Dimension dimension : dimensions) {
            assertTrue(expectedDimensions.containsKey(dimension.getFullName()));
            assertEquals(expectedDimensions.get(dimension.getFullName()).intValue(), dimension.getLength());
        }
    }

    @Test
    public void testSetDimensions_VaryingDimensionLengths() throws IOException {
        List<Dimension>[] dimensionLists = new ArrayList[2];
        List<Variable>[] variableLists = new ArrayList[2];

        File targetDirectory = tempFolder.newFolder("SetDimensions_VaryingDimensionLengths");
        final File inputFile1 = new File(targetDirectory, "input_1.nc");
        final NFileWriteable inputWriteable1 = NWritableFactory.create(inputFile1.getAbsolutePath(), "netcdf4");
        inputWriteable1.addDimension("rows", 5);
        inputWriteable1.addDimension("columns", 7);
        inputWriteable1.addDimension("the_twilight_zone", 12);
        final NVariable abVariable1 = inputWriteable1.addVariable("ab", DataType.BYTE, true, null, "rows columns");
        final NVariable cdVariable1 = inputWriteable1.addVariable("cd", DataType.LONG, false, null, "columns the_twilight_zone");
        inputWriteable1.create();
        abVariable1.writeFully(new ArrayByte(new int[]{5, 7}, false));
        cdVariable1.writeFully(new ArrayLong(new int[]{7, 12}, false));
        inputWriteable1.close();
        final NetcdfFile inputnc1 = NetcdfFileOpener.open(inputFile1);
        assertNotNull(inputnc1);
        dimensionLists[0] = inputnc1.getDimensions();
        variableLists[0] = inputnc1.getVariables();
        inputnc1.close();

        final File inputFile2 = new File(targetDirectory, "input_2.nc");
        final NFileWriteable inputWriteable2 = NWritableFactory.create(inputFile2.getAbsolutePath(), "netcdf4");
        inputWriteable2.addDimension("rows", 5);
        inputWriteable2.addDimension("columns", 7);
        inputWriteable2.addDimension("the_twilight_zone", 25);
        final NVariable abVariable2 = inputWriteable2.addVariable("ab", DataType.BYTE, true, null, "rows columns");
        final NVariable cdVariable2 = inputWriteable2.addVariable("ef", DataType.LONG, false, null, "columns the_twilight_zone");
        inputWriteable2.create();
        abVariable2.writeFully(new ArrayByte(new int[]{5, 7}, false));
        cdVariable2.writeFully(new ArrayLong(new int[]{7, 25}, false));
        inputWriteable2.close();
        final NetcdfFile inputnc2 = NetcdfFileOpener.open(inputFile2);
        assertNotNull(inputnc2);
        dimensionLists[1] = inputnc2.getDimensions();
        variableLists[1] = inputnc2.getVariables();
        inputnc2.close();

        final File file = new File(targetDirectory, "something.nc");
        SlstrNFileWritable netcdfWriteable = new SlstrNFileWritable(file.getAbsolutePath());
        ImageSize targetImageSize = new ImageSize("id", 10, 20, 10, 20);
        try {
            NcFileStitcher.setDimensions(netcdfWriteable, dimensionLists, targetImageSize, variableLists);
            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("Dimension the_twilight_zone has different lengths across input files", e.getMessage());
        } finally {
            netcdfWriteable.create();
            netcdfWriteable.close();
        }
    }

    @Test
    public void testSetDimensions_VaryingValues() throws IOException {
        List<Dimension>[] dimensionLists = new ArrayList[2];
        List<Variable>[] variableLists = new ArrayList[2];

        File targetDirectory = tempFolder.newFolder("SetDimensions_VaryingValues");
        final File inputFile1 = new File(targetDirectory, "input_1.nc");
        final NFileWriteable inputWriteable1 = NWritableFactory.create(inputFile1.getAbsolutePath(), "netcdf4");
        inputWriteable1.addDimension("rows", 5);
        inputWriteable1.addDimension("columns", 7);
        inputWriteable1.addDimension("cd", 12);
        final NVariable abVariable1 = inputWriteable1.addVariable("ab", DataType.BYTE, true, null, "rows columns");
        final NVariable cdVariable1 = inputWriteable1.addVariable("cd", DataType.LONG, false, null, "cd");
        inputWriteable1.create();
        abVariable1.writeFully(new ArrayByte(new int[]{5, 7}, false));
        cdVariable1.writeFully(new ArrayLong(new int[]{12}, false));
        inputWriteable1.close();
        final NetcdfFile inputnc1 = NetcdfFileOpener.open(inputFile1);
        assertNotNull(inputnc1);
        dimensionLists[0] = inputnc1.getDimensions();
        variableLists[0] = inputnc1.getVariables();

        final File inputFile2 = new File(targetDirectory, "input_2.nc");
        final NFileWriteable inputWriteable2 = NWritableFactory.create(inputFile2.getAbsolutePath(), "netcdf4");
        inputWriteable2.addDimension("rows", 5);
        inputWriteable2.addDimension("columns", 7);
        inputWriteable2.addDimension("cd", 12);
        final NVariable abVariable2 = inputWriteable2.addVariable("ab", DataType.BYTE, true, null, "rows columns");
        final NVariable cdVariable2 = inputWriteable2.addVariable("cd", DataType.LONG, false, null, "cd");
        inputWriteable2.create();
        abVariable2.writeFully(new ArrayByte(new int[]{5, 7}, false));
        final ArrayLong values = new ArrayLong(new int[]{12}, false);
        values.setLong(10, 10);
        cdVariable2.writeFully(values);
        inputWriteable2.close();
        final NetcdfFile inputnc2 = NetcdfFileOpener.open(inputFile2);
        assertNotNull(inputnc2);
        dimensionLists[1] = inputnc2.getDimensions();
        variableLists[1] = inputnc2.getVariables();

        final File file = new File(targetDirectory, "something.nc");
        SlstrNFileWritable netcdfWriteable = new SlstrNFileWritable(file.getAbsolutePath());
        ImageSize targetImageSize = new ImageSize("id", 10, 20, 10, 20);
        try {
            NcFileStitcher.setDimensions(netcdfWriteable, dimensionLists, targetImageSize, variableLists);
            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("Values for cd are different across input files", e.getMessage());
        } finally {
            netcdfWriteable.create();
            inputnc1.close();
            inputnc2.close();
            netcdfWriteable.close();
        }
    }

    @Test
    public void testDetermineDestinationOffsets_f1_BT_in() {
        int[][] expected_f1_BT_in_DestinationOffsets = {{0}, {3000000}, {6000000}};

        int[] rowOffsets = new int[]{0, 2000, 4000};
        int[] numberOfRows = new int[]{2000, 2000, 2000};
        int[] sectionSizes_f1_BT_in = new int[]{3000000, 3000000, 3000000};
        int[][] sourceOffsets = new int[][]{{0}, {0}, {0}};

        final int[][] actual_f1_BT_in_DestinationOffsets =
                NcFileStitcher.determineDestinationOffsets(rowOffsets, numberOfRows, sectionSizes_f1_BT_in, sourceOffsets);

        for (int i = 0; i < expected_f1_BT_in_DestinationOffsets.length; i++) {
            assertArrayEquals(expected_f1_BT_in_DestinationOffsets[i], actual_f1_BT_in_DestinationOffsets[i]);
        }
    }

    @Test
    public void testDetermineDestinationOffsets_met_tx() {
        int[][] expected_met_tx_DestinationOffsets = {{0, 780000, 1560000, 2340000, 3120000},
                {260000, 1040000, 1820000, 2600000, 3380000}, {520000, 1300000, 2080000, 2860000, 3640000}};
        int[] sectionSizes_met_tx = new int[]{260000, 260000, 260000};
        int[] rowOffsets = new int[]{0, 2000, 4000};
        int[] numberOfRows = new int[]{2000, 2000, 2000};
        int[][] sourceOffsets = new int[][]{{0, 260000, 520000, 780000, 1040000}, {0, 260000, 520000, 780000, 1040000},
                {0, 260000, 520000, 780000, 1040000}};

        final int[][] actual_met_tx_DestinationOffsets =
                NcFileStitcher.determineDestinationOffsets(rowOffsets, numberOfRows, sectionSizes_met_tx, sourceOffsets);

        for (int i = 0; i < expected_met_tx_DestinationOffsets.length; i++) {
            assertArrayEquals(expected_met_tx_DestinationOffsets[i], actual_met_tx_DestinationOffsets[i]);
        }
    }

    @Test
    public void testDetermineDestinationOffsets_S1_quality_an() {
        int[][] expectedDestinationOffsets =
                {{0, 3396, 6792, 10188, 13584, 16980, 20376, 23772},
                        {1601, 4997, 8393, 11789, 15185, 18581, 21977, 25373},
                        {3201, 6597, 9993, 13389, 16785, 20181, 23577, 26973}};
        int[] sectionSizes = new int[]{1601, 1600, 195};
        int[] rowOffsets = new int[]{0, 1601, 3201};
        int[] numberOfRows = new int[]{1601, 1600, 195};
        int[][] sourceOffsets = new int[][]{{0, 1601, 3202, 4803, 6404, 8005, 9606, 11207},
                {0, 1600, 3200, 4800, 6400, 8000, 9600, 11200}, {0, 195, 390, 585, 780, 975, 1170, 1365}};

        final int[][] actualDestinationOffsets =
                NcFileStitcher.determineDestinationOffsets(rowOffsets, numberOfRows, sectionSizes, sourceOffsets);

        for (int i = 0; i < expectedDestinationOffsets.length; i++) {
            assertArrayEquals(expectedDestinationOffsets[i], actualDestinationOffsets[i]);
        }
    }

    @Test
    public void testDetermineDestinationOffsets_differentSectionSizes() {
        int[][] expectedDestinationOffsets = {{0}, {2000000}, {6500000}};
        int[] rowOffsets = new int[]{0, 2000, 6500};
        int[] numberOfRows = new int[]{2000, 4500, 1500};
        int[] sectionSizes = new int[]{2000000, 4500000, 1500000};
        int[][] sourceOffsets = new int[][]{{0}, {0}, {0}};

        final int[][] actual_f1_BT_in_DestinationOffsets =
                NcFileStitcher.determineDestinationOffsets(rowOffsets, numberOfRows, sectionSizes, sourceOffsets);

        for (int i = 0; i < expectedDestinationOffsets.length; i++) {
            assertArrayEquals(expectedDestinationOffsets[i], actual_f1_BT_in_DestinationOffsets[i]);
        }
    }

    @Test
    public void testDetermineSourceOffsets() throws IOException, URISyntaxException {
        final File f1_BT_io_file = getSecondNcFile("F1_BT_io.nc");
        final NetcdfFile f1_BT_io_netcdfFile = NetcdfFileOpener.open(f1_BT_io_file);
        assertNotNull(f1_BT_io_netcdfFile);
        final Variable f1_BT_io_variable = f1_BT_io_netcdfFile.getVariables().get(0);

        assertArrayEquals(new int[]{0}, NcFileStitcher.determineSourceOffsets(1800000, f1_BT_io_variable));

        final File met_tx_file = getFirstNcFile("met_tx.nc");
        final NetcdfFile met_tx_netcdfFile = NetcdfFileOpener.open(met_tx_file);
        assertNotNull(met_tx_netcdfFile);
        final Variable u_wind_tx_variable = met_tx_netcdfFile.getVariables().get(11);

        assertArrayEquals(new int[]{0, 260000, 520000, 780000, 1040000},
                NcFileStitcher.determineSourceOffsets(260000, u_wind_tx_variable));
    }

    @Test
    public void testDetermineSectionSize() throws IOException, URISyntaxException {
        final File f1_BT_io_file = getSecondNcFile("F1_BT_io.nc");
        final NetcdfFile f1_BT_io_netcdfFile = NetcdfFileOpener.open(f1_BT_io_file);
        assertNotNull(f1_BT_io_netcdfFile);
        final Variable f1_BT_io_variable = f1_BT_io_netcdfFile.getVariables().get(0);

        assertEquals(1800000, NcFileStitcher.determineSectionSize(0, f1_BT_io_variable));

        final File met_tx_file = getFirstNcFile("met_tx.nc");
        final NetcdfFile met_tx_netcdfFile = NetcdfFileOpener.open(met_tx_file);
        assertNotNull(met_tx_netcdfFile);
        final Variable u_wind_tx_variable = met_tx_netcdfFile.getVariables().get(11);

        assertEquals(260000, NcFileStitcher.determineSectionSize(2, u_wind_tx_variable));
    }

    static File[] getNcFiles(String fileName) throws URISyntaxException {
        return new File[]{getFirstNcFile(fileName), getSecondNcFile(fileName), getThirdNcFile(fileName)};
    }

    private static File getFirstNcFile(String fileName) throws URISyntaxException {
        return getNcFile(TestUtils.FIRST_FILE_NAME, fileName);
    }

    static File getSecondNcFile(String fileName) throws URISyntaxException {
        return getNcFile(TestUtils.SECOND_FILE_NAME, fileName);
    }

    private static File getThirdNcFile(String fileName) throws URISyntaxException {
        return getNcFile(TestUtils.THIRD_FILE_NAME, fileName);
    }

    private static File getNcFile(String fileName, String name) throws URISyntaxException {
        final String fullFileName = "/testing/" + fileName + "/" + name;
        final URL resource = NcFileStitcherTest.class.getResource(fullFileName);
        URI uri = new URI(resource.toString());
        return new File(uri.getPath());
    }

}