package eu.esa.opt.dataio.s3;

import com.bc.ceres.annotation.STTM;
import com.bc.ceres.core.ProgressMonitor;
import eu.esa.opt.dataio.s3.dddb.ProductDescriptor;
import eu.esa.opt.dataio.s3.dddb.VariableDescriptor;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import org.esa.snap.core.dataio.IllegalFileFormatException;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.dataio.ProductSubsetDef;
import org.esa.snap.core.datamodel.*;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class Sentinel3DDDBReaderTest {

    @Test
    @STTM("SNAP-3711")
    public void testEnsureWithAndHeight_alreadySet() {
        final ProductDescriptor productDescriptor = new ProductDescriptor();
        productDescriptor.setWidth(27);
        productDescriptor.setHeight(38);

        final Manifest manifest = createManifest();
        Sentinel3DDDBReader.ensureWidthAndHeight(productDescriptor, manifest);

        assertEquals(27, productDescriptor.getWidth());
        assertEquals(38, productDescriptor.getHeight());
    }

    @Test
    @STTM("SNAP-3711")
    public void testEnsureWithAndHeight_fromXPath() {
        final ProductDescriptor productDescriptor = new ProductDescriptor();
        productDescriptor.setWidthXPath("/data/simple/width");
        productDescriptor.setHeightXPath("/data/simple/height");

        final Manifest manifest = createManifest();
        Sentinel3DDDBReader.ensureWidthAndHeight(productDescriptor, manifest);

        assertEquals(108, productDescriptor.getWidth());
        assertEquals(176, productDescriptor.getHeight());
    }

    @Test
    @STTM("SNAP-3711")
    public void testCreateDescriptorKey() {
        final VariableDescriptor descriptor = new VariableDescriptor();
        descriptor.setName("Elfriede");

        assertEquals("Elfriede", Sentinel3DDDBReader.createDescriptorKey(descriptor));
    }

    @Test
    @STTM("SNAP-3711")
    public void testClose() throws IOException {
        Sentinel3DDDBReader sentinel3DDDBReader = new Sentinel3DDDBReader(new Sentinel3ProductReaderPlugIn());

        sentinel3DDDBReader.close();
    }

    @Test
    @STTM("SNAP-3711")
    public void testBandNameToKey() throws IOException {
        assertEquals("Oa01", Sentinel3DDDBReader.bandNameToKey("Oa01_radiance"));
        assertEquals("Oa02", Sentinel3DDDBReader.bandNameToKey("Oa02_radiance_unc"));
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
                if (xPath.contains("width")) {
                    return 108;
                } else if (xPath.contains("height")) {
                    return 176;
                }

                throw new RuntimeException("unexpected value");
            }
        };
    }
}
