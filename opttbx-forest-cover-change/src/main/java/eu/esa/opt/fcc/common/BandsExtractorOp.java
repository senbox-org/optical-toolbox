package eu.esa.opt.fcc.common;

import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.dataop.barithm.BandArithmetic;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.jexp.ParseException;
import org.esa.snap.core.jexp.Term;
import org.esa.snap.core.util.ProductUtils;

import java.util.*;
import java.util.logging.Level;

/**
 * @author Razvan Dumitrascu
 * @since 5.0.6
 */
@OperatorMetadata(
        alias = "BandsExtractorOp",
        version="1.0",
        category = "Optical/Thematic Land Processing",
        description = "Creates a new product out of the source product containing only the indexes bands given",
        authors = "Razvan Dumitrascu",
        copyright = "Copyright (C) 2017 by CS ROMANIA")
public class BandsExtractorOp extends Operator {
    @SourceProduct(alias = "Source", description = "The source product to be modified.")
    private Product sourceProduct;

    @TargetProduct
    private Product targetProduct;

    @Parameter(label = "Source bands", description = "The source bands for the computation.", rasterDataNodeType = Band.class)
    private String[] sourceBandNames;

    @Parameter(label = "Source masks", description = "The source masks for the computation.", rasterDataNodeType = Mask.class)
    private String[] sourceMaskNames;

    @Parameter(label="Include references", description="Flag to include the referenced rasters.", defaultValue = "true")
    private boolean includeReferences;

