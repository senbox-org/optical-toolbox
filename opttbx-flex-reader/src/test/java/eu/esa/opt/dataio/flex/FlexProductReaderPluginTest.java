package eu.esa.opt.dataio.flex;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReaderUtils;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class FlexProductReaderPluginTest {

    private final Path validFlexPath = Paths.get("/some/path/to/FLX_GPP__L1B_OBS____20231231T235959_20240101T000000_20250101T123456__54321.xml");
    private final Path invalidFlexPath = Paths.get("/some/path/to/FLX_GPP__L1B_OBS____2023-12-31T235959_20240101T000000_20250101T123456__54321.xml");
    private FlexProductReaderPlugin plugin;

    @Before
    public void setUp() {
        this.plugin = new FlexProductReaderPlugin();
    }

    @STTM("SNAP-3737")
    @Test
    public void testDecodeQualification() {
        assertThat(plugin.getDecodeQualification(7), is(DecodeQualification.UNABLE));
        assertThat(plugin.getDecodeQualification("C:/some/path/to/file.nc"), is(DecodeQualification.UNABLE));

        assertThat(plugin.getDecodeQualification(validFlexPath.toFile()), is(DecodeQualification.INTENDED));
        assertThat(plugin.getDecodeQualification(invalidFlexPath.toFile()), is(DecodeQualification.UNABLE));

        assertThat(plugin.getDecodeQualification(validFlexPath.toString()), is(DecodeQualification.INTENDED));
        assertThat(plugin.getDecodeQualification(invalidFlexPath.toString()), is(DecodeQualification.UNABLE));
    }

    @STTM("SNAP-3737")
    @Test
    public void testInputTypes() {
        assertThat(plugin.getInputTypes(), is(equalTo(ProductReaderUtils.IO_TYPES)));
    }

    @STTM("SNAP-3737")
    @Test
    public void testCreateReaderInstance() {
        assertThat(plugin.createReaderInstance(), is(instanceOf(FlexProductReader.class)));
    }

    @STTM("SNAP-3737")
    @Test
    public void testFormatNames() {
        assertThat(plugin.getFormatNames(), is(equalTo(new String[]{"FLEX"})));
    }

    @STTM("SNAP-3737")
    @Test
    public void testDefaultFileExtensions() {
        assertThat(plugin.getDefaultFileExtensions(), is(equalTo(new String[]{".xml"})));
    }

    @STTM("SNAP-3737")
    @Test
    public void testDescription() {
        assertThat(plugin.getDescription(null), is(equalTo("FLEX products")));
    }

    @STTM("SNAP-3737")
    @Test
    public void testProductFilesFilter() {
        final SnapFileFilter sff = plugin.getProductFileFilter();
        assertThat(sff.accept(validFlexPath.toFile()), is(true));
        assertThat(sff.accept(invalidFlexPath.toFile()), is(false));
    }

}