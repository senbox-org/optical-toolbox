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
        FlexProductDescriptor productDescriptor = productDescriptorMap.get(productType);
        if (productDescriptor == null) {
            final String resourceName = productType + "/" + productType + ".json";
            final URL resourceUrl = getResourceUrl(resourceName);
            if (resourceUrl == null) {
                throw new IOException("Invalid DDDB resource: " + resourceName);
            }

            productDescriptor = readProductDescriptor(resourceUrl);
            productDescriptorMap.put(productType, productDescriptor);
        }
        return productDescriptor;
    }

    public FlexVariableDescriptor[] getVariableDescriptors(String dataFile, String productType) throws IOException {
        final String resourceName = productType + "/variables/" + dataFile + ".json";
        final URL resourceUrl = getResourceUrl(resourceName);
        if (resourceUrl == null) {
            throw new IOException("Requested resource not found: " + resourceName);
        }
        return readVariableDescriptors(resourceUrl);
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
