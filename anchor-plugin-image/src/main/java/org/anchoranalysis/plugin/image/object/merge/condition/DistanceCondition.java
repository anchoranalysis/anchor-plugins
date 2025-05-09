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

package org.anchoranalysis.plugin.image.object.merge.condition;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.core.dimensions.UnitConverter;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.spatial.box.BoundingBoxDistance;
import org.anchoranalysis.spatial.point.Point3d;

/**
 * A {@link BeforeCondition} that checks if two {@link ObjectMask}s are within a specified maximum
 * distance.
 */
@AllArgsConstructor
public class DistanceCondition implements BeforeCondition {

    /** The maximum allowed distance between objects. */
    private final Optional<UnitValueDistance> maxDistance;

    /** Whether to ignore the Z-dimension when calculating distances. */
    private final boolean suppressZ;

    /** Logger for outputting messages. */
    private final MessageLogger logger;

    @Override
    public boolean accept(
            ObjectMask source, ObjectMask destination, Optional<UnitConverter> unitConverter)
            throws OperationFailedException {

        // We impose a maximum distance condition if necessary
        if (maxDistance.isPresent()) {
            return isWithinMaxDistance(source, destination, unitConverter);
        } else {
            return true;
        }
    }

    /**
     * Checks if two {@link ObjectMask}s are within the maximum allowed distance.
     *
     * @param source the source {@link ObjectMask}
     * @param destination the destination {@link ObjectMask}
     * @param unitConverter an optional {@link UnitConverter} for unit conversions
     * @return true if the objects are within the maximum distance, false otherwise
     * @throws OperationFailedException if the distance check fails
     */
    private boolean isWithinMaxDistance(
            ObjectMask source, ObjectMask destination, Optional<UnitConverter> unitConverter)
            throws OperationFailedException {

        double distance =
                BoundingBoxDistance.distance(
                        source.boundingBox(), destination.boundingBox(), !suppressZ);

        double maxDistanceResolved =
                resolveDistance(
                        unitConverter,
                        source.boundingBox().midpoint(),
                        destination.boundingBox().midpoint());

        if (distance >= maxDistanceResolved) {
            return false;
        } else {

            logger.logFormatted(
                    "Maybe merging %s and %s with distance %f (<%f)",
                    source.boundingBox().midpoint(),
                    destination.boundingBox().midpoint(),
                    distance,
                    maxDistanceResolved);

            return true;
        }
    }

    /**
     * Checks if the distance between two points is within the maximum allowed distance.
     *
     * @param unitConverter an optional {@link UnitConverter} for unit conversions
     * @param point1 the first {@link Point3d}
     * @param point2 the second {@link Point3d}
     * @return the resolved maximum distance
     * @throws OperationFailedException if the distance resolution fails
     */
    private double resolveDistance(
            Optional<UnitConverter> unitConverter, Point3d point1, Point3d point2)
            throws OperationFailedException {
        if (suppressZ) {
            return maxDistance // NOSONAR
                    .get()
                    .resolve(unitConverter, point1.dropZ(), point2.dropZ());
        } else {
            return maxDistance // NOSONAR
                    .get()
                    .resolve(unitConverter, point1, point2);
        }
    }
}
