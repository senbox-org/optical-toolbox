package eu.esa.opt.dataio.s3.olci;

import ucar.ma2.Array;
import ucar.nc2.Variable;

import java.io.IOException;

public interface ReaderContext {

    Array readData(String name, Variable variable) throws IOException;
    Array readData(String name) throws IOException;

    boolean hasData(String name);
    void ingestToCache(String name, Array data);
}
