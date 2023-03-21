package eu.esa.opt.c2rcc.ancillary;

class InterpolationBorderComputer24H implements InterpolationBorderComputer {

    private double startFileTimeMJD;

    @Override
    public void setInterpolationTimeMJD(double timeMJD) {
        startFileTimeMJD = Math.floor(timeMJD - 0.5);
    }

    @Override
    public double getStartBorderTimeMDJ() {
        return startFileTimeMJD + 0.5;
    }

    @Override
    public double getEndBorderTimeMJD() {
        return getStartBorderTimeMDJ() + 1;
    }

    @Override
    public String getStartAncFilePrefix() {
        return AncillaryCommons.convertToFileNamePräfix(startFileTimeMJD);
    }

    @Override
    public String getEndAncFilePrefix() {
        return AncillaryCommons.convertToFileNamePräfix(startFileTimeMJD + 1);
    }
}
