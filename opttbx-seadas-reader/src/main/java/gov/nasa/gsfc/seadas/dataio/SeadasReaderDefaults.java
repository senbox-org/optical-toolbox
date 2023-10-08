package gov.nasa.gsfc.seadas.dataio;

import org.esa.snap.core.datamodel.Mask;

import java.awt.*;

public class SeadasReaderDefaults {


    final static Color LandBrown = new Color(100, 49, 12);
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


    // Preferences property prefix
    private static final String PROPERTY_SEADAS_READER_ROOT_KEY = "opt.toolbox.seadas.reader";

    


    // ATMFAIL

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
    public static final Color PROPERTY_MASK_ATMFAIL_COLOR_DEFAULT = FailRed;


    
    

    // LAND

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
    public static final Color PROPERTY_MASK_LAND_COLOR_DEFAULT = Color.BLACK;


    
    
    // PRODWARN

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
    public static final Color PROPERTY_MASK_PRODWARN_COLOR_DEFAULT = DeepBlue;



    // HIGLINT

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
    public static final Color PROPERTY_MASK_HIGLINT_COLOR_DEFAULT = BrightPink;

    

    // HILT

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
    public static final Color PROPERTY_MASK_HILT_COLOR_DEFAULT = Color.GRAY;

    
    

    // HISATZEN

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
    public static final Color PROPERTY_MASK_HISATZEN_COLOR_DEFAULT = LightCyan;

    
    
    // COASTZ

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
    public static final Color PROPERTY_MASK_COASTZ_COLOR_DEFAULT = BurntUmber;

    


    // STRAYLIGHT

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
    public static final Color PROPERTY_MASK_STRAYLIGHT_COLOR_DEFAULT = Color.YELLOW;



    // CLDICE

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
    public static final Color PROPERTY_MASK_CLDICE_COLOR_DEFAULT = Color.WHITE;




    // COCCOLITH

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
    public static final Color PROPERTY_MASK_COCCOLITH_COLOR_DEFAULT = Color.CYAN;

    
    
    // TURBIDW

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
    public static final Color PROPERTY_MASK_TURBIDW_COLOR_DEFAULT = LightBrown;

    
    
    // HISOLZEN

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
    public static final Color PROPERTY_MASK_HISOLZEN_COLOR_DEFAULT = Purple;

    
    
    // LOWLW

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
    public static final Color PROPERTY_MASK_LOWLW_COLOR_DEFAULT = Cornflower;

    
    
    // CHLFAIL

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
    public static final Color PROPERTY_MASK_CHLFAIL_COLOR_DEFAULT = FailRed;



    // NAVWARN

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
    public static final Color PROPERTY_MASK_NAVWARN_COLOR_DEFAULT = Color.MAGENTA;




    // ABSAER

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
    public static final Color PROPERTY_MASK_ABSAER_COLOR_DEFAULT = Color.ORANGE;




    // MAXAERITER

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
    public static final Color PROPERTY_MASK_MAXAERITER_COLOR_DEFAULT = MediumGray;




    // MODGLINT

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
    public static final Color PROPERTY_MASK_MODGLINT_COLOR_DEFAULT = LightPurple;




    // CHLWARN

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
    public static final Color PROPERTY_MASK_CHLWARN_COLOR_DEFAULT = Color.LIGHT_GRAY;



    // ATMWARN

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
    public static final Color PROPERTY_MASK_ATMWARN_COLOR_DEFAULT = Color.MAGENTA;




    // SEAICE

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
    public static final Color PROPERTY_MASK_SEAICE_COLOR_DEFAULT = Color.DARK_GRAY;



    // NAVFAIL

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
    public static final Color PROPERTY_MASK_NAVFAIL_COLOR_DEFAULT = FailRed;



    // FILTER

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
    public static final Color PROPERTY_MASK_FILTER_COLOR_DEFAULT = Color.LIGHT_GRAY;



    // BOWTIEDEL

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
    public static final Color PROPERTY_MASK_BOWTIEDEL_COLOR_DEFAULT = FailRed;



    // HIPOL

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
    public static final Color PROPERTY_MASK_HIPOL_COLOR_DEFAULT = Color.PINK;




    // PRODFAIL

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
    public static final Color PROPERTY_MASK_PRODFAIL_COLOR_DEFAULT = FailRed;



    
    // Quality_L2

    public static final String PROPERTY_MASK_Quality_L2_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.Quality_L2";

    public static final String PROPERTY_MASK_Quality_L2_SECTION_KEY = PROPERTY_MASK_Quality_L2_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_Quality_L2_SECTION_LABEL = "L2 Flag Mask: Quality_L2";
    public static final String PROPERTY_MASK_Quality_L2_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask Quality_L2";

    public static final String PROPERTY_MASK_Quality_L2_ENABLED_KEY = PROPERTY_MASK_Quality_L2_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_Quality_L2_ENABLED_LABEL = "Quality_L2: Set as Selected";
    public static final String PROPERTY_MASK_Quality_L2_ENABLED_TOOLTIP = "Set Quality_L2 mask as selected by default";
    public static final boolean PROPERTY_MASK_Quality_L2_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_Quality_L2_TRANSPARENCY_KEY = PROPERTY_MASK_Quality_L2_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_Quality_L2_TRANSPARENCY_LABEL = "Quality_L2: Transparency";
    public static final String PROPERTY_MASK_Quality_L2_TRANSPARENCY_TOOLTIP = "Set default transparency of the Quality_L2 mask";
    public static final double PROPERTY_MASK_Quality_L2_TRANSPARENCY_DEFAULT = 0.0;

