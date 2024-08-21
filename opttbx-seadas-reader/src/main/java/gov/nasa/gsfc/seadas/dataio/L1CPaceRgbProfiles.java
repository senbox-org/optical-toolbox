package gov.nasa.gsfc.seadas.dataio;

import org.esa.snap.core.datamodel.RGBImageProfile;
import org.esa.snap.core.datamodel.RGBImageProfileManager;

public class L1CPaceRgbProfiles {

    static void registerRGBProfiles() {
        RGBImageProfileManager manager = RGBImageProfileManager.getInstance();

        manager.addProfile(createL1CProfileLogTrueColor());
        manager.addProfile(createL1CProfileTrueColor());
        manager.addProfile(createL1CProfileFalseColor());
        manager.addProfile(createL1CProfileLogFalseColor());
    }

    static RGBImageProfile createL1CProfileTrueColor() {
        return new RGBImageProfile("PACE L1C - True Color",
                new String[]{
                        "i_20_654",
                        "i_20_555",
                        "i_20_442"
                },
                new String[]{
                        "Pace_L1C",
                        "PACE_OCI*L1C*",
                        "",
                });
    }

    static RGBImageProfile createL1CProfileFalseColor() {
        return new RGBImageProfile("PACE L1C - False Color",
                new String[]{
                        "i_20_864",
                        "i_20_654",
                        "i_20_555"
                },
                new String[]{
                        "Pace_L1C",
                        "PACE_OCI*L1C*",
                        "",
                });
    }

    static RGBImageProfile createL1CProfileLogTrueColor() {
        return new RGBImageProfile("PACE L1C - True Color (log)",
                new String[]{
                        "log(i_20_654/0.01)/log(1/0.01)",
                        "log(i_20_555/0.01)/log(1/0.01)",
                        "log(i_20_442/0.01)/log(1/0.01)"
                },
                new String[]{
                        "Pace_L1C",
                        "PACE_OCI*L1C*",
                        "",
                });
    }

    static RGBImageProfile createL1CProfileLogFalseColor() {
        return new RGBImageProfile("PACE L1C - False Color (log)",
                new String[]{
                        "log(i_20_864/0.01)/log(1/0.01)",
                        "log(i_20_654/0.01)/log(1/0.01)",
                        "log(i_20_555/0.01)/log(1/0.01)"
                },
                new String[]{
                        "Pace_L1C",
                        "PACE_OCI*L1C*",
                        "",
                });
    }
}
