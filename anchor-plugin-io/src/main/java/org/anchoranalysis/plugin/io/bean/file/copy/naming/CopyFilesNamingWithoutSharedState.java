package org.anchoranalysis.plugin.io.bean.file.copy.naming;

import java.nio.file.Path;
import org.anchoranalysis.experiment.task.NoSharedState;

/**
 * Base class for implementations of {@link CopyFilesNaming} where each file is copied independently
 * of the others.
 *
 * @author Owen Feehan
 */
public abstract class CopyFilesNamingWithoutSharedState extends CopyFilesNaming<NoSharedState> {

    @Override
    public NoSharedState beforeCopying(Path destinationDirectory, int totalNumberFiles) {
        // NOTHING TO DO
        return NoSharedState.INSTANCE;
    }
}
