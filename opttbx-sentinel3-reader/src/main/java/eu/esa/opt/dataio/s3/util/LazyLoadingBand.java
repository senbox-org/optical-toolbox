package eu.esa.opt.dataio.s3.util;

import eu.esa.opt.dataio.s3.dddb.VariableDescriptor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

public class LazyLoadingBand extends Band {

    //private String description;
    //private String unit;
    // private String rawDataType; // @todo check if we can make this type-safe as early as possible
    //private int rasterWidth;
    //private int rasterHeight;
    // private String validPixelExpression;
    //private boolean noDataValueUsed;
    //private float noDataValue; // @todo or double?
    // private float spectralWavelength; // @todo or double?
    // private float spectralBandwidth; // @todo or double?
    private String ancillaryVariables;
    private String ancillaryRelations;
    private boolean initalized;

    private final VariableDescriptor descriptor;

    public LazyLoadingBand(String name, int dataType, int width, int height, VariableDescriptor descriptor) {
        super(name, dataType, width, height);
        this.descriptor = descriptor;
        initalized = false;
    }

    // @todo 1 tb must use the reader based netCdf file caching 2024-12-20
    void initialize(Variable variable) {
        setDescription(variable.getFullName());
        setUnit(variable.getUnitsString());
        setValidPixelExpression(descriptor.getValidExpression());

        final Attribute attribute = variable.findAttribute("_FillValue");
        if (attribute != null) {
            setNoDataValue(attribute.getNumericValue().doubleValue());
            setNoDataValueUsed(true);
        }
        initalized = true;
    }
}
