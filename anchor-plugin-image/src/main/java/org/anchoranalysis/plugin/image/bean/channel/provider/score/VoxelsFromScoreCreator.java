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

package org.anchoranalysis.plugin.image.bean.channel.provider.score;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitializeException;
import org.anchoranalysis.core.value.Dictionary;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.core.object.HistogramFromObjectsFactory;
import org.anchoranalysis.image.feature.bean.VoxelScore;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.VoxelsWrapperList;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.math.histogram.Histogram;
import org.anchoranalysis.spatial.Extent;
import org.anchoranalysis.spatial.point.ReadableTuple3i;

@AllArgsConstructor
class VoxelsFromScoreCreator {

    private VoxelsWrapperList listVoxels;
    private Optional<Dictionary> dictionary;
    private List<Histogram> listAdditionalHistograms;

    public Voxels<UnsignedByteBuffer> createVoxelsFromPixelScore(
            VoxelScore pixelScore, Optional<ObjectMask> object) throws CreateException {

        // Sets up the Feature
        try {
            initialize(pixelScore, object);

            Extent extent = listVoxels.getFirstExtent();

            // We make our index buffer
            Voxels<UnsignedByteBuffer> voxelsOut =
                    VoxelsFactory.getUnsignedByte().createInitialized(extent);

            if (object.isPresent()) {
                setVoxelsWithMask(voxelsOut, object.get(), pixelScore);
            } else {
                setVoxelsWithoutMask(voxelsOut, pixelScore);
            }
            return voxelsOut;

        } catch (InitializeException | FeatureCalculationException e) {
            throw new CreateException(e);
        }
    }

    /** Initializes the pixel-score */
    private void initialize(VoxelScore pixelScore, Optional<ObjectMask> object)
            throws InitializeException {
        pixelScore.initialize(createHistograms(object), dictionary);
    }

    private List<Histogram> createHistograms(Optional<ObjectMask> object) {
        List<Histogram> out = new ArrayList<>();

        for (VoxelsWrapper voxels : listVoxels) {
            out.add(HistogramFromObjectsFactory.create(voxels, object));
        }

        for (Histogram histogram : listAdditionalHistograms) {
            out.add(histogram);
        }

        return out;
    }

    private void setVoxelsWithoutMask(Voxels<UnsignedByteBuffer> voxelsOut, VoxelScore pixelScore)
            throws FeatureCalculationException {

        Extent extent = voxelsOut.extent();

        for (int z = 0; z < extent.z(); z++) {

            List<VoxelBuffer<Object>> bufferList = listVoxels.bufferListForSlice(z);

            UnsignedByteBuffer bufferOut = voxelsOut.sliceBuffer(z);

            for (int y = 0; y < extent.y(); y++) {
                for (int x = 0; x < extent.x(); x++) {

                    int offset = extent.offset(x, y);
                    BufferUtilities.putScoreForOffset(pixelScore, bufferList, bufferOut, offset);
                }
            }
        }
    }

    private void setVoxelsWithMask(
            Voxels<UnsignedByteBuffer> voxelsOut, ObjectMask object, VoxelScore pixelScore)
            throws FeatureCalculationException {

        byte maskOn = object.binaryValuesByte().getOnByte();
        Extent e = voxelsOut.extent();
        Extent eMask = object.binaryVoxels().extent();

        ReadableTuple3i cornerMin = object.boundingBox().cornerMin();
        ReadableTuple3i cornerMax = object.boundingBox().calculateCornerMax();

        for (int z = cornerMin.z(); z <= cornerMax.z(); z++) {

            List<VoxelBuffer<Object>> bufferList = listVoxels.bufferListForSlice(z);

            int zRel = z - cornerMin.z();

            UnsignedByteBuffer bufferMask = object.sliceBufferLocal(zRel);
            UnsignedByteBuffer bufferOut = voxelsOut.sliceBuffer(z);

            for (int y = cornerMin.y(); y <= cornerMax.y(); y++) {
                for (int x = cornerMin.x(); x <= cornerMax.x(); x++) {

                    int offset = e.offset(x, y);

                    int offsetMask = eMask.offset(x - cornerMin.x(), y - cornerMin.y());

                    if (bufferMask.getRaw(offsetMask) == maskOn) {
                        BufferUtilities.putScoreForOffset(
                                pixelScore, bufferList, bufferOut, offset);
                    }
                }
            }
        }
    }
}
