/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.aggregate;

import java.util.ArrayList;
import java.util.List;
import org.anchoranalysis.core.error.CreateException;

class AggregateSharedState {

    private List<ImageAnnotation> annotations;

    public AggregateSharedState() throws CreateException {
        this.annotations = new ArrayList<>();
    }

    public List<ImageAnnotation> getAnnotations() {
        return annotations;
    }
}
