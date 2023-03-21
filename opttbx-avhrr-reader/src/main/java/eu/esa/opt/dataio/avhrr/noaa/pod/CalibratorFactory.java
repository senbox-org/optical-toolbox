package eu.esa.opt.dataio.avhrr.noaa.pod;

import eu.esa.opt.dataio.avhrr.calibration.Calibrator;

import java.io.IOException;

/**
* @author Ralf Quast
*/
interface CalibratorFactory {

    Calibrator createCalibrator(int i) throws IOException;

    String getBandName();

    String getBandUnit();

    String getBandDescription();
}
