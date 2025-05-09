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

package org.anchoranalysis.plugin.image.bean.object.provider.split;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.plugin.image.bean.object.provider.WithDimensionsBase;
import org.anchoranalysis.plugin.image.object.ObjectIntersectionRemover;

/**
 * Considers all possible pairs of objects in a provider, and removes any intersecting pixels
 *
 * @author Owen Feehan
 */
public class RemoveIntersectingVoxels extends WithDimensionsBase {

    // START BEAN PROPERTIES
    /** If true, throws an error if there is a disconnected object after the erosion */
    @BeanField @Getter @Setter private boolean errorDisconnectedObjects = false;

    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects)
            throws ProvisionFailedException {

        try {
            return ObjectIntersectionRemover.removeIntersectingVoxels(
                    objects, createDimensions(), errorDisconnectedObjects);
        } catch (OperationFailedException e) {
            throw new ProvisionFailedException(e);
        }
    }
}
