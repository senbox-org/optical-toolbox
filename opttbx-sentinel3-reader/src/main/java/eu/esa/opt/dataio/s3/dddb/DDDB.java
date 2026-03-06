package eu.esa.opt.dataio.s3.dddb;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.io.FileUtils;
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

/*
This class maintains the database of product description metadata that is missing in the
manifest file to contruct a fully equipped SNAP in-memory product.

All metadata is stored as json files in the resources tree of the module (/resources/dddb).
For each product type, e.g. olci L2W, there exists a sub directory named as the type.

This directory contains the product description resources for this product type.

Product Descriptor
==================
Contains product specific information:
- excludedIds - comma separated list of manifest dataIds not to read, empty by default


 */

public class DDDB {

    private static final String DB_RESOURCE_PATH = "dddb/";

    private final HashMap<String, ProductDescriptor> productDescriptorMap;

    public static DDDB getInstance() {
        return InstanceHolder.instance;
    }

    private DDDB() {
        productDescriptorMap = new HashMap<>();
    }

    public ProductDescriptor getProductDescriptor(String productType, String version) throws IOException {
        String resourceName = getResourceFileName(productType, version);

        ProductDescriptor productDescriptor = productDescriptorMap.get(resourceName);
        if (productDescriptor == null) {
            URL resourceUrl = getResourceUrl(productType, resourceName);
            if (resourceUrl == null) {
                // try if we have a default, unversioned, version of the file tb 2025-02-03
                resourceName = getResourceFileName(productType, null);
                resourceUrl = getResourceUrl(productType, resourceName);
            }

            if (resourceUrl == null) {
                throw new IOException("Invalid DDDB resource: " + resourceName);
            }

            productDescriptor = readProductDescriptor(resourceUrl);
            productDescriptorMap.put(resourceName, productDescriptor);
        }

        return productDescriptor;
    }

    public VariableDescriptor[] getVariableDescriptors(String dataFile, String productType, String version) throws IOException {
        final String inputFileName = FileUtils.getFilenameWithoutExtension(dataFile);

        String resourceFileName = getResourceFileName("variables/" + inputFileName, version);
        URL resourceUrl = getResourceUrl(productType, resourceFileName);
        if (resourceUrl == null) {
            // try if we have a default, unversioned, version of the file tb 2024-12-13
            resourceFileName = getResourceFileName("variables/" + inputFileName, null);
            resourceUrl = getResourceUrl(productType, resourceFileName);
        }

        if (resourceUrl == null) {
            throw new IOException("Requested resource not found: " + resourceFileName);
        }

        return readVariableDescriptors(resourceUrl);
    }

    private static ProductDescriptor readProductDescriptor(URL resourceUrl) throws IOException {
        ProductDescriptor productDescriptor;

        try (InputStream inputStream = resourceUrl.openStream()) {
            final JSONParser parser = getParser();

            try {
                final JSONObject json = (JSONObject) parser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String jsonString = json.toJSONString();

                final ObjectMapper objectMapper = getObjectMapper();
                productDescriptor = objectMapper.readValue(jsonString, ProductDescriptor.class);
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }
        return productDescriptor;
    }

    private static VariableDescriptor[] readVariableDescriptors(URL resourceUrl) throws IOException {
        VariableDescriptor[] variableDescriptors;

        try (InputStream inputStream = resourceUrl.openStream()) {
            final JSONParser parser = getParser();

            try {
                final JSONArray json = (JSONArray) parser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String jsonString = json.toJSONString();

                final ObjectMapper objectMapper = getObjectMapper();
                variableDescriptors = objectMapper.readValue(jsonString, VariableDescriptor[].class);
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }

        return variableDescriptors;
    }

    private static ObjectMapper getObjectMapper() {
        return ObjectMapperHolder.objectMapper;
    }

    private static JSONParser getParser() {
        return JsonParserHolder.jsonParser;
    }

    private static URL getResourceUrl(String productType, String resourceName) {
        final String dddbResourceName = getDddbResourceName(productType, resourceName);
        return DDDB.class.getClassLoader().getResource(dddbResourceName);
    }

    static String getDddbResourceName(String productType, String resourceName) {
        if (StringUtils.isNullOrEmpty(resourceName)) {
            return DB_RESOURCE_PATH + productType;
        }
        return DB_RESOURCE_PATH + productType + "/" + resourceName;
    }

    static String getResourceFileName(String productType, String version) {
        if (StringUtils.isNullOrEmpty(version)) {
            return productType + ".json";
        }
        return productType + "_" + version + ".json";
    }

    private static class InstanceHolder {
        private static final DDDB instance = new DDDB();
    }

    private static class ObjectMapperHolder {
        private static final ObjectMapper objectMapper = new ObjectMapper();
    }

    private static class JsonParserHolder {
        private static final JSONParser jsonParser = new JSONParser();
    }
}
