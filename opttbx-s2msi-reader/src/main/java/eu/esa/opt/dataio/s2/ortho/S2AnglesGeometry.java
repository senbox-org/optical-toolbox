package eu.esa.opt.dataio.s2.ortho;

import eu.esa.opt.dataio.s2.S2BandAnglesGrid;
import eu.esa.opt.dataio.s2.S2BandAnglesGridByDetector;

/**
 * Created by obarrile on 10/10/2018.
 */
public interface S2AnglesGeometry {
    S2BandAnglesGridByDetector[] getViewingIncidenceAnglesGrids(int bandId, int detectorId);
    S2BandAnglesGrid[] getSunAnglesGrid();
}
