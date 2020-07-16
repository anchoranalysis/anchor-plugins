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
