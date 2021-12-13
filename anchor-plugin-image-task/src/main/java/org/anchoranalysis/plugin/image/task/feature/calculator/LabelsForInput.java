package org.anchoranalysis.plugin.image.task.feature.calculator;

import java.util.Optional;
import org.anchoranalysis.feature.io.csv.metadata.RowLabels;

/**
 * Calculates labels for a given input (and index).
 *
 * @author Owen Feehan
 */
public interface LabelsForInput {

    /**
     * Calculates labels for a given input (and index).
     *
     * @param objectIdentifier a unique identifier for the object (within the context of whatever
     *     image it resides).
     * @param index the index in the input-collection for the particular input.
     * @return row-labels for the input.
     */
    RowLabels deriveLabels(String objectIdentifier, Optional<String> groupGeneratorName, int index);
}
