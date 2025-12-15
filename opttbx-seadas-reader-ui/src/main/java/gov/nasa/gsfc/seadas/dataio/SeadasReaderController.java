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
        displayName = "#Options_DisplayName_SeadasL2Reader",
        keywords = "#Options_Keywords_SeadasL2Reader",
        keywordsCategory = "Processors",
        id = "SeaDAS-L2-Flag-Masks")
@org.openide.util.NbBundle.Messages({
        "Options_DisplayName_SeadasL2Reader=SeaDAS Level-2 Masks",
        "Options_Keywords_SeadasL2Reader=seadas, ocssw, l2gen"
})
public final class SeadasReaderController extends DefaultConfigController {

    Property restoreDefaults;


    Property composite1MaskName;
    Property composite2MaskName;
    Property composite3MaskName;

    Property composite1Flags;
    Property composite2Flags;
    Property composite3Flags;


    boolean propertyValueChangeEventsEnabled = true;



    // Property Setting: Restore Defaults

    private static final String PROPERTY_RESTORE_KEY_SUFFIX = SeadasReaderDefaults.PROPERTY_SEADAS_READER_ROOT_KEY + ".restore.defaults";

    public static final String PROPERTY_RESTORE_SECTION_KEY = PROPERTY_RESTORE_KEY_SUFFIX + ".section";
    public static final String PROPERTY_RESTORE_SECTION_LABEL = "Restore";
    public static final String PROPERTY_RESTORE_SECTION_TOOLTIP = "Restores preferences to the package defaults";

    public static final String PROPERTY_RESTORE_DEFAULTS_KEY = PROPERTY_RESTORE_KEY_SUFFIX + ".apply";
    public static final String PROPERTY_RESTORE_DEFAULTS_LABEL = "Default (SeaDAS Reader Level-2 Masks Preferences)";
    public static final String PROPERTY_RESTORE_DEFAULTS_TOOLTIP = "Restore all band properties preferences to the original default";
    public static final boolean PROPERTY_RESTORE_DEFAULTS_DEFAULT = false;


    protected PropertySet createPropertySet() {
        return createPropertySet(new SeadasToolboxBean());
    }



    @Override
    protected JPanel createPanel(BindingContext context) {

        //
        // Initialize the default value contained within each property descriptor
        // This is done so subsequently the restoreDefaults actions can be performed
        //


//        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_OVERRIDE_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_OVERRIDE_COLOR_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_SORT_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_SORT_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_SORT_KEY, SeadasReaderDefaults.PROPERTY_MASK_SORT_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_LAND_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_LAND_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_LAND_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_LAND_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_LAND_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_LAND_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_LAND_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_ENABLED_DEFAULT);       
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HILT_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HILT_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_HILT_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HILT_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_HILT_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HILT_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_HILT_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COASTZ_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COASTZ_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_COASTZ_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COASTZ_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_COASTZ_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COASTZ_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_COASTZ_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_COLOR_DEFAULT);
        
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_LOWLW_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_LOWLW_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_LOWLW_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_LOWLW_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_LOWLW_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_LOWLW_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_LOWLW_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_ABSAER_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_ABSAER_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_ABSAER_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_ABSAER_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_ABSAER_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_ABSAER_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_ABSAER_COLOR_DEFAULT);
        
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_COLOR_DEFAULT);
        
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_SEAICE_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_SEAICE_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_SEAICE_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_SEAICE_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_SEAICE_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_SEAICE_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_SEAICE_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_FILTER_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_FILTER_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_FILTER_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_FILTER_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_FILTER_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_FILTER_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_FILTER_COLOR_DEFAULT);
        
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HIPOL_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HIPOL_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_HIPOL_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HIPOL_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_HIPOL_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_HIPOL_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_HIPOL_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_COLOR_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CLOUD_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CLOUD_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_CLOUD_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CLOUD_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_CLOUD_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CLOUD_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_CLOUD_COLOR_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_COLOR_DEFAULT);



        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_INCLUDE_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_INCLUDE_DEFAULT);
        composite1Flags = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAGS_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAGS_DEFAULT);
        composite1MaskName = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_COLOR_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_INCLUDE_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_INCLUDE_DEFAULT);
        composite2Flags = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAGS_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAGS_DEFAULT);
        composite2MaskName = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_COLOR_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_INCLUDE_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_INCLUDE_DEFAULT);
        composite3Flags = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAGS_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAGS_DEFAULT);
        composite3MaskName = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_COLOR_DEFAULT);





        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_Water_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_Water_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_Water_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_Water_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_Water_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_Water_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_Water_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_SPARE_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_SPARE_INCLUDE_KEY, SeadasReaderDefaults.PROPERTY_MASK_SPARE_INCLUDE_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_SPARE_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_SPARE_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_SPARE_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_SPARE_COLOR_DEFAULT);


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


        context.bindEnabledState(SeadasReaderDefaults.PROPERTY_MASK_SPARE_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_SPARE_INCLUDE_KEY).apply();
        context.bindEnabledState(SeadasReaderDefaults.PROPERTY_MASK_SPARE_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_SPARE_INCLUDE_KEY).apply();


        context.bindEnabledState(SeadasReaderDefaults.PROPERTY_MASK_SORT_KEY, SeadasReaderDefaults.PROPERTY_MASK_SORT_ENABLED_KEY).apply();


