/* (C)2020 */
package org.anchoranalysis.plugin.image.task.bean.labeller;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.experiment.task.NoSharedState;

/** @author Owen Feehan */
public abstract class BinaryOutcomeImageLabeller extends ImageLabeller<NoSharedState> {

    private static final String POSITIVE = "positive";
    private static final String NEGATIVE = "negative";

    @Override
    public NoSharedState init(Path pathForBinding) throws InitException {
        return NoSharedState.INSTANCE;
    }

    @Override
    public Set<String> allLabels(NoSharedState sharedState) {
        return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(POSITIVE, NEGATIVE)));
    }

    protected static String classificationString(boolean positive) {
        return positive ? POSITIVE : NEGATIVE;
    }
}