    public static final String PROPERTY_MASK_Quality_L2_COLOR_KEY = PROPERTY_MASK_Quality_L2_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_Quality_L2_COLOR_LABEL = "Quality_L2: Color";
    public static final String PROPERTY_MASK_Quality_L2_COLOR_TOOLTIP = "Set default color of the Quality_L2 mask";
    public static final Color PROPERTY_MASK_Quality_L2_COLOR_DEFAULT = DarkGreen;


    
    // Quality_L3

    public static final String PROPERTY_MASK_Quality_L3_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.Quality_L3";

    public static final String PROPERTY_MASK_Quality_L3_SECTION_KEY = PROPERTY_MASK_Quality_L3_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_Quality_L3_SECTION_LABEL = "L2 Flag Mask: Quality_L3";
    public static final String PROPERTY_MASK_Quality_L3_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask Quality_L3";

    public static final String PROPERTY_MASK_Quality_L3_ENABLED_KEY = PROPERTY_MASK_Quality_L3_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_Quality_L3_ENABLED_LABEL = "Quality_L3: Set as Selected";
    public static final String PROPERTY_MASK_Quality_L3_ENABLED_TOOLTIP = "Set Quality_L3 mask as selected by default";
    public static final boolean PROPERTY_MASK_Quality_L3_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_Quality_L3_TRANSPARENCY_KEY = PROPERTY_MASK_Quality_L3_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_Quality_L3_TRANSPARENCY_LABEL = "Quality_L3: Transparency";
    public static final String PROPERTY_MASK_Quality_L3_TRANSPARENCY_TOOLTIP = "Set default transparency of the Quality_L3 mask";
    public static final double PROPERTY_MASK_Quality_L3_TRANSPARENCY_DEFAULT = 0.1;

    public static final String PROPERTY_MASK_Quality_L3_COLOR_KEY = PROPERTY_MASK_Quality_L3_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_Quality_L3_COLOR_LABEL = "Quality_L3: Color";
    public static final String PROPERTY_MASK_Quality_L3_COLOR_TOOLTIP = "Set default color of the Quality_L3 mask";
    public static final Color PROPERTY_MASK_Quality_L3_COLOR_DEFAULT = Color.GREEN;




    // Water

    public static final String PROPERTY_MASK_Water_ROOT_KEY = PROPERTY_SEADAS_READER_ROOT_KEY + ".mask.Water";

    public static final String PROPERTY_MASK_Water_SECTION_KEY = PROPERTY_MASK_Water_ROOT_KEY + ".section";
    public static final String PROPERTY_MASK_Water_SECTION_LABEL = "L2 Flag Mask: Water";
    public static final String PROPERTY_MASK_Water_SECTION_TOOLTIP = "SeaDAS Level-2 reader options for mask Water";

    public static final String PROPERTY_MASK_Water_ENABLED_KEY = PROPERTY_MASK_Water_ROOT_KEY + ".show";
    public static final String PROPERTY_MASK_Water_ENABLED_LABEL = "Water: Set as Selected";
    public static final String PROPERTY_MASK_Water_ENABLED_TOOLTIP = "Set Water mask as selected by default";
    public static final boolean PROPERTY_MASK_Water_ENABLED_DEFAULT = false;

    public static final String PROPERTY_MASK_Water_TRANSPARENCY_KEY = PROPERTY_MASK_Water_ROOT_KEY + ".transparency";
    public static final String PROPERTY_MASK_Water_TRANSPARENCY_LABEL = "Water: Transparency";
    public static final String PROPERTY_MASK_Water_TRANSPARENCY_TOOLTIP = "Set default transparency of the Water mask";
    public static final double PROPERTY_MASK_Water_TRANSPARENCY_DEFAULT = 0.1;

    public static final String PROPERTY_MASK_Water_COLOR_KEY = PROPERTY_MASK_Water_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_Water_COLOR_LABEL = "Water: Color";
    public static final String PROPERTY_MASK_Water_COLOR_TOOLTIP = "Set default color of the Water mask";
    public static final Color PROPERTY_MASK_Water_COLOR_DEFAULT = Color.BLUE;



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
    public static final double PROPERTY_MASK_SPARE_TRANSPARENCY_DEFAULT = 0.1;

    public static final String PROPERTY_MASK_SPARE_COLOR_KEY = PROPERTY_MASK_SPARE_ROOT_KEY + ".color";
    public static final String PROPERTY_MASK_SPARE_COLOR_LABEL = "SPARE(s): Color";
    public static final String PROPERTY_MASK_SPARE_COLOR_TOOLTIP = "Set default color of the SPARE mask";
    public static final Color PROPERTY_MASK_SPARE_COLOR_DEFAULT = FailRed;
    


    // Property Setting: Restore Defaults

    private static final String PROPERTY_RESTORE_KEY_SUFFIX = PROPERTY_SEADAS_READER_ROOT_KEY + ".restore.defaults";

    public static final String PROPERTY_RESTORE_SECTION_KEY = PROPERTY_RESTORE_KEY_SUFFIX + ".section";
    public static final String PROPERTY_RESTORE_SECTION_LABEL = "Restore";
    public static final String PROPERTY_RESTORE_SECTION_TOOLTIP = "Restores preferences to the package defaults";

    public static final String PROPERTY_RESTORE_DEFAULTS_KEY = PROPERTY_RESTORE_KEY_SUFFIX + ".apply";
    public static final String PROPERTY_RESTORE_DEFAULTS_LABEL = "Default (SeaDAS Toolbox Preferences)";
    public static final String PROPERTY_RESTORE_DEFAULTS_TOOLTIP = "Restore all color bar legend preferences to the original default";
    public static final boolean PROPERTY_RESTORE_DEFAULTS_DEFAULT = false;


}
