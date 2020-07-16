/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.morphological;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalDilation;

/**
 * Dilates each object in the collection, growing bounding-boxes as necessary in relevant
 * dimensions.
 *
 * @author Owen Feehan
 */
public class Dilate extends ObjectCollectionProviderMorphological {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean bigNeighborhood = false;
    // END BEAN PROPERTIES

    @Override
    protected ObjectMask applyMorphologicalOperation(ObjectMask object, Optional<Extent> extent)
            throws CreateException {
        return MorphologicalDilation.createDilatedObject(
                object, extent, isDo3D(), getIterations(), bigNeighborhood);
    }
}
