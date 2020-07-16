/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.border;

import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;

public class AtBorderZ extends AtBorderBase {

    @Override
    protected boolean isBoundingBoxAtBorder(BoundingBox boundingBox, ImageDimensions dim) {
        return boundingBox.atBorderZ(dim);
    }
}
