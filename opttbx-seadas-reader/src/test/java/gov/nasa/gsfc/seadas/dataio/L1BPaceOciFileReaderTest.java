package gov.nasa.gsfc.seadas.dataio;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class L1BPaceOciFileReaderTest {

    @Test
    @STTM("SNAP-4122")
    public void testGetVariableName()  {
        assertEquals("werner_675.776", L1BPaceOciFileReader.getVariableName("werner",675.776f));
        assertEquals("heffalump_298.5502", L1BPaceOciFileReader.getVariableName("heffalump",298.5502f));
    }

    @Test
    @STTM("SNAP-4122")
    public void testAddAttributes_slope_intercept() {
        final Variable variable = mock(Variable.class);
        when(variable.findAttribute("units")).thenReturn(new Attribute("units", "m^2/kg"));
        when(variable.findAttribute("long_name")).thenReturn(new Attribute("long_name", "Maria"));
        when(variable.findAttribute("slope")).thenReturn(new Attribute("slope", 18.65));
        when(variable.findAttribute("intercept")).thenReturn(new Attribute("intercept", -0.887));
        when(variable.findAttribute("bad_value_scaled")).thenReturn(new Attribute("bad_value_scaled", 88.108));

        final Band band = new Band("test", ProductData.TYPE_FLOAT64, 12, 14);
        L1BPaceOciFileReader.addAttributes(variable, band, true);

        assertEquals("m^2/kg", band.getUnit());
        assertEquals("Maria", band.getDescription());
        assertEquals(18.65, band.getScalingFactor(), 1e-8);
        assertEquals(-0.887, band.getScalingOffset(), 1e-8);
        assertEquals(88.108, band.getNoDataValue(), 1e-8);
        assertTrue(band.isNoDataValueUsed());
    }

    @Test
    @STTM("SNAP-4122")
    public void testAddAttributes_slope_intercept_applyScalingPath() {
        final Variable variable = mock(Variable.class);
        when(variable.findAttribute("units")).thenReturn(new Attribute("units", "m^2/kg"));
        when(variable.findAttribute("long_name")).thenReturn(new Attribute("long_name", "Maria"));
        when(variable.findAttribute("slope")).thenReturn(new Attribute("slope", 18.65));
        when(variable.findAttribute("intercept")).thenReturn(new Attribute("intercept", -0.887));
        when(variable.findAttribute("bad_value_scaled")).thenReturn(new Attribute("bad_value_scaled", 88.108));

        final Band band = new Band("test", ProductData.TYPE_FLOAT64, 12, 14);
        L1BPaceOciFileReader.addAttributes(variable, band, false);

        assertEquals("m^2/kg", band.getUnit());
        assertEquals("Maria", band.getDescription());
        assertEquals(1.0, band.getScalingFactor(), 1e-8);
        assertEquals(0.0, band.getScalingOffset(), 1e-8);
        assertEquals(0.0, band.getNoDataValue(), 1e-8);
        assertFalse(band.isNoDataValueUsed());
    }

    @Test
    @STTM("SNAP-4122")
    public void testAddAttributes_scale_offset() {
        final Variable variable = mock(Variable.class);
        when(variable.findAttribute("slope")).thenReturn(new Attribute("scale_factor", 0.855));
        when(variable.findAttribute("intercept")).thenReturn(new Attribute("add_offset", 100.4));

        final Band band = new Band("test", ProductData.TYPE_FLOAT64, 12, 14);
        L1BPaceOciFileReader.addAttributes(variable, band, true);

        assertEquals(0.855, band.getScalingFactor(), 1e-8);
        assertEquals(100.4, band.getScalingOffset(), 1e-8);
        assertFalse(band.isNoDataValueUsed());
    }

    @Test
    @STTM("SNAP-4122")
    public void testStripWavelengthFromName() {
        assertEquals("rhot_blue", L1BPaceOciFileReader.stripWavelengthFromName("rhot_blue_123.554"));
        assertEquals("rhot_red", L1BPaceOciFileReader.stripWavelengthFromName("rhot_red_642.228"));
    }
}
