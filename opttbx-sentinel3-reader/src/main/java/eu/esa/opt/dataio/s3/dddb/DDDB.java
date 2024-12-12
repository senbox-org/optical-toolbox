package eu.esa.opt.dataio.s3.dddb;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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

    public static DDDB getInstance() {
        return InstanceHolder.instance;
    }

    public ProductDescriptor getProductDescriptor(String productType, String version) throws IOException {
        final String resourceName = getResourceName(productType, version);
        // @todo 2 tb/tb add caching mechanism and first check if cached 2024-12-11

        // getResourceAsStream
        final URL resourceUrl = getResourceUrl(productType, resourceName);
        if (resourceUrl == null) {
            return null;
        }

        return readProductDescriptor(resourceUrl);
    }

    private static ProductDescriptor readProductDescriptor(URL resourceUrl) throws IOException {
        ProductDescriptor productDescriptor = null;

        // import json file
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

    private static ObjectMapper getObjectMapper() {
        return ObjectMapperHolder.objectMapper;
    }

    private static JSONParser getParser() {
        return JsonParserHolder.jsonParser;
    }

    private static URL getResourceUrl(String productType, String resourceName) {
        final String dddbResourceName = getDddbResourceName(productType, resourceName);
        final URL resourceUrl = DDDB.class.getClassLoader().getResource(dddbResourceName);

        // @todo 2 tb/tb add check for resource without version string 2024-12-12
        return resourceUrl;
    }

    static String getDddbResourceName(String productType, String resourceName) {
        return DB_RESOURCE_PATH + productType + "/" + resourceName;
    }

    static String getResourceName(String productType, String version) {
        return productType + "_" + version + ".json";
    }

    private static final class InstanceHolder {
        private static final DDDB instance = new DDDB();
    }

    private static class ObjectMapperHolder {
        private static final ObjectMapper objectMapper = new ObjectMapper();
    }

    private static class JsonParserHolder {
        private static final JSONParser jsonParser = new JSONParser();
    }

    // check if resource present
    // if not:
    //  check if resource for type only is present (not version dependent info)
    // load and parse
    // ?? do we need to cache descriptors? Possibly clever and requires not much memory.
}
