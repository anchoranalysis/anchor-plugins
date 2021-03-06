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

import java.io.IOException;
import java.util.Optional;
import lombok.Getter;
import org.anchoranalysis.core.concurrency.ConcurrentModelPool;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.feature.io.results.LabelHeaders;
import org.anchoranalysis.feature.io.results.ResultsWriterOutputNames;
import org.anchoranalysis.image.feature.calculator.FeatureTableCalculator;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.feature.InputProcessContext;
import org.anchoranalysis.plugin.image.task.feature.SharedStateExportFeatures;

/**
 * Shared-state for instance segmentation
 *
 * @author Owen Feehan
 * @param <T> model-type in pool
 */
public class SharedStateSegmentInstance<T> {

    public static final String OUTPUT_SUMMARY_CSV = "summary";

    private static final ResultsWriterOutputNames OUTPUT_RESULTS =
            new ResultsWriterOutputNames(OUTPUT_SUMMARY_CSV, false, false);

    private final SharedStateExportFeatures<FeatureTableCalculator<FeatureInputSingleObject>>
            features;
    @Getter private final ConcurrentModelPool<T> modelPool;

    public SharedStateSegmentInstance(
            ConcurrentModelPool<T> modelPool,
            FeatureTableCalculator<FeatureInputSingleObject> featureTable,
            LabelHeaders identifierHeaders,
            InputOutputContext context)
            throws CreateException {
        this.modelPool = modelPool;
        this.features =
                SharedStateExportFeatures.createForFeatures(
                        OUTPUT_RESULTS, featureTable, identifierHeaders, context);
    }

    public InputProcessContext<FeatureTableCalculator<FeatureInputSingleObject>>
            createInputProcessContext(Optional<String> groupName, InputOutputContext context) {
        return features.createInputProcessContext(groupName, context);
    }

    public void closeAnyOpenIO() throws IOException {
        features.closeAnyOpenIO();
    }
}
