package gov.nasa.gsfc.seadas.dataio;

import org.esa.snap.core.datamodel.Mask;
import org.esa.snap.core.jexp.impl.AbstractSymbol;

import java.awt.*;

public class SeadasReaderDefaults {


    final static Color LandBrown = new Color(80, 60, 0);
    final static Color LandDarkGreenBrown = new Color(55, 55, 0);
    final static Color LightBrown = new Color(137, 99, 31);
    final static Color FailRed = new Color(255, 0, 26);
    final static Color DeepBlue = new Color(0, 16, 143);
    final static Color BrightPink = new Color(255, 61, 245);
    final static Color LightCyan = new Color(193, 255, 254);
    final static Color NewGreen = new Color(132, 199, 101);
    final static Color Mustard = new Color(206, 204, 70);
    final static Color MediumGray = new Color(160, 160, 160);
    final static Color Purple = new Color(141, 11, 134);
    final static Color Coral = new Color(255, 0, 95);
    final static Color DarkGreen = new Color(0, 101, 28);
    final static Color TealGreen = new Color(0, 80, 79);
    final static Color LightPink = new Color(255, 208, 241);
    final static Color LightPurple = new Color(191, 143, 247);
    final static Color BurntUmber = new Color(165, 0, 11);
    final static Color TealBlue = new Color(0, 103, 144);
    final static Color Cornflower = new Color(38, 115, 245);

    final static Color Gray240 = new Color(240, 240, 240);
    final static Color Gray225 = new Color(225, 225, 225);
    final static Color Gray200 = new Color(200, 200, 200);
    final static Color Gray175 = new Color(175, 175, 175);
    final static Color Gray150 = new Color(150, 150, 150);
    final static Color Gray125 = new Color(125, 125, 125);
    final static Color Gray100 = new Color(100, 100, 100);
    final static Color Gray75 = new Color(75, 75, 75);

    final static Color Warnings = Gray100;
    final static Color Failures = Gray75;
    final static Color BrightFlags = Gray225;
    //    final static Color MiscFlags = Gray150;
    final static Color MiscFlags = Purple;






    // Preferences property prefix
    private static final String PROPERTY_SEADAS_READER_ROOT_KEY = "opt.toolbox.seadas.reader";




    // ATMFAIL

    public static final String PROPERTY_MASK_ATMFAIL_NAME = "ATMFAIL";

    public static final String PROPERTY_MASK_ATMFAIL_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.ATMFAIL";

    public static final String PROPERTY_MASK_ATMFAIL_SECTION_KEY = PROPERTY_MASK_ATMFAIL_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_ATMFAIL_SECTION_LABEL = "L2 Flag Mask: ATMFAIL";
    public static final String PROPERTY_MASK_ATMFAIL_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask ATMFAIL";

    public static final String PROPERTY_MASK_ATMFAIL_ENABLED_KEY = PROPERTY_MASK_ATMFAIL_ROOT_KEY + ".selected";
    public static final String PROPERTY_MASK_ATMFAIL_ENABLED_LABEL = "ATMFAIL: Set as Selected";
    public static final String PROPERTY_MASK_ATMFAIL_ENABLED_TOOLTIP = "Set ATMFAIL mask as selected by default";
    public static final boolean PROPERTY_MASK_ATMFAIL_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_ATMFAIL_TRANSPARENCY_KEY = PROPERTY_MASK_ATMFAIL_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_ATMFAIL_TRANSPARENCY_LABEL = "ATMFAIL: Transparency";
    public static final String PROPERTY_MASK_ATMFAIL_TRANSPARENCY_TOOLTIP = "Set default transparency of the ATMFAIL mask";
    public static final double PROPERTY_MASK_ATMFAIL_TRANSPARENCY_DEFAULT = 0.0;

    public static final String PROPERTY_MASK_ATMFAIL_COLOR_KEY = PROPERTY_MASK_ATMFAIL_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_ATMFAIL_COLOR_LABEL = "ATMFAIL: Color";
    public static final String PROPERTY_MASK_ATMFAIL_COLOR_TOOLTIP = "Set default color of the ATMFAIL mask";
    //    public static final Color PROPERTY_MASK_ATMFAIL_COLOR_DEFAULT = Failures;
    public static final Color PROPERTY_MASK_ATMFAIL_COLOR_DEFAULT = FailRed;





    // LAND


    public static final String PROPERTY_MASK_LAND_NAME = "LAND";

    public static final String PROPERTY_MASK_LAND_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.LAND";

    public static final String PROPERTY_MASK_LAND_SECTION_KEY = PROPERTY_MASK_LAND_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_LAND_SECTION_LABEL = "L2 Flag Mask: LAND";
    public static final String PROPERTY_MASK_LAND_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask LAND";

    public static final String PROPERTY_MASK_LAND_ENABLED_KEY = PROPERTY_MASK_LAND_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_LAND_ENABLED_LABEL = "LAND: Set as Selected";
    public static final String PROPERTY_MASK_LAND_ENABLED_TOOLTIP = "Set LAND mask as selected by default";
    public static final boolean PROPERTY_MASK_LAND_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_LAND_TRANSPARENCY_KEY = PROPERTY_MASK_LAND_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_LAND_TRANSPARENCY_LABEL = "LAND: Transparency";
    public static final String PROPERTY_MASK_LAND_TRANSPARENCY_TOOLTIP = "Set default transparency of the LAND mask";
    public static final double PROPERTY_MASK_LAND_TRANSPARENCY_DEFAULT = 0.0;

    public static final String PROPERTY_MASK_LAND_COLOR_KEY = PROPERTY_MASK_LAND_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_LAND_COLOR_LABEL = "LAND: Color";
    public static final String PROPERTY_MASK_LAND_COLOR_TOOLTIP = "Set default color of the LAND mask";
    //    public static final Color PROPERTY_MASK_LAND_COLOR_DEFAULT = LandDarkGreenBrown;
    public static final Color PROPERTY_MASK_LAND_COLOR_DEFAULT = LandBrown;




    // PRODWARN

    public static final String PROPERTY_MASK_PRODWARN_NAME = "PRODWARN";

    public static final String PROPERTY_MASK_PRODWARN_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.PRODWARN";

    public static final String PROPERTY_MASK_PRODWARN_SECTION_KEY = PROPERTY_MASK_PRODWARN_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_PRODWARN_SECTION_LABEL = "L2 Flag Mask: PRODWARN";
    public static final String PROPERTY_MASK_PRODWARN_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask PRODWARN";

    public static final String PROPERTY_MASK_PRODWARN_ENABLED_KEY = PROPERTY_MASK_PRODWARN_ROOT_KEY + ".selected";
    public static final String PROPERTY_MASK_PRODWARN_ENABLED_LABEL = "PRODWARN: Set as Selected";
    public static final String PROPERTY_MASK_PRODWARN_ENABLED_TOOLTIP = "Set PRODWARN mask as selected by default";
    public static final boolean PROPERTY_MASK_PRODWARN_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_PRODWARN_TRANSPARENCY_KEY = PROPERTY_MASK_PRODWARN_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_PRODWARN_TRANSPARENCY_LABEL = "PRODWARN: Transparency";
    public static final String PROPERTY_MASK_PRODWARN_TRANSPARENCY_TOOLTIP = "Set default transparency of the PRODWARN mask";
    public static final double PROPERTY_MASK_PRODWARN_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_PRODWARN_COLOR_KEY = PROPERTY_MASK_PRODWARN_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_PRODWARN_COLOR_LABEL = "PRODWARN: Color";
    public static final String PROPERTY_MASK_PRODWARN_COLOR_TOOLTIP = "Set default color of the PRODWARN mask";
    //    public static final Color PROPERTY_MASK_PRODWARN_COLOR_DEFAULT = Warnings;
    public static final Color PROPERTY_MASK_PRODWARN_COLOR_DEFAULT = DeepBlue;



    // HIGLINT

    public static final String PROPERTY_MASK_HIGLINT_NAME = "HIGLINT";

    public static final String PROPERTY_MASK_HIGLINT_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.HIGLINT";

    public static final String PROPERTY_MASK_HIGLINT_SECTION_KEY = PROPERTY_MASK_HIGLINT_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_HIGLINT_SECTION_LABEL = "L2 Flag Mask: HIGLINT";
    public static final String PROPERTY_MASK_HIGLINT_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask HIGLINT";

    public static final String PROPERTY_MASK_HIGLINT_ENABLED_KEY = PROPERTY_MASK_HIGLINT_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_HIGLINT_ENABLED_LABEL = "HIGLINT: Set as Selected";
    public static final String PROPERTY_MASK_HIGLINT_ENABLED_TOOLTIP = "Set HIGLINT mask as selected by default";
    public static final boolean PROPERTY_MASK_HIGLINT_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_HIGLINT_TRANSPARENCY_KEY = PROPERTY_MASK_HIGLINT_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_HIGLINT_TRANSPARENCY_LABEL = "HIGLINT: Transparency";
    public static final String PROPERTY_MASK_HIGLINT_TRANSPARENCY_TOOLTIP = "Set default transparency of the HIGLINT mask";
    public static final double PROPERTY_MASK_HIGLINT_TRANSPARENCY_DEFAULT = 0.2;

    public static final String PROPERTY_MASK_HIGLINT_COLOR_KEY = PROPERTY_MASK_HIGLINT_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_HIGLINT_COLOR_LABEL = "HIGLINT: Color";
    public static final String PROPERTY_MASK_HIGLINT_COLOR_TOOLTIP = "Set default color of the HIGLINT mask";
    //    public static final Color PROPERTY_MASK_HIGLINT_COLOR_DEFAULT = BrightFlags;
    public static final Color PROPERTY_MASK_HIGLINT_COLOR_DEFAULT = BrightPink;



    // HILT

    public static final String PROPERTY_MASK_HILT_NAME = "HILT";

    public static final String PROPERTY_MASK_HILT_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.HILT";

    public static final String PROPERTY_MASK_HILT_SECTION_KEY = PROPERTY_MASK_HILT_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_HILT_SECTION_LABEL = "L2 Flag Mask: HILT";
    public static final String PROPERTY_MASK_HILT_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask HILT";

    public static final String PROPERTY_MASK_HILT_ENABLED_KEY = PROPERTY_MASK_HILT_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_HILT_ENABLED_LABEL = "HILT: Set as Selected";
    public static final String PROPERTY_MASK_HILT_ENABLED_TOOLTIP = "Set HILT mask as selected by default";
    public static final boolean PROPERTY_MASK_HILT_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_HILT_TRANSPARENCY_KEY = PROPERTY_MASK_HILT_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_HILT_TRANSPARENCY_LABEL = "HILT: Transparency";
    public static final String PROPERTY_MASK_HILT_TRANSPARENCY_TOOLTIP = "Set default transparency of the HILT mask";
    public static final double PROPERTY_MASK_HILT_TRANSPARENCY_DEFAULT = 0.2;

    public static final String PROPERTY_MASK_HILT_COLOR_KEY = PROPERTY_MASK_HILT_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_HILT_COLOR_LABEL = "HILT: Color";
    public static final String PROPERTY_MASK_HILT_COLOR_TOOLTIP = "Set default color of the HILT mask";
    //    public static final Color PROPERTY_MASK_HILT_COLOR_DEFAULT = BrightFlags;
    public static final Color PROPERTY_MASK_HILT_COLOR_DEFAULT =  Color.GRAY;




    // HISATZEN

    public static final String PROPERTY_MASK_HISATZEN_NAME = "HISATZEN";

    public static final String PROPERTY_MASK_HISATZEN_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.HISATZEN";

    public static final String PROPERTY_MASK_HISATZEN_SECTION_KEY = PROPERTY_MASK_HISATZEN_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_HISATZEN_SECTION_LABEL = "L2 Flag Mask: HISATZEN";
    public static final String PROPERTY_MASK_HISATZEN_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask HISATZEN";

    public static final String PROPERTY_MASK_HISATZEN_ENABLED_KEY = PROPERTY_MASK_HISATZEN_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_HISATZEN_ENABLED_LABEL = "HISATZEN: Set as Selected";
    public static final String PROPERTY_MASK_HISATZEN_ENABLED_TOOLTIP = "Set HISATZEN mask as selected by default";
    public static final boolean PROPERTY_MASK_HISATZEN_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_HISATZEN_TRANSPARENCY_KEY = PROPERTY_MASK_HISATZEN_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_HISATZEN_TRANSPARENCY_LABEL = "HISATZEN: Transparency";
    public static final String PROPERTY_MASK_HISATZEN_TRANSPARENCY_TOOLTIP = "Set default transparency of the HISATZEN mask";
    public static final double PROPERTY_MASK_HISATZEN_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_HISATZEN_COLOR_KEY = PROPERTY_MASK_HISATZEN_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_HISATZEN_COLOR_LABEL = "HISATZEN: Color";
    public static final String PROPERTY_MASK_HISATZEN_COLOR_TOOLTIP = "Set default color of the HISATZEN mask";
    //    public static final Color PROPERTY_MASK_HISATZEN_COLOR_DEFAULT = MiscFlags;
    public static final Color PROPERTY_MASK_HISATZEN_COLOR_DEFAULT = LightCyan;



    // COASTZ

    public static final String PROPERTY_MASK_COASTZ_NAME = "COASTZ";

    public static final String PROPERTY_MASK_COASTZ_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.COASTZ";

