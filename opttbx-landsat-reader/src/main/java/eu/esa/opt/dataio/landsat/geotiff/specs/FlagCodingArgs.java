/**
 * 
 */
package eu.esa.opt.dataio.landsat.geotiff.specs;

import java.awt.Color;

/**
 *  
 */
public class FlagCodingArgs {

    final int flagMask;
    final Integer flagValue;
    final String name;
    final String description;
    private Color color;
    private float transparency;

    FlagCodingArgs(int flagMask, String name, String description, Color color, float transparency) {
        this(flagMask, null, name, description, color, transparency);
    }

    FlagCodingArgs(int flagMask, Integer flagValue, String name, String description, Color color, float transparency) {
        this.flagMask = flagMask;
        this.flagValue = flagValue;
        this.name = name;
        this.description = description;
        this.color = color;
        this.transparency = transparency;
    }

    public boolean hasFlagValue() {
        return flagValue != null;
    }

    public int getFlagMask() {
        return flagMask;
    }

    public int getFlagValue() {
        return flagValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Color getColor() {
        return color;
    }

    public float getTransparency() {
        return transparency;
    }
}
