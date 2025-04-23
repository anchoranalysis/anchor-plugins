/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.bean.label;

import java.util.Collection;
import java.util.Set;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

/** A set of {@link AnnotationLabel}s partitioned into their unique groups. */
public class GroupedAnnotationLabels {

    // Key, Value types are <String,AnnotationLabel>
    private MultiMap map = new MultiValueMap();

    /**
     * Creates a new instance of GroupedAnnotationLabels.
     *
     * @param labels the collection of {@link AnnotationLabel}s to group.
     */
    public GroupedAnnotationLabels(Collection<AnnotationLabel> labels) {
        labels.forEach(label -> map.put(label.getGroup(), label));
    }

    /**
     * Gets the number of unique groups.
     *
     * @return the number of groups.
     */
    public int numberGroups() {
        return map.keySet().size();
    }

    /**
     * Gets the set of group keys.
     *
     * @return a set of group keys.
     */
    @SuppressWarnings("unchecked")
    public Set<String> keySet() {
        return map.keySet();
    }

    /**
     * Gets the collection of {@link AnnotationLabel}s for a specific group.
     *
     * @param key the group key.
     * @return a collection of {@link AnnotationLabel}s belonging to the specified group.
     */
    @SuppressWarnings("unchecked")
    public Collection<AnnotationLabel> get(String key) {
        return (Collection<AnnotationLabel>) map.get(key);
    }
}
