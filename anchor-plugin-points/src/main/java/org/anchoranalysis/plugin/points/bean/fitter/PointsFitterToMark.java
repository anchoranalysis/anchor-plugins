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

package org.anchoranalysis.plugin.points.bean.fitter;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.mpp.bean.points.PointsBean;
import org.anchoranalysis.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.mpp.bean.points.fitter.PointsFitter;
import org.anchoranalysis.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.spatial.point.Point3f;

/** A bean for fitting points to a mark using a specified points fitter. */
public class PointsFitterToMark extends PointsBean<PointsFitterToMark> {

    // START BEAN PROPERTIES
    /** The {@link PointsFitter} used to fit points to a mark. */
    @BeanField @Getter @Setter private PointsFitter pointsFitter;

    /** Provides the {@link Dimensions} for the fitting operation. */
    @BeanField @Getter @Setter private DimensionsProvider dimensions;

    /** The minimum number of points required for fitting. Objects with fewer points are ignored. */
    @BeanField @Positive @Getter @Setter private int minNumPoints = 1;

    /** Provides the {@link ObjectCollection} to be used in the fitting process. */
    @BeanField @Getter @Setter private ObjectCollectionProvider objects;
    // END BEAN PROPERTIES

    /**
     * Fits a list of points to a mark using the specified points fitter.
     *
     * @param pointsForFitter the list of {@link Point3f} to fit
     * @param mark the {@link Mark} to fit the points to
     * @param dim the {@link Dimensions} of the image
     * @throws OperationFailedException if the fitting operation fails
     */
    public void fitPointsToMark(List<Point3f> pointsForFitter, Mark mark, Dimensions dim)
            throws OperationFailedException {
        try {
            pointsFitter.fit(pointsForFitter, mark, dim);
        } catch (PointsFitterException | InsufficientPointsException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Creates and returns the {@link ObjectCollection} using the specified provider.
     *
     * @return the created {@link ObjectCollection}
     * @throws ProvisionFailedException if the object collection cannot be created
     */
    public ObjectCollection createObjects() throws ProvisionFailedException {
        return objects.get();
    }

    /**
     * Creates and returns the {@link Dimensions} using the specified provider.
     *
     * @return the created {@link Dimensions}
     * @throws ProvisionFailedException if the dimensions cannot be created
     */
    public Dimensions createDim() throws ProvisionFailedException {
        return dimensions.get();
    }
}
