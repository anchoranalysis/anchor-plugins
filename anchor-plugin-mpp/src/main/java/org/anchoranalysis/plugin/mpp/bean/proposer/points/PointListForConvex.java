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

package org.anchoranalysis.plugin.mpp.bean.proposer.points;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.plugin.mpp.bean.outline.visitscheduler.VisitSchedulerConvexAboutRoot;
import org.anchoranalysis.spatial.point.Point3i;

class PointListForConvex {

    private List<Point3i> list = new ArrayList<>();

    public boolean add(Point3i e) {
        return list.add(e);
    }

    public boolean convexWithAtLeastOnePoint(
            Point3i root, Point3i point, BinaryVoxels<UnsignedByteBuffer> voxels) {
        return VisitSchedulerConvexAboutRoot.isPointConvexTo(root, point, voxels);
    }

    public boolean convexWithAtLeastOnePoint(
            Point3i pointToHave, BinaryVoxels<UnsignedByteBuffer> voxels) {
        return list.stream()
                .anyMatch(point -> convexWithAtLeastOnePoint(point, pointToHave, voxels));
    }
}
