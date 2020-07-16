/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.scale;

import ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.BinaryChnlProviderScaleXY;
import ch.ethz.biol.cell.imageprocessing.chnl.provider.ChnlProviderScale;
import java.util.Set;
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
import org.anchoranalysis.image.bean.scale.ScaleCalculator;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.experiment.bean.task.RasterTask;
import org.anchoranalysis.image.interpolator.InterpolatorFactory;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.input.ImageInitParamsFactory;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.image.io.input.series.NamedChnlCollectionForSeries;
import org.anchoranalysis.image.io.stack.StackCollectionOutputter;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.wrap.WrapStackAsTimeSequenceStore;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;

/**
 * Scales many rasters
 *
 * <p>Expects a second-level output "stack" to determine which stacks get ouputted or not
 *
 * @author Owen Feehan
 */
public class ScaleTask extends RasterTask {

    private static final String KEY_OUTPUT_STACK = "stack";

    // START BEAN PROPERTIES
    @BeanField private ScaleCalculator scaleCalculator;

    @BeanField private boolean forceBinary = false;
    // END BEAN PROPERTIES

    @Override
    public void startSeries(
            BoundOutputManagerRouteErrors outputManager, ErrorReporter errorReporter)
            throws JobExecutionException {
        // NOTHING TO DO
    }

    @Override
    public void doStack(
            NamedChnlsInput inputObject, int seriesIndex, int numSeries, BoundIOContext context)
            throws JobExecutionException {

        // Input
        NamedChnlCollectionForSeries nccfs;
        try {
            nccfs = inputObject.createChnlCollectionForSeries(0, ProgressReporterNull.get());
        } catch (RasterIOException e1) {
            throw new JobExecutionException(e1);
        }

        ImageInitParams soImage = ImageInitParamsFactory.create(context);

        try {
            // We store each channel as a stack in our collection, in case they need to be
            // referenced by the scale calculator
            nccfs.addAsSeparateChnls(
                    new WrapStackAsTimeSequenceStore(soImage.getStackCollection()), 0);
            scaleCalculator.initRecursive(context.getLogger());
        } catch (InitException | OperationFailedException e) {
            throw new JobExecutionException(e);
        }

        populateAndOutputCollections(soImage, context);
    }

    private void populateAndOutputCollections(ImageInitParams soImage, BoundIOContext context)
            throws JobExecutionException {
        // Our output collections
        NamedImgStackCollection stackCollection = new NamedImgStackCollection();
        NamedImgStackCollection stackCollectionMIP = new NamedImgStackCollection();

        populateOutputCollectionsFromSharedObjects(
                soImage, stackCollection, stackCollectionMIP, context);

        outputStackCollection(stackCollection, KEY_OUTPUT_STACK, "chnlScaledCollection", context);
        outputStackCollection(
                stackCollectionMIP, KEY_OUTPUT_STACK, "chnlScaledCollectionMIP", context);
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    private static void outputStackCollection(
            NamedProvider<Stack> stackCollection,
            String outputSecondLevelKey,
            String outputName,
            BoundIOContext context) {
        BoundOutputManagerRouteErrors outputManager = context.getOutputManager();

        StackCollectionOutputter.output(
                StackCollectionOutputter.subset(
                        stackCollection,
                        outputManager.outputAllowedSecondLevel(outputSecondLevelKey)),
                outputManager.getDelegate(),
                outputName,
                "",
                context.getErrorReporter(),
                false);
    }

    private void populateOutputCollectionsFromSharedObjects(
            ImageInitParams so,
            NamedImgStackCollection stackCollection,
            NamedImgStackCollection stackCollectionMIP,
            BoundIOContext context)
            throws JobExecutionException {

        Set<String> chnlNames = so.getStackCollection().keys();
        for (String chnlName : chnlNames) {

            // If this output is not allowed we simply skip
            if (!context.getOutputManager()
                    .outputAllowedSecondLevel(KEY_OUTPUT_STACK)
                    .isOutputAllowed(chnlName)) {
                continue;
            }

            try {
                Channel chnlIn = so.getStackCollection().getException(chnlName).getChnl(0);

                Channel chnlOut;
                if (forceBinary) {
                    Mask binaryImg = new Mask(chnlIn, BinaryValues.getDefault());
                    chnlOut =
                            BinaryChnlProviderScaleXY.scale(
                                            binaryImg,
                                            scaleCalculator,
                                            InterpolatorFactory.getInstance().binaryResizing())
                                    .getChannel();
                } else {
                    chnlOut =
                            ChnlProviderScale.scale(
                                    chnlIn,
                                    scaleCalculator,
                                    InterpolatorFactory.getInstance().rasterResizing(),
                                    context.getLogger().messageLogger());
                }

                stackCollection.addImageStack(chnlName, new Stack(chnlOut));
                stackCollectionMIP.addImageStack(
                        chnlName, new Stack(chnlOut.maxIntensityProjection()));

            } catch (CreateException e) {
                throw new JobExecutionException(e);
            } catch (NamedProviderGetException e) {
                throw new JobExecutionException(e.summarize());
            }
        }
    }

    @Override
    public void endSeries(BoundOutputManagerRouteErrors outputManager)
            throws JobExecutionException {
        // NOTHING TO DO
    }

    public ScaleCalculator getScaleCalculator() {
        return scaleCalculator;
    }

    public void setScaleCalculator(ScaleCalculator scaleCalculator) {
        this.scaleCalculator = scaleCalculator;
    }

    public boolean isForceBinary() {
        return forceBinary;
    }

    public void setForceBinary(boolean forceBinary) {
        this.forceBinary = forceBinary;
    }
}
