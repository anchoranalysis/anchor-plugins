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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.geometry.ReadableTuple3i;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.convert.UnsignedByteBuffer;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.bean.score.VoxelScore;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.VoxelsWrapperList;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;

@AllArgsConstructor
class VoxelsFromScoreCreator {

    private VoxelsWrapperList listVoxels;
    private Optional<KeyValueParams> keyValueParams;
    private List<Histogram> listAdditionalHistograms;

    public Voxels<UnsignedByteBuffer> createVoxelsFromPixelScore(
            VoxelScore pixelScore, Optional<ObjectMask> object) throws CreateException {

        // Sets up the Feature
        try {
            init(pixelScore, object);

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

        } catch (InitException | FeatureCalculationException e) {
            throw new CreateException(e);
        }
    }

    /** Initializes the pixel-score */
    private void init(VoxelScore pixelScore, Optional<ObjectMask> object) throws InitException {
        pixelScore.init(createHistograms(object), keyValueParams);
    }

    private List<Histogram> createHistograms(Optional<ObjectMask> object) {
        List<Histogram> out = new ArrayList<>();

        for (VoxelsWrapper voxels : listVoxels) {
            out.add(HistogramFactory.create(voxels, object));
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

            List<VoxelBuffer<?>> bufferList = listVoxels.bufferListForSlice(z);

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

            List<VoxelBuffer<?>> bufferList = listVoxels.bufferListForSlice(z);

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
