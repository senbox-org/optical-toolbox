package gov.nasa.gsfc.seadas.dataio;

import org.esa.snap.core.datamodel.RGBImageProfile;
import org.esa.snap.core.datamodel.RGBImageProfileManager;

public class L1BPaceRgbProfiles {

    static void registerRGBProfiles() {
        RGBImageProfileManager manager = RGBImageProfileManager.getInstance();

        manager.addProfile(createL1BProfileLogTrueColor());
        manager.addProfile(createL1BProfileTrueColor());
        manager.addProfile(createL1BProfileFalseColor());
        manager.addProfile(createL1BProfileLogFalseColor());
    }

    static RGBImageProfile createL1BProfileTrueColor() {
        return new RGBImageProfile("PACE L1B - True Color",
                new String[]{
                        "rhot_red_654.602",
                        "rhot_blue_554.9926",
                        "rhot_blue_442.28088"
                },
                new String[]{
                        "PaceOCI_L1B",
                        "PACE_OCI*L1B*",
                        "",
                });
    }

    static RGBImageProfile createL1BProfileFalseColor() {
        return new RGBImageProfile("PACE L1B - False Color",
                new String[]{
                        "rhot_red_864.59247",
                        "rhot_red_654.602",
                        "rhot_blue_554.9926"
                },
                new String[]{
                        "PaceOCI_L1B",
                        "PACE_OCI*L1B*",
                        "",
                });
    }

    static RGBImageProfile createL1BProfileLogTrueColor() {
        return new RGBImageProfile("PACE L1B - True Color (log)",
                new String[]{
                        "log(rhot_red_654.602/0.01)/log(1/0.01)",
                        "log(rhot_blue_554.9926/0.01)/log(1/0.01)",
                        "log(rhot_blue_442.28088/0.01)/log(1/0.01)"
                },
                new String[]{
                        "PaceOCI_L1B",
                        "PACE_OCI*L1B*",
                        "",
                });
    }

    static RGBImageProfile createL1BProfileLogFalseColor() {
        return new RGBImageProfile("PACE L1B - False Color (log)",
                new String[]{
                        "log(rhot_red_864.59247/0.01)/log(1/0.01)",
                        "log(rhot_red_654.602/0.01)/log(1/0.01)",
                        "log(rhot_blue_554.9926/0.01)/log(1/0.01)"
                },
                new String[]{
                        "PaceOCI_L1B",
                        "PACE_OCI*L1B*",
                        "",
                });
    }
}
