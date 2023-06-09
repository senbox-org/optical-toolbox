package eu.esa.opt.dataio.s2.preferences.ui.msi;

import org.esa.snap.rcp.SnapApp;
import org.esa.snap.runtime.Config;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

import javax.swing.JLabel;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by obarrile on 27/06/2016.
 * Updated by Florian Douziech on 20 10 2021
 */
public class S2ReaderPanel extends javax.swing.JPanel {

    private javax.swing.JCheckBox detectorFootprintMasks;
    private javax.swing.JCheckBox radiometricQualityMasks;
    private javax.swing.JCheckBox technicalQualityMasks;
    private javax.swing.JCheckBox cloudMasks;
    private javax.swing.JCheckBox classificationMasks;
    private javax.swing.JCheckBox ECMWFTData;
    private javax.swing.JCheckBox CAMSData;
    private javax.swing.JCheckBox addNegativeOffset;

    S2ReaderPanel(final S2ReaderPanelController controller) {
        initComponents();

        detectorFootprintMasks.addItemListener(e -> controller.changed());
        radiometricQualityMasks.addItemListener(e -> controller.changed());
        technicalQualityMasks.addItemListener(e -> controller.changed());
        cloudMasks.addItemListener(e -> controller.changed());
        classificationMasks.addItemListener(e -> controller.changed());
        ECMWFTData.addItemListener(e -> controller.changed());
        CAMSData.addItemListener(e -> controller.changed());
        addNegativeOffset.addItemListener(e -> controller.changed());
    }

