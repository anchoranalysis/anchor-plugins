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

package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.BinaryChnlProviderOne;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxels;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.points.PointRange;

/**
 * Takes an existing binaryChnl and fits a box around the *On* pixels.
 *
 * <p>The tightest box that fits is always used
 *
 * @author Owen Feehan
 */
public class BinaryChnlProviderBox extends BinaryChnlProviderOne {

    // START BEAN PROPERTIES
    // If true, then each z slice is treated seperately
    @BeanField @Getter @Setter private boolean slicesSeperately = false;
    // END BEAN PROPERTIES

    @Override
    public Mask createFromMask(Mask bic) throws CreateException {

        if (slicesSeperately) {
            Extent e = bic.getDimensions().getExtent();
            for (int z = 0; z < e.getZ(); z++) {

                BoundingBox bbox = calcNarrowestBoxAroundMask(bic.extractSlice(z).binaryVoxels());
                bic.binaryVoxels().setPixelsToOn(bbox.shiftToZ(z));
            }
        } else {
            BoundingBox bbox = calcNarrowestBoxAroundMask(bic.binaryVoxels());
            bic.binaryVoxels().setPixelsToOn(bbox);
        }

        return bic;
    }

    private BoundingBox calcNarrowestBoxAroundMask(BinaryVoxels<ByteBuffer> vb)
            throws CreateException {

        PointRange pointRange = new PointRange();

        Extent extent = vb.extent();

        BinaryValuesByte bvb = vb.getBinaryValues().createByte();

        Point3i point = new Point3i(0, 0, 0);
        for (point.setZ(0); point.getZ() < extent.getZ(); point.incrementZ()) {

            ByteBuffer buf = vb.getPixelsForPlane(point.getZ()).buffer();

            for (point.setY(0); point.getY() < extent.getY(); point.incrementY()) {
                for (point.setX(0); point.getX() < extent.getX(); point.incrementX()) {

                    int offset = extent.offset(point.getX(), point.getY());
                    if (buf.get(offset) == bvb.getOnByte()) {
                        pointRange.add(point);
                    }
                }
            }
        }

        try {
            return pointRange.deriveBoundingBox();
        } catch (OperationFailedException e) {
            throw new CreateException(e);
        }
    }
}
