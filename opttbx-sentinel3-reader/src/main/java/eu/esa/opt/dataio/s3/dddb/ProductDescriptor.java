package eu.esa.opt.dataio.s3.dddb;

import org.esa.snap.core.util.StringUtils;

public class ProductDescriptor {

    private String excludedIds;
    private String widthXPath;
    private String heightXPath;
    private int width;
    private int height;

    public ProductDescriptor() {
        excludedIds = "";
        width = -1;
        height = -1;
    }

    public String getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(String excludedIds) {
        this.excludedIds = excludedIds;
    }

    public String[] getExcludedIdsAsArray() {
        if (excludedIds.isEmpty()) {
            return new String[0];
        } else if (excludedIds.indexOf(',') <= 0) {
            return new String[]{excludedIds};
        }

        return StringUtils.split(excludedIds, new char[]{','}, true);
    }

    public String getWidthXPath() {
        return widthXPath;
    }

    public void setWidthXPath(String widthXPath) {
        this.widthXPath = widthXPath;
    }

    public String getHeightXPath() {
        return heightXPath;
    }

    public void setHeightXPath(String heightXPath) {
        this.heightXPath = heightXPath;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
