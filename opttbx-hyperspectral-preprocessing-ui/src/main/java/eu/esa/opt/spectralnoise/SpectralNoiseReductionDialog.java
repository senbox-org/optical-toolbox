package eu.esa.opt.spectralnoise;

import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.ui.OperatorMenu;
import org.esa.snap.core.gpf.ui.OperatorParameterSupport;
import org.esa.snap.core.gpf.ui.SingleTargetProductDialog;
import org.esa.snap.ui.AppContext;


public class SpectralNoiseReductionDialog extends SingleTargetProductDialog {


    private final String operatorAlias;
    private final OperatorParameterSupport parameterSupport;
    private final SpectralNoiseReductionForm form;


    SpectralNoiseReductionDialog(String operatorAlias, AppContext appContext, String title, String helpId) {
        super(appContext, title, ID_APPLY_CLOSE, helpId);
        this.operatorAlias = operatorAlias;

        final OperatorSpi operatorSpi = GPF.getDefaultInstance()
                .getOperatorSpiRegistry()
                .getOperatorSpi(operatorAlias);

        parameterSupport = new OperatorParameterSupport(operatorSpi.getOperatorDescriptor());

        final OperatorMenu operatorMenu = new OperatorMenu(
                getJDialog(),
                operatorSpi.getOperatorDescriptor(),
                parameterSupport,
                appContext,
                helpId
        );
        getJDialog().setJMenuBar(operatorMenu.createDefaultMenu());

        form = new SpectralNoiseReductionForm(appContext, parameterSupport.getParameterMap(), getTargetProductSelector());
    }


    @Override
    protected Product createTargetProduct() {
        form.updateParameters();
        return GPF.createProduct(operatorAlias, parameterSupport.getParameterMap(), form.getSourceProduct());
    }

    @Override
    protected void onApply() {
        final String validationError = form.validateForm();
        if (validationError != null) {
            showErrorDialog(validationError);
            return;
        }
        super.onApply();
    }

    @Override
    public int show() {
        form.prepareShow();
        setContent(form);
        return super.show();
    }

    @Override
    public void hide() {
        form.prepareHide();
        super.hide();
    }
}
