package eu.esa.opt.dataio.enmap;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

final class InputTypes {

    private static final Class<?>[] types;
    private static final String[] typeNames;

    static {
        types = new Class[]{
                Path.class,
                File.class,
                String.class
        };

        typeNames = new String[types.length];
        IntStream.range(0, types.length).forEach(i -> typeNames[i] = types[i].getSimpleName());
    }

    private InputTypes() {
    }

    static Class<?>[] getTypes() {
        return types;
    }

    static Path toPath(Object inputObject) {
        if (inputObject instanceof Path) {
            return (Path) inputObject;
        }
        if (inputObject instanceof File) {
            return ((File) inputObject).toPath();
        }
        if (inputObject instanceof String) {
            return Paths.get((String) inputObject);
        }

        throw new IllegalArgumentException(String.format("Unsupported input type '%s'. Must be one of %s", String.valueOf(inputObject), StringUtils.join(typeNames, ",")));
    }
}
