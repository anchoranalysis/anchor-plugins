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

package org.anchoranalysis.plugin.image.bean.object.segment.channel;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.function.CheckedUnaryOperator;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjectsUnary;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Dimensions;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.interpolator.Interpolator;
import org.anchoranalysis.image.interpolator.InterpolatorFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.factory.ObjectCollectionFactory;
import org.anchoranalysis.image.scale.ScaleFactor;
import org.anchoranalysis.image.seed.SeedCollection;

/**
 * Perform a segmentation at a different scale, and then fit the results back to the original scale.
 *
 * @author Owen Feehan
 */
public class AtScale extends SegmentChannelIntoObjectsUnary {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ScaleCalculator scaleCalculator;

    @BeanField @Getter @Setter private int outlineWidth = 1;

    @BeanField @Getter @Setter private boolean interpolate = true;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection segment(
            Channel channel,
            Optional<ObjectMask> objectMask,
            Optional<SeedCollection> seeds,
            SegmentChannelIntoObjects upstreamSegmentation)
            throws SegmentationFailedException {

        Interpolator interpolator = createInterpolator();

        ScaleFactor scaleFactor = determineScaleFactor(channel.dimensions());

        Extent extent = channel.extent();

        // Perform segmentation on scaled versions of the channel, mask and seeds
        ObjectCollection scaledSegmentationResult =
                upstreamSegmentation.segment(
                        scaleChannel(channel, scaleFactor, interpolator),
                        scaleMask(objectMask, scaleFactor, extent),
                        scaleSeeds(seeds, scaleFactor, extent));

        // Segment and scale results back up to original-scale
        try {
            return scaleResultToOriginalScale(
                    scaledSegmentationResult, scaleFactor, channel.dimensions().extent());
        } catch (OperationFailedException e) {
            throw new SegmentationFailedException(e);
        }
    }

    private Channel scaleChannel(
            Channel channel, ScaleFactor scaleFactor, Interpolator interpolator) {
        return channel.scaleXY(scaleFactor, interpolator);
    }

    private Optional<ObjectMask> scaleMask(
            Optional<ObjectMask> objectMask, ScaleFactor scaleFactor, Extent extent)
            throws SegmentationFailedException {

        return mapScale(
                objectMask, object -> object.scale(scaleFactor, Optional.of(extent)), "mask");
    }

    private Optional<SeedCollection> scaleSeeds(
            Optional<SeedCollection> seeds, ScaleFactor scaleFactor, Extent extent)
            throws SegmentationFailedException {
        return mapScale(
                seeds, seedCollection -> scaleSeeds(seedCollection, scaleFactor, extent), "seeds");
    }

    private ScaleFactor determineScaleFactor(Dimensions dimensions)
            throws SegmentationFailedException {
        try {
            return scaleCalculator.calculate(Optional.of(dimensions));
        } catch (OperationFailedException e) {
            throw new SegmentationFailedException("Cannot calculate scale", e);
        }
    }

    private ObjectCollection scaleResultToOriginalScale(
            ObjectCollection objects, ScaleFactor scaleFactor, Extent originalExtent)
            throws OperationFailedException {
        return ObjectCollectionFactory.of(
                objects.scale(scaleFactor.invert(), originalExtent)
                        .asCollectionOrderNotPreserved());
    }

    /**
     * Scales an {@link Optional} if its present
     *
     * @param <T> optional-type
     * @param optional the optional to be scaled
     * @param scaleFunc function to use for scaling
     * @param textualDscrInError how to describe the optional in an error message
     * @return an optional with either a scaled value or empty() depending on the input-option
     * @throws SegmentationFailedException if the scaling operation fails
     */
    private static <T> Optional<T> mapScale(
            Optional<T> optional,
            CheckedUnaryOperator<T, OperationFailedException> scaleFunc,
            String textualDscrInError)
            throws SegmentationFailedException {
        try {
            if (optional.isPresent()) {
                return Optional.of(scaleFunc.apply(optional.get()));
            } else {
                return Optional.empty();
            }
        } catch (OperationFailedException e) {
            throw new SegmentationFailedException("Cannot scale " + textualDscrInError);
        }
    }

    private static SeedCollection scaleSeeds(
            SeedCollection seedsUnscaled, ScaleFactor scaleFactor, Extent extent)
            throws OperationFailedException {

        if (scaleFactor.x() != scaleFactor.y()) {
            throw new OperationFailedException(
                    "scaleFactor in X and Y must be equal to scale seeds");
        }

        SeedCollection seedsScaled = seedsUnscaled.duplicate();
        seedsScaled.scaleXY(scaleFactor.x(), extent);
        return seedsScaled;
    }

    private Interpolator createInterpolator() {
        return interpolate
                ? InterpolatorFactory.getInstance().rasterResizing()
                : InterpolatorFactory.getInstance().noInterpolation();
    }
}
