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

package org.anchoranalysis.plugin.image.bean.object.provider.connected;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.provider.Provider;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.bean.nonbean.error.UnitValueException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.bean.unitvalue.extent.UnitValueAreaOrVolume;
import org.anchoranalysis.image.bean.unitvalue.extent.volume.VolumeVoxels;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.BinaryVoxelsFactory;
import org.anchoranalysis.image.voxel.binary.connected.ObjectsFromConnectedComponentsFactory;
import org.anchoranalysis.image.voxel.binary.values.BinaryValues;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.extracter.VoxelsExtracter;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.apache.commons.lang.time.StopWatch;

/**
 * Converts a binary-mask into its connected components
 *
 * @author feehano
 */
public class ConnectedComponentsFromMask extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Provider<Mask> mask;

    @BeanField @Getter @Setter private UnitValueAreaOrVolume minVolume = new VolumeVoxels(1);

    @BeanField @Getter @Setter private boolean bySlices = false;

    /** If true uses 8 neighborhood rather than 4 neighborhood etc. in 2D, and similar in 3D */
    @BeanField @Getter @Setter private boolean bigNeighborhood = false;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection create() throws CreateException {

        Mask maskCreated = mask.create();

        StopWatch sw = new StopWatch();
        sw.start();

        try {
            int minNumberVoxels =
                    (int)
                            Math.round(
                                    minVolume.resolveToVoxels(
                                            maskCreated.dimensions().unitConvert()));

            if (bySlices) {
                return createObjectsBySlice(maskCreated, minNumberVoxels);
            } else {
                return createObjects3D(maskCreated, minNumberVoxels);
            }

        } catch (UnitValueException e) {
            throw new CreateException(e);
        }
    }

    private ObjectsFromConnectedComponentsFactory createFactory(int minNumberVoxels) {
        return new ObjectsFromConnectedComponentsFactory(bigNeighborhood, minNumberVoxels);
    }

    private ObjectCollection createObjects3D(Mask mask, int minNumberVoxels) {
        return createFactory(minNumberVoxels).createUnsignedByte(mask.binaryVoxels());
    }

    private ObjectCollection createObjectsBySlice(Mask mask, int minNumberVoxels)
            throws CreateException {

        ObjectsFromConnectedComponentsFactory creator = createFactory(minNumberVoxels);

        VoxelsExtracter<UnsignedByteBuffer> extracter = mask.voxels().extract();

        return ObjectCollectionFactory.flatMapFromRange(
                0,
                mask.dimensions().z(),
                CreateException.class,
                z -> createForSlice(creator, extractSlice(extracter, z, mask.binaryValues()), z));
    }

    private static BinaryVoxels<UnsignedByteBuffer> extractSlice(
            VoxelsExtracter<UnsignedByteBuffer> extracter, int z, BinaryValues binaryValues) {
        return BinaryVoxelsFactory.reuseByte(extracter.slice(z), binaryValues);
    }

    private ObjectCollection createForSlice(
            ObjectsFromConnectedComponentsFactory objectCreator,
            BinaryVoxels<UnsignedByteBuffer> binaryValues,
            int z) {
        // respecify the z
        return objectCreator.createUnsignedByte(binaryValues).stream()
                .mapBoundingBoxPreserveExtent(box -> box.shiftToZ(z));
    }
}
