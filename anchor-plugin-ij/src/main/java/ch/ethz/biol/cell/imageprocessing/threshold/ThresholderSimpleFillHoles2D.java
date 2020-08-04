/*-
 * #%L
 * anchor-plugin-ij
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

package ch.ethz.biol.cell.imageprocessing.threshold;

import ij.Prefs;
import ij.plugin.filter.Binary;
import ij.process.ImageProcessor;
import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.threshold.Thresholder;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.thresholder.VoxelsThresholder;

@NoArgsConstructor
@AllArgsConstructor
public class ThresholderSimpleFillHoles2D extends Thresholder {

    static {
        Prefs.blackBackground = true;
    }

    // START BEAN PROPERTIES
    /** Intensity for thresholding */
    @BeanField @Getter @Setter private int minIntensity = -1;
    // END BEAN PROPERTIES

    @Override
    public BinaryVoxels<ByteBuffer> threshold(
            VoxelsWrapper inputBuffer,
            BinaryValuesByte bvOut,
            Optional<Histogram> histogram,
            Optional<ObjectMask> objectMask)
            throws OperationFailedException {

        if (objectMask.isPresent()) {
            throw new OperationFailedException("A mask is not supported for this operation");
        }

        BinaryVoxels<ByteBuffer> thresholded =
                VoxelsThresholder.thresholdForLevel(
                        inputBuffer, minIntensity, bvOut, objectMask, false);

        Binary binaryPlugin = new Binary();
        binaryPlugin.setup("fill", null);
        binaryPlugin.setNPasses(1);

        for (int z = 0; z < thresholded.extent().getZ(); z++) {
            ImageProcessor ip =
                    IJWrap.imageProcessor(new VoxelsWrapper(thresholded.getVoxels()), z);
            binaryPlugin.run(ip);
        }

        return thresholded;
    }
}
