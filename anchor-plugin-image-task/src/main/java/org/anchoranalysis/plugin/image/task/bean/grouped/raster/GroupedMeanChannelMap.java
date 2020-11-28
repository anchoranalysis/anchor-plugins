/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.grouped.raster;

import java.io.IOException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.io.channel.output.ChannelGenerator;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.grouped.ConsistentChannelChecker;
import org.anchoranalysis.plugin.image.task.grouped.GroupMapByName;

class GroupedMeanChannelMap extends GroupMapByName<Channel, RunningSumChannel> {

    private static final String MANIFEST_FUNCTION = "channelMean";

    private final String outputName;
    /**
     * @param outputName the first-level output-name used to determine whether mean channels will be
     *     written or not
     */
    public GroupedMeanChannelMap(String outputName) {
        super("channel", MANIFEST_FUNCTION, RunningSumChannel::new);
        this.outputName = outputName;
    }

    @Override
    protected void addTo(Channel ind, RunningSumChannel agg) throws OperationFailedException {
        agg.addChannel(ind);
    }

    @Override
    protected void writeGroupOutputInSubdirectory(
            String partName,
            RunningSumChannel agg,
            ConsistentChannelChecker channelChecker,
            InputOutputContext context)
            throws IOException {
        // TODO change always2D
        VoxelDataType outputType = channelChecker.getChannelType();
        context.getOutputter()
                .writerSecondLevel(outputName)
                .write(
                        partName,
                        () -> new ChannelGenerator(MANIFEST_FUNCTION),
                        () -> generatorWithMean(agg, outputType, partName, context));
    }

    private static Channel generatorWithMean(
            RunningSumChannel agg,
            VoxelDataType outputType,
            String channelName,
            InputOutputContext context)
            throws OutputWriteFailedException {
        try {
            Channel mean = agg.createMeanChannel(outputType);

            context.getMessageReporter()
                    .logFormatted(
                            "Writing channel %s with %d items and numPixels>100=%d and outputType=%s",
                            channelName,
                            agg.count(),
                            mean.voxelsGreaterThan(100).count(),
                            outputType);

            return mean;
        } catch (OperationFailedException e) {
            throw new OutputWriteFailedException(e);
        }
    }
}
