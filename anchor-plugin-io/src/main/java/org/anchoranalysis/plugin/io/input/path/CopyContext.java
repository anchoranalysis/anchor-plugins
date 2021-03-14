package org.anchoranalysis.plugin.io.input.path;

import java.nio.file.Path;
import lombok.Value;

/**
 * Context parameters when copying many files from a source directory to a destination directory.
 *
 * @author Owen Feehan
 * @param <T> shared-state type
 */
@Value
public class CopyContext<T> {

    /** Source directory of copying. */
    Path sourceDirectory;

    /** Destination directory for copying. */
    Path destinationDirectory;

    /** State shared across all inputs when copying. */
    T sharedState;
}
