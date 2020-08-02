package org.anchoranalysis.plugin.image.bean.thumbnail.object;

import io.vavr.control.Either;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.color.ColorIndex;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.DrawObjectsGenerator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.RGBColorBean;
import org.anchoranalysis.io.bean.object.writer.Outline;
import org.anchoranalysis.io.color.ColorIndexModulo;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

@AllArgsConstructor
class DrawOutlineHelper {

    private final RGBColorBean color;
    private final int outlineWidth;
    private final FlattenAndScaler scaler;

    /**
     * Draws the outline of all objects on the background in the color of {@code
     * colorUnselectedObjects}
     *
     * @param backgroundScaled the scaled background
     * @param objectsUnscaled the unscaled objects to draw
     * @return the background-stack with the outline of all objects drawn on it
     * @throws OperationFailedException
     */
    public Stack drawObjects(Stack backgroundScaled, ObjectCollection objectsUnscaled)
            throws OperationFailedException {
        try {
            DisplayStack displayStack = DisplayStack.create(backgroundScaled);
            ObjectCollectionWithProperties objectsScaled =
                    new ObjectCollectionWithProperties(scaler.scaleObjects(objectsUnscaled));

            DrawObjectsGenerator drawOthers =
                    new DrawObjectsGenerator(
                            new Outline(outlineWidth),
                            objectsScaled,
                            Either.right(displayStack),
                            colorsForUnselected());
            return drawOthers.generate();
        } catch (OutputWriteFailedException | CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private ColorIndex colorsForUnselected() {
        return new ColorIndexModulo(new ColorList(color.rgbColor()));
    }
}
