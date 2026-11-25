package eu.esa.opt.dataio.s2;

public class GridPair {
    private String key;
    private String name;

    public GridPair(String key, String name) {
        this.key = key;
        this.name = name;
    }

    protected final String getKey() {
        return key;
    }

    protected final String getName() {
        return name;
    }

}
