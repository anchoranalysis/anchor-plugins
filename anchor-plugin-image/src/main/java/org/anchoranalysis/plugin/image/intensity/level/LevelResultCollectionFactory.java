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

package org.anchoranalysis.plugin.image.intensity.level;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.object.HistogramFromObjectsFactory;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.image.voxel.object.morphological.MorphologicalDilation;
import org.anchoranalysis.math.histogram.Histogram;

/** Factory for creating {@link LevelResultCollection} objects. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LevelResultCollectionFactory {

    /**
     * Creates a {@link LevelResultCollection} from a channel and object collection.
     *
     * @param channel the {@link Channel} to process
     * @param objects the {@link ObjectCollection} to process
     * @param calculateLevel the {@link CalculateLevel} bean to use for level calculation
     * @param numberDilations the number of dilations to apply to each object
     * @param logger the {@link MessageLogger} for logging messages
     * @return a new {@link LevelResultCollection}
     * @throws CreateException if an error occurs during creation
     */
    public static LevelResultCollection createCollection(
            Channel channel,
            ObjectCollection objects,
            CalculateLevel calculateLevel,
            int numberDilations,
            MessageLogger logger)
            throws CreateException {

        LevelResultCollection all = new LevelResultCollection();

        for (ObjectMask objectMask : objects) {

            ObjectMask objectForCalculateLevel;

            logger.logFormatted(
                    "Creating level result %s", objectMask.centerOfGravity().toString());

            // Optional dilation
            if (numberDilations != 0) {
                objectForCalculateLevel =
                        MorphologicalDilation.dilate(
                                objectMask,
                                Optional.of(channel.extent()),
                                channel.dimensions().z() > 1,
                                numberDilations,
                                false);
            } else {
                objectForCalculateLevel = objectMask;
            }

            Histogram histogram =
                    HistogramFromObjectsFactory.createFrom(channel, objectForCalculateLevel);
            int level;
            try {
                level = calculateLevel.calculateLevel(histogram);
            } catch (OperationFailedException e) {
                throw new CreateException(e);
            }

            LevelResult result = new LevelResult(level, objectMask, histogram);

            logger.logFormatted("Level result is %d", result.getLevel());

            all.add(result);
        }
        return all;
    }
}
