/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.stack.color;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.anchor.overlay.bean.DrawObject;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.DrawObjectsGenerator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.generator.ColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.HSBColorSetGenerator;
import org.anchoranalysis.io.bean.color.generator.ShuffleColorSetGenerator;
import org.anchoranalysis.io.bean.object.writer.Filled;
import org.anchoranalysis.io.bean.object.writer.Outline;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ColoredObjectsStackCreator {

    public static final ColorSetGenerator DEFAULT_COLOR_SET_GENERATOR =
            new ShuffleColorSetGenerator(new HSBColorSetGenerator());

    /**
     * @param objects
     * @param outline
     * @param outlineWidth
     * @param force2D
     * @param background
     * @param colors list of colors. If null, it is automatically generated.
     * @return
     * @throws CreateException
     */
    public static Stack create(
            ObjectCollection objects,
            boolean outline,
            int outlineWidth,
            boolean force2D,
            DisplayStack background,
            ColorList colors)
            throws CreateException {

        try {

            DrawObject drawObject = outline ? new Outline(outlineWidth, force2D) : new Filled();

            if (colors == null) {
                colors = DEFAULT_COLOR_SET_GENERATOR.generateColors(objects.size());
            }

            DrawObjectsGenerator generator =
                    new DrawObjectsGenerator(
                            drawObject,
                            new ObjectCollectionWithProperties(objects),
                            Optional.of(background),
                            colors);

            return generator.generate();

        } catch (OutputWriteFailedException | OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
