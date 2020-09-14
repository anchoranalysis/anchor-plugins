package org.anchoranalysis.plugin.image.bean.object.segment.stack;

import java.util.List;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.scale.AccessObjectMask;
import org.anchoranalysis.image.object.scale.Scaler;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Provides access for the {@link Scaler} to the object-representation of {@code WithConfidence<ObjectMask>}.
 * 
 * @author Owen Feehan
 *
 */
@AllArgsConstructor(access=AccessLevel.PUBLIC) class AccessSegmentedObjects implements AccessObjectMask<WithConfidence<ObjectMask>> {

    private final List<WithConfidence<ObjectMask>> listUnscaled;
    
    @Override
    public ObjectMask objectFor(WithConfidence<ObjectMask> element) {
        return element.getElement();
    }

    @Override
    public WithConfidence<ObjectMask> shiftBy(WithConfidence<ObjectMask> element,
            ReadableTuple3i quantity) {
        return element.map( existingObject->existingObject.shiftBy(quantity) );
    }

    @Override
    public WithConfidence<ObjectMask> clipTo(WithConfidence<ObjectMask> element,
            Extent extent) {
        return element.map( existingObject->existingObject.clipTo(extent) );
    }

    @Override
    public WithConfidence<ObjectMask> createFrom(int index, ObjectMask object) {
        return listUnscaled.get(index).map( existingObject->object );
    }
    
}