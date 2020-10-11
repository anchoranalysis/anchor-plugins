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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.log.MessageLogger;
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
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.io.output.outputter.OutputterChecked;
import org.anchoranalysis.plugin.image.bean.channel.provider.intensity.ScaleXY;

/**
 * Task to scale an image.
 *
 * <p>Expects a second-level output "scaled" and "scaledFlattened" to determine which stacks get outputted or not.
 *
 * @author Owen Feehan
 */
public class ScaleImage extends RasterTask {

    private static final String OUTPUT_SCALED = "scaled";
    private static final String OUTPUT_SCALED_FLATTENED = "scaledFlattened";

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ScaleCalculator scaleCalculator;

    @BeanField @Getter @Setter private boolean forceBinary = false;
    // END BEAN PROPERTIES

    @Override
    public void startSeries(InputOutputContext context)
            throws JobExecutionException {
        // NOTHING TO DO
    }

    @Override
    public void doStack(
            NamedChannelsInput input, int seriesIndex, int numberSeries, InputOutputContext context)
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
            namedChannels.addAsSeparateChannels(
                    new WrapStackAsTimeSequenceStore(soImage.stacks()), 0);
            scaleCalculator.initRecursive(context.getLogger());
        } catch (InitException | OperationFailedException e) {
            throw new JobExecutionException(e);
        }

        populateAndOutputCollections(soImage, context);
    }

    @Override
    public void endSeries(InputOutputContext context) throws JobExecutionException {
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

        outputStacks(
                stackCollection, OUTPUT_SCALED, context.getOutputter().getChecked());
        outputStacks(
                stackCollectionMIP, OUTPUT_SCALED_FLATTENED, context.getOutputter().getChecked());
    }

    private static void outputStacks(
            NamedProvider<Stack> stacks,
            String outputName,
            OutputterChecked outputter) throws JobExecutionException {
        try {
            StacksOutputter.output(stacks, outputName, false, outputter);
        } catch (OutputWriteFailedException e) {
            throw new JobExecutionException(
               "Failed to write a particular stack in: " + outputName, e);
        }
    }

    private void populateOutputCollectionsFromSharedObjects(
            ImageInitParams params,
            NamedStacks stacksNotFlattened,
            NamedStacks stacksFlattened,
            InputOutputContext context)
            throws JobExecutionException {

        for (String key : params.stacks().keys()) {

            // If neither output is allowed for this channel, we skip
            if (!isEitherOutputEnabled(context, key)) {
                continue;
            }
            
            try {
                Channel channelIn = params.stacks().getException(key).getChannel(0);

                Channel channelOut = scaleChannel(channelIn, context.getLogger().messageLogger());
                
                if (isNonFlattenedEnabled(context,key)) {
                    stacksNotFlattened.add(key, new Stack(channelOut));
                }
                
                if (isFlattenedEnabled(context,key)) {
                    stacksFlattened.add(key, new Stack(channelOut.projectMax()));
                }

            } catch (CreateException e) {
                throw new JobExecutionException(e);
            } catch (NamedProviderGetException e) {
                throw new JobExecutionException(e.summarize());
            }    
        }
    }
    
    private boolean isEitherOutputEnabled(InputOutputContext context, String key) {
        return isFlattenedEnabled(context, key) || isNonFlattenedEnabled(context, key); 
    }
    
    private boolean isNonFlattenedEnabled(InputOutputContext context, String key) {
        return isOutputEnabled(context, OUTPUT_SCALED, key);
    }
    
    private boolean isFlattenedEnabled(InputOutputContext context, String key) {
        return isOutputEnabled(context, OUTPUT_SCALED_FLATTENED, key);
    }
    
    private static boolean isOutputEnabled(InputOutputContext context, String outputName, String key) {
        return context.getOutputter().outputsEnabled().second(outputName).isOutputEnabled(key);
    }
    
    private Channel scaleChannel(Channel channelIn, MessageLogger logger) throws CreateException {
        if (forceBinary) {
            Mask mask = new Mask(channelIn);
            Mask maskScaled =
                    org.anchoranalysis.plugin.image.bean.mask.provider.resize.ScaleXY.scale(
                            mask, scaleCalculator);
            return maskScaled.channel();
        } else {
            return ScaleXY.scale(
                            channelIn,
                            scaleCalculator,
                            InterpolatorFactory.getInstance().rasterResizing(),
                            logger);
        }
    }
}
