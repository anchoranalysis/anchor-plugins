package org.anchoranalysis.plugin.image.task.feature;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.anchoranalysis.feature.calc.results.ResultsVector;
import org.anchoranalysis.image.stack.DisplayStack;

/**
 * A results-vector with an optional thumbnail associated with it
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
@Value
public class ResultsVectorWithThumbnail {

    private ResultsVector resultsVector;
    private Optional<DisplayStack> thumbnail;

    public ResultsVectorWithThumbnail(ResultsVector resultsVector) {
        this.resultsVector = resultsVector;
        this.thumbnail = Optional.empty();
    }
}
