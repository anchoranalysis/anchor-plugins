/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.bean.feature.source;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.system.path.ExtensionUtilities;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.feature.calculate.bound.FeatureCalculatorMulti;
import org.anchoranalysis.feature.initialization.FeatureInitialization;
import org.anchoranalysis.feature.results.ResultsVector;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.shared.SharedFeatures;
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
 *       ExtensionUtilities#extractExtension(String)}.
 *   <li>file creation date
 *   <li>file modification date
 *   <li><i>the results of each feature.</i>
 * </ol>
 *
 * <p>All dates are expressed in the current time-zone.
 */
public class FromImageMetadata
        extends SingleRowPerInput<ImageMetadataInput, FeatureInputImageMetadata> {

    /** Headers for non-group columns in the output. */
    private static final String[] NON_GROUP_HEADERS = {
        "image", "extension", "creationTime", "lastModifiedTime", "acquisitionTime"
    };

    /** Creates a new {@link FromImageMetadata} instance. */
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
                        input.metadata().getAcquisitionTime().map(this::convertDate).orElse("")
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
        return new ResultsVectorWithThumbnail(() -> calculateResults(input, context));
    }

    /**
     * Calculates the results vector for a given input.
     *
     * @param input the {@link ImageMetadataInput} to process
     * @param context the {@link FeatureCalculationContext} for calculation
     * @return a {@link ResultsVector} containing the calculated features
     * @throws OperationFailedException if the operation fails
     */
    private ResultsVector calculateResults(
            ImageMetadataInput input,
            FeatureCalculationContext<FeatureList<FeatureInputImageMetadata>> context)
            throws OperationFailedException {
        try {

            FeatureInitialization initialization = new FeatureInitialization();
            FeatureCalculatorMulti<FeatureInputImageMetadata> calculator =
                    FeatureSession.with(
                            context.getFeatureSource(),
                            initialization,
                            new SharedFeatures(),
                            context.getLogger());

            ImageMetadata metadata =
                    context.getExecutionTimeRecorder()
                            .recordExecutionTime("Reading image metadata", input::metadata);

            // Calculate the results for the current stack
            return context.getExecutionTimeRecorder()
                    .recordExecutionTime(
                            "Calculating features",
                            () -> calculator.calculate(new FeatureInputImageMetadata(metadata)));
        } catch (InitializeException | ImageIOException | NamedFeatureCalculateException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Converts a {@link ZonedDateTime} to an (appropriately-formatted) {@link String}.
     *
     * @param date the {@link ZonedDateTime} to convert
     * @return a formatted {@link String} representation of the date
     */
    private String convertDate(ZonedDateTime date) {
        return date.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
