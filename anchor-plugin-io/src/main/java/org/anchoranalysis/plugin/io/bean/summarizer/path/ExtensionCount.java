/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.io.bean.summarizer.path;

import java.nio.file.Path;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;
import org.anchoranalysis.plugin.io.shared.FrequencyMap;
import org.apache.commons.io.FilenameUtils;

/** Remembers each unique extension, and associated count */
public class ExtensionCount extends Summarizer<Path> {

    private static final String NO_EXTENSION = "NO_EXTENSION";

    private FrequencyMap<String> map = new FrequencyMap<>();

    @Override
    public void add(Path fullFilePath) {

        String extension = FilenameUtils.getExtension(fullFilePath.toString());

        map.incrementCount(tidyExtension(extension));
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
