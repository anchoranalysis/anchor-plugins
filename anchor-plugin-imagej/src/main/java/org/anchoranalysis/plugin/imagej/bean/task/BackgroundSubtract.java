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

package org.anchoranalysis.plugin.imagej.bean.task;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.convert.ChannelConverter;
import org.anchoranalysis.image.channel.convert.ConversionPolicy;
import org.anchoranalysis.image.channel.convert.ToUnsignedByte;
import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.experiment.identifiers.StackIdentifiers;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.generator.raster.ChannelGenerator;
import org.anchoranalysis.image.io.input.NamedChannelsInput;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.imagej.bean.channel.provider.BackgroundSubtractor;

public class BackgroundSubtract extends RasterTask {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int radius;

    @BeanField @Getter @Setter private int scaleDownIntensityFactor = 1;
    // END BEAN PROPERTIES

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public void startSeries(InputOutputContext context)
            throws JobExecutionException {
        // NOTHING TO DO
    }

    @Override
    public void doStack(
            NamedChannelsInput input, int seriesIndex, int numberSeries, InputOutputContext context)
            throws JobExecutionException {

        ProgressReporter progressReporter = ProgressReporterNull.get();

        try {
            NamedChannelsForSeries channels = input.createChannelsForSeries(0, progressReporter);

            Channel inputImage =
                    channels.getChannel(StackIdentifiers.INPUT_IMAGE, 0, progressReporter);

            Channel bgSubOut = BackgroundSubtractor.subtractBackground(inputImage, radius, false);
            Voxels<?> voxelsSubOut = bgSubOut.voxels().any();

            double maxPixel = voxelsSubOut.extract().voxelWithMaxIntensity();

            double scaleRatio = 255.0 / maxPixel;

            // We go from 2048 to 256
            if (scaleDownIntensityFactor != 1) {
                voxelsSubOut.arithmetic().multiplyBy(scaleRatio);
            }

            ChannelConverter<UnsignedByteBuffer> converter = new ToUnsignedByte();
            Channel channelOut =
                    converter.convert(bgSubOut, ConversionPolicy.CHANGE_EXISTING_CHANNEL);

            context.getOutputter()
                    .writerSelective()
                    .write("bgsub", () -> new ChannelGenerator("imgChannel", channelOut));

        } catch (ImageIOException | GetOperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void endSeries(InputOutputContext context) throws JobExecutionException {
        // NOTHING TO DO
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        assert false;
        return super.defaultOutputs();
    }
}
