package eu.esa.opt.dataio.s3.olci;

import eu.esa.opt.dataio.s3.Sentinel3ProductReader;
import eu.esa.snap.core.datamodel.group.BandGroupImpl;
import eu.esa.snap.core.datamodel.group.BandGroupingPath;
import org.esa.snap.core.datamodel.Product;

/**
 * @author Tonio Fincke
 */
public class OlciLevel2WProductFactory extends OlciProductFactory {

    public OlciLevel2WProductFactory(Sentinel3ProductReader productReader) {
        super(productReader);
    }

    @Override
    protected String getValidExpression() {
        return "!WQSF_lsb.INVALID";
    }

    @Override
    protected void setAutoGrouping(Product[] sourceProducts, Product targetProduct) {
        final String[][] inputPaths = {{"Oa*_reflectance"},
                {"Oa*_reflectance_err"},
                {"Oa*_reflectance_unc"},
                {"A865"},
                {"CHL"},
                {"IWV"},
                {"PAR"},
                {"T865"},
                {"TSM"},
                {"atmospheric_temperature_profile"},
                {"lambda0"},
                {"FWHM"},
                {"solar_flux"},
                {"IOP#anw_443,acdm_443,aphy_443,acdom_443,bbp_443,kd_490,bbp_slope,OWC,ADG443_NN,ADG443_NN_unc,KD490_M07,KD490_M07_unc"}};

        final BandGroupImpl bandGroup = new BandGroupImpl(inputPaths);
        targetProduct.setAutoGrouping(bandGroup);

        /*
        targetProduct.setAutoGrouping("Oa*_reflectance:Oa*_reflectance_err::Oa*_reflectance_unc:A865:ADG:CHL:IWV:KD490:PAR:T865:TSM:" +
                "atmospheric_temperature_profile:lambda0:FWHM:solar_flux:IOP");
                *
         */
    }

    @Override
    protected void setMasks(Product targetProduct) {
        super.setMasks(targetProduct);

        final String water = "(WQSF_lsb.WATER or WQSF_lsb.INLAND_WATER)";
        final String cloudOrInvalid = "(WQSF_lsb.CLOUD or WQSF_lsb.CLOUD_AMBIGUOUS or WQSF_lsb.CLOUD_MARGIN or WQSF_lsb.INVALID or WQSF_lsb.COSMETIC or WQSF_lsb.SATURATED or WQSF_lsb.SUSPECT or WQSF_lsb.HISOLZEN or WQSF_lsb.HIGHGLINT or WQSF_lsb.SNOW_ICE)";

        final String oceanColourExpression = water + " and not " + cloudOrInvalid;

        String openWaterExpression = "not (WQSF_lsb.AC_FAIL or WQSF_lsb.WHITECAPS or WQSF_lsb.ADJAC or " +
                "WQSF_msb.RWNEG_O2 or WQSF_msb.RWNEG_O3 or WQSF_msb.RWNEG_O4 or WQSF_msb.RWNEG_O5 or " +
                "WQSF_msb.RWNEG_O6 or WQSF_msb.RWNEG_O7 or WQSF_msb.RWNEG_O8)";

        addMask(targetProduct,
                createRecommendedMaskName("REFLECTANCE"),
                "not (" + oceanColourExpression + " and " + openWaterExpression + ")",
                "Excluding pixels that are deemed unreliable for Water Leaving Reflectances. Flag recommended by QWG.");
        addMask(targetProduct,
                createRecommendedMaskName("CHL_OC4ME"),
                "not (" + oceanColourExpression + " and " + openWaterExpression + ") or WQSF_lsb.OC4ME_FAIL",
                "Excluding pixels that are deemed unreliable for Algal Pigment Concentration. Flag recommended by QWG.");
        addMask(targetProduct,
                createRecommendedMaskName("KD490_M07"),
                "not (" + oceanColourExpression + " and " + openWaterExpression + ") or WQSF_lsb.KDM_FAIL",
                "Excluding pixels that are deemed unreliable for Diffuse Attenuation Coefficient. Flag recommended by QWG.");
        addMask(targetProduct,
                createRecommendedMaskName("PAR"),
                "not (" + oceanColourExpression + " and " + openWaterExpression + ") or WQSF_lsb.PAR_FAIL",
                "Excluding pixels that are deemed unreliable for Photosynthetically Active Radiation. Flag recommended by QWG.");
        addMask(targetProduct,
                createRecommendedMaskName("W_AER"),
                "not (" + oceanColourExpression + " and " + openWaterExpression + ")",
                "Excluding pixels that are deemed unreliable for Aerosol Optical Thickness (T865) and Angstrom exponent (A865). Flag recommended by QWG.");
        addMask(targetProduct,
                createRecommendedMaskName("CHL_NN"),
                "not (" + oceanColourExpression + ") or WQSF_lsb.OCNN_FAIL",
                "Excluding pixels that are deemed unreliable for Algal Pigment Concentration. Flag recommended by QWG.");
        addMask(targetProduct,
                createRecommendedMaskName("TSM_NN"),
                "not (" + oceanColourExpression + ") or WQSF_lsb.OCNN_FAIL",
                "Excluding pixels that are deemed unreliable for Total Suspended Matter Concentration. Flag recommended by QWG.");
        addMask(targetProduct,
                createRecommendedMaskName("ADG443_NN"),
                "not (" + oceanColourExpression + ") or WQSF_lsb.OCNN_FAIL",
                "Excluding pixels that are deemed unreliable for Coloured Detrital and Dissolved Material Absorption. Flag recommended by QWG.");
        addMask(targetProduct,
                createRecommendedMaskName("IWV"),
                "WQSF_lsb.MEGLINT or WQSF_lsb.WV_FAIL",
                "Excluding pixels that are deemed unreliable for Integrated Water Vapour Column. Flag recommended by QWG.");
    }

    private void addMask(Product targetProduct, String maskName, String expression, String description) {
        targetProduct.addMask(maskName, expression, description, getColorProvider().getMaskColor(maskName), 0.5);
    }

    // package access for testing only tb 2024-02-28
    static String createRecommendedMaskName(String maskNamePart) {
        return "WQSF_" + maskNamePart + "_RECOM";
    }
}
