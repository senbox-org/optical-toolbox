package eu.esa.opt.dataio.ecostress;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Metadata for ECOSTRESS L1A, L1B, L2, L3, L4 products
 *
 * @author adraghici
 */
public abstract class EcostressMetadata {

    private static EcostressMetadataDescriptor[] ecostressMetadataDescriptors;

    /**
     * Gets the ECOSTRESS product specific metadata elements paths
     *
     * @return the ECOSTRESS product specific metadata elements paths
     */
    protected abstract String[] getMetadataElementsPaths();

    /**
     * Gets the ECOSTRESS product bands grouping pattern
     *
     * @return the ECOSTRESS product bands grouping pattern
     */
    protected abstract String getGroupingPattern();

    /**
     * Gets the ECOSTRESS product bands elements paths
     *
     * @return the ECOSTRESS product bands elements paths
     */
    protected abstract String[] getBandsElementsPaths();

    /**
     * Gets the format name
     *
     * @return the format name
     */
    public abstract String getFormatName();

    /**
     * Gets the ECOSTRESS product elements paths
     *
     * @return the ECOSTRESS product elements paths
     */
    public String[] getAllElementsPaths() {
        final List<String> allElementsPaths = new ArrayList<>();
        Collections.addAll(allElementsPaths, getMetadataElementsPaths());
        Collections.addAll(allElementsPaths, getBandsElementsPaths());
        return allElementsPaths.toArray(new String[0]);
    }

    /**
     * Gets the remote platform name
     *
     * @return the remote platform name
     */
    protected abstract String getRemotePlatformName();

    /**
     * Gets the regex used to validate the ECOSTRESS product file name
     *
     * @return the regex used to validate the ECOSTRESS product file name
     */
    protected abstract String getProductFileNameRegex();

    /**
     * Checks whether the ECOSTRESS product is reversed when aquised during the night
     *
     * @return {@code true} when the ECOSTRESS product is reversed
     */
    protected abstract Boolean isReversedOnNight();

    /**
     * Checks whether the ECOSTRESS product file is valid by validating its filename with a regex
     *
     * @return {@code true} when the ECOSTRESS product file is valid
     */
    protected boolean isProductFileValid(EcostressFile ecostressFile) {
        return ecostressFile.getName().replaceAll(getProductFileNameRegex(), "").isEmpty();
    }

    /**
     * Gets the Ecostress Metadata for Ecostress file
     *
     * @param ecostressFile the Ecostress file
     * @return the Ecostress Metadata for Ecostress file
     */
    public static EcostressMetadata getEcostressMetadataForFile(EcostressFile ecostressFile) {
        if (ecostressMetadataDescriptors == null) {
            try {
                ecostressMetadataDescriptors = new ObjectMapper().readValue(EcostressMetadataDescriptor.class.getResource("EcostressMetadataDescriptors.json"), EcostressMetadataDescriptor[].class);
            } catch (IOException e) {
                Logger.getLogger(EcostressMetadata.class.getName()).severe("Fail to load the ECOSTRESS Metadata Descriptor JSON. Reason:" + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        for (EcostressMetadata ecostressMetadata : ecostressMetadataDescriptors) {
            if (ecostressMetadata.isProductFileValid(ecostressFile) && EcostressUtils.ecostressNodesExists(ecostressFile, ecostressMetadata.getAllElementsPaths())) {
                return ecostressMetadata;
            }
        }
        return null;
    }
}