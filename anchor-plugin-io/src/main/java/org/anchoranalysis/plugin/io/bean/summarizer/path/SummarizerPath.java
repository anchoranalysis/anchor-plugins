package org.anchoranalysis.plugin.io.bean.summarizer.path;

import java.nio.file.Path;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;

/**
 * Base class for {@link Summarizer}s that operate on the {@link Path} associated with inputs.
 *
 * @author Owen Feehan
 */
public abstract class SummarizerPath extends Summarizer<Path> {

    @Override
    public boolean requiresImageMetadata() {
        return false;
    }
}
