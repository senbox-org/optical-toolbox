package eu.esa.opt.dataio.s3.util;

import eu.esa.opt.dataio.s3.dddb.VariableDescriptor;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.snap.core.dataio.RasterExtract;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;
import ucar.nc2.Variable;

public interface SensorContext {

    String getInversePixelGeoCodingKey();
    String getTiePointForwardGeoCodingKey();
    String getUsePixelGeoCodingKey();
    String getCustomCalibrationKey();

    double getResolutionInKm(String productType);

    GeoLocationNames getGeoLocationNames();
    String bandNameToKey(String bandName);

    void addDescriptionAndUnit(Band band, VariableDescriptor descriptor);
    void applyCalibration(Band band);

    void handleSpecialDataRequest(RasterExtract rasterExtract, String name, Variable netCDFVariable);

    MetadataElement getBandDescriptionsElement(Manifest manifest);
}
