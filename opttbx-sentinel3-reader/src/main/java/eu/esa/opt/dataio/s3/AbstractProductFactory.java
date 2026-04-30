package eu.esa.opt.dataio.s3;/*
 * Copyright (C) 2012 Brockmann Consult GmbH (info@brockmann-consult.de)
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

import com.bc.ceres.core.VirtualDir;
import com.bc.ceres.multilevel.MultiLevelImage;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.opt.dataio.s3.manifest.XfduManifest;
import eu.esa.opt.dataio.s3.util.ColorProvider;
import eu.esa.opt.dataio.s3.util.S3NetcdfReader;
import eu.esa.opt.dataio.s3.util.S3Util;
import eu.esa.snap.core.dataio.cache.DataBuffer;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static eu.esa.opt.dataio.s3.util.S3Util.getFileFromVirtualDir;


public abstract class AbstractProductFactory implements ProductFactory {

    private final static Color[] uncertainty_colors = {
            new Color(0, 0, 0),
            new Color(255, 255, 255)
    };
    private final List<Product> openProductList = new ArrayList<>();
    private final Sentinel3ProductReader productReader;
    private final Logger logger;
    private final List<String> separatingDimensions;

    private volatile Manifest manifest;
    private final ColorProvider colorProvider;

    private final Map<String, S3NetcdfReader> bandCacheMap = new HashMap<>();
    private final Map<String, TpgEntry> tpgReaderMap = new HashMap<>();


    public AbstractProductFactory(Sentinel3ProductReader productReader) {
        this.productReader = productReader;
        this.logger = Logger.getLogger(getClass().getSimpleName());
        separatingDimensions = new ArrayList<>();
        colorProvider = new ColorProvider();
    }

    protected static Band copyBand(Band sourceBand, Product targetProduct, boolean copySourceImage) {
        return ProductUtils.copyBand(sourceBand.getName(), sourceBand.getProduct(), targetProduct, copySourceImage);
    }

    /** Returns an unmodifiable view of the band → reader mapping. */
    protected Map<String, S3NetcdfReader> getBandCacheMap() {
        return Collections.unmodifiableMap(bandCacheMap);
    }

    /** Returns an unmodifiable view of the TPG → cache-entry mapping. */
    Map<String, TpgEntry> getTpgReaderMap() {
        return Collections.unmodifiableMap(tpgReaderMap);
    }

    @Override
    public final Product createProduct(VirtualDir virtualDir) throws IOException {
        final InputStream manifestInputStream = getManifestInputStream(virtualDir);
        manifest = createManifest(manifestInputStream);

        final List<String> fileNames = getFileNames(manifest);
        final List<String> ensuredNames = removeLeadingSlash(fileNames);

        // @todo 1 tb/** replace this logic with something faster 2025-02-06
        readProducts(ensuredNames, virtualDir);

        final String productName = getProductName();
        final String productType = manifest.getProductType();
        final Product masterProduct = findMasterProduct();
        final int w = getSceneRasterWidth(masterProduct);
        final int h = masterProduct.getSceneRasterHeight();
        final Product targetProduct = new Product(productName, productType, w, h, productReader);
        targetProduct.setDescription(manifest.getDescription());
        targetProduct.setFileLocation(getInputFile());
        targetProduct.setNumResolutionsMax(masterProduct.getNumResolutionsMax());
        targetProduct.setPreferredTileSize(masterProduct.getPreferredTileSize());

        if (masterProduct.getSceneGeoCoding() instanceof CrsGeoCoding) {
            ProductUtils.copyGeoCoding(masterProduct, targetProduct);
        }
        targetProduct.getMetadataRoot().addElement(manifest.getMetadata());
        processProductSpecificMetadata(manifest.getMetadata().getElement("metadataSection"));
        addProductSpecificMetadata(targetProduct);
        addDataNodes(masterProduct, targetProduct);
        addSpecialVariables(masterProduct, targetProduct);
        setMasks(targetProduct);
        setTimes(targetProduct);
        setUncertaintyBands(targetProduct);
        if (targetProduct.getSceneGeoCoding() == null) {
            fixTiePointGrids(targetProduct);
            setGeoCoding(targetProduct);
        }
        setSceneTransforms(targetProduct);
        setBandGeoCodings(targetProduct);
        final Product[] sourceProducts = openProductList.toArray(new Product[0]);
        setAutoGrouping(sourceProducts, targetProduct);
        setTimeCoding(targetProduct, virtualDir);

        return targetProduct;
    }

    static List<String> removeLeadingSlash(List<String> fileNames) {
        final List<String> ensuredNames = new ArrayList<>();
        for (String origName : fileNames) {
            if (origName.startsWith("./")) {
                ensuredNames.add(origName.substring(2));
            } else {
                ensuredNames.add(origName);
            }
        }
        return ensuredNames;
    }

    @Override
    public void dispose() throws IOException {
        bandCacheMap.clear();
        tpgReaderMap.clear();

        openProductList.forEach(Product::dispose);
        openProductList.clear();
    }

    protected final Logger getLogger() {
        return logger;
    }

    @Override
    public MultiLevelImage getImageForTpg(String tpgName) {
        return null;
    }


    protected TiePointGrid copyBandAsTiePointGrid(Band sourceBand, Product targetProduct, int subSamplingX, int subSamplingY, double offsetX, double offsetY) {
        final String unit = sourceBand.getUnit();

        double newOffsetX = offsetX % subSamplingX;
        int dataOffsetX = (int) Math.round((newOffsetX - offsetX) / subSamplingX);
        double newWidth = Math.min(sourceBand.getRasterWidth(), Math.ceil((targetProduct.getSceneRasterWidth() - newOffsetX) / subSamplingX));

        double newOffsetY = offsetY % subSamplingY;
        int dataOffsetY = (int) Math.round((newOffsetY - offsetY) / subSamplingY);
        double newHeight = Math.min(sourceBand.getRasterHeight(), Math.ceil((targetProduct.getSceneRasterHeight() - newOffsetY) / subSamplingY));

        final String bandName = sourceBand.getName();

        final TiePointGrid tiePointGrid = new TiePointGrid(bandName,
                (int) newWidth, (int) newHeight,
                newOffsetX, newOffsetY,
                subSamplingX, subSamplingY);
        if (unit != null && unit.toLowerCase().contains("degree")) {
            tiePointGrid.setDiscontinuity(TiePointGrid.DISCONT_AUTO);
        }
        tiePointGrid.setDescription(sourceBand.getDescription());
        tiePointGrid.setGeophysicalNoDataValue(sourceBand.getGeophysicalNoDataValue());
        tiePointGrid.setNoDataValueUsed(sourceBand.isNoDataValueUsed());
        tiePointGrid.setUnit(unit);
        targetProduct.addTiePointGrid(tiePointGrid);

        final ProductReader reader = sourceBand.getProduct().getProductReader();
        if (reader instanceof S3NetcdfReader) {
            final String cacheKey = getTpgCacheKey(bandName);

            tpgReaderMap.put(bandName, new TpgEntry((S3NetcdfReader) reader, sourceBand, dataOffsetX, dataOffsetY,
                    (int) newWidth, (int) newHeight, cacheKey));
        } else {
            logger.warning("Cannot register TPG '" + bandName + "' with cache: " +
                    "source band product reader is not an S3NetcdfReader.");
        }

        return tiePointGrid;
    }

    protected void setSceneTransforms(Product product) {
    }

    protected void setBandGeoCodings(Product targetProduct) throws IOException {
    }

    protected void fixTiePointGrids(Product targetProduct) {
    }

    protected void setUncertaintyBands(Product product) {
        final Band[] bands = product.getBands();
        for (Band band : bands) {
            final String bandName = band.getName();
            final String errorBandName = bandName + "_err";
            final String uncBandName = bandName + "_unc";
            final String uncertaintyBandName = bandName + "_uncertainty";
            if (product.containsBand(errorBandName)) {
                final Band errorBand = product.getBand(errorBandName);
                band.addAncillaryVariable(errorBand, "error");
            } else if (product.containsBand(uncertaintyBandName)) {
                final Band uncertaintyBand = product.getBand(uncertaintyBandName);
                band.addAncillaryVariable(uncertaintyBand, "uncertainty");
            } else if (product.containsBand(uncBandName)) {
                final Band uncBand = product.getBand(uncBandName);
                band.addAncillaryVariable(uncBand, "uncertainty");
            }
        }
    }

    protected void processProductSpecificMetadata(MetadataElement metadataElement) {
    }

    protected void addProductSpecificMetadata(Product targetProduct) {
        for (final Product sourceProduct : openProductList) {
            MetadataElement root = targetProduct.getMetadataRoot();
            for (final MetadataElement element : sourceProduct.getMetadataRoot().getElement("Variable_Attributes").getElements()) {
                if (!root.containsElement(element.getDisplayName())) {
                    root.addElement(element.createDeepClone());
                }
            }
        }
    }

    protected int getSceneRasterWidth(Product masterProduct) {
        return masterProduct.getSceneRasterWidth();
    }

    protected void addSpecialVariables(Product masterProduct, Product targetProduct) throws IOException {
    }

    protected Product findMasterProduct() {
        return openProductList.get(0);
    }

    protected final List<Product> getOpenProductList() {
        return Collections.unmodifiableList(openProductList);
    }

    protected void setMasks(Product targetProduct) {
        final Band[] bands = targetProduct.getBands();
        for (Band band : bands) {
            final SampleCoding sampleCoding = band.getSampleCoding();
            if (sampleCoding != null) {
                final String bandName = band.getName();
                if (bandName.endsWith("_index")) {
                    continue;
                }
                final boolean isFlagBand = band.isFlagBand();
                for (int i = 0; i < sampleCoding.getNumAttributes(); i++) {
                    final String sampleName = sampleCoding.getSampleName(i);
                    final int sampleValue = sampleCoding.getSampleValue(i);
                    if (!"spare".equals(sampleName)) {
                        final String expression;
                        if (isFlagBand) {
                            expression = bandName + "." + sampleName;
                        } else {
                            expression = bandName + " == " + sampleValue;
                        }
                        final String maskName = bandName + "_" + sampleName;
                        final Color maskColor = getColorProvider().getMaskColor(sampleName);
                        targetProduct.addMask(maskName, expression, expression, maskColor, 0.5);
                    }
                }
            }
        }
    }

    protected ColorProvider getColorProvider() {
        return colorProvider;
    }

    protected Band addBand(Band sourceBand, Product targetProduct) {
        return copyBand(sourceBand, targetProduct, sourceBand.isSourceImageSet());
    }

    // package access for testing only tb 2025-04-08
    static ColorPaletteDef getCounterWaterColorPalette() {
        final ColorPaletteDef.Point[] points = new ColorPaletteDef.Point[8];
        points[0] = new ColorPaletteDef.Point(0.0, new Color(145, 70, 15));
        points[1] = new ColorPaletteDef.Point(1.0, new Color(145, 110, 15));
        points[2] = new ColorPaletteDef.Point(2.0, new Color(185, 160, 50));
        points[3] = new ColorPaletteDef.Point(3.0, new Color(135, 145, 105));
        points[4] = new ColorPaletteDef.Point(4.0, new Color(130, 160, 95));
        points[5] = new ColorPaletteDef.Point(5.0, new Color(95, 160, 120));
        points[6] = new ColorPaletteDef.Point(6.0, new Color(25, 140, 180));
        points[7] = new ColorPaletteDef.Point(7.0, new Color(0,0,0));
        return new ColorPaletteDef(points);
    }

    protected RasterDataNode addSpecialNode(Product masterProduct, Band sourceBand, Product targetProduct) {
        return null;
    }

    protected void setGeoCoding(Product targetProduct) throws IOException {
    }

    protected void configureTargetNode(Band sourceBand, RasterDataNode targetNode) {
    }

    protected void setAutoGrouping(Product[] sourceProducts, Product targetProduct) {
        final StringBuilder patternBuilder = new StringBuilder();
        for (final Product sourceProduct : sourceProducts) {
            if (sourceProduct.getAutoGrouping() != null) {
                if (patternBuilder.length() > 0) {
                    patternBuilder.append(":");
                }
                patternBuilder.append(sourceProduct.getAutoGrouping());
            }
        }
        targetProduct.setAutoGrouping(patternBuilder.toString());
    }

    protected void addDataNodes(Product masterProduct, Product targetProduct) throws IOException {
        for (final Product sourceProduct : openProductList) {
            final Map<String, String> mapping = new HashMap<>();
            for (final Band sourceBand : sourceProduct.getBands()) {
                if (!sourceBand.getName().contains("orphan")) {
                    RasterDataNode targetNode;
                    if (isNodeSpecial(sourceBand, targetProduct)) {
                        targetNode = addSpecialNode(masterProduct, sourceBand, targetProduct);
                    } else {
                        targetNode = addBand(sourceBand, targetProduct);
                    }
                    if (targetNode != null) {
                        configureTargetNode(sourceBand, targetNode);
                        mapping.put(sourceBand.getName(), targetNode.getName());

                        if (targetNode.getName().startsWith("counter_water")) {
                            targetNode.setImageInfo(new ImageInfo(getCounterWaterColorPalette()));
                        }

                        final ProductReader sourceReader = sourceProduct.getProductReader();
                        if (sourceReader instanceof S3NetcdfReader && !sourceBand.isSourceImageSet()) {
                            bandCacheMap.put(targetNode.getName(), (S3NetcdfReader) sourceReader);
                        }
                    }
                }
            }
            copyMasks(sourceProduct, targetProduct, mapping);
        }
    }

    protected boolean isNodeSpecial(Band sourceBand, Product targetProduct) {
        return sourceBand.getRasterWidth() != targetProduct.getSceneRasterWidth() ||
                sourceBand.getRasterHeight() != targetProduct.getSceneRasterHeight();
    }

    protected final void copyMasks(Product sourceProduct, Product targetProduct, Map<String, String> mapping) {
        final ProductNodeGroup<Mask> maskGroup = sourceProduct.getMaskGroup();
        for (int i = 0; i < maskGroup.getNodeCount(); i++) {
            final Mask mask = maskGroup.get(i);
            final Mask.ImageType imageType = mask.getImageType();
            if (imageType == Mask.BandMathsType.INSTANCE) {
                String name = mask.getName();
                if (!name.equals("spare")) {
                    String expression = Mask.BandMathsType.getExpression(mask);
                    for (final String sourceBandName : mapping.keySet()) {
                        if (expression.contains(sourceBandName)) {
                            final String targetBandName = mapping.get(sourceBandName);
                            if (!sourceBandName.equals(targetBandName)) {
                                name = name.replaceAll(sourceBandName, targetBandName);
                                expression = expression.replaceAll(sourceBandName, targetBandName);
                            }
                            final String description = sourceProduct.getDisplayName() + "." + mask.getDisplayName();
                            targetProduct.addMask(name, expression, description, mask.getImageColor(), mask.getImageTransparency());
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void setTimeCoding(Product targetProduct, VirtualDir virtualDir) throws IOException {

    }

    protected Product readProduct(String fileName, Manifest manifest, VirtualDir virtualDir) throws IOException {
        final File file = getFileFromVirtualDir(fileName, virtualDir);
        if (!file.exists()) {
            return null;
        }

        final ProductReader reader = ProductIO.getProductReaderForInput(file);
        if (reader == null) {
            final String msg = MessageFormat.format("Cannot read file ''{0}''. No appropriate reader found.", fileName);
            logger.log(Level.SEVERE, msg);
            throw new IOException(msg);
        }

        final Product product = reader.readProductNodes(file, null);
        if (product == null) {
            final String msg = MessageFormat.format("Cannot read file ''{0}''.", fileName);
            logger.log(Level.SEVERE, msg);
            throw new IOException(msg);
        }

        return product;
    }

    private File getInputFile() {
        return productReader.getInputFile();
    }

    protected final File getInputFileParentDirectory() {
        return productReader.getInputFileParentDirectory();
    }

    protected String getProductName() {
        return manifest.getProductName();
    }

    protected void addSeparatingDimensions(String[] suffixesForSeparatingDimensions) {
        for (String suffixForSeparatingDimension : suffixesForSeparatingDimensions) {
            if (!separatingDimensions.contains(suffixForSeparatingDimension)) {
                separatingDimensions.add(suffixForSeparatingDimension);
            }
        }
    }

    protected abstract List<String> getFileNames(Manifest manifest);

    protected void setTimeCoding(Product targetProduct, VirtualDir virtualDir, String timeDataFileName, String timeVariableName) throws IOException {
        final File file = getFileFromVirtualDir(timeDataFileName, virtualDir);
        if (!file.exists()) {
            throw new IOException("Time coordinates file not found: " + timeDataFileName);
        }

        try (NetcdfFile netcdfFile = NetcdfFileOpener.open(file)) {
            if (netcdfFile == null) {
                throw new IOException("Unable to open file: " + file.getAbsolutePath());
            }


            final Variable variable = netcdfFile.findVariable(timeVariableName);
            if (variable == null) {
                throw new IOException("Unable to read variable '" + timeVariableName + "': " + file.getAbsolutePath());
            }

            final Array timeStampArray = variable.read();
            final long[] timeStamps = (long[]) timeStampArray.copyTo1DJavaArray();
            final SentinelTimeCoding sentinelTimeCoding = new SentinelTimeCoding(timeStamps);
            targetProduct.setSceneTimeCoding(sentinelTimeCoding);
        }
    }


    protected void registerTpgWithCache(String tpgName, Band sourceBand,
                                        int dataOffsetX, int dataOffsetY,
                                        int gridWidth, int gridHeight) {
        final ProductReader reader = sourceBand.getProduct().getProductReader();
        if (reader instanceof S3NetcdfReader) {
            final String sourceName = sourceBand.getName();
            final String cacheKey = getTpgCacheKey(sourceName);

            tpgReaderMap.put(tpgName, new TpgEntry((S3NetcdfReader) reader, sourceBand, dataOffsetX, dataOffsetY,
                    gridWidth, gridHeight, cacheKey));
        } else {
            logger.warning("Cannot register TPG '" + tpgName + "' with cache: " +
                    "source band product reader is not an S3NetcdfReader.");
        }
    }


    protected double[] loadTiePointData(String tpgName) throws IOException {
        final TpgEntry entry = tpgReaderMap.get(tpgName);
        if (entry == null) {
            throw new IOException("No cache reader registered for tie-point grid '" + tpgName + "'.");
        }

        final int w = entry.gridWidth;
        final int h = entry.gridHeight;
        final int[] offsets = new int[]{entry.dataOffsetY, entry.dataOffsetX};
        final int[] shapes = new int[]{h, w};

        final ProductData data = ProductData.createInstance(entry.sourceBand.getDataType(), w * h);
        entry.reader.getProductCache().read(entry.cacheKey, offsets, shapes, new DataBuffer(data, offsets, shapes));

        final double[] result = new double[w * h];
        for (int ii = 0; ii < result.length; ii++) {
            double geoValue = S3Util.getGeophysicalValue(entry.sourceBand, data.getElemDoubleAt(ii));

            if (S3Util.isInvalidGeoCoordinate(tpgName, geoValue)) {
                geoValue = Double.NaN;
            }
            result[ii] = geoValue;
        }
        return result;
    }

    private void setTimes(Product targetProduct) {
        final Product sourceProduct = findMasterProduct();
        targetProduct.setStartTime(sourceProduct.getStartTime());
        targetProduct.setEndTime(sourceProduct.getEndTime());
        if (targetProduct.getStartTime() == null) {
            targetProduct.setStartTime(manifest.getStartTime());
        }
        if (targetProduct.getEndTime() == null) {
            targetProduct.setEndTime(manifest.getStopTime());
        }
    }

    private void readProducts(List<String> fileNames, VirtualDir virtualDir) throws IOException {
        for (final String fileName : fileNames) {
            if ("".equals(fileName)) {  // skip directory
                continue;
            }
            Product product = null;
            try {
                product = readProduct(fileName, manifest, virtualDir);
            } catch (IOException ioe) {
                logger.log(Level.WARNING, ioe.getMessage());
            }
            if (product != null) {
                openProductList.add(product);
            } else {
                logger.log(Level.WARNING, MessageFormat.format("Could not find ''{0}''.", fileName));
            }
        }
        if (openProductList.isEmpty()) {
            throw new IOException("Could not find or read any valid products.");
        }
    }

    private Manifest createManifest(InputStream inputStream) throws IOException {
        final Document xmlDocument;
        try (inputStream) {
            xmlDocument = createXmlDocument(inputStream);
        }
        // TODO (mp/16.09.2016) - probably not needed anymore
        // according to the documentation SYN L1C should also have a xfdumanifest file
        //if (file.getName().equals(EarthExplorerManifest.L1C_MANIFEST_FILE_NAME)) {
        //    return EarthExplorerManifest.createManifest(xmlDocument);
        //}
        return XfduManifest.createManifest(xmlDocument);
    }

    private Document createXmlDocument(InputStream inputStream) throws IOException {
        final String msg = "Cannot create document from manifest XML file.";

        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
        } catch (SAXException | ParserConfigurationException e) {
            getLogger().log(Level.SEVERE, msg, e);
            throw new IOException(msg, e);
        }
    }

    // @todo 3 tb/tb make static and mock test 2024-05-31
    private InputStream getManifestInputStream(VirtualDir virtualDir) throws IOException {
        final String[] list = virtualDir.listAllFiles();
        for (final String entry : list) {
            if (entry.toLowerCase().endsWith(XfduManifest.MANIFEST_FILE_NAME)) {
                return virtualDir.getInputStream(entry);
            }
        }

        return null;
    }

    private static String getTpgCacheKey(String bandName) {
        if ("TP_latitude".equals(bandName)) {
            return "latitude";
        }
        if ("TP_longitude".equals(bandName)) {
            return "longitude";
        }

        return bandName;
    }


    static class TpgEntry {
        final S3NetcdfReader reader;
        final Band sourceBand;
        final int dataOffsetX;
        final int dataOffsetY;
        final int gridWidth;
        final int gridHeight;
        final String cacheKey;


        TpgEntry(S3NetcdfReader reader, Band sourceBand, int dataOffsetX, int dataOffsetY,
                 int gridWidth, int gridHeight, String cacheKey) {
            this.reader = reader;
            this.sourceBand = sourceBand;
            this.dataOffsetX = dataOffsetX;
            this.dataOffsetY = dataOffsetY;
            this.gridWidth = gridWidth;
            this.gridHeight = gridHeight;
            this.cacheKey = cacheKey;
        }
    }
}
