package eu.esa.opt.dataio.s3.util;

import eu.esa.opt.dataio.s3.dddb.VariableDescriptor;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.snap.core.dataio.RasterExtract;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import ucar.nc2.Variable;

public abstract class AbstractSensorContext implements SensorContext {

    @Override
    public abstract String getInversePixelGeoCodingKey();

    @Override
    public abstract String getTiePointForwardGeoCodingKey();

    @Override
    public abstract String getUsePixelGeoCodingKey();

    @Override
    public abstract String getCustomCalibrationKey();

    @Override
    public abstract double getResolutionInKm(String productType);

    @Override
    public abstract GeoLocationNames getGeoLocationNames();

    @Override
    public abstract String bandNameToKey(String bandName);

    @Override
    public abstract void handleSpecialDataRequest(RasterExtract rasterExtract, String name, Variable netCDFVariable, ProductData destBuffer);

    @Override
    public void addDescriptionAndUnit(Band band, VariableDescriptor descriptor) {
        band.setDescription(descriptor.getDescription());
        band.setUnit(descriptor.getUnits());
    }

    @Override
    public abstract void applyCalibration(Band band);

    @Override
    public abstract MetadataElement getBandDescriptionsElement(Manifest manifest);
}
