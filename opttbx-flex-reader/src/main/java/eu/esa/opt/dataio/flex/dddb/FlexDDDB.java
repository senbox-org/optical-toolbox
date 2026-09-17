package eu.esa.opt.dataio.flex.dddb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class FlexDDDB {

    private static final String DB_RESOURCE_PATH = "eu/esa/opt/dataio/flex/dddb/";

    private final HashMap<String, FlexProductDescriptor> productDescriptorMap;

    public static FlexDDDB getInstance() {
        return InstanceHolder.instance;
    }

    private FlexDDDB() {
        productDescriptorMap = new HashMap<>();
    }

    public FlexProductDescriptor getProductDescriptor(String productType) throws IOException {
        return getProductDescriptor(productType, null);
    }

    public FlexProductDescriptor getProductDescriptor(String productType, String version) throws IOException {
        String resourceName = getProductResourceName(productType, version);
        URL resourceUrl = getResourceUrl(resourceName);
        if (resourceUrl == null && !isNullOrEmpty(version)) {
            resourceName = getProductResourceName(productType, null);
            resourceUrl = getResourceUrl(resourceName);
        }

        FlexProductDescriptor productDescriptor = productDescriptorMap.get(resourceName);
        if (productDescriptor == null) {
            if (resourceUrl == null) {
                throw new IOException("Invalid DDDB resource: " + resourceName);
            }

            productDescriptor = readProductDescriptor(resourceUrl);
            productDescriptorMap.put(resourceName, productDescriptor);
        }
        return productDescriptor;
    }

    public FlexVariableDescriptor[] getVariableDescriptors(String dataFile, String productType) throws IOException {
        return getVariableDescriptors(dataFile, productType, null);
    }

    public FlexVariableDescriptor[] getVariableDescriptors(String dataFile, String productType, String version) throws IOException {
        String resourceName = getVariableResourceName(dataFile, productType, version);
        URL resourceUrl = getResourceUrl(resourceName);
        if (resourceUrl == null && !isNullOrEmpty(version)) {
            resourceName = getVariableResourceName(dataFile, productType, null);
            resourceUrl = getResourceUrl(resourceName);
        }

        if (resourceUrl == null) {
            throw new IOException("Requested resource not found: " + resourceName);
        }
        return readVariableDescriptors(resourceUrl);
    }

    static String getProductResourceName(String productType, String version) {
        return productType + "/" + getResourceFileName(productType, version);
    }

    static String getVariableResourceName(String dataFile, String productType, String version) {
        if (isNullOrEmpty(version)) {
            return productType + "/variables/" + dataFile + ".json";
        }
        final String normalizedVersion = version.trim();
        return productType + "/variables_" + normalizedVersion + "/" + dataFile + "_" + normalizedVersion + ".json";
    }

    static String getResourceFileName(String name, String version) {
        if (isNullOrEmpty(version)) {
            return name + ".json";
        }
        return name + "_" + version.trim() + ".json";
    }

    private static FlexProductDescriptor readProductDescriptor(URL resourceUrl) throws IOException {
        try (InputStream inputStream = resourceUrl.openStream()) {
            final JSONParser parser = getParser();
            try {
                final JSONObject json = (JSONObject) parser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                final ObjectMapper objectMapper = getObjectMapper();
                return objectMapper.readValue(json.toJSONString(), FlexProductDescriptor.class);
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }
    }

    private static FlexVariableDescriptor[] readVariableDescriptors(URL resourceUrl) throws IOException {
        try (InputStream inputStream = resourceUrl.openStream()) {
            final JSONParser parser = getParser();
            try {
                final JSONArray json = (JSONArray) parser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                final ObjectMapper objectMapper = getObjectMapper();
                return objectMapper.readValue(json.toJSONString(), FlexVariableDescriptor[].class);
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }
    }

    private URL getResourceUrl(String resourceName) {
        return FlexDDDB.class.getClassLoader().getResource(DB_RESOURCE_PATH + resourceName);
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static ObjectMapper getObjectMapper() {
        return ObjectMapperHolder.objectMapper;
    }

    private static JSONParser getParser() {
        return JsonParserHolder.jsonParser;
    }

    private static class InstanceHolder {
        private static final FlexDDDB instance = new FlexDDDB();
    }

    private static class ObjectMapperHolder {
        private static final ObjectMapper objectMapper = new ObjectMapper();
    }

    private static class JsonParserHolder {
        private static final JSONParser jsonParser = new JSONParser();
    }
}
