package eu.esa.opt.dataio.enmap;

public enum ProductFormat {

    BSQ_Metadata, // BSQ+Metadata
    BIL_Metadata, // BIL+Metadata
    BIP_Metadata, // BIP+Metadata
    JPEG2000_Metadata, // JPEG2000+Metadata
    GeoTIFF_Metadata; // GeoTIFF+Metadata

    public static String toEnumName(String formatName) {
        return formatName.replace("+", "_");
    }

    public String asEnmapFormatName() {
        return name().replace("_", "+");
    }
}
