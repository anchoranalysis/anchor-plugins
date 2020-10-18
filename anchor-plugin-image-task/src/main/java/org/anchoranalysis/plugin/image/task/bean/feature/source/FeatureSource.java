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
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.results.LabelHeaders;
import org.anchoranalysis.feature.io.results.ResultsWriterOutputNames;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.image.task.feature.GenerateLabelHeadersForCSV;
import org.anchoranalysis.plugin.image.task.feature.InputProcessContext;
import org.anchoranalysis.plugin.image.task.feature.SharedStateExportFeatures;

/**
 * Extracts features from some kind of inputs to produce one or more rows in a feature-table.
 *
 * @author Owen Feehan
 * @param <T> input-type from which one or more rows of features are derived
 * @param <S> row-source that is duplicated for each new thread (to prevent any concurrency issues)
 * @param <U> feature-input type for {@code features} bean-field
 */
public abstract class FeatureSource<T extends InputFromManager, S, U extends FeatureInput>
        extends AnchorBean<FeatureSource<T, S, U>> {

    public abstract SharedStateExportFeatures<S> createSharedState(
            LabelHeaders metadataHeaders,
            List<NamedBean<FeatureListProvider<U>>> features,
            ResultsWriterOutputNames outputNames,
            InputOutputContext context)
            throws CreateException;

    /**
     * Iff true, group columns are added to the CSV exports, and other group exports may occur in
     * sub-directories.
     *
     * @param groupGeneratorDefined has a group-generator been defined for this experiment?
     * @return true iff a group-generator has been defined
     */
    public abstract boolean includeGroupInExperiment(boolean groupGeneratorDefined);

    public abstract GenerateLabelHeadersForCSV headers();

    /**
     * Processes one input to generate features.
     *
     * @param input one particular input that will creates one or more "rows" in a feature-table
     * @param context io-context
     * @throws OperationFailedException
     */
    public abstract void processInput(T input, InputProcessContext<S> context)
            throws OperationFailedException;

    /**
     * Highest class(es) that will function as a valid input.
     *
     * <p>This is usually the class of T (or sometimes the absolute base class InputFromManager)
     */
    public abstract InputTypesExpected inputTypesExpected();
}
