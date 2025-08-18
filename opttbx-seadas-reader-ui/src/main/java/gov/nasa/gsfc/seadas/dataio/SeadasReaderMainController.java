/*
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package gov.nasa.gsfc.seadas.dataio;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.binding.ValidationException;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.BindingContext;
import com.bc.ceres.swing.binding.PropertyEditorRegistry;
import com.bc.ceres.swing.binding.PropertyPane;
import org.esa.snap.rcp.preferences.DefaultConfigController;
import org.esa.snap.rcp.preferences.Preference;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;

import javax.swing.*;
import java.awt.*;

/**
 * * Panel handling colorbar layer preferences. Sub-panel of the "Layer"-panel.
 *
 * @author Daniel Knowles
 */


@OptionsPanelController.SubRegistration(location = "OPTTBX",
        displayName = "#Options_DisplayName_SeadasL2ReaderMain",
        keywords = "#Options_Keywords_SeadasL2ReaderMain",
        keywordsCategory = "Processors",
        id = "SeaDAS-File-Readers")
@org.openide.util.NbBundle.Messages({
        "Options_DisplayName_SeadasL2ReaderMain=SeaDAS File Readers",
        "Options_Keywords_SeadasL2ReaderMain=level-2, level-3, level-1B, level-1C"
})
public final class SeadasReaderMainController extends DefaultConfigController {

    Property restoreDefaults;

    boolean propertyValueChangeEventsEnabled = true;


    protected PropertySet createPropertySet() {
        return createPropertySet(new SeadasToolboxBean());
    }


    // Property Setting: Restore Defaults

    private static final String PROPERTY_RESTORE_KEY_SUFFIX = SeadasReaderDefaults.PROPERTY_LEVEL_MASKS_ROOT_KEY + ".restore.defaults";

    public static final String PROPERTY_RESTORE_SECTION_KEY = PROPERTY_RESTORE_KEY_SUFFIX + ".section";
    public static final String PROPERTY_RESTORE_SECTION_LABEL = "Restore";
    public static final String PROPERTY_RESTORE_SECTION_TOOLTIP = "Restores preferences to the package defaults";

    public static final String PROPERTY_RESTORE_DEFAULTS_KEY = PROPERTY_RESTORE_KEY_SUFFIX + ".apply";
    public static final String PROPERTY_RESTORE_DEFAULTS_LABEL = "Default (SeaDAS Reader Preferences)";
    public static final String PROPERTY_RESTORE_DEFAULTS_TOOLTIP = "Restore all band properties preferences to the original default";
    public static final boolean PROPERTY_RESTORE_DEFAULTS_DEFAULT = false;



