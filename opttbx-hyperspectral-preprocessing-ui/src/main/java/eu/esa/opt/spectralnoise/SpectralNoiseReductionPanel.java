package eu.esa.opt.spectralnoise;

import org.esa.snap.rcp.spectrallibrary.ui.noise.SpectralNoiseSettingsPanel;
import org.esa.snap.speclib.util.noise.SpectralNoiseKernelFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SpectralNoiseReductionPanel extends JPanel {


    private final DefaultListModel<String> sourceBandListModel = new DefaultListModel<>();
    private final JList<String> sourceBandList = new JList<>(sourceBandListModel);

    SpectralNoiseSettingsPanel settingsPanel = new SpectralNoiseSettingsPanel();

//    private final JComboBox<String> filterMethodCombo = new JComboBox<>(new String[]{
//            SpectralNoiseKernelFactory.FILTER_SG,
//            SpectralNoiseKernelFactory.FILTER_GAUSSIAN,
//            SpectralNoiseKernelFactory.FILTER_BOX
//    });
//
//    private final JSpinner kernelSizeSpinner = new JSpinner(new SpinnerNumberModel(11, 1, 9999, 1));
//    private final JSpinner gaussianSigmaSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.000001, 9999.0, 0.1));
//    private final JSpinner sgPolynomialOrderSpinner = new JSpinner(new SpinnerNumberModel(3, 0, 9999, 1));
//
//    private final JLabel gaussianSigmaLabel = new JLabel("Gaussian Sigma:");
//    private final JLabel sgPolynomialOrderLabel = new JLabel("Polynomial Order:");


    SpectralNoiseReductionPanel() {
        super(new GridBagLayout());

        sourceBandList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        final JScrollPane bandScrollPane = new JScrollPane(sourceBandList);
        bandScrollPane.setPreferredSize(new Dimension(250, 120));

//        filterMethodCombo.addActionListener(e -> updateFilterSpecificControls());

        final GridBagConstraints gbc = createConstraints();
        addRow(this, gbc, 0, new JLabel("Source Bands:"), bandScrollPane);
//        addRow(this, gbc, 1, new JLabel("Filter Method:"), filterMethodCombo);
//        addRow(this, gbc, 2, new JLabel("Kernel Size:"), kernelSizeSpinner);
//        addRow(this, gbc, 3, gaussianSigmaLabel, gaussianSigmaSpinner);
//        addRow(this, gbc, 4, sgPolynomialOrderLabel, sgPolynomialOrderSpinner);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(settingsPanel, gbc);

//        updateFilterSpecificControls();
    }

    void setAvailableBands(String[] bandNames) {
        setAvailableBands(bandNames, null);
    }

    void setAvailableBands(String[] bandNames, String[] selectedBands) {
        sourceBandListModel.clear();
        for (String bandName : bandNames) {
            sourceBandListModel.addElement(bandName);
        }
        sourceBandList.setVisibleRowCount(Math.min(8, Math.max(1, sourceBandListModel.size())));
        setSelectedBands(selectedBands);
    }

    void applyParameterMap(Map<String, Object> paramMap) {
//        final Object filterValue = paramMap.get("filterType");
//        filterMethodCombo.setSelectedItem(filterValue instanceof String ? filterValue : SpectralNoiseKernelFactory.FILTER_SG);
//
//        final Object kernelSizeValue = paramMap.get("kernelSize");
//        kernelSizeSpinner.setValue(kernelSizeValue instanceof Number ? ((Number) kernelSizeValue).intValue() : 11);
//
//        final Object gaussianSigmaValue = paramMap.get("gaussianSigma");
//        gaussianSigmaSpinner.setValue(gaussianSigmaValue instanceof Number ? ((Number) gaussianSigmaValue).doubleValue() : 1.0);
//
//        final Object sgPolynomialOrderValue = paramMap.get("sgPolynomialOrder");
//        sgPolynomialOrderSpinner.setValue(sgPolynomialOrderValue instanceof Number ? ((Number) sgPolynomialOrderValue).intValue() : 3);

        settingsPanel.applyParameterMap(paramMap);
        final Object sourceBandsValue = paramMap.get("sourceBands");
        setSelectedBands(sourceBandsValue instanceof String[] ? (String[]) sourceBandsValue : null);



//        updateFilterSpecificControls();
    }

    void updateParameterMap(Map<String, Object> paramMap) {
        paramMap.put("sourceBands", getSelectedBands());
//        paramMap.put("filterType", filterMethodCombo.getSelectedItem());
//        paramMap.put("kernelSize", ((Number) kernelSizeSpinner.getValue()).intValue());
//        paramMap.put("gaussianSigma", ((Number) gaussianSigmaSpinner.getValue()).doubleValue());
//        paramMap.put("sgPolynomialOrder", ((Number) sgPolynomialOrderSpinner.getValue()).intValue());
        settingsPanel.updateParameterMap(paramMap);
    }

    String validateParameters() {
//        final int kernelSize = ((Number) kernelSizeSpinner.getValue()).intValue();
//        final double gaussianSigma = ((Number) gaussianSigmaSpinner.getValue()).doubleValue();
//        final int sgPolynomialOrder = ((Number) sgPolynomialOrderSpinner.getValue()).intValue();
//        final String filterMethod = (String) filterMethodCombo.getSelectedItem();
//
//        final SpectralNoiseKernelFactory kernelParams = new SpectralNoiseKernelFactory(filterMethod, kernelSize, gaussianSigma, sgPolynomialOrder);
//        try {
//            kernelParams.validateFilterParameters();
//        } catch (IllegalArgumentException e) {
//            return e.getMessage();
//        }
//        return null;
        return settingsPanel.validateParameters();
    }

//    private void updateFilterSpecificControls() {
//        final String filterMethod = (String) filterMethodCombo.getSelectedItem();
//        final boolean gaussian = SpectralNoiseKernelFactory.FILTER_GAUSSIAN.equals(filterMethod);
//        final boolean sg = SpectralNoiseKernelFactory.FILTER_SG.equals(filterMethod);
//
//        gaussianSigmaLabel.setEnabled(gaussian);
//        gaussianSigmaSpinner.setEnabled(gaussian);
//
//        sgPolynomialOrderLabel.setEnabled(sg);
//        sgPolynomialOrderSpinner.setEnabled(sg);
//    }

    private String[] getSelectedBands() {
        final List<String> selectedValues = sourceBandList.getSelectedValuesList();
        return selectedValues.toArray(new String[0]);
    }

    private void setSelectedBands(String[] selectedBands) {
        sourceBandList.clearSelection();
        if (selectedBands == null || selectedBands.length == 0) {
            return;
        }

        final List<Integer> indices = new ArrayList<>();
        final ListModel<String> model = sourceBandList.getModel();
        for (String selectedBand : selectedBands) {
            for (int i = 0; i < model.getSize(); i++) {
                if (selectedBand.equals(model.getElementAt(i))) {
                    indices.add(i);
                    break;
                }
            }
        }
        sourceBandList.setSelectedIndices(indices.stream().mapToInt(Integer::intValue).toArray());
    }

    private static GridBagConstraints createConstraints() {
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        return gbc;
    }

    private static void addRow(JPanel panel,
                               GridBagConstraints gbc,
                               int row,
                               JComponent label,
                               JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }
}
