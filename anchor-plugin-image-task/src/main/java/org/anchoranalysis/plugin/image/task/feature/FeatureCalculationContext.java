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
package org.anchoranalysis.plugin.image.task.feature;

import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.checked.CheckedConsumer;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.io.output.outputter.InputOutputContext;

/**
 * The context in which features are calculated, so as to be later exported as a CSV.
 *
 * @author Owen Feehan
 * @param <S> row-source that is duplicated for each new thread (to prevent any concurrency issues)
 */
public class FeatureCalculationContext<S> {

    // START REQUIRED ARGUMENTS
    /** Adds results (a row in a feature-table) for export-features. */
    private CheckedConsumer<LabelledResultsVectorWithThumbnail, OperationFailedException> adder;

    @Getter private S rowSource;

    @Getter private FeatureNameList featureNames;

    @Getter private Optional<String> groupGeneratorName;

    /** Records the execution-time of particular operations. */
    @Getter private ExecutionTimeRecorder executionTimeRecorder;

    @Getter private InputOutputContext context;
    // END REQUIRED ARGUMENTS

    @Getter private boolean thumbnailsEnabled;

    /**
     * Default constructor.
     *
     * @param adder adds results (a row in a feature-table) for export-features.
     * @param rowSource
     * @param featureNames
     * @param groupGeneratorName
     * @param executionTimeRecorder
     * @param context
     */
    public FeatureCalculationContext(
            CheckedConsumer<LabelledResultsVectorWithThumbnail, OperationFailedException> adder,
            S rowSource,
            FeatureNameList featureNames,
            Optional<String> groupGeneratorName,
            ExecutionTimeRecorder executionTimeRecorder,
            InputOutputContext context) {
        this.adder = adder;
        this.rowSource = rowSource;
        this.featureNames = featureNames;
        this.groupGeneratorName = groupGeneratorName;
        this.executionTimeRecorder = executionTimeRecorder;
        this.context = context;
        this.thumbnailsEnabled = areThumbnailsEnabled(context);
    }

    public Path getModelDirectory() {
        return context.getModelDirectory();
    }

    public Logger getLogger() {
        return context.getLogger();
    }

    public void addResults(LabelledResultsVectorWithThumbnail results)
            throws OperationFailedException {
        adder.accept(results);
    }

    private boolean areThumbnailsEnabled(InputOutputContext context) {
        return context.getOutputter()
                .outputsEnabled()
                .isOutputEnabled(FeatureExporter.OUTPUT_THUMBNAILS);
    }
}
