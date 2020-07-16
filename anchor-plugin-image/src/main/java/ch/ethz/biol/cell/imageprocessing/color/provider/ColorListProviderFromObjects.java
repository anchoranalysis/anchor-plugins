/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.color.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ColorListProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.io.bean.color.generator.ColorSetGenerator;

public class ColorListProviderFromObjects extends ColorListProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ColorSetGenerator colorSetGenerator;

    @BeanField @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    @Override
    public ColorList create() throws CreateException {

        ObjectCollection objectCollection;
        try {
            objectCollection = objects.create();
        } catch (CreateException e) {
            throw new CreateException(e);
        }

        try {
            return colorSetGenerator.generateColors(objectCollection.size());
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
