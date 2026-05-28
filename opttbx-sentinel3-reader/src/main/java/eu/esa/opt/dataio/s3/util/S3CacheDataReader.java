package eu.esa.opt.dataio.s3.util;

import eu.esa.snap.core.dataio.cache.DataBuffer;

import java.io.IOException;

public interface S3CacheDataReader {

    void readCacheData(String cacheKey, int[] offsets, int[] shapes, DataBuffer targetBuffer) throws IOException;
}
