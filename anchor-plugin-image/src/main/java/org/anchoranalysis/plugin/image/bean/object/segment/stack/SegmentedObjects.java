package org.anchoranalysis.plugin.image.bean.object.segment.stack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
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
    
    public SegmentedObjects scale(ScaleFactor scaleFactor, Optional<Extent> extent) {
        return new SegmentedObjects(
             FunctionalList.mapToList(list, object -> scaleObject(object, scaleFactor, extent) ) 
        );
    }
    
    public ObjectCollection asObjects() {
        return new ObjectCollection( asList().stream().map(WithConfidence::getObject) );
    }
    
    public List<WithConfidence<ObjectMask>> asList() {
        return list;
    }
    
    private static WithConfidence<ObjectMask> scaleObject( WithConfidence<ObjectMask> objectWithConfidence, ScaleFactor scaleFactor, Optional<Extent> extent ) {
        return objectWithConfidence.map( object->
                object.scale(scaleFactor, extent)
        );
    }
}
