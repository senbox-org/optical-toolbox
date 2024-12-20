package eu.esa.opt.dataio.s3.util;

import org.esa.snap.core.datamodel.Band;

public class LazyLoadingBand extends Band {

    public LazyLoadingBand(String name, int dataType, int width, int height) {
        super(name, dataType, width, height);
    }
}
