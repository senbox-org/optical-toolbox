package eu.esa.opt.unmixing;

import org.esa.snap.speclib.io.CompositeSpectralLibraryIO;
import org.esa.snap.speclib.io.SpectralLibraryIO;
import org.esa.snap.speclib.model.SpectralLibrary;
import org.esa.snap.speclib.model.SpectralProfile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;


public class EndmemberSpectralLibrarySupport {


    private EndmemberSpectralLibrarySupport() {
    }


    public static boolean isSpectralLibrary(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        return new CompositeSpectralLibraryIO().canRead(path);
    }

    public static Endmember[] readSpectralLibrary(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        return readSpectralLibrary(path, new CompositeSpectralLibraryIO());
    }

    static Endmember[] readSpectralLibrary(Path path, SpectralLibraryIO libraryIO) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        Objects.requireNonNull(libraryIO, "libraryIO must not be null");
        List<Endmember> result = new ArrayList<>();

        for (SpectralLibrary library : libraryIO.readAll(path)) {
            result.addAll(Arrays.asList(toEndmembers(library)));
        }
        return result.toArray(new Endmember[0]);
    }

    public static Endmember[] toEndmembers(SpectralLibrary spectralLibrary) {
        Objects.requireNonNull(spectralLibrary, "spectralLibrary must not be null");

        double[] wavelengths = spectralLibrary.getAxis().getWavelengths();
        List<SpectralProfile> profiles = spectralLibrary.getProfiles();
        List<Endmember> endmembers = new ArrayList<>(profiles.size());

        for (SpectralProfile profile : profiles) {
            endmembers.add(new Endmember(
                    profile.getName(),
                    Arrays.copyOf(wavelengths, wavelengths.length),
                    profile.getSignature().getValues()
            ));
        }

        return endmembers.toArray(new Endmember[0]);
    }
}
