package eu.esa.opt.dataio.enmap;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class InputTypesTest {

    @Test
    public void getTypes() {
        Class<?>[] types = InputTypes.getTypes();

        assertEquals(3, types.length);
        assertEquals(Path.class, types[0]);
        assertEquals(Path.class, types[1]);
        assertEquals(String.class, types[2]);
    }

    @Test
    public void toPath_fromFile() {
        final Path file = Path.of("test");
        final Path path = InputTypes.toPath(file);
        assertEquals(file, path);
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

    @Test
    public void toPath_invalidInput() {
        try {
            InputTypes.toPath(23.88);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }
}