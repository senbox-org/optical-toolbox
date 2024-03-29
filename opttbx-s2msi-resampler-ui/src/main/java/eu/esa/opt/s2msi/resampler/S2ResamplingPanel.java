package eu.esa.opt.s2msi.resampler;

import com.bc.ceres.binding.*;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.binding.PropertyPane;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.descriptor.OperatorDescriptor;
import org.esa.snap.core.gpf.internal.RasterDataNodeValues;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.Callable;

class S2ResamplingPanel {
    private final OperatorDescriptor operatorDescriptor;
    private final PropertySet propertySet;

    private Field bandsField;
    private final BindingContext bindingContext;
    private Product currentProduct;
    private final JScrollPane operatorPanel;
    private final Callable<Product> sourceProductAccessor;

    S2ResamplingPanel(String operatorName, PropertySet propertySet, BindingContext bindingContext, Callable<Product> productAccessor) {
        if (productAccessor == null) {
            throw new IllegalArgumentException("The accessor for fetching source products must not be null");
        }
        OperatorSpi operatorSpi = GPF.getDefaultInstance().getOperatorSpiRegistry().getOperatorSpi(operatorName);
        if (operatorSpi == null) {
            throw new IllegalArgumentException("No SPI found for operator name '" + operatorName + "'");
        }
        this.operatorDescriptor = operatorSpi.getOperatorDescriptor();
        this.propertySet = propertySet;
        this.bindingContext = bindingContext == null ? new BindingContext(propertySet) : bindingContext;
        this.sourceProductAccessor = productAccessor;
        PropertyPane parametersPane = new PropertyPane(this.bindingContext);
        this.operatorPanel = new JScrollPane(parametersPane.createPanel());
    }

    JComponent createPanel() {
        this.bandsField = Arrays.stream(operatorDescriptor.getOperatorClass().getDeclaredFields())
                                .filter(f -> f.getAnnotation(Parameter.class) != null && f.getName().equals("bands"))
                                .findFirst().get();
        final Property property = this.propertySet.getProperty(bandsField.getName());
        property.addPropertyChangeListener(evt -> { });
        return this.operatorPanel;
    }

    boolean validateParameters() {
        return true;
    }

    /**
     * Sets the incidence angle and the quantification value according to the selected product.
     */
    void reactOnChange() {
        if (isInputProductChanged()) {
            if (this.currentProduct != null) {
                PropertySet propertySet = this.bindingContext.getPropertySet();
                Property property = propertySet.getProperty(this.bandsField.getName());
                updateValueSet(property, this.currentProduct);
                if (property.getValue() == null) {
                    try {
                        property.setValue(null);
                    } catch (ValidationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    BindingContext getBindingContext() { return this.bindingContext; }

    private boolean isInputProductChanged() {
        Product sourceProduct = getSourceProduct();
        if (sourceProduct != this.currentProduct) {
            this.currentProduct = sourceProduct;
            return true;
        } else {
            return false;
        }
    }

    private Product getSourceProduct() {
        try {
            return this.sourceProductAccessor.call();
        } catch (Exception e) {
            return null;
        }
    }

    private void updateValueSet(Property property, Product product) {
        String[] values = new String[0];
        PropertyDescriptor propertyDescriptor = property.getDescriptor();
        if (product != null) {
            Object object = propertyDescriptor.getAttribute(RasterDataNodeValues.ATTRIBUTE_NAME);
            if (object != null) {
                @SuppressWarnings("unchecked")
                Class<? extends RasterDataNode> rasterDataNodeType = (Class<? extends RasterDataNode>) object;
                boolean includeEmptyValue = !propertyDescriptor.isNotNull() && !propertyDescriptor.isNotEmpty() &&
                        !propertyDescriptor.getType().isArray();
                values = RasterDataNodeValues.getNames(product, rasterDataNodeType, includeEmptyValue);
            }
        }
        propertyDescriptor.setValueSet(new ValueSet(values));
    }

    private void setEnabled(String propertyName, boolean value) {
        this.bindingContext.setComponentsEnabled(propertyName, value);
    }
}
