package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InstrumentDataReaderTest {

    @Test
    @STTM("SNAP-3542")
    public void testGetMetadataElementName() {
        assertEquals("", InstrumentDataReader.getMetadataElementName("stupidName__Thing"));

        assertEquals("Covariances", InstrumentDataReader.getMetadataElementName("relative_spectral_covariance"));
        assertEquals("Central wavelengths", InstrumentDataReader.getMetadataElementName("lambda0"));
        assertEquals("Bandwidths", InstrumentDataReader.getMetadataElementName("FWHM"));
        assertEquals("Solar fluxes", InstrumentDataReader.getMetadataElementName("solar_flux"));
    }

    @Test
    @STTM("SNAP-3542")
    public void testGetMetadataAttributeName() {
        assertEquals("", InstrumentDataReader.getMetadataAttributeName("invalidAttribForTest"));

        assertEquals("Covariance", InstrumentDataReader.getMetadataAttributeName("relative_spectral_covariance"));
        assertEquals("Central wavelength", InstrumentDataReader.getMetadataAttributeName("lambda0"));
        assertEquals("Bandwidth", InstrumentDataReader.getMetadataAttributeName("FWHM"));
        assertEquals("Solar flux", InstrumentDataReader.getMetadataAttributeName("solar_flux"));
    }
}
