/*-
 * #%L
 * anchor-plugin-mpp-sgmn
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

package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import lombok.RequiredArgsConstructor;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMembership;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.voxelized.VoxelizedMark;
import org.anchoranalysis.anchor.mpp.mark.voxelized.memo.VoxelizedMarkMemo;
import org.anchoranalysis.anchor.mpp.proposer.PositionProposer;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.BoundingBox;

// Proposes a position from somewhere in a list of Memos, with some extra conditions
// NOT INTENDED TO BE IN A CONFIG FILE
@RequiredArgsConstructor
class PositionProposerMemoList implements PositionProposer {

    private static final int MAX_NUM_TRIES = 20;

    private final List<VoxelizedMarkMemo> listPxlMarkMemo;
    private final Mark markBlock;

    @Override
    public Optional<Point3d> propose(ProposerContext context) {

        RegionMembership rm =
                context.getRegionMap().membershipForIndex(GlobalRegionIdentifiers.SUBMARK_INSIDE);

        if (listPxlMarkMemo.isEmpty()) {
            return Optional.empty();
        }

        // ASSUMES a single channel
        for (int i = 0; i < MAX_NUM_TRIES; i++) {

            // We keep randomly picking a memo from the list
            // And randomly taking positions until we find a position that matches
            VoxelizedMark pm = randomMemo(context).voxelized();

            BoundingBox bbox = pm.getBoundingBox();

            Point3d point = randomPosition(bbox, context.getRandomNumberGenerator());

            if (insideRelevantRegion(pm, rm, point, bbox)) {
                return Optional.of(point);
            }
        }

        return Optional.empty();
    }

    private boolean insideRelevantRegion(
            VoxelizedMark pm, RegionMembership rm, Point3d point, BoundingBox bbox) {

        byte flags = rm.flags();

        Point3i rel =
                Point3i.immutableSubtract(PointConverter.intFromDouble(point), bbox.cornerMin());

        byte membershipExst =
                pm.getVoxels().getPixelsForPlane(rel.getZ()).get(bbox.extent().offsetSlice(rel));

        // If it's not inside our mark, then we don't consider it
        if (!rm.isMemberFlag(membershipExst, flags)) {
            return false;
        }

        byte membership = markBlock.evalPointInside(point);

        // If it's inside our block mark, then we don't consider it
        return !rm.isMemberFlag(membership, flags);
    }

    private VoxelizedMarkMemo randomMemo(ProposerContext context) {
        return context.getRandomNumberGenerator().sampleFromList(listPxlMarkMemo);
    }

    private static Point3d randomPosition(
            BoundingBox bbox, RandomNumberGenerator randomNumberGenerator) {
        return new Point3d(
                randomFromExtent(bbox, randomNumberGenerator, ReadableTuple3i::getX),
                randomFromExtent(bbox, randomNumberGenerator, ReadableTuple3i::getY),
                randomFromExtent(bbox, randomNumberGenerator, ReadableTuple3i::getZ));
    }

    private static int randomFromExtent(
            BoundingBox bbox,
            RandomNumberGenerator randomNumberGenerator,
            ToIntFunction<ReadableTuple3i> extract) {
        int corner = extract.applyAsInt(bbox.cornerMin());
        return corner
                + randomNumberGenerator.sampleIntFromRange(
                        extract.applyAsInt(bbox.extent().asTuple()));
    }
}
