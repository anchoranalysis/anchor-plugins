package org.anchoranalysis.plugin.image.thumbnail;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.stack.DisplayStack;

/**
 * Creates thumbnails for a particular batch of objects that can be calibrated to have similar properties (identical scale etc.)
 * 
 * @author Owen Feehan
 * @param <T> 
 */
@FunctionalInterface
public interface ThumbnailBatch<T> {

    /** Creates a thumbnail for an element in the batch */
    DisplayStack thumbnailFor(T element) throws CreateException;
}
