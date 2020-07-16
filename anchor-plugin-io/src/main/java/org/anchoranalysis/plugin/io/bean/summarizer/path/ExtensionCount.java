/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.summarizer.path;

import java.nio.file.Path;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;
import org.anchoranalysis.plugin.io.summarizer.FrequencyMap;
import org.apache.commons.io.FilenameUtils;

/** Remembers each unique extension, and associated count */
public class ExtensionCount extends Summarizer<Path> {

    private static final String NO_EXTENSION = "NO_EXTENSION";

    private FrequencyMap<String> map = new FrequencyMap<>();

    @Override
    public void add(Path fullFilePath) {

        String extension = FilenameUtils.getExtension(fullFilePath.toString());

        map.incrCount(tidyExtension(extension));
    }

    // Describes all the extensions found
    @Override
    public String describe() {
        return map.describe("extension");
    }

    private static String tidyExtension(String extension) {

        if (extension.isEmpty()) {
            return NO_EXTENSION;
        } else {
            return extension.toLowerCase();
        }
    }
}
