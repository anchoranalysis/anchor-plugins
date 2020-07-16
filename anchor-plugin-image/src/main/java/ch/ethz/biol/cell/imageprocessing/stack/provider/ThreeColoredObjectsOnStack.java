/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.stack.provider;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.color.RGBColor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.io.generator.raster.obj.rgb.DrawObjectsGenerator;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.properties.ObjectCollectionWithProperties;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;

/**
 * Colors three distinct collections of objects in RED, GREEN, BLUE on top of a stack.
 *
 * @author Owen Feehan
 */
public class ThreeColoredObjectsOnStack extends StackProviderRGBFromObjectBase {

    private static final RGBColor COLOR_RED = new RGBColor(255, 0, 0);
    private static final RGBColor COLOR_GREEN = new RGBColor(0, 255, 0);
    private static final RGBColor COLOR_BLUE = new RGBColor(0, 0, 255);

    // START BEAN PROPERTIES
    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objectsRed;

    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objectsBlue;

    @BeanField @OptionalBean @Getter @Setter private ObjectCollectionProvider objectsGreen;
    // END BEAN PROPERTIES

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);

        if (objectsRed == null && objectsBlue == null && objectsGreen == null) {
            throw new BeanMisconfiguredException(
                    "Either objectsRed or objectsBlue or objectsGreen must be non-null");
        }
    }

    @Override
    public Stack create() throws CreateException {

        ColorList colors = new ColorList();

        ObjectCollection objects =
                ObjectCollectionFactory.from(
                        addWithColor(objectsRed, COLOR_RED, colors),
                        addWithColor(objectsGreen, COLOR_GREEN, colors),
                        addWithColor(objectsBlue, COLOR_BLUE, colors));

        DrawObjectsGenerator generator =
                new DrawObjectsGenerator(
                        createDrawer(),
                        new ObjectCollectionWithProperties(objects),
                        Optional.of(maybeFlattenedBackground()),
                        colors);

        try {
            return generator.generate();
        } catch (OutputWriteFailedException e) {
            throw new CreateException(e);
        }
    }

    private Optional<ObjectCollection> addWithColor(
            ObjectCollectionProvider provider, RGBColor color, ColorList colors)
            throws CreateException {
        // If objects were created, we add some corresponding colors
        return OptionalFactory.create(provider)
                .map(objects -> maybeFlattenAddColor(objects, color, colors));
    }

    private ObjectCollection maybeFlattenAddColor(
            ObjectCollection objects, RGBColor color, ColorList colors) {
        colors.addMultiple(color, objects.size());
        return maybeFlatten(objects);
    }
}
