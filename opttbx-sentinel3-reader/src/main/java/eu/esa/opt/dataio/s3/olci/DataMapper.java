package eu.esa.opt.dataio.s3.olci;

public class DataMapper {

    private static final int INVALID_SENSOR = -1;
    private static final float TARGET_FILL_VALUE = -1.f;

    public void mapData(float[] instrumentData, float[][] targetBuffer, short[][] detectorIndices) {
        for (int i = 0; i < detectorIndices.length; i++) {
            final short[] detectorIndexLine = detectorIndices[i];
            final float[] targetLine = targetBuffer[i];
            mapScanLine(instrumentData, targetLine, detectorIndexLine);
        }
    }

    // package access for testing only tb 2025-04-17
    void mapScanLine(float[] instrumentData, float[] targetBuffer, short[] detectorIndices) {
        for (short i = 0; i < detectorIndices.length; i++) {
            final short sensorIndex = detectorIndices[i];
            if (sensorIndex == INVALID_SENSOR) {
                targetBuffer[i] = TARGET_FILL_VALUE;
                continue;
            }

            targetBuffer[i] = instrumentData[sensorIndex];
        }
    }
}
