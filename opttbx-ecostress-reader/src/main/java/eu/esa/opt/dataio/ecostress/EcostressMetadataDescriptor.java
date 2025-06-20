package eu.esa.opt.dataio.ecostress;

public class EcostressMetadataDescriptor extends EcostressMetadata {

    public String formatName;
    public String[] productDataDefinitionsGroups;
    public String[] productMetadataDefinitionsGroups;
    public String remotePlatformName;
    public String productFileNamePattern;
    public String groupingPattern;

    @Override
    public String getFormatName() {
        return formatName;
    }

    @Override
    protected String[] getMetadataElementsPaths() {
        return productMetadataDefinitionsGroups;
    }

    @Override
    protected String[] getBandsElementsPaths() {
        return productDataDefinitionsGroups;
    }

    @Override
    protected String getRemotePlatformName() {
        return remotePlatformName;
    }

    @Override
    protected String getProductFileNameRegex() {
        return productFileNamePattern;
    }

    @Override
    protected String getGroupingPattern() {
        return groupingPattern;
    }

}
