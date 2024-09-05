package eu.esa.opt.dataio.s3.slstr.dddb;

import com.fasterxml.jackson.annotation.JsonSetter;

public class VariableInformation {

    String name;
    String data_type;
    String width;
    String height;
    String source_file;

    public VariableInformation() {
    }

    public String getName() {
        return name;
    }

    @JsonSetter("name")
    public void setName(String name) {
        this.name = name;
    }

    public String getData_type() {
        return data_type;
    }

    @JsonSetter("data_type")
    public void setData_type(String data_type) {
        this.data_type = data_type;
    }

    public String getWidth() {
        return width;
    }

    @JsonSetter("width")
    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    @JsonSetter("height")
    public void setHeight(String height) {
        this.height = height;
    }

    public String getSource_file() {
        return source_file;
    }

    @JsonSetter("source_file")
    public void setSource_file(String source_file) {
        this.source_file = source_file;
    }
}
