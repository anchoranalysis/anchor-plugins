/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.anchoranalysis.core.geometry.Point3i;

// Stores the voxels for a plateau
@AllArgsConstructor
class PointWithNeighbor {

    @Getter private final Point3i point;

    @Getter private final int neighborIndex;
}
