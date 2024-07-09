package eu.esa.opt.s2msi.resampler;

import com.bc.ceres.swing.binding.BindingContext;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.graphbuilder.gpf.ui.BaseOperatorUI;
import org.esa.snap.graphbuilder.gpf.ui.UIValidation;
import org.esa.snap.ui.AppContext;

import javax.swing.*;
import java.util.Map;

public class S2ResamplingUI extends BaseOperatorUI {

    private S2ResamplingPanel baseUI;

    @Override
    public JComponent CreateOpTab(String operatorName, Map<String, Object> parameterMap, AppContext appContext) {
        initializeOperatorUI(operatorName, parameterMap);
        this.baseUI = new S2ResamplingPanel(operatorName,
                                            this.propertySet,
                                            new BindingContext(this.propertySet),
                                            this::getCurrentProduct);
        return this.baseUI.createPanel();
    }

    @Override
    public void initParameters() {
        updateParameters();
    }

    @Override
    public UIValidation validateParameters() {
        return this.baseUI.validateParameters() ?
               new UIValidation(UIValidation.State.OK, "") :
               new UIValidation(UIValidation.State.WARNING, "Some values are wrong");
    }

    @Override
    public void updateParameters() {
        this.baseUI.reactOnChange();
    }

    private Product getCurrentProduct() {
        return sourceProducts != null && sourceProducts.length > 0 ? sourceProducts[0] : null;
    }
}
