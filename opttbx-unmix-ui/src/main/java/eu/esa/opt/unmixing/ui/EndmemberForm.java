/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package eu.esa.opt.unmixing.ui;

import eu.esa.opt.unmixing.Endmember;
import org.esa.snap.ui.AppContext;
import org.esa.snap.ui.diagram.DiagramCanvas;
import org.esa.snap.ui.tool.ToolButtonFactory;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

class EndmemberForm extends JPanel {
    EndmemberFormModel formModel;
    JList<Endmember> endmemberList;
    DiagramCanvas diagramCanvas;

    public EndmemberForm(AppContext appContext) {
        this.formModel = new EndmemberFormModel(appContext);
        initComponents();
    }

    public EndmemberFormModel getFormModel() {
        return formModel;
    }

    private void initComponents() {

        endmemberList = new JList<>();
        endmemberList.setModel(formModel.getEndmemberListModel());
        endmemberList.setSelectionModel(formModel.getEndmemberListSelectionModel());
        endmemberList.setPreferredSize(new Dimension(80, 160));

        diagramCanvas = new DiagramCanvas();
        diagramCanvas.setDiagram(formModel.getEndmemberDiagram());
        formModel.getPropertyChangeSupport().addPropertyChangeListener("selectedEndmemberIndex",
                evt -> diagramCanvas.repaint());

        AbstractButton addButton = ToolButtonFactory.createButton(formModel.getAddAction(), false);
        AbstractButton removeButton = ToolButtonFactory.createButton(formModel.getRemoveAction(), false);
        AbstractButton clearButton = ToolButtonFactory.createButton(formModel.getClearAction(), false);
        AbstractButton exportButton = ToolButtonFactory.createButton(formModel.getExportAction(), false);

        GridBagLayout gbl = new GridBagLayout();
        JPanel actionPanel = new JPanel(gbl);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipady = 2;
        gbc.gridy = 0;
        actionPanel.add(addButton, gbc);
        gbc.gridy++;
        actionPanel.add(removeButton, gbc);
        gbc.gridy++;
        actionPanel.add(clearButton, gbc);
        gbc.gridy++;
        actionPanel.add(exportButton, gbc);
        gbc.gridy++;
        gbc.weighty = 1;
        actionPanel.add(new JLabel(), gbc);
        final Color color = actionPanel.getBackground();
        final float[] rgbColors = new float[3];
        color.getRGBColorComponents(rgbColors);
        final float factor = 0.9f;
        actionPanel.setBackground(new Color(rgbColors[0] * factor, rgbColors[1] * factor, rgbColors[2] * factor));

        JPanel endmemberSelectionPanel = new JPanel(new BorderLayout());
        endmemberSelectionPanel.add(new JScrollPane(endmemberList), BorderLayout.CENTER);
        endmemberSelectionPanel.add(actionPanel, BorderLayout.WEST);

        JPanel endmemberPreviewPanel = new JPanel(new BorderLayout());
        endmemberPreviewPanel.add(diagramCanvas, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(endmemberSelectionPanel);
        splitPane.setRightComponent(endmemberPreviewPanel);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setUI(createPlainDividerSplitPaneUI());

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
    }

    private BasicSplitPaneUI createPlainDividerSplitPaneUI() {
        return new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        // do nothing
                    }
                };
            }
        };
    }

}
