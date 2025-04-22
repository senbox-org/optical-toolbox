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
        "Options_Keywords_SeadasL2ReaderMain=seadas, ocssw, l2gen"
})
public final class SeadasReaderMainController extends DefaultConfigController {

    Property restoreDefaults;

    Property bandGrouping;
    Property bandGroupingReset;

    boolean propertyValueChangeEventsEnabled = true;


    protected PropertySet createPropertySet() {
        return createPropertySet(new SeadasToolboxBean());
    }



    @Override
    protected JPanel createPanel(BindingContext context) {


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L3_MAPPED_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L3_MAPPED_KEY, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L3_MAPPED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FLIPX_L3_MAPPED_KEY, SeadasReaderDefaults.PROPERTY_FLIPX_L3_MAPPED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FLIPY_L3_MAPPED_KEY, SeadasReaderDefaults.PROPERTY_FLIPY_L3_MAPPED_DEFAULT);

        
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_LEVEL2_SECTION_KEY, true);
        bandGrouping = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_LEVEL2_KEY, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_LEVEL2_DEFAULT);
//        bandGroupingReset = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_RESET_KEY, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_RESET_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FLIPX_LEVEL2_KEY, SeadasReaderDefaults.PROPERTY_FLIPX_LEVEL2_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FLIPY_LEVEL2_KEY, SeadasReaderDefaults.PROPERTY_FLIPY_LEVEL2_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1B_PACE_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_PACE_KEY, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_PACE_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FLIPX_L1B_PACE_KEY, SeadasReaderDefaults.PROPERTY_FLIPX_L1B_PACE_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FLIPY_L1B_PACE_KEY, SeadasReaderDefaults.PROPERTY_FLIPY_L1B_PACE_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1C_PACE_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_KEY, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FLIPX_L1C_PACE_KEY, SeadasReaderDefaults.PROPERTY_FLIPX_L1C_PACE_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FLIPY_L1C_PACE_KEY, SeadasReaderDefaults.PROPERTY_FLIPY_L1C_PACE_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_HARP2_KEY, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_HARP2_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1B_MODIS_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_MODIS_KEY, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_MODIS_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FLIPX_L1B_MODIS_KEY, SeadasReaderDefaults.PROPERTY_FLIPX_L1B_MODIS_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FLIPY_L1B_MODIS_KEY, SeadasReaderDefaults.PROPERTY_FLIPY_L1B_MODIS_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1B_VIIRS_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_VIIRS_KEY, SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_VIIRS_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FLIPX_L1B_VIIRS_KEY, SeadasReaderDefaults.PROPERTY_FLIPX_L1B_VIIRS_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_FLIPY_L1B_VIIRS_KEY, SeadasReaderDefaults.PROPERTY_FLIPY_L1B_VIIRS_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_RESTORE_SECTION_KEY, true);
        restoreDefaults =  initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_RESTORE_DEFAULTS_KEY, SeadasReaderDefaults.PROPERTY_RESTORE_DEFAULTS_DEFAULT);



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

