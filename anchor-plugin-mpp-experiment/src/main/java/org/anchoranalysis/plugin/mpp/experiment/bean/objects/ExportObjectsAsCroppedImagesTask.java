/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.functional.IdentityOperation;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.NamedImgStackCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.IterableGeneratorBridge;
import org.anchoranalysis.io.generator.combined.IterableCombinedListGenerator;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceIncrementalRerouteErrors;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceIncrementalWriter;
import org.anchoranalysis.io.namestyle.IndexableOutputNameStyle;
import org.anchoranalysis.io.namestyle.IntegerPrefixOutputNameStyle;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.sgmn.bean.define.DefineOutputterMPP;

/**
 * Exports a cropped image for each object-mask showing its context iwthin an image
 *
 * <p>Specifically, a bounding-box is placed around an object-mask, maybe padded and extended, and
 * this is shown
 *
 * @author Owen Feehan
 */
public class ExportObjectsAsCroppedImagesTask extends ExportObjectsBase<MultiInput, NoSharedState> {

    private static final String MANIFEST_FUNCTION = "rasterExtract";

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private DefineOutputterMPP define;

    @BeanField @OptionalBean @Getter @Setter
    private List<NamedBean<StackProvider>> listStackProvider =
            new ArrayList<>(); // The channels we apply the masks to - all assumed to be of same
    // dimension

    @BeanField @OptionalBean @Getter @Setter
    private List<NamedBean<StackProvider>> listStackProviderMIP =
            new ArrayList<>(); // The channels we apply the masks to - all assumed to be of same
    // dimension

    @BeanField @Getter @Setter private StringSet outputRGBOutline = new StringSet();

    @BeanField @Getter @Setter private StringSet outputRGBOutlineMIP = new StringSet();

    @BeanField @Getter @Setter private int outlineWidth = 1;

    @BeanField @Getter @Setter
    private boolean extendInZ =
            false; // Extends the objects in z-dimension (uses maximum intensity for the
    // segmentation, but in all slices)

    /**
     * If true, rather than writing out a bounding-box around the object mask, the entire image is
     * written
     */
    @BeanField @Getter @Setter private boolean keepEntireImage = false;
    // END BEAN PROPERTIES

