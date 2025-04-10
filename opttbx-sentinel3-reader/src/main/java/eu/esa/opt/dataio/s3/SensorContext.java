package eu.esa.opt.dataio.s3;

import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.opt.dataio.s3.util.GeoLocationNames;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;

public interface SensorContext {

    String getInversePixelGeoCodingKey();
    String getTiePointForwardGeoCodingKey();
    String getUsePixelGeoCodingKey();
    String getCustomCalibrationKey();
    String getCalibrationPatternKey();

    double getResolutionInKm(String productType);

    GeoLocationNames getGeoLocationNames();
    String bandNameToKey(String bandName);

    MetadataElement getBandDescriptionsElement(Manifest manifest);
}
