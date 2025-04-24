package eu.esa.opt.olci.l1csyn;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.Tile;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.core.util.math.DistanceMeasure;
import org.esa.snap.core.util.math.EuclideanDistance;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;


@OperatorMetadata(alias = "Misregister",
        internal = true,
        category = "Raster/Geometric",
        version = "1.0",
        authors = "Roman Shevchuk, Marco Peters",
        copyright = "(c) 2019 by Brockmann Consult",
        description = "Coregister OLCI and SLSTR L1 Products using TreeMaps from MISR product. At minimum one Treemap for oblique and one for nadir view must be provided."
)
public class MisrOp extends Operator {

    @SourceProduct(alias = "olciSource", description = "OLCI source product")
    private Product olciSourceProduct;

    @SourceProduct(alias = "slstrSource", description = "SLSTR source product")
    private Product slstrSourceProduct;

    @Parameter(description = "If set to true empty pixels after MISR will be filled with neighbouring values")
    private boolean fillEmptyPixels;

    @Parameter(description = "If set to true orphan pixels will be used",
            defaultValue = "false")
    private boolean orphan;

    @Parameter(description = "Pixel map for S1 nadir view")
    private Map<int[], int[]> S1PixelMap;

    @Parameter(description = "Pixel map for S2 nadir view")
    private Map<int[], int[]> S2PixelMap;

    @Parameter(description = "Pixel map for S3 nadir view")
    private Map<int[], int[]> S3PixelMap;

    @Parameter(description = "Pixel map for S4 nadir view")
    private Map<int[], int[]> S4PixelMap;

    @Parameter(description = "Pixel map for S5 nadir view")
    private Map<int[], int[]> S5PixelMap;

    @Parameter(description = "Pixel map for S6 nadir view")
    private Map<int[], int[]> S6PixelMap;

    @Parameter(description = "Pixel map for ao oblique view")
    private Map<int[], int[]> aoPixelMap;

    @Parameter(description = "Pixel map for bo oblique view")
    private Map<int[], int[]> boPixelMap;

    @Parameter(description = "Pixel map for co oblique view")
    private Map<int[], int[]> coPixelMap;

    @Parameter(description = "Orphan pixel map for S1 nadir view")
    private Map<int[], int[]> S1OrphanMap;

    @Parameter(description = "Orphan pixel map for S2 nadir view")
    private Map<int[], int[]> S2OrphanMap;

    @Parameter(description = "Orphan pixel map for S3 nadir view")
    private Map<int[], int[]> S3OrphanMap;

    @Parameter(description = "Orphan pixel map for S4 nadir view")
    private Map<int[], int[]> S4OrphanMap;

    @Parameter(description = "Orphan pixel map for S5 nadir view")
    private Map<int[], int[]> S5OrphanMap;

    @Parameter(alias = "S6OrphanMap", description = "Orphan pixel map for S6 nadir view")
    private Map<int[], int[]> S6OrphanMap;

    @Parameter(description = "Orphan pixel map for ao oblique view")
    private Map<int[], int[]> aoOrphanMap;

    @Parameter(description = "Orphan pixel map for bo oblique view")
    private Map<int[], int[]> boOrphanMap;

    @Parameter(description = "Orphan pixel map for co oblique view")
    private Map<int[], int[]> coOrphanMap;
    @TargetProduct
    private Product targetProduct;

    @Override
    public void initialize() throws OperatorException {
        targetProduct = createTargetProduct(olciSourceProduct, slstrSourceProduct, fillEmptyPixels);
    }