//        bandGroupingReset.addPropertyChangeListener(evt -> {
//            handleResetBandGroupingToDefaults(context);
//        });
//
//        bandGrouping.addPropertyChangeListener(evt -> {
//            handleBandGroupingChanged(context);
//        });


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

            context.setComponentsEnabled(SeadasReaderDefaults.PROPERTY_RESTORE_DEFAULTS_KEY, false);
        }
    }







    // disabled but might want to use in future
    private void handleResetBandGroupingToDefaults(BindingContext context) {
        if (propertyValueChangeEventsEnabled) {
            propertyValueChangeEventsEnabled = false;
            try {
                if (bandGroupingReset.getValue()) {
                    bandGrouping.setValue(bandGrouping.getDescriptor().getDefaultValue());
                }
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            propertyValueChangeEventsEnabled = true;

            context.setComponentsEnabled(SeadasReaderDefaults.PROPERTY_BAND_GROUPING_RESET_KEY, false);
        }
    }

    // disabled but might want to use in future
    private void handleBandGroupingChanged(BindingContext context) {
        if (propertyValueChangeEventsEnabled) {
            propertyValueChangeEventsEnabled = false;
            boolean isDefault = false;
            if (bandGrouping.getValue().equals(bandGrouping.getDescriptor().getDefaultValue())) {
                isDefault = true;
            }

            try {
                bandGroupingReset.setValue(isDefault);
                context.setComponentsEnabled(SeadasReaderDefaults.PROPERTY_BAND_GROUPING_RESET_KEY, !isDefault);
            } catch (ValidationException e) {
                e.printStackTrace();
            }
            propertyValueChangeEventsEnabled = true;
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
                context.setComponentsEnabled(SeadasReaderDefaults.PROPERTY_RESTORE_DEFAULTS_KEY, !isDefaults(context));
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
        return new HelpCtx("seadasToolboxPreferences");
    }

    @SuppressWarnings("UnusedDeclaration")
    static class SeadasToolboxBean {


        @Preference(key = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L3_MAPPED_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L3_MAPPED_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L3_MAPPED_SECTION_TOOLTIP)
        boolean maskL3_MAPPED_Section = true;


        @Preference(key = SeadasReaderDefaults.PROPERTY_FLIPX_L3_MAPPED_KEY,
                label = SeadasReaderDefaults.PROPERTY_FLIPX_L3_MAPPED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FLIPX_L3_MAPPED_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_MISSION_DEFAULT,
                        SeadasReaderDefaults.FlIP_YES,
                        SeadasReaderDefaults.FlIP_NO})
        String flipxL3MappedDefault = SeadasReaderDefaults.PROPERTY_FLIPX_L3_MAPPED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_FLIPY_L3_MAPPED_KEY,
                label = SeadasReaderDefaults.PROPERTY_FLIPY_L3_MAPPED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FLIPY_L3_MAPPED_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_MISSION_DEFAULT,
                        SeadasReaderDefaults.FlIP_YES,
                        SeadasReaderDefaults.FlIP_NO})
        String flipyL3MappedDefault = SeadasReaderDefaults.PROPERTY_FLIPY_L3_MAPPED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L3_MAPPED_KEY,
                label = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L3_MAPPED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L3_MAPPED_TOOLTIP)
        String bandGroupingL3MappedDefault = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L3_MAPPED_DEFAULT;




        
        

        @Preference(key = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_LEVEL2_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_LEVEL2_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_LEVEL2_SECTION_TOOLTIP)
        boolean mask_BAND_GROUPING_Section = true;


        @Preference(key = SeadasReaderDefaults.PROPERTY_FLIPX_LEVEL2_KEY,
                label = SeadasReaderDefaults.PROPERTY_FLIPX_LEVEL2_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FLIPX_LEVEL2_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_MISSION_DEFAULT,
                        SeadasReaderDefaults.FlIP_YES,
                        SeadasReaderDefaults.FlIP_NO})
        String flipxDefault = SeadasReaderDefaults.PROPERTY_FLIPX_LEVEL2_DEFAULT;


        @Preference(key = SeadasReaderDefaults.PROPERTY_FLIPY_LEVEL2_KEY,
                label = SeadasReaderDefaults.PROPERTY_FLIPY_LEVEL2_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FLIPY_LEVEL2_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_MISSION_DEFAULT,
                        SeadasReaderDefaults.FlIP_YES,
                        SeadasReaderDefaults.FlIP_NO})
        String flipyDefault = SeadasReaderDefaults.PROPERTY_FLIPY_LEVEL2_DEFAULT;



        @Preference(key = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_LEVEL2_KEY,
                label = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_LEVEL2_LABEL,
                description = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_LEVEL2_TOOLTIP)
        String bandGroupingDefault = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_LEVEL2_DEFAULT;

