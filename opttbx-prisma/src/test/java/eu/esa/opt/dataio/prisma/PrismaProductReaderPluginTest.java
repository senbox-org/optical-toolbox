package eu.esa.opt.dataio.prisma;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class PrismaProductReaderPluginTest {

    private final Path validHe5Path = Paths.get("\\a\\path\\PRS_L2D_STD_20230815104500_20230815104504_0001.he5");
    private final Path validZipPath = Paths.get("\\a\\path\\PRS_L2D_STD_20230815104500_20230815104504_0001.zip");
    private final Path invalidHdfPath = Paths.get("\\a\\path\\PRS_L2D_STD_20230815104500_20230815104504_0001.hdf");
    private final Path invalidZapPath = Paths.get("\\a\\path\\PRS_L2D_STD_20230815104500_20230815104504_0001.zap");
    private PrismaL2ProductReaderPlugin plugin;

    @Before
    @STTM("SNAP-3445")
    public void setUp() {
        plugin = new PrismaL2ProductReaderPlugin();
    }

    @Test
    @STTM("SNAP-3445")
    public void getDecodeQualification() {
        assertThat(plugin.getDecodeQualification(new Integer(3)), is(DecodeQualification.UNABLE));
        assertThat(plugin.getDecodeQualification("c:\\a\\path\\aFile.name"), is(DecodeQualification.UNABLE));

        // Path objects are allowed. See PrismaProductReaderPlugin.getInputTypes()
        assertThat(plugin.getDecodeQualification(validHe5Path), is(DecodeQualification.INTENDED));
        assertThat(plugin.getDecodeQualification(invalidHdfPath), is(DecodeQualification.UNABLE));
        assertThat(plugin.getDecodeQualification(validZipPath), is(DecodeQualification.INTENDED));
        assertThat(plugin.getDecodeQualification(invalidZapPath), is(DecodeQualification.UNABLE));

        // File objects are allowed. See PrismaProductReaderPlugin.getInputTypes()
        assertThat(plugin.getDecodeQualification(validHe5Path.toFile()), is(DecodeQualification.INTENDED));
        assertThat(plugin.getDecodeQualification(invalidHdfPath.toFile()), is(DecodeQualification.UNABLE));
        assertThat(plugin.getDecodeQualification(validZipPath.toFile()), is(DecodeQualification.INTENDED));
        assertThat(plugin.getDecodeQualification(invalidZapPath.toFile()), is(DecodeQualification.UNABLE));

        // String objects are allowed. See PrismaProductReaderPlugin.getInputTypes()
        assertThat(plugin.getDecodeQualification(validHe5Path.toString()), is(DecodeQualification.INTENDED));
        assertThat(plugin.getDecodeQualification(invalidHdfPath.toString()), is(DecodeQualification.UNABLE));
        assertThat(plugin.getDecodeQualification(validZipPath.toString()), is(DecodeQualification.INTENDED));
        assertThat(plugin.getDecodeQualification(invalidZapPath.toString()), is(DecodeQualification.UNABLE));
    }

    @Test
    @STTM("SNAP-3445")
    public void getInputTypes() {
        final Class[] inputTypes = plugin.getInputTypes();
        assertThat(inputTypes, is(equalTo(new Class[]{Path.class, File.class, String.class})));
    }

    @Test
    @STTM("SNAP-3445")
    public void createReaderInstance() {
        assertThat(plugin.createReaderInstance(), is(instanceOf(PrismaL2ProductReader.class)));
    }

    @Test
    @STTM("SNAP-3445")
    public void getFormatNames() {
        assertThat(plugin.getFormatNames(), is(equalTo(new String[]{"PRISMA"})));
    }

    @Test
    @STTM("SNAP-3445")
    public void getDefaultFileExtensions() {
        assertThat(plugin.getDefaultFileExtensions(), is(equalTo(new String[]{".he5", ".zip"})));
    }

    @Test
    @STTM("SNAP-3445")
    public void getProductFileFilter() {
        final SnapFileFilter pff = plugin.getProductFileFilter();
        assertThat(pff.accept(validHe5Path.toFile()), is(true) );
        assertThat(pff.accept(validZipPath.toFile()), is(true) );
        assertThat(pff.accept(invalidHdfPath.toFile()), is(false) );
        assertThat(pff.accept(invalidZapPath.toFile()), is(false) );
    }

    @Test
    @STTM("SNAP-3445")
    public void getDescription() {
        assertThat(plugin.getDescription(null), is(equalTo("Prisma ASI (Agenzia Spaziale Italiana)")));
    }
}