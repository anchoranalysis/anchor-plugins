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

package org.anchoranalysis.plugin.image.bean.object.filter.independent;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.error.UnitValueException;
import org.anchoranalysis.image.bean.unitvalue.extent.volume.UnitValueVolume;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.math.relation.DoubleBiPredicate;
import org.anchoranalysis.plugin.image.bean.object.filter.ObjectFilterRelation;

/**
 * Only keeps objects whose feature-value satisfies a condition relative to a threshold.
 *
 * <p>Specifically, <code>relation(volume,threshold)</code> must be true.
 *
 * @author Owen Feehan
 */
public class ThresholdedVolume extends ObjectFilterRelation {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private UnitValueVolume threshold;
    // END BEAN PROPERTIES

    private int thresholdResolved;

    @Override
    protected void start(Optional<Dimensions> dimensions, ObjectCollection objectsToFilter)
            throws OperationFailedException {
        super.start(dimensions, objectsToFilter);
        thresholdResolved = resolveThreshold(dimensions);
    }

    @Override
    protected boolean match(
            ObjectMask object, Optional<Dimensions> dim, DoubleBiPredicate relation) {
        return relation.test(object.numberVoxelsOn(), thresholdResolved);
    }

    private int resolveThreshold(Optional<Dimensions> dim) throws OperationFailedException {
        try {
            return (int)
                    Math.floor(threshold.resolveToVoxels(dim.flatMap(Dimensions::unitConvert)));
        } catch (UnitValueException e) {
            throw new OperationFailedException(e);
        }
    }
}
