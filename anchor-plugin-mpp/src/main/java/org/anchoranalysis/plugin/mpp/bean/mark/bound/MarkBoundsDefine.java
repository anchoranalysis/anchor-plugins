/* (C)2020 */
package org.anchoranalysis.plugin.mpp.bean.mark.bound;

import org.anchoranalysis.anchor.mpp.bean.bound.MarkBounds;
import org.anchoranalysis.anchor.mpp.bean.provider.MarkBoundsProvider;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;

public class MarkBoundsDefine extends MarkBoundsProvider {

    // START BEAN FIELDS
    @BeanField private MarkBounds markBounds;
    // END BEAN FIELDS

    public MarkBoundsDefine() {
        // Standard bean constructor
    }

    public MarkBoundsDefine(MarkBounds markBounds) {
        this.markBounds = markBounds;
    }

    @Override
    public MarkBounds create() throws CreateException {
        return markBounds;
    }

    public MarkBounds getMarkBounds() {
        return markBounds;
    }

    public void setMarkBounds(MarkBounds markBounds) {
        this.markBounds = markBounds;
    }
}
