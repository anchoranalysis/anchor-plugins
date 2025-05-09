/*-
 * #%L
 * anchor-plugin-ij
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

package org.anchoranalysis.plugin.imagej.bean.object.segment;

import ij.process.ImageProcessor;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.segment.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.object.label.DecodeLabels;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.io.imagej.convert.ConvertToImageProcessor;

/**
 * Creates an object for each separate intensity-value (beginning at {@code startingColor}) by
 * flood-filling.
 *
 * <p>This algorithm only works with 2D images.
 *
 * @author Owen Feehan
 */
public class FloodFillEachIntensityLevel extends SegmentChannelIntoObjects {

    // START BEAN PROPERTIES
    /**
     * Only objects whose bounding-box volume is greater or equal to this threshold are included. By
     * default, all objects are included.
     */
    @BeanField @Getter @Setter private int minimumBoundingBoxVolume = 1;

    /**
     * The first intensity-value to consider as a valid object (e.g. usually 0 is ignored as
     * background).
     */
    @BeanField @Getter @Setter private int startingIntensity = 1;

    // END BEAN PROPERTIES

    @Override
    public ObjectCollection segment(
            Channel channel, Optional<ObjectMask> objectMask, Optional<ObjectCollection> seeds)
            throws SegmentationFailedException {
        checkUnsupportedObjectMask(objectMask);
        checkUnsupportedSeeds(seeds);
        checkUnsupported3D(channel);

        try {
            int numberColors = floodFillChannel(channel);
            return objectsFromLabels(channel, numberColors);

        } catch (OperationFailedException e) {
            throw new SegmentationFailedException(e);
        }
    }

    /**
     * Flood fills a channel, converting it into objects each labelled with an incrementing integer
     * identifier.
     *
     * @param channel channel to flood-fill.
     * @return the number of objects (each corresponding to intensity level 1.... N).
     * @throws OperationFailedException if the flood-filling fails to complete sucessfully.
     */
    private int floodFillChannel(Channel channel) throws OperationFailedException {
        ImageProcessor imageProcessor =
                ConvertToImageProcessor.fromByte(channel.voxels().asByte().slices(), 0);

        FloodFillHelper helper =
                new FloodFillHelper(
                        minimumBoundingBoxVolume,
                        BinaryValuesByte.getDefault().getOn(),
                        imageProcessor);
        return helper.floodFill2D(startingIntensity);
    }

    /**
     * Create object-masks from an image labelled as per {@link #floodFillChannel(Channel)}.
     *
     * @param channel a channel labelled as per {@link #floodFillChannel(Channel)}
     * @param numberLabels the number of objects, so that the label ids are a sequence (1,numLabels)
     *     inclusive.
     * @return a derived collection of objects
     * @throws OperationFailedException
     */
    private ObjectCollection objectsFromLabels(Channel channel, int numberLabels)
            throws OperationFailedException {
        try {
            DecodeLabels<ObjectMask> createObjects =
                    new DecodeLabels<>(
                            channel.voxels().asByte(),
                            1,
                            numberLabels,
                            (index, scaledObject) -> scaledObject);

            return ObjectCollectionFactory.of(createObjects.create(minimumBoundingBoxVolume));
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
    }
}
