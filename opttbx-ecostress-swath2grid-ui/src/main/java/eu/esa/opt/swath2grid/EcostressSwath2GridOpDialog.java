package eu.esa.opt.swath2grid;

import com.bc.ceres.binding.ValueSet;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.SubProgressMonitor;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.progress.ProgressMonitorSwingWorker;
import org.apache.commons.collections4.set.ListOrderedSet;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.common.WriteOp;
import org.esa.snap.core.gpf.internal.OperatorExecutor;
import org.esa.snap.core.gpf.internal.OperatorProductReader;
import org.esa.snap.core.gpf.ui.DefaultSingleTargetProductDialog;
import org.esa.snap.core.gpf.ui.TargetProductSelectorModel;
import org.esa.snap.core.util.io.FileUtils;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.rcp.actions.file.SaveProductAsAction;
import org.esa.snap.ui.AppContext;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.factory.FallbackAuthorityFactory;
import org.geotools.util.factory.Hints;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeodeticCRS;
import org.opengis.referencing.crs.ProjectedCRS;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

public class EcostressSwath2GridOpDialog extends DefaultSingleTargetProductDialog {

    private static final String AUTHORITY = "EPSG";
    private static final String UTM_ZONE_PROPERTY_NAME = "utmZone";
    private static final String CRS_IN_PROPERTY_NAME = "crsIN";
    private static final String CRS_IN_PROPERTY_VALUE = "UTM";
    private static final String TARGET_PRODUCT_NAME_SUFFIX = "_swath2grid";
    private static final String OP_ERROR_SIGNAL = "Swath2GridExecError";

    private long createTargetProductTime;

    public EcostressSwath2GridOpDialog(String operatorName, AppContext appContext, String title, String helpID) {
        super(operatorName, appContext, title, helpID, true);

        final ValueSet valueSet = new ValueSet(getUTMCodes());
        getBindingContext().getPropertySet().getDescriptor(UTM_ZONE_PROPERTY_NAME).setAttribute("valueSet", valueSet);
        setTargetProductNameSuffix(TARGET_PRODUCT_NAME_SUFFIX);
    }

