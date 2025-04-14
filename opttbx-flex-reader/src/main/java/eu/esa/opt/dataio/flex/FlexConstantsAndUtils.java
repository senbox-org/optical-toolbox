package eu.esa.opt.dataio.flex;

import java.util.regex.Pattern;

public class FlexConstantsAndUtils {

    static final String FORMAT_NAME = "FLEX";
    static final String DESCRIPTION = "FLEX products";
    static final String FLEX_EXTENSION = ".xml";
    // TODO: get filename convention from product specification
    static final String FLEX_FILENAME_REGEX = "FLX_GPP__L1B_OBS____\\d{8}T\\d{6}_\\d{8}T\\d{6}_\\d{8}T\\d{6}__\\d+.xml";
    static final Pattern FLEX_FILENAME_PATTERN = Pattern.compile(FLEX_FILENAME_REGEX, Pattern.CASE_INSENSITIVE);
}
