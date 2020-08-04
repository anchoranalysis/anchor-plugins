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

package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

import java.nio.IntBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.nonbean.error.SegmentationFailedException;
import org.anchoranalysis.image.bean.segment.object.SegmentChannelIntoObjects;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.image.voxel.iterator.IterateVoxels;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedVoxels;

/**
 * A 'rainfall' watershed algorithm
 *
 * <p><div> See:
 *
 * <ul>
 *   <li>3D watershed based on rainfall-simulation for volume segmentation, Yeong et al. 2009
 *       International Conference on Intelligent Human-Machine Systems and Cybernetics
 *   <li>An improved watershed algorithm based on efficient computation of shortest paths, Osma-Ruiz
 *       et al., Pattern Reconigion(40), 2007
 * </ul>
 *
 * </div>
 *
 * <p><div> Note:
 *
 * <ul>
 *   <li>Does not record a watershed line
 * </ul>
 *
 * </div>
 *
 * @author Owen Feehan
 */
public class WatershedYeong extends SegmentChannelIntoObjects {

    // START PROPERTIES
    /** If true, exits early and just returns the minima, without any further segmentation */
    @BeanField @Getter @Setter private boolean exitWithMinima = false;
    // END PROPERTIES

    @Override
    public ObjectCollection segment(
            Channel channel, Optional<ObjectMask> objectMask, Optional<SeedCollection> seeds)
            throws SegmentationFailedException {

        EncodedVoxels matS = createS(channel.dimensions().extent());

        Optional<MinimaStore> minimaStore =
                OptionalFactory.create(exitWithMinima, MinimaStore::new);

        if (seeds.isPresent()) {
            MarkSeeds.apply(seeds.get(), matS, minimaStore, objectMask);
        }

        pointPixelsOrMarkAsMinima(channel.voxels().any(), matS, objectMask, minimaStore);

        // Special behavior where we just want to find the minima and nothing more
        if (minimaStore.isPresent()) {
            try {
                return minimaStore.get().createObjects();
            } catch (CreateException e) {
                throw new SegmentationFailedException(e);
            }
        }

        // TODO let's only work on the areas with regions
        convertAllToConnectedComponents(matS, objectMask);

        try {
            return createObjectsFromLabels(matS.voxels(), objectMask);
        } catch (CreateException e) {
            throw new SegmentationFailedException(e);
        }
    }

    /** Create 'S' matrix */
    private EncodedVoxels createS(Extent extent) {
        return new EncodedVoxels( VoxelsFactory.getInt().createInitialized(extent) );
    }

    private static void pointPixelsOrMarkAsMinima(
            Voxels<?> voxelsImg,
            EncodedVoxels matS,
            Optional<ObjectMask> objectMask,
            Optional<MinimaStore> minimaStore) {

        SlidingBufferPlus buffer = new SlidingBufferPlus(voxelsImg, matS, objectMask, minimaStore);
        IterateVoxels.callEachPoint(
                objectMask, buffer.getSlidingBuffer(), new PointPixelsOrMarkAsMinima(buffer));
    }

    private static void convertAllToConnectedComponents(
            EncodedVoxels matS, Optional<ObjectMask> objectMask) {
        IterateVoxels.callEachPoint(
                objectMask, matS.voxels(), new ConvertAllToConnectedComponents(matS));
    }

    private static ObjectCollection createObjectsFromLabels(
            Voxels<IntBuffer> matS, Optional<ObjectMask> objectMask) throws CreateException {

        final BoundingBoxMap bbm = new BoundingBoxMap();

        IterateVoxels.callEachPoint(
                objectMask,
                matS,
                (Point3i point, IntBuffer buffer, int offset) -> {
                    int crntVal = buffer.get(offset);
                    buffer.put(offset, bbm.addPointForValue(point, crntVal) + 1);
                });

        try {
            return bbm.deriveObjects(matS);
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
