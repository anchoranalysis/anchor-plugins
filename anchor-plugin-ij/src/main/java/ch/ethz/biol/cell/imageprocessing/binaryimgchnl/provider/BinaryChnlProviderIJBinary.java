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

package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import ij.Prefs;
import ij.plugin.filter.Binary;
import ij.process.ImageProcessor;
import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.convert.IJWrap;

public class BinaryChnlProviderIJBinary extends BinaryChnlProviderOne {

    // START BEAN PROPERTIES
    /** One of: open, close, fill, erode, dilate, skel, outline */
    @BeanField @Getter @Setter private String command = "";

    /** iterations for erode, dilate, open, close */
    @BeanField @Positive @Getter @Setter private int iterations = 1;
    // END BEAN PROPERTIES

    public static void fill(BinaryVoxels<ByteBuffer> bvb) throws OperationFailedException {
        doCommand(bvb, "fill", 1);
    }

    private static BinaryVoxels<ByteBuffer> doCommand(
            BinaryVoxels<ByteBuffer> bvb, String command, int iterations)
            throws OperationFailedException {

        if (bvb.binaryValues().getOnInt() != 255 || bvb.binaryValues().getOffInt() != 0) {
            throw new OperationFailedException("On byte must be 255, and off byte must be 0");
        }

        Prefs.blackBackground = true;

        // Fills Holes
        Binary binaryPlugin = new Binary();
        binaryPlugin.setup(command, null);
        binaryPlugin.setNPasses(bvb.extent().z());

        for (int i = 0; i < iterations; i++) {

            // Are we missing a Z slice?
            for (int z = 0; z < bvb.extent().z(); z++) {

                ImageProcessor ip =
                        IJWrap.imageProcessorByte(bvb.voxels().slices(), z);
                binaryPlugin.run(ip);
            }
        }

        return bvb;
    }

    @Override
    public Mask createFromMask(Mask mask) throws CreateException {

        BinaryVoxels<ByteBuffer> bvb = mask.binaryVoxels();

        try {
            BinaryVoxels<ByteBuffer> bvoxelsOut = doCommand(bvb, command, iterations);
            return new Mask(
                    bvoxelsOut,
                    mask.dimensions().resolution()
                    );
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
