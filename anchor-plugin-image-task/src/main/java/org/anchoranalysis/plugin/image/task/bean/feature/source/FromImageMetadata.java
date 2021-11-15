package org.anchoranalysis.plugin.image.task.bean.feature.source;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.system.path.ExtensionUtilities;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calculate.FeatureInitialization;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.feature.results.ResultsVector;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.multi.FeatureCalculatorMulti;
import org.anchoranalysis.feature.shared.SharedFeatureMulti;
import org.anchoranalysis.image.core.stack.ImageFileAttributes;
import org.anchoranalysis.image.core.stack.ImageMetadata;
import org.anchoranalysis.image.feature.input.FeatureInputImageMetadata;
import org.anchoranalysis.image.io.ImageIOException;
import org.anchoranalysis.image.io.stack.input.ImageMetadataInput;
import org.anchoranalysis.plugin.image.task.feature.FeatureCalculationContext;
import org.anchoranalysis.plugin.image.task.feature.ResultsVectorWithThumbnail;

/**
 * Calculates features from the <b>metadata</b> from single image.
 *
 * <p>The image voxels should not be loaded in memory, to keep this as computationally efficient as
 * possible.
 *
 * <p>Each image's metadata produces a single row of features.
 *
 * <p>The columns produced are:
 *
 * <ol>
 *   <li>an image identifier
 *   <li>file extension (or empty if none exists) according to the procedure in {@link
 *       ExtensionUtilities}.
 *   <li>file creation date
 *   <li>file modification date
 *   <li><i>the results of each feature.</i>
 * </ol>
 *
 * <p>All dates are expressed in the current time-zone.
 */
public class FromImageMetadata
        extends SingleRowPerInput<ImageMetadataInput, FeatureInputImageMetadata> {

    private static final String[] NON_GROUP_HEADERS = {
        "image", "extension", "creationTime", "lastModifiedTime", "acqusitionTime"
    };

    public FromImageMetadata() {
        super(NON_GROUP_HEADERS);
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
    protected Optional<String[]> additionalLabelsFor(ImageMetadataInput input)
            throws OperationFailedException {
        try {
            ImageFileAttributes attributes = input.metadata().getFileAttributes();
            return Optional.of(
                    new String[] {
                        attributes.extension().orElse(""),
                        convertDate(attributes.getCreationTime()),
                        convertDate(attributes.getModificationTime()),
                        input.metadata().getAcqusitionTime().map(this::convertDate).orElse("")
                    });
        } catch (ImageIOException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    protected ResultsVectorWithThumbnail calculateResultsForInput(
            ImageMetadataInput input,
            FeatureCalculationContext<FeatureList<FeatureInputImageMetadata>> context)
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

    /** Converts a {@link Date} to an (appropriately-formatted) {@link String}. */
    private String convertDate(ZonedDateTime date) {
        return date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
