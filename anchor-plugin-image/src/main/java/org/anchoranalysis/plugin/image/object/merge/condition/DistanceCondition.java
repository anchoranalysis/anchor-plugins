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
/* (C)2020 */
package org.anchoranalysis.plugin.image.object.merge.condition;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.extent.BoundingBoxDistance;
import org.anchoranalysis.image.extent.ImageResolution;
import org.anchoranalysis.image.object.ObjectMask;

@AllArgsConstructor
public class DistanceCondition implements BeforeCondition {

    private final Optional<UnitValueDistance> maxDistance;
    private final boolean suppressZ;
    private final MessageLogger logger;

    @Override
    public boolean accept(ObjectMask source, ObjectMask destination, Optional<ImageResolution> res)
            throws OperationFailedException {

        // We impose a maximum distance condition if necessary
        if (maxDistance.isPresent()) {
            return isWithinMaxDistance(source, destination, res);
        } else {
            return true;
        }
    }

    private boolean isWithinMaxDistance(
            ObjectMask source, ObjectMask destination, Optional<ImageResolution> res)
            throws OperationFailedException {

        double distance =
                BoundingBoxDistance.distance(
                        source.getBoundingBox(), destination.getBoundingBox(), !suppressZ);

        double maxDistanceResolved =
                resolveDistance(
                        res,
                        source.getBoundingBox().midpoint(),
                        destination.getBoundingBox().midpoint());

        if (distance >= maxDistanceResolved) {
            return false;
        } else {

            logger.logFormatted(
                    "Maybe merging %s and %s with distance %f (<%f)",
                    source.getBoundingBox().midpoint(),
                    destination.getBoundingBox().midpoint(),
                    distance,
                    maxDistanceResolved);

            return true;
        }
    }

    private double resolveDistance(Optional<ImageResolution> res, Point3d point1, Point3d point2)
            throws OperationFailedException {
        if (suppressZ) {
            return maxDistance
                    .get()
                    .resolve( // NOSONAR
                            res, point1.dropZ(), point2.dropZ());
        } else {
            return maxDistance
                    .get()
                    .resolve( // NOSONAR
                            res, point1, point2);
        }
    }
}
