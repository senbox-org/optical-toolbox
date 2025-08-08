package eu.esa.opt.radiometry.util;

/**
 * Constants class for the Radiometric Indices Operators generators
 *
 * @author Adrian Draghici
 */
public class RadiometricIndicesOpsGeneratorsConstants {
    static final String RADIOMETRIC_INDICES_TEMPLATE_OP_FILENAME = "RadiometricIndicesTemplateOp.java";
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
}
