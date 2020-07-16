/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.provider.morphological;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalErosion;
import org.anchoranalysis.image.object.morph.accept.AcceptIterationList;
import org.anchoranalysis.image.object.morph.accept.RejectIterationIfAllHigh;
import org.anchoranalysis.image.object.morph.accept.RejectIterationIfLowDisconnected;

/**
 * Erodes each object in the collection, growing bounding-boxes as necessary in relevant dimensions.
 *
 * @author Owen Feehan
 */
public class Erode extends ObjectCollectionProviderMorphological {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean outsideAtThreshold = true;

    @BeanField @Getter @Setter private boolean rejectIterationIfAllLow = false;

    @BeanField @Getter @Setter private boolean rejectIterationIfDisconnected = false;
    // END BEAN PROPERTIES

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (!outsideAtThreshold && getDim() == null) {
            throw new BeanMisconfiguredException(
                    "If outsideAtThreshold==false then dim must be set");
        }
    }

    @Override
    protected ObjectMask applyMorphologicalOperation(ObjectMask object, Optional<Extent> extent)
            throws CreateException {

        AcceptIterationList acceptConditionsDilation = new AcceptIterationList();
        if (rejectIterationIfAllLow) {
            acceptConditionsDilation.add(new RejectIterationIfAllHigh());
        }
        if (rejectIterationIfDisconnected) {
            acceptConditionsDilation.add(new RejectIterationIfLowDisconnected());
        }

        return MorphologicalErosion.createErodedObject(
                object,
                extent,
                isDo3D(),
                getIterations(),
                outsideAtThreshold,
                Optional.of(acceptConditionsDilation));
    }
}
