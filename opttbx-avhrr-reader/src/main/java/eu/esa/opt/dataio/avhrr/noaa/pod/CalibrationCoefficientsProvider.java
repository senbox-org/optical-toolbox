package eu.esa.opt.dataio.avhrr.noaa.pod;

import com.bc.ceres.binio.SequenceData;

import java.io.IOException;

/**
 * @author Ralf Quast
 */
interface CalibrationCoefficientsProvider {

    SequenceData getCalibrationCoefficients(int recordIndex) throws IOException;

    double getSlopeScaleFactor();

    double getInterceptScaleFactor();
}
