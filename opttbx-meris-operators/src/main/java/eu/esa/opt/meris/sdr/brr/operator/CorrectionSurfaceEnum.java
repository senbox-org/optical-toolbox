package eu.esa.opt.meris.sdr.brr.operator;

/**
 * Enum to specify the application area of the Rayleigh correction (land, water, everywhere)
 *
 * @author olafd
 */
public enum CorrectionSurfaceEnum {
    ALL_SURFACES,
    LAND,
    WATER {
        @Override
        public String toString() {
            return super.toString();
        }
    }
}
