/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer.input;

import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.io.input.InputFromManager;

public class ExtractDescriptiveNameAndPath<T extends InputFromManager>
        extends SummarizerInputFromManager<T, String> {

    @Override
    protected Optional<String> extractFrom(T input) {
        return Optional.of(
                String.format(
                        "%s\t -> %s",
                        input.descriptiveName(),
                        input.pathForBinding().map(Path::toString).orElse("")));
    }
}
