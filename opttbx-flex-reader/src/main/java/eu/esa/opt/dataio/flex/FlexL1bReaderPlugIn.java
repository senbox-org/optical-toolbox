package eu.esa.opt.dataio.flex;

import java.util.regex.Pattern;

public class FlexL1bReaderPlugIn extends FlexReaderPlugIn {

    private static final String FORMAT_NAME = "FLEX_L1B";
    private static final String DESCRIPTION = "FLEX L1B Products (FLORIS TOA radiance)";
    private static final Pattern SOURCE_NAME_PATTERN = Pattern.compile(
            "(?i)FLX_L1B_OBS____.*"
    );

    public FlexL1bReaderPlugIn() {
        super(FORMAT_NAME, DESCRIPTION, SOURCE_NAME_PATTERN);
    }
}
