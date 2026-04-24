package eu.esa.opt.spectralnoise;

import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.rcp.actions.AbstractSnapAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;


@ActionID(category = "Processing", id = "eu.esa.opt.spectralnoise.SpectralNoiseReductionAction")
@ActionRegistration(displayName = "#CTL_SpectralNoiseReductionAction_Text")
@ActionReference(path = "Menu/Optical/Preprocessing/Hyperspectral", position = 10)
@NbBundle.Messages("CTL_SpectralNoiseReductionAction_Text=Spectral Noise Reduction")
public class SpectralNoiseReductionAction extends AbstractSnapAction {


    private static final String HELP_ID = "spectralNoiseReductionTool";


    public SpectralNoiseReductionAction() {
        putValue(Action.SHORT_DESCRIPTION, "Reduce spectral noise in hyperspectral data.");
        setHelpId(HELP_ID);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final OperatorMetadata metadata = SpectralNoiseReductionOp.class.getAnnotation(OperatorMetadata.class);
        final SpectralNoiseReductionDialog dialog = new SpectralNoiseReductionDialog(
                metadata.alias(),
                getAppContext(),
                "Spectral Noise Reduction",
                getHelpId()
        );
        dialog.getJDialog().pack();
        dialog.show();
    }
}
