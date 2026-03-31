package eu.esa.opt.spectralresample;

/**
 * Class holding a spectral response function value of wavelength/weight.
 * See <a href="https://en.wikipedia.org/wiki/Full_width_at_half_maximum">...</a> for more details.
 *
 */
public class SpectralResponse {
    float wvl;
    float weight;

    public SpectralResponse() {
    }

    public SpectralResponse(float wvl, float weight) {
        this.wvl = wvl;
        this.weight = weight;
    }

    public float getWvl() {
        return wvl;
    }

    public void setWvl(float wvl) {
        this.wvl = wvl;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
