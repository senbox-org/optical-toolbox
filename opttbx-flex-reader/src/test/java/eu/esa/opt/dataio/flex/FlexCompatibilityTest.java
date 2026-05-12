package eu.esa.opt.dataio.flex;

import org.junit.Test;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class FlexCompatibilityTest {

    // --- StandardFlexCompatibility tests ---

    @Test
    public void testStandard_resolveDataFilePath_plain() {
        final StandardFlexCompatibility compat = new StandardFlexCompatibility();
        assertEquals("data.nc", compat.resolveDataFilePath("data.nc"));
    }

    @Test
    public void testStandard_resolveDataFilePath_stripsDotSlash() {
        final StandardFlexCompatibility compat = new StandardFlexCompatibility();
        assertEquals("data.nc", compat.resolveDataFilePath("./data.nc"));
    }

    @Test
    public void testStandard_resolveDimension_fromRoot() {
        final StandardFlexCompatibility compat = new StandardFlexCompatibility();
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        final Dimension dim = new Dimension("rows", 3640);
        when(ncFile.findDimension("rows")).thenReturn(dim);

        assertEquals(3640, compat.resolveDimension(ncFile, "Measurement_data", "rows", 0));
    }

    @Test
    public void testStandard_resolveDimension_notFound_returnsDefault() {
        final StandardFlexCompatibility compat = new StandardFlexCompatibility();
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension("rows")).thenReturn(null);

        assertEquals(3640, compat.resolveDimension(ncFile, "Measurement_data", "rows", 3640));
    }

    // --- EarlyProcessorCompatibility tests ---

    @Test
    public void testEarly_resolveDataFilePath_stripsDoubleNcExtension() {
        final EarlyProcessorCompatibility compat = new EarlyProcessorCompatibility();
        assertEquals("data.nc", compat.resolveDataFilePath("./data.nc.nc"));
    }

    @Test
    public void testEarly_resolveDataFilePath_singleNcUnchanged() {
        final EarlyProcessorCompatibility compat = new EarlyProcessorCompatibility();
        assertEquals("data.nc", compat.resolveDataFilePath("./data.nc"));
    }

    @Test
    public void testEarly_resolveDataFilePath_noPrefixDoubleNc() {
        final EarlyProcessorCompatibility compat = new EarlyProcessorCompatibility();
        assertEquals("data.nc", compat.resolveDataFilePath("data.nc.nc"));
    }

    @Test
    public void testEarly_resolveDimension_fromRoot() {
        final EarlyProcessorCompatibility compat = new EarlyProcessorCompatibility();
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        final Dimension dim = new Dimension("rows", 3640);
        when(ncFile.findDimension("rows")).thenReturn(dim);

        assertEquals(3640, compat.resolveDimension(ncFile, "Measurement_data", "rows", 0));
    }

    @Test
    public void testEarly_resolveDimension_fallsBackToGroup() {
        final EarlyProcessorCompatibility compat = new EarlyProcessorCompatibility();
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension("rows")).thenReturn(null);

        final Dimension groupDim = new Dimension("rows", 3640);
        final Group measurementGroup = mock(Group.class);
        when(measurementGroup.findDimensionLocal("rows")).thenReturn(groupDim);
        when(ncFile.findGroup("Measurement_data")).thenReturn(measurementGroup);

        assertEquals(3640, compat.resolveDimension(ncFile, "Measurement_data", "rows", 0));
    }

    @Test
    public void testEarly_resolveDimension_nestedGroupPath() {
        final EarlyProcessorCompatibility compat = new EarlyProcessorCompatibility();
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension("rows")).thenReturn(null);

        final Dimension groupDim = new Dimension("rows", 3640);
        final Group geometryGroup = mock(Group.class);
        when(geometryGroup.findDimensionLocal("rows")).thenReturn(groupDim);
        when(ncFile.findGroup("Annotation_data/Geometry")).thenReturn(geometryGroup);

        assertEquals(3640, compat.resolveDimension(ncFile, "Annotation_data/Geometry", "rows", 0));
    }

    @Test
    public void testEarly_resolveDimension_noGroupFound_returnsDefault() {
        final EarlyProcessorCompatibility compat = new EarlyProcessorCompatibility();
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension("rows")).thenReturn(null);
        when(ncFile.findGroup("NonExistent")).thenReturn(null);

        assertEquals(3640, compat.resolveDimension(ncFile, "NonExistent", "rows", 3640));
    }
}
