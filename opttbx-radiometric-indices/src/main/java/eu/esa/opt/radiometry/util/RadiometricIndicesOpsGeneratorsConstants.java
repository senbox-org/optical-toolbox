package eu.esa.opt.radiometry.util;

/**
 * Constants class for the Radiometric Indices Operators generators
 *
 * @author Adrian Draghici
 */
public class RadiometricIndicesOpsGeneratorsConstants {
    static final String RADIOMETRIC_INDICES_TEMPLATE_OP_FILENAME = "RadiometricIndicesTemplateOp.java";
    static final String RADIOMETRIC_INDICES_TEMPLATE_OP_TEST_FILENAME = "RadiometricIndicesTemplateOpTest.java";
    static final String RADIOMETRIC_INDICES_TEMPLATE_LAYER_XML_FILENAME = "RadiometricIndicesTemplateLayer.xml";
    static final String RADIOMETRIC_INDICES_TEMPLATE_OPERATORS_INDEX_LIST_HTML_FILENAME = "RadiometricIndicesTemplateOperatorsIndexList.html";
    static final String RADIOMETRIC_INDICES_TEMPLATE_OPERATOR_TOOL_HTML_FILENAME = "RadiometricIndicesTemplateOperatorTool.html";
    static final String RADIOMETRIC_INDICES_NAME_AREA = "/*radiometric_indices_name_area*/";
    static final String OPERATOR_CLASS_NAME_AREA = "/*operator_class_name_area*/";
    static final String OPERATOR_CLASS_NAME_LOWERCASE_AREA = "/*operator_class_name_lowercase_area*/";
    static final String CATEGORY_AREA = "/*category_area*/";
    static final String DESCRIPTION_AREA = "/*description_area*/";
    static final String DOMAIN_AREA = "/*domain_area*/";
    static final String PARAMETERS_AREA = "/*parameters_area*/";
    static final String SOURCE_BANDS_AREA = "/*source_bands_area*/";
    static final String TILE_VARS_AREA = "/*tile_vars_area*/";
    static final String EQUATION_VARS_AREA = "/*equation_vars_area*/";
    static final String EQUATION_FORMULA_AREA = "/*equation_formula_area*/";
    static final String PARAMETER_NAME_AREA = "/*parameter_name_area*/";
    static final String PARAMETER_NAME_CC_AREA = "/*parameter_name_cc_area*/";
    static final String PARAMETER_VALUE_AREA = "/*parameter_value_area*/";
    static final String SOURCE_BAND_NAME_AREA = "/*source_band_name_area*/";
    static final String SOURCE_BAND_VAR_NAME_AREA = "/*source_band_var_name_area*/";
    static final String SOURCE_BAND_NAME_CC_AREA = "/*source_band_name_cc_area*/";
    static final String SOURCE_BAND_MIN_WAVELENGTH_AREA = "/*source_band_min_wavelength_area*/";
    static final String SOURCE_BAND_MAX_WAVELENGTH_AREA = "/*source_band_max_wavelength_area*/";
    static final String EQUATION_VAR_NAME_AREA = "/*equation_var_name_area*/";
    static final String POW_METHOD_AREA = "/*pow_method_area*/";
    static final String PARAMETERS_AREA_CONTENT_TEMPLATE = """
            @Parameter(label = \"""" + PARAMETER_NAME_CC_AREA + """
            \sParameter", defaultValue = "1.0F", description = "The\s""" + PARAMETER_NAME_AREA + """
             parameter.")
            \tprivate float\s""" + PARAMETER_NAME_AREA + """
            ;""";
    static final String SOURCE_BANDS_AREA_CONTENT_TEMPLATE = """
            @Parameter(label = \"""" + SOURCE_BAND_NAME_CC_AREA + """
             source band",
            \t\t\tdescription = "The\s""" + SOURCE_BAND_NAME_AREA + """
             band for the Template computation. If not provided, the operator will try to find the best fitting band.",
            \t\t\trasterDataNodeType = Band.class)
            \t@BandParameter(minWavelength =\s""" + SOURCE_BAND_MIN_WAVELENGTH_AREA + """
            , maxWavelength =\s""" + SOURCE_BAND_MAX_WAVELENGTH_AREA + """
            )
            \tprivate String\s""" + SOURCE_BAND_VAR_NAME_AREA + """
            SourceBand;""";
    static final String TILE_VARS_AREA_CONTENT_TEMPLATE = """
            Tile\s""" + SOURCE_BAND_VAR_NAME_AREA + """
            Tile = getSourceTile(getSourceProduct().getBand(""" + SOURCE_BAND_VAR_NAME_AREA + """
            SourceBand), rectangle);""";
    static final String EQUATION_VARS_AREA_CONTENT_TEMPLATE = """
            final float\s""" + EQUATION_VAR_NAME_AREA + """
            =\s""" + SOURCE_BAND_VAR_NAME_AREA + """
            Tile.getSampleFloat(x, y);""";

