package eu.esa.opt.dataio.s2.l1b.metadata;

import com.bc.ceres.core.Assert;
import eu.esa.opt.dataio.s2.*;
import eu.esa.opt.dataio.s2.filepatterns.S2DatastripDirFilename;
import eu.esa.opt.dataio.s2.filepatterns.S2DatastripFilename;
import eu.esa.opt.dataio.s2.filepatterns.S2GranuleDirFilename;
import eu.esa.opt.dataio.s2.l1b.L1bPSD150Constants;
import eu.esa.opt.dataio.s2.l1b.filepaterns.S2L1BDatastripFilename;
import eu.esa.opt.dataio.s2.l1b.filepaterns.S2L1BGranuleDirFilename;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.metadata.GenericXmlMetadata;
import org.esa.snap.core.metadata.XmlMetadataParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by obarrile on 07/10/2016.
 */
public class L1bProductMetadataPSD150 extends GenericXmlMetadata implements IL1bProductMetadata {

    private static class L1bProductMetadataPSD150Parser extends XmlMetadataParser<L1bProductMetadataPSD150> {

        public L1bProductMetadataPSD150Parser(Class metadataFileClass) {
            super(metadataFileClass);
            setSchemaLocations(L1bPSD150Constants.getProductSchemaLocations());
            setSchemaBasePath(L1bPSD150Constants.getProductSchemaBasePath());
        }

        @Override
        protected boolean shouldValidateSchema() {
            return false;
        }
    }



    public static L1bProductMetadataPSD150 create(VirtualPath path) throws IOException, ParserConfigurationException, SAXException {
        Assert.notNull(path);
        L1bProductMetadataPSD150 result = null;
        InputStream stream = null;
        try {
            if (path.exists()) {
                stream = path.getInputStream();
                L1bProductMetadataPSD150Parser parser = new L1bProductMetadataPSD150Parser(L1bProductMetadataPSD150.class);
                result = parser.parse(stream);
                result.setName("Level-1B_User_Product");
            }
        } finally {
            try {
                if(stream != null) {
                    stream.close();
                }
            } catch (IOException ignore) {
            }
        }
        return result;
    }
    public L1bProductMetadataPSD150(String name) {
        super(name);
    }

    @Override
    public String getFileName() {
        return null;
    }

    @Override
    public String getMetadataProfile() {
        return null;
    }

    @Override
    public S2Metadata.ProductCharacteristics getProductOrganization() {
        S2Metadata.ProductCharacteristics characteristics = new S2Metadata.ProductCharacteristics();
        characteristics.setSpacecraft(getAttributeValue(L1bPSD150Constants.PATH_PRODUCT_METADATA_SPACECRAFT, "Sentinel-2"));
        characteristics.setDatasetProductionDate(getAttributeValue(L1bPSD150Constants.PATH_PRODUCT_METADATA_SENSING_START, "Unknown"));
        characteristics.setProcessingLevel(getAttributeValue(L1bPSD150Constants.PATH_PRODUCT_METADATA_PROCESSING_LEVEL, "Level-1B"));

        characteristics.setProductStartTime(getAttributeValue(L1bPSD150Constants.PATH_PRODUCT_METADATA_PRODUCT_START_TIME, "Unknown"));
        characteristics.setProductStopTime(getAttributeValue(L1bPSD150Constants.PATH_PRODUCT_METADATA_PRODUCT_STOP_TIME, "Unknown"));

        List<S2BandInformation> aInfo = new ArrayList<>();

        aInfo.add(L1bMetadataProc.makeSpectralInformation(S2BandConstants.B1, S2SpatialResolution.R60M, getFormat()));
        aInfo.add(L1bMetadataProc.makeSpectralInformation(S2BandConstants.B2, S2SpatialResolution.R10M, getFormat()));
        aInfo.add(L1bMetadataProc.makeSpectralInformation(S2BandConstants.B3, S2SpatialResolution.R10M, getFormat()));
        aInfo.add(L1bMetadataProc.makeSpectralInformation(S2BandConstants.B4, S2SpatialResolution.R10M, getFormat()));
        aInfo.add(L1bMetadataProc.makeSpectralInformation(S2BandConstants.B5, S2SpatialResolution.R20M, getFormat()));
        aInfo.add(L1bMetadataProc.makeSpectralInformation(S2BandConstants.B6, S2SpatialResolution.R20M, getFormat()));
        aInfo.add(L1bMetadataProc.makeSpectralInformation(S2BandConstants.B7, S2SpatialResolution.R20M, getFormat()));
        aInfo.add(L1bMetadataProc.makeSpectralInformation(S2BandConstants.B8, S2SpatialResolution.R10M, getFormat()));
        aInfo.add(L1bMetadataProc.makeSpectralInformation(S2BandConstants.B8A, S2SpatialResolution.R20M, getFormat()));
        aInfo.add(L1bMetadataProc.makeSpectralInformation(S2BandConstants.B9, S2SpatialResolution.R60M, getFormat()));
        aInfo.add(L1bMetadataProc.makeSpectralInformation(S2BandConstants.B10, S2SpatialResolution.R60M, getFormat()));
        aInfo.add(L1bMetadataProc.makeSpectralInformation(S2BandConstants.B11, S2SpatialResolution.R20M, getFormat()));
        aInfo.add(L1bMetadataProc.makeSpectralInformation(S2BandConstants.B12, S2SpatialResolution.R20M, getFormat()));

        int size = aInfo.size();
        characteristics.setBandInformations(aInfo.toArray(new S2BandInformation[size]));

        return characteristics;
    }

