package eu.esa.opt.dataio.flex.header;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FlexProductHeader {

    private String productName;
    private String productType;
    private String startTime;
    private String stopTime;
    private String platformName;
    private String instrumentName;
    private int orbitNumber;
    private String orbitDirection;
    private String processorName;
    private String processorVersion;
    private List<String> dataFileNames;
    private Map<String, String> vendorSpecific;

    public FlexProductHeader() {
        productName = "";
        productType = "";
        startTime = "";
        stopTime = "";
        platformName = "";
        instrumentName = "";
        orbitNumber = -1;
        orbitDirection = "";
        processorName = "";
        processorVersion = "";
        dataFileNames = Collections.emptyList();
        vendorSpecific = Collections.emptyMap();
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public int getOrbitNumber() {
        return orbitNumber;
    }

    public void setOrbitNumber(int orbitNumber) {
        this.orbitNumber = orbitNumber;
    }

    public String getOrbitDirection() {
        return orbitDirection;
    }

    public void setOrbitDirection(String orbitDirection) {
        this.orbitDirection = orbitDirection;
    }

    public String getProcessorName() {
        return processorName;
    }

    public void setProcessorName(String processorName) {
        this.processorName = processorName;
    }

    public String getProcessorVersion() {
        return processorVersion;
    }

    public void setProcessorVersion(String processorVersion) {
        this.processorVersion = processorVersion;
    }

    public List<String> getDataFileNames() {
        return dataFileNames;
    }

    public void setDataFileNames(List<String> dataFileNames) {
        this.dataFileNames = dataFileNames;
    }

    public Map<String, String> getVendorSpecific() {
        return vendorSpecific;
    }

    public void setVendorSpecific(Map<String, String> vendorSpecific) {
        this.vendorSpecific = vendorSpecific;
    }
}