    private static String[] getUTMCodes() {
        final Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, true);
        final Set<CRSAuthorityFactory> factories = ReferencingFactoryFinder.getCRSAuthorityFactories(hints);
        final List<CRSAuthorityFactory> filtered = new ArrayList<>();
        for (final CRSAuthorityFactory factory : factories) {
            if (Citations.identifierMatches(factory.getAuthority(), AUTHORITY)) {
                filtered.add(factory);
            }
        }
        final CRSAuthorityFactory crsAuthorityFactory = FallbackAuthorityFactory.create(CRSAuthorityFactory.class, filtered);
        final Set<String> codes = new ListOrderedSet<>();
        codes.add("auto_lookup");
        retrieveCodes(codes, GeodeticCRS.class, crsAuthorityFactory);
        retrieveCodes(codes, ProjectedCRS.class, crsAuthorityFactory);
        return codes.toArray(new String[0]);
    }

    private static void retrieveCodes(Set<String> codes, Class<? extends CoordinateReferenceSystem> crsType,
                                      CRSAuthorityFactory factory) {
        final Set<String> localCodes;
        try {
            localCodes = factory.getAuthorityCodes(crsType);
        } catch (FactoryException ignore) {
            return;
        }
        localCodes.removeIf(localCode -> !localCode.matches("^32[6-7]\\d{2}"));
        codes.addAll(localCodes);
    }

    @Override
    public int show() {
        final int result = super.show();
        final BindingContext bindingContext = getBindingContext();
        final JComboBox<?> utmCB = (JComboBox<?>) bindingContext.getBinding(UTM_ZONE_PROPERTY_NAME).getComponentAdapter().getComponents()[0];
        final String utmCBItemSelected = (String) utmCB.getSelectedItem();
        if (utmCBItemSelected != null) {
            utmCB.setEnabled(utmCBItemSelected.matches(CRS_IN_PROPERTY_VALUE));
        }
        final JComboBox<?> crsInCB = (JComboBox<?>) bindingContext.getBinding(CRS_IN_PROPERTY_NAME).getComponentAdapter().getComponents()[0];
        crsInCB.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                final String itemSelected = (String) e.getItem();
                utmCB.setEnabled(itemSelected.matches(CRS_IN_PROPERTY_VALUE));
            }
        });
        return result;
    }

    @Override
    protected void onApply() {
        if (!canApply()) {
            return;
        }

        final String productDir = targetProductSelector.getModel().getProductDir().getAbsolutePath();
        appContext.getPreferences().setPropertyString(SaveProductAsAction.PREFERENCES_KEY_LAST_PRODUCT_DIR, productDir);

        Product targetProduct = null;
        try {
            long t0 = System.currentTimeMillis();
            targetProduct = createTargetProduct();
            createTargetProductTime = System.currentTimeMillis() - t0;
            if (targetProduct == null) {
                throw new NullPointerException("Target product is null.");
            }
        } catch (Throwable t) {
            handleInitialisationError(t);
        }
        if (targetProduct == null) {
            return;
        }

        if (targetProduct.getName().matches(OP_ERROR_SIGNAL)) {
            final String errorMsg = targetProduct.getDescription();
            handleProcessingError(new IllegalStateException(errorMsg));
        } else {
            if (targetProductSelector.getModel().isSaveToFileSelected()) {
                targetProduct.setFileLocation(targetProductSelector.getModel().getProductFile());
                final ProgressMonitorSwingWorker<?, ?> worker = new ProductWriterSwingWorker(targetProduct);
                worker.executeWithBlocking();
            }else if (targetProductSelector.getModel().isOpenInAppSelected()) {
                appContext.getProductManager().addProduct(targetProduct);
                showOpenInAppInfo();
            }
        }
    }

    private class ProductWriterSwingWorker extends ProgressMonitorSwingWorker<Product, Object> {

        private Product targetProduct;
        private long saveTime;

        private ProductWriterSwingWorker(Product targetProduct) {
            super(getJDialog(), "Writing Target Product");
            this.targetProduct = targetProduct;
        }

        @Override
        protected Product doInBackground(ProgressMonitor pm) throws Exception {
            final TargetProductSelectorModel model = getTargetProductSelector().getModel();
            pm.beginTask("Writing...", model.isOpenInAppSelected() ? 100 : 95);
            saveTime = 0L;
            Product product = null;
            try {
                long t0 = System.currentTimeMillis();
                final Operator execOp = buildExecOp(model);

                final OperatorExecutor executor = OperatorExecutor.create(execOp);
                executor.execute(SubProgressMonitor.create(pm, 95));

                saveTime = System.currentTimeMillis() - t0;
                if (model.isOpenInAppSelected()) {
                    File targetFile = model.getProductFile();
                    if (!targetFile.exists())
                        targetFile = targetProduct.getFileLocation();
                    if (targetFile.exists()) {
                        targetProduct.dispose();
                        targetProduct = null;
                        product = ProductIO.readProduct(targetFile);
                    }
                    pm.worked(5);
                }
            } finally {
                pm.done();
                if (targetProduct != null) {
                    targetProduct.dispose();
                    targetProduct = null;
                }
                final Preferences preferences = SnapApp.getDefault().getPreferences();
                if (preferences.getBoolean(GPF.BEEP_AFTER_PROCESSING_PROPERTY, false)) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
            return product;
        }

        private Operator buildExecOp(TargetProductSelectorModel model) {
            Operator execOp = null;
            if (targetProduct.getProductReader() instanceof OperatorProductReader) {
                final OperatorProductReader opReader = (OperatorProductReader) targetProduct.getProductReader();
                final Operator operator = opReader.getOperatorContext().getOperator();
                if (operator.getSpi().getOperatorDescriptor().isAutoWriteDisabled()) {
                    execOp = operator;
                }
            }
            if (execOp == null) {
                final WriteOp writeOp = new WriteOp(targetProduct, model.getProductFile(), model.getFormatName());
                writeOp.setDeleteOutputOnFailure(true);
                writeOp.setWriteEntireTileRows(true);
                writeOp.setClearCacheAfterRowWrite(false);
                execOp = writeOp;
            }
            return execOp;
        }

        @Override
        protected void done() {
            final TargetProductSelectorModel model = getTargetProductSelector().getModel();
            long totalSaveTime = saveTime + createTargetProductTime;
            try {
                final Product targetProduct = get();
                if (model.isOpenInAppSelected()) {
                    appContext.getProductManager().addProduct(targetProduct);
                    showSaveAndOpenInAppInfo(totalSaveTime);
                } else {
                    showSaveInfo(totalSaveTime);
                }
            } catch (InterruptedException e) {
                // ignore
            } catch (ExecutionException e) {
                handleProcessingError(e.getCause());
            } catch (Throwable t) {
                handleProcessingError(t);
            }
        }


        private void showSaveInfo(long saveTime) {
            File productFile = getTargetProductSelector().getModel().getProductFile();
            final String message = MessageFormat.format(
                    "<html>The target product has been successfully written to<br>{0}<br>" +
                            "Total time spend for processing: {1}",
                    formatFile(productFile),
                    formatDuration(saveTime)
            );
            showSuppressibleInformationDialog(message, "saveInfo");
        }

        private void showSaveAndOpenInAppInfo(long saveTime) {
            File productFile = getTargetProductSelector().getModel().getProductFile();
            final String message = MessageFormat.format(
                    "<html>The target product has been successfully written to<br>" +
                            "<p>{0}</p><br>" +
                            "and has been opened in {1}.<br><br>" +
                            "Total time spend for processing: {2}<br>",
                    formatFile(productFile),
                    appContext.getApplicationName(),
                    formatDuration(saveTime)
            );
            showSuppressibleInformationDialog(message, "saveAndOpenInAppInfo");
        }

        private String formatFile(File file) {
            return FileUtils.getDisplayText(file, 54);
        }

        private String formatDuration(long millis) {
            long seconds = millis / 1000;
            millis -= seconds * 1000;
            long minutes = seconds / 60;
            seconds -= minutes * 60;
            long hours = minutes / 60;
            minutes -= hours * 60;
            return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
        }
    }
}