    @Override
    public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) {
        Map<int[], int[]> map = new HashMap<>();
        Map<int[], int[]> mapOrphan = new HashMap<>();

        if (targetBand.getName().contains("_ao")) {
            map = aoPixelMap;
            mapOrphan = aoOrphanMap;
        } else if (targetBand.getName().contains("_bo")) {
            map = boPixelMap;
            mapOrphan = boOrphanMap;
        } else if (targetBand.getName().contains("_co")) {
            map = coPixelMap;
            mapOrphan = coOrphanMap;
        } else if ((targetBand.getName().contains("S1") && targetBand.getName().contains("_an")) || (targetBand.getName().contains("S1") && targetBand.getName().contains("_bn")) || (targetBand.getName().contains("S1") && targetBand.getName().contains("_cn"))) {
            map = S1PixelMap;
            mapOrphan = S1OrphanMap;
        } else if ((targetBand.getName().contains("S2") && targetBand.getName().contains("_an")) || (targetBand.getName().contains("S2") && targetBand.getName().contains("_bn")) || (targetBand.getName().contains("S2") && targetBand.getName().contains("_cn"))) {
            map = S2PixelMap;
            mapOrphan = S2OrphanMap;
        } else if ((targetBand.getName().contains("S3") && targetBand.getName().contains("_an")) || (targetBand.getName().contains("S3") && targetBand.getName().contains("_bn")) || (targetBand.getName().contains("S3") && targetBand.getName().contains("_cn"))) {
            map = S3PixelMap;
            mapOrphan = S3OrphanMap;
        } else if ((targetBand.getName().contains("S4") && targetBand.getName().contains("_an")) || (targetBand.getName().contains("S4") && targetBand.getName().contains("_bn")) || (targetBand.getName().contains("S4") && targetBand.getName().contains("_cn"))) {
            map = S4PixelMap;
            mapOrphan = S4OrphanMap;
        } else if ((targetBand.getName().contains("S5") && targetBand.getName().contains("_an")) || (targetBand.getName().contains("S5") && targetBand.getName().contains("_bn")) || (targetBand.getName().contains("S5") && targetBand.getName().contains("_cn"))) {
            map = S5PixelMap;
            mapOrphan = S5OrphanMap;
        } else if ((targetBand.getName().contains("S6") && targetBand.getName().contains("_an")) || (targetBand.getName().contains("S6") && targetBand.getName().contains("_bn")) || (targetBand.getName().contains("S6") && targetBand.getName().contains("_cn"))) {
            map = S6PixelMap;
            mapOrphan = S6OrphanMap;
        } else if ((targetBand.getName().contains("_an") || targetBand.getName().contains("_bn") || targetBand.getName().contains("_cn"))) {
            map = S3PixelMap;
            mapOrphan = S3OrphanMap;
        }

        final double targetNoDataValue = targetBand.getNoDataValue();
        if (slstrSourceProduct.containsBand(targetBand.getName())) {
            Band sourceBand = slstrSourceProduct.getBand(targetBand.getName());
            int sourceRasterWidth = sourceBand.getRasterWidth();
            int sourceRasterHeight = sourceBand.getRasterHeight();
            for (Tile.Pos pos : targetTile) {
                targetTile.setSample(pos.x, pos.y, targetNoDataValue);
                int[] position = {pos.x, pos.y};
                int[] slstrGridPosition = map.get(position);
                if (slstrGridPosition != null) {
                    final int slstrGridPosX = slstrGridPosition[0];
                    final int slstrGridPosY = slstrGridPosition[1];
                    if (slstrGridPosX < sourceRasterWidth && slstrGridPosY < sourceRasterHeight) {
                        double reflecValue = sourceBand.getSampleFloat(slstrGridPosX, slstrGridPosY);
                        if (reflecValue < 0) {
                            reflecValue = targetNoDataValue;
                        }
                        targetTile.setSample(pos.x, pos.y, reflecValue);
                    }
                }
            }
            //Orphan pixels
            if (orphan) {
                String parentPath = slstrSourceProduct.getFileLocation().getParent();
                String netcdfDataPath = parentPath + "/" + targetBand.getName() + ".nc";
                if (Files.exists(Paths.get(netcdfDataPath))) {
                    try (NetcdfFile netcdf = NetcdfFiles.open(netcdfDataPath)) {
                        Variable orphanVariable = netcdf.findVariable(targetBand.getName().replace("radiance_", "radiance_orphan_"));
                        if (orphanVariable == null) {
                            throw new OperatorException(String.format("No information about orphans found in file '%s'", netcdfDataPath));
                        }
                        final Attribute scale_factorAttribute = orphanVariable.findAttribute("scale_factor");
                        double scaleFactor = 1.0;
                        if (scale_factorAttribute != null) {
                            final Number scale_factor = scale_factorAttribute.getNumericValue();
                            if (scale_factor != null) {
                                scaleFactor = (double) scale_factor;
                            }
                        }


                        final Array orphanData = orphanVariable.read();
                        final int[] dataShape = orphanData.getShape(); // shape is [2400, 374] for S3_radiance_orphan_an
                        final Index rawIndex = orphanData.getIndex();
                        for (Tile.Pos pos : targetTile) {
                            int[] position = {pos.x, pos.y};
                            int[] slstrOrphanPosition = mapOrphan.get(position);
                            if (slstrOrphanPosition != null) {
                                final int orphanPosX = slstrOrphanPosition[0];
                                final int orphanPosY = slstrOrphanPosition[1];
                                if (orphanPosX < dataShape[0] && orphanPosY < dataShape[1]) {
                                    rawIndex.set(orphanPosY, orphanPosX); // Dimension is Y, X  --> so 'wrong' order of Y and X here
                                    double dataValue = orphanData.getDouble(rawIndex);
                                    if (dataValue > 0) {
                                        targetTile.setSample(pos.x, pos.y, dataValue * scaleFactor);
                                    }
                                }
                            }
                        }
                    } catch (IOException ioe) {
                        SystemUtils.LOG.log(Level.WARNING, String.format("Could not process file %s: %s", netcdfDataPath, ioe.getMessage()));
                    }
                } else {
                    SystemUtils.LOG.log(Level.FINE, String.format("File %s does not exist", netcdfDataPath));
                }
            }

            if (fillEmptyPixels) {
                for (Tile.Pos pos : targetTile) {
                    if (targetTile.getSampleDouble(pos.x, pos.y) == targetNoDataValue) {
                        double neighborPixel = getNeighborPixel(pos.x, pos.y, targetBand, map, sourceBand);
                        targetTile.setSample(pos.x, pos.y, neighborPixel);
                    }
                }
            }

        } else if (targetBand.getName().equals("misr_flags")) {
            map = S3PixelMap;
            for (Tile.Pos pos : targetTile) {
                int[] position = {pos.x, pos.y};
                int[] slstrGridPosition = map.get(position);
                if (slstrGridPosition != null) {
                    targetTile.setSample(pos.x, pos.y, 1);
                } else {
                    targetTile.setSample(pos.x, pos.y, 0);
                }
            }
        } else if (targetBand.getName().equals("filled_flags")) {
            map = S3PixelMap;
            RasterDataNode oa17_radiance = olciSourceProduct.getRasterDataNode("Oa17_radiance");

            for (Tile.Pos pos : targetTile) {
                int[] position = {pos.x, pos.y};
                int[] slstrGridPosition = map.get(position);
                if (slstrGridPosition == null && oa17_radiance.isPixelValid(pos.x, pos.y)) {
                    targetTile.setSample(pos.x, pos.y, 1);
                }
            }
        }
    }

    private double getNeighborPixel(int x, int y, Band targetBand, Map<int[], int[]> map, Band sourceBand) {
        double neighborPixel = targetBand.getNoDataValue();
        int[] position = {x, y};
        int[] slstrGridPosition = map.get(position);
        GeoPos pixelGeoPos = targetBand.getGeoCoding().getGeoPos(new PixelPos(x, y), null);
        if (slstrGridPosition != null) {
            return sourceBand.getSampleFloat(slstrGridPosition[0], slstrGridPosition[1]);
        } else {
            EuclideanDistance euclideanDistance = new EuclideanDistance(pixelGeoPos.getLon(), pixelGeoPos.getLat());
            for (int size = 3; size < 10; size += 2) {
                neighborPixel = searchClosetPixel(size, sourceBand, euclideanDistance, x, y, targetBand, map);
                if (neighborPixel != targetBand.getNoDataValue()) {
                    return neighborPixel;
                }
            }
        }
        return neighborPixel;
    }

    private double searchClosetPixel(int size, Band sourceBand, DistanceMeasure distanceMeasure, int x, int y, Band targetBand, Map<int[], int[]> map) {
        double distance = Double.MAX_VALUE;
        double neighborPixel = targetBand.getNoDataValue();
        int step = size / 2;
        final GeoCoding sourceGeoCoding = sourceBand.getGeoCoding();
        for (int i = 0; i < size; i += 1) {
            for (int j = 0; j < size; j += 1) {
                int[] neighborPos = {x - step + i, y - step + j};
                int[] slstrNeighbor = map.get(neighborPos);
                if (slstrNeighbor != null) {
                    GeoPos neighborGeoPos = sourceGeoCoding.getGeoPos(new PixelPos(slstrNeighbor[0], slstrNeighbor[1]), null);
                    double neighborDist = distanceMeasure.distance(neighborGeoPos.getLon(), neighborGeoPos.getLat());
                    if (neighborDist < distance) {
                        neighborPixel = sourceBand.getSampleFloat(slstrNeighbor[0], slstrNeighbor[1]);
                        distance = neighborDist;
                    }
                }
            }
        }
        return neighborPixel;
    }


    static Product createTargetProduct(Product olciSourceProduct, Product slstrSourceProduct, boolean fillEmptyPixels) {
        Product targetProduct = new Product(olciSourceProduct.getName(), olciSourceProduct.getProductType(),
                olciSourceProduct.getSceneRasterWidth(),
                olciSourceProduct.getSceneRasterHeight());


        for (Band olciBand : olciSourceProduct.getBands()) {
            ProductUtils.copyBand(olciBand.getName(), olciSourceProduct, targetProduct, true);
        }

        for (Band slstrBand : slstrSourceProduct.getBands()) {
            final String slstrBandName = slstrBand.getName();
            if (slstrBandName.contains("_an") || slstrBandName.contains("_bn") || slstrBandName.contains("_cn")
                    || slstrBandName.contains("_ao") || slstrBandName.contains("_bo") || slstrBandName.contains("_co")) {
                Band copiedBand = targetProduct.addBand(slstrBandName, ProductData.TYPE_FLOAT32);
                copiedBand.setDescription(slstrBand.getDescription());
                copiedBand.setUnit(slstrBand.getUnit());
                copiedBand.setNoDataValue(slstrSourceProduct.getBand(slstrBandName).getNoDataValue());
                copiedBand.setNoDataValueUsed(true);
                copiedBand.setSpectralBandIndex(slstrBand.getSpectralBandIndex());
                copiedBand.setSpectralWavelength(slstrBand.getSpectralWavelength());
                copiedBand.setSpectralBandwidth(slstrBand.getSpectralBandwidth());
            } else {
                if (!slstrBandName.contains("_in") && !slstrBandName.contains("_io") &&
                        !slstrBandName.contains("_fn") && !slstrBandName.contains("_fo")) {
                    ProductUtils.copyBand(slstrBandName, slstrSourceProduct, targetProduct, true);
                }
            }
        }

        ProductUtils.copyMetadata(olciSourceProduct, targetProduct);
        ProductUtils.copyTiePointGrids(olciSourceProduct, targetProduct);
        ProductUtils.copyMasks(olciSourceProduct, targetProduct);
        ProductUtils.copyFlagBands(olciSourceProduct, targetProduct, true);
        ProductUtils.copyGeoCoding(olciSourceProduct, targetProduct);

        final FlagCoding misrFlagCoding = new FlagCoding("MISR_Applied");
        misrFlagCoding.setDescription("MISR processor flag");
        targetProduct.getFlagCodingGroup().add(misrFlagCoding);
        Band misrFlags = new Band("misr_flags", ProductData.TYPE_UINT32,
                olciSourceProduct.getSceneRasterWidth(),
                olciSourceProduct.getSceneRasterHeight());
        misrFlagCoding.addFlag("MISR not applied", 0, "MISR not Applied");
        misrFlagCoding.addFlag("MISR applied", 1, "MISR applied");
        misrFlags.setSampleCoding(misrFlagCoding);

        targetProduct.addBand(misrFlags);
        targetProduct.addMask("MISR pixel applied", "misr_flags != 0",
                "MISR information was used to get value of this pixel", Color.RED, 0.5);

        if (fillEmptyPixels) {
            final FlagCoding filledFlagCoding = new FlagCoding("Filled pixel after MISR");
            filledFlagCoding.setDescription("Filled pixels");
            targetProduct.getFlagCodingGroup().add(filledFlagCoding);
            Band filledFlags = new Band("filled_flags", ProductData.TYPE_UINT32,
                    olciSourceProduct.getSceneRasterWidth(),
                    olciSourceProduct.getSceneRasterHeight());
            filledFlagCoding.addFlag("pixel was not filled", 0, "pixel was not filled");
            filledFlagCoding.addFlag("pixel was filled", 1, "pixel was filled");
            filledFlags.setSampleCoding(filledFlagCoding);
            targetProduct.addBand(filledFlags);

            targetProduct.addMask("Filled pixel after MISR", "filled_flags != 0",
                    "After applying misregistration, this pixel was filled with the value of its neighbour", Color.BLUE, 0.5);
        }
        return targetProduct;
    }

    public static class Spi extends OperatorSpi {
        public Spi() {
            super(MisrOp.class);
        }
    }

}

