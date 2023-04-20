package eu.esa.opt.meris;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.util.ResourceInstaller;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.runtime.Activator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModuleActivator implements Activator {
    public static final AtomicBoolean activated = new AtomicBoolean(false);

    public static final Path AUXDATA_DIR = SystemUtils.getAuxDataPath().resolve("meris-operators");

    public static void activate() {
        if (!activated.getAndSet(true)) {
            final Path sourceDirPath = ResourceInstaller.findModuleCodeBasePath(ModuleActivator.class).resolve("auxdata");

            final ResourceInstaller resourceInstaller = new ResourceInstaller(sourceDirPath, AUXDATA_DIR);
            try {
                resourceInstaller.install(".*", ProgressMonitor.NULL);
            } catch (IOException e) {
                throw new OperatorException(String.format("Failed to install auxdata into %s", AUXDATA_DIR), e);
            }
        }
    }

    @Override
    public void start() {
        activate();
    }

    @Override
    public void stop() {

    }
}
