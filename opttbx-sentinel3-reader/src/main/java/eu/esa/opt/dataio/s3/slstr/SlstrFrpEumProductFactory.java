package eu.esa.opt.dataio.s3.slstr;

import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.s3.Manifest;
import eu.esa.opt.dataio.s3.Sentinel3ProductReader;
import eu.esa.opt.dataio.s3.slstr.dddb.SlstrDDDB;
import eu.esa.opt.dataio.s3.slstr.dddb.VariableInformation;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import ucar.nc2.NetcdfFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class SlstrFrpEumProductFactory extends SlstrProductFactory {

    private final SlstrDDDB dddb;

    // @todo 1 check if we need this - possibly it is a good concept

    public SlstrFrpEumProductFactory(Sentinel3ProductReader productReader) {
        super(productReader);
        dddb = SlstrDDDB.instance();
    }

    @Override
    public Product createProduct(VirtualDir virtualDir) throws IOException {
        final InputStream manifestInputStream = getManifestInputStream(virtualDir);
        final Manifest manifest = createManifest(manifestInputStream);

        final MetadataElement metadataRoot = manifest.getMetadata();
        final String productType = manifest.getProductType();
        final String processingVersion = manifest.getProcessingVersion();
        final String productName = manifest.getProductName();

        final Dimension productSize = getProductSize(metadataRoot);
        final Product product = new Product(productName, productType, productSize.width, productSize.height);

        final VariableInformation[] variableInformations = dddb.getVariableInformations(productType, processingVersion);
        for (final VariableInformation variableInformation : variableInformations) {
            final int width = getDimension(variableInformation.getSource_file(), variableInformation.getWidth(), virtualDir);
            final int height = getDimension(variableInformation.getSource_file(), variableInformation.getHeight(), virtualDir);


        }

        return product;
    }

    @Override
    protected List<String> getFileNames(Manifest manifest) {
        return manifest.getFileNames("");
    }

    @Override
    protected Double getStartOffset(String gridIndex) {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected Double getTrackOffset(String gridIndex) {
        throw new RuntimeException("not implemented");
    }

    // @todo 3 add tests tb 2024-09-04
    Dimension getProductSize(MetadataElement metadataRoot) {
        // @todo 3 extract method, try to be generic tb 2024-09-04
        final MetadataElement metadataSection = metadataRoot.getElement("metadataSection");
        final MetadataElement slstrProductInformation = metadataSection.getElement("slstrProductInformation");
        final MetadataElement nadirImageSizeElement = slstrProductInformation.getElement("nadirImageSize");

        // @todo 3 extract method tb 2024-09-04
        final NadirImageSize nadirImageSize = new NadirImageSize();
        nadirImageSize.startOffset = nadirImageSizeElement.getAttributeInt("startOffset");
        nadirImageSize.trackOffset = nadirImageSizeElement.getAttributeInt("trackOffset");
        nadirImageSize.rows = nadirImageSizeElement.getAttributeInt("rows");
        nadirImageSize.columns = nadirImageSizeElement.getAttributeInt("columns");

        return new Dimension(nadirImageSize.columns, nadirImageSize.rows);
    }

    private int getDimension(String fileName, String dimensionName, VirtualDir virtualDir) throws IOException {
        File file = virtualDir.getFile(fileName);
        NetcdfFile ncFile = NetcdfFile.open(file.getAbsolutePath());
        List<ucar.nc2.Dimension> dimensions = ncFile.getDimensions();
        for (final ucar.nc2.Dimension dimension : dimensions) {
            if (dimension.getShortName().equals(dimensionName)) {
                return dimension.getLength();
            }
        }

        return -1;
    }
}
