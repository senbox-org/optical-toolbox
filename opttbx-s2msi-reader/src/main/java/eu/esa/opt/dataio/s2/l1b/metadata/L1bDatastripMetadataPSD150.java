package eu.esa.opt.dataio.s2.l1b.metadata;

import com.bc.ceres.core.Assert;
import eu.esa.opt.dataio.s2.VirtualPath;
import eu.esa.opt.dataio.s2.l1b.L1bPSD150Constants;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.metadata.GenericXmlMetadata;
import org.esa.snap.core.metadata.XmlMetadataParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by obarrile on 07/10/2016.
 */

public class L1bDatastripMetadataPSD150 extends GenericXmlMetadata implements IL1bDatastripMetadata {
    private static class L1bDatastripMetadataPSD150Parser extends XmlMetadataParser<L1bDatastripMetadataPSD150> {

        public L1bDatastripMetadataPSD150Parser(Class metadataFileClass) {
            super(metadataFileClass);
            setSchemaLocations(L1bPSD150Constants.getDatastripSchemaLocations());
            setSchemaBasePath(L1bPSD150Constants.getDatastripSchemaBasePath());
        }

        @Override
        protected boolean shouldValidateSchema() {
            return false;
        }
    }

    public static L1bDatastripMetadataPSD150 create(VirtualPath path) throws IOException, ParserConfigurationException, SAXException {
        Assert.notNull(path);
        L1bDatastripMetadataPSD150 result = null;
        InputStream stream = null;
        try {
            if (path.exists()) {
                stream = path.getInputStream();
                L1bDatastripMetadataPSD150Parser parser = new L1bDatastripMetadataPSD150Parser(L1bDatastripMetadataPSD150.class);
                result = parser.parse(stream);
                result.setName("Level-1B_DataStrip_ID");
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

    public L1bDatastripMetadataPSD150(String name) {
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
    public MetadataElement getMetadataElement() {
        return rootElement;
    }
}
