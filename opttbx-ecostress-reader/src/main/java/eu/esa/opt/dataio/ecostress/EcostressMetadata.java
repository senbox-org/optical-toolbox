package eu.esa.opt.dataio.ecostress;

import com.bc.ceres.core.ServiceRegistry;
import com.bc.ceres.core.ServiceRegistryManager;
import org.esa.snap.core.util.ServiceLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Metadata for ECOSTRESS L1A, L1B, L2, L3, L4 products
 *
 * @author adraghici
 */
public abstract class EcostressMetadata {

    private static final ServiceRegistry<EcostressMetadata> ecostressMetadataRegistry;

    static {
        final ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        ecostressMetadataRegistry = serviceRegistryManager.getServiceRegistry(EcostressMetadata.class);
        ServiceLoader.loadServices(ecostressMetadataRegistry);
    }

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
        for (EcostressMetadata ecostressMetadata : ecostressMetadataRegistry.getServices()) {
            if (ecostressMetadata.isProductFileValid(ecostressFile) && EcostressUtils.ecostressNodesExists(ecostressFile, ecostressMetadata.getAllElementsPaths())) {
                return ecostressMetadata;
            }
        }
        return null;
    }
}