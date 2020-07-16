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
/* (C)2020 */
package org.anchoranalysis.plugin.image.task.feature;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.Getter;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.feature.io.csv.GroupedResultsVectorCollection;
import org.anchoranalysis.feature.io.csv.MetadataHeaders;
import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.name.FeatureNameList;
import org.anchoranalysis.feature.resultsvectorcollection.FeatureInputResults;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.bound.BoundIOContext;

/**
 * Shared-state for an export-features class
 *
 * @param <S> a source-of-features that is duplicated for each new thread (to prevent any
 *     concurrency issues)
 * @author Owen Feehan
 */
public class SharedStateExportFeatures<S> {

    private final GroupedResultsVectorCollection groupedResults;

    @Getter private final FeatureNameList featureNames;

    private final Supplier<S> featureSourceSupplier;

    public SharedStateExportFeatures(
            MetadataHeaders metadataHeaders,
            FeatureNameList featureNames,
            Supplier<S> featureSourceSupplier,
            BoundIOContext context)
            throws AnchorIOException {
        this.featureNames = featureNames;
        this.featureSourceSupplier = featureSourceSupplier;
        this.groupedResults =
                new GroupedResultsVectorCollection(metadataHeaders, featureNames, context);
    }

    public void addResultsFor(StringLabelsForCsvRow labels, ResultsVector results) {
        groupedResults.addResultsFor(labels, results);
    }

    /**
     * Writes all the results that have been collected as a CSV file
     *
     * @param <T> feature input-type
     * @param featuresAggregate features that can be used for generating additional "aggregated"
     *     exports
     * @param includeGroups iff TRUE a group-column is included in the CSV file and the group
     *     exports occur, otherwise not
     * @param context io-context
     * @throws AnchorIOException
     */
    public void writeGroupedResults(
            Optional<NamedFeatureStore<FeatureInputResults>> featuresAggregate,
            boolean includeGroups,
            BoundIOContext context)
            throws AnchorIOException {
        groupedResults.writeResultsForAllGroups(featuresAggregate, includeGroups, context);
    }

    public S duplicateForNewThread() {
        return featureSourceSupplier.get();
    }

    public void closeAnyOpenIO() throws IOException {
        groupedResults.close();
    }
}
