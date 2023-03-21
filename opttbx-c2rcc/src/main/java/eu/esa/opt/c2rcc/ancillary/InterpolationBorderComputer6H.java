package eu.esa.opt.c2rcc.ancillary;

class InterpolationBorderComputer6H implements InterpolationBorderComputer {

    private double startFileTimeMJD;

    @Override
    public void setInterpolationTimeMJD(double timeMJD) {
        startFileTimeMJD = Math.floor((timeMJD - 0.125) * 4) * 0.25;
    }

    @Override
    public double getStartBorderTimeMDJ() {
        return startFileTimeMJD + 0.125;
    }

    @Override
    public double getEndBorderTimeMJD() {
        return getStartBorderTimeMDJ() + 0.25;
    }

    @Override
    public String getStartAncFilePrefix() {
        return AncillaryCommons.convertToFileNamePräfix(startFileTimeMJD);
    }

    @Override
    public String getEndAncFilePrefix() {
        return AncillaryCommons.convertToFileNamePräfix(startFileTimeMJD + 0.25);
    }

}
