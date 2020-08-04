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

package org.anchoranalysis.plugin.image.bean.object.filter.independent;

import java.nio.ByteBuffer;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.axis.AxisType;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.plugin.image.bean.object.filter.ObjectFilterPredicate;

/**
 * Only keep objects where at least one voxel (on a particular channel) has intensity greater or
 * equal to a threshold.
 *
 * @author Owen Feehan
 */
public class IntensityGreaterEqualThan extends ObjectFilterPredicate {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChannelProvider chnl;

    // The threshold we use, the distance is always calculated in the direction of the XY plane.
    @BeanField @Getter @Setter private UnitValueDistance threshold;
    // END BEAN PROPERTIES

    private Voxels<?> voxels;

    @Override
    protected void start(Optional<ImageDimensions> dim, ObjectCollection objectsToFilter)
            throws OperationFailedException {

        Channel chnlSingleRegion;
        try {
            chnlSingleRegion = chnl.create();
        } catch (CreateException e) {
            throw new OperationFailedException(e);
        }
        assert (chnlSingleRegion != null);
        voxels = chnlSingleRegion.voxels().any();
    }

    @Override
    protected boolean match(ObjectMask object, Optional<ImageDimensions> dim)
            throws OperationFailedException {

        int thresholdResolved = threshold(dim);

        for (int z = 0; z < object.boundingBox().extent().z(); z++) {

            ByteBuffer bb = object.voxels().slice(z).buffer();

            int z1 = z + object.boundingBox().cornerMin().z();
            VoxelBuffer<?> bbChnl = voxels.slice(z1);

            for (int y = 0; y < object.boundingBox().extent().y(); y++) {
                for (int x = 0; x < object.boundingBox().extent().x(); x++) {

                    int offset = object.boundingBox().extent().offset(x, y);
                    if (bb.get(offset) == object.binaryValuesByte().getOnByte()) {

                        int y1 = y + object.boundingBox().cornerMin().y();
                        int x1 = x + object.boundingBox().cornerMin().x();

                        int offsetGlobal = voxels.extent().offset(x1, y1);

                        // Now we get a value from the voxels
                        int val = bbChnl.getInt(offsetGlobal);
                        if (val >= thresholdResolved) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private int threshold(Optional<ImageDimensions> dim) throws OperationFailedException {
        return (int)
                Math.ceil(threshold.resolveForAxis(dim.map(ImageDimensions::resolution), AxisType.X));
    }

    @Override
    protected void end() throws OperationFailedException {
        voxels = null;
    }
}
