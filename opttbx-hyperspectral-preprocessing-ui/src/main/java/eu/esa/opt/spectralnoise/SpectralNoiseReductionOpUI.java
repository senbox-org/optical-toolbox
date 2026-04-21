package eu.esa.opt.spectralnoise;

import org.esa.snap.graphbuilder.gpf.ui.BaseOperatorUI;
import org.esa.snap.graphbuilder.gpf.ui.UIValidation;
import org.esa.snap.ui.AppContext;

import javax.swing.*;
import java.util.Map;


public class SpectralNoiseReductionOpUI extends BaseOperatorUI {


    private SpectralNoiseReductionPanel panel;


    @Override
    public JComponent CreateOpTab(String operatorName, Map<String, Object> parameterMap, AppContext appContext) {
        initializeOperatorUI(operatorName, parameterMap);

        panel = new SpectralNoiseReductionPanel();
        panel.setAvailableBands(getBandNames(), paramMap.get("sourceBands") instanceof String[]
                ? (String[]) paramMap.get("sourceBands")
                : null);
        panel.applyParameterMap(paramMap);

        return new JScrollPane(panel);
    }

    @Override
    public void initParameters() {
        if (panel == null) {
            return;
        }
        panel.setAvailableBands(getBandNames(), paramMap.get("sourceBands") instanceof String[]
                ? (String[]) paramMap.get("sourceBands")
                : null);
        panel.applyParameterMap(paramMap);
    }

    @Override
    public UIValidation validateParameters() {
        final String error = panel.validateParameters();
        if (error != null) {
            return new UIValidation(UIValidation.State.ERROR, error);
        }
        return new UIValidation(UIValidation.State.OK, "");
    }

    @Override
    public void updateParameters() {
        panel.updateParameterMap(paramMap);
    }

}
