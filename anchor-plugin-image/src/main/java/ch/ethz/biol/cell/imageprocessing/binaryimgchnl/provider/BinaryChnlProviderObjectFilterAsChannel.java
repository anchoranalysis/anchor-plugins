/* (C)2020 */
package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.object.ObjectFilter;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectCollectionFactory;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.CreateFromEntireChnlFactory;

// Treats the entire binaryimgchnl as an object, and sees if it passes an {@link ObjectFilter}
public class BinaryChnlProviderObjectFilterAsChannel extends BinaryChnlProviderElseBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectFilter filter;
    // END BEAN PROPERTIES

    @Override
    protected boolean condition(Mask chnl) throws CreateException {

        ObjectMask objectMask = CreateFromEntireChnlFactory.createObject(chnl);

        try {
            ObjectCollection objects =
                    filter.filter(
                            ObjectCollectionFactory.from(objectMask),
                            Optional.of(chnl.getDimensions()),
                            Optional.empty());
            return objects.size() == 1;
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
