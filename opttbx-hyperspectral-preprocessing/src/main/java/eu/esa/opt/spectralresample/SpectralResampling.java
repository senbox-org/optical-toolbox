package eu.esa.opt.spectralresample;

public class SpectralResampling {

    public SpectralResampling() {
    }

    public static double[] resample(double[] inputSpectrum, SpectralResponseFunction srf) {
        double[] resampledSpectrum = new double[srf.getSpectralResponsesList().size()];

        // TODO: implement following Enmap Python code:
        //       - https://github.com/EnMAP-Box/enmap-box/blob/main/enmapboxprocessing/algorithm/spectralresamplingbyresponsefunctionconvolutionalgorithmbase.py
        //         --> function resampleData, with responses from
        //             1. wvl/fwhm pairs (start)
        //             2. fully defined responses2 computed in processAlgorithm
        //             to be implemented in SpectralResponseFunction
        return resampledSpectrum;
    }

}
