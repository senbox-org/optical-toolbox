package eu.esa.opt.s2msi.resampler;

import org.esa.snap.core.gpf.ui.DefaultOperatorAction;
import org.esa.snap.ui.ModelessDialog;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class S2ResamplingOperatorAction extends DefaultOperatorAction {

    private static final Set<String> KNOWN_KEYS = new HashSet<>(Arrays.asList("displayName", "operatorName", "dialogTitle", "helpId", "targetProductNameSuffix"));

    public S2ResamplingOperatorAction() {
        super();
    }

    public static S2ResamplingOperatorAction create(Map<String, Object> properties) {
        S2ResamplingOperatorAction action = new S2ResamplingOperatorAction();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (KNOWN_KEYS.contains(entry.getKey())) {
                action.putValue(entry.getKey(), entry.getValue());
            }
        }
        return action;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        ModelessDialog dialog = createOperatorDialog();
        dialog.show();
    }

    @Override
    protected ModelessDialog createOperatorDialog() {
        S2ResamplingDialog productDialog = new S2ResamplingDialog(getOperatorName(), getAppContext(), getDialogTitle(), getHelpId());
        if (getTargetProductNameSuffix() != null) {
            productDialog.setTargetProductNameSuffix(getTargetProductNameSuffix());
        }
        return productDialog;
    }
}

