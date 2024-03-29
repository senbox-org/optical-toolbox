package eu.esa.opt.s2msi.resampler;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import com.bc.ceres.swing.selection.SelectionChangeListener;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.descriptor.OperatorDescriptor;
import org.esa.snap.core.gpf.ui.DefaultIOParametersPanel;
import org.esa.snap.core.gpf.ui.DefaultSingleTargetProductDialog;
import org.esa.snap.core.gpf.ui.SourceProductSelector;
import org.esa.snap.ui.AppContext;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class S2ResamplingDialog extends DefaultSingleTargetProductDialog {

    private final Field bandsField;

    public S2ResamplingDialog(String operatorName, AppContext appContext, String title, String helpID) {
        this(operatorName, appContext, title, helpID, true);
    }

    public S2ResamplingDialog(String operatorName, AppContext appContext, String title, String helpID, boolean targetProductSelectorDisplay) {
        super(operatorName, appContext, title, helpID, targetProductSelectorDisplay);

        OperatorDescriptor descriptor = GPF.getDefaultInstance().getOperatorSpiRegistry().getOperatorSpi(operatorName).getOperatorDescriptor();
        bandsField = Arrays.stream(descriptor.getOperatorClass().getDeclaredFields())
                           .filter(f -> f.getAnnotation(Parameter.class) != null && f.getName().equals("bands"))
                           .findFirst().get();
        DefaultIOParametersPanel ioParametersPanel = getDefaultIOParametersPanel();

        List<SourceProductSelector> sourceProductSelectorList = ioParametersPanel.getSourceProductSelectorList();
        if (!sourceProductSelectorList.isEmpty()) {
            SelectionChangeListener listener = new SelectionChangeListener() {
                public void selectionChanged(SelectionChangeEvent event) {
                    processSelectedProduct();
                }

                public void selectionContextChanged(SelectionChangeEvent event) {
                }
            };
            sourceProductSelectorList.get(0).addSelectionChangeListener(listener);
        }

        BindingContext bindingContext = getBindingContext();
        PropertySet propertySet = bindingContext.getPropertySet();
        final Property prop = propertySet.getProperty(this.bandsField.getName());
        prop.addPropertyChangeListener(evt-> { });
    }

    @Override
    public int show() {
        int result = super.show();
        processSelectedProduct();
        return result;
    }

    @Override
    public void hide() {
        super.hide();
    }

    /**
     * Returns the selected product.
     *
     * @return the selected product
     */
    private Product getSelectedProduct() {
        DefaultIOParametersPanel ioParametersPanel = getDefaultIOParametersPanel();
        List<SourceProductSelector> sourceProductSelectorList = ioParametersPanel.getSourceProductSelectorList();
        return sourceProductSelectorList.get(0).getSelectedProduct();
    }

    private void processSelectedProduct() {
        Product selectedProduct = getSelectedProduct();
        if (selectedProduct != null) {
            BindingContext bindingContext = getBindingContext();
            PropertySet propertySet = bindingContext.getPropertySet();
            propertySet.setDefaultValues();
            propertySet.setValue(this.bandsField.getName(), null);
        }
    }

    private void setEnabled(String propertyName, boolean value) {
        if (this.getJDialog().isVisible()) {
            BindingContext bindingContext = getBindingContext();
            bindingContext.setComponentsEnabled(propertyName, value);
        }
    }
}
