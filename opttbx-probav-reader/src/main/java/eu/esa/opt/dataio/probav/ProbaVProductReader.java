package eu.esa.opt.dataio.probav;

import com.bc.ceres.core.Assert;
import com.bc.ceres.core.ProgressMonitor;
import hdf.object.FileFormat;
import hdf.object.Group;
import hdf.object.HObject;
import hdf.object.h5.H5Datatype;
import hdf.object.h5.H5Group;
import hdf.object.h5.H5ScalarDS;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Reader for Proba-V L2A products
 *
 * @author olafd
 */
public class ProbaVProductReader extends AbstractProductReader {

    private int productWidth;
    private int productHeight;

    private long file_id;

    private String probavProductType;  // 'LEVEL3' (Synthesis) or 'LEVEL2A'

    private boolean isLevel3TocProduct;
    private boolean isLevel3NdviProduct;

    private HashMap<String, Hdf5DatasetVar> datasetVars;

    /**
     * Constructs a new abstract product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader
     *                     implementations
     */
    protected ProbaVProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        final Object inputObject = getInput();
        final ProbaVFile probavFile = ProbaVProductReaderPlugIn.getProbaVFile(inputObject);

        try {
            final FileFormat h5File = probavFile.getH5File();
            file_id = probavFile.getFileId();

            final Group probaVRootNode = (Group) h5File.getRootObject();
            final Group probavTypeNode = (Group) probaVRootNode.getMember(0);
            probavProductType = probavTypeNode.getName();
            isLevel3TocProduct = (probavProductType.equals("LEVEL3")) && ProbaVUtils.isLevel3Toc(probavTypeNode);
            isLevel3NdviProduct = (probavProductType.equals("LEVEL3")) && ProbaVUtils.isLevel3Ndvi(probavTypeNode);

            return createTargetProduct(probavFile, probaVRootNode);
        } catch (Exception e) {
            throw new IOException("Failed to open file '" + probavFile.getPath() + "': " + e.getMessage(), e);
        }
    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX,
                                          int sourceOffsetY,
                                          int sourceWidth,
                                          int sourceHeight,
                                          int sourceStepX,
                                          int sourceStepY,
                                          Band targetBand,
                                          int targetOffsetX,
                                          int targetOffsetY,
                                          int targetWidth,
                                          int targetHeight,
                                          ProductData targetBuffer,
                                          ProgressMonitor pm) {

        Assert.state(sourceOffsetX == targetOffsetX, "sourceOffsetX != targetOffsetX");
        Assert.state(sourceOffsetY == targetOffsetY, "sourceOffsetY != targetOffsetY");
        Assert.state(sourceStepX == 1, "sourceStepX != 1");
        Assert.state(sourceStepY == 1, "sourceStepY != 1");
        Assert.state(sourceWidth == targetWidth, "sourceWidth != targetWidth");
        Assert.state(sourceHeight == targetHeight, "sourceHeight != targetHeight");

        final Hdf5DatasetVar datasetVar = datasetVars.get(targetBand.getName());
        synchronized (datasetVar) {
            if (datasetVar.name().equals("/" + probavProductType + "/QUALITY/" + ProbaVConstants.SM_BAND_NAME) &&
                    targetBand.getName().equals(ProbaVConstants.SM_FLAG_BAND_NAME)) {
                ProductData tmpBuffer =
                        ProbaVUtils.getDataBufferForH5Dread(datasetVar.type(), targetWidth, targetHeight);
                ProbaVUtils.readProbaVData(file_id,
                        targetWidth, targetHeight,
                        targetOffsetX, targetOffsetY,
                        datasetVar.name(),
                        datasetVar.type(),
                        tmpBuffer);
                ProbaVFlags.setSmFlagBuffer(targetBuffer, tmpBuffer, probavProductType);
            } else {
                ProbaVUtils.readProbaVData(file_id,
                        targetWidth, targetHeight,
                        targetOffsetX, targetOffsetY,
                        datasetVar.name(),
                        datasetVar.type(),
                        targetBuffer);
            }
        }
    }

    //////////// private methods //////////////////

    private Product createTargetProduct(File inputFile, Group inputFileRootNode) throws Exception {
        Product product = null;

        if (inputFileRootNode != null) {
            final Group productTypeNode = (Group) inputFileRootNode.getMember(0);        // 'LEVEL2A'

            // get dimensions either from GEOMETRY/SAA or for NDVI products from NDVI/NDVI
            final int productTypeNodeStartIndex = isLevel3NdviProduct ? 1 : 0;
            final int rasterNodeStartIndex = probavProductType.equals("LEVEL3") ? 0 : 1;

            final Group productDataGroup = (Group) productTypeNode.getMember(productTypeNodeStartIndex);
            productWidth = (int) ProbaVUtils.getH5ScalarDS(productDataGroup.getMember(rasterNodeStartIndex)).getDims()[1];   // take from SAA
            productHeight = (int) ProbaVUtils.getH5ScalarDS(productDataGroup.getMember(rasterNodeStartIndex)).getDims()[0];
            product = new Product(inputFile.getName(), "PROBA-V " + probavProductType, productWidth, productHeight);
            product.setPreferredTileSize(productWidth, 16);
            product.setAutoGrouping("TOA_REFL:TOC_REFL:VAA:VZA");

            datasetVars = new HashMap<>(32);

            final H5Group rootGroup = (H5Group) inputFileRootNode;
            final List<?> rootMetadata = rootGroup.getMetadata();
            ProbaVUtils.addMetadataElementWithAttributes(rootMetadata, product.getMetadataRoot(), ProbaVConstants.MPH_NAME);
            product.setDescription(ProbaVUtils.getStringAttributeValue(rootMetadata, "DESCRIPTION"));
            ProbaVUtils.addStartStopTimes(product, inputFileRootNode);
            product.setFileLocation(inputFile);

            for (int i = 0; i < productTypeNode.getNumberOfMembersInFile(); i++) {
                // we have: 'GEOMETRY', 'NDVI', 'QUALITY', 'RADIOMETRY', 'TIME'
                final HObject productTypeChildNode = productTypeNode.getMember(i);
                final String productTypeChildNodeName = productTypeChildNode.getName();


                switch (productTypeChildNodeName) {
                    case ProbaVConstants.GEOMETRY_BAND_GROUP_NAME ->
                            createGeometryBand(inputFileRootNode, product, productTypeChildNode);
                    case ProbaVConstants.NDVI_BAND_GROUP_NAME -> {
                        // only present in LEVEL3 Synthesis products
                        if (probavProductType.equals("LEVEL2A")) {
                            break;
                        }
                        createNdviBand(product, productTypeChildNode);
                    }
                    case ProbaVConstants.QUALITY_BAND_GROUP_NAME -> createQualityBand(product, productTypeChildNode);
                    case ProbaVConstants.RADIOMETRY_BAND_GROUPNAME ->
                            createRadiometryBand(product, productTypeChildNode);
                    case ProbaVConstants.TIME_BAND_GROUPNAME -> {
                        // only present in LEVEL3 Synthesis products

                        // add start/end time to product:
                        ProbaVUtils.addStartStopTimes(product, productTypeChildNode);
                        if (isLevel3NdviProduct) {
                            // empty in NDVI products
                            break;
                        }
                        createTimeBand(product, productTypeChildNode);
                    }
                    default -> {
                    }
                }
            }
        }

        return product;
    }

    private void createTimeBand(Product product, HObject productTypeChildNode) throws Exception {
        final Group parentNode = (Group) productTypeChildNode;
        final H5ScalarDS timeDS = ProbaVUtils.getH5ScalarDS(parentNode.getMember(0));
        final Band timeBand;
        // NOTE: it seems that identical product types may have different data types here. E.g.:
        // PROBAV_S1_TOC_X18Y06_20140316_100M_V001.HDF5 has 8-bit unsigned char (CLASS_CHAR), but
        // PROBAV_S1_TOA_X18Y02_20140902_100M_V001.HDF5 has 16-bit unsigned integer (CLASS_INTEGER)
        final int timeDatatypeClass = timeDS.getDatatype().getDatatypeClass();   // 0
        if (timeDatatypeClass == H5Datatype.CLASS_CHAR) {
            // 8-bit unsigned character in this case
            timeBand = ProbaVUtils.createTargetBand(product, timeDS.getMetadata(), "TIME", ProductData.TYPE_UINT8);
            timeBand.setNoDataValue(ProbaVConstants.TIME_NO_DATA_VALUE_UINT8);
        } else {
            // 16-bit unsigned integer
            timeBand = ProbaVUtils.createTargetBand(product, timeDS.getMetadata(), "TIME", ProductData.TYPE_UINT16);
            timeBand.setNoDataValue(ProbaVConstants.TIME_NO_DATA_VALUE_UINT16);
        }
        ProbaVUtils.setBandUnitAndDescription(timeDS.getMetadata(), timeBand);
        timeBand.setNoDataValueUsed(true);

        final String timeDatasetName = "/LEVEL3/TIME/TIME";
        datasetVars.put(timeBand.getName(), new Hdf5DatasetVar(timeDatasetName,
                timeDatatypeClass));

        ProbaVUtils.addBandSubGroupMetadata(product, parentNode, ProbaVConstants.TIME_BAND_GROUPNAME);
    }

    private void createQualityBand(Product product, HObject productTypeChildNode) throws Exception {
        final H5Group parentNode = (H5Group) productTypeChildNode;
        if (isLevel3NdviProduct) {
            // add metadata element only
            final List<?> metadata = parentNode.getMetadata();
            ProbaVUtils.addMetadataElementWithAttributes(metadata, product.getMetadataRoot(), ProbaVConstants.NDVI_BAND_GROUP_NAME);
            return;
        }
        final H5ScalarDS qualityDS = ProbaVUtils.getH5ScalarDS(parentNode.getMember(0));

        FlagCoding probavSmFlagCoding = new FlagCoding(ProbaVConstants.SM_FLAG_BAND_NAME);
        ProbaVFlags.addQualityFlags(probavSmFlagCoding, probavProductType);
        ProbaVFlags.addQualityMasks(product, probavProductType);
        product.getFlagCodingGroup().add(probavSmFlagCoding);
        final Band smFlagBand = product.addBand(ProbaVConstants.SM_FLAG_BAND_NAME, ProductData.TYPE_INT16);
        smFlagBand.setDescription("PROBA-V SM Flags");
        smFlagBand.setSampleCoding(probavSmFlagCoding);

        final String qualityDatasetName = "/" + probavProductType + "/QUALITY/" + ProbaVConstants.SM_BAND_NAME;
        final int qualityDatatypeClass = qualityDS.getDatatype().getDatatypeClass();
        datasetVars.put(smFlagBand.getName(),
                new Hdf5DatasetVar(qualityDatasetName,
                        qualityDatatypeClass));

        ProbaVUtils.addBandSubGroupMetadata(product, parentNode, ProbaVConstants.QUALITY_BAND_GROUP_NAME);
    }

    private void createGeometryBand(HObject inputFileRootNode, Product product, HObject productTypeChildNode) throws Exception {
        ProbaVUtils.setProbaVGeoCoding(product, inputFileRootNode, productTypeChildNode,
                productWidth, productHeight);
        if (isLevel3NdviProduct) {
            return;
        }

        // 8-bit unsigned character
        // skip 'CONTOUR' in case of LEVEL2A
        final int childNodeStartIndex = probavProductType.equals("LEVEL3") ? 0 : 1;
        final Group parentNode = (Group) productTypeChildNode;
        ProbaVUtils.addRootMetadataElement(product, parentNode, ProbaVConstants.GEOMETRY_BAND_GROUP_NAME);
        final MetadataElement rootMetadataElement = product.getMetadataRoot().getElement(ProbaVConstants.GEOMETRY_BAND_GROUP_NAME);

        for (int j = childNodeStartIndex; j < parentNode.getNumberOfMembersInFile(); j++) {
            final Group geometryChildNode = (Group) parentNode.getMember(j);
            final String geometryChildNodeName = geometryChildNode.getName();

            if (ProbaVUtils.isProbaVSunAngleDataNode(geometryChildNodeName)) {
                final H5ScalarDS sunAngleDS = ProbaVUtils.getH5ScalarDS(geometryChildNode);
                final Band sunAngleBand = ProbaVUtils.createTargetBand(product,
                        sunAngleDS.getMetadata(),
                        geometryChildNodeName,
                        ProductData.TYPE_UINT8);
                ProbaVUtils.setBandUnitAndDescription(sunAngleDS.getMetadata(), sunAngleBand);
                sunAngleBand.setNoDataValue(ProbaVConstants.GEOMETRY_NO_DATA_VALUE);
                sunAngleBand.setNoDataValueUsed(true);

                final String sunAngleDatasetName = "/" + probavProductType + "/GEOMETRY/" + geometryChildNodeName;
                final int sunAngleDatatypeClass = sunAngleDS.getDatatype().getDatatypeClass();   // 0
                datasetVars.put(sunAngleBand.getName(), new Hdf5DatasetVar(sunAngleDatasetName,
                        sunAngleDatatypeClass));

                final H5ScalarDS sunAngleDs = ProbaVUtils.getH5ScalarDS(geometryChildNode);
                final List<?> childGeometryMetadata = sunAngleDs.getMetadata();
                ProbaVUtils.addMetadataElementWithAttributes(childGeometryMetadata, rootMetadataElement, geometryChildNodeName);
            } else if (ProbaVUtils.isProbaVViewAngleGroupNode(geometryChildNodeName)) {
                for (int k = 0; k < geometryChildNode.getNumberOfMembersInFile(); k++) {
                    final HObject geometryViewAngleChildNode = geometryChildNode.getMember(k);
                    final H5ScalarDS viewAngleDS = ProbaVUtils.getH5ScalarDS(geometryViewAngleChildNode);
                    final String geometryViewAngleChildNodeName =
                            geometryViewAngleChildNode.getName();
                    final String viewAngleBandName = geometryViewAngleChildNodeName + "_" +
                            geometryChildNodeName;
                    final Band viewAngleBand = ProbaVUtils.createTargetBand(product,
                            viewAngleDS.getMetadata(),
                            viewAngleBandName,
                            ProductData.TYPE_UINT8);
                    ProbaVUtils.setBandUnitAndDescription(viewAngleDS.getMetadata(), viewAngleBand);
                    viewAngleBand.setNoDataValue(ProbaVConstants.GEOMETRY_NO_DATA_VALUE);
                    viewAngleBand.setNoDataValueUsed(true);

                    final String viewAngleDatasetName = "/" + probavProductType + "/GEOMETRY/" +
                            geometryChildNodeName + "/" + geometryViewAngleChildNodeName;
                    final int viewAngleDatatypeClass = viewAngleDS.getDatatype().getDatatypeClass();   // 0
                    datasetVars.put(viewAngleBand.getName(), new Hdf5DatasetVar(viewAngleDatasetName,
                            viewAngleDatatypeClass));

                    final H5ScalarDS viewAngleDs = ProbaVUtils.getH5ScalarDS(geometryViewAngleChildNode);
                    final List<?> childGeometryMetadata = viewAngleDs.getMetadata();
                    ProbaVUtils.addMetadataElementWithAttributes(childGeometryMetadata, rootMetadataElement, viewAngleBandName);
                }
            }
        }
    }

    private void createRadiometryBand(Product product, HObject productTypeChildNode) throws Exception {
        // 16-bit integer
        final String radiometryBandPrePrefix = isLevel3TocProduct ? "TOC" : "TOA";
        final Group parentNode = (Group) productTypeChildNode;
        ProbaVUtils.addRootMetadataElement(product, parentNode, ProbaVConstants.RADIOMETRY_BAND_GROUPNAME);
        final MetadataElement rootMetadataElement =
                product.getMetadataRoot().getElement(ProbaVConstants.RADIOMETRY_BAND_GROUPNAME);

        //  blue, nir, red, swir:
        for (int j = 0; j < parentNode.getNumberOfMembersInFile(); j++) {
            // we want the sequence BLUE, RED, NIR, SWIR, rather than original BLUE, NIR, RED, SWIR...
            final int k = ProbaVConstants.RADIOMETRY_CHILD_INDEX[j];
            final Group radiometryChildNode = (Group) parentNode.getMember(k);
            final H5ScalarDS radiometryDS = ProbaVUtils.getH5ScalarDS(radiometryChildNode.getMember(0));
            final String radiometryChildNodeName = radiometryChildNode.getName();
            final String radiometryBandPrefix = radiometryBandPrePrefix + "_REFL_";
            final String reflBandName = radiometryBandPrefix + radiometryChildNodeName;
            final Band radiometryBand = ProbaVUtils.createTargetBand(product,
                    radiometryDS.getMetadata(),
                    reflBandName,
                    ProductData.TYPE_INT16);
            ProbaVUtils.setBandUnitAndDescription(radiometryDS.getMetadata(), radiometryBand);
            ProbaVUtils.setSpectralBandProperties(radiometryChildNode, radiometryBand);
            radiometryBand.setNoDataValue(ProbaVConstants.RADIOMETRY_NO_DATA_VALUE);
            radiometryBand.setNoDataValueUsed(true);

            final String radiometryDatasetName = "/" + probavProductType + "/RADIOMETRY/" +
                    radiometryChildNodeName + "/" + radiometryBandPrePrefix;
            final int radiometryDatatypeClass = radiometryDS.getDatatype().getDatatypeClass();
            datasetVars.put(radiometryBand.getName(),
                    new Hdf5DatasetVar(radiometryDatasetName,
                            radiometryDatatypeClass));

            // add metadata:
            final MetadataElement childMetadataElement = new MetadataElement(radiometryChildNodeName);
            final H5Group childGroup = (H5Group) radiometryChildNode;
            final List<?> childMetadata = childGroup.getMetadata();
            ProbaVUtils.addMetadataAttributes(childMetadata, childMetadataElement);
            for (int m = 0; m < radiometryChildNode.getNumberOfMembersInFile(); m++) {
                // e.g. BLUE-->TOA
                final HObject childChildNode = radiometryChildNode.getMember(m);
                final H5ScalarDS viewAngleDs = ProbaVUtils.getH5ScalarDS(childChildNode);
                final List<?> childChildMetadata = viewAngleDs.getMetadata();
                ProbaVUtils.addMetadataElementWithAttributes(childChildMetadata, rootMetadataElement, reflBandName);
            }
        }
    }

    private void createNdviBand(Product product, HObject productTypeChildNode) throws Exception {
        // 8-bit unsigned character
        final Group parentNode = (Group) productTypeChildNode;
        final H5ScalarDS ndviDS = (H5ScalarDS) parentNode.getMember(0);
        final Band ndviBand = ProbaVUtils.createTargetBand(product, ndviDS.getMetadata(), "NDVI", ProductData.TYPE_UINT8);

        ndviBand.setDescription("Normalized Difference Vegetation Index");
        ndviBand.setUnit("dl");
        ndviBand.setNoDataValue(ProbaVConstants.NDVI_NO_DATA_VALUE);
        ndviBand.setNoDataValueUsed(true);

        final String ndviDatasetName = "/LEVEL3/NDVI/NDVI";
        final int ndviDatatypeClass = ndviDS.getDatatype().getDatatypeClass();

        datasetVars.put(ndviBand.getName(), new Hdf5DatasetVar(ndviDatasetName,
                ndviDatatypeClass));

        ProbaVUtils.addBandSubGroupMetadata(product, parentNode, ProbaVConstants.NDVI_BAND_GROUP_NAME);
    }

    private record Hdf5DatasetVar(String name, int type) {

    }

}
