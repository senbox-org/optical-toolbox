package eu.esa.opt.dataio.avhrr.noaa.pod;

import eu.esa.opt.dataio.avhrr.AvhrrConstants;

/**
* @author Ralf Quast
*/
class RadianceCalibratorFactory extends AbstractCalibratorFactory {

    RadianceCalibratorFactory(int channelIndex, CalibrationCoefficientsProvider provider) {
        super(channelIndex, provider);
    }

    @Override
    public String getBandName() {
        return "radiance_" + (getChannelIndex() + 1);
    }

    @Override
    public String getBandUnit() {
        return AvhrrConstants.IR_RADIANCE_UNIT;
    }

    @Override
    public String getBandDescription() {
        return "IR radiance";
    }
}