//        @Preference(key = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_RESET_KEY,
//                label = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_RESET_LABEL,
//                description = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_RESET_TOOLTIP)
//        boolean bandGroupingResetDefault = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_RESET_DEFAULT;



        @Preference(key = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1C_PACE_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1C_PACE_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1C_PACE_SECTION_TOOLTIP)
        boolean maskL1C_PACE_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_FLIPX_L1C_PACE_KEY,
                label = SeadasReaderDefaults.PROPERTY_FLIPX_L1C_PACE_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FLIPX_L1C_PACE_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_MISSION_DEFAULT,
                        SeadasReaderDefaults.FlIP_YES,
                        SeadasReaderDefaults.FlIP_NO})
        String flipxL1CPaceDefault = SeadasReaderDefaults.PROPERTY_FLIPX_L1C_PACE_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_FLIPY_L1C_PACE_KEY,
                label = SeadasReaderDefaults.PROPERTY_FLIPY_L1C_PACE_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FLIPY_L1C_PACE_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_MISSION_DEFAULT,
                        SeadasReaderDefaults.FlIP_YES,
                        SeadasReaderDefaults.FlIP_NO})
        String flipyL1CPaceDefault = SeadasReaderDefaults.PROPERTY_FLIPY_L1C_PACE_DEFAULT;


        @Preference(key = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_KEY,
                label = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_LABEL,
                description = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_TOOLTIP)
        String bandGroupingL1CPaceDefault = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_DEFAULT;


        @Preference(key = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_HARP2_KEY,
                label = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_HARP2_LABEL,
                description = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_HARP2_TOOLTIP)
        String bandGroupingL1CPaceHarp2Default = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1C_PACE_HARP2_DEFAULT;





        @Preference(key = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1B_PACE_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1B_PACE_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1B_PACE_SECTION_TOOLTIP)
        boolean maskL1B_PACE_Section = true;


        @Preference(key = SeadasReaderDefaults.PROPERTY_FLIPX_L1B_PACE_KEY,
                label = SeadasReaderDefaults.PROPERTY_FLIPX_L1B_PACE_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FLIPX_L1B_PACE_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_MISSION_DEFAULT,
                        SeadasReaderDefaults.FlIP_YES,
                        SeadasReaderDefaults.FlIP_NO})
        String flipxL1bPaceDefault = SeadasReaderDefaults.PROPERTY_FLIPX_L1B_PACE_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_FLIPY_L1B_PACE_KEY,
                label = SeadasReaderDefaults.PROPERTY_FLIPY_L1B_PACE_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FLIPY_L1B_PACE_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_MISSION_DEFAULT,
                        SeadasReaderDefaults.FlIP_YES,
                        SeadasReaderDefaults.FlIP_NO})
        String flipyL1bPaceDefault = SeadasReaderDefaults.PROPERTY_FLIPY_L1B_PACE_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_PACE_KEY,
                label = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_PACE_LABEL,
                description = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_PACE_TOOLTIP)
        String bandGroupingL1bPaceDefault = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_PACE_DEFAULT;










        @Preference(key = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1B_MODIS_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1B_MODIS_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1B_MODIS_SECTION_TOOLTIP)
        boolean maskL1B_MODIS_Section = true;


        @Preference(key = SeadasReaderDefaults.PROPERTY_FLIPX_L1B_MODIS_KEY,
                label = SeadasReaderDefaults.PROPERTY_FLIPX_L1B_MODIS_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FLIPX_L1B_MODIS_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_MISSION_DEFAULT,
                        SeadasReaderDefaults.FlIP_YES,
                        SeadasReaderDefaults.FlIP_NO})
        String flipxL1B_MODISDefault = SeadasReaderDefaults.PROPERTY_FLIPX_L1B_MODIS_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_FLIPY_L1B_MODIS_KEY,
                label = SeadasReaderDefaults.PROPERTY_FLIPY_L1B_MODIS_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FLIPY_L1B_MODIS_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_MISSION_DEFAULT,
                        SeadasReaderDefaults.FlIP_YES,
                        SeadasReaderDefaults.FlIP_NO})
        String flipyL1B_MODISDefault = SeadasReaderDefaults.PROPERTY_FLIPY_L1B_MODIS_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_MODIS_KEY,
                label = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_MODIS_LABEL,
                description = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_MODIS_TOOLTIP)
        String bandGroupingL1B_MODISDefault = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_MODIS_DEFAULT;







        @Preference(key = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1B_VIIRS_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1B_VIIRS_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FILE_PROPERTIES_L1B_VIIRS_SECTION_TOOLTIP)
        boolean maskL1B_VIIRS_Section = true;


        @Preference(key = SeadasReaderDefaults.PROPERTY_FLIPX_L1B_VIIRS_KEY,
                label = SeadasReaderDefaults.PROPERTY_FLIPX_L1B_VIIRS_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FLIPX_L1B_VIIRS_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_MISSION_DEFAULT,
                        SeadasReaderDefaults.FlIP_YES,
                        SeadasReaderDefaults.FlIP_NO})
        String flipxL1B_VIIRSDefault = SeadasReaderDefaults.PROPERTY_FLIPX_L1B_VIIRS_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_FLIPY_L1B_VIIRS_KEY,
                label = SeadasReaderDefaults.PROPERTY_FLIPY_L1B_VIIRS_LABEL,
                description = SeadasReaderDefaults.PROPERTY_FLIPY_L1B_VIIRS_TOOLTIP,
                valueSet = {SeadasReaderDefaults.FlIP_MISSION_DEFAULT,
                        SeadasReaderDefaults.FlIP_YES,
                        SeadasReaderDefaults.FlIP_NO})
        String flipyL1B_VIIRSDefault = SeadasReaderDefaults.PROPERTY_FLIPY_L1B_VIIRS_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_VIIRS_KEY,
                label = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_VIIRS_LABEL,
                description = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_VIIRS_TOOLTIP)
        String bandGroupingL1B_VIIRSDefault = SeadasReaderDefaults.PROPERTY_BAND_GROUPING_L1B_VIIRS_DEFAULT;


        // Restore Defaults Section

        @Preference(label = SeadasReaderDefaults.PROPERTY_RESTORE_SECTION_LABEL,
                key = SeadasReaderDefaults.PROPERTY_RESTORE_SECTION_KEY,
                description = SeadasReaderDefaults.PROPERTY_RESTORE_SECTION_TOOLTIP)
        boolean restoreDefaultsSection = true;

        @Preference(label = SeadasReaderDefaults.PROPERTY_RESTORE_DEFAULTS_LABEL,
                key = SeadasReaderDefaults.PROPERTY_RESTORE_DEFAULTS_KEY,
                description = SeadasReaderDefaults.PROPERTY_RESTORE_DEFAULTS_TOOLTIP)
        boolean restoreDefaults = SeadasReaderDefaults.PROPERTY_RESTORE_DEFAULTS_DEFAULT;

    }

}
