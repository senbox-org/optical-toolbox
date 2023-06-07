package eu.esa.opt.dataio.worldview2esa;

import org.esa.snap.engine_utilities.dataio.readers.BaseProductReaderPlugIn;
import eu.esa.opt.dataio.worldview2esa.common.WorldView2ESAConstants;
import org.esa.snap.core.metadata.MetadataInspector;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.datamodel.RGBImageProfile;
import org.esa.snap.core.datamodel.RGBImageProfileManager;

import java.util.Locale;

/**
 * Plugin for reading WorldView ESA archive files
 */

public class WorldView2ESAProductReaderPlugin extends BaseProductReaderPlugIn {
    private static final String COLOR_PALETTE_FILE_NAME = "WorldView2ESA_color_palette.cpd";

    public WorldView2ESAProductReaderPlugin() {
        super("eu/esa/opt/dataio/worldview2esa/" + WorldView2ESAProductReaderPlugin.COLOR_PALETTE_FILE_NAME);
        this.folderDepth = 1;
    }

    @Override
    public MetadataInspector getMetadataInspector() {
        return new WorldView2ESAMetadataInspector();
    }

    @Override
    public Class[] getInputTypes() {
        return WorldView2ESAConstants.READER_INPUT_TYPES;
    }

    @Override
    public ProductReader createReaderInstance() {
        return new WorldView2ESAProductReader(this);
    }

    @Override
    public String[] getFormatNames() {
        return WorldView2ESAConstants.FORMAT_NAMES;
    }

    @Override
    public String[] getDefaultFileExtensions() {
        return WorldView2ESAConstants.DEFAULT_EXTENSIONS;
    }

    @Override
    public String getDescription(Locale locale) {
        return WorldView2ESAConstants.DESCRIPTION;
    }

    @Override
    protected String[] getMinimalPatternList() {
        return WorldView2ESAConstants.MINIMAL_PRODUCT_PATTERNS;
    }

    @Override
    protected String[] getExclusionPatternList() {
        return new String[0];
    }

    @Override
    protected void registerRGBProfile() {
        RGBImageProfileManager.getInstance().addProfile(new RGBImageProfile("WorldView-2 ESA", WorldView2ESAConstants.WORLDVIEW2_RGB_PROFILE));
    }
}
