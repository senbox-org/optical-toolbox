package eu.esa.opt.dataio.s3.preferences.ui;

import eu.esa.opt.dataio.s3.util.CalibrationUtils;
import org.esa.snap.ui.ModalDialog;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

public class S3CustomCalibrationWindow extends ModalDialog {

    private final String offset;
    private final String factor;
    private final String adjustment_factor;

    private final String productType;
    private final boolean isSLSTR;
    private final Preferences preferences;

    private final HashMap<String, Double> changedOffsets = new HashMap<>();
    private final HashMap<String, Double> changedFactors = new HashMap<>();
    private final HashMap<String, Double> changedAdjustmentFactors = new HashMap<>();

    public S3CustomCalibrationWindow(Window parent, String title, int buttonMask, String helpID, Preferences preferences, CalibrationUtils.PRODUCT_TYPE productType) {
        super(parent, title, buttonMask, helpID);
        this.preferences = preferences;
        this.productType = productType.name().toLowerCase();
        this.offset = CalibrationUtils.OFFSET_PARAM;
        this.factor = CalibrationUtils.FACTOR_PARAM;
        this.adjustment_factor = CalibrationUtils.ADJUSTMENT_FACTOR_PARAM;
        this.isSLSTR = productType == CalibrationUtils.PRODUCT_TYPE.SLSTRL1B;

        initUI();
    }

    @Override
    public void onOK() {
        writeUpdatedParametersToPreferences();
        super.onOK();
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());

        JLabel noteLabel = new JLabel("<html><b>NOTE:</b> NaN values represent no change in the calibration coefficients.");
        noteLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        headerPanel.add(noteLabel, BorderLayout.NORTH);
        headerPanel.add(new JSeparator(), BorderLayout.SOUTH);

        JPanel editPane = createEditPane();
        JScrollPane scrollPane = new JScrollPane(editPane);

        final JPanel content = new JPanel(new BorderLayout());
        content.add(headerPanel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);
        content.setMinimumSize(new Dimension(200, 300));
        content.setPreferredSize(new Dimension(400, 600));

        setContent(content);
    }

    private JPanel createEditPane() {

        JPanel editPane = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 5, 2, 5);

        gbc.gridwidth = 1;
        gbc.gridy = 0;

        Map<String, int[]> bandRanges = CalibrationUtils.getBandRanges(this.productType);

        for (String key : bandRanges.keySet()) {
            int[] bandRange = bandRanges.get(key);
            for (int ii = bandRange[0]; ii <= bandRange[1]; ii++ ) {

                String bandName = CalibrationUtils.getBandName(key, ii);
                Double offsetValue = CalibrationUtils.getParameterValueFromConfig(bandName, this.offset, this.preferences, this.productType);
                Double scalingFactorValue = CalibrationUtils.getParameterValueFromConfig(bandName, this.factor, this.preferences, this.productType);

                gbc.gridx = 0;
                JLabel keyLabel = new JLabel(bandName);
                editPane.add(keyLabel, gbc);

                gbc.gridx = 1;
                JLabel offsetLabel = new JLabel("Offset:");
                editPane.add(offsetLabel, gbc);

                gbc.gridx = 2;
                JTextField offsetField = new JTextField(String.valueOf(offsetValue), 15);
                offsetField.getDocument().addDocumentListener(
                        new CalibrationDocumentListener(bandName, this.offset)
                );
                editPane.add(offsetField, gbc);

                gbc.gridx = 1;
                gbc.gridy++;
                JLabel factorLabel = new JLabel("Scaling factor:");
                editPane.add(factorLabel, gbc);

                gbc.gridx = 2;
                JTextField factorField = new JTextField(String.valueOf(scalingFactorValue), 15);
                factorField.getDocument().addDocumentListener(
                        new CalibrationDocumentListener(bandName, this.factor)
                );
                editPane.add(factorField, gbc);

                if (this.isSLSTR) {
                    gbc.gridx = 1;
                    gbc.gridy++;
                    Double adjustmentFactorValue = CalibrationUtils.getParameterValueFromConfig(bandName, this.adjustment_factor, this.preferences, this.productType);

                    JLabel adjustmentFactorLabel = new JLabel("Adjustment factor:");
                    editPane.add(adjustmentFactorLabel, gbc);

                    gbc.gridx = 2;
                    JTextField adjustmentFactorField = new JTextField(String.valueOf(adjustmentFactorValue), 15);
                    adjustmentFactorField.getDocument().addDocumentListener(
                            new CalibrationDocumentListener(bandName, this.adjustment_factor)
                    );
                    editPane.add(adjustmentFactorField, gbc);
                }

                gbc.gridy++;
            }
        }

        return editPane;
    }

    private void writeUpdatedParametersToPreferences() {
        writeSingleParamToPreferences(changedOffsets, this.offset);
        writeSingleParamToPreferences(changedFactors, this.factor);
        if (this.isSLSTR) {
            writeSingleParamToPreferences(changedAdjustmentFactors, this.adjustment_factor);
        }
    }

    private void writeSingleParamToPreferences(Map<String, Double> map, String param) {
        map.forEach((bandName, value) -> {
            String key = CalibrationUtils.getCalibrationKey(bandName, param, this.productType);
            if (!Double.isNaN(value)) {
                this.preferences.putDouble(key, value);
            } else {
                this.preferences.remove(key);
            }
        });
    }

    private class CalibrationDocumentListener implements DocumentListener {

        private final String bandName;
        private final String paramType;

        public CalibrationDocumentListener(String bandName, String paramType) {
            this.bandName = bandName;
            this.paramType = paramType;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            updateParameterValue(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateParameterValue(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            // not needed
        }

        private void updateParameterValue(DocumentEvent e) {
            double value;
            try {
                String text = e.getDocument().getText(0, e.getDocument().getLength());
                value = Double.parseDouble(text);

            } catch (NumberFormatException | BadLocationException ex) {
                value = Double.NaN;
            }
            if (value < 0) {
                value = Double.NaN;
            }

            if (paramType.equals(CalibrationUtils.OFFSET_PARAM)) {
                changedOffsets.put(bandName, value);
            } else if (paramType.equals(CalibrationUtils.FACTOR_PARAM)) {
                changedFactors.put(bandName, value);
            } else if (paramType.equals(CalibrationUtils.ADJUSTMENT_FACTOR_PARAM)) {
                changedAdjustmentFactors.put(bandName, value);
            }
        }
    }
}
