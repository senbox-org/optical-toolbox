package eu.esa.opt.fcc;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.ValueSet;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.selection.SelectionChangeEvent;
import com.bc.ceres.swing.selection.SelectionChangeListener;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.dataop.barithm.BandArithmetic;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.descriptor.OperatorDescriptor;
import org.esa.snap.core.gpf.ui.DefaultIOParametersPanel;
import org.esa.snap.core.gpf.ui.DefaultSingleTargetProductDialog;
import org.esa.snap.core.gpf.ui.SourceProductSelector;
import org.esa.snap.core.jexp.ParseException;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.ui.AppContext;

import javax.swing.*;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.*;


/**
 * @author CSG RO
 * @since 13.0.0
 */
@Deprecated
public class BandExtractorDialog  extends DefaultSingleTargetProductDialog {

    private final Field bandsField;
    private final Field masksField;
    private final Field includeReferencesField;

    public BandExtractorDialog(String operatorName, AppContext appContext, String title, String helpID) {
        this(operatorName, appContext, title, helpID, true);
    }
    public BandExtractorDialog(String operatorName, AppContext appContext, String title, String helpID, boolean targetProductSelectorDisplay) {
        super(operatorName, appContext, title, helpID, targetProductSelectorDisplay);

        OperatorDescriptor descriptor = GPF.getDefaultInstance().getOperatorSpiRegistry().getOperatorSpi(operatorName).getOperatorDescriptor();
        bandsField = Arrays.stream(descriptor.getOperatorClass().getDeclaredFields())
                .filter(f -> f.getAnnotation(Parameter.class) != null && f.getName().equals("sourceBandNames"))
                .findFirst().get();
        masksField = Arrays.stream(descriptor.getOperatorClass().getDeclaredFields())
                .filter(f -> f.getAnnotation(Parameter.class) != null && f.getName().equals("sourceMaskNames"))
                .findFirst().get();
        includeReferencesField = Arrays.stream(descriptor.getOperatorClass().getDeclaredFields())
                .filter(f -> f.getAnnotation(Parameter.class) != null && f.getName().equals("includeReferences"))
                .findFirst().get();
        DefaultIOParametersPanel ioParametersPanel = getDefaultIOParametersPanel();

        List<SourceProductSelector> sourceProductSelectorList = ioParametersPanel.getSourceProductSelectorList();
        if (!sourceProductSelectorList.isEmpty()) {
            SelectionChangeListener listener = new SelectionChangeListener() {
                public void selectionChanged(SelectionChangeEvent event) {
                    processSelectedProduct();
                }

                public void selectionContextChanged(SelectionChangeEvent event) {
                }
            };
            sourceProductSelectorList.get(0).addSelectionChangeListener(listener);
        }

        BindingContext bindingContext = getBindingContext();
        PropertySet propertySet = bindingContext.getPropertySet();
        final Property propBands = propertySet.getProperty(this.bandsField.getName());
        propBands.addPropertyChangeListener(evt-> { });
        final Property propMasks = propertySet.getProperty(this.masksField.getName());
        propMasks.addPropertyChangeListener(evt-> { });

        final Property propIncludeReferences = propertySet.getProperty(this.includeReferencesField.getName());
        propIncludeReferences.getDescriptor().setAttribute("visible", false);

    }

