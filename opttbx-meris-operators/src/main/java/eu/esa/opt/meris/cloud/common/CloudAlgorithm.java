/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
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
package eu.esa.opt.meris.cloud.common;

import com.bc.jnn.Jnn;
import com.bc.jnn.JnnNet;
import org.esa.snap.core.util.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Logger;

public class CloudAlgorithm {
    private static final String PARAM_1_KEY = "param_1";
    private static final String PARAM_2_KEY = "param_2";
    private static final String VALID_KEY = "validExpression";

    private JnnNet neuralNet;
    private String validExpression;
    private double param1;
    private double param2;
    private double[] minInputValuesNN = new double[15];
    private double[] maxInputValuesNN = new double[15];

    /**
     * @deprecated since OPTTBX 10.0; use {@link #CloudAlgorithm(Path, String)}  CloudAlgorithm() } instead
     */
    public CloudAlgorithm(File auxDataDir, String configName) throws IOException {
        this(auxDataDir.toPath(), configName);
    }

    public CloudAlgorithm(Path auxDataDir, String configName) throws IOException {
        final Path propertiesFile = auxDataDir.resolve(configName);
        Properties properties = new Properties();
        try (InputStream propertiesStream = Files.newInputStream(propertiesFile)) {
            properties.load(propertiesStream);
        }
        validExpression = properties.getProperty(VALID_KEY, "");
        param1 = Double.parseDouble(properties.getProperty(PARAM_1_KEY));
        param2 = Double.parseDouble(properties.getProperty(PARAM_2_KEY));
        for (int i = 0; i < 15; i++) {
            minInputValuesNN[i] = Double.parseDouble(properties.getProperty("min_" + (i + 1)));
            maxInputValuesNN[i] = Double.parseDouble(properties.getProperty("max_" + (i + 1)));
        }
        loadNeuralNet(auxDataDir, properties.getProperty("neural_net"));
    }

    private void loadNeuralNet(Path auxDataDir, String neuralNetName) throws IOException {
        Jnn.setOptimizing(true);
        final Path neuralNetFile = auxDataDir.resolve(neuralNetName);
        try (BufferedReader reader = Files.newBufferedReader(neuralNetFile)) {
            neuralNet = Jnn.readNna(reader);
            final Logger logger = SystemUtils.LOG;
            logger.info("Using JNN Neural Net Library, version " + Jnn.VERSION_STRING);
            logger.info(neuralNetFile + " loaded");
        } catch (Exception e) {
            throw new IOException("Failed to load neural net " + neuralNetName + ":\n" + e.getMessage());
        }
    }

    public String getValidExpression() {
        return validExpression;
    }

    /**
     * Computes the cloudProbability for one pixel
     * using the given array as input for the neural net.
     *
     * @param cloudIn input parameters
     * @return cloudProbability
     */
    public double computeCloudProbability(double[] cloudIn) {
        // check for input values which are out-of-bounds
        for (int j = 0; j < 15; j++) {
            final double q = cloudIn[j];
            if (q < minInputValuesNN[j]) {
                cloudIn[j] = minInputValuesNN[j];
            } else if (q > maxInputValuesNN[j]) {
                cloudIn[j] = maxInputValuesNN[j];
            }
        }

        double nnResult = computeCloud(cloudIn);
        return nn2Probability(nnResult);
    }

    /**
     * Computes the cloud parameter that can later be converted into a probability.
     *
     * @param cloudIn input parameters
     * @return the cloud parameter
     */
    protected double computeCloud(final double[] cloudIn) {
        final double[] output = new double[1];

        neuralNet.process(cloudIn, output);
        return output[0];
    }

    protected double nn2Probability(double nnResult) {
        double a = (param2 * (nnResult + param1)) * (-1);
        if (a < (-80)) {
            a = -80;
        } else if (a > 80) {
            a = 80;
        }
        return 1.0 / (1.0 + Math.exp(a));
    }

    @Override
    public CloudAlgorithm clone() throws CloneNotSupportedException {
        try {
            CloudAlgorithm clone = (CloudAlgorithm) super.clone();
            clone.neuralNet = neuralNet.clone();
            clone.validExpression = validExpression;
            clone.param1 = param1;
            clone.param2 = param2;
            clone.minInputValuesNN = minInputValuesNN.clone();
            System.arraycopy(minInputValuesNN, 0, clone.minInputValuesNN, 0, minInputValuesNN.length);
            clone.maxInputValuesNN = maxInputValuesNN.clone();
            System.arraycopy(maxInputValuesNN, 0, clone.maxInputValuesNN, 0, maxInputValuesNN.length);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
