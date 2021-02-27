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

package org.anchoranalysis.plugin.image.bean.object.provider;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.object.scale.ScaledElements;
import org.anchoranalysis.image.core.object.scale.Scaler;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.scale.ScaleFactor;

/**
 * Scales all the objects in the collection by a particular scale-factor.
 *
 * <p>Note that the order of object is not perserved in the scaled object-collection.
 *
 * @author Owen Feehan
 */
public class Scale extends WithDimensionsBase {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ScaleCalculator scaleCalculator;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {

        Dimensions dimensions = createDimensions();

        ScaleFactor factor = calculateFactor(dimensions);
        return scaleObjects(objects, factor, dimensions.extent());
    }

    private ScaleFactor calculateFactor(Dimensions dimensions) throws CreateException {
        try {
            return scaleCalculator.calculate(
                    Optional.of(dimensions), getInitialization().getSuggestedResize());
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }

    private ObjectCollection scaleObjects(
            ObjectCollection objects, ScaleFactor factor, Extent extent) throws CreateException {
        try {
            ScaledElements<ObjectMask> scaledObjects = Scaler.scaleObjects(objects, factor, extent);
            return ObjectCollectionFactory.of(scaledObjects.asCollectionOrderNotPreserved());
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
