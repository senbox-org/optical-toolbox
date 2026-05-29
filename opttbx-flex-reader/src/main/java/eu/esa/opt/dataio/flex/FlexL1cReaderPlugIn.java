package eu.esa.opt.dataio.flex;

import java.util.regex.Pattern;

public class FlexL1cReaderPlugIn extends FlexReaderPlugIn {

    private static final String FORMAT_NAME = "FLEX_L1C";
    private static final String DESCRIPTION = "FLEX L1C Products (geo-registered TOA radiance)";
    private static final Pattern SOURCE_NAME_PATTERN = Pattern.compile(
            "(?i)^FLX_L1C_FLXSYN_\\d{8}T\\d{6}_\\d{8}T\\d{6}_\\d{8}T\\d{6}_\\d{4}_\\d{3}_\\d{3}_\\d{4}_\\d{2}$"
    );

    public FlexL1cReaderPlugIn() {
        super(FORMAT_NAME, DESCRIPTION, SOURCE_NAME_PATTERN);
    }
}
