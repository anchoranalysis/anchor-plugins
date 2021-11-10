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

package org.anchoranalysis.plugin.image.task.bean.feature.source;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.exception.BeanDuplicateException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calculate.NamedFeatureCalculateException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.RowLabels;
import org.anchoranalysis.feature.io.name.SimpleName;
import org.anchoranalysis.feature.io.results.FeatureOutputNames;
import org.anchoranalysis.feature.io.results.LabelHeaders;
import org.anchoranalysis.feature.store.NamedFeatureStore;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.plugin.image.task.feature.FeatureCalculationContext;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporter;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporterContext;
import org.anchoranalysis.plugin.image.task.feature.LabelHeadersForCSV;
import org.anchoranalysis.plugin.image.task.feature.LabelledResultsVectorWithThumbnail;
import org.anchoranalysis.plugin.image.task.feature.ResultsVectorWithThumbnail;

/**
 * Base class for exporting features, where features are calculated per-image using a {@link
 * NamedFeatureStore}.
 *
 * @author Owen Feehan
 * @param <T> input-manager type
 * @param <S> feature-input type
 */
@AllArgsConstructor
public abstract class SingleRowPerInput<T extends InputFromManager, S extends FeatureInput>
        extends FeatureSource<T, FeatureList<S>, S> {

    /** The column names (not pertaining to groups), the first of which should refer to an identifier. */
    private String[] nonGroupHeaders;
    
    /**
     * Creates with a single non-group header that should be describe an identifier.
     * 
     * @param headerIdentifier the column-name to describe an identifier.
     */
    protected SingleRowPerInput(String headerIdentifier) {
        nonGroupHeaders = new String[]{headerIdentifier};
    }

    @Override
    public FeatureExporter<FeatureList<S>> createExporter(
            LabelHeaders metadataHeaders,
            List<NamedBean<FeatureListProvider<S>>> features,
            FeatureOutputNames outputNames,
            FeatureExporterContext context)
            throws CreateException {
        return FeatureExporter.create(features, metadataHeaders, outputNames, context);
    }

    @Override
    public LabelHeaders headers(boolean groupsEnabled) {
        return LabelHeadersForCSV.createHeaders(nonGroupHeaders, Optional.empty(), groupsEnabled);
    }

    @Override
    public void calculateAndOutput(T input, FeatureCalculationContext<FeatureList<S>> context)
            throws OperationFailedException {
        try {
            ResultsVectorWithThumbnail results = calculateResultsForInput(input, context);
            LabelledResultsVectorWithThumbnail labelledResults =
                    new LabelledResultsVectorWithThumbnail(
                            labelsFor(input, context.getGroupGeneratorName()),
                            results);
            context.addResults(labelledResults);

        } catch (BeanDuplicateException | NamedFeatureCalculateException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Calculates feature-results for a particular input.
     * 
     * @param input the input.
     * @param context context for calculating features.
     * @return the results, with optionally associated thumbnail.
     * @throws NamedFeatureCalculateException if any feature cannot calculate.
     */
    protected abstract ResultsVectorWithThumbnail calculateResultsForInput(
            T input, FeatureCalculationContext<FeatureList<S>> context)
            throws NamedFeatureCalculateException;

    /**
     * Additional labels for an input to include (after the identifier, and before any group labels).
     * 
     * <p>These should always correspond (when appended to the identifier) exactly to the {@code nonGroupHeaders}.
     * 
     * @param input the input.
     * @return any additional labels for the input.
     */
    protected abstract Optional<String[]> additionalLabelsFor(T input) throws OperationFailedException;

    /** Row-labels for a particular input. */
    private RowLabels labelsFor(T input, Optional<String> groupGeneratorName) throws OperationFailedException {
        Optional<String[]> additionalLabels = additionalLabelsFor(input);
        String[] nonGroupLabels = combine(input.identifier(), additionalLabels);
        if (nonGroupLabels.length!=nonGroupHeaders.length) {
            throw new OperationFailedException(String.format("There were %d non-group labels, when %d were expected.", nonGroupLabels.length, nonGroupHeaders.length));
        }
        return new RowLabels(
                Optional.of(nonGroupLabels), groupGeneratorName.map(SimpleName::new));
    }
    
    /** Combines a {@link String} with optional additional others to form an array. */
    private static String[] combine(String identifier, Optional<String[]> others) {
        if (others.isPresent()) {
            String[] out = new String[others.get().length+1];
            out[0] = identifier;
            System.arraycopy(others.get(), 0, out, 1, others.get().length);
            return out;
        } else {
            return new String[] {identifier};
        }
    }
}
