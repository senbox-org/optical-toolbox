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
        id = "OCSSW-Processors")
@org.openide.util.NbBundle.Messages({
        "Options_DisplayName_SeadasL2Reader=SeaDAS L2 Reader",
        "Options_Keywords_SeadasL2Reader=seadas, ocssw, l2gen"
})
public final class SeadasReaderController extends DefaultConfigController {

    Property restoreDefaults;


    Property composite1FlagSelector;
    boolean composite1FlagSelectorIgnore = false;
    Property composite1Flags;
    Property composite1MaskName;
    Property composite2MaskName;
    Property composite3MaskName;

    Property composite2FlagSelector;
    boolean composite2FlagSelectorIgnore = false;
    Property composite2Flags;

    Property composite3FlagSelector;
    boolean composite3FlagSelectorIgnore = false;
    Property composite3Flags;

    boolean propertyValueChangeEventsEnabled = true;


    protected PropertySet createPropertySet() {
        return createPropertySet(new SeadasToolboxBean());
    }



    @Override
    protected JPanel createPanel(BindingContext context) {

        //
        // Initialize the default value contained within each property descriptor
        // This is done so subsequently the restoreDefaults actions can be performed
        //


        
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


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_CLDICE_COLOR_DEFAULT);

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_INCLUDE_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_INCLUDE_DEFAULT);
        composite1FlagSelector = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAG_PRESETS_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAG_PRESETS_DEFAULT);
        composite1Flags = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAGS_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAGS_DEFAULT);
        composite1MaskName = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_COLOR_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_INCLUDE_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_INCLUDE_DEFAULT);
        composite2FlagSelector = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAG_PRESETS_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAG_PRESETS_DEFAULT);
        composite2Flags = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAGS_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAGS_DEFAULT);
        composite2MaskName = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_ENABLED_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_ENABLED_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_DEFAULT);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_COLOR_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_COLOR_DEFAULT);


        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_SECTION_KEY, true);
        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_INCLUDE_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_INCLUDE_DEFAULT);
        composite3FlagSelector = initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAG_PRESETS_KEY, SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAG_PRESETS_DEFAULT);
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

        initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_RESTORE_SECTION_KEY, true);
        restoreDefaults =  initPropertyDefaults(context, SeadasReaderDefaults.PROPERTY_RESTORE_DEFAULTS_KEY, SeadasReaderDefaults.PROPERTY_RESTORE_DEFAULTS_DEFAULT);




        //
//                flagCoding.addFlag("ATMFAIL", 0x01, "Atmospheric correction failure");
//                flagCoding.addFlag("LAND", 0x02, "Land");
//                flagCoding.addFlag("PRODWARN", 0x04, "One (or more) product algorithms generated a warning");
//                flagCoding.addFlag("HIGLINT", 0x08, "High glint determined");
//                flagCoding.addFlag("HILT", 0x10, "High (or saturating) TOA radiance");
//                flagCoding.addFlag("HISATZEN", 0x20, "Large satellite zenith angle");
//                flagCoding.addFlag("COASTZ", 0x40, "Shallow water (<30m)");
//                flagCoding.addFlag("SPARE8", 0x80, "Unused");
//                flagCoding.addFlag("STRAYLIGHT", 0x100, "Straylight determined");
//                flagCoding.addFlag("CLDICE", 0x200, "Cloud/Ice determined");
//                flagCoding.addFlag("COCCOLITH", 0x400, "Coccolithophores detected");
        
        


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


