/*
 * Copyright (C) 2014 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package eu.esa.opt.dataio.avhrr.noaa.pod;

import com.bc.ceres.binio.CompoundData;
import com.bc.ceres.binio.DataContext;
import com.bc.ceres.binio.DataFormat;
import com.bc.ceres.binio.IOHandler;
import com.bc.ceres.binio.SequenceData;
import eu.esa.opt.dataio.avhrr.AvhrrConstants;
import eu.esa.opt.dataio.avhrr.BandReader;
import eu.esa.opt.dataio.avhrr.HeaderUtil;
import eu.esa.opt.dataio.avhrr.noaa.HeaderWrapper;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.TiePointGrid;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a NOAA POD AVHRR HRPT file.
 *
 * @author Ralf Quast
 */
final class PodAvhrrFile implements VideoDataProvider, CalibrationCoefficientsProvider {

    static final int PRODUCT_WIDTH = 2048;
    static final int TIE_POINT_GRID_WIDTH = 51;
    static final int TIE_POINT_SAMPLING_X = 40;
    static final int TIE_POINT_OFFSET_X = 25;

    // http://www.ncdc.noaa.gov/oa/pod-guide/ncdc/docs/podug/html/c3/sec3-1.htm#sec3-121 (Table 3.1.2.1-2)
    private static final int QUALITY_INDICATOR_BIT_MASK = 0b11001100000000000000000000000000;
    private static final double SLOPE_SCALE_FACTOR = PodTypes.getSlopeMetadata().getScalingFactor();
    private static final double INTERCEPT_SCALE_FACTOR = PodTypes.getInterceptMetadata().getScalingFactor();

    private final DataContext context;
    private final CompoundData data;
    private final int tbmHeaderRecordIndex;
    private final int datasetHeaderRecordIndex;
    private final int dataRecordsIndex;
    private final int videoDataIndex;
    private final int qualityDataIndex;
    private final int calibrationCofficientsIndex;
    private final Map<Band, BandReader> bandReaderMap;
    private final String productName;

    static boolean canDecode(IOHandler ioHandler) {
        return new PodFormatDetector().canDecode(ioHandler);
    }

    PodAvhrrFile(IOHandler ioHandler, String productName) {
        this.productName = productName;
        final DataFormat dataFormat = new DataFormat(PodTypes.HRPT_TYPE, ByteOrder.BIG_ENDIAN);
        context = dataFormat.createContext(ioHandler);
        data = context.getData();
        tbmHeaderRecordIndex = PodTypes.HRPT_TYPE.getMemberIndex("TBM_HEADER_RECORD");
        datasetHeaderRecordIndex = PodTypes.HRPT_TYPE.getMemberIndex("DATASET_HEADER_RECORD");
        dataRecordsIndex = PodTypes.HRPT_TYPE.getMemberIndex("DATA_RECORDS");
        videoDataIndex = PodTypes.DATA_RECORD_TYPE.getMemberIndex("VIDEO_DATA");
        qualityDataIndex = PodTypes.DATA_RECORD_TYPE.getMemberIndex("QUALITY_INDICATORS");
        calibrationCofficientsIndex = PodTypes.DATA_RECORD_TYPE.getMemberIndex("CALIBRATION_COEFFICIENTS");
        bandReaderMap = new HashMap<>(15);
    }

    @Override
    public SequenceData getVideoData(int recordIndex) throws IOException {
        return getDataRecord(recordIndex).getSequence(videoDataIndex);
    }

    @Override
    public boolean isValid(int recordIndex) throws IOException {
        return (getDataRecord(recordIndex).getInt(qualityDataIndex) & QUALITY_INDICATOR_BIT_MASK) == 0;
    }

    @Override
    public SequenceData getCalibrationCoefficients(int recordIndex) throws IOException {
        return getDataRecord(recordIndex).getSequence(calibrationCofficientsIndex);
    }

    @Override
    public double getSlopeScaleFactor() {
        return SLOPE_SCALE_FACTOR;
    }

    @Override
    public double getInterceptScaleFactor() {
        return INTERCEPT_SCALE_FACTOR;
    }

    void dispose() throws IOException {
        context.dispose();
    }

    BandReader getBandReader(Band band) {
        return bandReaderMap.get(band);
    }

    Product createProduct() throws IOException {
        final String productType = "NOAA_POD_AVHRR_HRPT";
        final int dataRecordCount = data.getUShort("NUMBER_OF_SCANS");
        final Product product = new Product(productName, productType, PRODUCT_WIDTH, dataRecordCount);
        product.setPreferredTileSize(new Dimension(PRODUCT_WIDTH / 2, PRODUCT_WIDTH / 2));

        addCountsBand(product, 0);
        addCountsBand(product, 1);
        addCountsBand(product, 2);
        addCountsBand(product, 3);
        addCountsBand(product, 4);

        addAlbedoBand(product, 0);
        addAlbedoBand(product, 1);
        addRadianceBand(product, 2);
        addRadianceBand(product, 3);
        addRadianceBand(product, 4);

        addTiePointGridsAndGeoCoding(product);

        final CompoundData startTimeCode = getDataRecord(0).getCompound("TIME_CODE");
        final ProductData.UTC startTime = toUTC(startTimeCode);
        product.setStartTime(startTime);

        final CompoundData endTimeCode = getDataRecord(dataRecordCount - 1).getCompound("TIME_CODE");
        final ProductData.UTC endTime = toUTC(endTimeCode);
        product.setEndTime(endTime);

        addMetadata(product);

        return product;
    }

