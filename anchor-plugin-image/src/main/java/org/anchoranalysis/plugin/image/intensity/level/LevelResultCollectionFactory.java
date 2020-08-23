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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
import org.anchoranalysis.image.bean.threshold.CalculateLevel;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalDilation;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LevelResultCollectionFactory {

    public static LevelResultCollection createCollection(
            Channel channel,
            ObjectCollection objects,
            CalculateLevel calculateLevel,
            int numDilations,
            MessageLogger logger)
            throws CreateException {

        LevelResultCollection all = new LevelResultCollection();

        for (ObjectMask objectMask : objects) {

            ObjectMask objectForCalculateLevel;

            logger.logFormatted(
                    "Creating level result %s", objectMask.centerOfGravity().toString());

            // Optional dilation
            if (numDilations != 0) {
                objectForCalculateLevel =
                        MorphologicalDilation.createDilatedObject(
                                objectMask,
                                Optional.of(channel.extent()),
                                channel.dimensions().z() > 1,
                                numDilations,
                                false);
            } else {
                objectForCalculateLevel = objectMask;
            }

            Histogram h = HistogramFactory.create(channel, objectForCalculateLevel);
            int level;
            try {
                level = calculateLevel.calculateLevel(h);
            } catch (OperationFailedException e) {
                throw new CreateException(e);
            }

            LevelResult res = new LevelResult(level, objectMask, h);

            logger.logFormatted("Level result is %d", res.getLevel());

            all.add(res);
        }
        return all;
    }
}