    static final String POW_METHOD_AREA_CONTENT = """
            \n\tprivate static float pow(float n, float p) {
            \t\t\treturn (float) Math.pow(n, p);
            \t}
            """;
    static final String SQRT_METHOD_AREA_CONTENT = """
            \n\tprivate static float sqrt(float n) {
            \t\t\treturn (float) Math.sqrt(n);
            \t}
            """;
    static final String OPERATOR_BANDS_NAMES_AREA = "/*operator_bands_names_area*/";
    static final String OPERATOR_PRODUCT_WIDTH_AREA = "/*operator_product_width_area*/";
    static final String OPERATOR_PRODUCT_HEIGHT_AREA = "/*operator_product_height_area*/";
    static final int OPERATOR_PRODUCT_WIDTH = 3;
    static final int OPERATOR_PRODUCT_HEIGHT = 3;
    static final String OPERATOR_BANDS_WAVELENGTHS_AREA = "/*operator_bands_wavelengths_area*/";
    static final String OPERATOR_BANDS_MIN_VALUES_AREA = "/*operator_bands_min_values_area*/";
    static final String OPERATOR_BANDS_MAX_VALUES_AREA = "/*operator_bands_max_values_area*/";
    static final String OPERATOR_PARAMETERS_MAP_VALUES_AREA = "/*operator_parameters_map_values*/";
    static final String OPERATOR_PARAMETERS_MAP_CONTENT_AREA_TEMPLATE = """
            {{
            """ + OPERATOR_PARAMETERS_MAP_VALUES_AREA + """
            }}
            """;
    static final String OPERATOR_PARAMETERS_MAP_VALUES_CONTENT_AREA_TEMPLATE = """
            put(\"""" + PARAMETER_NAME_AREA + """
            ",\s""" + PARAMETER_VALUE_AREA + """
            );
            """;
    static final String OPERATOR_PARAMETERS_MAP_AREA = "/*operator_parameters_map_area*/";
    static final String OPERATOR_TARGET_VALUES_AREA = "/*operator_target_values_area*/";
    static final String OPERATORS_UIS_AREA = "<!--operators_uis_area-->";
    static final String OPERATORS_UIS_ACTIONS_AREA = "<!--operators_uis_actions_area-->";
    static final String OPERATORS_UIS_MENU_ENTRY_VEGETATION_AREA = "<!--operators_uis_menu_entry_vegetation_area-->";
    static final String OPERATORS_UIS_MENU_ENTRY_SOIL_AREA = "<!--operators_uis_menu_entry_soil_area-->";
    static final String OPERATORS_UIS_MENU_ENTRY_BURN_AREA = "<!--operators_uis_menu_entry_burn_area-->";
    static final String OPERATORS_UIS_MENU_ENTRY_URBAN_AREA = "<!--operators_uis_menu_entry_urban_area-->";
    static final String OPERATORS_UIS_MENU_ENTRY_WATER_AREA = "<!--operators_uis_menu_entry_water_area-->";
    static final String OPERATOR_UI_CONTENT_AREA_TEMPLATE = """
            <file name="eu.esa.opt.radiometry.""" + OPERATOR_CLASS_NAME_AREA + """
            ">
            \t<attr name="operatorUIClass" stringvalue="eu.esa.opt.radiometry.RadiometricIndicesUI"/>
            \t<attr name="operatorName" stringvalue=\"""" + OPERATOR_CLASS_NAME_AREA + """
            Op"/>
            </file>
            """;
    static final String OPERATOR_UI_ACTIONS_CONTENT_AREA_TEMPLATE = """
            <file name="eu-esa-opt-radiometry-""" + OPERATOR_CLASS_NAME_AREA + """
            OpAction.instance">
                <attr name="instanceCreate" methodvalue="org.openide.awt.Actions.alwaysEnabled"/>
                <attr name="delegate" methodvalue="eu.esa.opt.radiometry.RadiometricOperatorAction.create"/>
                <attr name="displayName" stringvalue=\"""" + RADIOMETRIC_INDICES_NAME_AREA + """
            Processor"/>
                <attr name="operatorName" stringvalue=\"""" + OPERATOR_CLASS_NAME_AREA + """
            Op"/>
                <attr name="dialogTitle" stringvalue=\"""" + RADIOMETRIC_INDICES_NAME_AREA + """
            "/>
                <attr name="helpId" stringvalue=\"""" + RADIOMETRIC_INDICES_NAME_AREA + """
            Operator"/>
                <attr name="targetProductNameSuffix" stringvalue="_""" + OPERATOR_CLASS_NAME_LOWERCASE_AREA + """
            "/>
                <attr name="ShortDescription" stringvalue="Generates\s""" + RADIOMETRIC_INDICES_NAME_AREA + """
             from a source product with at least two spectral bands."/>
            </file>
            """;
    static final String OPERATORS_UIS_MENU_ENTRY_CONTENT_AREA_TEMPLATE = """
            <file name="eu-esa-opt-radiometry-""" + OPERATOR_CLASS_NAME_AREA + """
            OpAction.shadow">
                <attr name="originalFile"
                      stringvalue="Actions/Operators/eu-esa-opt-radiometry-""" + OPERATOR_CLASS_NAME_AREA + """
            OpAction.instance"/>
            </file>
            """;
    static final String OPERATORS_INDEX_LIST_ENTRY_VEGETATION_AREA = "<!--operator_index_list_entry_vegetation_area-->";
    static final String OPERATORS_INDEX_LIST_ENTRY_SOIL_AREA = "<!--operator_index_list_entry_soil_area-->";
    static final String OPERATORS_INDEX_LIST_ENTRY_BURN_AREA = "<!--operator_index_list_entry_burn_area-->";
    static final String OPERATORS_INDEX_LIST_ENTRY_URBAN_AREA = "<!--operator_index_list_entry_urban_area-->";
    static final String OPERATORS_INDEX_LIST_ENTRY_WATER_AREA = "<!--operator_index_list_entry_water_area-->";
    static final String OPERATOR_INDEX_LIST_ENTRY_CONTENT_AREA_TEMPLATE = """
            <tr>
                <td><h3><b><a href=\"""" + OPERATOR_CLASS_NAME_LOWERCASE_AREA + """
            /""" + OPERATOR_CLASS_NAME_AREA + """
            AlgorithmSpecification.html">""" + RADIOMETRIC_INDICES_NAME_AREA + """
            </a></b></h3></td>
            <td><h5>""" + DESCRIPTION_AREA + """
            </h5></td>
            <td><h5></h5></td>
            <td><h5>""" + DOMAIN_AREA + """
             Index</h5></td>
            </tr>
            """;
}
