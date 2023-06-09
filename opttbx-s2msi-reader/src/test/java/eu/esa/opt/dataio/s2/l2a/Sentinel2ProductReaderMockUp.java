/*
 * Copyright (C) 2014-2015 CS-SI (foss-contact@thor.si.c-s.fr)
 * Copyright (C) 2013-2015 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package eu.esa.opt.dataio.s2.l2a;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductSubsetDef;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.ui.ModalDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

/**
 * @author Norman Fomferra
 */
public class Sentinel2ProductReaderMockUp extends AbstractProductReader {

    public Sentinel2ProductReaderMockUp(Sentinel2ProductReaderMockUpPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    public Product readProductNodes(Object input, ProductSubsetDef subsetDef) throws IOException {
        ModalDialog modalDialog = new ModalDialog(null, "Sentinel-2 MSI Reader Options", ModalDialog.ID_OK_CANCEL_HELP, "");
        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(6, 6, 6, 6));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets.top = 2;
        constraints.insets.bottom = 2;
        constraints.insets.left = 2;
        constraints.insets.right = 2;
        constraints.gridx = 0;
        constraints.gridy = -1;

        constraints.gridy++;
        content.add(new JLabel("This data product contains images in various spatial resolutions."), constraints);
        constraints.insets.top = 0;
        constraints.gridy++;
        content.add(new JLabel("Please specify how to treat them:"), constraints);

        constraints.insets.top = 12;
        constraints.gridy++;
        content.add(new JRadioButton("Read a single data product and make all bands have the same resolution:", true), constraints);
        constraints.insets.top = 0;
        constraints.insets.left = 24;

        constraints.gridy++;
        content.add(new JRadioButton("Scale to 10m (duplicate 20m and 60m pixels)", true), constraints);
        constraints.gridy++;
        content.add(new JRadioButton("Scale to 20m (average 10m and duplicate 60m pixels)", false), constraints);
        constraints.gridy++;
        content.add(new JRadioButton("Scale to 60m (average 10m and 20m pixels)", false), constraints);

        constraints.gridy++;
        constraints.insets.top = 12;
        constraints.insets.left = 2;
        content.add(new JRadioButton("Read as groups of bands with different resolutions:", false), constraints);
        constraints.insets.top = 2;
        constraints.insets.left = 24;

        constraints.insets.top = 0;
        constraints.gridy++;
        JCheckBox checkBox10 = new JCheckBox("Read 10m bands (B2, B8, B3, B4)", true);
        checkBox10.setEnabled(false);
        content.add(checkBox10, constraints);
        constraints.gridy++;
        JCheckBox checkBox20 = new JCheckBox("Read 20m bands (B5, B6, B7, B8a, B11, B12)", true);
        checkBox20.setEnabled(false);
        content.add(checkBox20, constraints);
        constraints.gridy++;
        JCheckBox checkBox60 = new JCheckBox("Read 60m bands (B1, B9, B10)", true);
        checkBox60.setEnabled(false);
        content.add(checkBox60, constraints);

        constraints.gridy++;
        constraints.insets.left = 2;
        constraints.insets.top = 12;
        content.add(new JCheckBox("Save as default values and don't ask again."), constraints);

        constraints.gridy++;
        constraints.insets.left = 24;
        constraints.insets.top = 0;
        content.add(new JLabel("<html><small>Note that you can always change these settings in the preferences dialog.</small></html>"), constraints);

        modalDialog.setContent(content);
        final int show = modalDialog.show();

        if (show == ModalDialog.ID_OK) {
            return super.readProductNodes(input, subsetDef);
        } else {
            return null;
        }
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        return new Product("S2", "S2", 16, 16);
    }


    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY, Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {
    }
}
