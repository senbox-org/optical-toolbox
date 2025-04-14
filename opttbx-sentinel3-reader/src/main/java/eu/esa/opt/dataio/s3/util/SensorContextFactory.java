package eu.esa.opt.dataio.s3.util;

import eu.esa.opt.dataio.s3.olci.OlciContext;

import java.util.HashMap;
import java.util.Map;

public class SensorContextFactory {

    private static final Map<String, SensorContext> contextMap = new HashMap<>();

    public static SensorContext get(String sensorKey) {
        SensorContext sensorContext = contextMap.get(sensorKey);
        if (sensorContext == null) {
            sensorContext = new OlciContext();
            contextMap.put(sensorKey, sensorContext);
        }

        return sensorContext;
    }
}
