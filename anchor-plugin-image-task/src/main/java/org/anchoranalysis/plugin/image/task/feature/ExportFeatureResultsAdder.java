package org.anchoranalysis.plugin.image.task.feature;

import org.anchoranalysis.feature.io.csv.StringLabelsForCsvRow;

/**
 * Adds results (a row in a feature-table) for export-features
 * 
 * @author Owen Feehan
 *
 */
@FunctionalInterface
public interface ExportFeatureResultsAdder {
    
    /**
     * Adds labeled results (which makes a row in a feature-table, and possibly other outputs).
     * 
     * @param labels labels for results
     * @param results the results
     */
    void addResultsFor(StringLabelsForCsvRow labels, ResultsVectorWithThumbnail results);
}
