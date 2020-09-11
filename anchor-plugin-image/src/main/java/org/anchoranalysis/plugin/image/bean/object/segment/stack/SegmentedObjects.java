package org.anchoranalysis.plugin.image.bean.object.segment.stack;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.scale.ScaledElements;
import org.anchoranalysis.image.object.scale.Scaler;
import org.anchoranalysis.image.scale.ScaleFactor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Objects that are a result of an instance-segmentation.
 * 
 * <p>Unlike a {@link ObjectCollection}, each object also has a confidence score.
 * 
 * @author Owen Feehan
 *
 */
@AllArgsConstructor(access=AccessLevel.PUBLIC)
public class SegmentedObjects {

    private final List<WithConfidence<ObjectMask>> list;

    public SegmentedObjects() {
        list = new ArrayList<>();
    }
    
    /**
     * Scales the segmented-objects.
     * 
     * @param scaleFactor how much to scale by
     * @param extent an extent all objects are clipped to remain inside.
     * @return a segmented-objects with identical order, confidence-values etc. but with corresponding object-masks scaled.
     * @throws OperationFailedException
     */
    public SegmentedObjects scale(ScaleFactor scaleFactor, Extent extent) throws OperationFailedException {
        ScaledElements<WithConfidence<ObjectMask>> listScaled = Scaler.scaleElements(list, scaleFactor, extent, new AccessSegmentedObjects(list));
        return new SegmentedObjects(listScaled.asListOrderPreserved(list));
    }
    
    public ObjectCollection asObjects() {
        return new ObjectCollection( asList().stream().map(WithConfidence::getObject) );
    }
    
    public List<WithConfidence<ObjectMask>> asList() {
        return list;
    }
}
