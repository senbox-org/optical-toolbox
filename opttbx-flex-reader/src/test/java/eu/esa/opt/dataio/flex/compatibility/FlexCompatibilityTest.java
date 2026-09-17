package eu.esa.opt.dataio.flex.compatibility;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class FlexCompatibilityTest {


    // --- StandardFlexCompatibility tests ---

    @Test
    @STTM("SNAP-4126")
    public void testStandard_resolveDataFilePath_plain() {
        final StandardFlexCompatibility compat = new StandardFlexCompatibility();
        assertEquals("data.nc", compat.resolveDataFilePath("data.nc"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testStandard_resolveDataFilePath_stripsDotSlash() {
        final StandardFlexCompatibility compat = new StandardFlexCompatibility();
        assertEquals("data.nc", compat.resolveDataFilePath("./data.nc"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testStandard_resolveDataFilePath_keepsDoubleNcExtension() {
        final StandardFlexCompatibility compat = new StandardFlexCompatibility();
        assertEquals("data.nc.nc", compat.resolveDataFilePath("data.nc.nc"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testStandard_resolveDimension_fromRoot() {
        final StandardFlexCompatibility compat = new StandardFlexCompatibility();
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        final Dimension dim = new Dimension("rows", 3640);
        when(ncFile.findDimension("rows")).thenReturn(dim);

        assertEquals(3640, compat.resolveDimension(ncFile, "Measurement_data", "rows", 0));
    }

    @Test
    @STTM("SNAP-4126")
    public void testStandard_resolveDimension_fromGroup() {
        final StandardFlexCompatibility compat = new StandardFlexCompatibility();
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension("rows")).thenReturn(null);

        final Dimension groupDim = new Dimension("rows", 3640);
        final Group measurementGroup = mock(Group.class);
        when(measurementGroup.findDimensionLocal("rows")).thenReturn(groupDim);
        when(ncFile.findGroup("Measurement_data")).thenReturn(measurementGroup);

        assertEquals(3640, compat.resolveDimension(ncFile, "Measurement_data", "rows", 0));
    }

    @Test
    @STTM("SNAP-4126")
    public void testStandard_resolveDimension_fromNestedGroup() {
        final StandardFlexCompatibility compat = new StandardFlexCompatibility();
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension("rows")).thenReturn(null);

        final Dimension groupDim = new Dimension("rows", 4138);
        final Group geometryGroup = mock(Group.class);
        when(geometryGroup.findDimensionLocal("rows")).thenReturn(groupDim);
        when(ncFile.findGroup("Annotation_data/Geometry")).thenReturn(geometryGroup);

        assertEquals(4138, compat.resolveDimension(ncFile, "Annotation_data/Geometry", "rows", 0));
    }

    @Test
    @STTM("SNAP-4126")
    public void testStandard_resolveDimension_notFound_returnsDefault() {
        final StandardFlexCompatibility compat = new StandardFlexCompatibility();
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension("rows")).thenReturn(null);

        assertEquals(3640, compat.resolveDimension(ncFile, "Measurement_data", "rows", 3640));
    }

    @Test
    @STTM("SNAP-4126")
    public void testStandard_resolveDimension_nullGroupPathReturnsDefault() {
        final StandardFlexCompatibility compat = new StandardFlexCompatibility();
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension("rows")).thenReturn(null);

        assertEquals(3640, compat.resolveDimension(ncFile, null, "rows", 3640));
        assertEquals(3640, compat.resolveDimension(ncFile, "", "rows", 3640));
        verify(ncFile, never()).findGroup(anyString());
    }

    @Test
    @STTM("SNAP-4126")
    public void testStandard_resolveDimension_groupFoundButDimensionMissing_returnsDefault() {
        final StandardFlexCompatibility compat = new StandardFlexCompatibility();
        final NetcdfFile ncFile = mock(NetcdfFile.class);
        when(ncFile.findDimension("rows")).thenReturn(null);

        final Group group = mock(Group.class);
        when(group.findDimensionLocal("rows")).thenReturn(null);
        when(ncFile.findGroup("Measurement_data")).thenReturn(group);

        assertEquals(3640, compat.resolveDimension(ncFile, "Measurement_data", "rows", 3640));
    }
}
