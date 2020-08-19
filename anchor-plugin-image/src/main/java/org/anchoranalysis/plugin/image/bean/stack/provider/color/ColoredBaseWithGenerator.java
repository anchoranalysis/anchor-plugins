package org.anchoranalysis.plugin.image.bean.stack.provider.color;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.io.bean.color.generator.ColorSetGenerator;
import org.anchoranalysis.plugin.image.object.ColoredObjectCollection;
import lombok.Getter;
import lombok.Setter;

/**
 * Like {@link ColoredBase} but uses a generator to determine the colors for the objects
 * 
 * @author Owen Feehan
 *
 */
public abstract class ColoredBaseWithGenerator extends ColoredBase {

    // START BEAN PROPERTIES
    /** Colors to use for drawing objects */
    @BeanField @Getter @Setter
    private ColorSetGenerator colors = DEFAULT_COLOR_SET_GENERATOR;
    // END BEAN PROPERTIES
    
    @Override
    protected ColoredObjectCollection coloredObjectsToDraw(ImageDimensions backgroundDimensions) throws CreateException {
        return addColors(objectsToDraw(backgroundDimensions));
    }
    
    /** 
     * The objects to draw (without any colors) on the background
     * 
     * @param backgroundDimensions the dimensions of the background
     * @return the objects to be drawn on the background
     * */
    protected abstract ObjectCollection objectsToDraw(ImageDimensions backgroundDimensions) throws CreateException;

    private ColoredObjectCollection addColors(ObjectCollection objectsCreated) throws CreateException {
        try {
            ColorList colorsGenerated = colors.generateColors(objectsCreated.size());
            return new ColoredObjectCollection(objectsCreated, colorsGenerated);
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
