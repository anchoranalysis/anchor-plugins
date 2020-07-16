/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.boundingbox;

import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.image.extent.BoundingBox;

public class BoundingBoxMinimumAlongAxis extends BoundingBoxAlongAxisBase {

    @Override
    protected ReadableTuple3i extractTupleForBoundingBox(BoundingBox bbox) {
        return bbox.cornerMin();
    }
}
