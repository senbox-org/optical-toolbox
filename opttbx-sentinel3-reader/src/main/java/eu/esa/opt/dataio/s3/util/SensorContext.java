package eu.esa.opt.dataio.s3.util;

import eu.esa.opt.dataio.s3.dddb.VariableDescriptor;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.opt.dataio.s3.olci.ReaderContext;
import eu.esa.snap.core.dataio.RasterExtract;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
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

    void setReaderContext(ReaderContext context);

    MetadataElement getBandDescriptionsElement(Manifest manifest);
}