    public static final String PROPERTY_MASK_COASTZ_SECTION_KEY = PROPERTY_MASK_COASTZ_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_COASTZ_SECTION_LABEL = "L2 Flag Mask: COASTZ";
    public static final String PROPERTY_MASK_COASTZ_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask COASTZ";

    public static final String PROPERTY_MASK_COASTZ_ENABLED_KEY = PROPERTY_MASK_COASTZ_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_COASTZ_ENABLED_LABEL = "COASTZ: Set as Selected";
    public static final String PROPERTY_MASK_COASTZ_ENABLED_TOOLTIP = "Set COASTZ mask as selected by default";
    public static final boolean PROPERTY_MASK_COASTZ_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_COASTZ_TRANSPARENCY_KEY = PROPERTY_MASK_COASTZ_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_COASTZ_TRANSPARENCY_LABEL = "COASTZ: Transparency";
    public static final String PROPERTY_MASK_COASTZ_TRANSPARENCY_TOOLTIP = "Set default transparency of the COASTZ mask";
    public static final double PROPERTY_MASK_COASTZ_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_COASTZ_COLOR_KEY = PROPERTY_MASK_COASTZ_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_COASTZ_COLOR_LABEL = "COASTZ: Color";
    public static final String PROPERTY_MASK_COASTZ_COLOR_TOOLTIP = "Set default color of the COASTZ mask";
    //    public static final Color PROPERTY_MASK_COASTZ_COLOR_DEFAULT = MiscFlags;
    public static final Color PROPERTY_MASK_COASTZ_COLOR_DEFAULT = BurntUmber;




    // STRAYLIGHT

    public static final String PROPERTY_MASK_STRAYLIGHT_NAME = "STRAYLIGHT";

    public static final String PROPERTY_MASK_STRAYLIGHT_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.STRAYLIGHT";

    public static final String PROPERTY_MASK_STRAYLIGHT_SECTION_KEY = PROPERTY_MASK_STRAYLIGHT_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_STRAYLIGHT_SECTION_LABEL = "L2 Flag Mask: STRAYLIGHT";
    public static final String PROPERTY_MASK_STRAYLIGHT_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask STRAYLIGHT";

    public static final String PROPERTY_MASK_STRAYLIGHT_ENABLED_KEY = PROPERTY_MASK_STRAYLIGHT_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_STRAYLIGHT_ENABLED_LABEL = "STRAYLIGHT: Set as Selected";
    public static final String PROPERTY_MASK_STRAYLIGHT_ENABLED_TOOLTIP = "Set STRAYLIGHT mask as selected by default";
    public static final boolean PROPERTY_MASK_STRAYLIGHT_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_STRAYLIGHT_TRANSPARENCY_KEY = PROPERTY_MASK_STRAYLIGHT_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_STRAYLIGHT_TRANSPARENCY_LABEL = "STRAYLIGHT: Transparency";
    public static final String PROPERTY_MASK_STRAYLIGHT_TRANSPARENCY_TOOLTIP = "Set default transparency of the STRAYLIGHT mask";
    public static final double PROPERTY_MASK_STRAYLIGHT_TRANSPARENCY_DEFAULT = 0.2;

    public static final String PROPERTY_MASK_STRAYLIGHT_COLOR_KEY = PROPERTY_MASK_STRAYLIGHT_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_STRAYLIGHT_COLOR_LABEL = "STRAYLIGHT: Color";
    public static final String PROPERTY_MASK_STRAYLIGHT_COLOR_TOOLTIP = "Set default color of the STRAYLIGHT mask";
    //    public static final Color PROPERTY_MASK_STRAYLIGHT_COLOR_DEFAULT = BrightFlags;
    public static final Color PROPERTY_MASK_STRAYLIGHT_COLOR_DEFAULT = Color.YELLOW;



    // CLDICE

    public static final String PROPERTY_MASK_CLDICE_NAME = "CLDICE";

    public static final String PROPERTY_MASK_CLDICE_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.CLDICE";

    public static final String PROPERTY_MASK_CLDICE_SECTION_KEY = PROPERTY_MASK_CLDICE_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_CLDICE_SECTION_LABEL = "L2 Flag Mask: CLDICE";
    public static final String PROPERTY_MASK_CLDICE_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask CLDICE";

    public static final String PROPERTY_MASK_CLDICE_ENABLED_KEY = PROPERTY_MASK_CLDICE_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_CLDICE_ENABLED_LABEL = "CLDICE: Set as Selected";
    public static final String PROPERTY_MASK_CLDICE_ENABLED_TOOLTIP = "Set CLDICE mask as selected by default";
    public static final boolean PROPERTY_MASK_CLDICE_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_CLDICE_TRANSPARENCY_KEY = PROPERTY_MASK_CLDICE_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_CLDICE_TRANSPARENCY_LABEL = "CLDICE: Transparency";
    public static final String PROPERTY_MASK_CLDICE_TRANSPARENCY_TOOLTIP = "Set default transparency of the CLDICE mask";
    public static final double PROPERTY_MASK_CLDICE_TRANSPARENCY_DEFAULT = 0.0;

    public static final String PROPERTY_MASK_CLDICE_COLOR_KEY = PROPERTY_MASK_CLDICE_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_CLDICE_COLOR_LABEL = "CLDICE: Color";
    public static final String PROPERTY_MASK_CLDICE_COLOR_TOOLTIP = "Set default color of the CLDICE mask";
    //    public static final Color PROPERTY_MASK_CLDICE_COLOR_DEFAULT = Gray240;
    public static final Color PROPERTY_MASK_CLDICE_COLOR_DEFAULT = Color.WHITE;




    // COCCOLITH

    public static final String PROPERTY_MASK_COCCOLITH_NAME = "COCCOLITH";

    public static final String PROPERTY_MASK_COCCOLITH_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.COCCOLITH";

    public static final String PROPERTY_MASK_COCCOLITH_SECTION_KEY = PROPERTY_MASK_COCCOLITH_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_COCCOLITH_SECTION_LABEL = "L2 Flag Mask: COCCOLITH";
    public static final String PROPERTY_MASK_COCCOLITH_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask COCCOLITH";

    public static final String PROPERTY_MASK_COCCOLITH_ENABLED_KEY = PROPERTY_MASK_COCCOLITH_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_COCCOLITH_ENABLED_LABEL = "COCCOLITH: Set as Selected";
    public static final String PROPERTY_MASK_COCCOLITH_ENABLED_TOOLTIP = "Set COCCOLITH mask as selected by default";
    public static final boolean PROPERTY_MASK_COCCOLITH_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_COCCOLITH_TRANSPARENCY_KEY = PROPERTY_MASK_COCCOLITH_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_COCCOLITH_TRANSPARENCY_LABEL = "COCCOLITH: Transparency";
    public static final String PROPERTY_MASK_COCCOLITH_TRANSPARENCY_TOOLTIP = "Set default transparency of the COCCOLITH mask";
    public static final double PROPERTY_MASK_COCCOLITH_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_COCCOLITH_COLOR_KEY = PROPERTY_MASK_COCCOLITH_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_COCCOLITH_COLOR_LABEL = "COCCOLITH: Color";
    public static final String PROPERTY_MASK_COCCOLITH_COLOR_TOOLTIP = "Set default color of the COCCOLITH mask";
    //    public static final Color PROPERTY_MASK_COCCOLITH_COLOR_DEFAULT = MiscFlags;
    public static final Color PROPERTY_MASK_COCCOLITH_COLOR_DEFAULT = Color.CYAN;



    // TURBIDW

    public static final String PROPERTY_MASK_TURBIDW_NAME = "TURBIDW";

    public static final String PROPERTY_MASK_TURBIDW_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.TURBIDW";

    public static final String PROPERTY_MASK_TURBIDW_SECTION_KEY = PROPERTY_MASK_TURBIDW_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_TURBIDW_SECTION_LABEL = "L2 Flag Mask: TURBIDW";
    public static final String PROPERTY_MASK_TURBIDW_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask TURBIDW";

    public static final String PROPERTY_MASK_TURBIDW_ENABLED_KEY = PROPERTY_MASK_TURBIDW_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_TURBIDW_ENABLED_LABEL = "TURBIDW: Set as Selected";
    public static final String PROPERTY_MASK_TURBIDW_ENABLED_TOOLTIP = "Set TURBIDW mask as selected by default";
    public static final boolean PROPERTY_MASK_TURBIDW_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_TURBIDW_TRANSPARENCY_KEY = PROPERTY_MASK_TURBIDW_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_TURBIDW_TRANSPARENCY_LABEL = "TURBIDW: Transparency";
    public static final String PROPERTY_MASK_TURBIDW_TRANSPARENCY_TOOLTIP = "Set default transparency of the TURBIDW mask";
    public static final double PROPERTY_MASK_TURBIDW_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_TURBIDW_COLOR_KEY = PROPERTY_MASK_TURBIDW_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_TURBIDW_COLOR_LABEL = "TURBIDW: Color";
    public static final String PROPERTY_MASK_TURBIDW_COLOR_TOOLTIP = "Set default color of the TURBIDW mask";
    //    public static final Color PROPERTY_MASK_TURBIDW_COLOR_DEFAULT = MiscFlags;
    public static final Color PROPERTY_MASK_TURBIDW_COLOR_DEFAULT = LightBrown;



    // HISOLZEN

    public static final String PROPERTY_MASK_HISOLZEN_NAME = "HISOLZEN";

    public static final String PROPERTY_MASK_HISOLZEN_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.HISOLZEN";

    public static final String PROPERTY_MASK_HISOLZEN_SECTION_KEY = PROPERTY_MASK_HISOLZEN_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_HISOLZEN_SECTION_LABEL = "L2 Flag Mask: HISOLZEN";
    public static final String PROPERTY_MASK_HISOLZEN_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask HISOLZEN";

    public static final String PROPERTY_MASK_HISOLZEN_ENABLED_KEY = PROPERTY_MASK_HISOLZEN_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_HISOLZEN_ENABLED_LABEL = "HISOLZEN: Set as Selected";
    public static final String PROPERTY_MASK_HISOLZEN_ENABLED_TOOLTIP = "Set HISOLZEN mask as selected by default";
    public static final boolean PROPERTY_MASK_HISOLZEN_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_HISOLZEN_TRANSPARENCY_KEY = PROPERTY_MASK_HISOLZEN_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_HISOLZEN_TRANSPARENCY_LABEL = "HISOLZEN: Transparency";
    public static final String PROPERTY_MASK_HISOLZEN_TRANSPARENCY_TOOLTIP = "Set default transparency of the HISOLZEN mask";
    public static final double PROPERTY_MASK_HISOLZEN_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_HISOLZEN_COLOR_KEY = PROPERTY_MASK_HISOLZEN_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_HISOLZEN_COLOR_LABEL = "HISOLZEN: Color";
    public static final String PROPERTY_MASK_HISOLZEN_COLOR_TOOLTIP = "Set default color of the HISOLZEN mask";
    //    public static final Color PROPERTY_MASK_HISOLZEN_COLOR_DEFAULT = MiscFlags;
    public static final Color PROPERTY_MASK_HISOLZEN_COLOR_DEFAULT = Purple;



    // LOWLW

    public static final String PROPERTY_MASK_LOWLW_NAME = "LOWLW";

    public static final String PROPERTY_MASK_LOWLW_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.LOWLW";

    public static final String PROPERTY_MASK_LOWLW_SECTION_KEY = PROPERTY_MASK_LOWLW_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_LOWLW_SECTION_LABEL = "L2 Flag Mask: LOWLW";
    public static final String PROPERTY_MASK_LOWLW_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask LOWLW";

    public static final String PROPERTY_MASK_LOWLW_ENABLED_KEY = PROPERTY_MASK_LOWLW_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_LOWLW_ENABLED_LABEL = "LOWLW: Set as Selected";
    public static final String PROPERTY_MASK_LOWLW_ENABLED_TOOLTIP = "Set LOWLW mask as selected by default";
    public static final boolean PROPERTY_MASK_LOWLW_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_LOWLW_TRANSPARENCY_KEY = PROPERTY_MASK_LOWLW_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_LOWLW_TRANSPARENCY_LABEL = "LOWLW: Transparency";
    public static final String PROPERTY_MASK_LOWLW_TRANSPARENCY_TOOLTIP = "Set default transparency of the LOWLW mask";
    public static final double PROPERTY_MASK_LOWLW_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_LOWLW_COLOR_KEY = PROPERTY_MASK_LOWLW_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_LOWLW_COLOR_LABEL = "LOWLW: Color";
    public static final String PROPERTY_MASK_LOWLW_COLOR_TOOLTIP = "Set default color of the LOWLW mask";
    //    public static final Color PROPERTY_MASK_LOWLW_COLOR_DEFAULT = Gray75;
    public static final Color PROPERTY_MASK_LOWLW_COLOR_DEFAULT = Cornflower;



    // CHLFAIL

    public static final String PROPERTY_MASK_CHLFAIL_NAME = "CHLFAIL";

    public static final String PROPERTY_MASK_CHLFAIL_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.CHLFAIL";

    public static final String PROPERTY_MASK_CHLFAIL_SECTION_KEY = PROPERTY_MASK_CHLFAIL_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_CHLFAIL_SECTION_LABEL = "L2 Flag Mask: CHLFAIL";
    public static final String PROPERTY_MASK_CHLFAIL_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask CHLFAIL";

