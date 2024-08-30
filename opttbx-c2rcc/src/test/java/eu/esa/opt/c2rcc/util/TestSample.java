package eu.esa.opt.c2rcc.util;

import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.gpf.pointop.Sample;

public class TestSample implements Sample {

    private double sample;

    public TestSample(double value) {
        this.sample = value;
    }

    @Override
    public RasterDataNode getNode() {
        return null;
    }

    @Override
    public int getIndex() {
        return 0;
    }

    @Override
    public int getDataType() {
        return 0;
    }

    @Override
    public boolean getBit(int bitIndex) {
        return false;
    }

    @Override
    public boolean getBoolean() {
        return false;
    }

    @Override
    public int getInt() {
        return 0;
    }

    @Override
    public float getFloat() {
        return 0;
    }

    @Override
    public double getDouble() {
        return this.sample;
    }
}
