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
 * * Panel handling preferences for band properties
 *
 * @author Daniel Knowles
 */


@OptionsPanelController.SubRegistration(location = "OPTTBX",
        displayName = "#Options_DisplayName_BandProperties",
        keywords = "#Options_Keywords_BandProperties",
        keywordsCategory = "Processors",
        id = "SeaDAS-BandProperties")
@org.openide.util.NbBundle.Messages({
        "Options_DisplayName_BandProperties=Band Properties",
        "Options_Keywords_BandProperties=valid pixel expression"
})
public final class SeadasReaderBandPropertiesController extends DefaultConfigController {

    Property restoreDefaults;

    boolean propertyValueChangeEventsEnabled = true;


    protected PropertySet createPropertySet() {
        return createPropertySet(new SeadasToolboxBean());
    }



    // Property Setting: Restore Defaults

    private static final String PROPERTY_RESTORE_KEY_SUFFIX = SeadasReaderDefaults.PROPERTY_BAND_PROPERTIES_ROOT_KEY + ".restore.defaults";

    public static final String PROPERTY_RESTORE_SECTION_KEY = PROPERTY_RESTORE_KEY_SUFFIX + ".section";
    public static final String PROPERTY_RESTORE_SECTION_LABEL = "Restore";
    public static final String PROPERTY_RESTORE_SECTION_TOOLTIP = "Restores preferences to the package defaults";

    public static final String PROPERTY_RESTORE_DEFAULTS_KEY = PROPERTY_RESTORE_KEY_SUFFIX + ".apply";
    public static final String PROPERTY_RESTORE_DEFAULTS_LABEL = "Default (SeaDAS Toolbox Preferences)";
    public static final String PROPERTY_RESTORE_DEFAULTS_TOOLTIP = "Restore all band properties preferences to the original default";
    public static final boolean PROPERTY_RESTORE_DEFAULTS_DEFAULT = false;



    @Override
    protected JPanel createPanel(BindingContext context) {


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_VALID_PIXEL_ROUND_KEY, SeadasReaderDefaults.PROPERTY_VALID_PIXEL_ROUND_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_KEY, SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_DEFAULT);


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


        context.bindEnabledState(SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_KEY, SeadasReaderDefaults.PROPERTY_VALID_PIXEL_ROUND_KEY).apply();


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
        return new HelpCtx("seadasToolboxPreferences");
    }

    @SuppressWarnings("UnusedDeclaration")
    static class SeadasToolboxBean {



        @Preference(key = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SECTION_TOOLTIP)
        boolean validPixelSection = true;


        @Preference(key = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_ROUND_KEY,
                label = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_ROUND_LABEL,
                description = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_ROUND_TOOLTIP)
        boolean validPixelRoundDefault = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_ROUND_DEFAULT;



        @Preference(key = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_KEY,
                label = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_LABEL,
                description = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_TOOLTIP,
                interval = "[3,32]")
        int propertyValidPixelSigFigsDefault = SeadasReaderDefaults.PROPERTY_VALID_PIXEL_SIG_FIGS_DEFAULT;





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
