package eu.esa.opt.spectralnoise.util;


public class SpectralNoiseUtils {


    private SpectralNoiseUtils() {}


    public static void readSpectrumAtPixel(SpectralNoiseReductionContext context, int absoluteX, int absoluteY, int tileIndex, double[] spectrum, boolean[] validMask) {
        for (int b = 0; b < context.getBandCount(); b++) {
            final double value = context.getSourceSamples()[b][tileIndex];
            final boolean valid = isValidSample(context, b, absoluteX, absoluteY, value);

            validMask[b] = valid;
            spectrum[b] = valid ? value : Double.NaN;
        }
    }

    private static boolean isValidSample(SpectralNoiseReductionContext context, int bandIndex, int absoluteX, int absoluteY, double value) {
        if (!context.getSourceTiles()[bandIndex].isSampleValid(absoluteX, absoluteY)) {
            return false;
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return false;
        }
        return !context.getNoDataUsed()[bandIndex] ||
                Double.compare(value, context.getNoDataValues()[bandIndex]) != 0;
    }

    public static void writeFilteredSpectrumAtPixel(SpectralNoiseReductionContext context, int tileIndex, double[] spectrum, boolean[] validMask, double[] filteredSpectrum) {
        for (int b = 0; b < context.getBandCount(); b++) {
            if (context.getTargetSamples()[b] == null) {
                continue;
            }

            if (!validMask[b]) {
                context.getTargetSamples()[b][tileIndex] = context.getNoDataUsed()[b]
                        ? context.getNoDataValues()[b]
                        : spectrum[b];
            } else {
                context.getTargetSamples()[b][tileIndex] = filteredSpectrum[b];
            }
        }
    }

    public static void writeTargetTiles(SpectralNoiseReductionContext context) {
        for (int b = 0; b < context.getBandCount(); b++) {
            if (context.getRequestedTargetTiles()[b] != null) {
                context.getRequestedTargetTiles()[b].setSamples(context.getTargetSamples()[b]);
            }
        }
    }
}
