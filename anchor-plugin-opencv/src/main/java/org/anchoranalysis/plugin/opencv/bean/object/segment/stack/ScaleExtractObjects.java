/*-
 * #%L
 * anchor-plugin-opencv
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.opencv.bean.object.segment.stack;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.plugin.image.bean.object.segment.stack.SegmentedObjects;
import org.anchoranalysis.plugin.image.segment.WithConfidence;

/**
 * Extracts and object-mask from the list and scales.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ScaleExtractObjects {

    public static SegmentedObjects apply(
            SegmentedObjects segmentedObjects, ScaleFactor scaleFactor, Extent extent)
            {
        // Scale back to the needed original resolution
        return scaleObjects(segmentedObjects.asList(), scaleFactor, extent);
    }

    private static SegmentedObjects scaleObjects(
            List<WithConfidence<ObjectMask>> objects, ScaleFactor scaleFactor, Extent extent)
    {
        return new SegmentedObjects( FunctionalList.mapToList(objects, withConfidence -> scale(withConfidence, scaleFactor, extent) ));
    }
    
    private static WithConfidence<ObjectMask> scale(WithConfidence<ObjectMask> withConfidence, ScaleFactor scaleFactor, Extent extent) {
        return withConfidence.map( object -> object.scale(scaleFactor, Optional.of(extent)) );
    }
}
