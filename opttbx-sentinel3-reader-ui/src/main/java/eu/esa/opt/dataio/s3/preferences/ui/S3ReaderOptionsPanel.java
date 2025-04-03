/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.esa.opt.dataio.s3.preferences.ui;

import com.bc.ceres.swing.TableLayout;
import eu.esa.opt.dataio.s3.meris.MerisProductFactory;
import eu.esa.opt.dataio.s3.olci.OlciContext;
import eu.esa.opt.dataio.s3.olci.OlciLevel1ProductFactory;
import eu.esa.opt.dataio.s3.olci.OlciProductFactory;
import eu.esa.opt.dataio.s3.slstr.SlstrLevel1ProductFactory;
import eu.esa.opt.dataio.s3.slstr.SlstrSstProductFactory;
import eu.esa.opt.dataio.s3.util.CalibrationUtils;
import eu.esa.opt.dataio.s3.util.S3Util;
import org.esa.snap.rcp.SnapApp;
import org.esa.snap.rcp.preferences.PreferenceUtils;
import org.esa.snap.runtime.Config;
import org.esa.snap.ui.ModalDialog;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

final class S3ReaderOptionsPanel extends javax.swing.JPanel {

    private JCheckBox slstrL1BPixelGeocodingsCheckBox;
    private JCheckBox slstrL1BOrphanPixelsCheckBox;
    private JCheckBox slstrL1BCalibrationCheckBox;
    private JCheckBox slstrL1BS3MPCRecommendationCheckBox;
    private JCheckBox slstrL2SSTPixelGeocodingsCheckBox;
    private JCheckBox olciPixelGeocodingsCheckBox;
    private JCheckBox olciL1CalibrationCheckBox;
    private JCheckBox merisPixelGeocodingsCheckBox;

    S3ReaderOptionsPanel(final S3ReaderOptionsPanelController controller) {
        initComponents();
        // listen to changes in form fields and call controller.changed()
        slstrL1BPixelGeocodingsCheckBox.addItemListener(e -> controller.changed());
        slstrL1BOrphanPixelsCheckBox.addItemListener(e -> controller.changed());
        slstrL1BCalibrationCheckBox.addItemListener(e -> controller.changed());
        slstrL1BS3MPCRecommendationCheckBox.addItemListener(e -> controller.changed());
        slstrL2SSTPixelGeocodingsCheckBox.addItemListener(e -> controller.changed());
        olciPixelGeocodingsCheckBox.addItemListener(e -> controller.changed());
        olciL1CalibrationCheckBox.addItemListener(e -> controller.changed());
        merisPixelGeocodingsCheckBox.addItemListener(e -> controller.changed());
    }

