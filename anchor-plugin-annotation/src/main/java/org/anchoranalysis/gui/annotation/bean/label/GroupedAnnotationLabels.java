/* (C)2020 */
package org.anchoranalysis.gui.annotation.bean.label;

import java.util.Collection;
import java.util.Set;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;

/** A set of annotation labels partitoned into their unique groups */
public class GroupedAnnotationLabels {

    // <String,AnnotationLabel>
    private MultiMap map = new MultiValueMap();

    public GroupedAnnotationLabels(Collection<AnnotationLabel> labels) {

        for (AnnotationLabel lab : labels) {
            map.put(lab.getGroup(), lab);
        }
    }

    public int numGroups() {
        return map.keySet().size();
    }

    @SuppressWarnings("unchecked")
    public Set<String> keySet() {
        return (Set<String>) map.keySet();
    }

    @SuppressWarnings("unchecked")
    public Collection<AnnotationLabel> get(String key) {
        return (Collection<AnnotationLabel>) map.get(key);
    }
}
