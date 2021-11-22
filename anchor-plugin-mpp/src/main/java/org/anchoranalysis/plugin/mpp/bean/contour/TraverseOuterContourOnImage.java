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

package org.anchoranalysis.plugin.mpp.bean.contour;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.Provider;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.dimensions.Resolution;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.mpp.bean.contour.visitscheduler.VisitScheduler;
import org.anchoranalysis.plugin.mpp.contour.ContourTraverser;
import org.anchoranalysis.spatial.box.BoundingBox;
import org.anchoranalysis.spatial.box.Extent;
import org.anchoranalysis.spatial.point.Point3i;
import org.anchoranalysis.spatial.point.Tuple3i;

/**
 * Implementation of {@link TraverseOuterCounter} that uses a {@link VisitScheduler} for determining
 * which pixels are traversed.
 *
 * @author Owen Feehan
 */
public class TraverseOuterContourOnImage extends TraverseOuterCounter {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private VisitScheduler visitScheduler;

    @BeanField @Getter @Setter private boolean bigNeighborhood = true;

    @BeanField @Getter @Setter private Provider<Mask> maskContour;

    @BeanField @Getter @Setter private Provider<Mask> maskFilled;

    @BeanField @Getter @Setter private boolean useZ = true;
    // END BEAN PROPERTIES

    private ObjectMask objectFilled; // Not-changed as traversal occurs
    private ObjectMask objectOutline; // Changed as traversal occurs (visited pixels are removed)

    @Override
    public void traverse(
            Point3i root, List<Point3i> listOut, RandomNumberGenerator randomNumberGenerator)
            throws TraverseContourException {

        Mask outline = createOutline();
        Mask filled = createFilled();

        checkDimensions(outline.dimensions(), filled.dimensions());

        useZ = useZ && (outline.dimensions().z() > 1);

        callBefore(outline.resolution(), randomNumberGenerator);

        objectOutline = createObjectForPoint(root, outline);

        objectFilled = objectForFilled(root, filled);
        callAfter(root, outline.dimensions().resolution(), randomNumberGenerator);
        traverseOutline(root, listOut);
    }

    private Mask createOutline() throws TraverseContourException {
        try {
            return maskContour.get();
        } catch (ProvisionFailedException e) {
            throw new TraverseContourException(
                    "Unable to create binaryImgChannelProviderOutline", e);
        }
    }

    private Mask createFilled() throws TraverseContourException {
        try {
            return maskFilled.get();
        } catch (ProvisionFailedException e) {
            throw new TraverseContourException(
                    "Unable to create binaryImgChannelProviderFilled", e);
        }
    }

    private void checkDimensions(Dimensions dimOutline, Dimensions dimFilled)
            throws TraverseContourException {
        if (!dimOutline.equals(dimFilled)) {
            throw new TraverseContourException(
                    String.format("Dimensions %s and %s are not equal", dimOutline, dimFilled));
        }
    }

    private void callBefore(
            Optional<Resolution> resolution, RandomNumberGenerator randomNumberGenerator)
            throws TraverseContourException {
        try {
            visitScheduler.beforeCreateObject(randomNumberGenerator, resolution);
        } catch (InitializeException e1) {
            throw new TraverseContourException(
                    "Failure to call beforeCreateObject on visitScheduler", e1);
        }
    }

    private ObjectMask objectForFilled(Point3i root, Mask mask) throws TraverseContourException {
        // Important, so we can use the contains function later
        return createObjectForPoint(root, mask).shiftToOrigin();
    }

    private void callAfter(
            Point3i root,
            Optional<Resolution> resolution,
            RandomNumberGenerator randomNumberGenerator)
            throws TraverseContourException {
        Point3i rootRelToMask =
                Point3i.immutableSubtract(root, objectOutline.boundingBox().cornerMin());
        try {
            visitScheduler.afterCreateObject(rootRelToMask, resolution, randomNumberGenerator);
        } catch (InitializeException e) {
            throw new TraverseContourException(
                    "Cannot call afterCreateObject on visitScheduler", e);
        }
    }

    private void traverseOutline(Point3i root, List<Point3i> listOut)
            throws TraverseContourException {
        try {
            new ContourTraverser(
                            objectOutline,
                            (point, distance) ->
                                    visitScheduler.considerVisit(point, distance, objectFilled),
                            useZ,
                            bigNeighborhood)
                    .traverseGlobal(root, listOut::add);
        } catch (OperationFailedException e) {
            throw new TraverseContourException("Cannot traverse outline", e);
        }
    }

    private ObjectMask createObjectForPoint(Point3i root, Mask mask)
            throws TraverseContourException {

        try {
            Tuple3i maxDistance =
                    visitScheduler
                            .maxDistanceFromRootPoint(mask.resolution())
                            .orElseThrow(
                                    () ->
                                            new CreateException(
                                                    "An undefined max-distance is not supported"));

            // We make sure the box is within our scene boundaries
            BoundingBox box = createBoxAroundPoint(root, maxDistance, mask.extent());

            // This is our final intersection box, that we use for traversing and memorizing pixels
            //  that we have already visited
            assert (!box.extent().isEmpty());

            return mask.region(box, false);

        } catch (OperationFailedException | CreateException e) {
            throw new TraverseContourException(
                    "Unable to create an object-mask for the outline", e);
        }
    }

    private static BoundingBox createBoxAroundPoint(Point3i point, Tuple3i width, Extent clampTo) {
        // We create a raster around the point, maxDistance*2 in both directions, so long as it
        // doesn't escape the region
        BoundingBox box =
                new BoundingBox(
                        Point3i.immutableSubtract(point, width),
                        Point3i.immutableAdd(point, width));
        return box.clampTo(clampTo);
    }
}