    public static final String PROPERTY_MASK_CHLFAIL_ENABLED_KEY = PROPERTY_MASK_CHLFAIL_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_CHLFAIL_ENABLED_LABEL = "CHLFAIL: Set as Selected";
    public static final String PROPERTY_MASK_CHLFAIL_ENABLED_TOOLTIP = "Set CHLFAIL mask as selected by default";
    public static final boolean PROPERTY_MASK_CHLFAIL_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_CHLFAIL_TRANSPARENCY_KEY = PROPERTY_MASK_CHLFAIL_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_CHLFAIL_TRANSPARENCY_LABEL = "CHLFAIL: Transparency";
    public static final String PROPERTY_MASK_CHLFAIL_TRANSPARENCY_TOOLTIP = "Set default transparency of the CHLFAIL mask";
    public static final double PROPERTY_MASK_CHLFAIL_TRANSPARENCY_DEFAULT = 0.0;

    public static final String PROPERTY_MASK_CHLFAIL_COLOR_KEY = PROPERTY_MASK_CHLFAIL_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_CHLFAIL_COLOR_LABEL = "CHLFAIL: Color";
    public static final String PROPERTY_MASK_CHLFAIL_COLOR_TOOLTIP = "Set default color of the CHLFAIL mask";
    //    public static final Color PROPERTY_MASK_CHLFAIL_COLOR_DEFAULT = Failures;
    public static final Color PROPERTY_MASK_CHLFAIL_COLOR_DEFAULT = FailRed;



    // NAVWARN

    public static final String PROPERTY_MASK_NAVWARN_NAME = "NAVWARN";

    public static final String PROPERTY_MASK_NAVWARN_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.NAVWARN";

    public static final String PROPERTY_MASK_NAVWARN_SECTION_KEY = PROPERTY_MASK_NAVWARN_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_NAVWARN_SECTION_LABEL = "L2 Flag Mask: NAVWARN";
    public static final String PROPERTY_MASK_NAVWARN_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask NAVWARN";

    public static final String PROPERTY_MASK_NAVWARN_ENABLED_KEY = PROPERTY_MASK_NAVWARN_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_NAVWARN_ENABLED_LABEL = "NAVWARN: Set as Selected";
    public static final String PROPERTY_MASK_NAVWARN_ENABLED_TOOLTIP = "Set NAVWARN mask as selected by default";
    public static final boolean PROPERTY_MASK_NAVWARN_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_NAVWARN_TRANSPARENCY_KEY = PROPERTY_MASK_NAVWARN_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_NAVWARN_TRANSPARENCY_LABEL = "NAVWARN: Transparency";
    public static final String PROPERTY_MASK_NAVWARN_TRANSPARENCY_TOOLTIP = "Set default transparency of the NAVWARN mask";
    public static final double PROPERTY_MASK_NAVWARN_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_NAVWARN_COLOR_KEY = PROPERTY_MASK_NAVWARN_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_NAVWARN_COLOR_LABEL = "NAVWARN: Color";
    public static final String PROPERTY_MASK_NAVWARN_COLOR_TOOLTIP = "Set default color of the NAVWARN mask";
    //    public static final Color PROPERTY_MASK_NAVWARN_COLOR_DEFAULT = Warnings;
    public static final Color PROPERTY_MASK_NAVWARN_COLOR_DEFAULT = Color.MAGENTA;




    // ABSAER

    public static final String PROPERTY_MASK_ABSAER_NAME = "ABSAER";

    public static final String PROPERTY_MASK_ABSAER_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.ABSAER";

    public static final String PROPERTY_MASK_ABSAER_SECTION_KEY = PROPERTY_MASK_ABSAER_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_ABSAER_SECTION_LABEL = "L2 Flag Mask: ABSAER";
    public static final String PROPERTY_MASK_ABSAER_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask ABSAER";

    public static final String PROPERTY_MASK_ABSAER_ENABLED_KEY = PROPERTY_MASK_ABSAER_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_ABSAER_ENABLED_LABEL = "ABSAER: Set as Selected";
    public static final String PROPERTY_MASK_ABSAER_ENABLED_TOOLTIP = "Set ABSAER mask as selected by default";
    public static final boolean PROPERTY_MASK_ABSAER_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_ABSAER_TRANSPARENCY_KEY = PROPERTY_MASK_ABSAER_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_ABSAER_TRANSPARENCY_LABEL = "ABSAER: Transparency";
    public static final String PROPERTY_MASK_ABSAER_TRANSPARENCY_TOOLTIP = "Set default transparency of the ABSAER mask";
    public static final double PROPERTY_MASK_ABSAER_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_ABSAER_COLOR_KEY = PROPERTY_MASK_ABSAER_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_ABSAER_COLOR_LABEL = "ABSAER: Color";
    public static final String PROPERTY_MASK_ABSAER_COLOR_TOOLTIP = "Set default color of the ABSAER mask";
    //    public static final Color PROPERTY_MASK_ABSAER_COLOR_DEFAULT = MiscFlags;
    public static final Color PROPERTY_MASK_ABSAER_COLOR_DEFAULT = Color.ORANGE;




    // MAXAERITER

    public static final String PROPERTY_MASK_MAXAERITER_NAME = "MAXAERITER";

    public static final String PROPERTY_MASK_MAXAERITER_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.MAXAERITER";

    public static final String PROPERTY_MASK_MAXAERITER_SECTION_KEY = PROPERTY_MASK_MAXAERITER_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_MAXAERITER_SECTION_LABEL = "L2 Flag Mask: MAXAERITER";
    public static final String PROPERTY_MASK_MAXAERITER_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask MAXAERITER";

    public static final String PROPERTY_MASK_MAXAERITER_ENABLED_KEY = PROPERTY_MASK_MAXAERITER_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_MAXAERITER_ENABLED_LABEL = "MAXAERITER: Set as Selected";
    public static final String PROPERTY_MASK_MAXAERITER_ENABLED_TOOLTIP = "Set MAXAERITER mask as selected by default";
    public static final boolean PROPERTY_MASK_MAXAERITER_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_MAXAERITER_TRANSPARENCY_KEY = PROPERTY_MASK_MAXAERITER_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_MAXAERITER_TRANSPARENCY_LABEL = "MAXAERITER: Transparency";
    public static final String PROPERTY_MASK_MAXAERITER_TRANSPARENCY_TOOLTIP = "Set default transparency of the MAXAERITER mask";
    public static final double PROPERTY_MASK_MAXAERITER_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_MAXAERITER_COLOR_KEY = PROPERTY_MASK_MAXAERITER_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_MAXAERITER_COLOR_LABEL = "MAXAERITER: Color";
    public static final String PROPERTY_MASK_MAXAERITER_COLOR_TOOLTIP = "Set default color of the MAXAERITER mask";
    //    public static final Color PROPERTY_MASK_MAXAERITER_COLOR_DEFAULT = MiscFlags;
    public static final Color PROPERTY_MASK_MAXAERITER_COLOR_DEFAULT = MediumGray;




    // MODGLINT

    public static final String PROPERTY_MASK_MODGLINT_NAME = "MODGLINT";

    public static final String PROPERTY_MASK_MODGLINT_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.MODGLINT";

    public static final String PROPERTY_MASK_MODGLINT_SECTION_KEY = PROPERTY_MASK_MODGLINT_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_MODGLINT_SECTION_LABEL = "L2 Flag Mask: MODGLINT";
    public static final String PROPERTY_MASK_MODGLINT_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask MODGLINT";

    public static final String PROPERTY_MASK_MODGLINT_ENABLED_KEY = PROPERTY_MASK_MODGLINT_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_MODGLINT_ENABLED_LABEL = "MODGLINT: Set as Selected";
    public static final String PROPERTY_MASK_MODGLINT_ENABLED_TOOLTIP = "Set MODGLINT mask as selected by default";
    public static final boolean PROPERTY_MASK_MODGLINT_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_MODGLINT_TRANSPARENCY_KEY = PROPERTY_MASK_MODGLINT_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_MODGLINT_TRANSPARENCY_LABEL = "MODGLINT: Transparency";
    public static final String PROPERTY_MASK_MODGLINT_TRANSPARENCY_TOOLTIP = "Set default transparency of the MODGLINT mask";
    public static final double PROPERTY_MASK_MODGLINT_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_MODGLINT_COLOR_KEY = PROPERTY_MASK_MODGLINT_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_MODGLINT_COLOR_LABEL = "MODGLINT: Color";
    public static final String PROPERTY_MASK_MODGLINT_COLOR_TOOLTIP = "Set default color of the MODGLINT mask";
    //    public static final Color PROPERTY_MASK_MODGLINT_COLOR_DEFAULT = BrightFlags;
    public static final Color PROPERTY_MASK_MODGLINT_COLOR_DEFAULT = LightPurple;




    // CHLWARN

    public static final String PROPERTY_MASK_CHLWARN_NAME = "CHLWARN";

    public static final String PROPERTY_MASK_CHLWARN_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.CHLWARN";

    public static final String PROPERTY_MASK_CHLWARN_SECTION_KEY = PROPERTY_MASK_CHLWARN_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_CHLWARN_SECTION_LABEL = "L2 Flag Mask: CHLWARN";
    public static final String PROPERTY_MASK_CHLWARN_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask CHLWARN";

    public static final String PROPERTY_MASK_CHLWARN_ENABLED_KEY = PROPERTY_MASK_CHLWARN_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_CHLWARN_ENABLED_LABEL = "CHLWARN: Set as Selected";
    public static final String PROPERTY_MASK_CHLWARN_ENABLED_TOOLTIP = "Set CHLWARN mask as selected by default";
    public static final boolean PROPERTY_MASK_CHLWARN_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_CHLWARN_TRANSPARENCY_KEY = PROPERTY_MASK_CHLWARN_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_CHLWARN_TRANSPARENCY_LABEL = "CHLWARN: Transparency";
    public static final String PROPERTY_MASK_CHLWARN_TRANSPARENCY_TOOLTIP = "Set default transparency of the CHLWARN mask";
    public static final double PROPERTY_MASK_CHLWARN_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_CHLWARN_COLOR_KEY = PROPERTY_MASK_CHLWARN_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_CHLWARN_COLOR_LABEL = "CHLWARN: Color";
    public static final String PROPERTY_MASK_CHLWARN_COLOR_TOOLTIP = "Set default color of the CHLWARN mask";
    //    public static final Color PROPERTY_MASK_CHLWARN_COLOR_DEFAULT = Warnings;
    public static final Color PROPERTY_MASK_CHLWARN_COLOR_DEFAULT = Color.LIGHT_GRAY;



    // ATMWARN

    public static final String PROPERTY_MASK_ATMWARN_NAME = "ATMWARN";

    public static final String PROPERTY_MASK_ATMWARN_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.ATMWARN";

    public static final String PROPERTY_MASK_ATMWARN_SECTION_KEY = PROPERTY_MASK_ATMWARN_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_ATMWARN_SECTION_LABEL = "L2 Flag Mask: ATMWARN";
    public static final String PROPERTY_MASK_ATMWARN_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask ATMWARN";

    public static final String PROPERTY_MASK_ATMWARN_ENABLED_KEY = PROPERTY_MASK_ATMWARN_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_ATMWARN_ENABLED_LABEL = "ATMWARN: Set as Selected";
    public static final String PROPERTY_MASK_ATMWARN_ENABLED_TOOLTIP = "Set ATMWARN mask as selected by default";
    public static final boolean PROPERTY_MASK_ATMWARN_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_ATMWARN_TRANSPARENCY_KEY = PROPERTY_MASK_ATMWARN_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_ATMWARN_TRANSPARENCY_LABEL = "ATMWARN: Transparency";
    public static final String PROPERTY_MASK_ATMWARN_TRANSPARENCY_TOOLTIP = "Set default transparency of the ATMWARN mask";
    public static final double PROPERTY_MASK_ATMWARN_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_ATMWARN_COLOR_KEY = PROPERTY_MASK_ATMWARN_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_ATMWARN_COLOR_LABEL = "ATMWARN: Color";
    public static final String PROPERTY_MASK_ATMWARN_COLOR_TOOLTIP = "Set default color of the ATMWARN mask";
    //    public static final Color PROPERTY_MASK_ATMWARN_COLOR_DEFAULT = Warnings;
    public static final Color PROPERTY_MASK_ATMWARN_COLOR_DEFAULT = Color.MAGENTA;




    // SEAICE

    public static final String PROPERTY_MASK_SEAICE_NAME = "SEAICE";

    public static final String PROPERTY_MASK_SEAICE_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.SEAICE";

    public static final String PROPERTY_MASK_SEAICE_SECTION_KEY = PROPERTY_MASK_SEAICE_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_SEAICE_SECTION_LABEL = "L2 Flag Mask: SEAICE";
    public static final String PROPERTY_MASK_SEAICE_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask SEAICE";

