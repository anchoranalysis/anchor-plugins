/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer.input;

import java.util.Optional;
import org.anchoranalysis.io.input.InputFromManager;

public class ExtractDescriptiveName<T extends InputFromManager>
        extends SummarizerInputFromManager<T, String> {

    @Override
    protected Optional<String> extractFrom(T input) {
        return Optional.of(input.descriptiveName());
    }
}
