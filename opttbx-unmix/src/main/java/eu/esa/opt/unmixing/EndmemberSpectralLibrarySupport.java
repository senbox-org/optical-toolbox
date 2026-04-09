package eu.esa.opt.unmixing;

import org.esa.snap.speclib.io.envi.EnviSpectralLibraryIO;
import org.esa.snap.speclib.model.SpectralLibrary;
import org.esa.snap.speclib.model.SpectralProfile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class EndmemberSpectralLibrarySupport {


    private EndmemberSpectralLibrarySupport() {
    }

    public static boolean isEnviSpectralLibrary(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        return fileName.endsWith(".hdr") || fileName.endsWith(".sli");
    }

    public static Endmember[] readEnviLibrary(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        SpectralLibrary spectralLibrary = new EnviSpectralLibraryIO().read(path);
        return toEndmembers(spectralLibrary);
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
