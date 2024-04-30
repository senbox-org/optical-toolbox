package gov.nasa.gsfc.seadas.dataio;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.dataio.ProductIOException;
import org.esa.snap.core.datamodel.*;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Reader for "Level 3 SeaDAS Mapped" file formats
 */
public class Level3_SeadasMappedFileReader extends SeadasFileReader {

    Level3_SeadasMappedFileReader(SeadasProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct() throws ProductIOException {

        int[] dims;
        int sceneHeight = 0;
        int sceneWidth = 0;
        Group geodata = ncFile.findGroup("geophysical_data");
        if (geodata == null) {
            geodata = ncFile.findGroup("Geophysical_Data");
        }
        if (productReader.getProductType() == SeadasProductReader.ProductType.OISST) {
            dims = ncFile.getVariables().get(4).getShape();
            sceneHeight = dims[2];
            sceneWidth = dims[3];
            mustFlipY = true;
        } else if (productReader.getProductType() == SeadasProductReader.ProductType.ANCCLIM) {
            List<Variable> vars = ncFile.getVariables();
            for (Variable v : vars) {
                if (v.getRank() == 2) {
                    dims = v.getShape();
                    sceneHeight = dims[0];
                    sceneWidth = dims[1];
                }
            }
        } else {
            if (geodata != null){
                dims = geodata.getVariables().get(0).getShape();
                sceneHeight = dims[0];
                sceneWidth = dims[1];
            } else {
                ucar.nc2.Dimension latdim = ncFile.findDimension("lat");
                ucar.nc2.Dimension londim = ncFile.findDimension("lon");
                if (latdim != null) {
                    sceneHeight = latdim.getLength();
                    sceneWidth = londim.getLength();
                } else {
                    dims = ncFile.getVariables().get(0).getShape();
                    sceneHeight = dims[0];
                    sceneWidth = dims[1];
                }
            }
        }

        String productName = productReader.getInputFile().getName();
        try {
            productName = getStringAttribute("Product_Name");
        } catch (Exception ignored) {

        }

        SeadasProductReader.ProductType productType = productReader.getProductType();

        Product product = new Product(productName, productType.toString(), sceneWidth, sceneHeight);
        product.setDescription(productName);

        product.setFileLocation(productReader.getInputFile());
        product.setProductReader(productReader);

        setStartTime(product);
        setEndTime(product);

        addGlobalMetadata(product);
        addSmiMetadata(product);
//        variableMap = addBands(product, ncFile.getVariables());
        variableMap = addBands(product, ncFile.getVariables());
        try {
            addGeocoding(product);
        } catch (Exception ignored) {
        }
        addFlagsAndMasks(product);
        if (productReader.getProductType() == SeadasProductReader.ProductType.Bathy) {
            mustFlipY = true;
            Dimension tileSize = new Dimension(640, 320);
            product.setPreferredTileSize(tileSize);
        }
        product.setAutoGrouping("Rrs:nLw:Lt:La:Lr:Lw:L_q:L_u:Es:TLg:rhom:rhos:rhot:Taua:Kd:aot:adg:aph_:bbp:vgain:BT:tg_sol:tg_sen");
        return product;
    }

    public void addGeocoding(final Product product) throws ProductIOException {
        final String longitude = "longitude";
        final String latitude = "latitude";
        Band latBand;
        Band lonBand;

        latBand = product.getBand(latitude);
        lonBand = product.getBand(longitude);
        latBand.setNoDataValue(-999.);
        lonBand.setNoDataValue(-999.);
        latBand.setNoDataValueUsed(true);
        lonBand.setNoDataValueUsed(true);

        try {

            product.setSceneGeoCoding(new PixelGeoCoding(latBand, lonBand, null, 5, ProgressMonitor.NULL));

        } catch (IOException e) {
            throw new ProductIOException(e.getMessage(), e);
        }
    }

    private void setEndTime(Product product) {
        ProductData.UTC coverageEndTime = getUTCAttribute("time_coverage_end");
        if(coverageEndTime != null) {
            product.setEndTime(coverageEndTime);
        }
    }

    private void setStartTime(Product product) {
        ProductData.UTC coverageStartTime = getUTCAttribute("time_coverage_start");
        if(coverageStartTime != null) {
            product.setStartTime(coverageStartTime);
        }
    }

    private static boolean isShifted180(Array lonData) {
        final Index i0 = lonData.getIndex().set(0);
        final Index i1 = lonData.getIndex().set(1);
        final Index iN = lonData.getIndex().set((int) lonData.getSize() - 1);
        double lonDelta = (lonData.getDouble(i1) - lonData.getDouble(i0));

        return (lonData.getDouble(0) < lonDelta && lonData.getDouble(iN) > 360.0 - lonDelta);
    }

    public void addSmiMetadata(final Product product) {
//        Variable l3mvar = ncFile.findVariable("l3m_data");
        final MetadataElement bandAttributes = new MetadataElement("Band_Attributes");
        List<Variable> variables = ncFile.getVariables();
        for (Variable variable : variables) {
            final String name = variable.getShortName();
            final MetadataElement sdsElement = new MetadataElement(name);
            final int dataType = getProductDataType(variable);
            final MetadataAttribute prodtypeattr = new MetadataAttribute("data_type", dataType);

            sdsElement.addAttribute(prodtypeattr);
            bandAttributes.addElement(sdsElement);

            final List<Attribute> list = variable.getAttributes();
            for (Attribute varAttribute : list) {
                addAttributeToElement(sdsElement, varAttribute);
            }
        }
        final MetadataElement metadataRoot = product.getMetadataRoot();
        metadataRoot.addElement(bandAttributes);

    }

    @Override
    protected void addFlagsAndMasks(Product product) {
        Band QFBand = product.getBand("l3m_qual");
        if (QFBand != null) {
            FlagCoding flagCoding = new FlagCoding("SST_Quality");
            flagCoding.addFlag("Best", 0x00, "Highest quality retrieval");
            flagCoding.addFlag("Good", 0x01, "Good quality retrieval");
            flagCoding.addFlag("Questionable", 0x02, "Questionable quality retrieval");
            flagCoding.addFlag("Bad", 0x03, "Bad quality retrieval");
            product.getFlagCodingGroup().add(flagCoding);
            QFBand.setSampleCoding(flagCoding);

            product.getMaskGroup().add(Mask.BandMathsType.create("Best", "Highest quality retrieval",
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(), "l3m_qual == 0",
                    SeadasFileReader.Cornflower, 0.6));
            product.getMaskGroup().add(Mask.BandMathsType.create("Good", "Good quality retrieval",
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(), "l3m_qual == 1",
                    SeadasFileReader.LightPurple, 0.6));
            product.getMaskGroup().add(Mask.BandMathsType.create("Questionable", "Questionable quality retrieval",
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(), "l3m_qual == 2",
                    SeadasFileReader.BurntUmber, 0.6));
            product.getMaskGroup().add(Mask.BandMathsType.create("Bad", "Bad quality retrieval",
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(), "l3m_qual == 3",
                    SeadasFileReader.FailRed, 0.6));

        }
        QFBand = product.getBand("qual_sst");
        if (QFBand != null) {
            FlagCoding flagCoding = new FlagCoding("SST_Quality");
            flagCoding.addFlag("Best", 0x00, "Highest quality retrieval");
            flagCoding.addFlag("Good", 0x01, "Good quality retrieval");
            flagCoding.addFlag("Questionable", 0x02, "Questionable quality retrieval");
            flagCoding.addFlag("Bad", 0x03, "Bad quality retrieval");
            product.getFlagCodingGroup().add(flagCoding);
            QFBand.setSampleCoding(flagCoding);

            product.getMaskGroup().add(Mask.BandMathsType.create("Best", "Highest quality retrieval",
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(), "qual_sst == 0",
                    SeadasFileReader.Cornflower, 0.6));
            product.getMaskGroup().add(Mask.BandMathsType.create("Good", "Good quality retrieval",
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(), "qual_sst == 1",
                    SeadasFileReader.LightPurple, 0.6));
            product.getMaskGroup().add(Mask.BandMathsType.create("Questionable", "Questionable quality retrieval",
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(), "qual_sst == 2",
                    SeadasFileReader.BurntUmber, 0.6));
            product.getMaskGroup().add(Mask.BandMathsType.create("Bad", "Bad quality retrieval",
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(), "qual_sst == 3",
                    SeadasFileReader.FailRed, 0.6));
            product.getMaskGroup().add(Mask.BandMathsType.create("No Data", "No data retrieval",
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(), "qual_sst == -1",
                    SeadasFileReader.MediumGray, 0.6));
        }
        QFBand = product.getBand("qual_sst4");
        if (QFBand != null) {
            FlagCoding flagCoding = new FlagCoding("SST_Quality");
            flagCoding.addFlag("Best", 0x00, "Highest quality retrieval");
            flagCoding.addFlag("Good", 0x01, "Good quality retrieval");
            flagCoding.addFlag("Questionable", 0x02, "Questionable quality retrieval");
            flagCoding.addFlag("Bad", 0x03, "Bad quality retrieval");
            product.getFlagCodingGroup().add(flagCoding);
            QFBand.setSampleCoding(flagCoding);

            product.getMaskGroup().add(Mask.BandMathsType.create("Best", "Highest quality retrieval",
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(), "qual_sst4 == 0",
                    SeadasFileReader.Cornflower, 0.6));
            product.getMaskGroup().add(Mask.BandMathsType.create("Good", "Good quality retrieval",
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(), "qual_sst4 == 1",
                    SeadasFileReader.LightPurple, 0.6));
            product.getMaskGroup().add(Mask.BandMathsType.create("Questionable", "Questionable quality retrieval",
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(), "qual_sst4 == 2",
                    SeadasFileReader.BurntUmber, 0.6));
            product.getMaskGroup().add(Mask.BandMathsType.create("Bad", "Bad quality retrieval",
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(), "qual_sst4 == 3",
                    SeadasFileReader.FailRed, 0.6));
            product.getMaskGroup().add(Mask.BandMathsType.create("No Data", "No data retrieval",
                    product.getSceneRasterWidth(),
                    product.getSceneRasterHeight(), "qual_sst4 == -1",
                    SeadasFileReader.MediumGray, 0.6));
        }
    }
}