package org.anchoranalysis.plugin.image.task.feature;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.anchoranalysis.feature.io.csv.RowLabels;
import org.anchoranalysis.feature.io.results.LabelledResultsVector;

/**
 * Like a {@link ResultsVectorWithThumbnail} but additionally contains labels to describe the
 * calculated results.
 *
 * @author Owen Feehan
 */
@Value
@AllArgsConstructor
public class LabelledResultsVectorWithThumbnail {

    /** The labels. */
    private final RowLabels labels;

    /** The results. */
    private final ResultsVectorWithThumbnail results;

    /**
     * Exposes as a {@link LabelledResultsVector} without a thumbnail.
     *
     * @return a newly created {@link LabelledResultsVector} derived from the current object.
     */
    public LabelledResultsVector withoutThumbnail() {
        return new LabelledResultsVector(labels, results.getResultsVector());
    }
}
