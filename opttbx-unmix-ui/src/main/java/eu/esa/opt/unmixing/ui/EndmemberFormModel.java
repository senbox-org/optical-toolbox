/*
 * Copyright (C) 2012 Brockmann Consult GmbH (info@brockmann-consult.de)
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
import org.esa.snap.core.util.ResourceInstaller;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.esa.snap.runtime.Config;
import org.esa.snap.tango.TangoIcons;
import org.esa.snap.ui.AppContext;
import org.esa.snap.ui.diagram.DefaultDiagramGraph;
import org.esa.snap.ui.diagram.DefaultDiagramGraphStyle;
import org.esa.snap.ui.diagram.Diagram;
import org.esa.snap.ui.diagram.DiagramAxis;
import org.esa.snap.ui.diagram.DiagramGraph;
import org.esa.snap.ui.diagram.DiagramGraphIO;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.prefs.Preferences;

class EndmemberFormModel {

    private static final Path defaultEndmemberDir = SystemUtils.getAuxDataPath().resolve("unmix");
    private final DefaultListModel<Endmember> endmemberListModel;
    private final DefaultListSelectionModel endmemberListSelectionModel;
    private final Diagram endmemberDiagram;

    private final Action addAction;
    private final Action removeAction;
    private final Action clearAction;
    private final Action exportAction;

    private final AppContext appContext;

    private final PropertyChangeSupport propertyChangeSupport;

    private final Color[] defaultColors = new Color[]{Color.BLACK, Color.RED.darker(), Color.GREEN.darker(), Color.BLUE.darker(), Color.YELLOW};
    private int selectedEndmemberIndex;

    public EndmemberFormModel(AppContext appContext) {
        this.appContext = appContext;
        endmemberListModel = new DefaultListModel<>();
        endmemberListSelectionModel = new DefaultListSelectionModel();
        endmemberListSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        endmemberListModel.addListDataListener(new EndmemberListDataListener());
        endmemberListSelectionModel.addListSelectionListener(new EndmemberListSelectionListener());
        addAction = new AddAction();
        removeAction = new RemoveAction();
        clearAction = new ClearAction();
        exportAction = new ExportAction();
        endmemberDiagram = new Diagram();
        endmemberDiagram.setXAxis(new DiagramAxis("Wavelength", ""));
        endmemberDiagram.setYAxis(new DiagramAxis("Radiation", ""));
        endmemberDiagram.setDrawGrid(false);
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    private static DefaultDiagramGraph createEndmemberGraph(Endmember endmember) {
        return new DefaultDiagramGraph("Wavelength", endmember.getWavelengths(), endmember.getName(), endmember.getRadiations());
    }

    public Endmember[] getEndmembers() {
        Endmember[] endmembers = new Endmember[endmemberListModel.getSize()];
        for (int i = 0; i < endmembers.length; i++) {
            endmembers[i] = endmemberListModel.getElementAt(i);
        }
        return endmembers;
    }

    public void setEndmembers(Endmember[] endmembers) {
        endmemberListModel.removeAllElements();
        for (Endmember endmember : endmembers) {
            endmemberListModel.addElement(endmember);
        }
    }

    public ListModel<Endmember> getEndmemberListModel() {
        return endmemberListModel;
    }

    public DefaultListSelectionModel getEndmemberListSelectionModel() {
        return endmemberListSelectionModel;
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public Action getAddAction() {
        return addAction;
    }

    public Action getRemoveAction() {
        return removeAction;
    }

    public Action getClearAction() {
        return clearAction;
    }

    public Action getExportAction() {
        return exportAction;
    }

    public Diagram getEndmemberDiagram() {
        return endmemberDiagram;
    }

    private void addEndmember(Endmember endmember) {
        endmemberListModel.addElement(endmember);
        DefaultDiagramGraph endmemberGraph = createEndmemberGraph(endmember);
        Color color = defaultColors[endmemberListModel.getSize() % defaultColors.length];
        DefaultDiagramGraphStyle style = ((DefaultDiagramGraphStyle) endmemberGraph.getStyle());
        style.setOutlineColor(color);
        style.setOutlineStroke(new BasicStroke(1.0f));
        style.setShowingPoints(false);
        endmemberDiagram.addGraph(endmemberGraph);
        endmemberDiagram.adjustAxes(true);
    }

    public void setSelectedEndmemberIndex(int index) {
        int oldIndex = selectedEndmemberIndex;
        if (oldIndex == index) {
            return;
        }
        if (oldIndex >= 0 && endmemberDiagram.getGraphCount() > 0) {
            final DiagramGraph endmemberGraph = endmemberDiagram.getGraph(oldIndex);
            ((DefaultDiagramGraphStyle) endmemberGraph.getStyle()).setOutlineStroke(new BasicStroke(1.0f));
        }
        selectedEndmemberIndex = index;
        if (selectedEndmemberIndex >= 0 && endmemberDiagram.getGraphCount() > 0) {
            final DiagramGraph endmemberGraph = endmemberDiagram.getGraph(selectedEndmemberIndex);
            ((DefaultDiagramGraphStyle) endmemberGraph.getStyle()).setOutlineStroke(new BasicStroke(2.0f));
        }
        endmemberDiagram.invalidate();
        propertyChangeSupport.firePropertyChange("selectedEndmemberIndex", oldIndex, selectedEndmemberIndex);
    }

    private void ensureDefaultDirSet() {
        if (!Files.exists(defaultEndmemberDir)) {
            Path sourceDirPath = ResourceInstaller.findModuleCodeBasePath(SpectralUnmixingDialog.class).resolve("auxdata");
            final ResourceInstaller resourceInstaller = new ResourceInstaller(sourceDirPath, defaultEndmemberDir);
            try {
                resourceInstaller.install(".*", com.bc.ceres.core.ProgressMonitor.NULL);
            } catch (IOException e) {
                // failed, so what
            }
        }

        final String key = DiagramGraphIO.DIAGRAM_GRAPH_IO_LAST_DIR_KEY;
        final Preferences preferences = Config.instance().preferences();
        if (preferences.get(key, null) == null) {
            preferences.put(key, defaultEndmemberDir.toAbsolutePath().toString());
        }
    }

    private class AddAction extends AbstractAction {

        public AddAction() {
            super("Add");
            putValue(LARGE_ICON_KEY, TangoIcons.actions_list_add(TangoIcons.Res.R16));
            putValue(SHORT_DESCRIPTION, "Add Endmembers");
        }

        public void actionPerformed(ActionEvent e) {
            ensureDefaultDirSet();
            DiagramGraph[] diagramGraphs = DiagramGraphIO.readGraphs(null,
                    "Add Endmembers",
                    new SnapFileFilter[]{DiagramGraphIO.SPECTRA_CSV_FILE_FILTER},
                    appContext.getPreferences());
            Endmember[] endmembers = convertGraphsToEndmembers(diagramGraphs);
            for (Endmember endmember : endmembers) {
                addEndmember(endmember);
            }
        }

        private Endmember[] convertGraphsToEndmembers(DiagramGraph[] diagramGraphs) {
            Endmember[] endmembers = new Endmember[diagramGraphs.length];
            for (int i = 0; i < diagramGraphs.length; i++) {
                DiagramGraph diagramGraph = diagramGraphs[i];
                int numValues = diagramGraph.getNumValues();
                double[] wavelengths = new double[numValues];
                double[] radiations = new double[numValues];
                for (int j = 0; j < numValues; j++) {
                    wavelengths[j] = diagramGraph.getXValueAt(j);
                    radiations[j] = diagramGraph.getYValueAt(j);
                }
                endmembers[i] = new Endmember(diagramGraph.getYName(), wavelengths, radiations);
            }
            return endmembers;
        }
    }

    private class RemoveAction extends AbstractAction {
        public RemoveAction() {
            super("Remove");
            putValue(LARGE_ICON_KEY, TangoIcons.actions_list_remove(TangoIcons.Res.R16));
            putValue(SHORT_DESCRIPTION, "Remove Endmember");
        }

        public void actionPerformed(ActionEvent e) {
            int index = selectedEndmemberIndex;
            if (index >= 0) {
                setSelectedEndmemberIndex(-1);
                endmemberListModel.removeElementAt(index);
                endmemberDiagram.removeGraph(endmemberDiagram.getGraph(index));
                endmemberDiagram.adjustAxes(true);
            }
        }
    }

    private class ClearAction extends AbstractAction {
        public ClearAction() {
            super("Clear");
            putValue(LARGE_ICON_KEY, TangoIcons.actions_edit_clear(TangoIcons.Res.R16));
            putValue(SHORT_DESCRIPTION, "Clear List");
        }

        public void actionPerformed(ActionEvent e) {
            setSelectedEndmemberIndex(-1);
            endmemberListModel.removeAllElements();
            endmemberDiagram.removeAllGraphs();
        }
    }

    private class ExportAction extends AbstractAction {
        public ExportAction() {
            super("Export");
            putValue(LARGE_ICON_KEY, TangoIcons.actions_document_save_as(TangoIcons.Res.R16));
            putValue(SHORT_DESCRIPTION, "Export Endmembers");
        }

        public void actionPerformed(ActionEvent e) {
            ensureDefaultDirSet();
            DiagramGraphIO.writeGraphs(null,
                    "Export Endmembers",
                    new SnapFileFilter[]{DiagramGraphIO.SPECTRA_CSV_FILE_FILTER},
                    appContext.getPreferences(),
                    endmemberDiagram.getGraphs());
        }
    }

    private class EndmemberListDataListener implements ListDataListener {
        public void intervalAdded(ListDataEvent e) {
            endmemberDiagram.invalidate();
        }

        public void intervalRemoved(ListDataEvent e) {
            endmemberDiagram.invalidate();
        }

        public void contentsChanged(ListDataEvent e) {
            endmemberDiagram.invalidate();
        }
    }

    private class EndmemberListSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                if (endmemberListSelectionModel.isSelectionEmpty()) {
                    setSelectedEndmemberIndex(-1);
                } else {
                    setSelectedEndmemberIndex(endmemberListSelectionModel.getLeadSelectionIndex());
                }
            }
        }
    }
}
