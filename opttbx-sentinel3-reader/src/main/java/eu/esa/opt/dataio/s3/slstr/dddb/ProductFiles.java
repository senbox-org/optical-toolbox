package eu.esa.opt.dataio.s3.slstr.dddb;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class ProductFiles {

    private String[] productFileNames;

    public ProductFiles() {
        productFileNames = new String[0];
    }

    public ProductFiles(String[] productFileNames) {
        this.productFileNames = productFileNames;
    }

    @JsonSetter("product_files")
    public void setProductFileNames(String[] productNames) {
        this.productFileNames = productNames;
    }

    @JsonGetter("product_files")
    public String[] getProductFileNames() {
        return productFileNames;
    }
}
