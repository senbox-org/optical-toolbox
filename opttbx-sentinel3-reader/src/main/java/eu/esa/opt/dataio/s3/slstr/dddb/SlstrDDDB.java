package eu.esa.opt.dataio.s3.slstr.dddb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class SlstrDDDB {

    private static SlstrDDDB instance = null;
    private final ObjectMapper objectMapper;

    private ProductFiles productFiles = null;
    private VariableInformation[] variableInformations = null;

    public static SlstrDDDB instance() {
        if (instance == null) {
            instance = new SlstrDDDB();
        }
        return instance;
    }

    private SlstrDDDB() {
        objectMapper = new ObjectMapper();
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
                    throw new IOException("Unable to load resource: product_files.json");
                }

                final String content = readToString(resourceStream);
                productFiles = objectMapper.readValue(content, ProductFiles.class);
            }
        }
        return productFiles.getProductFileNames();
    }

    // @todo 3 write tests tb 2024-09-03
    private static String readToString(InputStream resourceStream) throws IOException {
        final byte[] bytes = resourceStream.readAllBytes();
        return new String(bytes);
    }

    public VariableInformation[] getVariableInformations(String type, String processingVersion) throws IOException {
        if (variableInformations == null) {
            try (InputStream resourceStream = SlstrDDDB.class.getResourceAsStream("variables.json")) {
                if (resourceStream == null) {
                    throw new IOException("Unable to load resource: variables.json");
                }

                final String content = readToString(resourceStream);
                final List<VariableInformation> varInfoList = objectMapper.readValue(content, new TypeReference<>() {
                });

                variableInformations = varInfoList.toArray(new VariableInformation[0]);
            }
        }
        return variableInformations;
    }
}