    @Override
    public Collection<String> getTiles() {
        String[] granuleList = getAttributeValues(L1bPSD150Constants.PATH_PRODUCT_METADATA_GRANULE_LIST);
        if(granuleList == null) {
            granuleList = getAttributeValues(L1bPSD150Constants.PATH_PRODUCT_METADATA_GRANULE_LIST_ALT);
        }
        if(granuleList == null) {
            return null;
        }
        return new ArrayList<>(Arrays.asList(granuleList));
    }

    @Override
    public S2DatastripFilename getDatastrip() {
        String[] datastripList = getAttributeValues(L1bPSD150Constants.PATH_PRODUCT_METADATA_DATASTRIP_LIST);
        if(datastripList == null) {
            datastripList = getAttributeValues(L1bPSD150Constants.PATH_PRODUCT_METADATA_DATASTRIP_LIST_ALT);
        }
        if(datastripList == null) {
            return null;
        }

        S2DatastripDirFilename dirDatastrip = S2DatastripDirFilename.create(datastripList[0], null);

        S2DatastripFilename datastripFilename = null;
        if (dirDatastrip != null) {
            String fileName = dirDatastrip.getFileName(null);

            if (fileName != null) {
                datastripFilename = S2L1BDatastripFilename.create(fileName);
            }
        }

        return datastripFilename;
    }

    @Override
    public S2DatastripDirFilename getDatastripDir() {

        String[] granuleList = getAttributeValues(L1bPSD150Constants.PATH_PRODUCT_METADATA_GRANULE_LIST);
        if(granuleList == null) {
            granuleList = getAttributeValues(L1bPSD150Constants.PATH_PRODUCT_METADATA_GRANULE_LIST_ALT);
        }
        String[] datastripList = getAttributeValues(L1bPSD150Constants.PATH_PRODUCT_METADATA_DATASTRIP_LIST);
        if(datastripList == null) {
            datastripList = getAttributeValues(L1bPSD150Constants.PATH_PRODUCT_METADATA_DATASTRIP_LIST_ALT);
        }
        if(granuleList == null || datastripList == null) {
            return null;
        }
        S2GranuleDirFilename grafile = S2L1BGranuleDirFilename.create(granuleList[0]);

        S2DatastripDirFilename datastripDirFilename = null;
        if (grafile != null) {
            String fileCategory = grafile.fileCategory;
            String dataStripMetadataFilenameCandidate = datastripList[0];
            datastripDirFilename = S2DatastripDirFilename.create(dataStripMetadataFilenameCandidate, fileCategory);

        }
        return datastripDirFilename;
    }

    @Override
    public MetadataElement getMetadataElement() {
        return rootElement;
    }

    @Override
    public String getFormat() {
        return getAttributeValue(L1bPSD150Constants.PATH_PRODUCT_METADATA_PRODUCT_FORMAT, "SAFE");
    }
}
