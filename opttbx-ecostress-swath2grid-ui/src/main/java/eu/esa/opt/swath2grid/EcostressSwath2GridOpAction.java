package eu.esa.opt.swath2grid;

import org.esa.snap.core.gpf.ui.DefaultOperatorAction;
import org.esa.snap.ui.ModelessDialog;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class EcostressSwath2GridOpAction extends DefaultOperatorAction {

    private static final Set<String> KNOWN_KEYS = new HashSet<>(Arrays.asList("displayName", "operatorName", "dialogTitle", "helpId", "targetProductNameSuffix"));

    public static EcostressSwath2GridOpAction create(Map<String, Object> properties) {
        final EcostressSwath2GridOpAction action = new EcostressSwath2GridOpAction();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (KNOWN_KEYS.contains(entry.getKey())) {
                action.putValue(entry.getKey(), entry.getValue());
            }
        }
        return action;
    }

    public EcostressSwath2GridOpAction() {
        super();
    }

    @Override
    protected ModelessDialog createOperatorDialog() {
        return new EcostressSwath2GridOpDialog(getOperatorName(), getAppContext(), getDialogTitle(), getHelpId());
    }
}
