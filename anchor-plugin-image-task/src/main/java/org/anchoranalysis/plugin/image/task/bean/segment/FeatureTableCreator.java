/*-
 * #%L
 * anchor-plugin-mpp-experiment
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
package org.anchoranalysis.plugin.image.task.bean.segment;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.store.NamedFeatureStore;
import org.anchoranalysis.feature.store.NamedFeatureStoreFactory;
import org.anchoranalysis.image.feature.bean.object.single.CenterOfGravity;
import org.anchoranalysis.image.feature.bean.object.single.NumberVoxels;
import org.anchoranalysis.image.feature.calculator.FeatureTableCalculator;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.EachObjectIndependently;
import org.anchoranalysis.spatial.axis.Axis;

/**
 * Creates certain hard-coded features to describe a segmented-object.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class FeatureTableCreator {

    private static final NamedFeatureStoreFactory STORE_FACTORY =
            NamedFeatureStoreFactory.parametersOnly();

    /**
     * Creates {@link FeatureTableCalculator} needed for calculating features on the segmented
     * objects.
     *
     * @param features any user-specified features, if they exist.
     * @param combineObjects how objects are combined together, to form the feature-table.
     * @return a newly created {@link FeatureTableCalculator}.
     */
    public static FeatureTableCalculator<FeatureInputSingleObject> tableCalculator(
            Optional<List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>>> features,
            EachObjectIndependently combineObjects)
            throws CreateException {
        if (features.isPresent()) {
            return combineObjects.createFeatures(features.get(), STORE_FACTORY, true);
        } else {
            return combineObjects.createFeatures(FeatureTableCreator.defaultInstanceSegmentation());
        }
    }

    /**
     * Creates default features for describing the results of instance-segmentation.
     *
     * @return the default features.
     */
    private static NamedFeatureStore<FeatureInputSingleObject> defaultInstanceSegmentation() {
        NamedFeatureStore<FeatureInputSingleObject> store = new NamedFeatureStore<>();
        store.add("centerOfGravity.x", new CenterOfGravity(Axis.X));
        store.add("centerOfGravity.y", new CenterOfGravity(Axis.Y));
        store.add("centerOfGravity.z", new CenterOfGravity(Axis.Z));
        store.add("numberVoxels", new NumberVoxels());
        return store;
    }
}