    private void initComponents() {
        slstrL1BPixelGeocodingsCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(slstrL1BPixelGeocodingsCheckBox,
                                   NbBundle.getMessage(S3ReaderOptionsPanel.class,
                                                       "S3ReaderOptionsPanel.slstrL1BPixelGeocodingsCheckBox.text")); // NOI18N
        slstrL1BOrphanPixelsCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(slstrL1BOrphanPixelsCheckBox,
                                   NbBundle.getMessage(S3ReaderOptionsPanel.class,
                                                       "S3ReaderOptionsPanel.slstrL1BOrphanPixelsCheckBox.text")); // NOI18N
        slstrL1BCalibrationCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(slstrL1BCalibrationCheckBox,
                                   NbBundle.getMessage(S3ReaderOptionsPanel.class,
                                                       "S3ReaderOptionsPanel.slstrL1BCalibrationFactorCheckBox.text")); // NOI18N
        slstrL1BS3MPCRecommendationCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(slstrL1BS3MPCRecommendationCheckBox,
                                   NbBundle.getMessage(S3ReaderOptionsPanel.class,
                                                       "S3ReaderOptionsPanel.slstrL1BS3MPCRecommendationCheckBox.text")); // NOI18N
        slstrL2SSTPixelGeocodingsCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(slstrL2SSTPixelGeocodingsCheckBox,
                                   NbBundle.getMessage(S3ReaderOptionsPanel.class,
                                                       "S3ReaderOptionsPanel.slstrL2SSTPixelGeocodingsCheckBox.text")); // NOI18N
        olciPixelGeocodingsCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(olciPixelGeocodingsCheckBox,
                                   NbBundle.getMessage(S3ReaderOptionsPanel.class,
                                                       "S3ReaderOptionsPanel.olciPixelGeocodingsCheckBox.text")); // NOI18N
        olciL1CalibrationCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(olciL1CalibrationCheckBox,
                                   NbBundle.getMessage(S3ReaderOptionsPanel.class,
                                                       "S3ReaderOptionsPanel.olciL1CalibrationCheckBox.text")); // NOI18N
        merisPixelGeocodingsCheckBox = new JCheckBox();
        Mnemonics.setLocalizedText(merisPixelGeocodingsCheckBox,
                                   NbBundle.getMessage(S3ReaderOptionsPanel.class,
                                                       "S3ReaderOptionsPanel.merisPixelGeocodingsCheckBox.text")); // NOI18N

        JButton slstrLevel1bCalibrationEditButton = new JButton("Edit");
        slstrLevel1bCalibrationEditButton.setName("Sentinel-S3 SLSTR L1B Custom Calibration");
        slstrLevel1bCalibrationEditButton.addActionListener(e -> {
            openEditCalibrationDialog(e, CalibrationUtils.PRODUCT_TYPE.SLSTRL1B);
        });

        JPanel slstrL1BCalibrationPanel = new JPanel();
        slstrL1BCalibrationPanel.setLayout(new BoxLayout(slstrL1BCalibrationPanel, BoxLayout.X_AXIS));
        slstrL1BCalibrationPanel.add(slstrL1BCalibrationCheckBox);
        slstrL1BCalibrationPanel.add(Box.createHorizontalStrut(10));
        slstrL1BCalibrationPanel.add(slstrLevel1bCalibrationEditButton);

        JButton olciL1CalibrationEditButton = new JButton("Edit");
        olciL1CalibrationEditButton.setName("Sentinel-S3 OLCI L1 Custom Calibration");
        olciL1CalibrationEditButton.addActionListener(e -> {
            openEditCalibrationDialog(e, CalibrationUtils.PRODUCT_TYPE.OLCIL1);
        });

        JPanel olciL1CalibrationPanel = new JPanel();
        olciL1CalibrationPanel.setLayout(new BoxLayout(olciL1CalibrationPanel, BoxLayout.X_AXIS));
        olciL1CalibrationPanel.add(olciL1CalibrationCheckBox);
        olciL1CalibrationPanel.add(Box.createHorizontalStrut(10));
        olciL1CalibrationPanel.add(olciL1CalibrationEditButton);

        JPanel slstrLabel = PreferenceUtils.createTitleLabel("SLSTR");
        JPanel olciLabel = PreferenceUtils.createTitleLabel("OLCI");
        JPanel merisLabel = PreferenceUtils.createTitleLabel("MERIS");
        JLabel commentLabel = new JLabel("<html><b>NOTE:</b> For configuring the behaviour of geo-coding, please \n" +
                                                 "have also a look at the general Geo-Location panel, too.");
        final JSeparator separator = new JSeparator();

        TableLayout tableLayout = new TableLayout(1);
        tableLayout.setTableAnchor(TableLayout.Anchor.NORTHWEST);
        tableLayout.setTablePadding(new Insets(4, 10, 0, 0));
        tableLayout.setTableFill(TableLayout.Fill.BOTH);
        tableLayout.setColumnWeightX(0, 1.0);
        this.setLayout(tableLayout);

        add(slstrLabel);
        add(slstrL1BPixelGeocodingsCheckBox);
        add(slstrL1BOrphanPixelsCheckBox);
        add(slstrL1BCalibrationPanel);
        add(slstrL1BS3MPCRecommendationCheckBox);
        add(slstrL2SSTPixelGeocodingsCheckBox);
        add(slstrL2SSTPixelGeocodingsCheckBox);
        add(olciLabel);
        add(olciPixelGeocodingsCheckBox);
        add(olciL1CalibrationPanel);
        add(merisLabel);
        add(merisPixelGeocodingsCheckBox);
        add(separator);
        add(commentLabel);
    }

