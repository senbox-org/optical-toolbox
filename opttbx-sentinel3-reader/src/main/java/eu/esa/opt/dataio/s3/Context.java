package eu.esa.opt.dataio.s3;

import eu.esa.opt.dataio.s3.util.GeoLocationNames;

public interface Context {

    String getInversePixelGeoCodingKey();
    String getTiePointForwardGeoCodingKey();
    String getUsePixelGeoCodingKey();
    String getCustomCalibrationKey();
    String getCalibrationPatternKey();
    GeoLocationNames getGeoLocationNames();
    String bandNameToKey(String bandName);
}
