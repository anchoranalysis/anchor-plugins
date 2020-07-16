/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.filter.combine;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.bean.object.ObjectFilter;

/**
 * A filter that combines other filters (in a list)
 *
 * @author Owen Feehan
 */
public abstract class ObjectFilterCombine extends ObjectFilter {

    // START BEAN PROPERTIES
    /** A list of other filters from which the sub-classes implement filtering behaviour. */
    @BeanField private List<ObjectFilter> list = new ArrayList<>();
    // END BEAN PROPERTIES

    public List<ObjectFilter> getList() {
        return list;
    }

    public void setList(List<ObjectFilter> list) {
        this.list = list;
    }
}
