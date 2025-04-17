package eu.esa.opt.dataio.s3.olci;

import ucar.ma2.Array;
import ucar.nc2.Variable;

public interface ReaderContext {

    Array readData(String name, Variable variable);
}
