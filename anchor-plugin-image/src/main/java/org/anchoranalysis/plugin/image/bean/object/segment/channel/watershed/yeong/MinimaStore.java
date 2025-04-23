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

package org.anchoranalysis.plugin.image.bean.object.segment.channel.watershed.yeong;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.spatial.point.Point3i;

/** Stores and manages a collection of minima points for watershed segmentation. */
class MinimaStore {

    /** List of {@link Minima} objects representing the stored minima points. */
    private List<Minima> list = new ArrayList<>();

    /**
     * Adds a new minima, but duplicates the point (which is mutable and may change during
     * iteration) before adding.
     *
     * @param point the {@link Point3i} to be added as a new minima
     */
    public void addDuplicated(Point3i point) {
        list.add(new Minima(new Point3i(point)));
    }

    /**
     * Adds a list of points as a new minima.
     *
     * @param points the {@link List} of {@link Point3i} to be added as a new minima
     */
    public void add(List<Point3i> points) {
        list.add(new Minima(points));
    }

    /**
     * Creates an {@link ObjectCollection} from the stored minima points.
     *
     * @return an {@link ObjectCollection} created from the stored minima points
     * @throws CreateException if there's an error creating the objects
     */
    public ObjectCollection createObjects() throws CreateException {
        return ObjectCollectionFactory.mapFrom(
                list,
                CreateException.class,
                minima -> CreateObjectFromPoints.create(minima.getListPoints()));
    }

    /**
     * Returns the number of minima stored.
     *
     * @return the number of minima in the store
     */
    public int size() {
        return list.size();
    }
}
