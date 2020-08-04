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
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.experiment.task.ParametersExperiment;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.stack.StackProvider;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.generator.raster.bbox.ObjectsWithBoundingBox;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.stack.NamedStacksSet;
import org.anchoranalysis.image.stack.NamedStacksUniformSize;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceFactory;
import org.anchoranalysis.io.generator.sequence.GeneratorSequenceIncrementalRerouteErrors;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.io.output.bound.BoundOutputManagerRouteErrors;
import org.anchoranalysis.mpp.io.input.MultiInput;
import org.anchoranalysis.mpp.sgmn.bean.define.DefineOutputterMPP;

/**
 * Exports a cropped image for each object-mask showing its context within an image
 *
 * <p>The context is defined by a bounding-box is placed around an object-mask, and optionally
 * padded.
 *
 * @author Owen Feehan
 */
public class ExportObjectsAsCroppedImagesTask extends ExportObjectsBase<MultiInput, NoSharedState> {

    private static final GeneratorSequenceFactory GENERATOR_SEQUENCE_FACTORY =
            new GeneratorSequenceFactory("extractedObjects", "object");

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private DefineOutputterMPP define;

    /** The channels we extract the object-masks from - all assumed to be of same dimension */
    @BeanField @OptionalBean @Getter @Setter
    private List<NamedBean<StackProvider>> listStackProvider = new ArrayList<>();

    /** The channels we extract the object-masks from - all assumed to be of same dimension */
    @BeanField @OptionalBean @Getter @Setter
    private List<NamedBean<StackProvider>> listStackProviderMIP = new ArrayList<>();

    @BeanField @Getter @Setter private StringSet outputRGBOutline = new StringSet();

    @BeanField @Getter @Setter private StringSet outputRGBOutlineFlattened = new StringSet();

    @BeanField @Getter @Setter private int outlineWidth = 1;

    /**
     * Extends the objects in z-dimension (uses maximum intensity for the segmentation, but in all
     * slices)
     */
    @BeanField @Getter @Setter private boolean extendInZ = false;

    /**
     * If true, rather than writing out a bounding-box around the object-mask, the entire image is
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
            IterableGenerator<ObjectsWithBoundingBox> generator,
            ObjectCollection objects,
            BoundIOContext context) {
        GeneratorSequenceIncrementalRerouteErrors<ObjectsWithBoundingBox> generatorSeq =
                GENERATOR_SEQUENCE_FACTORY.createIncremental(generator, context);

        generatorSeq.start();
        objects.streamStandardJava().forEach( object->
            generatorSeq.add( new ObjectsWithBoundingBox(object) )
        );
        generatorSeq.end();
    }

    private void outputObjects(ImageInitParams paramsInit, BoundIOContext context)
            throws OperationFailedException {

        try {
            Logger logger = context.getLogger();

            NamedStacksSet stacks = createStacksFromProviders(listStackProvider, paramsInit, logger);
            NamedStacksSet stacksProjected =
                    createStacksFromProviders(listStackProviderMIP, paramsInit, logger);

            if (stacks.keys().isEmpty()) {
                // Nothing to do
                return;
            }

            ImageDimensions dimensions = stacks.getArbitraryElement().getDimensions();

            outputGeneratorSeq(
                    createGenerator(dimensions, stacks, stacksProjected),
                    maybeExtendZObjects(inputObjects(paramsInit, logger), dimensions.getZ()),
                    context);
        } catch (CreateException | InitException e) {
            throw new OperationFailedException(e);
        }
    }

    private static NamedStacksSet createStacksFromProviders(
            List<NamedBean<StackProvider>> stackProviders, ImageInitParams so, Logger logger)
            throws CreateException {
        // Get named image stack collection
        NamedStacksUniformSize stacks = new NamedStacksUniformSize();

        for (NamedBean<StackProvider> namedStackProvider : stackProviders) {

            try {
                namedStackProvider.getValue().initRecursive(so, logger);
            } catch (InitException e) {
                // NB if we cannot create a particular channel provider, we simply skip.  We use
                // this as a means to provide for channels
                //  that might not always be present
                logger.errorReporter().recordError(ExportObjectsAsCroppedImagesTask.class, e);
                continue;
            }

            try {
                stacks.add(namedStackProvider.getName(), namedStackProvider.getValue().create());
            } catch (OperationFailedException e) {
                throw new CreateException(e);
            }
        }

        return stacks.withoutUniformSizeConstraint();
    }

    private IterableGenerator<ObjectsWithBoundingBox> createGenerator(
            ImageDimensions dimensions, NamedStacksSet stacks, NamedStacksSet stacksFlattened)
            throws CreateException {

        IterableGenerator<ObjectsWithBoundingBox> generator =
                new BuildGeneratorHelper(outlineWidth)
                        .forStacks(
                                dimensions,
                                stacks.subset(outputRGBOutline),
                                stacksFlattened.subset(outputRGBOutlineFlattened));

        // Maybe we need to change the objectMask to a padded version
        return AddPaddingToGenerator.addPadding(
                generator, dimensions, getPadding(), keepEntireImage);
    }

    private ObjectCollection maybeExtendZObjects(ObjectCollection objects, int sizeZ) {
        if (extendInZ) {
            return extendObjectsInZ(objects, sizeZ);
        } else {
            return objects;
        }
    }

    private static ObjectCollection extendObjectsInZ(ObjectCollection objects, int sz) {
        return objects.stream().map(objectMask -> objectMask.flattenZ().growToZ(sz));
    }
}
