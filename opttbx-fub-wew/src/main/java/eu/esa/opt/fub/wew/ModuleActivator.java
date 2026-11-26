package eu.esa.opt.fub.wew;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.util.ResourceInstaller;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.runtime.Activator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

public class ModuleActivator implements Activator {

    @Override
    public void start() {
        final Path auxdataDir = SystemUtils.getAuxDataPath().resolve("color_palettes");
        final Path sourceDirPath = ResourceInstaller.findModuleCodeBasePath(this.getClass()).resolve("auxdata/color_palettes");

        final ResourceInstaller resourceInstaller = new ResourceInstaller(sourceDirPath, auxdataDir);
        try {
            resourceInstaller.install(".*.cpd", ProgressMonitor.NULL);
        } catch (IOException e) {
            SystemUtils.LOG.log(Level.WARNING, "Could not install color palettes", e);
        }
    }

    @Override
    public void stop() {

    }
}
