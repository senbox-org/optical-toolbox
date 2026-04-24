package eu.esa.opt.spectralnoise;

import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.ui.TargetProductSelector;
import org.esa.snap.ui.AppContext;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SpectralNoiseReductionForm extends JPanel {


    private final AppContext appContext;
    private final Map<String, Object> parameterMap;
    private final TargetProductSelector targetProductSelector;

    private final JComboBox<Product> sourceProductCombo;
    private final SpectralNoiseReductionPanel parameterPanel;


    SpectralNoiseReductionForm(AppContext appContext,
                               Map<String, Object> parameterMap,
                               TargetProductSelector targetProductSelector) {
        super(new BorderLayout(4, 4));
        this.appContext = appContext;
        this.parameterMap = parameterMap;
        this.targetProductSelector = targetProductSelector;

        sourceProductCombo = new JComboBox<>();
        sourceProductCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Product) {
                    label.setText(((Product) value).getDisplayName());
                }
                return label;
            }
        });
        sourceProductCombo.addActionListener(e -> updateBandsForSelectedSource());

        parameterPanel = new SpectralNoiseReductionPanel();

        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("I/O Parameters", createIoPanel());
        tabbedPane.addTab("Processing Parameters", parameterPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }


    Product getSourceProduct() {
        return (Product) sourceProductCombo.getSelectedItem();
    }

    void prepareShow() {
        loadSourceProducts();
        applyParameterMapToPanel();
    }

    void prepareHide() {
    }

    String validateForm() {
        if (getSourceProduct() == null) {
            return "Please select a source product.";
        }
        return parameterPanel.validateParameters();
    }

    void updateParameters() {
        parameterPanel.updateParameterMap(parameterMap);
    }

    private JPanel createIoPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Source Product:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(sourceProductCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(targetProductSelector.createDefaultPanel(), gbc);

        return panel;
    }

    private void loadSourceProducts() {
        sourceProductCombo.removeAllItems();
        final Product[] products = appContext.getProductManager().getProducts();
        for (Product product : products) {
            sourceProductCombo.addItem(product);
        }
        if (products.length > 0) {
            sourceProductCombo.setSelectedIndex(0);
        }
        updateBandsForSelectedSource();
    }

    private void updateBandsForSelectedSource() {
        final Product sourceProduct = getSourceProduct();
        if (sourceProduct == null) {
            parameterPanel.setAvailableBands(new String[0]);
            return;
        }

        final List<String> spectralBands = new ArrayList<>();
        for (Band band : sourceProduct.getBands()) {
            if (band.getSpectralWavelength() > 0.0) {
                spectralBands.add(band.getName());
            }
        }

        final Object selectedBands = parameterMap.get("sourceBands");
        parameterPanel.setAvailableBands(
                spectralBands.toArray(new String[0]),
                selectedBands instanceof String[] ? (String[]) selectedBands : null
        );
    }

    private void applyParameterMapToPanel() {
        parameterPanel.applyParameterMap(parameterMap);
    }
}
