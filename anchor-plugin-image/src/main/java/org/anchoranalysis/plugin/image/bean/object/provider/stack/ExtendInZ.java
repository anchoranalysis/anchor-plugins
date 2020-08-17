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

package org.anchoranalysis.plugin.image.bean.object.provider.stack;

import java.nio.ByteBuffer;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.plugin.image.bean.object.provider.ObjectCollectionProviderWithDimensions;

/**
 * Creates a 3D version of an object by replicating each input-object across the z-dimension to meet
 * (3D) dimensions.
 *
 * <p>An object-collection is produced with an identical number of objects, but with each expanded
 * in the z-dimension.
 *
 * <p>If the input-object is a 2D-slice it is replicated directly, if it is already a 3D-object its
 * flattened version (a maximum intensity projection) is used.
 *
 * @author Owen Feehan
 */
public class ExtendInZ extends ObjectCollectionProviderWithDimensions {

    @Override
    public ObjectCollection createFromObjects(ObjectCollection objects) throws CreateException {

        ImageDimensions dimensions = createDimensions();

        return objects.stream().map(objectMask -> expandZ(objectMask.flattenZ(), dimensions));
    }

    private static ObjectMask expandZ(ObjectMask object, ImageDimensions dim) {

        BoundingBox box = object.boundingBox().changeExtentZ(dim.z());

        Voxels<ByteBuffer> voxels =
                createVoxelsOfDuplicatedPlanes(
                        object.voxels().slice(0), box.extent());

        return new ObjectMask(box, voxels, object.binaryValues());
    }

    private static Voxels<ByteBuffer> createVoxelsOfDuplicatedPlanes(
            VoxelBuffer<ByteBuffer> planeIn, Extent extent) {
        Voxels<ByteBuffer> voxels = VoxelsFactory.getByte().createInitialized(extent);
        for (int z = 0; z < extent.z(); z++) {
            voxels.replaceSlice(z, planeIn);
        }
        return voxels;
    }
}
