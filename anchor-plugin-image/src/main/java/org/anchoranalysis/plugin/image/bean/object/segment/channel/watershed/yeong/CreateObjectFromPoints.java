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

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.points.BoundingBoxFromPoints;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.point.Point3i;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CreateObjectFromPoints {

    // TODO Optimize by requiring sorted list of points and moving through the z-stacks sequentially
    public static ObjectMask create(List<Point3i> points) throws CreateException {

        try {
            ObjectMask object = new ObjectMask(BoundingBoxFromPoints.fromStream(points.stream()));

            for (Point3i point : points) {

                object.binaryVoxels()
                        .setOn(Point3i.immutableSubtract(point, object.boundingBox().cornerMin()));
            }

            return object;

        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
