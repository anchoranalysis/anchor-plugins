/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.labeller;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.anchoranalysis.io.manifest.ManifestDirectoryDescription;
import org.anchoranalysis.io.manifest.sequencetype.StringsWithoutOrder;
import org.anchoranalysis.io.output.outputter.Outputter;

/** A set of outputters, one for each group */
class GroupedMultiplexOutputters {

    private static final ManifestDirectoryDescription MANIFEST_DESCRIPTION =
            new ManifestDirectoryDescription(
                    "groupOutput", "outputManager", new StringsWithoutOrder());

    private Map<String, Outputter> map;

    public GroupedMultiplexOutputters(Outputter baseOutputter, Set<String> groups) {

        map = new TreeMap<>();

        for (String key : groups) {
            map.put(key, baseOutputter.deriveSubdirectory(key, MANIFEST_DESCRIPTION, false));
        }
    }

    // Returns null if no object exists
    public Outputter getOutputterFor(String key) {
        return map.get(key);
    }
}
