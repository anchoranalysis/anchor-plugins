/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.provider.collection;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.core.points.PointsFromObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.mpp.bean.provider.MarkCollectionProvider;
import org.anchoranalysis.mpp.mark.MarkCollection;
import org.anchoranalysis.mpp.mark.points.PointListFactory;

/** Creates a {@link MarkCollection} of point marks from an {@link ObjectCollection}. */
public class PointsFromObjects extends MarkCollectionProvider {

    /** Provider for the object collection from which to create point marks. */
    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    @Override
    public MarkCollection get() throws ProvisionFailedException {
        return createMarksFromObjects(objects.get());
    }

    /**
     * Creates a {@link MarkCollection} of point marks from an {@link ObjectCollection}.
     *
     * @param objectCollection the collection of objects to convert to point marks
     * @return a new {@link MarkCollection} containing point marks derived from the objects
     */
    private static MarkCollection createMarksFromObjects(ObjectCollection objectCollection) {

        MarkCollection out = new MarkCollection();

        for (int i = 0; i < objectCollection.size(); i++) {

            ObjectMask object = objectCollection.get(i);

            out.add(
                    PointListFactory.create(
                            PointsFromObject.listFrom3d(object), i // We assign an unique integer ID
                            ));
        }
        return out;
    }
}
