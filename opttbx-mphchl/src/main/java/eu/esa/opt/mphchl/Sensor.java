package eu.esa.opt.mphchl;

import static eu.esa.opt.mphchl.MphChlConstants.*;

/**
 * Enumeration for supported sensors (OLCI, MERIS 3rd and 4th reprocessing)
 *
 * @author olafd
 */
public enum Sensor {
    MERIS_3RD("MERIS_3RD", MERIS_REQUIRED_RADIANCE_BAND_NAMES, MERIS_REQUIRED_BRR_BAND_NAMES, MERIS_VALID_PIXEL_EXPR_3RD),
    MERIS_4TH("MERIS_4TH", MERIS_REQUIRED_RADIANCE_BAND_NAMES_4TH, MERIS_REQUIRED_BRR_BAND_NAMES, MERIS_VALID_PIXEL_EXPR_4TH),
    OLCI("OLCI", OLCI_REQUIRED_RADIANCE_BAND_NAMES, OLCI_REQUIRED_BRR_BAND_NAMES, OLCI_VALID_PIXEL_EXPR);

    private String name;
    private String[] requiredRadianceBandNames;
    private String[] requiredBrrBandNames;
    private String validPixelExpression;

    Sensor(String name, String requiredRadianceBandNames[], String[] requiredBrrBandNames, String validPixelExpression) {
        this.name = name;
        this.requiredRadianceBandNames = requiredRadianceBandNames;
        this.requiredBrrBandNames = requiredBrrBandNames;
        this.validPixelExpression = validPixelExpression;
    }

    public String getName() {
        return name;
    }

    public String[] getRequiredRadianceBandNames() {
        return requiredRadianceBandNames;
    }

    public String[] getRequiredBrrBandNames() {
        return requiredBrrBandNames;
    }

    public String getValidPixelExpression() {
        return validPixelExpression;
    }
}
