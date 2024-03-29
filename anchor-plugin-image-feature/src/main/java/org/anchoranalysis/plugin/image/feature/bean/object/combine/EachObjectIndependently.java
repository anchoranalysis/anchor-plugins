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

package org.anchoranalysis.plugin.image.feature.bean.object.combine;

import java.util.List;
import lombok.NoArgsConstructor;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.store.NamedFeatureStore;
import org.anchoranalysis.feature.store.NamedFeatureStoreFactory;
import org.anchoranalysis.image.bean.interpolator.Interpolator;
import org.anchoranalysis.image.feature.calculator.FeatureTableCalculator;
import org.anchoranalysis.image.feature.calculator.SingleTableCalculator;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Simply selects features directly from the list, and objects directly from the list passed.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public class EachObjectIndependently extends CombineObjectsForFeatures<FeatureInputSingleObject> {

    /**
     * Create with a specific interpolator.
     *
     * @param interpolator interpolator used to resize images in thumbnail generation.
     */
    public EachObjectIndependently(Interpolator interpolator) {
        super(interpolator);
    }

    @Override
    public FeatureTableCalculator<FeatureInputSingleObject> createFeatures(
            List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> list,
            NamedFeatureStoreFactory storeFactory,
            boolean suppressErrors)
            throws CreateException {
        try {
            return createFeatures(storeFactory.createNamedFeatureList(list));
        } catch (ProvisionFailedException e) {
            throw new CreateException(e);
        }
    }

    public FeatureTableCalculator<FeatureInputSingleObject> createFeatures(
            NamedFeatureStore<FeatureInputSingleObject> features) {
        return new SingleTableCalculator(features);
    }

    @Override
    public String uniqueIdentifierFor(FeatureInputSingleObject input) {
        return UniqueIdentifierUtilities.forObject(input.getObject());
    }

    @Override
    public List<FeatureInputSingleObject> startBatchDeriveInputs(
            ObjectCollection objects, EnergyStack energyStack, Logger logger)
            throws CreateException {
        return objects.stream()
                .mapToList(
                        object ->
                                new FeatureInputSingleObject(
                                        checkObjectInsideScene(object, energyStack.extent()),
                                        energyStack));
    }

    @Override
    public ObjectCollection objectsForThumbnail(FeatureInputSingleObject input)
            throws CreateException {
        return ObjectCollectionFactory.of(input.getObject());
    }

    @Override
    protected BoundingBox boundingBoxThatSpansInput(FeatureInputSingleObject input) {
        return input.getObject().boundingBox();
    }

    private ObjectMask checkObjectInsideScene(ObjectMask object, Extent extent)
            throws CreateException {
        if (!extent.contains(object.boundingBox())) {
            throw new CreateException(
                    String.format(
                            "Object is not (perhaps fully) contained inside the scene: %s is not in %s",
                            object.boundingBox(), extent));
        }
        return object;
    }
}