    public static final String PROPERTY_MASK_SEAICE_ENABLED_KEY = PROPERTY_MASK_SEAICE_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_SEAICE_ENABLED_LABEL = "SEAICE: Set as Selected";
    public static final String PROPERTY_MASK_SEAICE_ENABLED_TOOLTIP = "Set SEAICE mask as selected by default";
    public static final boolean PROPERTY_MASK_SEAICE_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_SEAICE_TRANSPARENCY_KEY = PROPERTY_MASK_SEAICE_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_SEAICE_TRANSPARENCY_LABEL = "SEAICE: Transparency";
    public static final String PROPERTY_MASK_SEAICE_TRANSPARENCY_TOOLTIP = "Set default transparency of the SEAICE mask";
    public static final double PROPERTY_MASK_SEAICE_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_SEAICE_COLOR_KEY = PROPERTY_MASK_SEAICE_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_SEAICE_COLOR_LABEL = "SEAICE: Color";
    public static final String PROPERTY_MASK_SEAICE_COLOR_TOOLTIP = "Set default color of the SEAICE mask";
    //    public static final Color PROPERTY_MASK_SEAICE_COLOR_DEFAULT = BrightFlags;
    public static final Color PROPERTY_MASK_SEAICE_COLOR_DEFAULT = Color.DARK_GRAY;



    // NAVFAIL

    public static final String PROPERTY_MASK_NAVFAIL_NAME = "NAVFAIL";

    public static final String PROPERTY_MASK_NAVFAIL_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.NAVFAIL";

    public static final String PROPERTY_MASK_NAVFAIL_SECTION_KEY = PROPERTY_MASK_NAVFAIL_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_NAVFAIL_SECTION_LABEL = "L2 Flag Mask: NAVFAIL";
    public static final String PROPERTY_MASK_NAVFAIL_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask NAVFAIL";

    public static final String PROPERTY_MASK_NAVFAIL_ENABLED_KEY = PROPERTY_MASK_NAVFAIL_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_NAVFAIL_ENABLED_LABEL = "NAVFAIL: Set as Selected";
    public static final String PROPERTY_MASK_NAVFAIL_ENABLED_TOOLTIP = "Set NAVFAIL mask as selected by default";
    public static final boolean PROPERTY_MASK_NAVFAIL_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_NAVFAIL_TRANSPARENCY_KEY = PROPERTY_MASK_NAVFAIL_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_NAVFAIL_TRANSPARENCY_LABEL = "NAVFAIL: Transparency";
    public static final String PROPERTY_MASK_NAVFAIL_TRANSPARENCY_TOOLTIP = "Set default transparency of the NAVFAIL mask";
    public static final double PROPERTY_MASK_NAVFAIL_TRANSPARENCY_DEFAULT = 0.0;

    public static final String PROPERTY_MASK_NAVFAIL_COLOR_KEY = PROPERTY_MASK_NAVFAIL_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_NAVFAIL_COLOR_LABEL = "NAVFAIL: Color";
    public static final String PROPERTY_MASK_NAVFAIL_COLOR_TOOLTIP = "Set default color of the NAVFAIL mask";
    //    public static final Color PROPERTY_MASK_NAVFAIL_COLOR_DEFAULT = Failures;
    public static final Color PROPERTY_MASK_NAVFAIL_COLOR_DEFAULT = FailRed;



    // FILTER

    public static final String PROPERTY_MASK_FILTER_NAME = "FILTER";

    public static final String PROPERTY_MASK_FILTER_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.FILTER";

    public static final String PROPERTY_MASK_FILTER_SECTION_KEY = PROPERTY_MASK_FILTER_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_FILTER_SECTION_LABEL = "L2 Flag Mask: FILTER";
    public static final String PROPERTY_MASK_FILTER_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask FILTER";

    public static final String PROPERTY_MASK_FILTER_ENABLED_KEY = PROPERTY_MASK_FILTER_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_FILTER_ENABLED_LABEL = "FILTER: Set as Selected";
    public static final String PROPERTY_MASK_FILTER_ENABLED_TOOLTIP = "Set FILTER mask as selected by default";
    public static final boolean PROPERTY_MASK_FILTER_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_FILTER_TRANSPARENCY_KEY = PROPERTY_MASK_FILTER_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_FILTER_TRANSPARENCY_LABEL = "FILTER: Transparency";
    public static final String PROPERTY_MASK_FILTER_TRANSPARENCY_TOOLTIP = "Set default transparency of the FILTER mask";
    public static final double PROPERTY_MASK_FILTER_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_FILTER_COLOR_KEY = PROPERTY_MASK_FILTER_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_FILTER_COLOR_LABEL = "FILTER: Color";
    public static final String PROPERTY_MASK_FILTER_COLOR_TOOLTIP = "Set default color of the FILTER mask";
    //    public static final Color PROPERTY_MASK_FILTER_COLOR_DEFAULT = MiscFlags;
    public static final Color PROPERTY_MASK_FILTER_COLOR_DEFAULT = Color.LIGHT_GRAY;



    // BOWTIEDEL

    public static final String PROPERTY_MASK_BOWTIEDEL_NAME = "BOWTIEDEL";

    public static final String PROPERTY_MASK_BOWTIEDEL_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.BOWTIEDEL";

    public static final String PROPERTY_MASK_BOWTIEDEL_SECTION_KEY = PROPERTY_MASK_BOWTIEDEL_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_BOWTIEDEL_SECTION_LABEL = "L2 Flag Mask: BOWTIEDEL";
    public static final String PROPERTY_MASK_BOWTIEDEL_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask BOWTIEDEL";

    public static final String PROPERTY_MASK_BOWTIEDEL_ENABLED_KEY = PROPERTY_MASK_BOWTIEDEL_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_BOWTIEDEL_ENABLED_LABEL = "BOWTIEDEL: Set as Selected";
    public static final String PROPERTY_MASK_BOWTIEDEL_ENABLED_TOOLTIP = "Set BOWTIEDEL mask as selected by default";
    public static final boolean PROPERTY_MASK_BOWTIEDEL_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_BOWTIEDEL_TRANSPARENCY_KEY = PROPERTY_MASK_BOWTIEDEL_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_BOWTIEDEL_TRANSPARENCY_LABEL = "BOWTIEDEL: Transparency";
    public static final String PROPERTY_MASK_BOWTIEDEL_TRANSPARENCY_TOOLTIP = "Set default transparency of the BOWTIEDEL mask";
    public static final double PROPERTY_MASK_BOWTIEDEL_TRANSPARENCY_DEFAULT = 0.1;

    public static final String PROPERTY_MASK_BOWTIEDEL_COLOR_KEY = PROPERTY_MASK_BOWTIEDEL_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_BOWTIEDEL_COLOR_LABEL = "BOWTIEDEL: Color";
    public static final String PROPERTY_MASK_BOWTIEDEL_COLOR_TOOLTIP = "Set default color of the BOWTIEDEL mask";
    //    public static final Color PROPERTY_MASK_BOWTIEDEL_COLOR_DEFAULT = MiscFlags;
    public static final Color PROPERTY_MASK_BOWTIEDEL_COLOR_DEFAULT = FailRed;



    // HIPOL

    public static final String PROPERTY_MASK_HIPOL_NAME = "HIPOL";

    public static final String PROPERTY_MASK_HIPOL_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.HIPOL";

    public static final String PROPERTY_MASK_HIPOL_SECTION_KEY = PROPERTY_MASK_HIPOL_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_HIPOL_SECTION_LABEL = "L2 Flag Mask: HIPOL";
    public static final String PROPERTY_MASK_HIPOL_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask HIPOL";

    public static final String PROPERTY_MASK_HIPOL_ENABLED_KEY = PROPERTY_MASK_HIPOL_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_HIPOL_ENABLED_LABEL = "HIPOL: Set as Selected";
    public static final String PROPERTY_MASK_HIPOL_ENABLED_TOOLTIP = "Set HIPOL mask as selected by default";
    public static final boolean PROPERTY_MASK_HIPOL_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_HIPOL_TRANSPARENCY_KEY = PROPERTY_MASK_HIPOL_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_HIPOL_TRANSPARENCY_LABEL = "HIPOL: Transparency";
    public static final String PROPERTY_MASK_HIPOL_TRANSPARENCY_TOOLTIP = "Set default transparency of the HIPOL mask";
    public static final double PROPERTY_MASK_HIPOL_TRANSPARENCY_DEFAULT = 0.5;

    public static final String PROPERTY_MASK_HIPOL_COLOR_KEY = PROPERTY_MASK_HIPOL_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_HIPOL_COLOR_LABEL = "HIPOL: Color";
    public static final String PROPERTY_MASK_HIPOL_COLOR_TOOLTIP = "Set default color of the HIPOL mask";
    //    public static final Color PROPERTY_MASK_HIPOL_COLOR_DEFAULT = BrightFlags;
    public static final Color PROPERTY_MASK_HIPOL_COLOR_DEFAULT = Color.PINK;




    // PRODFAIL

    public static final String PROPERTY_MASK_PRODFAIL_NAME = "PRODFAIL";

    public static final String PROPERTY_MASK_PRODFAIL_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.PRODFAIL";

    public static final String PROPERTY_MASK_PRODFAIL_SECTION_KEY = PROPERTY_MASK_PRODFAIL_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_PRODFAIL_SECTION_LABEL = "L2 Flag Mask: PRODFAIL";
    public static final String PROPERTY_MASK_PRODFAIL_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask PRODFAIL";

    public static final String PROPERTY_MASK_PRODFAIL_ENABLED_KEY = PROPERTY_MASK_PRODFAIL_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_PRODFAIL_ENABLED_LABEL = "PRODFAIL: Set as Selected";
    public static final String PROPERTY_MASK_PRODFAIL_ENABLED_TOOLTIP = "Set PRODFAIL mask as selected by default";
    public static final boolean PROPERTY_MASK_PRODFAIL_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_PRODFAIL_TRANSPARENCY_KEY = PROPERTY_MASK_PRODFAIL_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_PRODFAIL_TRANSPARENCY_LABEL = "PRODFAIL: Transparency";
    public static final String PROPERTY_MASK_PRODFAIL_TRANSPARENCY_TOOLTIP = "Set default transparency of the PRODFAIL mask";
    public static final double PROPERTY_MASK_PRODFAIL_TRANSPARENCY_DEFAULT = 0.1;

    public static final String PROPERTY_MASK_PRODFAIL_COLOR_KEY = PROPERTY_MASK_PRODFAIL_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_PRODFAIL_COLOR_LABEL = "PRODFAIL: Color";
    public static final String PROPERTY_MASK_PRODFAIL_COLOR_TOOLTIP = "Set default color of the PRODFAIL mask";
    //    public static final Color PROPERTY_MASK_PRODFAIL_COLOR_DEFAULT = Failures;
    public static final Color PROPERTY_MASK_PRODFAIL_COLOR_DEFAULT = FailRed;



    // GEOREGION

    public static final String PROPERTY_MASK_GEOREGION_NAME = "GEOREGION";

    public static final String PROPERTY_MASK_GEOREGION_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.GEOREGION";

    public static final String PROPERTY_MASK_GEOREGION_SECTION_KEY = PROPERTY_MASK_GEOREGION_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_GEOREGION_SECTION_LABEL = "L2 Flag Mask: GEOREGION";
    public static final String PROPERTY_MASK_GEOREGION_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask GEOREGION";

    public static final String PROPERTY_MASK_GEOREGION_ENABLED_KEY = PROPERTY_MASK_GEOREGION_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_GEOREGION_ENABLED_LABEL = "GEOREGION: Set as Selected";
    public static final String PROPERTY_MASK_GEOREGION_ENABLED_TOOLTIP = "Set GEOREGION mask as selected by default";
    public static final boolean PROPERTY_MASK_GEOREGION_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_GEOREGION_TRANSPARENCY_KEY = PROPERTY_MASK_GEOREGION_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_GEOREGION_TRANSPARENCY_LABEL = "GEOREGION: Transparency";
    public static final String PROPERTY_MASK_GEOREGION_TRANSPARENCY_TOOLTIP = "Set default transparency of the GEOREGION mask";
    public static final double PROPERTY_MASK_GEOREGION_TRANSPARENCY_DEFAULT = 0.0;

    public static final String PROPERTY_MASK_GEOREGION_COLOR_KEY = PROPERTY_MASK_GEOREGION_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_GEOREGION_COLOR_LABEL = "GEOREGION: Color";
    public static final String PROPERTY_MASK_GEOREGION_COLOR_TOOLTIP = "Set default color of the GEOREGION mask";
    public static final Color PROPERTY_MASK_GEOREGION_COLOR_DEFAULT = MiscFlags;





    public static final String PROPERTY_MASK_OVERRIDE_COLOR_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".override.color";
    public static final String PROPERTY_MASK_OVERRIDE_COLOR_LABEL = "Quality_L2: Color Override";
    public static final String PROPERTY_MASK_OVERRIDE_COLOR_TOOLTIP = "Override all masks with this color";
    public static final Color PROPERTY_MASK_OVERRIDE_COLOR_DEFAULT = Color.darkGray;


    public static final String PROPERTY_MASK_SORT_ENABLED_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + "mask.sort.enabled";
    public static final String PROPERTY_MASK_SORT_ENABLED_LABEL = "Sort Level-2 Flag Masks";
    public static final String PROPERTY_MASK_SORT_ENABLED_TOOLTIP = "Sort Level-2 Flag Masks";
    public static final boolean PROPERTY_MASK_SORT_ENABLED_DEFAULT = true;

