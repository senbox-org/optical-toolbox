package eu.esa.opt.spectralresample;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.speclib.io.csv.util.CsvTable;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SpectralResponseFunctionTest {


    @Test
    @STTM("SNAP-4174")
    public void test_setupSrfFromCsv_enmap() throws Exception {
        SpectralResponseFunction srf = new SpectralResponseFunction("enmap");
        assertNotNull(srf.getID());
        assertEquals("enmap", srf.getID());
        assertNotNull(srf.getSpectralResponsesList());
        assertEquals(0, srf.getSpectralResponsesList().size());

        final URL resource = getClass().getResource(srf.getID() + ".csv");
        assertNotNull(resource);
        final File csvFile = new File(resource.toURI());
        final CsvTable srTable = SpectralResponseFunction.readSpectralResponsesFromCsv(csvFile);

        assertEquals(List.of("wavelength", "fwhm"), srTable.header());
        assertEquals(224, srTable.rows().size());
        assertEquals(List.of("418.24", "6.99561"), srTable.rows().getFirst());

        assertEquals("418.24", srTable.rows().getFirst().getFirst());
        assertEquals("6.99561", srTable.rows().getFirst().get(1));

        assertEquals(418.24f, Float.parseFloat(srTable.rows().getFirst().getFirst()), 1.E-2);
        assertEquals(6.99561f, Float.parseFloat(srTable.rows().getFirst().get(1)), 1.E-5);

        srf.setSpectralResponses(srTable);
        final List<SpectralResponseFunction.SpectralResponse> srList = srf.getSpectralResponsesList();

        assertEquals(224, srList.size());
        assertEquals(418.24f, srList.getFirst().getWvl(), 1.E-2);
        assertEquals(6.99561f, srList.getFirst().getFwhm(), 1.E-5);
        assertEquals(911.715f, srList.get(81).getWvl(), 1.E-2);
        assertEquals(10.2508f, srList.get(81).getFwhm(), 1.E-5);
        assertEquals(2445.53f, srList.get(223).getWvl(), 1.E-2);
        assertEquals(7.1581f, srList.get(223).getFwhm(), 1.E-5);
    }

    @Test
    @STTM("SNAP-4174")
    public void test_setupSrfFromCsv_prisma() throws Exception {
        SpectralResponseFunction srf = new SpectralResponseFunction("prisma");
        assertNotNull(srf.getID());
        assertEquals("prisma", srf.getID());
        assertNotNull(srf.getSpectralResponsesList());
        assertEquals(0, srf.getSpectralResponsesList().size());

        final URL resource = getClass().getResource(srf.getID() + ".csv");
        assertNotNull(resource);
        final File csvFile = new File(resource.toURI());
        final CsvTable srTable = SpectralResponseFunction.readSpectralResponsesFromCsv(csvFile);

        assertEquals(List.of("wavelength", "fwhm"), srTable.header());
        assertEquals(234, srTable.rows().size());
        assertEquals(List.of("402.5", "11.4"), srTable.rows().getFirst());

        assertEquals("402.5", srTable.rows().getFirst().getFirst());
        assertEquals("11.4", srTable.rows().getFirst().get(1));

        assertEquals(402.5f, Float.parseFloat(srTable.rows().getFirst().getFirst()), 1.E-1);
        assertEquals(11.4f, Float.parseFloat(srTable.rows().getFirst().get(1)), 1.E-1);

        srf.setSpectralResponses(srTable);
        final List<SpectralResponseFunction.SpectralResponse> srList = srf.getSpectralResponsesList();

        assertEquals(234, srList.size());
        assertEquals(402.5f, srList.getFirst().getWvl(), 1.E-1);
        assertEquals(11.4f, srList.getFirst().getFwhm(), 1.E-1);
        assertEquals(1626.8f, srList.get(128).getWvl(), 1.E-1);
        assertEquals(13.3f, srList.get(128).getFwhm(), 1.E-1);
        assertEquals(2496.9f, srList.get(233).getWvl(), 1.E-1);
        assertEquals(9.5f, srList.get(233).getFwhm(), 1.E-1);
    }
}