    @Override
    public void initialize() throws OperatorException {
        if (this.sourceBandNames == null || this.sourceBandNames.length == 0) {
            throw new OperatorException("Please select at least one band.");
        }

        List<String> lstSourceBandNames =this.sourceBandNames == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(this.sourceBandNames));
        List<String> lstSourceMaskNames = this.sourceMaskNames == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(this.sourceMaskNames));
       if (includeReferences ) {
           List<String> lstAllNodes = new ArrayList<String>();
           lstAllNodes.addAll(lstSourceBandNames);
           lstAllNodes.addAll(lstSourceMaskNames);
           if (!lstAllNodes.isEmpty()) {
               final ArrayList<String> referencedBandNames = new ArrayList<>();
               final ArrayList<String> referencedMaskNames = new ArrayList<>();
               for (String nodeName : lstAllNodes) {
                   collectNotIncludedReferences(nodeName, referencedBandNames, referencedMaskNames);
               }

               referencedBandNames.forEach(bandName ->
               {
                   if (!lstSourceBandNames.contains(bandName))
                       lstSourceBandNames.add(bandName);
               });
               referencedMaskNames.forEach(maskName ->
               {
                   if (!lstSourceMaskNames.contains(maskName))
                       lstSourceMaskNames.add(maskName);
               });
           }
       }
        this.targetProduct = extractBands(this.sourceProduct, lstSourceBandNames.toArray(new String[0]), lstSourceMaskNames.toArray(new String[0]));
    }

    private void collectNotIncludedReferences(String nodeName, ArrayList<String> referencedBandNames, ArrayList<String> referencedMaskNames) {
        RasterDataNode rasterDataNode = sourceProduct.getRasterDataNode(nodeName);
        if (rasterDataNode == null) {
            throw new OperatorException(String.format("Source product does not contain a raster named '%s'.", nodeName));
        }
        final String validPixelExpression = rasterDataNode.getValidPixelExpression();
        collectReferencedRastersInExpression(validPixelExpression, referencedBandNames, referencedMaskNames);

        if (rasterDataNode instanceof VirtualBand || rasterDataNode instanceof Mask) {
            String strExpression = getRasterDataNodeExpression (rasterDataNode);
            collectReferencedRastersInExpression(strExpression, referencedBandNames, referencedMaskNames);
        }
    }

    private String getRasterDataNodeExpression(RasterDataNode rasterDataNode){
        if (rasterDataNode == null )
            return null;
        String strExpression = null;
        if (rasterDataNode instanceof VirtualBand) {
            strExpression = ((VirtualBand) rasterDataNode).getExpression();
        }else if  (rasterDataNode instanceof Mask) {
            Mask mask = (Mask) rasterDataNode;
            if (mask.getImageType() == Mask.BandMathsType.INSTANCE) {
                strExpression = Mask.BandMathsType.getExpression(mask);
            } else if (mask.getImageType() == Mask.RangeType.INSTANCE) {
                strExpression = Mask.RangeType.getRasterName(mask);
            }
        }
        return strExpression;
    }

    private void collectReferencedRastersInExpression(String expression, ArrayList<String> referencedBandNames, ArrayList<String> referencedMaskNames) {
        if (expression == null || expression.trim().isEmpty()) {
            return;
        }
        try {
            final Term term = sourceProduct.parseExpression(expression);
            final RasterDataNode[] refRasters = BandArithmetic.getRefRasters(term);
            for (RasterDataNode refRaster : refRasters) {
                final String refNodeName = refRaster.getName();
                Band bandNode =  sourceProduct.getBand(refNodeName) ;
                if ( bandNode!= null){
                    if (!referencedBandNames.contains(refNodeName)) {
                        referencedBandNames.add(refNodeName);
                    }
                    final String bandExpression = getRasterDataNodeExpression (bandNode);
                    collectReferencedRastersInExpression(bandExpression, referencedBandNames, referencedMaskNames);
                }

                Mask maskNode =  sourceProduct.getMaskGroup().get(refNodeName) ;
                if (maskNode != null){
                    if (!referencedMaskNames.contains(refNodeName)) {
                        referencedMaskNames.add(refNodeName);
                    }
                    final String maskExpression = getRasterDataNodeExpression (maskNode);
                    collectReferencedRastersInExpression(maskExpression, referencedBandNames, referencedMaskNames);
                }
            }
        } catch (ParseException e) {
            getLogger().log(Level.WARNING, e.getMessage(), e);
        }
    }

    public static Product extractBands(Product sourceProduct, String[] sourceBandNames, String[] sourceMaskNames) {
        Product product = new Product(sourceProduct.getName(), sourceProduct.getProductType(), sourceProduct.getSceneRasterWidth(), sourceProduct.getSceneRasterHeight());
        product.setStartTime(sourceProduct.getStartTime());
        product.setEndTime(sourceProduct.getEndTime());
        product.setNumResolutionsMax(sourceProduct.getNumResolutionsMax());

        ProductUtils.copyMetadata(sourceProduct, product);
        ProductUtils.copyGeoCoding(sourceProduct, product);
        ProductUtils.copyTiePointGrids(sourceProduct, product);
        ProductUtils.copyVectorData(sourceProduct, product);

        for (int i=0; i<sourceBandNames.length; i++) {
            Band sourceBand = sourceProduct.getBand(sourceBandNames[i]);
            String sourceBandName = sourceBand.getName();
            String targetBandName = sourceBandName;
            if (sourceBand instanceof VirtualBand ){
                ProductUtils.copyVirtualBandWithStatistics(product,(VirtualBand)sourceBand, targetBandName,false);
            } else {
                ProductUtils.copyBand(sourceBandName, sourceProduct, targetBandName, product, true);
            }

            Band targetBand = product.getBand(targetBandName);
            ProductUtils.copyGeoCoding(sourceBand, targetBand);
        }

        if (sourceMaskNames != null && sourceMaskNames.length > 0) {
            // first the bands have to be copied and then the masks, otherwise the referenced bands, e.g. flag band,
            // is not contained in the target product and the mask is not copied
            ProductUtils.copyFlagBandsWithoutMasks(sourceProduct, product, true);
            ProductUtils.copyMasks(sourceProduct, product, sourceMaskNames, false);
        }

        return product;
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(BandsExtractorOp.class);
        }
    }
}