    public static final String PROPERTY_MASK_SORT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.sort";
    public static final String PROPERTY_MASK_SORT_LABEL = "Ordered Flag List";
    public static final String PROPERTY_MASK_SORT_TOOLTIP = "Ordered Flags (any flags not included in this list will still be loaded at bottom of Mask Manager)";
    public static final String PROPERTY_MASK_SORT_DEFAULT = "LAND ABSAER ATMFAIL ATMWARN BOWTIEDEL CHLFAIL CHLWARN CLDICE COASTZ COCCOLITH FILTER GEOREGION HIGLINT HILT HIPOL HISATZEN HISOLZEN LOWLW MAXAERITER MODGLINT NAVFAIL NAVWARN PRODFAIL PRODWARN SEAICE STRAYLIGHT TURBIDW";



    public static final String PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION1 = "Select Stored Presets";
    public static final String PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION2_FLAGS_ONLY = "ATMFAIL,LAND,HILT,HISATZEN,STRAYLIGHT,CLDICE,COCCOLITH,LOWLW,CHLWARN,CHLFAIL,NAVWARN,MAXAERITER,ATMWARN,HISOLZEN,NAVFAIL,FILTER,HIGLINT,BOWTIEDEL";
    public static final String PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION2 = "OC: " + PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION2_FLAGS_ONLY;
    public static final String PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION3 = "CYAN: LAND,CLDICE,HISATZEN,BOWTIEDEL";
    public static final String PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION4 = "SFREFL: ATMFAIL,BOWTIEDEL";



    // COMPOSITE1

    public static final String PROPERTY_MASK_COMPOSITE1_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.Composite1";

    public static final String PROPERTY_MASK_COMPOSITE1_SECTION_KEY = PROPERTY_MASK_COMPOSITE1_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_COMPOSITE1_SECTION_LABEL = "Derived Mask: Composite1";
    public static final String PROPERTY_MASK_COMPOSITE1_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask Composite1";

    public static final String PROPERTY_MASK_COMPOSITE1_INCLUDE_KEY = PROPERTY_MASK_COMPOSITE1_ROOT_KEY + ".include";
    public static final String PROPERTY_MASK_COMPOSITE1_INCLUDE_LABEL = "Composite1: Include";
    public static final String PROPERTY_MASK_COMPOSITE1_TOOLTIP = "Include Composite1 mask";
    public static final boolean PROPERTY_MASK_COMPOSITE1_INCLUDE_DEFAULT = true;

    public static final String PROPERTY_MASK_COMPOSITE1_ENABLED_KEY = PROPERTY_MASK_COMPOSITE1_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_COMPOSITE1_ENABLED_LABEL = "Composite1: Set as Selected";
    public static final String PROPERTY_MASK_COMPOSITE1_ENABLED_TOOLTIP = "Set Composite1 mask as selected by default";
    public static final boolean PROPERTY_MASK_COMPOSITE1_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_COMPOSITE1_NAME_KEY = PROPERTY_MASK_COMPOSITE1_ROOT_KEY + ".name";
    public static final String PROPERTY_MASK_COMPOSITE1_NAME_LABEL = "Composite1: Mask Name";
    public static final String PROPERTY_MASK_COMPOSITE1_NAME_TOOLTIP = "Set name of Composite1 mask";
    public static final String PROPERTY_MASK_COMPOSITE1_NAME_DEFAULT = "Composite1";

    public static final String PROPERTY_MASK_COMPOSITE1_FLAGS_KEY = PROPERTY_MASK_COMPOSITE1_ROOT_KEY + ".flags";
    public static final String PROPERTY_MASK_COMPOSITE1_FLAGS_LABEL = "Composite1: Flags";
    public static final String PROPERTY_MASK_COMPOSITE1_FLAGS_TOOLTIP = "Set flags for Composite1 mask";
    public static final String PROPERTY_MASK_COMPOSITE1_FLAGS_DEFAULT = PROPERTY_MASK_COMPOSITE_FLAG_PRESETS_OPTION2_FLAGS_ONLY;

    public static final String PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_KEY = PROPERTY_MASK_COMPOSITE1_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_LABEL = "Composite1: Transparency";
    public static final String PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_TOOLTIP = "Set default transparency of the Composite1 mask";
    public static final double PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_DEFAULT = 0.0;

    public static final String PROPERTY_MASK_COMPOSITE1_COLOR_KEY = PROPERTY_MASK_COMPOSITE1_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_COMPOSITE1_COLOR_LABEL = "Composite1: Color";
    public static final String PROPERTY_MASK_COMPOSITE1_COLOR_TOOLTIP = "Set default color of the Composite1 mask";
    public static final Color PROPERTY_MASK_COMPOSITE1_COLOR_DEFAULT = Gray200;






    // COMPOSITE2

    public static final String PROPERTY_MASK_COMPOSITE2_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.Composite2";

    public static final String PROPERTY_MASK_COMPOSITE2_SECTION_KEY = PROPERTY_MASK_COMPOSITE2_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_COMPOSITE2_SECTION_LABEL = "Derived Mask: Composite2";
    public static final String PROPERTY_MASK_COMPOSITE2_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask Composite2";

    public static final String PROPERTY_MASK_COMPOSITE2_INCLUDE_KEY = PROPERTY_MASK_COMPOSITE2_ROOT_KEY + ".include";
    public static final String PROPERTY_MASK_COMPOSITE2_INCLUDE_LABEL = "Composite2: Include";
    public static final String PROPERTY_MASK_COMPOSITE2_TOOLTIP = "Include Composite2 mask";
    public static final boolean PROPERTY_MASK_COMPOSITE2_INCLUDE_DEFAULT = false;

    public static final String PROPERTY_MASK_COMPOSITE2_ENABLED_KEY = PROPERTY_MASK_COMPOSITE2_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_COMPOSITE2_ENABLED_LABEL = "Composite2: Set as Selected";
    public static final String PROPERTY_MASK_COMPOSITE2_ENABLED_TOOLTIP = "Set Composite2 mask as selected by default";
    public static final boolean PROPERTY_MASK_COMPOSITE2_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_COMPOSITE2_NAME_KEY = PROPERTY_MASK_COMPOSITE2_ROOT_KEY + ".name";
    public static final String PROPERTY_MASK_COMPOSITE2_NAME_LABEL = "Composite2: Mask Name";
    public static final String PROPERTY_MASK_COMPOSITE2_NAME_TOOLTIP = "Set name of Composite2 mask";
    public static final String PROPERTY_MASK_COMPOSITE2_NAME_DEFAULT = "Composite2";

    public static final String PROPERTY_MASK_COMPOSITE2_FLAGS_KEY = PROPERTY_MASK_COMPOSITE2_ROOT_KEY + ".flags";
    public static final String PROPERTY_MASK_COMPOSITE2_FLAGS_LABEL = "Composite2: Flags";
    public static final String PROPERTY_MASK_COMPOSITE2_FLAGS_TOOLTIP = "Set flags for Composite2 mask";
    public static final String PROPERTY_MASK_COMPOSITE2_FLAGS_DEFAULT = "";

    public static final String PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_KEY = PROPERTY_MASK_COMPOSITE2_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_LABEL = "Composite2: Transparency";
    public static final String PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_TOOLTIP = "Set default transparency of the Composite2 mask";
    public static final double PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_DEFAULT = 0.0;

    public static final String PROPERTY_MASK_COMPOSITE2_COLOR_KEY = PROPERTY_MASK_COMPOSITE2_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_COMPOSITE2_COLOR_LABEL = "Composite2: Color";
    public static final String PROPERTY_MASK_COMPOSITE2_COLOR_TOOLTIP = "Set default color of the Composite2 mask";
    public static final Color PROPERTY_MASK_COMPOSITE2_COLOR_DEFAULT = Gray200;





    // COMPOSITE3

    public static final String PROPERTY_MASK_COMPOSITE3_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.Composite3";

    public static final String PROPERTY_MASK_COMPOSITE3_SECTION_KEY = PROPERTY_MASK_COMPOSITE3_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_COMPOSITE3_SECTION_LABEL = "Derived Mask: Composite3";
    public static final String PROPERTY_MASK_COMPOSITE3_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask Composite3";

    public static final String PROPERTY_MASK_COMPOSITE3_INCLUDE_KEY = PROPERTY_MASK_COMPOSITE3_ROOT_KEY + ".include";
    public static final String PROPERTY_MASK_COMPOSITE3_INCLUDE_LABEL = "Composite3: Include";
    public static final String PROPERTY_MASK_COMPOSITE3_TOOLTIP = "Include Composite3 mask";
    public static final boolean PROPERTY_MASK_COMPOSITE3_INCLUDE_DEFAULT = false;

    public static final String PROPERTY_MASK_COMPOSITE3_ENABLED_KEY = PROPERTY_MASK_COMPOSITE3_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_COMPOSITE3_ENABLED_LABEL = "Composite3: Set as Selected";
    public static final String PROPERTY_MASK_COMPOSITE3_ENABLED_TOOLTIP = "Set Composite3 mask as selected by default";
    public static final boolean PROPERTY_MASK_COMPOSITE3_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_COMPOSITE3_NAME_KEY = PROPERTY_MASK_COMPOSITE3_ROOT_KEY + ".name";
    public static final String PROPERTY_MASK_COMPOSITE3_NAME_LABEL = "Composite3: Mask Name";
    public static final String PROPERTY_MASK_COMPOSITE3_NAME_TOOLTIP = "Set name of Composite3 mask";
    public static final String PROPERTY_MASK_COMPOSITE3_NAME_DEFAULT = "Composite3";


    public static final String PROPERTY_MASK_COMPOSITE3_FLAGS_KEY = PROPERTY_MASK_COMPOSITE3_ROOT_KEY + ".flags";
    public static final String PROPERTY_MASK_COMPOSITE3_FLAGS_LABEL = "Composite3: Flags";
    public static final String PROPERTY_MASK_COMPOSITE3_FLAGS_TOOLTIP = "Set flags for Composite3 mask";
    public static final String PROPERTY_MASK_COMPOSITE3_FLAGS_DEFAULT = "";

    public static final String PROPERTY_MASK_COMPOSITE3_TRANSPARENCY_KEY = PROPERTY_MASK_COMPOSITE3_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_COMPOSITE3_TRANSPARENCY_LABEL = "Composite3: Transparency";
    public static final String PROPERTY_MASK_COMPOSITE3_TRANSPARENCY_TOOLTIP = "Set default transparency of the Composite3 mask";
    public static final double PROPERTY_MASK_COMPOSITE3_TRANSPARENCY_DEFAULT = 0.0;

    public static final String PROPERTY_MASK_COMPOSITE3_COLOR_KEY = PROPERTY_MASK_COMPOSITE3_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_COMPOSITE3_COLOR_LABEL = "Composite3: Color";
    public static final String PROPERTY_MASK_COMPOSITE3_COLOR_TOOLTIP = "Set default color of the Composite3 mask";
    public static final Color PROPERTY_MASK_COMPOSITE3_COLOR_DEFAULT = Gray200;




    // Water

    public static final String PROPERTY_MASK_Water_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.Water";

    public static final String PROPERTY_MASK_Water_SECTION_KEY = PROPERTY_MASK_Water_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_Water_SECTION_LABEL = "Derived Mask: Water";
    public static final String PROPERTY_MASK_Water_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask Water";

    public static final String PROPERTY_MASK_Water_ENABLED_KEY = PROPERTY_MASK_Water_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_Water_ENABLED_LABEL = "Water: Set as Selected";
    public static final String PROPERTY_MASK_Water_ENABLED_TOOLTIP = "Set Water mask as selected by default";
    public static final boolean PROPERTY_MASK_Water_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_Water_TRANSPARENCY_KEY = PROPERTY_MASK_Water_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_Water_TRANSPARENCY_LABEL = "Water: Transparency";
    public static final String PROPERTY_MASK_Water_TRANSPARENCY_TOOLTIP = "Set default transparency of the Water mask";
    public static final double PROPERTY_MASK_Water_TRANSPARENCY_DEFAULT = 0.0;

    public static final String PROPERTY_MASK_Water_COLOR_KEY = PROPERTY_MASK_Water_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_Water_COLOR_LABEL = "Water: Color";
    public static final String PROPERTY_MASK_Water_COLOR_TOOLTIP = "Set default color of the Water mask";
    public static final Color PROPERTY_MASK_Water_COLOR_DEFAULT = DeepBlue;



    // SPARE

    public static final String PROPERTY_MASK_SPARE_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.SPARE";

    public static final String PROPERTY_MASK_SPARE_SECTION_KEY = PROPERTY_MASK_SPARE_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_SPARE_SECTION_LABEL = "L2 Flag Mask: Developer Unused SPARE Masks";
    public static final String PROPERTY_MASK_SPARE_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for masks SPARE";

    public static final String PROPERTY_MASK_SPARE_INCLUDE_KEY = PROPERTY_MASK_SPARE_ROOT_KEY + ".include";
    public static final String PROPERTY_MASK_SPARE_INCLUDE_LABEL = "SPARE(s): Include non-operational development masks";
    public static final String PROPERTY_MASK_SPARE_INCLUDE_TOOLTIP = "Include SPARE development masks";
    public static final boolean PROPERTY_MASK_SPARE_INCLUDE_DEFAULT = false;

