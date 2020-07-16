/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.stack.provider;

import ch.ethz.biol.cell.imageprocessing.stack.color.ColoredObjectsStackCreator;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ColorListProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.bean.color.generator.ColorSetGenerator;

public class ColoredObjectsOnStack extends StackProviderRGBFromObjectBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;

    @BeanField @OptionalBean @Getter @Setter
    private ColorListProvider colorListProvider; // If null, uses the colorListGenerator below

    // Fallback generator if colorListProvider is null
    @BeanField @Getter @Setter
    private ColorSetGenerator colorSetGenerator =
            ColoredObjectsStackCreator.DEFAULT_COLOR_SET_GENERATOR;
    // END BEAN PROPERTIES

    @Override
    public Stack create() throws CreateException {

        ObjectCollection objectCollection = objects.create();
        return createStack(objectCollection, colors(objectCollection.size()));
    }

    private ColorList colors(int size) throws CreateException {
        Optional<ColorList> colorList = OptionalFactory.create(colorListProvider);

        if (!colorList.isPresent()) {
            try {
                return colorSetGenerator.generateColors(size);
            } catch (OperationFailedException e) {
                throw new CreateException(e);
            }
        }

        return colorList.get();
    }
}
