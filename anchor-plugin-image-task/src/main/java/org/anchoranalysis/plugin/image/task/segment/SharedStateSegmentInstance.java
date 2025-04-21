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
package org.anchoranalysis.plugin.image.task.segment;

import java.util.Optional;
import lombok.Getter;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.time.ExecutionTimeRecorder;
import org.anchoranalysis.feature.io.csv.metadata.LabelHeaders;
import org.anchoranalysis.feature.io.results.FeatureOutputNames;
import org.anchoranalysis.image.feature.calculator.FeatureTableCalculator;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.inference.InferenceModel;
import org.anchoranalysis.inference.concurrency.ConcurrentModelPool;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.bean.feature.ExportFeaturesStyle;
import org.anchoranalysis.plugin.image.task.feature.FeatureCalculationContext;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporter;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporterContext;

/**
 * Shared-state for instance segmentation.
 *
 * @author Owen Feehan
 * @param <T> model-type in pool
 */
public class SharedStateSegmentInstance<T extends InferenceModel> {

    /** The feature exporter for the instance segmentation. */
    private final FeatureExporter<FeatureTableCalculator<FeatureInputSingleObject>> features;

    /** The concurrent model pool for inference. */
    @Getter private final ConcurrentModelPool<T> modelPool;

    /** The context for feature exporting. */
    private final FeatureExporterContext context;

    /**
     * Creates a new instance of SharedStateSegmentInstance.
     *
     * @param modelPool the concurrent model pool
     * @param featureTable the feature table calculator
     * @param identifierHeaders the label headers for identifiers
     * @param outputNameFeatures the output name for features
     * @param context the feature exporter context
     * @throws CreateException if there's an error creating the feature exporter
     */
    public SharedStateSegmentInstance(
            ConcurrentModelPool<T> modelPool,
            FeatureTableCalculator<FeatureInputSingleObject> featureTable,
            LabelHeaders identifierHeaders,
            String outputNameFeatures,
            FeatureExporterContext context)
            throws CreateException {
        this.modelPool = modelPool;

        FeatureOutputNames outputNames = new FeatureOutputNames(outputNameFeatures, false, false);

        this.features =
                FeatureExporter.create(
                        outputNames, featureTable, identifierHeaders, Optional.empty(), context);
        this.context = context;
    }

    /**
     * Creates a {@link FeatureCalculationContext} for calculating features to later use this
     * exporter.
     *
     * @param executionTimeRecorder records execution-times.
     * @param ioContext IO-context.
     * @return a newly created context.
     */
    public FeatureCalculationContext<FeatureTableCalculator<FeatureInputSingleObject>>
            createCalculationContext(
                    ExecutionTimeRecorder executionTimeRecorder, InputOutputContext ioContext) {
        return features.createCalculationContext(
                Optional.empty(), executionTimeRecorder, ioContext);
    }

    /**
     * Writes all the results that have been collected as a CSV file, and closes open I/O handles
     * and memory structures.
     *
     * @param style the style for exporting features
     * @throws OutputWriteFailedException if any output cannot be written, or there is an error
     *     closing open I/O.
     */
    public void closeAndWriteOutputs(ExportFeaturesStyle style) throws OutputWriteFailedException {
        features.closeAndWriteOutputs(
                Optional.empty(),
                false,
                contextForWriter -> style.deriveContext(contextForWriter)::csvWriter,
                context.getContext());
    }
}