    @Override
    public int show() {
        int result = super.show();
        processSelectedProduct();
        return result;
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    protected boolean verifyUserInput() {

        return checkReferencedRastersIncluded();
    }

    private boolean checkReferencedRastersIncluded() {

        Product selectedProduct = getSelectedProduct();
        if (selectedProduct == null) {
            return true;
        }

        final Set<String> notIncludedNames = new TreeSet<>();

        BindingContext bindingContext = getBindingContext();
        PropertySet propertySet = bindingContext.getPropertySet();

        final Property propertyBands = propertySet.getProperty(this.bandsField.getName());
        List<String> lstSourceBandNames = new ArrayList<>();
        if (propertyBands != null && propertyBands.getValue() != null) {
            Arrays.asList(propertyBands.getValue()).forEach(bandName -> lstSourceBandNames.add((String) bandName));
        }

        final Property propertyMasks = propertySet.getProperty(this.masksField.getName());
        List<String> lstSourceMaskNames = new ArrayList<>();
        if (propertyMasks != null && propertyMasks.getValue() != null) {
            Arrays.asList(propertyMasks.getValue()).forEach(maskName -> lstSourceMaskNames.add((String) maskName));
        }

        final ArrayList<String> referencedBandNames = new ArrayList<>();
        final ArrayList<String> referencedMaskNames = new ArrayList<>();
        String[] nodeNames = lstSourceBandNames.toArray(new String[0]);
        if (nodeNames != null) {
            for (String nodeName : nodeNames) {
                collectNotIncludedReferences(nodeName, referencedBandNames, referencedMaskNames);
            }

            referencedBandNames.forEach(bandName ->
                            {
                                if (!lstSourceBandNames.contains(bandName)) {
                                    lstSourceBandNames.add(bandName);
                                    notIncludedNames.add(bandName);
                                }
                            });
            referencedMaskNames.forEach(maskName ->
                            {
                                if (!lstSourceMaskNames.contains(maskName)) {
                                    lstSourceMaskNames.add(maskName);
                                    notIncludedNames.add(maskName);
                                }
                            });
        }

        boolean ok = true;
        if (!notIncludedNames.isEmpty()) {
            StringBuilder nameListText = new StringBuilder();
            for (String notIncludedName : notIncludedNames) {
                nameListText.append("  '").append(notIncludedName).append("'\n");
            }

            final String pattern = "The following dataset(s) are referenced but not included\n" +
                    "in your current selection:\n" +
                    "{0}\n" +
                    "If you do not include these dataset(s) into your selection,\n" +
                    "you might get unexpected results while working with the\n" +
                    "resulting product.\n\n" +
                    "Do you wish to include the referenced dataset(s) into your\n" +
                    "list?\n";
            final MessageFormat format = new MessageFormat(pattern);
            int status = JOptionPane.showConfirmDialog(getJDialog(),
                    format.format(new Object[]{nameListText.toString()}),
                    "Incomplete Subset Definition",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (status == JOptionPane.YES_OPTION) {
                propertySet.setValue(this.includeReferencesField.getName(), true);
                ok = true;
            } else if (status == JOptionPane.NO_OPTION) {
                propertySet.setValue(this.includeReferencesField.getName(), false);
                ok = true;
            } else if (status == JOptionPane.CANCEL_OPTION) {
                ok = false;
            }
        }
        return ok;
    }

    private void collectNotIncludedReferences(String nodeName, ArrayList<String> referencedBandNames, ArrayList<String> referencedMaskNames) {
        Product sourceProduct = getSelectedProduct();
        if (sourceProduct == null)
            return;

        RasterDataNode rasterDataNode = sourceProduct.getRasterDataNode(nodeName);
        if (rasterDataNode == null) {
            throw new OperatorException(String.format("Source product does not contain a raster named '%s'.", nodeName));
        }
        final String validPixelExpression = rasterDataNode.getValidPixelExpression();
        collectReferencedRastersInExpression(validPixelExpression, referencedBandNames, referencedMaskNames);

        if (rasterDataNode instanceof VirtualBand || rasterDataNode instanceof Mask) {
            String strExpression = getRasterDataNodeExpression (rasterDataNode);
            collectReferencedRastersInExpression(strExpression, referencedBandNames, referencedMaskNames);
        }
    }

    private String getRasterDataNodeExpression(RasterDataNode rasterDataNode){
        if (rasterDataNode == null )
            return null;
        String strExpression = null;
        if (rasterDataNode instanceof VirtualBand) {
            strExpression = ((VirtualBand) rasterDataNode).getExpression();
        }else if  (rasterDataNode instanceof Mask) {
            Mask mask = (Mask) rasterDataNode;
            if (mask.getImageType() == Mask.BandMathsType.INSTANCE) {
                strExpression = Mask.BandMathsType.getExpression(mask);
            } else if (mask.getImageType() == Mask.RangeType.INSTANCE) {
                strExpression = Mask.RangeType.getRasterName(mask);
            }
        }
        return strExpression;
    }

    private void collectReferencedRastersInExpression(String expression, ArrayList<String> referencedBandNames, ArrayList<String> referencedMaskNames) {
        if (expression == null || expression.trim().isEmpty()) {
            return;
        }
        try {
            Product sourceProduct = getSelectedProduct();
            if (sourceProduct == null)
                return;

            final Term term = sourceProduct.parseExpression(expression);
            final RasterDataNode[] refRasters = BandArithmetic.getRefRasters(term);
            for (RasterDataNode refRaster : refRasters) {
                final String refNodeName = refRaster.getName();
                Band bandNode =  sourceProduct.getBand(refNodeName) ;
                if ( bandNode!= null){
                    if (!referencedBandNames.contains(refNodeName)) {
                        referencedBandNames.add(refNodeName);
                    }
                    final String bandExpression = getRasterDataNodeExpression (bandNode);
                    collectReferencedRastersInExpression(bandExpression, referencedBandNames, referencedMaskNames);
                }

                Mask maskNode =  sourceProduct.getMaskGroup().get(refNodeName) ;
                if (maskNode != null){
                    if (!referencedMaskNames.contains(refNodeName)) {
                        referencedMaskNames.add(refNodeName);
                    }
                    final String maskExpression = getRasterDataNodeExpression (maskNode);
                    collectReferencedRastersInExpression(maskExpression, referencedBandNames, referencedMaskNames);
                }
            }
        } catch (ParseException e) {
            //getLogger().log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void processSelectedProduct() {
        Product selectedProduct = getSelectedProduct();
        if (selectedProduct != null) {
            BindingContext bindingContext = getBindingContext();
            PropertySet propertySet = bindingContext.getPropertySet();
            propertySet.setDefaultValues();
            final Property propertyBands = propertySet.getProperty(this.bandsField.getName());
            propertyBands.getDescriptor().setValueSet(new ValueSet(selectedProduct.getBandNames()));
            propertySet.setValue(this.bandsField.getName(), null);

            final Property propertyMasks = propertySet.getProperty(this.masksField.getName());
            propertyMasks.getDescriptor().setValueSet(new ValueSet(selectedProduct.getMaskGroup().getNodeNames()));
            propertySet.setValue(this.masksField.getName(), null);
        }
    }

    /**
     * Returns the selected product.
     *
     * @return the selected product
     */
    private Product getSelectedProduct() {
        DefaultIOParametersPanel ioParametersPanel = getDefaultIOParametersPanel();
        List<SourceProductSelector> sourceProductSelectorList = ioParametersPanel.getSourceProductSelectorList();
        return sourceProductSelectorList.get(0).getSelectedProduct();
    }
}
