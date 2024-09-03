package eu.esa.opt.dataio.s3.slstr.dddb;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class SlstrDDDB {

    private static SlstrDDDB instance = null;

    private ProductFiles productFiles = null;

    public static SlstrDDDB instance() {
        if (instance == null) {
            instance = new SlstrDDDB();
        }
        return instance;
    }

    // retrieves the component file names required for this product
    // @todo 2 extend to supply a product type string tb 2024-09-03
    // @todo 3 extend to (optionally) supply a version identifier tb 2024-09-03
    // @todo 3 if no version is supplied, the latest released version shall be used. tb 2024-09-03
    String[] getProductFileNames() throws IOException {
        if (productFiles == null) {
            // get local resource URL for dddb resources
            try (InputStream resourceStream = SlstrDDDB.class.getResourceAsStream("product_files.json")) {
                if (resourceStream == null) {
                    return new String[0];
                }

                final String content = readToString(resourceStream);

                // @todo 3 refactor this tb 2024-09-03
                final ObjectMapper mapper = new ObjectMapper();
                productFiles = mapper.readValue(content, ProductFiles.class);
            }
        }
        return productFiles.getProductFileNames();
    }

    // @todo 3 write tests tb 2024-09-03
    private static String readToString(InputStream resourceStream) throws IOException {
        final byte[] bytes = resourceStream.readAllBytes();
        return new String(bytes);
    }
}
