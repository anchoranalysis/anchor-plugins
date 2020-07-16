/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.obj.intersecting;

import org.anchoranalysis.image.feature.bean.object.pair.FeaturePairObjects;
import org.anchoranalysis.image.feature.bean.object.pair.First;
import org.anchoranalysis.image.feature.bean.object.single.NumberVoxels;
import org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting.FeatureIntersectingObjects;
import org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting.FeatureIntersectingObjectsSingleElement;
import org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting.FeatureIntersectingObjectsThreshold;

class FeatureHelper {

    static final int VALUE_NO_OBJECTS = -1;

    static final int EXPECTED_NUM_PIXELS_FIRST_CIRCLE = 81;

    static final int EXPECTED_NUM_PIXELS_SECOND_CIRCLE = 149;

    static final int EXPECTED_NUM_PIXELS_SECOND_LAST_CIRCLE = 529;

    /**
     * The threshold placed on the number of voxels rejects the smaller (initial) circles before
     * passing the later larger ones
     *
     * <p>Therefore the counts returned in these tests are initially 0 and latterly the same as
     * {#link {@link NumberIntersectingObjectsTest}}
     *
     * @return
     */
    public static FeatureIntersectingObjects createWithThreshold(
            FeatureIntersectingObjectsThreshold feature) {
        feature.setThreshold(90);
        return createWithFeature(feature);
    }

    /**
     * A pair feature (the number-of-voxels of the first object) is set on whatever feature is
     * passed
     *
     * @param feature feature to set new pair featore on as property
     * @return feature with the proprty changed
     */
    public static FeatureIntersectingObjects createWithFeature(
            FeatureIntersectingObjectsSingleElement feature) {
        feature.setItem(createPairFeature());
        feature.setValueNoObjects(VALUE_NO_OBJECTS);
        return feature;
    }

    private static FeaturePairObjects createPairFeature() {
        return new First(new NumberVoxels());
    }
}