    public static final String PROPERTY_MASK_SPARE_TRANSPARENCY_KEY = PROPERTY_MASK_SPARE_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_SPARE_TRANSPARENCY_LABEL = "SPARE(s): Transparency";
    public static final String PROPERTY_MASK_SPARE_TRANSPARENCY_TOOLTIP = "Set default transparency of the SPARE mask";
    public static final double PROPERTY_MASK_SPARE_TRANSPARENCY_DEFAULT = 0.0;

    public static final String PROPERTY_MASK_SPARE_COLOR_KEY = PROPERTY_MASK_SPARE_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_SPARE_COLOR_LABEL = "SPARE(s): Color";
    public static final String PROPERTY_MASK_SPARE_COLOR_TOOLTIP = "Set default color of the SPARE mask";
    public static final Color PROPERTY_MASK_SPARE_COLOR_DEFAULT = MiscFlags;


    public static final String FlIP_MISSION_DEFAULT = "MISSION DEFAULT: (Earth Orientation)";
    public static final String FlIP_YES = "YES";
    public static final String FlIP_NO = "NO: (Native Sensor Orientation)";
    public static final String FlIP_NO_L3 = "NO: (Native Orientation)";


    // L3_MAPPED FILES

    public static final String PROPERTY_FILE_PROPERTIES_L3_MAPPED_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".file_properties.L3_MAPPED";

    public static final String PROPERTY_FILE_PROPERTIES_L3_MAPPED_SECTION_KEY = PROPERTY_FILE_PROPERTIES_L3_MAPPED_ROOT_KEY + ".section";
    public static final String PROPERTY_FILE_PROPERTIES_L3_MAPPED_SECTION_LABEL = "Level L3 Mapped Files";
    public static final String PROPERTY_FILE_PROPERTIES_L3_MAPPED_SECTION_TOOLTIP = "SeaDAS Level-3 Mapped file reader options";

    public static final String PROPERTY_BAND_GROUPING_L3_MAPPED_KEY = PROPERTY_FILE_PROPERTIES_L3_MAPPED_ROOT_KEY + ".band_grouping";
    public static final String PROPERTY_BAND_GROUPING_L3_MAPPED_LABEL = "Band Grouping";
    public static final String PROPERTY_BAND_GROUPING_L3_MAPPED_TOOLTIP = "Expression to create band groupings into folders";
    public static final String PROPERTY_BAND_GROUPING_L3_MAPPED_DEFAULT = "Rrs:nLw:Lt:La:Lr:Lw:L_q:L_u:Es:TLg:rhom:rhos:rhot:Taua:Kd:aot:adg:aph_:bbp:vgain:BT:tg_sol:tg_sen";

    public static final String PROPERTY_FLIPX_L3_MAPPED_KEY = PROPERTY_FILE_PROPERTIES_L3_MAPPED_ROOT_KEY + ".flipx";
    public static final String PROPERTY_FLIPX_L3_MAPPED_LABEL = "Flip Horizontal";
    public static final String PROPERTY_FLIPX_L3_MAPPED_TOOLTIP = "Flip image horizontally";
    public static final String PROPERTY_FLIPX_L3_MAPPED_DEFAULT = FlIP_NO_L3;

    public static final String PROPERTY_FLIPY_L3_MAPPED_KEY = PROPERTY_FILE_PROPERTIES_L3_MAPPED_ROOT_KEY + ".flipy";
    public static final String PROPERTY_FLIPY_L3_MAPPED_LABEL = "Flip Vertical";
    public static final String PROPERTY_FLIPY_L3_MAPPED_TOOLTIP = "Flip image vertically";
    public static final String PROPERTY_FLIPY_L3_MAPPED_DEFAULT = FlIP_NO_L3;




    // LEVEL2 FILES

    public static final String PROPERTY_FILE_PROPERTIES_LEVEL2_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".file_properties.level2";

    public static final String PROPERTY_FILE_PROPERTIES_LEVEL2_SECTION_KEY = PROPERTY_FILE_PROPERTIES_LEVEL2_ROOT_KEY + ".section";
    public static final String PROPERTY_FILE_PROPERTIES_LEVEL2_SECTION_LABEL = "Level 2 Files";
    public static final String PROPERTY_FILE_PROPERTIES_LEVEL2_SECTION_TOOLTIP = "SeaDAS Level-2 file reader options";

    public static final String PROPERTY_BAND_GROUPING_LEVEL2_KEY = PROPERTY_FILE_PROPERTIES_LEVEL2_ROOT_KEY + ".band_grouping";
    public static final String PROPERTY_BAND_GROUPING_LEVEL2_LABEL = "Band Grouping";
    public static final String PROPERTY_BAND_GROUPING_LEVEL2_TOOLTIP = "Expression to create band groupings into folders";
    public static final String PROPERTY_BAND_GROUPING_LEVEL2_DEFAULT = "Rrs_unc:Rrs:Rrs_raman:nLw:Lt:La:Lr:Lw:L_q:L_u:Es:rhom:rhos:rhot:Taua:taua:Kd:aot:adg:aph_:bbp:bb:vgain:BT:tg_sen:tg_sol:t_sen:t_sol:tLf:TLg:brdf";

    // disabled but might want to use in future
    public static final String PROPERTY_BAND_GROUPING_RESET_KEY = PROPERTY_FILE_PROPERTIES_LEVEL2_ROOT_KEY + ".band_grouping.reset";
    public static final String PROPERTY_BAND_GROUPING_RESET_LABEL = "Band Grouping (reset)";
    public static final String PROPERTY_BAND_GROUPING_RESET_TOOLTIP = "Reset band grouping to default";
    public static final boolean PROPERTY_BAND_GROUPING_RESET_DEFAULT = true;

    public static final String PROPERTY_FLIPX_LEVEL2_KEY = PROPERTY_FILE_PROPERTIES_LEVEL2_ROOT_KEY + ".flipx";
    public static final String PROPERTY_FLIPX_LEVEL2_LABEL = "Flip Horizontal";
    public static final String PROPERTY_FLIPX_LEVEL2_TOOLTIP = "Flip image horizontally";
    public static final String PROPERTY_FLIPX_LEVEL2_DEFAULT = FlIP_MISSION_DEFAULT;

    public static final String PROPERTY_FLIPY_LEVEL2_KEY = PROPERTY_FILE_PROPERTIES_LEVEL2_ROOT_KEY + ".flipy";
    public static final String PROPERTY_FLIPY_LEVEL2_LABEL = "Flip Vertical";
    public static final String PROPERTY_FLIPY_LEVEL2_TOOLTIP = "Flip image vertically";
    public static final String PROPERTY_FLIPY_LEVEL2_DEFAULT = FlIP_MISSION_DEFAULT;



    // L1B_PACE FILES

    public static final String PROPERTY_FILE_PROPERTIES_L1B_PACE_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".file_properties.l1b_pace";

    public static final String PROPERTY_FILE_PROPERTIES_L1B_PACE_SECTION_KEY = PROPERTY_FILE_PROPERTIES_L1B_PACE_ROOT_KEY + ".section";
    public static final String PROPERTY_FILE_PROPERTIES_L1B_PACE_SECTION_LABEL = "Level 1B PACE Files";
    public static final String PROPERTY_FILE_PROPERTIES_L1B_PACE_SECTION_TOOLTIP = "SeaDAS Level-1B PACE OCI file reader options";

    public static final String PROPERTY_BAND_GROUPING_L1B_PACE_KEY = PROPERTY_FILE_PROPERTIES_L1B_PACE_ROOT_KEY + ".band_grouping";
    public static final String PROPERTY_BAND_GROUPING_L1B_PACE_LABEL = "Band Grouping (OCI)";
    public static final String PROPERTY_BAND_GROUPING_L1B_PACE_TOOLTIP = "Expression to create band groupings into folders for the OCI instrument";
    public static final String PROPERTY_BAND_GROUPING_L1B_PACE_DEFAULT = "rhot_blue:rhot_red:rhot_SWIR:qual_blue:qual_red:qual_SWIR:Lt_blue:Lt_red:Lt_SWIR";

    public static final String PROPERTY_FLIPX_L1B_PACE_KEY = PROPERTY_FILE_PROPERTIES_L1B_PACE_ROOT_KEY + ".flipx";
    public static final String PROPERTY_FLIPX_L1B_PACE_LABEL = "Flip Horizontal";
    public static final String PROPERTY_FLIPX_L1B_PACE_TOOLTIP = "Flip image horizontally";
    public static final String PROPERTY_FLIPX_L1B_PACE_DEFAULT = FlIP_MISSION_DEFAULT;

    public static final String PROPERTY_FLIPY_L1B_PACE_KEY = PROPERTY_FILE_PROPERTIES_L1B_PACE_ROOT_KEY + ".flipy";
    public static final String PROPERTY_FLIPY_L1B_PACE_LABEL = "Flip Vertical";
    public static final String PROPERTY_FLIPY_L1B_PACE_TOOLTIP = "Flip image vertically";
    public static final String PROPERTY_FLIPY_L1B_PACE_DEFAULT = FlIP_MISSION_DEFAULT;



    // L1C_PACE FILES

    public static final String PROPERTY_FILE_PROPERTIES_L1C_PACE_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".file_properties.l1c_pace";

    public static final String PROPERTY_FILE_PROPERTIES_L1C_PACE_SECTION_KEY = PROPERTY_FILE_PROPERTIES_L1C_PACE_ROOT_KEY + ".section";
    public static final String PROPERTY_FILE_PROPERTIES_L1C_PACE_SECTION_LABEL = "Level 1C PACE Files";
    public static final String PROPERTY_FILE_PROPERTIES_L1C_PACE_SECTION_TOOLTIP = "SeaDAS Level-1C PACE OCI file reader options";

    public static final String PROPERTY_BAND_GROUPING_L1C_PACE_KEY = PROPERTY_FILE_PROPERTIES_L1C_PACE_ROOT_KEY + ".band_grouping";
    public static final String PROPERTY_BAND_GROUPING_L1C_PACE_LABEL = "Band Grouping (OCI)";
    public static final String PROPERTY_BAND_GROUPING_L1C_PACE_TOOLTIP = "Expression to create band groupings into folders for the OCI instrument";
    public static final String PROPERTY_BAND_GROUPING_L1C_PACE_DEFAULT = "I_-20:I_20:i_20:i_-20:obs_per_view:view_time_offsets:sensor_azimuth:sensor_zenith:" +
            "solar_azimuth:solar_zenith:scattering_angle:rotation_angle:qc_bitwise_-20:qc_bitwise_20:" +
            "qc_-20:qc_20:I_stdev_-20:I_stdev_20:i_stdev_20:i_stdev_-20";

    public static final String PROPERTY_FLIPX_L1C_PACE_KEY = PROPERTY_FILE_PROPERTIES_L1C_PACE_ROOT_KEY + ".flipx";
    public static final String PROPERTY_FLIPX_L1C_PACE_LABEL = "Flip Horizontal";
    public static final String PROPERTY_FLIPX_L1C_PACE_TOOLTIP = "Flip image horizontally";
    public static final String PROPERTY_FLIPX_L1C_PACE_DEFAULT = FlIP_MISSION_DEFAULT;

    public static final String PROPERTY_FLIPY_L1C_PACE_KEY = PROPERTY_FILE_PROPERTIES_L1C_PACE_ROOT_KEY + ".flipy";
    public static final String PROPERTY_FLIPY_L1C_PACE_LABEL = "Flip Vertical";
    public static final String PROPERTY_FLIPY_L1C_PACE_TOOLTIP = "Flip image vertically";
    public static final String PROPERTY_FLIPY_L1C_PACE_DEFAULT = FlIP_MISSION_DEFAULT;



    public static final String PROPERTY_BAND_GROUPING_L1C_PACE_HARP2_KEY = PROPERTY_FILE_PROPERTIES_L1C_PACE_ROOT_KEY + ".band_grouping.harp2";
    public static final String PROPERTY_BAND_GROUPING_L1C_PACE_HARP2_LABEL = "Band Grouping (HARP2)";
    public static final String PROPERTY_BAND_GROUPING_L1C_PACE_HARP2_TOOLTIP = "Expression to create band groupings into folders for the HARP2 instrument";
    public static final String PROPERTY_BAND_GROUPING_L1C_PACE_HARP2_DEFAULT = "I_*_549:I_*_669:I_*_867:I_*_441:Q_*_549:Q_*_669:Q_*_867:Q_*_441:" +
            "U_*_549:U_*_669:U_*_867:U_*_441:DOLP_*_549:DOLP_*_669:DOLP_*_867:DOLP_*_441:" +
            "I_noise_*_549:I_noise_*_669:I_noise_*_867:I_noise_*_441:Q_noise_*_549:Q_noise_*_669:Q_noise_*_867:Q_noise_*_441:" +
            "U_noise_*_549:U_noise_*_669:U_noise_*_867:U_noise_*_441:DOLP_noise_*_549:DOLP_noise_*_669:DOLP_noise_*_867:DOLP_noise_*_441:" +
            "Sensor_Zenith:Sensor_Azimuth:Solar_Zenith:Solar_Azimuth:view_time_offsets:obs_per_view:number_of_observations:" +
            "sensor_zenith_angle:sensor_azimuth_angle:solar_zenith_angle:solar_azimuth_angle:scattering_angle:rotation_angle";




    // L1B_MODIS FILES

    public static final String PROPERTY_FILE_PROPERTIES_L1B_MODIS_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".file_properties.l1b_modis";