//        context.bindEnabledState(SeadasReaderDefaults.PROPERTY_MASK_Quality_L3_EXPRESSION_CUSTOM_KEY, true,
//                SeadasReaderDefaults.PROPERTY_MASK_Quality_L3_EXPRESSION_KEY,
//                SeadasReaderDefaults.PROPERTY_MASK_Quality_L3_EXPRESSION_OPTION5).apply();
//

        // Handle resetDefaults events - set all other components to defaults
        restoreDefaults.addPropertyChangeListener(evt -> {
            handleRestoreDefaults(context);
        });



        composite1FlagSelector.addPropertyChangeListener(evt -> {
                    if (!composite1FlagSelectorIgnore &&
                            !SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION1.equals(composite1FlagSelector.getValue())) {
                        try {
                            String flagPreset = composite1FlagSelector.getValue();
                            if (flagPreset != null && flagPreset.length() > 0) {
                                String flags = trimPresetIDFromMaskName(flagPreset);

                                if (flags != null && flags.length() > 0) {
                                    String maskName = "Composite1";
                                    maskName = appendPresetToMaskName(maskName, flagPreset);
                                    composite1MaskName.setValue(maskName);

                                    composite1Flags.setValue(flags);
                                    composite1FlagSelectorIgnore = true;
                                    composite1FlagSelector.setValue(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION1);
                                    composite1FlagSelectorIgnore = false;
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
        });




        composite2FlagSelector.addPropertyChangeListener(evt -> {
            if (!composite2FlagSelectorIgnore &&
                    !SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION1.equals(composite2FlagSelector.getValue())) {
                try {
                    String flagPreset = composite2FlagSelector.getValue();
                    if (flagPreset != null && flagPreset.length() > 0) {
                        String flags = trimPresetIDFromMaskName(flagPreset);

                        if (flags != null && flags.length() > 0) {
                            String maskName = "Composite2";
                            maskName = appendPresetToMaskName(maskName, flagPreset);
                            composite2MaskName.setValue(maskName);

                            composite2Flags.setValue(flags);
                            composite2FlagSelectorIgnore = true;
                            composite2FlagSelector.setValue(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION1);
                            composite2FlagSelectorIgnore = false;
                        }
                    }
                } catch (Exception e) {
                }
            }
        });



        composite3FlagSelector.addPropertyChangeListener(evt -> {
            if (!composite3FlagSelectorIgnore &&
                    !SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION1.equals(composite3FlagSelector.getValue())) {
                try {
                    String flagPreset = composite3FlagSelector.getValue();
                    if (flagPreset != null && flagPreset.length() > 0) {
                        String flags = trimPresetIDFromMaskName(flagPreset);

                        if (flags != null && flags.length() > 0) {
                            String maskName = "Composite3";
                            maskName = appendPresetToMaskName(maskName, flagPreset);
                            composite3MaskName.setValue(maskName);

                            composite3Flags.setValue(flags);
                            composite3FlagSelectorIgnore = true;
                            composite3FlagSelector.setValue(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION1);
                            composite3FlagSelectorIgnore = false;
                        }
                    }
                } catch (Exception e) {
                }
            }
        });
        
        
        
        composite2FlagSelector.addPropertyChangeListener(evt -> {
            if (!composite2FlagSelectorIgnore &&
                    !SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION1.equals(composite2FlagSelector.getValue())) {
                try {
                    composite2Flags.setValue(composite2FlagSelector.getValue());
                    composite2FlagSelectorIgnore = true;
                    composite2FlagSelector.setValue(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION1);
                    composite2FlagSelectorIgnore = false;
                } catch (Exception e) {
                }
            }
        });


        composite3FlagSelector.addPropertyChangeListener(evt -> {
            if (!composite3FlagSelectorIgnore &&
                    !SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION1.equals(composite3FlagSelector.getValue())) {
                try {
                    composite3Flags.setValue(composite3FlagSelector.getValue());
                    composite3FlagSelectorIgnore = true;
                    composite3FlagSelector.setValue(SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION1);
                    composite3FlagSelectorIgnore = false;
                } catch (Exception e) {
                }
            }
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

            context.setComponentsEnabled(SeadasReaderDefaults.PROPERTY_RESTORE_DEFAULTS_KEY, false);
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


        

        // Quality_L2

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_SECTION_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_SECTION_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_SECTION_TOOLTIP)
        boolean mask_Quality_L2_Section = true;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_ENABLED_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_ENABLED_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_ENABLED_TOOLTIP)
        boolean mask_Quality_L2_EnabledDefault = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_ENABLED_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_TRANSPARENCY_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_TRANSPARENCY_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_TRANSPARENCY_TOOLTIP,
                interval = "[0.0,1.0]")
        double mask_Quality_L2_TransparencyDefault = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_TRANSPARENCY_DEFAULT;

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_COLOR_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_COLOR_LABEL,
                description = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_COLOR_TOOLTIP)
        Color mask_Quality_L2_ColorDefault = SeadasReaderDefaults.PROPERTY_MASK_Quality_L2_COLOR_DEFAULT;




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


        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAG_PRESETS_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAG_PRESETS_LABEL,
                valueSet = {SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION1,
                        SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION2,
                        SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION3,
                        SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION4},
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAG_PRESETS_TOOLTIP)
        String maskComposite1FlagPresetsDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAG_PRESETS_DEFAULT;

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

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAG_PRESETS_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAG_PRESETS_LABEL,
                valueSet = {SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION1,
                        SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION2,
                        SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION3,
                        SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION4},
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAG_PRESETS_TOOLTIP)
        String maskComposite2FlagPresetsDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAG_PRESETS_DEFAULT;

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

        @Preference(key = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAG_PRESETS_KEY,
                label = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAG_PRESETS_LABEL,
                valueSet = {SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION1,
                        SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION2,
                        SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION3,
                        SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION4},
                description = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAG_PRESETS_TOOLTIP)
        String maskComposite3FlagPresetsDefault = SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAG_PRESETS_DEFAULT;

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
