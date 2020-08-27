package org.anchoranalysis.plugin.image.bean.object.provider.segment;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.segment.object.SegmentStackIntoObjects;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.provider.ProviderAsStack;
import lombok.Getter;
import lombok.Setter;

/**
 * Segments a stack into objects.
 * 
 * @author Owen Feehan
 *
 */
public class SegmentStack extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    /** The stack to segment */
    @Getter @Setter @BeanField
    private ProviderAsStack stack;
    
    /** The segmentation procedure. */
    @Getter @Setter @BeanField
    private SegmentStackIntoObjects segment;
    // END BEAN PROPERTIES
    
    @Override
    public ObjectCollection create() throws CreateException {
        try {
            return segment.segment(stack.createAsStack());
        } catch (SegmentationFailedException e) {
            throw new CreateException(e);
        }
    }
}