    public static final String PROPERTY_FILE_PROPERTIES_L1B_MODIS_SECTION_KEY = PROPERTY_FILE_PROPERTIES_L1B_MODIS_ROOT_KEY + ".section";
    public static final String PROPERTY_FILE_PROPERTIES_L1B_MODIS_SECTION_LABEL = "Level 1B MODIS Files";
    public static final String PROPERTY_FILE_PROPERTIES_L1B_MODIS_SECTION_TOOLTIP = "SeaDAS Level-1B MODIS file reader options";

    public static final String PROPERTY_BAND_GROUPING_L1B_MODIS_KEY = PROPERTY_FILE_PROPERTIES_L1B_MODIS_ROOT_KEY + ".band_grouping";
    public static final String PROPERTY_BAND_GROUPING_L1B_MODIS_LABEL = "Band Grouping";
    public static final String PROPERTY_BAND_GROUPING_L1B_MODIS_TOOLTIP = "Expression to create band groupings into folders for the MODIS instruments";
    public static final String PROPERTY_BAND_GROUPING_L1B_MODIS_DEFAULT = "RefSB:Emissive";

    public static final String PROPERTY_FLIPX_L1B_MODIS_KEY = PROPERTY_FILE_PROPERTIES_L1B_MODIS_ROOT_KEY + ".flipx";
    public static final String PROPERTY_FLIPX_L1B_MODIS_LABEL = "Flip Horizontal";
    public static final String PROPERTY_FLIPX_L1B_MODIS_TOOLTIP = "Flip image horizontally";
    public static final String PROPERTY_FLIPX_L1B_MODIS_DEFAULT = FlIP_MISSION_DEFAULT;

    public static final String PROPERTY_FLIPY_L1B_MODIS_KEY = PROPERTY_FILE_PROPERTIES_L1B_MODIS_ROOT_KEY + ".flipy";
    public static final String PROPERTY_FLIPY_L1B_MODIS_LABEL = "Flip Vertical";
    public static final String PROPERTY_FLIPY_L1B_MODIS_TOOLTIP = "Flip image vertically";
    public static final String PROPERTY_FLIPY_L1B_MODIS_DEFAULT = FlIP_MISSION_DEFAULT;



    // L1B_VIIRS FILES

    public static final String PROPERTY_FILE_PROPERTIES_L1B_VIIRS_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".file_properties.l1b_viirs";

    public static final String PROPERTY_FILE_PROPERTIES_L1B_VIIRS_SECTION_KEY = PROPERTY_FILE_PROPERTIES_L1B_VIIRS_ROOT_KEY + ".section";
    public static final String PROPERTY_FILE_PROPERTIES_L1B_VIIRS_SECTION_LABEL = "Level 1B VIIRS Files";
    public static final String PROPERTY_FILE_PROPERTIES_L1B_VIIRS_SECTION_TOOLTIP = "SeaDAS Level-1B VIIRS file reader options";

    public static final String PROPERTY_BAND_GROUPING_L1B_VIIRS_KEY = PROPERTY_FILE_PROPERTIES_L1B_VIIRS_ROOT_KEY + ".band_grouping";
    public static final String PROPERTY_BAND_GROUPING_L1B_VIIRS_LABEL = "Band Grouping";
    public static final String PROPERTY_BAND_GROUPING_L1B_VIIRS_TOOLTIP = "Expression to create band groupings into folders for the VIIRS instruments";
    public static final String PROPERTY_BAND_GROUPING_L1B_VIIRS_DEFAULT = "RefSB:Emissive";

    public static final String PROPERTY_FLIPX_L1B_VIIRS_KEY = PROPERTY_FILE_PROPERTIES_L1B_VIIRS_ROOT_KEY + ".flipx";
    public static final String PROPERTY_FLIPX_L1B_VIIRS_LABEL = "Flip Horizontal";
    public static final String PROPERTY_FLIPX_L1B_VIIRS_TOOLTIP = "Flip image horizontally";
    public static final String PROPERTY_FLIPX_L1B_VIIRS_DEFAULT = FlIP_MISSION_DEFAULT;

    public static final String PROPERTY_FLIPY_L1B_VIIRS_KEY = PROPERTY_FILE_PROPERTIES_L1B_VIIRS_ROOT_KEY + ".flipy";
    public static final String PROPERTY_FLIPY_L1B_VIIRS_LABEL = "Flip Vertical";
    public static final String PROPERTY_FLIPY_L1B_VIIRS_TOOLTIP = "Flip image vertically";
    public static final String PROPERTY_FLIPY_L1B_VIIRS_DEFAULT = FlIP_MISSION_DEFAULT;





    // Property Setting: Restore Defaults

    private static final String PROPERTY_RESTORE_KEY_SUFFIX = PROPERTY_SEADAS_READER_ROOT_KEY + ".restore.defaults";

    public static final String PROPERTY_RESTORE_SECTION_KEY = PROPERTY_RESTORE_KEY_SUFFIX + ".section";
    public static final String PROPERTY_RESTORE_SECTION_LABEL = "Restore";
    public static final String PROPERTY_RESTORE_SECTION_TOOLTIP = "Restores preferences to the package defaults";

    public static final String PROPERTY_RESTORE_DEFAULTS_KEY = PROPERTY_RESTORE_KEY_SUFFIX + ".apply";
    public static final String PROPERTY_RESTORE_DEFAULTS_LABEL = "Default (SeaDAS Toolbox Preferences)";
    public static final String PROPERTY_RESTORE_DEFAULTS_TOOLTIP = "Restore all color bar legend preferences to the original default";
    public static final boolean PROPERTY_RESTORE_DEFAULTS_DEFAULT = false;




    public static String getEnabledKey(MaskType maskType) {
        switch (maskType) {
            case WATER: return SeadasReaderDefaults.PROPERTY_MASK_Water_ENABLED_KEY;
            case SPARE: return SeadasReaderDefaults.PROPERTY_MASK_SPARE_INCLUDE_KEY;
            case COASTZ: return SeadasReaderDefaults.PROPERTY_MASK_COASTZ_ENABLED_KEY;
            case STRAYLIGHT: return SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_ENABLED_KEY;
            case CLDICE: return SeadasReaderDefaults.PROPERTY_MASK_CLDICE_ENABLED_KEY;
            case COCCOLITH: return SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_ENABLED_KEY;
            case TURBIDW: return SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_ENABLED_KEY;
            case HISOLZEN: return SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_ENABLED_KEY;
            case LOWLW: return SeadasReaderDefaults.PROPERTY_MASK_LOWLW_ENABLED_KEY;
            case CHLFAIL: return SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_ENABLED_KEY;
            case NAVWARN: return SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_ENABLED_KEY;
            case ABSAER: return SeadasReaderDefaults.PROPERTY_MASK_ABSAER_ENABLED_KEY;
            case MAXAERITER: return SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_ENABLED_KEY;
            case MODGLINT: return SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_ENABLED_KEY;
            case CHLWARN: return SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_ENABLED_KEY;
            case ATMWARN: return SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_ENABLED_KEY;
            case SEAICE: return SeadasReaderDefaults.PROPERTY_MASK_SEAICE_ENABLED_KEY;
            case NAVFAIL: return SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_ENABLED_KEY;
            case FILTER: return SeadasReaderDefaults.PROPERTY_MASK_FILTER_ENABLED_KEY;
            case BOWTIEDEL: return SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_ENABLED_KEY;
            case HIPOL: return SeadasReaderDefaults.PROPERTY_MASK_HIPOL_ENABLED_KEY;
            case PRODFAIL: return SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_ENABLED_KEY;
            case GEOREGION: return SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_ENABLED_KEY;
            case HIGLINT: return SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_ENABLED_KEY;
            case HILT: return SeadasReaderDefaults.PROPERTY_MASK_HILT_ENABLED_KEY;
            case HISATZEN: return SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_ENABLED_KEY;
            case PRODWARN: return SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_ENABLED_KEY;
            case LAND: return SeadasReaderDefaults.PROPERTY_MASK_LAND_ENABLED_KEY;
            case ATMFAIL: return SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_ENABLED_KEY;
            case COMPOSITE1_INCLUDE: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_INCLUDE_KEY;
            case COMPOSITE1: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_ENABLED_KEY;
            case COMPOSITE2_INCLUDE: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_INCLUDE_KEY;
            case COMPOSITE2: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_ENABLED_KEY;
            case COMPOSITE3_INCLUDE: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_INCLUDE_KEY;
            case COMPOSITE3: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_ENABLED_KEY;
            default: throw new IllegalArgumentException("Unknown MaskType: " + maskType);
        }
    }

    public static String getColorKey(MaskType maskType) {
        switch (maskType) {
            case WATER: return SeadasReaderDefaults.PROPERTY_MASK_Water_COLOR_KEY;
            case SPARE: return SeadasReaderDefaults.PROPERTY_MASK_SPARE_COLOR_KEY;
            case COASTZ: return SeadasReaderDefaults.PROPERTY_MASK_COASTZ_COLOR_KEY;
            case STRAYLIGHT: return SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_COLOR_KEY;
            case CLDICE: return SeadasReaderDefaults.PROPERTY_MASK_CLDICE_COLOR_KEY;
            case COCCOLITH: return SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_COLOR_KEY;
            case TURBIDW: return SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_COLOR_KEY;
            case HISOLZEN: return SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_COLOR_KEY;
            case LOWLW: return SeadasReaderDefaults.PROPERTY_MASK_LOWLW_COLOR_KEY;
            case CHLFAIL: return SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_COLOR_KEY;
            case NAVWARN: return SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_COLOR_KEY;
            case ABSAER: return SeadasReaderDefaults.PROPERTY_MASK_ABSAER_COLOR_KEY;
            case MAXAERITER: return SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_COLOR_KEY;
            case MODGLINT: return SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_COLOR_KEY;
            case CHLWARN: return SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_COLOR_KEY;
            case ATMWARN: return SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_COLOR_KEY;
            case SEAICE: return SeadasReaderDefaults.PROPERTY_MASK_SEAICE_COLOR_KEY;
            case NAVFAIL: return SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_COLOR_KEY;
            case FILTER: return SeadasReaderDefaults.PROPERTY_MASK_FILTER_COLOR_KEY;
            case BOWTIEDEL: return SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_COLOR_KEY;
            case HIPOL: return SeadasReaderDefaults.PROPERTY_MASK_HIPOL_COLOR_KEY;
            case PRODFAIL: return SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_COLOR_KEY;
            case GEOREGION: return SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_COLOR_KEY;
            case HIGLINT: return SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_COLOR_KEY;
            case HILT: return SeadasReaderDefaults.PROPERTY_MASK_HILT_COLOR_KEY;
            case HISATZEN: return SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_COLOR_KEY;
            case PRODWARN: return SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_COLOR_KEY;
            case LAND: return SeadasReaderDefaults.PROPERTY_MASK_LAND_COLOR_KEY;
            case ATMFAIL: return SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_COLOR_KEY;
            case COMPOSITE1: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_COLOR_KEY;
            case COMPOSITE2: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_COLOR_KEY;
            case COMPOSITE3: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_COLOR_KEY;
            default: throw new IllegalArgumentException("Unknown MaskType: " + maskType);
        }
    }

    public static String getTransparencyKey(MaskType maskType) {
        switch (maskType) {
            case WATER: return SeadasReaderDefaults.PROPERTY_MASK_Water_TRANSPARENCY_KEY;
            case SPARE: return SeadasReaderDefaults.PROPERTY_MASK_SPARE_TRANSPARENCY_KEY;
            case COASTZ: return SeadasReaderDefaults.PROPERTY_MASK_COASTZ_TRANSPARENCY_KEY;
            case STRAYLIGHT: return SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_TRANSPARENCY_KEY;
            case CLDICE: return SeadasReaderDefaults.PROPERTY_MASK_CLDICE_TRANSPARENCY_KEY;
            case COCCOLITH: return SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_TRANSPARENCY_KEY;
            case TURBIDW: return SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_TRANSPARENCY_KEY;
            case HISOLZEN: return SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_TRANSPARENCY_KEY;
            case LOWLW: return SeadasReaderDefaults.PROPERTY_MASK_LOWLW_TRANSPARENCY_KEY;
            case CHLFAIL: return SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_TRANSPARENCY_KEY;
            case NAVWARN: return SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_TRANSPARENCY_KEY;
            case ABSAER: return SeadasReaderDefaults.PROPERTY_MASK_ABSAER_TRANSPARENCY_KEY;
            case MAXAERITER: return SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_TRANSPARENCY_KEY;
            case MODGLINT: return SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_TRANSPARENCY_KEY;
            case CHLWARN: return SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_TRANSPARENCY_KEY;
            case ATMWARN: return SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_TRANSPARENCY_KEY;
            case SEAICE: return SeadasReaderDefaults.PROPERTY_MASK_SEAICE_TRANSPARENCY_KEY;
            case NAVFAIL: return SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_TRANSPARENCY_KEY;
            case FILTER: return SeadasReaderDefaults.PROPERTY_MASK_FILTER_TRANSPARENCY_KEY;
            case BOWTIEDEL: return SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_TRANSPARENCY_KEY;
            case HIPOL: return SeadasReaderDefaults.PROPERTY_MASK_HIPOL_TRANSPARENCY_KEY;
            case PRODFAIL: return SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_TRANSPARENCY_KEY;
            case GEOREGION: return SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_TRANSPARENCY_KEY;
            case HIGLINT: return SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_TRANSPARENCY_KEY;
            case HILT: return SeadasReaderDefaults.PROPERTY_MASK_HILT_TRANSPARENCY_KEY;
            case HISATZEN: return SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_TRANSPARENCY_KEY;
            case PRODWARN: return SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_TRANSPARENCY_KEY;
            case LAND: return SeadasReaderDefaults.PROPERTY_MASK_LAND_TRANSPARENCY_KEY;
            case ATMFAIL: return SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_TRANSPARENCY_KEY;
            case COMPOSITE1: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_KEY;
            case COMPOSITE2: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_KEY;
            case COMPOSITE3: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_TRANSPARENCY_KEY;
            default: throw new IllegalArgumentException("Unknown MaskType: " + maskType);
        }
    }



