/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import java.util.Optional;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedVoxelBox;

/**
 * Writes all points in a particular connected-component using the same ID
 *
 * @author Owen Feehan
 */
final class ConnectedComponentWriter {

    private final EncodedVoxelBox matS;
    private final Optional<MinimaStore> minimaStore;

    /** Keeps track of the IDs used */
    private int id = -1;

    public ConnectedComponentWriter(EncodedVoxelBox matS, Optional<MinimaStore> minimaStore) {
        super();
        this.matS = matS;
        this.minimaStore = minimaStore;
    }

    /** @param point a point that is treated immutably */
    public void writePoint(Point3i point) {
        // We write a connected component id based upon the first voxel encountered
        if (id == -1) {
            id = matS.extent().offset(point);

            if (minimaStore.isPresent()) {
                minimaStore.get().addDuplicated(point);
            }
        }

        matS.setPointConnectedComponentID(point, id);
    }
}
