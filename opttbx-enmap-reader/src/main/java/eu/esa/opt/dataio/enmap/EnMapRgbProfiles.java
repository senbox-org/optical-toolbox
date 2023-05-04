package eu.esa.opt.dataio.enmap;

import org.esa.snap.core.datamodel.RGBImageProfile;
import org.esa.snap.core.datamodel.RGBImageProfileManager;

class EnMapRgbProfiles {
    private EnMapRgbProfiles() {
    }

    static void registerRGBProfiles() {
        RGBImageProfileManager manager = RGBImageProfileManager.getInstance();
        manager.addProfile(new RGBImageProfile("EnMAP True Color",
                new String[]{
                        "band_046",
                        "band_028",
                        "band_014"
                },
                new String[]{
                        "ENMAP*",
                        "ENMAP_L*",
                        "",
                }
        ));
        manager.addProfile(new RGBImageProfile("EnMAP VNIR",
                new String[]{
                        "band_071",
                        "band_047",
                        "band_026"
                },
                new String[]{
                        "ENMAP*",
                        "ENMAP_L*",
                        "",
                }
        ));
        manager.addProfile(new RGBImageProfile("EnMAP SWIR",
                new String[]{
                        "band_188",
                        "band_146",
                        "band_103"
                },
                new String[]{
                        "ENMAP*",
                        "ENMAP_L*",
                        "",
                }
        ));
    }
}