    void load() {
        final Preferences preferences = Config.instance("opttbx").load().preferences();
        slstrL1BPixelGeocodingsCheckBox.setSelected(
                preferences.getBoolean(SlstrLevel1ProductFactory.SLSTR_L1B_USE_PIXELGEOCODINGS, true));
        slstrL1BOrphanPixelsCheckBox.setSelected(
                preferences.getBoolean(SlstrLevel1ProductFactory.SLSTR_L1B_LOAD_ORPHAN_PIXELS, false));
        slstrL1BCalibrationCheckBox.setSelected(
                preferences.getBoolean(SlstrLevel1ProductFactory.SLSTR_L1B_CUSTOM_CALIBRATION, false));
        slstrL1BS3MPCRecommendationCheckBox.setSelected(
                preferences.getBoolean(SlstrLevel1ProductFactory.SLSTR_L1B_S3MPC_CALIBRATION, false));
        slstrL2SSTPixelGeocodingsCheckBox.setSelected(
                preferences.getBoolean(SlstrSstProductFactory.SLSTR_L2_SST_USE_PIXELGEOCODINGS, true));
        // @todo 2 tb/tb invent something more clever here 2025-04-03
        final OlciContext olciContext = new OlciContext();
        olciPixelGeocodingsCheckBox.setSelected(
                preferences.getBoolean(olciContext.getUsePixelGeoCodingKey(), true));
        olciL1CalibrationCheckBox.setSelected(
                preferences.getBoolean(olciContext.getCustomCalibrationKey(), false));
        merisPixelGeocodingsCheckBox.setSelected(
                preferences.getBoolean(MerisProductFactory.MERIS_SAFE_USE_PIXELGEOCODING, true));
    }

    void store() {
        final Preferences preferences = Config.instance("opttbx").load().preferences();
        preferences.putBoolean(SlstrLevel1ProductFactory.SLSTR_L1B_USE_PIXELGEOCODINGS,
                               slstrL1BPixelGeocodingsCheckBox.isSelected());
        preferences.putBoolean(SlstrLevel1ProductFactory.SLSTR_L1B_LOAD_ORPHAN_PIXELS,
                               slstrL1BOrphanPixelsCheckBox.isSelected());
        preferences.putBoolean(SlstrLevel1ProductFactory.SLSTR_L1B_CUSTOM_CALIBRATION,
                               slstrL1BCalibrationCheckBox.isSelected());
        preferences.putBoolean(SlstrLevel1ProductFactory.SLSTR_L1B_S3MPC_CALIBRATION,
                               slstrL1BS3MPCRecommendationCheckBox.isSelected());
        preferences.putBoolean(SlstrSstProductFactory.SLSTR_L2_SST_USE_PIXELGEOCODINGS,
                               slstrL2SSTPixelGeocodingsCheckBox.isSelected());
        // @todo 1 tb/tb move to factory for contexts 2025-04-03
        final OlciContext olciContext = new OlciContext();
        preferences.putBoolean(olciContext.getUsePixelGeoCodingKey(), olciPixelGeocodingsCheckBox.isSelected());
        preferences.putBoolean(olciContext.getCustomCalibrationKey(), olciL1CalibrationCheckBox.isSelected());
        preferences.putBoolean(MerisProductFactory.MERIS_SAFE_USE_PIXELGEOCODING, merisPixelGeocodingsCheckBox.isSelected());
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            SnapApp.getDefault().getLogger().severe(e.getMessage());
        }
    }

    boolean valid() {
        // Check whether form is consistent and complete
        return true;
    }

    private void openEditCalibrationDialog(ActionEvent e, CalibrationUtils.PRODUCT_TYPE productType) {
        JButton button =(JButton) e.getSource();
        String title = button.getName();
        Preferences preferences = Config.instance("opttbx").load().preferences();

        S3CustomCalibrationWindow calibrationWindow = new S3CustomCalibrationWindow(SwingUtilities.getWindowAncestor(this), title, ModalDialog.ID_OK_CANCEL, null, preferences, productType);
        calibrationWindow.show();
    }
}
