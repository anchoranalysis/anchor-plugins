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

package org.anchoranalysis.plugin.image.bean.object.provider.morphological;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.exception.BeanMisconfiguredException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.image.voxel.object.morphological.MorphologicalErosion;
import org.anchoranalysis.image.voxel.object.morphological.predicate.AcceptIterationList;
import org.anchoranalysis.image.voxel.object.morphological.predicate.RejectIterationIfAllHigh;
import org.anchoranalysis.image.voxel.object.morphological.predicate.RejectIterationIfLowDisconnected;
import org.anchoranalysis.spatial.Extent;

/**
 * Erodes each object in the collection, growing bounding-boxes as necessary in relevant dimensions.
 *
 * @author Owen Feehan
 */
public class Erode extends ObjectCollectionProviderMorphological {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private boolean rejectIterationIfAllLow = false;

    @BeanField @Getter @Setter private boolean rejectIterationIfDisconnected = false;
    // END BEAN PROPERTIES

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        if (getDimensions() == null) {
            throw new BeanMisconfiguredException(
                    "If outsideAtThreshold==false then dim must be set");
        }
    }

    @Override
    protected ObjectMask applyMorphologicalOperation(ObjectMask object, Optional<Extent> extent)
            throws CreateException {

        AcceptIterationList acceptConditionsDilation = new AcceptIterationList();
        if (rejectIterationIfAllLow) {
            acceptConditionsDilation.add(new RejectIterationIfAllHigh());
        }
        if (rejectIterationIfDisconnected) {
            acceptConditionsDilation.add(new RejectIterationIfLowDisconnected());
        }

        return MorphologicalErosion.erode(
                object,
                getIterations(),
                isDo3D(),
                Optional.of(acceptConditionsDilation));
    }
}
