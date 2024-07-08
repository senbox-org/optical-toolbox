package eu.esa.opt.s2msi.resampler;

import com.bc.ceres.binding.*;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.binding.PropertyPane;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.descriptor.OperatorDescriptor;
import org.esa.snap.core.gpf.internal.RasterDataNodeValues;
import org.esa.snap.ui.GridBagUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

class S2ResamplingPanel {

    private static final Font SMALL_PLAIN_FONT = new Font("SansSerif", Font.PLAIN, 10);
    private static final Font SMALL_ITALIC_FONT = SMALL_PLAIN_FONT.deriveFont(Font.ITALIC);

    private final OperatorDescriptor operatorDescriptor;
    private final PropertySet propertySet;

    private Field bandsField;
    private Field masksField;
    private final BindingContext bindingContext;
    private Product currentProduct;
    private final JScrollPane operatorPanel;
    private BandPane bandsPane;
    private BandPane masksPane;
    private final Callable<Product> sourceProductAccessor;

    S2ResamplingPanel(String operatorName, PropertySet propertySet, BindingContext bindingContext, Callable<Product> productAccessor) {
        if (productAccessor == null) {
            throw new IllegalArgumentException("The accessor for fetching source products must not be null");
        }
        OperatorSpi operatorSpi = GPF.getDefaultInstance().getOperatorSpiRegistry().getOperatorSpi(operatorName);
        if (operatorSpi == null) {
            throw new IllegalArgumentException("No SPI found for operator name '" + operatorName + "'");
        }
        this.operatorDescriptor = operatorSpi.getOperatorDescriptor();
        this.propertySet = propertySet;
        this.bindingContext = bindingContext == null ? new BindingContext(propertySet) : bindingContext;
        this.sourceProductAccessor = productAccessor;
        PropertyPane parametersPane = new PropertyPane(this.bindingContext);
        this.bandsPane = new BandPane(getSourceProduct() == null ? new Band[0] : getSourceProduct().getBands(), false);
        this.masksPane = new BandPane(getSourceProduct() == null ? new Mask[0] : getSourceProduct().getMaskGroup().toArray(new Mask[0]), false);
        this.operatorPanel = new JScrollPane(parametersPane.createPanel());
    }

    JComponent createPanel() {
        this.bandsField = Arrays.stream(operatorDescriptor.getOperatorClass().getDeclaredFields())
                                .filter(f -> f.getAnnotation(Parameter.class) != null && f.getName().equals("bands"))
                                .findFirst().get();
        final Property bandProperty = this.propertySet.getProperty(bandsField.getName());
        bandProperty.addPropertyChangeListener(evt -> { });

        this.masksField = Arrays.stream(operatorDescriptor.getOperatorClass().getDeclaredFields())
                .filter(f -> f.getAnnotation(Parameter.class) != null && f.getName().equals("masks"))
                .findFirst().get();
        final Property masksProperty = this.propertySet.getProperty(masksField.getName());
        masksProperty.addPropertyChangeListener(evt -> { });

        return this.operatorPanel;
    }

    boolean validateParameters() {
        return true;
    }

    /**
     * Sets the incidence angle and the quantification value according to the selected product.
     */
    void reactOnChange() {
        if (isInputProductChanged()) {
            if (this.currentProduct != null) {
                PropertySet propertySet = this.bindingContext.getPropertySet();
                updateProperty(propertySet.getProperty(this.bandsField.getName()));
                updateProperty(propertySet.getProperty(this.masksField.getName()));
            }
        }
    }

    private void updateProperty(Property property){
        updateValueSet(property, this.currentProduct);
        if (property.getValue() == null) {
            try {
                property.setValue(null);
            } catch (ValidationException e) {
                e.printStackTrace();
            }
        }
    }

    BindingContext getBindingContext() { return this.bindingContext; }

    private boolean isInputProductChanged() {
        Product sourceProduct = getSourceProduct();
        if (sourceProduct != this.currentProduct) {
            this.currentProduct = sourceProduct;
            return true;
        } else {
            return false;
        }
    }

    private Product getSourceProduct() {
        try {
            return this.sourceProductAccessor.call();
        } catch (Exception e) {
            return null;
        }
    }

    private void updateValueSet(Property property, Product product) {
        String[] values = new String[0];
        PropertyDescriptor propertyDescriptor = property.getDescriptor();
        if (product != null) {
            Object object = propertyDescriptor.getAttribute(RasterDataNodeValues.ATTRIBUTE_NAME);
            if (object != null) {
                @SuppressWarnings("unchecked")
                Class<? extends RasterDataNode> rasterDataNodeType = (Class<? extends RasterDataNode>) object;
                boolean includeEmptyValue = !propertyDescriptor.isNotNull() && !propertyDescriptor.isNotEmpty() &&
                        !propertyDescriptor.getType().isArray();
                values = RasterDataNodeValues.getNames(product, rasterDataNodeType, includeEmptyValue);
            }
        }
        propertyDescriptor.setValueSet(new ValueSet(values));
    }

