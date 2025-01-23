/**
 * 
 */
package eu.esa.opt.dataio.landsat.geotiff.specs;

import java.awt.Color;
import java.util.Map;

/**
 * Base for all Landsat products specification 
 * 
 * @author Lucian Barbulescu
 */
public abstract class ProductSensorSpecification {

	/**
	 * Get the product specification.
	 * 
	 * @param id product id
	 * @return the product specification
	 */
	public static ProductSensorSpecification getProductSpecification(final String id) {
		switch(id) {
			case "04":
			case "05":
				return new ProductSensorSpecificationL4L5();
			case "07":
				return new ProductSensorSpecificationL7();
			case "08":
			case "09":
			default:
				return new ProductSensorSpecificationL8L9();
		}
	}
	
    /**
     * Create a brighter version of a color.
     * 
     * @param times levels of brightness
     * @param color color to brighten
     * @return the new color
     */
    static Color brighter(int times, Color color) {
        for (int i = 0; i < times; i++) {
            color = color.brighter();
        }
        return color;
    }

    /**
     * Create a darker version of a color.
     * 
     * @param times levels of darkness
     * @param color color to darken
     * @return the new color
     */
    static Color darker(int times, Color color) {
        for (int i = 0; i < times; i++) {
            color = color.darker();
        }
        return color;
    }	
	
	/**
	 * Get the band description
	 * 
	 * @param id band id
	 * @return description
	 */
	public abstract String getBandDescription(final String id); 

	/**
	 * Get the band scaling factor
	 * 
	 * @param id band id
	 * @return scaling factor
	 */
	public abstract Double getScalingFactor(final String id); 	

	/**
	 * Get the band add offset
	 * 
	 * @param id band id
	 * @return add offset
	 */
	public abstract Double getAddOffset(final String id); 	

	/**
	 * Get the band fill value
	 * 
	 * @param id band id
	 * @return fill value
	 */
	public abstract Double getFillValue(final String id); 	

	/**
	 * Get the band unit
	 * 
	 * @param id band id
	 * @return unit
	 */
	public abstract String getUnit(final String id); 	

	/**
	 * Get the band wavelength
	 * 
	 * @param id band id
	 * @return wavelength
	 */
	public abstract Float getWavelength(final String id); 	

	/**
	 * Get the band bandwidth
	 * 
	 * @param id band id
	 * @return bandwidth
	 */
	public abstract Float getBandwidth(final String id); 	
	
	/**
	 * Get the flag coding
	 * 
	 * @param id flag id
	 * @return flag coding
	 */
	public abstract  Map<String, FlagCodingArgs> getFlagCoding(final String id);
}