//        context.bindEnabledState(SeadasReaderDefaults.PROPERTY_MASK_Quality_L3_EXPRESSION_CUSTOM_KEY, true,
//                SeadasReaderDefaults.PROPERTY_MASK_Quality_L3_EXPRESSION_KEY,
//                SeadasReaderDefaults.PROPERTY_MASK_Quality_L3_EXPRESSION_OPTION5).apply();
//

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
    }


    String trimPresetIDFromMaskName(String flagPreset) {

        String flag = flagPreset;

        if (flagPreset != null && flagPreset.length() > 0) {
            String[] flagPresetNameArray = flagPreset.split(":");
            if (flagPresetNameArray != null && flagPresetNameArray.length > 1 && flagPresetNameArray[0] != null && flagPresetNameArray[0].trim().length() > 0) {
                flag = flagPresetNameArray[1].trim();
            }
        }

        return flag;
    }

    String appendPresetToMaskName(String maskName, String flagPreset) {

        if (flagPreset != null && flagPreset.length() > 0) {
            String[] flagPresetNameArray = flagPreset.split(":");
            if (flagPresetNameArray != null && flagPresetNameArray.length > 1 && flagPresetNameArray[0] != null && flagPresetNameArray[0].trim().length() > 0) {
                String namePart = flagPresetNameArray[0].trim();
                maskName = maskName + "_" + namePart;
            }
        }

        return maskName;
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
        return new HelpCtx("preferencesSeaDASLevel2Masks");
    }

    @SuppressWarnings("UnusedDeclaration")
    static class SeadasToolboxBean {



//        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_OVERRIDE_COLOR_KEY,
//                label = SeadasReaderDefaults.PROPERTY_MASK_OVERRIDE_COLOR_LABEL,
//                description = SeadasReaderDefaults.PROPERTY_MASK_OVERRIDE_COLOR_TOOLTIP)
//        Color maskOverrideColorDefault = SeadasReaderDefaults.PROPERTY_MASK_OVERRIDE_COLOR_DEFAULT;



        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_SORT_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_SORT_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_SORT_ENABLED_TOOLTIP)
        boolean maskSortEnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_SORT_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_SORT_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_SORT_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_SORT_TOOLTIP)
        String maskSortDefault = SeadasReaderDefaults.PROPERTY_MASK_SORT_DEFAULT;





        // ATMFAIL

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_SECTION_TOOLTIP)
        boolean mask_ATMFAIL_Section = true;
        
        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_ENABLED_TOOLTIP)
        boolean mask_ATMFAIL_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_ATMFAIL_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_COLOR_TOOLTIP)
        Color mask_ATMFAIL_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_COLOR_DEFAULT;
        
        

        
        // LAND

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_LAND_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_LAND_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_LAND_SECTION_TOOLTIP)
        boolean mask_LAND_Section = true;
        
        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_LAND_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_LAND_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_LAND_ENABLED_TOOLTIP)
        boolean mask_LAND_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_LAND_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_LAND_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_LAND_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_LAND_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_LAND_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_LAND_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_LAND_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_LAND_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_LAND_COLOR_TOOLTIP)
        Color mask_LAND_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_LAND_COLOR_DEFAULT;



        
        // PRODWARN
        
        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_SECTION_TOOLTIP)
        boolean mask_PRODWARN_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_ENABLED_TOOLTIP)
        boolean mask_PRODWARN_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_PRODWARN_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_COLOR_TOOLTIP)
        Color mask_PRODWARN_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_COLOR_DEFAULT;




        // HIGLINT

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_SECTION_TOOLTIP)
        boolean mask_HIGLINT_Section = true;
        
        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_ENABLED_TOOLTIP)
        boolean mask_HIGLINT_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_HIGLINT_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_COLOR_TOOLTIP)
        Color mask_HIGLINT_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_COLOR_DEFAULT;



        // HILT

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HILT_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HILT_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HILT_SECTION_TOOLTIP)
        boolean mask_HILT_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HILT_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HILT_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HILT_ENABLED_TOOLTIP)
        boolean mask_HILT_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_HILT_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HILT_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HILT_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HILT_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_HILT_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_HILT_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HILT_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HILT_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HILT_COLOR_TOOLTIP)
        Color mask_HILT_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_HILT_COLOR_DEFAULT;




        // HISATZEN

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_SECTION_TOOLTIP)
        boolean mask_HISATZEN_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_ENABLED_TOOLTIP)
        boolean mask_HISATZEN_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_HISATZEN_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_COLOR_TOOLTIP)
        Color mask_HISATZEN_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_COLOR_DEFAULT;




        // COASTZ

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_SECTION_TOOLTIP)
        boolean mask_COASTZ_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_ENABLED_TOOLTIP)
        boolean mask_COASTZ_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_COASTZ_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_COLOR_TOOLTIP)
        Color mask_COASTZ_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_COASTZ_COLOR_DEFAULT;
        
        
        
        

        // STRAYLIGHT

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_SECTION_TOOLTIP)
        boolean mask_STRAYLIGHT_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_ENABLED_TOOLTIP)
        boolean maskSTRAYLIGHTEnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double maskSTRAYLIGHTTransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_COLOR_TOOLTIP)
        Color maskSTRAYLIGHTColorDefault = SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_COLOR_DEFAULT;




        // CLDICE

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_SECTION_TOOLTIP)
        boolean mask_CLDICE_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_ENABLED_TOOLTIP)
        boolean mask_CLDICE_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_CLDICE_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_COLOR_TOOLTIP)
        Color mask_CLDICE_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_CLDICE_COLOR_DEFAULT;



        // COCCOLITH

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_SECTION_TOOLTIP)
        boolean mask_COCCOLITH_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_ENABLED_TOOLTIP)
        boolean mask_COCCOLITH_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_COCCOLITH_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_COLOR_TOOLTIP)
        Color mask_COCCOLITH_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_COLOR_DEFAULT;



        // TURBIDW

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_SECTION_TOOLTIP)
        boolean mask_TURBIDW_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_ENABLED_TOOLTIP)
        boolean mask_TURBIDW_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_TURBIDW_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_COLOR_TOOLTIP)
        Color mask_TURBIDW_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_COLOR_DEFAULT;



        // HISOLZEN

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_SECTION_TOOLTIP)
        boolean mask_HISOLZEN_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_ENABLED_TOOLTIP)
        boolean mask_HISOLZEN_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_HISOLZEN_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_COLOR_TOOLTIP)
        Color mask_HISOLZEN_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_COLOR_DEFAULT;



        // LOWLW

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_SECTION_TOOLTIP)
        boolean mask_LOWLW_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_ENABLED_TOOLTIP)
        boolean mask_LOWLW_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_LOWLW_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_COLOR_TOOLTIP)
        Color mask_LOWLW_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_LOWLW_COLOR_DEFAULT;


        
        // CHLFAIL

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_SECTION_TOOLTIP)
        boolean mask_CHLFAIL_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_ENABLED_TOOLTIP)
        boolean mask_CHLFAIL_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_CHLFAIL_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_COLOR_TOOLTIP)
        Color mask_CHLFAIL_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_COLOR_DEFAULT;




        // NAVWARN

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_SECTION_TOOLTIP)
        boolean mask_NAVWARN_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_ENABLED_TOOLTIP)
        boolean mask_NAVWARN_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_NAVWARN_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_COLOR_TOOLTIP)
        Color mask_NAVWARN_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_COLOR_DEFAULT;

        

        // ABSAER

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_SECTION_TOOLTIP)
        boolean mask_ABSAER_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_ENABLED_TOOLTIP)
        boolean mask_ABSAER_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_ABSAER_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_COLOR_TOOLTIP)
        Color mask_ABSAER_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_ABSAER_COLOR_DEFAULT;




        // MAXAERITER

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_SECTION_TOOLTIP)
        boolean mask_MAXAERITER_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_ENABLED_TOOLTIP)
        boolean mask_MAXAERITER_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_MAXAERITER_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_COLOR_TOOLTIP)
        Color mask_MAXAERITER_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_COLOR_DEFAULT;


        
        // MODGLINT

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_SECTION_TOOLTIP)
        boolean mask_MODGLINT_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_ENABLED_TOOLTIP)
        boolean mask_MODGLINT_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_MODGLINT_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_COLOR_TOOLTIP)
        Color mask_MODGLINT_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_COLOR_DEFAULT;

        

        // CHLWARN

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_SECTION_TOOLTIP)
        boolean mask_CHLWARN_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_ENABLED_TOOLTIP)
        boolean mask_CHLWARN_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_CHLWARN_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_COLOR_TOOLTIP)
        Color mask_CHLWARN_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_COLOR_DEFAULT;


        
        // ATMWARN

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_SECTION_TOOLTIP)
        boolean mask_ATMWARN_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_ENABLED_TOOLTIP)
        boolean mask_ATMWARN_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_ATMWARN_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_COLOR_TOOLTIP)
        Color mask_ATMWARN_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_COLOR_DEFAULT;



        // SEAICE

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_SECTION_TOOLTIP)
        boolean mask_SEAICE_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_ENABLED_TOOLTIP)
        boolean mask_SEAICE_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_SEAICE_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_COLOR_TOOLTIP)
        Color mask_SEAICE_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_SEAICE_COLOR_DEFAULT;



        // NAVFAIL

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_SECTION_TOOLTIP)
        boolean mask_NAVFAIL_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_ENABLED_TOOLTIP)
        boolean mask_NAVFAIL_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_NAVFAIL_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_COLOR_TOOLTIP)
        Color mask_NAVFAIL_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_COLOR_DEFAULT;




        // FILTER

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_FILTER_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_FILTER_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_FILTER_SECTION_TOOLTIP)
        boolean mask_FILTER_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_FILTER_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_FILTER_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_FILTER_ENABLED_TOOLTIP)
        boolean mask_FILTER_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_FILTER_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_FILTER_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_FILTER_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_FILTER_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_FILTER_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_FILTER_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_FILTER_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_FILTER_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_FILTER_COLOR_TOOLTIP)
        Color mask_FILTER_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_FILTER_COLOR_DEFAULT;



        // BOWTIEDEL

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_SECTION_TOOLTIP)
        boolean mask_BOWTIEDEL_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_ENABLED_TOOLTIP)
        boolean mask_BOWTIEDEL_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_BOWTIEDEL_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_COLOR_TOOLTIP)
        Color mask_BOWTIEDEL_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_COLOR_DEFAULT;




        // HIPOL

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_SECTION_TOOLTIP)
        boolean mask_HIPOL_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_ENABLED_TOOLTIP)
        boolean mask_HIPOL_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_HIPOL_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_COLOR_TOOLTIP)
        Color mask_HIPOL_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_HIPOL_COLOR_DEFAULT;



        // PRODFAIL

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_SECTION_TOOLTIP)
        boolean mask_PRODFAIL_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_ENABLED_TOOLTIP)
        boolean mask_PRODFAIL_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_PRODFAIL_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_COLOR_TOOLTIP)
        Color mask_PRODFAIL_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_COLOR_DEFAULT;






        // GEOREGION

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_SECTION_TOOLTIP)
        boolean mask_GEOREGION_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_ENABLED_TOOLTIP)
        boolean mask_GEOREGION_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_GEOREGION_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_COLOR_TOOLTIP)
        Color mask_GEOREGION_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_COLOR_DEFAULT;



        // OPSHAL

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_SECTION_TOOLTIP)
        boolean mask_OPSHAL_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_ENABLED_TOOLTIP)
        boolean mask_OPSHAL_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_OPSHAL_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_COLOR_TOOLTIP)
        Color mask_OPSHAL_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_OPSHAL_COLOR_DEFAULT;




        // CLOUD

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_SECTION_TOOLTIP)
        boolean mask_CLOUD_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_ENABLED_TOOLTIP)
        boolean mask_CLOUD_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_CLOUD_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_COLOR_TOOLTIP)
        Color mask_CLOUD_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_CLOUD_COLOR_DEFAULT;




        // SNOWICE

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_SECTION_TOOLTIP)
        boolean mask_SNOWICE_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_ENABLED_TOOLTIP)
        boolean mask_SNOWICE_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_SNOWICE_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_COLOR_TOOLTIP)
        Color mask_SNOWICE_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_SNOWICE_COLOR_DEFAULT;



        
        

        // Composite1

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_SECTION_TOOLTIP)
        boolean maskComposite1Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_INCLUDE_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_INCLUDE_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_TOOLTIP)
        boolean maskComposite1IncludeDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_INCLUDE_DEFAULT;
        
        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_ENABLED_TOOLTIP)
        boolean maskComposite1EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_ENABLED_DEFAULT;


        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_TOOLTIP)
        String maskComposite1NameDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAGS_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAGS_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAGS_TOOLTIP)
        String maskComposite1FlagsDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAGS_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double maskComposite1TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_COLOR_TOOLTIP)
        Color maskComposite1ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_COLOR_DEFAULT;





        // Composite2

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_SECTION_TOOLTIP)
        boolean maskComposite2Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_INCLUDE_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_INCLUDE_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_TOOLTIP)
        boolean maskComposite2IncludeDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_INCLUDE_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_ENABLED_TOOLTIP)
        boolean maskComposite2EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_TOOLTIP)
        String maskComposite2NameDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAGS_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAGS_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAGS_TOOLTIP)
        String maskComposite2FlagsDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAGS_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double maskComposite2TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_COLOR_TOOLTIP)
        Color maskComposite2ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_COLOR_DEFAULT;





        // Composite3

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_SECTION_TOOLTIP)
        boolean maskComposite3Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_INCLUDE_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_INCLUDE_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_TOOLTIP)
        boolean maskComposite3IncludeDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_INCLUDE_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_ENABLED_TOOLTIP)
        boolean maskComposite3EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_TOOLTIP)
        String maskComposite3NameDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAGS_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAGS_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAGS_TOOLTIP)
        String maskComposite3FlagsDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAGS_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double maskComposite3TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_COLOR_TOOLTIP)
        Color maskComposite3ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_COLOR_DEFAULT;





        // Water

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_Water_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_Water_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_Water_SECTION_TOOLTIP)
        boolean mask_Water_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_Water_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_Water_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_Water_ENABLED_TOOLTIP)
        boolean mask_Water_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_Water_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_Water_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_Water_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_Water_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_Water_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_Water_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_Water_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_Water_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_Water_COLOR_TOOLTIP)
        Color mask_Water_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_Water_COLOR_DEFAULT;



        // SPARE

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_SPARE_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_SPARE_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_SPARE_SECTION_TOOLTIP)
        boolean mask_SPARE_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_SPARE_INCLUDE_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_SPARE_INCLUDE_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_SPARE_INCLUDE_TOOLTIP)
        boolean mask_SPARE_IncludeDefault = SeadasReaderDefaults.PROPERTY_MASK_SPARE_INCLUDE_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_SPARE_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_SPARE_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_SPARE_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_SPARE_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_SPARE_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_SPARE_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_SPARE_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_SPARE_COLOR_TOOLTIP)
        Color mask_SPARE_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_SPARE_COLOR_DEFAULT;




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
