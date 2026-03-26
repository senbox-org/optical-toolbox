package eu.esa.opt.spectralresample;

import com.bc.ceres.core.Assert;
import org.esa.snap.speclib.io.csv.util.CsvTable;
import org.esa.snap.speclib.io.csv.util.CsvUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Class representing a spectral response function, to be used e.g. for Spectral Resampling
 *
 * @author olafd
 */
public class SpectralResponseFunction {

    private String ID;
    private String wvlUnit;
    private String fwhmUnit;
    private List<SpectralResponse> spectralResponsesList;


    /**
     * Constructor
     *
     * @param ID - Identifier (could be sensor name, tbd)
     */
    public SpectralResponseFunction(String ID) {
        this.ID = ID;
        this.wvlUnit = "nm";
        this.fwhmUnit = "nm";

        spectralResponsesList = new ArrayList<SpectralResponse>();
    }

    /**
     * Constructor
     *
     * @param ID - Identifier (could be sensor name, tbd)
     * @param wvlUnit - wavelength unit
     * @param fwhmUnit - full width of half maximum unit
     */
    public SpectralResponseFunction(String ID, String wvlUnit, String fwhmUnit) {
        this.ID = ID;
        this.wvlUnit = wvlUnit;
        this.fwhmUnit = fwhmUnit;

        spectralResponsesList = new ArrayList<SpectralResponse>();
    }

    /**
     * Reads spectral responses from a csv file. Makes use of classes/methods from the snap-spectrallibrary package.
     *
     * @param csvFile - File
     * @return - CsvTable
     * @throws IOException
     */
    public static CsvTable readSpectralResponsesFromCsv(File csvFile) throws IOException {
        return CsvUtils.read(csvFile.toPath());
    }

    /**
     * Fills spectral responses list with spectral responses from CsvTable
     *
     * @param csvTable
     * @throws IOException
     */
    public void setSpectralResponses(CsvTable csvTable) throws IOException {
        Assert.notNull(csvTable);
        csvTable.rows().iterator().forEachRemaining(row -> {
            SpectralResponse sr = new SpectralResponse();
            sr.setWvl(Float.parseFloat(row.get(0)));
            sr.setFwhm(Float.parseFloat(row.get(1)));

            spectralResponsesList.add(sr);
        });
    }

    /**
     * Reads spectral responses from a GeoJson file.
     *
     * @param geoJsonFile
     * @throws IOException
     */
    public void readSpectralResponsesFromGeoJson(File geoJsonFile) throws IOException {
        // TODO
    }

    public String getID() {
        return ID;
    }

    public List<SpectralResponse> getSpectralResponsesList() {
        return spectralResponsesList;
    }

    /**
     * Class holding a spectral response function value of wavelength/FWHM.
     * See https://en.wikipedia.org/wiki/Full_width_at_half_maximum for more details.
     *
     */
    class SpectralResponse {
        float wvl;
        float fwhm;

        public float getWvl() {
            return wvl;
        }

        public void setWvl(float wvl) {
            this.wvl = wvl;
        }

        public float getFwhm() {
            return fwhm;
        }

        public void setFwhm(float fwhm) {
            this.fwhm = fwhm;
        }
    }
}
