/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.bean.object.segment.channel.watershed.yeong;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.image.bean.nonbean.segment.SegmentationFailedException;
import org.anchoranalysis.image.voxel.iterator.IterateVoxelsObjectMask;
import org.anchoranalysis.image.voxel.iterator.process.ProcessPoint;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedVoxels;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MarkSeeds {

    public static void apply(
            ObjectCollection seeds,
            EncodedVoxels matS,
            Optional<MinimaStore> minimaStore,
            Optional<ObjectMask> containingMask)
            throws SegmentationFailedException {

        if (containingMask.isPresent()
                && !matS.extent().equals(containingMask.get().boundingBox().extent())) {
            throw new SegmentationFailedException("Extent of matS does not match containingMask");
        }

        for (ObjectMask object : seeds) {

            throwExceptionIfNotConnected(object);

            IterateVoxelsObjectMask.withPoint(
                    object, containingMask, createPointProcessor(matS, minimaStore));
        }
    }

    private static ProcessPoint createPointProcessor(
            EncodedVoxels matS, Optional<MinimaStore> minimaStore) {
        ConnectedComponentWriter ccWriter = new ConnectedComponentWriter(matS, minimaStore);
        return ccWriter::writePoint;
    }

    private static void throwExceptionIfNotConnected(ObjectMask obj)
            throws SegmentationFailedException {
        if (!obj.checkIfConnected()) {
            throw new SegmentationFailedException("Seed must be a single connected-component");
        }
    }
}
