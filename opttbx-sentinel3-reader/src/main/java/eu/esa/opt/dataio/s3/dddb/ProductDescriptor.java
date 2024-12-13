package eu.esa.opt.dataio.s3.dddb;

import org.esa.snap.core.util.StringUtils;

public class ProductDescriptor {

    private String excludedIds;

    public ProductDescriptor() {
        excludedIds = "";
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
}
