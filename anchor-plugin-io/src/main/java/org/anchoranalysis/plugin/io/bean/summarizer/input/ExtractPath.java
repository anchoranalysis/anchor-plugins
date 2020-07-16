/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer.input;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.io.input.InputFromManager;

public class ExtractPath<T extends InputFromManager> extends SummarizerInputFromManager<T, Path> {

    @Override
    protected Optional<Path> extractFrom(T input) {
        return input.pathForBinding();
    }
}
