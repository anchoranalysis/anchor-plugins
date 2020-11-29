/*-
 * #%L
 * anchor-plugin-image
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
package org.anchoranalysis.plugin.image.bean.object.segment.stack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.object.scale.ScaledElements;
import org.anchoranalysis.image.core.object.scale.Scaler;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.WithConfidence;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Objects that are a result of an instance-segmentation.
 *
 * <p>Unlike a {@link ObjectCollection}, each object also has a confidence score.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor(access = AccessLevel.PUBLIC)
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
     * @return a segmented-objects with identical order, confidence-values etc. but with
     *     corresponding object-masks scaled.
     * @throws OperationFailedException
     */
    public SegmentedObjects scale(ScaleFactor scaleFactor, Extent extent)
            throws OperationFailedException {
        ScaledElements<WithConfidence<ObjectMask>> listScaled =
                Scaler.scaleElements(list, scaleFactor, extent, new AccessSegmentedObjects(list));
        return new SegmentedObjects(listScaled.asListOrderPreserved(list));
    }

    public Optional<WithConfidence<ObjectMask>> highestConfidence() {
        return list.stream().max((a, b) -> Double.compare(a.getConfidence(), b.getConfidence()));
    }

    public ObjectCollection asObjects() {
        return new ObjectCollection(asList().stream().map(WithConfidence::getElement));
    }

    public List<WithConfidence<ObjectMask>> asList() {
        return list;
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
}
