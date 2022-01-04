package org.anchoranalysis.plugin.image.task.bean.segment;

import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.task.InputBound;
import org.anchoranalysis.feature.energy.EnergyStack;
import org.anchoranalysis.feature.io.csv.metadata.RowLabels;
import org.anchoranalysis.image.core.stack.Stack;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.inference.segment.WithConfidence;
import org.anchoranalysis.image.io.stack.input.StackSequenceInput;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.inference.InferenceModel;
import org.anchoranalysis.plugin.image.feature.bean.object.combine.CombineObjectsForFeatures;
import org.anchoranalysis.plugin.image.task.feature.InitializationWithEnergyStack;
import org.anchoranalysis.plugin.image.task.feature.calculator.CalculateFeaturesForObjects;
import org.anchoranalysis.plugin.image.task.segment.SharedStateSegmentInstance;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class FeatureCalculatorHelper {

    /**
     * Calculate feature-values (if enabled) and thumbnails (if enabled) for a particualr input.
     *
     * @param <T> model inference type
     * @param input the input to the segmentation-task (that provided {@code stack} below).
     * @param stack the stack that was segmented (at full scale).
     * @param segments the results of the segmentation corresponding to {@code input}.
     * @param combineObjects how to make a feature-table.
     * @throws OperationFailedException if results cannot be successfully calculated.
     */
    public static <T extends InferenceModel> void calculateFeaturesAndThumbnails(
            InputBound<StackSequenceInput, SharedStateSegmentInstance<T>> input,
            Stack stack,
            List<WithConfidence<ObjectMask>> segments,
            CombineObjectsForFeatures<FeatureInputSingleObject> combineObjects)
            throws OperationFailedException {

        if (segments.isEmpty()) {
            // Exit early, nothing to do, as there are no objects.
            return;
        }

        EnergyStack energyStack = new EnergyStack(stack);

        ObjectCollection objects = deriveObjects(segments);

        CalculateFeaturesForObjects<FeatureInputSingleObject> calculator =
                new CalculateFeaturesForObjects<>(
                        combineObjects,
                        new InitializationWithEnergyStack(
                                energyStack, input.createInitializationContext()),
                        true,
                        input.getSharedState()
                                .createCalculationContext(
                                        input.getContextJob().getExecutionTimeRecorder(),
                                        input.getContextJob()));

        String imageIdentifier = input.getInput().identifier();

        input.getContextJob()
                .getExecutionTimeRecorder()
                .recordExecutionTime(
                        "Calculate for objects",
                        () ->
                                calculator.calculateForObjects(
                                        objects,
                                        energyStack,
                                        (instanceIdentifier, groupGeneratorName, index) ->
                                                rowLabelsFor(
                                                        imageIdentifier,
                                                        instanceIdentifier,
                                                        segments.get(index).getConfidence())));
    }

    /** Constructs a {@link RowLabels} instance for a particular instance in a particular image. */
    private static RowLabels rowLabelsFor(
            String imageIdentifier, String instanceIdentifier, double confidence) {
        return new RowLabels(
                Optional.of(
                        new String[] {
                            imageIdentifier, instanceIdentifier, Double.toString(confidence)
                        }),
                Optional.empty());
    }

    /** Derive an {@link ObjectCollection} from a {@code List<WithConfidence<ObjectMask>>}. */
    private static ObjectCollection deriveObjects(
            List<WithConfidence<ObjectMask>> objectsWithConfidence) {
        return new ObjectCollection(objectsWithConfidence.stream().map(WithConfidence::getElement));
    }
}
