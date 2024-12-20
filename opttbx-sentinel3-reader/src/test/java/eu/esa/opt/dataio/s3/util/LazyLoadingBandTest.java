package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.dddb.VariableDescriptor;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LazyLoadingBandTest {

    @Test
    @STTM("SNAP-3711")
    public void testInitialize() {
        final VariableDescriptor descriptor = new VariableDescriptor();
        descriptor.setValidExpression("!my_fault");

        final LazyLoadingBand band = new LazyLoadingBand("Heinz", ProductData.TYPE_INT32, 23, 55, descriptor);
        band.setSpectralWavelength(675.3f);
        band.setSpectralBandwidth(9.8f);

        final Attribute fillAttribute = mock(Attribute.class);
        when(fillAttribute.getShortName()).thenReturn("_FillValue");
        when(fillAttribute.getNumericValue()).thenReturn(65535);

        final Variable variable = mock(Variable.class);
        when(variable.getFullName()).thenReturn("The description of a famous data type");
        when(variable.getUnitsString()).thenReturn("non/sense(squared)");
        when(variable.getDataType()).thenReturn(DataType.USHORT);
        when(variable.findAttribute("_FillValue")).thenReturn(fillAttribute);

        band.initialize(variable);

        assertEquals("Heinz", band.getName());
        assertEquals("The description of a famous data type", band.getDescription());
        assertEquals("non/sense(squared)", band.getUnit());
        assertEquals(ProductData.TYPE_INT32, band.getDataType());
        assertEquals(23, band.getRasterWidth());
        assertEquals(55, band.getRasterHeight());
        assertEquals("!my_fault", band.getValidPixelExpression());
        assertEquals(65535.0, band.getNoDataValue(), 1e-8);
        assertTrue(band.isNoDataValueSet());
        assertEquals(675.3f, band.getSpectralWavelength(), 1e-8);
        assertEquals(9.8f, band.getSpectralBandwidth(), 1e-8);
    }

    @Test
    @STTM("SNAP-3711")
    public void testInitialize_withoutNoDataValue() {
        final VariableDescriptor descriptor = new VariableDescriptor();
        final Variable variable = mock(Variable.class);

        final LazyLoadingBand band = new LazyLoadingBand("Heinz", ProductData.TYPE_INT32, 23, 55, descriptor);
        band.initialize(variable);

        assertFalse(band.isNoDataValueUsed());
        assertEquals(0, band.getNoDataValue(), 1e-8);
    }
}
