package eu.esa.opt.spectralresample;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.speclib.io.csv.util.CsvTable;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SpectralResamplingTest {

    @Test
    @STTM("SNAP-4174")
    public void test_resample_olci_to_enmap() throws Exception {
        SpectralResponseFunction srf = new SpectralResponseFunction("enmap");

        final URL resource = getClass().getResource(srf.getID() + ".csv");
        assertNotNull(resource);
        final File csvFile = new File(resource.toURI());
        final CsvTable srTable = SpectralResponseFunction.readSpectralResponsesFromCsv(csvFile);

        srf.setSpectralResponses(srTable);
        List<SpectralResponseFunction> fullSrfList =
                SpectralResponseFunction.getFullyDefinedSrf(srf.getSpectralResponsesList());

        final double[] inputSpectrum = new double[]{
                105.33761, 115.1706, 120.02343, 116.7073, 114.318726, 114.59385, 133.63246, 138.82625,
                138.58409, 138.04797, 132.03772, 131.85347, 37.904022, 65.76642, 114.50595, 122.30608,
                100.14883, 96.83181, 64.75161, 20.552023, 81.00681};

        final double[] inputWvls = new double[]{
                400.1732, 411.75812, 442.95776, 490.5534, 510.52353, 560.5521, 620.395, 665.3918,
                674.1551, 681.66376, 709.24176, 754.2953, 761.8105, 764.92523, 768.0407, 779.3815,
                865.4787, 884.3511, 899.3343, 938.9488, 1015.7598};

        final double[] resampledSpectrum = SpectralResampling.resample(inputSpectrum, inputWvls, fullSrfList);

        assertNotNull(resampledSpectrum);
        assertEquals(224, resampledSpectrum.length);
        assertEquals(115.17, resampledSpectrum[0], 1.E-2);
        assertEquals(0.0, resampledSpectrum[1], 1.E-2);
        assertEquals(114.32, resampledSpectrum[19], 1.E-2);
        assertEquals(114.59, resampledSpectrum[28], 1.E-2);
        assertEquals(133.63, resampledSpectrum[41], 1.E-2);
        assertEquals(122.11, resampledSpectrum[63], 1.E-2);
        assertEquals(95.75, resampledSpectrum[77], 1.E-2);
        assertEquals(20.55, resampledSpectrum[89], 1.E-2);
        assertEquals(0.0, resampledSpectrum[100], 1.E-2);
        assertEquals(81.01, resampledSpectrum[103], 1.E-2);
    }

    @Test
    @STTM("SNAP-4174")
    public void test_resample_enmap_to_olci() throws Exception {
        // TODO
    }

    @Test
    @STTM("SNAP-4174")
    public void test_resample_prisma_to_enmap() throws Exception {
        // TODO
    }

    @Test
    @STTM("SNAP-4174")
    public void test_resample_enmap_to_prisma() throws Exception {
        // TODO
    }
}
