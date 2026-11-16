package eu.esa.opt.unmixing;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.speclib.io.SpectralLibraryIO;
import org.esa.snap.speclib.model.SpectralAxis;
import org.esa.snap.speclib.model.SpectralLibrary;
import org.esa.snap.speclib.model.SpectralProfile;
import org.esa.snap.speclib.model.SpectralSignature;
import org.esa.snap.speclib.model.AttributeSchema;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;


public class EndmemberSpectralLibrarySupportTest {


    @Test
    @STTM("SNAP-4336")
    public void readsAllLibrariesFromGenericSource() throws Exception {
        SpectralLibrary first = library("first", 400, 500);
        SpectralLibrary second = library("second", 600, 700);
        SpectralLibraryIO io = new SpectralLibraryIO() {
            @Override
            public SpectralLibrary read(Path path) {
                return first;
            }

            @Override
            public List<SpectralLibrary> readAll(Path path) {
                return List.of(first, second);
            }

            @Override
            public void write(SpectralLibrary library, Path path) { }

            @Override
            public boolean canRead(Path path) {
                return true;
            }

            @Override
            public boolean canWrite(Path path) {
                return true;
            }

            @Override
            public List<String> getFileExtensions() {
                return List.of("test");
            }
        };

        Endmember[] endmembers = EndmemberSpectralLibrarySupport.readSpectralLibrary(Path.of("input.test"), io);

        assertEquals(2, endmembers.length);
        assertEquals("first", endmembers[0].getName());
        assertEquals("second", endmembers[1].getName());
        assertEquals(600.0, endmembers[1].getWavelength(0), 0.0);
    }

    private static SpectralLibrary library(String name, double first, double second) {
        SpectralAxis axis = new SpectralAxis(new double[]{first, second}, "nm");
        SpectralProfile profile = SpectralProfile.create(name, SpectralSignature.of(new double[]{0.1, 0.2}));
        return new SpectralLibrary(UUID.randomUUID(), name, axis, null, List.of(profile), new AttributeSchema());
    }
}