    @Override
    protected JPanel createPanel(BindingContext context) {

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_COMMON_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_KEY, SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L3_MAPPED_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L3_MAPPED_BAND_GROUPING_KEY, SeadasReaderDefaults.PROPERTY_L3_MAPPED_BAND_GROUPING_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L3_MAPPED_FLIPX_KEY, SeadasReaderDefaults.PROPERTY_L3_MAPPED_FLIPX_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L3_MAPPED_FLIPY_KEY, SeadasReaderDefaults.PROPERTY_L3_MAPPED_FLIPY_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_LEVEL2_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_LEVEL2_BAND_GROUPING_KEY, SeadasReaderDefaults.PROPERTY_LEVEL2_BAND_GROUPING_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_LEVEL2_FLIPX_KEY, SeadasReaderDefaults.PROPERTY_LEVEL2_FLIPX_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_LEVEL2_FLIPY_KEY, SeadasReaderDefaults.PROPERTY_LEVEL2_FLIPY_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1B_PACE_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1B_PACE_BAND_GROUPING_KEY, SeadasReaderDefaults.PROPERTY_L1B_PACE_BAND_GROUPING_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1B_PACE_FLIPX_KEY, SeadasReaderDefaults.PROPERTY_L1B_PACE_FLIPX_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1B_PACE_FLIPY_KEY, SeadasReaderDefaults.PROPERTY_L1B_PACE_FLIPY_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1C_PACE_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1C_PACE_OCI_BAND_GROUPING_KEY, SeadasReaderDefaults.PROPERTY_L1C_PACE_OCI_BAND_GROUPING_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1C_PACE_HARP2_BAND_GROUPING_KEY, SeadasReaderDefaults.PROPERTY_L1C_PACE_HARP2_BAND_GROUPING_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1C_PACE_SPEXONE_BAND_GROUPING_KEY, SeadasReaderDefaults.PROPERTY_L1C_PACE_SPEXONE_BAND_GROUPING_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1C_PACE_FLIPX_KEY, SeadasReaderDefaults.PROPERTY_L1C_PACE_FLIPX_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1C_PACE_FLIPY_KEY, SeadasReaderDefaults.PROPERTY_L1C_PACE_FLIPY_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1B_MODIS_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1B_MODIS_BAND_GROUPING_KEY, SeadasReaderDefaults.PROPERTY_L1B_MODIS_BAND_GROUPING_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1B_MODIS_FLIPX_KEY, SeadasReaderDefaults.PROPERTY_L1B_MODIS_FLIPX_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1B_MODIS_FLIPY_KEY, SeadasReaderDefaults.PROPERTY_L1B_MODIS_FLIPY_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1B_VIIRS_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1B_VIIRS_BAND_GROUPING_KEY, SeadasReaderDefaults.PROPERTY_L1B_VIIRS_BAND_GROUPING_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1B_VIIRS_FLIPX_KEY, SeadasReaderDefaults.PROPERTY_L1B_VIIRS_FLIPX_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_L1B_VIIRS_FLIPY_KEY, SeadasReaderDefaults.PROPERTY_L1B_VIIRS_FLIPY_DEFAULT);


        initPropertyDefaults(context, PROPERTY_RESTORE_SECTION_KEY, true);
        restoreDefaults =  initPropertyDefaults(context, PROPERTY_RESTORE_DEFAULTS_KEY, PROPERTY_RESTORE_DEFAULTS_DEFAULT);



        //
        // Create UI
        //

        TableLayout tableLayout = new TableLayout(2);
        tableLayout.setTableAnchor(TableLayout.Anchor.NORTHWEST);
        tableLayout.setTablePadding(new Insets(4, 10, 0, 0));
        tableLayout.setTableFill(TableLayout.Fill.BOTH);
        tableLayout.setColumnWeightX(1, 1.0);

        JPanel pageUI = new JPanel(tableLayout);

        PropertyEditorRegistry registry = PropertyEditorRegistry.getInstance();

        PropertySet propertyContainer = context.getPropertySet();
        Property[] properties = propertyContainer.getProperties();

        int currRow = 0;
        for (Property property : properties) {
            PropertyDescriptor descriptor = property.getDescriptor();
            PropertyPane.addComponent(currRow, tableLayout, pageUI, context, registry, descriptor);
            currRow++;
        }

        pageUI.add(tableLayout.createVerticalSpacer());

