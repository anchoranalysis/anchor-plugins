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
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.experiment.task.InputTypesExpected;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.io.csv.metadata.LabelHeaders;
import org.anchoranalysis.feature.io.results.FeatureOutputNames;
import org.anchoranalysis.io.input.InputFromManager;
import org.anchoranalysis.io.input.grouper.InputGrouper;
import org.anchoranalysis.plugin.image.task.feature.FeatureCalculationContext;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporter;
import org.anchoranalysis.plugin.image.task.feature.FeatureExporterContext;

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

    /**
     * Creates the {@link FeatureExporter} to be used for calculating and exporting feature-results.
     *
     * @param metadataHeaders headers to use for additional "metadata" before feature-results.
     * @param features the features to calculate.
     * @param outputNames the names of various kind of outputs.
     * @param grouper when defined, assigns each input to a group.
     * @param context IO-context.
     * @return a newly created {@link FeatureExporter} as matches this source of features.
     * @throws CreateException if it cannot be created.
     */
    public abstract FeatureExporter<S> createExporter(
            LabelHeaders metadataHeaders,
            List<NamedBean<FeatureListProvider<U>>> features,
            FeatureOutputNames outputNames,
            Optional<InputGrouper> grouper,
            FeatureExporterContext context)
            throws CreateException;

    /**
     * Determines if group columns should be added to the CSV exports and other group exports may
     * occur in sub-directories.
     *
     * @param groupGeneratorDefined has a group-generator been defined for this experiment?
     * @return true if a group-generator has been defined, false otherwise
     */
    public abstract boolean includeGroupInExperiment(boolean groupGeneratorDefined);

    /**
     * Generates label-headers for the non-feature-result columns in the CSV.
     *
     * @param groupsEnabled whether groups are enabled
     * @return a {@link LabelHeaders} object for the non-feature-result columns
     */
    public abstract LabelHeaders headers(boolean groupsEnabled);

    /**
     * Processes one input to calculate feature-results and output them to the file-system.
     *
     * @param input one particular input that will create one or more "rows" in a feature-table
     * @param context the {@link FeatureCalculationContext} for calculation
     * @throws OperationFailedException if the operation fails
     */
    public abstract void calculateAndOutput(T input, FeatureCalculationContext<S> context)
            throws OperationFailedException;

    /**
     * Specifies the highest class(es) that will function as a valid input.
     *
     * <p>This is usually the class of T (or sometimes the absolute base class {@link
     * InputFromManager})
     *
     * @return an {@link InputTypesExpected} object specifying the expected input types
     */
    public abstract InputTypesExpected inputTypesExpected();
}
