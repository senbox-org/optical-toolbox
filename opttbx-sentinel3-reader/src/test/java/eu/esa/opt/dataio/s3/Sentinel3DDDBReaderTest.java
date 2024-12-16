package eu.esa.opt.dataio.s3;

import com.bc.ceres.annotation.STTM;
import com.bc.ceres.core.ProgressMonitor;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import org.esa.snap.core.dataio.IllegalFileFormatException;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.dataio.ProductSubsetDef;
import org.esa.snap.core.datamodel.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Sentinel3DDDBReaderTest {



    private static ProductReader getProductReader() {
        return new ProductReader() {
            @Override
            public ProductReaderPlugIn getReaderPlugIn() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public Object getInput() {
                return "S3A_OL_1_EFR____20240526T155849_20240526T160149_20240526T174356_0179_112_382_2700_PS1_O_NR_004.SEN3/xfdumanifest.xml";
            }

            @Override
            public ProductSubsetDef getSubsetDef() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public Product readProductNodes(Object input, ProductSubsetDef subsetDef) throws IOException, IllegalFileFormatException {
                throw new RuntimeException("not implemented");
            }

            @Override
            public GeoCoding readGeoCoding(Product product) throws IOException {
                throw new RuntimeException("not implemented");
            }

            @Override
            public void readBandRasterData(Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {
                throw new RuntimeException("not implemented");
            }

            @Override
            public void close() throws IOException {
                throw new RuntimeException("not implemented");
            }
        };
    }

    private static Manifest createManifest() {
        return new Manifest() {
            @Override
            public String getProductName() {
                return "S3A_OL_1_EFR____20240526T155849_20240526T160149_20240526T174356_0179_112_382_2700_PS1_O_NR_004.SEN3";
            }

            @Override
            public String getProductType() {
                return "OL_1_EFR";
            }

            @Override
            public String getBaselineCollection() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public String getDescription() {
                return "SENTINEL-3 OLCI Level 1 Earth Observation Full Resolution Product";
            }

            @Override
            public ProductData.UTC getStartTime() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public ProductData.UTC getStopTime() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public List<String> getFileNames(String schema) {
                throw new RuntimeException("not implemented");
            }

            @Override
            public List<String> getFileNames(String[] excluded) {
                throw new RuntimeException("not implemented");
            }

            @Override
            public MetadataElement getMetadata() {
                throw new RuntimeException("not implemented");
            }

            @Override
            public String getXPathString(String xPath) {
                throw new RuntimeException("not implemented");
            }

            @Override
            public int getXPathInt(String xPath) {
                throw new RuntimeException("not implemented");
            }
        };
    }
}
