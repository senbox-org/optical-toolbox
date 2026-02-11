package eu.esa.opt.dataio.s3.dddb;

public class FlagMask {

    private String name;
    private String expression;
    private String description;

    public FlagMask(String name, String expression, String description) {
        this.name = name;
        this.expression = expression;
        this.description = description;
    }

    public FlagMask() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
