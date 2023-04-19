/*
 * $Id: CloudAlgorithm.java,v 1.2 2007/04/25 14:15:31 marcoz Exp $
 *
 * Copyright (C) 2006 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package eu.esa.opt.meris.sdr.cloud;

import com.bc.jnn.Jnn;
import com.bc.jnn.JnnException;
import com.bc.jnn.JnnNet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by marcoz.
 *
 * @author marcoz
 * @version $Revision: 1.2 $ $Date: 2007/04/25 14:15:31 $
 */
@SuppressWarnings("JavaDoc")
public class CloudAlgorithm implements Cloneable {
    private static final String PARAM_1_KEY = "param_1";
    private static final String PARAM_2_KEY = "param_2";
    private static final String VALID_KEY = "validExpression";

    private JnnNet neuralNet;
    private String validExpression;
    private double param1;
    private double param2;
    private double[] minInputValuesNN = new double[15];
    private double[] maxInputValuesNN = new double[15];

    public CloudAlgorithm(File auxDataDir, String configName) throws IOException {
        final File propertiesFile = new File(auxDataDir, configName);
        final InputStream propertiesStream = new FileInputStream(propertiesFile);
        Properties properties = new Properties();
        properties.load(propertiesStream);
        validExpression = properties.getProperty(VALID_KEY, "");
        param1 = Double.parseDouble(properties.getProperty(PARAM_1_KEY));
        param2 = Double.parseDouble(properties.getProperty(PARAM_2_KEY));
        for (int i = 0; i < 15; i++) {
            minInputValuesNN[i] = Double.parseDouble(properties.getProperty("min_" + (i + 1)));
            maxInputValuesNN[i] = Double.parseDouble(properties.getProperty("max_" + (i + 1)));
        }
        final String neuralNetName = properties.getProperty("neural_net");
        final File neuralNetFile = new File(auxDataDir, neuralNetName);
        try {
            loadNeuralNet(neuralNetFile);
        } catch (Exception e) {
            throw new IOException("Failed to load neural net " + neuralNetName + ":\n" + e.getMessage());
        }
    }

    private void loadNeuralNet(File neuralNetFile) throws IOException, JnnException {
        Jnn.setOptimizing(true);
        neuralNet = Jnn.readNna(neuralNetFile);

        // OLD Beam:
//        final Logger logger = BeamLogManager.getSystemLogger();
//        logger.info("Using JNN Neural Net Library, version " + Jnn.VERSION_STRING);
//        logger.info(neuralNetFile + " loaded");
        // Snap: todo

    }

    /**
     * Computes the cloudProbability for one pixel
     * using the given array as input for the neural net.
     *
     * @param cloudIn
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
     * @param cloudIn
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
    protected CloudAlgorithm clone() throws CloneNotSupportedException {
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
