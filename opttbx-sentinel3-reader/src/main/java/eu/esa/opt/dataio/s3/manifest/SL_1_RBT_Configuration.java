package eu.esa.opt.dataio.s3.manifest;

class SL_1_RBT_Configuration implements TypeConfiguration {

    @Override
    public int getRasterWidth() {
        return 3000;
    }

    @Override
    public int getRasterHeight() {
        return 2400;
    }
}
