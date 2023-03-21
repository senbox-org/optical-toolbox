package eu.esa.opt.c2rcc.ancillary;

interface InterpolationBorderComputer {

    void setInterpolationTimeMJD(double timeMJD);

    double getStartBorderTimeMDJ();

    double getEndBorderTimeMJD();

    String getStartAncFilePrefix();

    String getEndAncFilePrefix();

}