    @Override
    public void doJobOnInputObject(InputBound<MultiInput, NoSharedState> params)
            throws JobExecutionException {
        try {
            define.processInputImage(
                    params.getInputObject(),
                    params.context(),
                    paramsInit -> outputObjects(paramsInit, params.context()));

        } catch (OperationFailedException e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(MultiInput.class);
    }

    @Override
    public boolean hasVeryQuickPerInputExecution() {
        return false;
    }

    private void outputObjects(ImageInitParams paramsInit, BoundIOContext context)
            throws OperationFailedException {

        try {
            Logger logger = context.getLogger();

            NamedImgStackCollection stackCollection = createStackCollection(paramsInit, logger);
            NamedImgStackCollection stackCollectionMIP =
                    createStackCollectionMIP(paramsInit, logger);

            if (stackCollection.keys().isEmpty()) {
                // Nothing to do
                return;
            }

            ImageDimensions dimensions = stackCollection.getArbitraryElement().getDimensions();

            outputGeneratorSeq(
                    createGenerator(dimensions, stackCollection, stackCollectionMIP),
                    maybeExtendZObjects(inputObjects(paramsInit, logger), dimensions.getZ()),
                    context);
        } catch (CreateException | InitException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    public NoSharedState beforeAnyJobIsExecuted(
            BoundOutputManagerRouteErrors outputManager, ParametersExperiment params)
            throws ExperimentExecutionException {
        return NoSharedState.INSTANCE;
    }

    @Override
    public void afterAllJobsAreExecuted(NoSharedState sharedState, BoundIOContext context)
            throws ExperimentExecutionException {
        // NOTHING TO DO
    }

    private void outputGeneratorSeq(
            IterableGenerator<ObjectMask> generator,
            ObjectCollection objects,
            BoundIOContext context) {
        GeneratorSequenceIncrementalRerouteErrors<ObjectMask> generatorSeq =
                createGeneratorSequence(generator, context);

        generatorSeq.start();

        for (ObjectMask objectMask : objects) {
            generatorSeq.add(objectMask);
        }

        generatorSeq.end();
    }

    private ObjectCollection maybeExtendZObjects(ObjectCollection objectCollection, int sizeZ) {

        if (extendInZ) {
            objectCollection = extendObjectsInZ(objectCollection, sizeZ);
        }

        return objectCollection;
    }

    private NamedImgStackCollection createStackCollection(ImageInitParams so, Logger logger)
            throws CreateException {
        // Get named image stack collection
        ImageDimensions dimensions = null;
        NamedImgStackCollection stacks = new NamedImgStackCollection();

        for (NamedBean<StackProvider> ni : listStackProvider) {

            try {
                ni.getValue().initRecursive(so, logger);
            } catch (InitException e) {
                // NB if we cannot create a particular channel provider, we simply skip.  We use
                // this as a means to provide for channels
                //  that might not always be present
                logger.errorReporter().recordError(ExportObjectsAsCroppedImagesTask.class, e);
                continue;
            }

            Stack stack = ni.getValue().create();

            if (dimensions == null) {
                dimensions = stack.getDimensions();
            } else {
                if (!stack.getDimensions().equals(dimensions)) {
                    throw new CreateException(
                            String.format(
                                    "Channel dimensions are not uniform across the channels (%s vs %s)",
                                    stack.getDimensions(), dimensions));
                }
            }

            try {
                stacks.add(ni.getName(), new IdentityOperation<>(stack));
            } catch (OperationFailedException e) {
                throw new CreateException(e);
            }
        }

        return stacks;
    }

    private NamedImgStackCollection createStackCollectionMIP(ImageInitParams so, Logger logger)
            throws CreateException {
        // Get named image stack collection
        ImageDimensions dimensions = null;
        NamedImgStackCollection stackCollection = new NamedImgStackCollection();

        for (NamedBean<StackProvider> ni : listStackProviderMIP) {

            try {
                ni.getValue().initRecursive(so, logger);
            } catch (InitException e) {
                // NB if we cannot create a particular channel provider, we simply skip.  We use
                // this as a means to provide for channels
                //  that might not always be present
                continue;
            }

            Stack stack = ni.getValue().create();

            if (dimensions == null) {
                dimensions = stack.getDimensions();
            } else {
                if (!stack.getDimensions().equals(dimensions)) {
                    throw new CreateException("Stack dimensions do not match");
                }
            }

            try {
                stackCollection.add(ni.getName(), new IdentityOperation<>(stack));
            } catch (OperationFailedException e) {
                throw new CreateException(e);
            }
        }
        return stackCollection;
    }

    private IterableGenerator<ObjectMask> createGenerator(
            ImageDimensions dimensions,
            NamedImgStackCollection stacks,
            NamedImgStackCollection stacksFlattened)
            throws CreateException {

        IterableCombinedListGenerator<ObjectMask> out =
                new GeneratorHelper(outlineWidth, outputRGBOutline, outputRGBOutlineMIP)
                        .buildGenerator(
                                dimensions,
                                stacks,
                                stacksFlattened,
                                stack ->
                                        createBoundingBoxGeneratorForStack(
                                                stack, MANIFEST_FUNCTION));

        // Maybe we need to change the objectMask to a padded version
        return new IterableGeneratorBridge<>(
                out,
                object -> {
                    if (keepEntireImage) {
                        return extractObjectKeepEntireImage(object, dimensions);
                    } else {
                        return maybePadObject(object, dimensions);
                    }
                });
    }

    private static GeneratorSequenceIncrementalRerouteErrors<ObjectMask> createGeneratorSequence(
            IterableGenerator<ObjectMask> generator, BoundIOContext context) {
        IndexableOutputNameStyle outputNameStyle =
                new IntegerPrefixOutputNameStyle("extractedObjects", 6);

        return new GeneratorSequenceIncrementalRerouteErrors<>(
                new GeneratorSequenceIncrementalWriter<>(
                        context.getOutputManager().getDelegate(),
                        outputNameStyle.getOutputName(),
                        outputNameStyle,
                        generator,
                        0,
                        true),
                context.getErrorReporter());
    }

    private static ObjectMask extractObjectKeepEntireImage(
            ObjectMask object, ImageDimensions dimensions) {
        return BoundingBoxUtilities.createObjectForBoundingBox(
                object, new BoundingBox(dimensions.getExtent()));
    }

    private static ObjectCollection extendObjectsInZ(ObjectCollection objects, int sz) {
        return objects.stream().map(objectMask -> objectMask.flattenZ().growToZ(sz));
    }
}
