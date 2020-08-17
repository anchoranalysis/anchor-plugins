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

package org.anchoranalysis.plugin.image.feature.bean.object.single.moments;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.feature.cache.calculation.FeatureCalculation;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.math.moment.ImageMoments;

/**
 * Calculates a matrix of second moments (covariance) of all points in an object-mask.
 *
 * <p>NOTE, the matrix rows order the eigen-values, so that the first row is highest eigen-value,
 * second row is second-highest etc.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
class CalculateSecondMoments extends FeatureCalculation<ImageMoments, FeatureInputSingleObject> {

    /** Whether to ignore the z-dimension */
    private final boolean suppressZ;

    @Override
    protected ImageMoments execute(FeatureInputSingleObject params) {
        return new ImageMoments(
                createPointMatrixFromObject(params.getObject()), suppressZ, false);
    }

    /**
     * Creates a point-matrix with the distance of each point in each dimension to the origin of the bounding-box
     *
     * @param object object whose ON voxels form the points in the matrix
     * @return newly created matrix
     */
    private static DoubleMatrix2D createPointMatrixFromObject(ObjectMask object) {
        return createPointMatrixInteger( object.derivePointsLocal() );
    }

    private static DoubleMatrix2D createPointMatrixInteger(List<Point3i> points) {
        DoubleMatrix2D matrix = DoubleFactory2D.dense.make(points.size(), 3);
        for (int i = 0; i < points.size(); i++) {
            Point3i point = points.get(i);
            matrix.set(i, 0, point.x());
            matrix.set(i, 1, point.y());
            matrix.set(i, 2, point.z());
        }
        return matrix;
    }
}