    private void initComponents() {

        detectorFootprintMasks = new javax.swing.JCheckBox();
        Mnemonics.setLocalizedText(detectorFootprintMasks,
                                   NbBundle.getMessage(S2ReaderPanel.class,
                                                       "S2TBXReaderOptionsPanel.detectorFootprintMasks.text")); // NOI18N
        radiometricQualityMasks = new javax.swing.JCheckBox();
        Mnemonics.setLocalizedText(radiometricQualityMasks,
                                   NbBundle.getMessage(S2ReaderPanel.class,
                                                       "S2TBXReaderOptionsPanel.radiometricQualityMasks.text")); // NOI18N
        technicalQualityMasks = new javax.swing.JCheckBox();
        Mnemonics.setLocalizedText(technicalQualityMasks,
                                   NbBundle.getMessage(S2ReaderPanel.class,
                                                       "S2TBXReaderOptionsPanel.technicalQualityMasks.text")); // NOI18N
        cloudMasks = new javax.swing.JCheckBox();
        Mnemonics.setLocalizedText(cloudMasks,
                                   NbBundle.getMessage(S2ReaderPanel.class,
                                                       "S2TBXReaderOptionsPanel.cloudMasks.text")); // NOI18N
        
        classificationMasks = new javax.swing.JCheckBox();
        Mnemonics.setLocalizedText(classificationMasks,
                                   NbBundle.getMessage(S2ReaderPanel.class,
                                                       "S2TBXReaderOptionsPanel.classificationMasks.text"));

        ECMWFTData = new javax.swing.JCheckBox();
        Mnemonics.setLocalizedText(ECMWFTData,
                                   NbBundle.getMessage(S2ReaderPanel.class,
                                                       "S2TBXReaderOptionsPanel.ECMWFTData.text"));
        CAMSData = new javax.swing.JCheckBox();
        Mnemonics.setLocalizedText(CAMSData,
                                   NbBundle.getMessage(S2ReaderPanel.class,
                                                       "S2TBXReaderOptionsPanel.CAMSData.text"));

        addNegativeOffset = new javax.swing.JCheckBox();
        Mnemonics.setLocalizedText(addNegativeOffset,
                                   NbBundle.getMessage(S2ReaderPanel.class,
                                                       "S2TBXReaderOptionsPanel.negativeRadiometricOffset.text"));
        JLabel titleMask = new JLabel("Sentinel-2 masks");
        JLabel titleAUX = new JLabel("Sentinel-2 auxilary data");
        JLabel titleOffset = new JLabel("Negative radiometric offset (L1C-L2A)");
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                            .addComponent(titleMask)
                                                            // .addComponent(sep1)
                                                            .addGap(0, 512, Short.MAX_VALUE)
                                                            .addComponent(detectorFootprintMasks)
                                                            .addGap(0, 512, Short.MAX_VALUE)
                                                            .addComponent(radiometricQualityMasks)
                                                            .addGap(0, 512, Short.MAX_VALUE)
                                                            .addComponent(technicalQualityMasks)
                                                            .addGap(0, 512, Short.MAX_VALUE)
                                                            .addComponent(cloudMasks)
                                                            .addGap(0, 512, Short.MAX_VALUE)
                                                            .addComponent(classificationMasks)
                                                            .addGap(0, 512, Short.MAX_VALUE)
                                                            .addComponent(titleAUX)
                                                            .addGap(0, 512, Short.MAX_VALUE)
                                                            .addComponent(ECMWFTData)
                                                            .addGap(0, 512, Short.MAX_VALUE)
                                                            .addComponent(CAMSData)
                                                            .addGap(0, 512, Short.MAX_VALUE)
                                                            .addComponent(titleOffset)
                                                            .addGap(0, 512, Short.MAX_VALUE)
                                                            .addComponent(addNegativeOffset))
                                          .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                          .addComponent(titleMask)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addGap(0, 10,10)
                                          .addComponent(detectorFootprintMasks)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(radiometricQualityMasks)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(technicalQualityMasks)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(cloudMasks)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(classificationMasks)
                                          .addGap(0, 10,10)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(titleAUX)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(ECMWFTData)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(CAMSData)
                                          .addGap(0, 10,10)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(titleOffset)
                                          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                          .addComponent(addNegativeOffset)
                                          .addContainerGap())
        );
    }

    void load() {
        final Preferences preferences = Config.instance("opttbx").load().preferences();

        detectorFootprintMasks.setSelected(
                preferences.getBoolean("opttbx.dataio.s2msi.detectorFootprintMasks", true));
        radiometricQualityMasks.setSelected(
                preferences.getBoolean("opttbx.dataio.s2msi.radiometricQualityMasks", true));
        technicalQualityMasks.setSelected(
                preferences.getBoolean("opttbx.dataio.s2msi.technicalQualityMasks", true));
        cloudMasks.setSelected(
                preferences.getBoolean("opttbx.dataio.s2msi.cloudMasks", true));
        classificationMasks.setSelected(
                preferences.getBoolean("opttbx.dataio.s2msi.classificationMasks", true));
        ECMWFTData.setSelected(
                preferences.getBoolean("opttbx.dataio.s2msi.ECMWFTData", true));
        CAMSData.setSelected(
                preferences.getBoolean("opttbx.dataio.s2msi.CAMSData", true));
        addNegativeOffset.setSelected(
                preferences.getBoolean("opttbx.dataio.s2msi.negativeRadiometricOffset", false));
    }

    void store() {
        final Preferences preferences = Config.instance("opttbx").load().preferences();

        preferences.putBoolean("opttbx.dataio.s2msi.detectorFootprintMasks", detectorFootprintMasks.isSelected());
        preferences.putBoolean("opttbx.dataio.s2msi.radiometricQualityMasks", radiometricQualityMasks.isSelected());
        preferences.putBoolean("opttbx.dataio.s2msi.technicalQualityMasks", technicalQualityMasks.isSelected());
        preferences.putBoolean("opttbx.dataio.s2msi.cloudMasks", cloudMasks.isSelected());
        preferences.putBoolean("opttbx.dataio.s2msi.classificationMasks", classificationMasks.isSelected());
        preferences.putBoolean("opttbx.dataio.s2msi.ECMWFTData", ECMWFTData.isSelected());
        preferences.putBoolean("opttbx.dataio.s2msi.CAMSData", CAMSData.isSelected());
        preferences.putBoolean("opttbx.dataio.s2msi.negativeRadiometricOffset", addNegativeOffset.isSelected());

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
}