    public static boolean getDefaultBool(MaskType maskType) {
        switch (maskType) {
            case WATER: return SeadasReaderDefaults.PROPERTY_MASK_Water_ENABLED_DEFAULT;
            case SPARE: return SeadasReaderDefaults.PROPERTY_MASK_SPARE_INCLUDE_DEFAULT;
            case COASTZ: return SeadasReaderDefaults.PROPERTY_MASK_COASTZ_ENABLED_DEFAULT;
            case STRAYLIGHT: return SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_ENABLED_DEFAULT;
            case CLDICE: return SeadasReaderDefaults.PROPERTY_MASK_CLDICE_ENABLED_DEFAULT;
            case COCCOLITH: return SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_ENABLED_DEFAULT;
            case TURBIDW: return SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_ENABLED_DEFAULT;
            case HISOLZEN: return SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_ENABLED_DEFAULT;
            case LOWLW: return SeadasReaderDefaults.PROPERTY_MASK_LOWLW_ENABLED_DEFAULT;
            case CHLFAIL: return SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_ENABLED_DEFAULT;
            case NAVWARN: return SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_ENABLED_DEFAULT;
            case ABSAER: return SeadasReaderDefaults.PROPERTY_MASK_ABSAER_ENABLED_DEFAULT;
            case MAXAERITER: return SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_ENABLED_DEFAULT;
            case MODGLINT: return SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_ENABLED_DEFAULT;
            case CHLWARN: return SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_ENABLED_DEFAULT;
            case ATMWARN: return SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_ENABLED_DEFAULT;
            case SEAICE: return SeadasReaderDefaults.PROPERTY_MASK_SEAICE_ENABLED_DEFAULT;
            case NAVFAIL: return SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_ENABLED_DEFAULT;
            case FILTER: return SeadasReaderDefaults.PROPERTY_MASK_FILTER_ENABLED_DEFAULT;
            case BOWTIEDEL: return SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_ENABLED_DEFAULT;
            case HIPOL: return SeadasReaderDefaults.PROPERTY_MASK_HIPOL_ENABLED_DEFAULT;
            case PRODFAIL: return SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_ENABLED_DEFAULT;
            case GEOREGION: return SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_ENABLED_DEFAULT;
            case HIGLINT: return SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_ENABLED_DEFAULT;
            case HILT: return SeadasReaderDefaults.PROPERTY_MASK_HILT_ENABLED_DEFAULT;
            case HISATZEN: return SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_ENABLED_DEFAULT;
            case PRODWARN: return SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_ENABLED_DEFAULT;
            case LAND: return SeadasReaderDefaults.PROPERTY_MASK_LAND_ENABLED_DEFAULT;
            case ATMFAIL: return SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_ENABLED_DEFAULT;
            case COMPOSITE1_INCLUDE: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_INCLUDE_DEFAULT;
            case COMPOSITE1: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_ENABLED_DEFAULT;
            case COMPOSITE2_INCLUDE: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_INCLUDE_DEFAULT;
            case COMPOSITE2: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_ENABLED_DEFAULT;
            case COMPOSITE3: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_ENABLED_DEFAULT;
            case COMPOSITE3_INCLUDE: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_INCLUDE_DEFAULT;
            default: return false;
        }
    }

    public static Color getDefaultColor(MaskType maskType) {
        switch (maskType) {
            case WATER: return SeadasReaderDefaults.PROPERTY_MASK_Water_COLOR_DEFAULT;
            case SPARE: return SeadasReaderDefaults.PROPERTY_MASK_SPARE_COLOR_DEFAULT;
            case COASTZ: return SeadasReaderDefaults.PROPERTY_MASK_COASTZ_COLOR_DEFAULT;
            case STRAYLIGHT: return SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_COLOR_DEFAULT;
            case CLDICE: return SeadasReaderDefaults.PROPERTY_MASK_CLDICE_COLOR_DEFAULT;
            case COCCOLITH: return SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_COLOR_DEFAULT;
            case TURBIDW: return SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_COLOR_DEFAULT;
            case HISOLZEN: return SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_COLOR_DEFAULT;
            case LOWLW: return SeadasReaderDefaults.PROPERTY_MASK_LOWLW_COLOR_DEFAULT;
            case CHLFAIL: return SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_COLOR_DEFAULT;
            case NAVWARN: return SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_COLOR_DEFAULT;
            case ABSAER: return SeadasReaderDefaults.PROPERTY_MASK_ABSAER_COLOR_DEFAULT;
            case MAXAERITER: return SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_COLOR_DEFAULT;
            case MODGLINT: return SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_COLOR_DEFAULT;
            case CHLWARN: return SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_COLOR_DEFAULT;
            case ATMWARN: return SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_COLOR_DEFAULT;
            case SEAICE: return SeadasReaderDefaults.PROPERTY_MASK_SEAICE_COLOR_DEFAULT;
            case NAVFAIL: return SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_COLOR_DEFAULT;
            case FILTER: return SeadasReaderDefaults.PROPERTY_MASK_FILTER_COLOR_DEFAULT;
            case BOWTIEDEL: return SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_COLOR_DEFAULT;
            case HIPOL: return SeadasReaderDefaults.PROPERTY_MASK_HIPOL_COLOR_DEFAULT;
            case PRODFAIL: return SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_COLOR_DEFAULT;
            case GEOREGION: return SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_COLOR_DEFAULT;
            case HIGLINT: return SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_COLOR_DEFAULT;
            case HILT: return SeadasReaderDefaults.PROPERTY_MASK_HILT_COLOR_DEFAULT;
            case HISATZEN: return SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_COLOR_DEFAULT;
            case PRODWARN: return SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_COLOR_DEFAULT;
            case LAND: return SeadasReaderDefaults.PROPERTY_MASK_LAND_COLOR_DEFAULT;
            case ATMFAIL: return SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_COLOR_DEFAULT;
            case COMPOSITE1: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_COLOR_DEFAULT;
            case COMPOSITE2: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_COLOR_DEFAULT;
            case COMPOSITE3: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_COLOR_DEFAULT;
            default: return Color.BLACK;
        }
    }

    public static double getDefaultTransparency(MaskType maskType) {
        switch (maskType) {
            case WATER: return SeadasReaderDefaults.PROPERTY_MASK_Water_TRANSPARENCY_DEFAULT;
            case SPARE: return SeadasReaderDefaults.PROPERTY_MASK_SPARE_TRANSPARENCY_DEFAULT;
            case COASTZ: return SeadasReaderDefaults.PROPERTY_MASK_COASTZ_TRANSPARENCY_DEFAULT;
            case STRAYLIGHT: return SeadasReaderDefaults.PROPERTY_MASK_STRAYLIGHT_TRANSPARENCY_DEFAULT;
            case CLDICE: return SeadasReaderDefaults.PROPERTY_MASK_CLDICE_TRANSPARENCY_DEFAULT;
            case COCCOLITH: return SeadasReaderDefaults.PROPERTY_MASK_COCCOLITH_TRANSPARENCY_DEFAULT;
            case TURBIDW: return SeadasReaderDefaults.PROPERTY_MASK_TURBIDW_TRANSPARENCY_DEFAULT;
            case HISOLZEN: return SeadasReaderDefaults.PROPERTY_MASK_HISOLZEN_TRANSPARENCY_DEFAULT;
            case LOWLW: return SeadasReaderDefaults.PROPERTY_MASK_LOWLW_TRANSPARENCY_DEFAULT;
            case CHLFAIL: return SeadasReaderDefaults.PROPERTY_MASK_CHLFAIL_TRANSPARENCY_DEFAULT;
            case NAVWARN: return SeadasReaderDefaults.PROPERTY_MASK_NAVWARN_TRANSPARENCY_DEFAULT;
            case ABSAER: return SeadasReaderDefaults.PROPERTY_MASK_ABSAER_TRANSPARENCY_DEFAULT;
            case MAXAERITER: return SeadasReaderDefaults.PROPERTY_MASK_MAXAERITER_TRANSPARENCY_DEFAULT;
            case MODGLINT: return SeadasReaderDefaults.PROPERTY_MASK_MODGLINT_TRANSPARENCY_DEFAULT;
            case CHLWARN: return SeadasReaderDefaults.PROPERTY_MASK_CHLWARN_TRANSPARENCY_DEFAULT;
            case ATMWARN: return SeadasReaderDefaults.PROPERTY_MASK_ATMWARN_TRANSPARENCY_DEFAULT;
            case SEAICE: return SeadasReaderDefaults.PROPERTY_MASK_SEAICE_TRANSPARENCY_DEFAULT;
            case NAVFAIL: return SeadasReaderDefaults.PROPERTY_MASK_NAVFAIL_TRANSPARENCY_DEFAULT;
            case FILTER: return SeadasReaderDefaults.PROPERTY_MASK_FILTER_TRANSPARENCY_DEFAULT;
            case BOWTIEDEL: return SeadasReaderDefaults.PROPERTY_MASK_BOWTIEDEL_TRANSPARENCY_DEFAULT;
            case HIPOL: return SeadasReaderDefaults.PROPERTY_MASK_HIPOL_TRANSPARENCY_DEFAULT;
            case PRODFAIL: return SeadasReaderDefaults.PROPERTY_MASK_PRODFAIL_TRANSPARENCY_DEFAULT;
            case GEOREGION: return SeadasReaderDefaults.PROPERTY_MASK_GEOREGION_TRANSPARENCY_DEFAULT;
            case HIGLINT: return SeadasReaderDefaults.PROPERTY_MASK_HIGLINT_TRANSPARENCY_DEFAULT;
            case HILT: return SeadasReaderDefaults.PROPERTY_MASK_HILT_TRANSPARENCY_DEFAULT;
            case HISATZEN: return SeadasReaderDefaults.PROPERTY_MASK_HISATZEN_TRANSPARENCY_DEFAULT;
            case PRODWARN: return SeadasReaderDefaults.PROPERTY_MASK_PRODWARN_TRANSPARENCY_DEFAULT;
            case LAND: return SeadasReaderDefaults.PROPERTY_MASK_LAND_TRANSPARENCY_DEFAULT;
            case ATMFAIL: return SeadasReaderDefaults.PROPERTY_MASK_ATMFAIL_TRANSPARENCY_DEFAULT;
            case COMPOSITE1: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_TRANSPARENCY_DEFAULT;
            case COMPOSITE2: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_TRANSPARENCY_DEFAULT;
            case COMPOSITE3: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_TRANSPARENCY_DEFAULT;
            default: return 0.0;
        }
    }

    public static String getDefaultStrings(MaskType maskType) {
        switch (maskType) {
            case COMPOSITE1_NAME: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_DEFAULT;
            case COMPOSITE1_EXPRESSION: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAGS_DEFAULT;
            case COMPOSITE2_NAME: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_DEFAULT;
            case COMPOSITE2_EXPRESSION: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAGS_DEFAULT;
            case COMPOSITE3_NAME: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_DEFAULT;
            case COMPOSITE3_EXPRESSION: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAGS_DEFAULT;
            default: return null;
        }
    }

    public static String getStringsKey(MaskType maskType) {
        switch (maskType) {
            case COMPOSITE1_NAME: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_NAME_KEY;
            case COMPOSITE1_EXPRESSION: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE1_FLAGS_KEY;
            case COMPOSITE2_NAME: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_NAME_KEY;
            case COMPOSITE2_EXPRESSION: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE2_FLAGS_KEY;
            case COMPOSITE3_NAME: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_NAME_KEY;
            case COMPOSITE3_EXPRESSION: return SeadasReaderDefaults.PROPERTY_MASK_COMPOSITE3_FLAGS_KEY;
            default: return null;
        }
    }



    public enum MaskType {
        WATER, SPARE, COASTZ, STRAYLIGHT, CLDICE, COCCOLITH, TURBIDW,
        HISOLZEN, LOWLW, CHLFAIL, NAVWARN, ABSAER, MAXAERITER, MODGLINT,
        CHLWARN, ATMWARN, SEAICE, NAVFAIL, FILTER, BOWTIEDEL, HIPOL, PRODFAIL, GEOREGION,
        HIGLINT, HILT, HISATZEN, PRODWARN, LAND, ATMFAIL,
        COMPOSITE1, COMPOSITE1_INCLUDE, COMPOSITE1_NAME, COMPOSITE1_EXPRESSION,
        COMPOSITE2, COMPOSITE2_INCLUDE, COMPOSITE2_NAME, COMPOSITE2_EXPRESSION,
        COMPOSITE3, COMPOSITE3_INCLUDE, COMPOSITE3_NAME, COMPOSITE3_EXPRESSION;
    }
}
