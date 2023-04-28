package eu.esa.opt.dataio.enmap;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class InputTypesTest {

    @Test
    public void getTypes() {
        Class<?>[] types = InputTypes.getTypes();

        assertEquals(3, types.length);
        assertEquals(Path.class, types[0]);
        assertEquals(File.class, types[1]);
        assertEquals(String.class, types[2]);
    }

    @Test
    public void toPath_fromFile() {
        File file = new File("test");
        Path path = InputTypes.toPath(file);
        assertEquals(file, path.toFile());
    }

    @Test
    public void toPath_fromPath() {
        Path input = Paths.get("test");
        Path path = InputTypes.toPath(input);
        assertEquals(input, path);
    }

    @Test
    public void toPath_fromString() {
        String input = "test";
        Path path = InputTypes.toPath(input);
        assertEquals(input, path.toString());
    }
}