    private void setEnabled(String propertyName, boolean value) {
        this.bindingContext.setComponentsEnabled(propertyName, value);
    }

    private void updateNameList() {
        if (this.currentProduct != null) {
            final Property propertyBands = this.propertySet.getProperty(bandsField.getName());
            final Property propertyMasks = this.propertySet.getProperty(masksField.getName());
            try {
                propertyBands.setValue(bandsPane.getSubsetNames());
                propertyMasks.setValue(masksPane.getSubsetNames());
            } catch (ValidationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class BandPane extends JPanel {

        private final Band[] bands;
        private List<JCheckBox> checkers;
        private JCheckBox allCheck;
        private JCheckBox noneCheck;
        private final boolean selected;

        private void setComponentName(JComponent component, String name) {
            if (component != null) {
                Container parent = component.getParent();
                if (parent != null) {
                    component.setName(parent.getName() + "." + name);
                } else {
                    component.setName(name);
                }
            }
        }

        private BandPane(Band[] bands, boolean selected) {
            this.bands = bands;
            this.selected = selected;
            createUI();
        }

        private void createUI() {

            ActionListener productNodeCheckListener = e -> updateUIState();

            checkers = new ArrayList<>(10);
            JPanel checkersPane = GridBagUtils.createPanel();
            setComponentName(checkersPane, "CheckersPane");

            GridBagConstraints gbc = GridBagUtils.createConstraints("insets.left=4,anchor=WEST,fill=HORIZONTAL");
            for (int i = 0; i < bands.length; i++) {
                Band band = bands[i];

                String name = band.getName();
                JCheckBox productNodeCheck = new JCheckBox(name);
                productNodeCheck.setSelected(selected);
                productNodeCheck.setFont(SMALL_PLAIN_FONT);
                productNodeCheck.addActionListener(productNodeCheckListener);

                checkers.add(productNodeCheck);

                String description = band.getDescription();
                JLabel productNodeLabel = new JLabel(description != null ? description : " ");
                productNodeLabel.setFont(SMALL_ITALIC_FONT);

                GridBagUtils.addToPanel(checkersPane, productNodeCheck, gbc, "weightx=0,gridx=0,gridy=" + i);
                GridBagUtils.addToPanel(checkersPane, productNodeLabel, gbc, "weightx=1,gridx=1,gridy=" + i);
            }
            // Add a last 'filler' row
            GridBagUtils.addToPanel(checkersPane, new JLabel(" "), gbc,
                                    "gridwidth=2,weightx=1,weighty=1,gridx=0,gridy=" + bands.length);

            ActionListener allCheckListener = e -> {
                if (e.getSource() == allCheck) {
                    checkAllProductNodes(true);
                } else if (e.getSource() == noneCheck) {
                    checkAllProductNodes(false);
                }
                updateUIState();
            };

            allCheck = new JCheckBox("Select all");
            allCheck.setName("selectAll");
            allCheck.setMnemonic('a');
            allCheck.addActionListener(allCheckListener);

            noneCheck = new JCheckBox("Select none");
            noneCheck.setName("SelectNone");
            noneCheck.setMnemonic('n');
            noneCheck.addActionListener(allCheckListener);

            JScrollPane scrollPane = new JScrollPane(checkersPane);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getVerticalScrollBar().setUnitIncrement(20);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(20);

            JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            buttonRow.add(allCheck);
            buttonRow.add(noneCheck);

            setLayout(new BorderLayout());
            add(scrollPane, BorderLayout.CENTER);
            add(buttonRow, BorderLayout.SOUTH);
            setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));

            updateUIState();
        }

        void updateUIState() {
            allCheck.setSelected(areAllProductNodesChecked(true));
            noneCheck.setSelected(areAllProductNodesChecked(false));
            updateNameList();
        }

        String[] getSubsetNames() {
            String[] names = new String[countChecked(true)];
            int pos = 0;
            for (int i = 0; i < checkers.size(); i++) {
                JCheckBox checker = checkers.get(i);
                if (checker.isSelected()) {
                    ProductNode productNode = bands[i];
                    names[pos] = productNode.getName();
                    pos++;
                }
            }
            return names;
        }

        void checkAllProductNodes(boolean checked) {
            for (JCheckBox checker : checkers) {
                if (checker.isEnabled()) {
                    checker.setSelected(checked);
                }
            }
        }

        boolean areAllProductNodesChecked(boolean checked) {
            return countChecked(checked) == checkers.size();
        }

        int countChecked(boolean checked) {
            int counter = 0;
            for (JCheckBox checker : checkers) {
                if (checker.isSelected() == checked) {
                    counter++;
                }
            }
            return counter;
        }
    }
}
