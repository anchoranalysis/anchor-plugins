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
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.NamedFeatureCalculationException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.LabelHeaders;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.io.csv.name.SimpleName;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.image.task.feature.InputProcessContext;
import org.anchoranalysis.plugin.image.task.feature.GenerateHeadersForCSV;
import org.anchoranalysis.plugin.image.task.feature.ResultsVectorWithThumbnail;
import org.anchoranalysis.plugin.image.task.feature.SharedStateExportFeatures;
import lombok.AllArgsConstructor;

/**
 * Base class for exporting features, where features are calculated per-image using a
 * NamedFeatureStore
 *
 * @author Owen Feehan
 * @param T input-manager type
 * @param S feature-input type
 */
@AllArgsConstructor
public abstract class SingleRowPerInput<T extends InputFromManager, S extends FeatureInput>
        extends FeatureSource<T, FeatureList<S>, S> {

    private static final NamedFeatureStoreFactory STORE_FACTORY =
            NamedFeatureStoreFactory.factoryParamsOnly();

    /** The first column-name in the CSV file that is outputted. */
    private String firstResultHeader;

    @Override
    public SharedStateExportFeatures<FeatureList<S>> createSharedState(
            LabelHeaders metadataHeaders,
            List<NamedBean<FeatureListProvider<S>>> features,
            BoundIOContext context)
            throws CreateException {
        try {
            NamedFeatureStore<S> store = STORE_FACTORY.createNamedFeatureList(features);
            return new SharedStateExportFeatures<>(
                    metadataHeaders,
                    store.createFeatureNames(),
                    store.deepCopy()::listFeatures,
                    context);
        } catch (AnchorIOException e) {
            throw new CreateException(e);
        }
    }

    @Override
    public GenerateHeadersForCSV headers() {
        return new GenerateHeadersForCSV(new String[] {firstResultHeader}, Optional.empty());
    }

    @Override
    public void processInput(
            T input,
            InputProcessContext<FeatureList<S>> context)
            throws OperationFailedException {
        try {
            ResultsVectorWithThumbnail results =
                    calculateResultsForInput(input, context);

            context.addResultsFor(
                    identifierFor(input.descriptiveName(), context.getGroupGeneratorName()),
                    results 
            );

        } catch (BeanDuplicateException | NamedFeatureCalculationException e) {
            throw new OperationFailedException(e);
        }
    }

    protected abstract ResultsVectorWithThumbnail calculateResultsForInput(
        T inputObject,
        InputProcessContext<FeatureList<S>> context
    ) throws NamedFeatureCalculationException;
    
    private static StringLabelsForCsvRow identifierFor(
            String descriptiveName, Optional<String> groupGeneratorName)
            throws OperationFailedException {
        return new StringLabelsForCsvRow(
                Optional.of(new String[] {descriptiveName}),
                groupGeneratorName.map(SimpleName::new));
    }
}
