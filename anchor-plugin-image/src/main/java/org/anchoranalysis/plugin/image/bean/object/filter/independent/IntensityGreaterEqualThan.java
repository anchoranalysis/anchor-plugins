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

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.filter.ObjectFilterPredicate;
import org.anchoranalysis.spatial.axis.Axis;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Only keep objects where at least one voxel (on a particular channel) has intensity greater or
 * equal to a threshold.
 *
 * @author Owen Feehan
 */
public class IntensityGreaterEqualThan extends ObjectFilterPredicate {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private ChannelProvider channel;

    // The threshold we use, the distance is always calculated in the direction of the XY plane.
    @BeanField @Getter @Setter private UnitValueDistance threshold;
    // END BEAN PROPERTIES

    private Voxels<?> voxels;

    @Override
    protected boolean precondition(ObjectCollection objectsToFilter) {
        return true;
    }

    @Override
    protected void start(Optional<Dimensions> dimensions, ObjectCollection objectsToFilter)
            throws OperationFailedException {

        Channel channelSingleRegion;
        try {
            channelSingleRegion = channel.get();
        } catch (ProvisionFailedException e) {
            throw new OperationFailedException(e);
        }
        assert (channelSingleRegion != null);
        voxels = channelSingleRegion.voxels().any();
    }

    @Override
    protected boolean match(ObjectMask object, Optional<Dimensions> dimensions)
            throws OperationFailedException {

        int thresholdResolved = threshold(dimensions);

        Extent extent = object.extent();

        for (int z = 0; z < extent.z(); z++) {

            UnsignedByteBuffer buffer = object.sliceBufferLocal(z);

            VoxelBuffer<?> bufferChannel = voxels.slice(z + object.boundingBox().cornerMin().z());

            for (int y = 0; y < extent.y(); y++) {
                for (int x = 0; x < extent.x(); x++) {

                    int offset = extent.offset(x, y);

                    if (buffer.getRaw(offset) == object.binaryValuesByte().getOn()) {

                        int y1 = y + object.boundingBox().cornerMin().y();
                        int x1 = x + object.boundingBox().cornerMin().x();

                        int offsetGlobal = voxels.extent().offset(x1, y1);

                        // Now we get a value from the voxels
                        int val = bufferChannel.getInt(offsetGlobal);
                        if (val >= thresholdResolved) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private int threshold(Optional<Dimensions> dim) throws OperationFailedException {
        return (int)
                Math.ceil(threshold.resolveForAxis(dim.flatMap(Dimensions::unitConvert), Axis.X));
    }

    @Override
    protected void end() throws OperationFailedException {
        voxels = null;
    }
}
