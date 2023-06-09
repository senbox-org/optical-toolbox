/*
 *
 *  * Copyright (C) 2012 Brockmann Consult GmbH (info@brockmann-consult.de)
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package eu.esa.opt.olci.radiometry.gasabsorption;

import com.bc.ceres.core.ProgressMonitor;
import com.google.common.primitives.Doubles;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.OperatorSpiRegistry;
import org.esa.snap.core.util.ResourceInstaller;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.core.util.io.CsvReader;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author muhammad.bc.
 */
public class GaseousAbsorptionAux {

    private List<double[]> ozoneHighs;


    private List<double[]> coeffhighres = new ArrayList<>();

    GaseousAbsorptionAux() {
        try {
            Path installAuxdata = installAuxdata();
            Path resolve = installAuxdata.resolve("ozone-highres.txt");
            FileReader fileReader = new FileReader(resolve.toString());
            CsvReader reader = new CsvReader(fileReader, new char[]{' ', '\t'}, true, "#");
            ozoneHighs = reader.readDoubleRecords();
            coeffhighres = getCoeffhighres(ozoneHighs);
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GaseousAbsorptionAux getInstance() {
        return Holder.instance;
    }

    public List<double[]> getOzoneHighs() {
        return ozoneHighs;
    }

    public List<double[]> getCoeffhighres(List<double[]> ozoneHighs) {
        List<Double> o3wavelengthList = new ArrayList<>();
        List<Double> o3absorptionList = new ArrayList<>();

        for (double[] ozoneHigh : ozoneHighs) {
            o3wavelengthList.add(ozoneHigh[0]);
            o3absorptionList.add(ozoneHigh[1]);
        }
        List<double[]> O3Main = new ArrayList<>();

        O3Main.add(Doubles.toArray(o3wavelengthList));
        O3Main.add(Doubles.toArray(o3absorptionList));
        return O3Main;
    }

    Path installAuxdata() throws IOException {
        OperatorSpiRegistry operatorSpiRegistry = GPF.getDefaultInstance().getOperatorSpiRegistry();
        OperatorSpi spi = operatorSpiRegistry.getOperatorSpi("GaseousAbsorption");
        String version = "v" + spi.getOperatorDescriptor().getVersion();
        Path auxdataDirectory = SystemUtils.getAuxDataPath().resolve("olci/gaseous/" + version);
        final Path sourceDirPath = ResourceInstaller.findModuleCodeBasePath(GaseousAbsorptionAux.class).resolve("auxdata/gaseous");
        final ResourceInstaller resourceInstaller = new ResourceInstaller(sourceDirPath, auxdataDirectory);
        resourceInstaller.install(".*", ProgressMonitor.NULL);
        return auxdataDirectory;
    }


    double convolve(double lower, double upper, List<double[]> coeffhighres) {
        double[] o3absorption = coeffhighres.get(1);
        double[] O3wavelength = coeffhighres.get(0);
        int length = o3absorption.length;
        int weight = 0;
        double totalValue = 0.0;
        for (int i = 0; i < length; i++) {
            if (O3wavelength[i] >= lower && O3wavelength[i] <= upper) {
                weight += 1;
                totalValue += o3absorption[i];
            }
        }
        return weight > 0 ? (totalValue / weight) : 0.0;
    }


    public double[] absorptionOzone(String instrument) {
        List<Number> o3absorpInstrument = new ArrayList<>();

        double[] lamC;
        if (instrument.equals("MERIS")) {
            lamC = new double[]{412.5, 442.0, 490.0, 510.0, 560.0, 620.0, 665.0, 681.25, 708.75, 753.0, 761.25, 779.0, 865.0,
                    885.0, 900};
            double[] lamW = new double[]{10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 3.075, 10.0, 20.0, 20.0, 40.0};
            for (int i = 0; i < lamC.length; i++) {
                double lower = lamC[i] - lamW[i] / 2;
                double upper = lamC[i] + lamW[i] / 2;
                o3absorpInstrument.add(convolve(lower, upper, this.coeffhighres));
            }
        }
        if (instrument.equals("OLCI")) {
            lamC = new double[]{
                    400.0, 412.5, 442.0, 490.0, 510.0, 560.0, 620.0, 665.0, 673.75, 681.25, 708.75, 753.75, 761.25, 764.375, 767.5, 778.75, 865.0,
                    885.0, 900.0, 940.0, 1020.0};
            double[] lamW = new double[]{15.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 7.05, 7.05, 10.0, 7.05, 2.05, 3.075, 2.05, 15.0, 20.0, 10.0, 10.0, 20.0, 40.0};

            for (int i = 0; i < lamC.length; i++) {
                double lower = lamC[i] - lamW[i] / 2;
                double upper = lamC[i] + lamW[i] / 2;
                o3absorpInstrument.add(convolve(lower, upper, this.coeffhighres));
            }
        }
        if (instrument.equals("S2_MSI")) {
            lamC = new double[]{
                    442.0, 490.0, 560.0, 665.0, 705.0, 740.0, 783.0, 842.0, 865.0, 945.0,
                    1375.0, 1610.0, 2190.0};
            double[] lamW = new double[]{20.0, 65.0, 35.0, 30.0, 15.0, 15.0, 20.0, 115.0, 20.0, 20.0,
                    30.0, 90.0, 180.0}; // http://www.gdal.org/frmt_sentinel2.html

            for (int i = 0; i < lamC.length; i++) {
                double lower = lamC[i] - lamW[i] / 2;
                double upper = lamC[i] + lamW[i] / 2;
                o3absorpInstrument.add(convolve(lower, upper, this.coeffhighres));
            }
        }

        return Doubles.toArray(o3absorpInstrument);
    }

    private static class Holder {

        private static final GaseousAbsorptionAux instance = new GaseousAbsorptionAux();

        private Holder() {
        }
    }
}
