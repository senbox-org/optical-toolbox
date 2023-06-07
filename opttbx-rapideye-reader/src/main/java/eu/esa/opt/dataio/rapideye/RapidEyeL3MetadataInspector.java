package eu.esa.opt.dataio.rapideye;

import org.esa.snap.engine_utilities.commons.FilePathInputStream;
import org.esa.snap.engine_utilities.dataio.VirtualDirEx;
import eu.esa.opt.dataio.rapideye.metadata.RapidEyeConstants;
import eu.esa.opt.dataio.rapideye.metadata.RapidEyeMetadata;
import eu.esa.opt.dataio.readers.MetadataList;
import eu.esa.opt.dataio.readers.RastersMetadata;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.metadata.MetadataInspector;
import org.esa.snap.dataio.geotiff.GeoTiffImageReader;
import org.esa.snap.dataio.geotiff.GeoTiffProductReader;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by jcoravu on 9/12/2019.
 */
public class RapidEyeL3MetadataInspector implements MetadataInspector {

    public RapidEyeL3MetadataInspector() {
    }

    @Override
    public Metadata getMetadata(Path productPath) throws IOException {
        try (VirtualDirEx productDirectory = VirtualDirEx.build(productPath, false, true)) {
            MetadataList<RapidEyeMetadata> metadataList = RapidEyeL3Reader.readMetadata(productDirectory);

            RastersMetadata rastersMetadata = RapidEyeL3Reader.computeMaximumDefaultProductSize(metadataList, productDirectory);

            Metadata metadata = new Metadata(rastersMetadata.getMaximumWidh(), rastersMetadata.getMaximumHeight());

            for (int i = 0; i < metadataList.getCount(); i++) {
                String[] bandNames = RapidEyeConstants.BAND_NAMES;
                String bandPrefix = RapidEyeL3Reader.computeBandPrefix(metadataList.getCount(), i);
                for (int k = 0; k < bandNames.length; k++) {
                    String bandName = bandPrefix + bandNames[k];
                    metadata.addBandName(bandName);
                }
            }

            GeoCoding geoCoding = addGeoCoding(metadataList, productDirectory);
            metadata.setGeoCoding(geoCoding);

            return metadata;
        } catch (RuntimeException | IOException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IOException(exception);
        }
    }

    private GeoCoding addGeoCoding(MetadataList<RapidEyeMetadata> metadataList, VirtualDirEx productDirectory) throws Exception {
        boolean inputStreamSuccess = false;
        GeoTiffImageReader geoTiffImageReader;
        FilePathInputStream filePathInputStream = productDirectory.getInputStream(metadataList.getMetadataImageRelativePath(0));
        try {
            geoTiffImageReader = new GeoTiffImageReader(filePathInputStream, null);
            inputStreamSuccess = true;
        } finally {
            if (!inputStreamSuccess) {
                filePathInputStream.close();
            }
        }
        return GeoTiffProductReader.readGeoCoding(geoTiffImageReader, null);
    }
}