    private void addMetadata(Product product) throws IOException {
        final MetadataElement tbmHeaderElement = HeaderWrapper.getAsMetadataElement(
                data.getCompound(tbmHeaderRecordIndex));
        product.getMetadataRoot().addElement(tbmHeaderElement);
        final MetadataElement datasetHeaderElement = HeaderWrapper.getAsMetadataElement(
                data.getCompound(datasetHeaderRecordIndex));
        product.getMetadataRoot().addElement(datasetHeaderElement);
    }

    // package public for testing only
    static ProductData.UTC toUTC(CompoundData timeCode) throws IOException {
        final int yearDay = timeCode.getUShort(0);
        final int year = 1900 + (yearDay >> 9);
        final int dayOfYear = yearDay & 0b0000000111111111;
        final int millisInDay = timeCode.getInt(1);

        return HeaderUtil.createUTCDate(year, dayOfYear, millisInDay);
    }

    private Band addCountsBand(Product product, int channelIndex) {
        final BandReader bandReader = BandReaderFactory.createCountBandReader(channelIndex, this);
        return addBand(product, bandReader, channelIndex);
    }

    private Band addAlbedoBand(Product product, int channelIndex) {
        final BandReader bandReader = BandReaderFactory.createAlbedoBandReader(channelIndex, this, this);
        return addBand(product, bandReader, channelIndex);
    }

    private Band addRadianceBand(Product product, int channelIndex) {
        final BandReader bandReader = BandReaderFactory.createRadianceBandReader(channelIndex, this, this);
        return addBand(product, bandReader, channelIndex);
    }

    private Band addBand(Product product, BandReader bandReader, int channelIndex) {
        final Band band = product.addBand(bandReader.getBandName(), bandReader.getDataType());

        band.setScalingFactor(bandReader.getScalingFactor());
        band.setUnit(bandReader.getBandUnit());
        band.setDescription(bandReader.getBandDescription());
        band.setSpectralBandIndex(channelIndex + 1);
        band.setValidPixelExpression(band.getName() + " != NaN");

        bandReaderMap.put(band, bandReader);

        return band;
    }

    private void addTiePointGridsAndGeoCoding(Product product) throws IOException {
        final int tiePointGridHeight = product.getSceneRasterHeight();
        final int tiePointCount = TIE_POINT_GRID_WIDTH * tiePointGridHeight;
        final float[][] gridData = new float[3][tiePointCount];

        for (int tiePointIndex = 0, y = 0; y < tiePointGridHeight; y++) {
            final int[] rawAngles = new int[TIE_POINT_GRID_WIDTH];
            final int[] rawLat = new int[TIE_POINT_GRID_WIDTH];
            final int[] rawLon = new int[TIE_POINT_GRID_WIDTH];
            final CompoundData dataRecord = getDataRecord(y);
            final SequenceData solarZenithAngleSequence = dataRecord.getSequence("SOLAR_ZENITH_ANGLES");
            for (int i = 0; i < TIE_POINT_GRID_WIDTH; i++) {
                rawAngles[i] = solarZenithAngleSequence.getUByte(i);
            }
            final SequenceData earthLocationSequence = dataRecord.getSequence("EARTH_LOCATION");
            for (int i = 0; i < TIE_POINT_GRID_WIDTH; i++) {
                CompoundData earthLocationSequenceCompound = earthLocationSequence.getCompound(i);
                rawLat[i] = earthLocationSequenceCompound.getInt(0);
                rawLon[i] = earthLocationSequenceCompound.getInt(1);
            }
            final double solarZenithAnglesScaleFactor = PodTypes.getSolarZenithAnglesMetadata().getScalingFactor();
            final double latScaleFactor = PodTypes.getLatMetadata().getScalingFactor();
            final double lonScaleFactor = PodTypes.getLonMetadata().getScalingFactor();

            for (int pointIndex = 0; pointIndex < TIE_POINT_GRID_WIDTH; pointIndex++) {
                gridData[0][tiePointIndex] = (float)(rawAngles[pointIndex] * solarZenithAnglesScaleFactor);
                gridData[1][tiePointIndex] = (float)(rawLat[pointIndex] * latScaleFactor);
                gridData[2][tiePointIndex] = (float)(rawLon[pointIndex] * lonScaleFactor);
                tiePointIndex += 1;
            }
        }

        addTiePointGrid(product, AvhrrConstants.SZA_DS_NAME, PodTypes.getSolarZenithAnglesMetadata().getUnits(),
                        tiePointGridHeight, gridData[0], TiePointGrid.DISCONT_AT_180);
        final TiePointGrid latGrid = addTiePointGrid(product, AvhrrConstants.LAT_DS_NAME,
                                                     PodTypes.getLatMetadata().getUnits(),
                                                     tiePointGridHeight, gridData[1], TiePointGrid.DISCONT_NONE);
        final TiePointGrid lonGrid = addTiePointGrid(product, AvhrrConstants.LON_DS_NAME,
                                                     PodTypes.getLonMetadata().getUnits(),
                                                     tiePointGridHeight, gridData[2], TiePointGrid.DISCONT_AT_180);

        product.setSceneGeoCoding(new PodGeoCoding(latGrid, lonGrid));
    }



    private TiePointGrid addTiePointGrid(Product product, String name, String units, int height, float[] data,
                                         int discontinuity) {
        final TiePointGrid grid = new TiePointGrid(name,
                                                   TIE_POINT_GRID_WIDTH, height,
                                                   TIE_POINT_OFFSET_X + 0.5f, 0.5f,
                                                   TIE_POINT_SAMPLING_X, 1,
                                                   data, discontinuity);
        grid.setUnit(units);
        product.addTiePointGrid(grid);
        return grid;
    }

    private CompoundData getDataRecord(int i) throws IOException {
        return data.getSequence(dataRecordsIndex).getCompound(i);
    }

}
