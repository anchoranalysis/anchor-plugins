package org.anchoranalysis.plugin.image.task.bean.feature.source;

import java.util.Optional;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calculate.FeatureInitialization;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.feature.results.ResultsVector;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.multi.FeatureCalculatorMulti;
import org.anchoranalysis.feature.shared.SharedFeatureMulti;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.feature.input.FeatureInputImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.ImageMetadataInput;
import org.anchoranalysis.plugin.image.task.feature.InputProcessContext;
import org.anchoranalysis.plugin.image.task.feature.ResultsVectorWithThumbnail;

/**
 * Calculates features from the <b>metadata</b> from single image.
 *
 * <p>The image voxels should not be loaded in memory, to keep this as computationally efficient as
 * possible.
 *
 * <p>Each image's metadata produces a single row of features.
 */
public class FromImageMetadata
        extends SingleRowPerInput<ImageMetadataInput, FeatureInputImageMetadata> {

    public FromImageMetadata() {
        super("image");
    }

    @Override
    public boolean includeGroupInExperiment(boolean groupGeneratorDefined) {
        return groupGeneratorDefined;
    }

    @Override
    public InputTypesExpected inputTypesExpected() {
        return new InputTypesExpected(ImageMetadataInput.class);
    }

    @Override
    protected ResultsVectorWithThumbnail calculateResultsForInput(
            ImageMetadataInput input,
            InputProcessContext<FeatureList<FeatureInputImageMetadata>> context)
            throws NamedFeatureCalculateException {

        try {
            FeatureInitialization initialization = new FeatureInitialization();
            FeatureCalculatorMulti<FeatureInputImageMetadata> calculator =
                    FeatureSession.with(
                            context.getRowSource(),
                            initialization,
                            new SharedFeatureMulti(),
                            context.getLogger());

            ImageMetadata metadata =
                    context.getExecutionTimeRecorder()
                            .recordExecutionTime("Reading image metadata", input::metadata);

            // Calculate the results for the current stack
            ResultsVector results =
                    context.getExecutionTimeRecorder()
                            .recordExecutionTime(
                                    "Calculating features",
                                    () ->
                                            calculator.calculate(
                                                    new FeatureInputImageMetadata(metadata)));

            return new ResultsVectorWithThumbnail(results, Optional.empty());
        } catch (InitializeException | ImageIOException e) {
            throw new NamedFeatureCalculateException(e);
        }
    }
}