        JPanel parent = new JPanel(new BorderLayout());
        parent.add(pageUI, BorderLayout.CENTER);
        parent.add(Box.createHorizontalStrut(50), BorderLayout.EAST);
        return parent;
    }


    @Override
    protected void configure(BindingContext context) {

        // Handle resetDefaults events - set all other components to defaults
        restoreDefaults.addPropertyChangeListener(evt -> {
            handleRestoreDefaults(context);
        });





        // Add listeners to all components in order to uncheck restoreDefaults checkbox accordingly

        PropertySet propertyContainer = context.getPropertySet();
        Property[] properties = propertyContainer.getProperties();

        for (Property property : properties) {
            if (property != restoreDefaults) {
                property.addPropertyChangeListener(evt -> {
                    handlePreferencesPropertyValueChange(context);
                });
            }
        }

        // This call is an initialization call which set restoreDefault initial value
        handlePreferencesPropertyValueChange(context);
//        handleBandGroupingChanged(context);
    }






    /**
     * Test all properties to determine whether the current value is the default value
     *
     * @param context
     * @return
     * @author Daniel Knowles
     */
    private boolean isDefaults(BindingContext context) {

        PropertySet propertyContainer = context.getPropertySet();
        Property[] properties = propertyContainer.getProperties();

        for (Property property : properties) {
            if (property != restoreDefaults && property.getDescriptor().getDefaultValue() != null)
                if (!property.getValue().equals(property.getDescriptor().getDefaultValue())) {
                    return false;
                }
        }

        return true;
    }


    /**
     * Handles the restore defaults action
     *
     * @param context
     * @author Daniel Knowles
     */
    private void handleRestoreDefaults(BindingContext context) {
        if (propertyValueChangeEventsEnabled) {
            propertyValueChangeEventsEnabled = false;
            try {
                if (restoreDefaults.getValue()) {

                    PropertySet propertyContainer = context.getPropertySet();
                    Property[] properties = propertyContainer.getProperties();

                    for (Property property : properties) {
                        if (property != restoreDefaults && property.getDescriptor().getDefaultValue() != null)
                            property.setValue(property.getDescriptor().getDefaultValue());
                    }
                }
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            propertyValueChangeEventsEnabled = true;

            context.setComponentsEnabled(PROPERTY_RESTORE_DEFAULTS_KEY, false);
        }
    }







    /**
     * Set restoreDefault component because a property has changed
     * @param context
     * @author Daniel Knowles
     */
    private void handlePreferencesPropertyValueChange(BindingContext context) {
        if (propertyValueChangeEventsEnabled) {
            propertyValueChangeEventsEnabled = false;
            try {
                restoreDefaults.setValue(isDefaults(context));
                context.setComponentsEnabled(PROPERTY_RESTORE_DEFAULTS_KEY, !isDefaults(context));
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            propertyValueChangeEventsEnabled = true;
        }
    }


    /**
     * Initialize the property descriptor default value
     *
     * @param context
     * @param propertyName
     * @param propertyDefault
     * @return
     * @author Daniel Knowles
     */
    private Property initPropertyDefaults(BindingContext context, String propertyName, Object propertyDefault) {

        System.out.println("propertyName=" + propertyName);

        if (context == null) {
            System.out.println("WARNING: context is null");
        }

        Property property = context.getPropertySet().getProperty(propertyName);
        if (property == null) {
            System.out.println("WARNING: property is null");
        }

        property.getDescriptor().setDefaultValue(propertyDefault);

        return property;
    }

    // todo add a help page ... see the ColorBarLayerController for example

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("preferencesSeaDASReaders");
    }

    @SuppressWarnings("UnusedDeclaration")
    static class SeadasToolboxBean {


        @Preference(key = SeadasReaderDefaults.PROPERTY_COMMON_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_COMMON_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_COMMON_SECTION_TOOLTIP)
        boolean validPixelSection = true;


        @Preference(key = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_KEY,
                label = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_LABEL,
                description = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_TOOLTIP,
                valueSet = {SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_EXACT,
                        "5 (Significant Figures)",
                        "6 (Significant Figures)",
                        SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_DEFAULT,
                        "8 (Significant Figures)",
                        "9 (Significant Figures)",
                        "10 (Significant Figures)",
                        "11 (Significant Figures)",
                        "12 (Significant Figures)",
                        "13 (Significant Figures)",
                        "14 (Significant Figures)",
                        "15 (Significant Figures)",
                        "16 (Significant Figures)"
                })
//                interval = "[3,32]")
        String propertyValidPixelSigFigsDefault = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_DEFAULT;




        @Preference(key = SeadasReaderDefaults.PROPERTY_L3_MAPPED_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_L3_MAPPED_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L3_MAPPED_SECTION_TOOLTIP)
        boolean l3MappedSection = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L3_MAPPED_BAND_GROUPING_KEY,
                label = SeadasReaderDefaults.PROPERTY_L3_MAPPED_BAND_GROUPING_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L3_MAPPED_BAND_GROUPING_TOOLTIP)
        String l3MappedBandGroupingDefault = SeadasReaderDefaults.PROPERTY_L3_MAPPED_BAND_GROUPING_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L3_MAPPED_FLIPX_KEY,
                label = SeadasReaderDefaults.PROPERTY_L3_MAPPED_FLIPX_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L3_MAPPED_FLIPX_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_NO_L3, SeadasReaderDefaults.FlIP_YES})
        String l3MappedFlipxDefault = SeadasReaderDefaults.PROPERTY_L3_MAPPED_FLIPX_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L3_MAPPED_FLIPY_KEY,
                label = SeadasReaderDefaults.PROPERTY_L3_MAPPED_FLIPY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L3_MAPPED_FLIPY_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_NO_L3, SeadasReaderDefaults.FlIP_YES})
        String l3MappedFlipyDefault = SeadasReaderDefaults.PROPERTY_L3_MAPPED_FLIPY_DEFAULT;






        @Preference(key = SeadasReaderDefaults.PROPERTY_LEVEL2_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_LEVEL2_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_LEVEL2_SECTION_TOOLTIP)
        boolean level2Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_LEVEL2_BAND_GROUPING_KEY,
                label = SeadasReaderDefaults.PROPERTY_LEVEL2_BAND_GROUPING_LABEL,
                description = SeadasReaderDefaults.PROPERTY_LEVEL2_BAND_GROUPING_TOOLTIP)
        String level2BandGroupingDefault = SeadasReaderDefaults.PROPERTY_LEVEL2_BAND_GROUPING_DEFAULT;


        @Preference(key = SeadasReaderDefaults.PROPERTY_LEVEL2_FLIPX_KEY,
                label = SeadasReaderDefaults.PROPERTY_LEVEL2_FLIPX_LABEL,
                description = SeadasReaderDefaults.PROPERTY_LEVEL2_FLIPX_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_NO, SeadasReaderDefaults.FlIP_MISSION_DEFAULT, SeadasReaderDefaults.FlIP_YES})
        String level2FlipxDefault = SeadasReaderDefaults.PROPERTY_LEVEL2_FLIPX_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_LEVEL2_FLIPY_KEY,
                label = SeadasReaderDefaults.PROPERTY_LEVEL2_FLIPY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_LEVEL2_FLIPY_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_NO, SeadasReaderDefaults.FlIP_MISSION_DEFAULT, SeadasReaderDefaults.FlIP_YES})
        String level2FlipyDefault = SeadasReaderDefaults.PROPERTY_LEVEL2_FLIPY_DEFAULT;




        @Preference(key = SeadasReaderDefaults.PROPERTY_L1C_PACE_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1C_PACE_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1C_PACE_SECTION_TOOLTIP)
        boolean l1cPaceSection = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L1C_PACE_OCI_BAND_GROUPING_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1C_PACE_OCI_BAND_GROUPING_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1C_PACE_OCI_BAND_GROUPING_TOOLTIP)
        String l1cPaceOciBandGroupingDefault = SeadasReaderDefaults.PROPERTY_L1C_PACE_OCI_BAND_GROUPING_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L1C_PACE_HARP2_BAND_GROUPING_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1C_PACE_HARP2_BAND_GROUPING_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1C_PACE_HARP2_BAND_GROUPING_TOOLTIP)
        String l1cPaceHarp2BandGroupingDefault = SeadasReaderDefaults.PROPERTY_L1C_PACE_HARP2_BAND_GROUPING_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L1C_PACE_SPEXONE_BAND_GROUPING_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1C_PACE_SPEXONE_BAND_GROUPING_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1C_PACE_SPEXONE_BAND_GROUPING_TOOLTIP)
        String l1cPaceSpexoneBandGroupingDefault = SeadasReaderDefaults.PROPERTY_L1C_PACE_SPEXONE_BAND_GROUPING_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L1C_PACE_FLIPX_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1C_PACE_FLIPX_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1C_PACE_FLIPX_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_NO, SeadasReaderDefaults.FlIP_MISSION_DEFAULT, SeadasReaderDefaults.FlIP_YES})
        String l1cPaceFlipxDefault = SeadasReaderDefaults.PROPERTY_L1C_PACE_FLIPX_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L1C_PACE_FLIPY_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1C_PACE_FLIPY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1C_PACE_FLIPY_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_NO, SeadasReaderDefaults.FlIP_MISSION_DEFAULT, SeadasReaderDefaults.FlIP_YES})
        String l1cPaceFlipyDefault = SeadasReaderDefaults.PROPERTY_L1C_PACE_FLIPY_DEFAULT;






        @Preference(key = SeadasReaderDefaults.PROPERTY_L1B_PACE_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1B_PACE_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1B_PACE_SECTION_TOOLTIP)
        boolean l1bPaceSection = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L1B_PACE_BAND_GROUPING_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1B_PACE_BAND_GROUPING_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1B_PACE_BAND_GROUPING_TOOLTIP)
        String l1bPaceBandGroupingDefault = SeadasReaderDefaults.PROPERTY_L1B_PACE_BAND_GROUPING_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L1B_PACE_FLIPX_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1B_PACE_FLIPX_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1B_PACE_FLIPX_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_NO, SeadasReaderDefaults.FlIP_MISSION_DEFAULT, SeadasReaderDefaults.FlIP_YES})
        String l1bPaceFlipxDefault = SeadasReaderDefaults.PROPERTY_L1B_PACE_FLIPX_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L1B_PACE_FLIPY_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1B_PACE_FLIPY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1B_PACE_FLIPY_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_NO, SeadasReaderDefaults.FlIP_MISSION_DEFAULT, SeadasReaderDefaults.FlIP_YES})
        String l1bPaceFlipyDefault = SeadasReaderDefaults.PROPERTY_L1B_PACE_FLIPY_DEFAULT;









        @Preference(key = SeadasReaderDefaults.PROPERTY_L1B_MODIS_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1B_MODIS_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1B_MODIS_SECTION_TOOLTIP)
        boolean l1bModisSection = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L1B_MODIS_BAND_GROUPING_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1B_MODIS_BAND_GROUPING_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1B_MODIS_BAND_GROUPING_TOOLTIP)
        String l1bModisBandGroupingDefault = SeadasReaderDefaults.PROPERTY_L1B_MODIS_BAND_GROUPING_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L1B_MODIS_FLIPX_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1B_MODIS_FLIPX_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1B_MODIS_FLIPX_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_NO, SeadasReaderDefaults.FlIP_MISSION_DEFAULT, SeadasReaderDefaults.FlIP_YES})
        String l1bModisFlipxDefault = SeadasReaderDefaults.PROPERTY_L1B_MODIS_FLIPX_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L1B_MODIS_FLIPY_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1B_MODIS_FLIPY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1B_MODIS_FLIPY_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_NO, SeadasReaderDefaults.FlIP_MISSION_DEFAULT, SeadasReaderDefaults.FlIP_YES})
        String l1bModisFlipyDefault = SeadasReaderDefaults.PROPERTY_L1B_MODIS_FLIPY_DEFAULT;





        @Preference(key = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_SECTION_TOOLTIP)
        boolean l1bViirsSection = true;


        @Preference(key = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_BAND_GROUPING_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_BAND_GROUPING_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_BAND_GROUPING_TOOLTIP)
        String l1bViirsBandGroupingDefault = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_BAND_GROUPING_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_FLIPX_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_FLIPX_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_FLIPX_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_NO, SeadasReaderDefaults.FlIP_MISSION_DEFAULT, SeadasReaderDefaults.FlIP_YES})
        String l1bViirsFlipxDefault = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_FLIPX_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_FLIPY_KEY,
                label = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_FLIPY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_FLIPY_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_NO, SeadasReaderDefaults.FlIP_MISSION_DEFAULT, SeadasReaderDefaults.FlIP_YES})
        String l1bViirsFlipyDefault = SeadasReaderDefaults.PROPERTY_L1B_VIIRS_FLIPY_DEFAULT;



        // Restore Defaults Section

        @Preference(label = PROPERTY_RESTORE_SECTION_LABEL,
                key = PROPERTY_RESTORE_SECTION_KEY,
                description = PROPERTY_RESTORE_SECTION_TOOLTIP)
        boolean restoreDefaultsSection = true;

        @Preference(label = PROPERTY_RESTORE_DEFAULTS_LABEL,
                key = PROPERTY_RESTORE_DEFAULTS_KEY,
                description = PROPERTY_RESTORE_DEFAULTS_TOOLTIP)
        boolean restoreDefaults = PROPERTY_RESTORE_DEFAULTS_DEFAULT;

    }

}
