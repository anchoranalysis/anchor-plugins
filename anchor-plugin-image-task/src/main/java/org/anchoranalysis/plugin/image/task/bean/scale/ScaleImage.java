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

package org.anchoranalysis.plugin.image.task.bean.scale;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.error.reporter.ErrorReporter;
import org.anchoranalysis.core.name.provider.NamedProvider;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.progress.ProgressReporterNull;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.spatial.ScaleCalculator;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.interpolator.InterpolatorFactory;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.input.ImageInitParamsFactory;
import org.anchoranalysis.image.io.input.NamedChannelsInput;
import org.anchoranalysis.image.io.input.series.NamedChannelsForSeries;
import org.anchoranalysis.image.io.stack.StacksOutputter;
import org.anchoranalysis.image.stack.NamedStacks;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.wrap.WrapStackAsTimeSequenceStore;
import org.anchoranalysis.io.output.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.Outputter;
import org.anchoranalysis.plugin.image.bean.channel.provider.intensity.ScaleXY;

/**
 * Scales many rasters
 *
 * <p>Expects a second-level output "stack" to determine which stacks get ouputted or not
 *
 * @author Owen Feehan
 */
public class ScaleImage extends RasterTask {

    private static final String KEY_OUTPUT_STACK = "stack";

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ScaleCalculator scaleCalculator;

    @BeanField @Getter @Setter private boolean forceBinary = false;
    // END BEAN PROPERTIES

    @Override
    public void startSeries(Outputter outputter, ErrorReporter errorReporter)
            throws JobExecutionException {
        // NOTHING TO DO
    }

    @Override
    public void doStack(
            NamedChannelsInput input,
            int seriesIndex,
            int numberSeries,
            InputOutputContext context)
            throws JobExecutionException {

        // Input
        NamedChannelsForSeries namedChannels;
        try {
            namedChannels = input.createChannelsForSeries(0, ProgressReporterNull.get());
        } catch (RasterIOException e1) {
            throw new JobExecutionException(e1);
        }

        ImageInitParams soImage = ImageInitParamsFactory.create(context);

        try {
            // We store each channel as a stack in our collection, in case they need to be
            // referenced by the scale calculator
            namedChannels.addAsSeparateChannels(new WrapStackAsTimeSequenceStore(soImage.stacks()), 0);
            scaleCalculator.initRecursive(context.getLogger());
        } catch (InitException | OperationFailedException e) {
            throw new JobExecutionException(e);
        }

        populateAndOutputCollections(soImage, context);
    }

    @Override
    public void endSeries(Outputter outputter) throws JobExecutionException {
        // NOTHING TO DO
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    @Override
    public OutputEnabledMutable defaultOutputs() {
        assert (false);
        return super.defaultOutputs();
    }

    private void populateAndOutputCollections(ImageInitParams soImage, InputOutputContext context)
            throws JobExecutionException {
        // Our output collections
        NamedStacks stackCollection = new NamedStacks();
        NamedStacks stackCollectionMIP = new NamedStacks();

        populateOutputCollectionsFromSharedObjects(
                soImage, stackCollection, stackCollectionMIP, context);

        outputStackCollection(
                stackCollection, KEY_OUTPUT_STACK, "channelScaledCollection", context);
        outputStackCollection(
                stackCollectionMIP, KEY_OUTPUT_STACK, "channelScaledCollectionMIP", context);
    }
    
    private static void outputStackCollection(
            NamedProvider<Stack> stackCollection,
            String outputSecondLevelKey,
            String outputName,
            InputOutputContext context) {
        Outputter outputter = context.getOutputter();

        StacksOutputter.output(
                StacksOutputter.subset(
                        stackCollection, outputter.outputsEnabled().second(outputSecondLevelKey)),
                outputter.getChecked(),
                outputName,
                "",
                context.getErrorReporter(),
                false);
    }

    private void populateOutputCollectionsFromSharedObjects(
            ImageInitParams params,
            NamedStacks stackCollection,
            NamedStacks stackCollectionMIP,
            InputOutputContext context)
            throws JobExecutionException {

        Set<String> channelNames = params.stacks().keys();
        for (String channelName : channelNames) {

            // If this output is not allowed we simply skip
            if (!context.getOutputter()
                    .outputsEnabled()
                    .second(KEY_OUTPUT_STACK)
                    .isOutputEnabled(channelName)) {
                continue;
            }

            try {
                Channel channelIn = params.stacks().getException(channelName).getChannel(0);

                Channel channelOut;
                if (forceBinary) {
                    Mask mask = new Mask(channelIn);
                    Mask maskScaled =
                            org.anchoranalysis.plugin.image.bean.mask.provider.resize.ScaleXY.scale(
                                    mask, scaleCalculator);
                    channelOut = maskScaled.channel();
                } else {
                    channelOut =
                            ScaleXY.scale(
                                    channelIn,
                                    scaleCalculator,
                                    InterpolatorFactory.getInstance().rasterResizing(),
                                    context.getLogger().messageLogger());
                }

                stackCollection.add(channelName, new Stack(channelOut));
                stackCollectionMIP.add(channelName, new Stack(channelOut.projectMax()));

            } catch (CreateException e) {
                throw new JobExecutionException(e);
            } catch (NamedProviderGetException e) {
                throw new JobExecutionException(e.summarize());
            }
        }
    }
}
