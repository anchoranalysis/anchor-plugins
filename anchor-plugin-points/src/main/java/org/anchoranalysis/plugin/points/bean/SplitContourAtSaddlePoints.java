/*-
 * #%L
 * anchor-plugin-points
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

package org.anchoranalysis.plugin.points.bean;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProviderUnary;
import org.anchoranalysis.image.core.object.ObjectFromPointsFactory;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.point.Contour;
import org.anchoranalysis.spatial.point.PointConverter;

/**
 * Splits a 2D contour represented by an object-mask into several contours, splitting at "turn"
 * points.
 *
 * <p>Specifically, smoothing spline interpolation is performed along the contour and splits occur
 * at saddle points.
 *
 * <p>Each contour is represented by an input object.
 *
 * @author Owen Feehan
 */
public class SplitContourAtSaddlePoints extends ObjectCollectionProviderUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private double smoothingFactor = 0.001;

    @BeanField @Getter @Setter private int numberLoopPoints = 0;

    /** If a contour has less than this number of points, we don't split it, and return it as-is */
    @BeanField @Getter @Setter private int minNumberPoints = 10;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects)
            throws ProvisionFailedException {
        return objects.stream().flatMap(CreateException.class, this::splitContoursFromObject);
    }

    private ObjectCollection splitContoursFromObject(ObjectMask object)
            throws ProvisionFailedException {

        if (object.boundingBox().extent().z() > 1) {
            throw new ProvisionFailedException("Only objects with z-slices > 1 are allowed");
        }

        try {
            return contoursAsObjects(
                    SplitContourSmoothingSpline.apply(
                            object, smoothingFactor, numberLoopPoints, minNumberPoints));

        } catch (OperationFailedException e) {
            throw new ProvisionFailedException(e);
        }
    }

    private static ObjectCollection contoursAsObjects(List<Contour> contourList)
            throws OperationFailedException {
        try {
            return ObjectCollectionFactory.mapFrom(
                    contourList, CreateException.class, contour -> createObjectFromContour(contour, true));
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }

    private static ObjectMask createObjectFromContour(Contour contour, boolean round)
            throws CreateException {
        return ObjectFromPointsFactory.create(PointConverter.convert3i(contour.getPoints(), round));
    }
}
