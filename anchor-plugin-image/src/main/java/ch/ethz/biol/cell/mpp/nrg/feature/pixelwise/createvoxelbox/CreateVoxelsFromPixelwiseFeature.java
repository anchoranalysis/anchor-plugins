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

package ch.ethz.biol.cell.mpp.nrg.feature.pixelwise.createvoxelbox;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.bean.pixelwise.PixelScore;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactory;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.VoxelsWrapperList;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import lombok.AllArgsConstructor;

/**
 * TODO integrate into a factory or common class with {@link CreateVoxelsFromPixelwiseFeatureWithMask}
 * 
 * @author Owen Feehan
 *
 */
@AllArgsConstructor
public class CreateVoxelsFromPixelwiseFeature {

    private VoxelsWrapperList listVoxels;
    private Optional<KeyValueParams> keyValueParams;
    private List<Histogram> listAdditionalHistograms;

    public Voxels<ByteBuffer> createVoxelsFromPixelScore(PixelScore pixelScore)
            throws CreateException {

        // Sets up the Feature
        try {
            Extent e = listVoxels.getFirstExtent();

            // We make our index buffer
            Voxels<ByteBuffer> voxelsOut = VoxelsFactory.getByte().createInitialized(e);
            setPixels(voxelsOut, pixelScore);
            return voxelsOut;

        } catch (InitException | FeatureCalculationException e) {
            throw new CreateException(e);
        }
    }

    private void setPixels(Voxels<ByteBuffer> voxelsOut, PixelScore pixelScore)
            throws FeatureCalculationException, InitException {

        pixelScore.init(createHistograms(), keyValueParams);

        Extent extent = voxelsOut.extent();

        for (int z = 0; z < extent.z(); z++) {

            List<VoxelBuffer<?>> bbList = listVoxels.bufferListForSlice(z);

            ByteBuffer bbOut = voxelsOut.sliceBuffer(z);

            for (int y = 0; y < extent.y(); y++) {
                for (int x = 0; x < extent.x(); x++) {

                    int offset = extent.offset(x, y);

                    BufferUtilities.putScoreForOffset(pixelScore, bbList, bbOut, offset);
                }
            }
        }
    }

    private List<Histogram> createHistograms() {
        List<Histogram> out = new ArrayList<>();

        for (VoxelsWrapper voxels : listVoxels) {
            out.add(HistogramFactory.create(voxels, Optional.empty()));
        }

        for (Histogram hist : listAdditionalHistograms) {
            out.add(hist);
        }

        return out;
    }
}
