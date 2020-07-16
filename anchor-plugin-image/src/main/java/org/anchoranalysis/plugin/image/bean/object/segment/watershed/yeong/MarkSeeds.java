/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import java.util.Optional;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.seed.Seed;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.image.voxel.iterator.ProcessVoxel;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedVoxelBox;

class MarkSeeds {

    private MarkSeeds() {}

    public static void apply(
            SeedCollection seeds,
            EncodedVoxelBox matS,
            Optional<MinimaStore> minimaStore,
            Optional<ObjectMask> containingMask)
            throws SegmentationFailedException {

        if (containingMask.isPresent()
                && !matS.extent().equals(containingMask.get().getBoundingBox().extent())) {
            throw new SegmentationFailedException("Extent of matS does not match containingMask");
        }

        for (Seed s : seeds) {

            ObjectMask mask = s.createMask();

            throwExceptionIfNotConnected(mask);

            IterateVoxels.overMasks(mask, containingMask, createPointProcessor(matS, minimaStore));
        }
    }

    private static ProcessVoxel createPointProcessor(
            EncodedVoxelBox matS, Optional<MinimaStore> minimaStore) {
        ConnectedComponentWriter ccWriter = new ConnectedComponentWriter(matS, minimaStore);
        return ccWriter::writePoint;
    }

    private static void throwExceptionIfNotConnected(ObjectMask obj)
            throws SegmentationFailedException {
        try {
            if (!obj.checkIfConnected()) {
                throw new SegmentationFailedException("Seed must be a single connected-component");
            }
        } catch (OperationFailedException e) {
            throw new SegmentationFailedException(
                    "Cannot determine if a seed is a connected-component", e);
        }
    }
}
