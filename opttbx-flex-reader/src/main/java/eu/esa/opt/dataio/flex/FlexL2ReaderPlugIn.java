package eu.esa.opt.dataio.flex;

import java.util.regex.Pattern;

public class FlexL2ReaderPlugIn extends FlexReaderPlugIn {

    private static final String FORMAT_NAME = "FLEX_L2";
    private static final String DESCRIPTION = "FLEX L2 Products (geophysical parameters)";
    private static final Pattern SOURCE_NAME_PATTERN = Pattern.compile(
            "(?i)FLX_L2__FLXSYN_.*"
    );

    public FlexL2ReaderPlugIn() {
        super(FORMAT_NAME, DESCRIPTION, SOURCE_NAME_PATTERN);
    }
}
