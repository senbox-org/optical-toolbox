package eu.esa.opt.commons;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Created by jcoravu on 30/4/2019.
 * @deprecated since 10.0.0, use {@link org.esa.snap.engine_utilities.commons.FilePathInputStream} instead
 */
public class FilePathInputStream extends FilterInputStream {

    private final Path path;
    private final Closeable closeable;

    public FilePathInputStream(Path path, InputStream inputStream, Closeable closeable) {
        super(inputStream);

        this.path = path;
        this.closeable = closeable;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            if (this.closeable != null) {
                this.closeable.close();
            }
        }
    }

    public Path getPath() {
        return this.path;
    }
}
