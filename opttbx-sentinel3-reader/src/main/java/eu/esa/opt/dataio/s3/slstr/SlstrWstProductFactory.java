package eu.esa.opt.dataio.s3.slstr;

/* Copyright (C) 2012 Brockmann Consult GmbH (info@brockmann-consult.de)
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
import eu.esa.opt.dataio.s3.Sentinel3ProductReader;
import eu.esa.opt.dataio.s3.util.S3CachedGeoDataUtil;
import eu.esa.opt.dataio.s3.util.S3NetcdfReader;
import eu.esa.opt.dataio.s3.util.S3Util;
import org.apache.commons.lang3.StringUtils;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.geocoding.*;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.RasterDataNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SlstrWstProductFactory extends SlstrSstProductFactory {

    private static final short[] RESOLUTIONS = new short[]{1000, 1000};
    private static final double RESOLUTION_IN_KM = 1.0;
    private static final String SYSPROP_SLSTR_WST_PIXEL_INVERSE = "opttbx.reader.slstr.wst.pixelGeoCoding.inverse";
    private static final String NADIR = "NADIR";
    private static final String DUAL = "DUAL";

    public SlstrWstProductFactory(Sentinel3ProductReader productReader) {
        super(productReader);
    }

    @Override
    protected void setGeoCoding(Product targetProduct) throws IOException {
        final String lonVariableName = "lon";
        final String latVariableName = "lat";
        final Band lonBand = targetProduct.getBand(lonVariableName);
        final Band latBand = targetProduct.getBand(latVariableName);
        if (lonBand == null || latBand == null) {
            // no way to create a geocoding tb 2020-01-24
            return;
        }

        final S3NetcdfReader readerLon = getBandCacheMap().get(lonBand.getName());
        final S3NetcdfReader readerLat = getBandCacheMap().get(latBand.getName());

        final double[] longitudes = S3CachedGeoDataUtil.readCachedGeophysicalBandAsDouble(readerLon, lonBand);
        final double[] latitudes = S3CachedGeoDataUtil.readCachedGeophysicalBandAsDouble(readerLat, latBand);

        final GeoRaster geoRaster = new GeoRaster(longitudes, latitudes, lonVariableName, latVariableName,
                targetProduct.getSceneRasterWidth(), targetProduct.getSceneRasterHeight(), RESOLUTION_IN_KM);

        final String[] keys = S3Util.getForwardAndInverseKeys_pixelCoding(SYSPROP_SLSTR_WST_PIXEL_INVERSE);
        final ForwardCoding forward = ComponentFactory.getForward(keys[0]);
        final InverseCoding inverse = ComponentFactory.getInverse(keys[1]);

        final ComponentGeoCoding geoCoding = new ComponentGeoCoding(geoRaster, forward, inverse, GeoChecks.POLES);
        geoCoding.initialize();

        targetProduct.setSceneGeoCoding(geoCoding);
    }

    @Override
    protected void addDataNodes(Product masterProduct, Product targetProduct) throws IOException {
        final String masterProductName = masterProduct.getName();
        String prefix = getBaseline004Prefix(masterProductName);
        if (StringUtils.isEmpty(prefix)) {
            super.addDataNodes(masterProduct, targetProduct);
            return;
        }

        for (final Product sourceProduct : openProductList) {
            prefix = getBaseline004Prefix(sourceProduct.getName());
            final Map<String, String> mapping = new HashMap<>();
            for (final Band sourceBand : sourceProduct.getBands()) {
                final RasterDataNode targetNode = addBand(sourceBand, targetProduct, prefix);

                if (targetNode != null) {
                    mapping.put(sourceBand.getName(), targetNode.getName());
                }
                final ProductReader sourceReader = sourceProduct.getProductReader();
                if (sourceReader instanceof S3NetcdfReader && !sourceBand.isSourceImageSet()) {
                    bandCacheMap.put(targetNode.getName(), (S3NetcdfReader) sourceReader);
                }
            }
        }
    }

    @Override
    protected void setAutoGrouping(Product[] sourceProducts, Product targetProduct) {
        setAutoGrouping(targetProduct);
    }

    @Override
    public String getBandCacheKey(Band destBand) {
        final String destBandName = destBand.getName();
        if (destBandName.contains(NADIR)) {
            final int index = destBandName.indexOf(NADIR);
            return destBandName.substring(0, index - 1);
        } else if (destBandName.contains(DUAL)) {
            final int index = destBandName.indexOf(DUAL);
            return destBandName.substring(0, index - 1);
        } else {
            return destBandName;
        }
    }

    static void setAutoGrouping(Product targetProduct) {
        targetProduct.setAutoGrouping("NADIR:DUAL:brightness_temperature:nedt");
    }

    @Override
    protected void setUncertaintyBands(Product product) {
        super.setUncertaintyBands(product);
        // sst_theoretical_error is the old name
        String[] possibleUncBandNames = new String[]{"sst_theoretical_uncertainty", "sst_theoretical_error"};
        if (product.containsBand("sea_surface_temperature")) {
            final Band seaSurfaceTemperatureBand = product.getBand("sea_surface_temperature");
            for (String bandName : possibleUncBandNames) {
                if (product.containsBand(bandName)) {
                    final Band band = product.getBand(bandName);
                    seaSurfaceTemperatureBand.addAncillaryVariable(band, "uncertainty");
                    break;
                }
            }
        }
    }

    @Override
    protected boolean isNodeSpecial(Band sourceBand, Product targetProduct) {
        // None of the nodes needs special treatment
        return false;
    }

    @Override
    protected Double getStartOffset(String gridIndex) {
        return 0.0;
    }

    @Override
    protected Double getTrackOffset(String gridIndex) {
        return 0.0;
    }

    @Override
    protected short[] getResolutions(String gridIndex) {
        return RESOLUTIONS;
    }

    @Override
    protected short[] getReferenceResolutions() {
        return RESOLUTIONS;
    }

    @Override
    protected void configureTargetNode(Band sourceBand, RasterDataNode targetNode) {
    }

    @Override
    protected void setBandGeoCodings(Product product) {
        // this is intended - we do not have band geo-codings for this product type tb 2020-04-20
    }

    @Override
    protected void setTimeCoding(Product targetProduct, VirtualDir virtualDir) throws IOException {
        // empty by design - prevents the SlstrSstProductFactory implementation from being called tb 2021-01-19
    }

    // package access for testing only tb 2026-06-22
    static String getBaseline004Prefix(String productName) {
        if (productName.contains(NADIR)) {
            return NADIR;
        } else if (productName.contains(DUAL)) {
            return DUAL;
        }
        return null;
    }
}
