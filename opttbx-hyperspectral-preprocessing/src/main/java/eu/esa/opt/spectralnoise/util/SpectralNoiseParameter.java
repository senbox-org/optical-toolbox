package eu.esa.opt.spectralnoise.util;

import org.apache.commons.math3.linear.*;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.util.math.SingularMatrixException;

import java.util.Arrays;


public class SpectralNoiseParameter {


    public static final String FILTER_SG = "Savitzky-Golay";
    public static final String FILTER_GAUSSIAN = "Gaussian";
    public static final String FILTER_BOX = "Box";

    private final String filterType;
    private final int kernelSize;
    private final double gaussianSigma;
    private final int sgPolynomialOrder;


    public SpectralNoiseParameter(String filterType, int kernelSize, double gaussianSigma, int sgPolynomialOrder) {
        this.filterType = filterType;
        this.kernelSize = kernelSize;
        this.gaussianSigma = gaussianSigma;
        this.sgPolynomialOrder = sgPolynomialOrder;
    }


    public void ensureKernelSize(int numBands) {
        if (kernelSize > numBands) {
            throw new OperatorException("Kernel size must not be greater than the number of selected bands.");
        }
    }

    public void validateFilterParameters() {
        if (kernelSize < 3 || kernelSize % 2 == 0) {
            throw new OperatorException("Kernel size must be odd and >= 3.");
        }

        if (FILTER_GAUSSIAN.equals(filterType) && gaussianSigma <= 0.0) {
            throw new OperatorException("Gaussian sigma must be > 0.");
        }

        if (FILTER_SG.equals(filterType)) {
            if (sgPolynomialOrder < 0) {
                throw new OperatorException("Savitzky-Golay polynomial order must be >= 0.");
            }
            if (sgPolynomialOrder >= kernelSize) {
                throw new OperatorException("Savitzky-Golay polynomial order must be smaller than kernel size.");
            }
        }
    }


    public double[] createKernel() {
        if (FILTER_BOX.equals(filterType)) {
            return createBoxKernel(kernelSize);
        }
        if (FILTER_GAUSSIAN.equals(filterType)) {
            return createGaussianKernel(kernelSize, gaussianSigma);
        }
        if (FILTER_SG.equals(filterType)) {
            return createSavitzkyGolayKernel(kernelSize, sgPolynomialOrder);
        }

        throw new OperatorException("Unsupported filter type: " + filterType);
    }

    private static double[] createBoxKernel(int size) {
        final double[] kernel = new double[size];
        final double weight = 1.0 / size;
        Arrays.fill(kernel, weight);
        return kernel;
    }

    private static double[] createGaussianKernel(int size, double sigma) {
        final double[] kernel = new double[size];
        final int half = size / 2;

        double sum = 0.0;
        for (int ii = -half; ii <= half; ii++) {
            final double value = Math.exp(-(ii * ii) / (2.0 * sigma * sigma));
            kernel[ii + half] = value;
            sum += value;
        }

        for (int i = 0; i < kernel.length; i++) {
            kernel[i] /= sum;
        }

        return kernel;
    }


    private static double[] createSavitzkyGolayKernel(int windowSize, int polynomialOrder) {
        final int half = windowSize / 2;
        final int cols = polynomialOrder + 1;

        final double[][] aData = new double[windowSize][cols];
        for (int row = 0; row < windowSize; row++) {
            final int x = row - half;
            double value = 1.0;
            for (int col = 0; col < cols; col++) {
                aData[row][col] = value;
                value *= x;
            }
        }

        final RealMatrix a = new Array2DRowRealMatrix(aData, false);
        final RealMatrix ata = a.transpose().multiply(a);

        final double[] e0Data = new double[cols];
        e0Data[0] = 1.0;
        final RealVector e0 = new ArrayRealVector(e0Data, false);

        final RealVector tmp;
        try {
            final DecompositionSolver solver = new LUDecomposition(ata).getSolver();
            tmp = solver.solve(e0);   // tmp = (A^T A)^-1 * e0
        } catch (SingularMatrixException e) {
            throw new OperatorException("Matrix inversion failed while creating Savitzky-Golay kernel.", e);
        }

        final double[] kernel = a.operate(tmp).toArray();   // kernel = A * tmp

        double sum = 0.0;
        for (double value : kernel) {
            sum += value;
        }
        if (sum != 0.0) {
            for (int ii = 0; ii < kernel.length; ii++) {
                kernel[ii] /= sum;
            }
        }

        return kernel;
    }
}
