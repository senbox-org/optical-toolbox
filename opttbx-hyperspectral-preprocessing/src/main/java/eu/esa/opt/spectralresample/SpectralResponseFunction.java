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
    private float refWvl;
    private List<SpectralResponse> spectralResponsesList;


    /**
     * Constructor
     *
     */
    public SpectralResponseFunction() {
        spectralResponsesList = new ArrayList<>();
    }

    /**
     * Constructor
     *
     * @param ID - Identifier (could be sensor name, tbd)
     */
    public SpectralResponseFunction(String ID) {
        this.ID = ID;

        spectralResponsesList = new ArrayList<>();
    }

    public static List<SpectralResponse> getConvolvedSpectralResponses(List<SpectralResponse> fwhmSpectralResponses) {
        // TODO: implement following Enmap Python code:
        //       - https://github.com/EnMAP-Box/enmap-box/blob/main/enmapboxprocessing/algorithm/spectralresamplingbyresponsefunctionconvolutionalgorithmbase.py
        //         --> function processAlgorithm, with responses from
        //             fwhmSpectralResponses <--> responses
        //             convolvedSpectralResponses <--> responses2

        List<SpectralResponse> convolvedSpectralResponses;
        convolvedSpectralResponses = fwhmSpectralResponses;  // remove later!!

        return convolvedSpectralResponses;
    }


    /**
     * Reads spectral responses from a csv file. Makes use of classes/methods from the snap-spectrallibrary package.
     *
     * @param csvFile - File
     * @return - CsvTable
     */
    public static CsvTable readSpectralResponsesFromCsv(File csvFile) throws IOException {
        return CsvUtils.read(csvFile.toPath());
    }

    /**
     * Provides a list of fully defined Spectral Response Functions, each of them defined as pairs of (wvl, weight)
     * around a given reference wavelength. A fully defined SRF is retrieved from an input pair (refWvl, FWHM).
     * See more details at <a href="https://en.wikipedia.org/wiki/Full_width_at_half_maximum">...</a>
     *
     * @param fwhmSrfList -
     * @return -
     */
    public static List<SpectralResponseFunction> getFullyDefinedSrf(List<SpectralResponse> fwhmSrfList) {

        List<SpectralResponseFunction> fullSrfList = new ArrayList<>();

        fwhmSrfList.iterator().forEachRemaining(sr -> {
            final float x0 = sr.getWvl();
            final float fwhm = sr.getWeight();
            final float sigma =  fwhm / 2.355f;
            final float a = 2.0f * sigma * sigma;
            final float b = (float) (sigma * Math.sqrt(2.0 * Math.PI));
            List<SpectralResponse> srList = new ArrayList<>();
            float maxWeight = Float.MIN_VALUE;
            final int left = (int) (x0 - 3.0 * sigma);
            final int right = (int) (x0 + 3.0 * sigma);
            for (int x = left; x < right + 2; x++) {
                final float c = -1.0f * (x - x0) * (x - x0);
                final float weight = (float) (Math.exp(c / a) / b);
                if (weight > maxWeight) maxWeight = weight;
                srList.add(new SpectralResponse(x, weight));
            }
            for (SpectralResponse spectralResponse : srList) {
                spectralResponse.setWeight(spectralResponse.getWeight() / maxWeight);
            }

            SpectralResponseFunction fullSrf = new SpectralResponseFunction();
            fullSrf.setRefWvl(x0);
            fullSrf.setSpectralResponsesList(srList);
            fullSrfList.add(fullSrf);
        });

        return fullSrfList;
    }

    /**
     * Fills spectral responses list with spectral responses from CsvTable
     *
     * @param csvTable -
     */
    public void setSpectralResponsesList(CsvTable csvTable) {
        Assert.notNull(csvTable);
        List<SpectralResponse> fwhmpSectralResponsesList = new ArrayList<>();
        csvTable.rows().iterator().forEachRemaining(row -> {
            SpectralResponse sr = new SpectralResponse();
            sr.setWvl(Float.parseFloat(row.get(0)));
            sr.setWeight(Float.parseFloat(row.get(1)));

            fwhmpSectralResponsesList.add(sr);
        });

        spectralResponsesList = getConvolvedSpectralResponses(fwhmpSectralResponsesList);
    }

    /**
     * Reads spectral responses from a GeoJson file.
     *
     * @param geoJsonFile -
     */
    public void readSpectralResponsesFromGeoJson(File geoJsonFile) {
        // TODO
    }

    public String getID() {
        return ID;
    }

    public List<SpectralResponse> getSpectralResponsesList() {
        return spectralResponsesList;
    }


    public float getRefWvl() {
        return refWvl;
    }

    public void setRefWvl(float refWvl) {
        this.refWvl = refWvl;
    }

    public void setSpectralResponsesList(List<SpectralResponse> spectralResponsesList) {
        this.spectralResponsesList = spectralResponsesList;
    }
}
