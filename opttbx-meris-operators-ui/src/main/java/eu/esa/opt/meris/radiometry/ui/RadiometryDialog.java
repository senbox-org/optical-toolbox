/*
 * Copyright (C) 2014 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package eu.esa.opt.meris.radiometry.ui;

import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.ui.OperatorMenu;
import org.esa.snap.core.gpf.ui.OperatorParameterSupport;
import org.esa.snap.core.gpf.ui.SingleTargetProductDialog;
import org.esa.snap.core.gpf.ui.TargetProductSelectorModel;
import org.esa.snap.core.util.ArrayUtils;
import org.esa.snap.dataio.envisat.EnvisatConstants;
import org.esa.snap.rcp.util.Dialogs;
import org.esa.snap.ui.AppContext;

import java.util.HashMap;

class RadiometryDialog extends SingleTargetProductDialog {

    private final String alias;
    private final OperatorParameterSupport parameterSupport;
    private final RadiometryForm form;

    RadiometryDialog(String alias, AppContext appContext, String title, String helpId) {
        super(appContext, title, ID_APPLY_CLOSE, helpId,
              TargetProductSelectorModel.createEnvisatTargetProductSelectorModel());
        this.alias = alias;
        final OperatorSpi operatorSpi = GPF.getDefaultInstance().getOperatorSpiRegistry().getOperatorSpi(alias);

        parameterSupport = new OperatorParameterSupport(operatorSpi.getOperatorDescriptor());
        form = new RadiometryForm(appContext, operatorSpi, parameterSupport.getPropertySet(),
                                  getTargetProductSelector());
        OperatorMenu operatorMenu = new OperatorMenu(this.getJDialog(),
                                                     operatorSpi.getOperatorDescriptor(),
                                                     parameterSupport,
                                                     appContext,
                                                     helpId);
        getJDialog().setJMenuBar(operatorMenu.createDefaultMenu());
    }

    @Override
    protected Product createTargetProduct() throws Exception {
        final Product sourceProduct = form.getSourceProduct();
        final Product radioCorrProduct = GPF.createProduct(alias, parameterSupport.getParameterMap(), sourceProduct);
        if (isEnvisatFormatSelected() && getTargetProductSelector().getModel().isSaveToFileSelected()) {
            final HashMap<String, Object> n1Parameters = new HashMap<String, Object>();
            n1Parameters.put("patchedFile", getTargetProductSelector().getModel().getProductFile());
            final HashMap<String, Product> sourceProductMap = new HashMap<String, Product>();
            sourceProductMap.put("n1", form.getSourceProduct());
            sourceProductMap.put("input", radioCorrProduct);
            return GPF.createProduct("Meris.N1Patcher", n1Parameters, sourceProductMap);
        }
        return radioCorrProduct;
    }

    @Override
    protected void onApply() {
        if (validateUserInput()) {
            super.onApply();
        }

    }

    private boolean validateUserInput() {
        if (isEnvisatFormatSelected()) {
            final ProductReader productReader = form.getSourceProduct().getProductReader();
            boolean isEnvisatSource = false;
            if (productReader != null) {
                final String[] formatNames = productReader.getReaderPlugIn().getFormatNames();
                isEnvisatSource = ArrayUtils.getElementIndex(EnvisatConstants.ENVISAT_FORMAT_NAME, formatNames) != -1;
            }
            if (!isEnvisatSource) {
                final String msg = "If " + EnvisatConstants.ENVISAT_FORMAT_NAME + " is selected as output format, " +
                        "the source product must be in the same format.";
                Dialogs.showError("Invalid Settings", msg);
                return false;
            }
        }
        return true;
    }

    private boolean isEnvisatFormatSelected() {
        return EnvisatConstants.ENVISAT_FORMAT_NAME.equals(getTargetProductSelector().getModel().getFormatName());
    }

    @Override
    public int show() {
        form.prepareShow();
        setContent(form);
        return super.show();
    }

    @Override
    public void hide() {
        form.prepareHide();
        super.hide();
    }
}
