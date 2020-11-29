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

package org.anchoranalysis.plugin.image.bean.object.segment.channel.watershed.minima;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.core.channel.factory.ChannelFactory;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.core.mask.MaskFromObjects;
import org.anchoranalysis.image.core.object.seed.SeedCollection;
import org.anchoranalysis.image.voxel.VoxelsWrapper;
import org.anchoranalysis.image.voxel.assigner.VoxelsAssigner;
import org.anchoranalysis.image.voxel.factory.VoxelsFactory;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.segment.channel.watershed.minima.grayscalereconstruction.GrayscaleReconstructionByErosion;

public class MinimaImpositionGrayscaleReconstruction extends MinimaImposition {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private GrayscaleReconstructionByErosion grayscaleReconstruction;
    // END BEAN PROPERTIES

    @Override
    public Channel imposeMinima(
            Channel channel, SeedCollection seeds, Optional<ObjectMask> containingMask)
            throws OperationFailedException {

        if (seeds.size() < 1) {
            throw new OperationFailedException("There must be at least one seed");
        }

        seeds.verifySeedsAreInside(channel.extent());

        ObjectCollection objects = seeds.deriveObjects();

        // We need 255 for the landini algorithms to work
        Mask markerMask =
                MaskFromObjects.createFromObjects(
                        objects, channel.dimensions(), objects.getFirstBinaryValues());

        // We duplicate the channel so we are not manipulating the original
        channel = channel.duplicate();

        VoxelsWrapper voxelsIntensity = channel.voxels();

        VoxelsAssigner zeroAssigner = voxelsIntensity.assignValue(0);

        // We set the EDM to 0 at the points of the minima
        objects.forEach(zeroAssigner::toObject);

        // We set the EDM to 255 outside the channel, otherwise the reconstruction will be messed up
        // Better alternative is to apply the reconstruction only on the mask
        if (containingMask.isPresent()) {
            voxelsIntensity
                    .any()
                    .assignValue((int) voxelsIntensity.getVoxelDataType().maxValue())
                    .toObject(containingMask.get());
        }

        VoxelsWrapper markerForReconstruction =
                createMarkerImageFromGradient(markerMask, voxelsIntensity);

        VoxelsWrapper reconBuffer =
                grayscaleReconstruction.reconstruction(
                        voxelsIntensity, markerForReconstruction, containingMask);

        return ChannelFactory.instance().create(reconBuffer.any(), channel.resolution());
    }

    private VoxelsWrapper createMarkerImageFromGradient(Mask marker, VoxelsWrapper gradientImage) {

        VoxelsWrapper out =
                VoxelsFactory.instance()
                        .create(gradientImage.any().extent(), gradientImage.getVoxelDataType());
        out.assignValue((int) gradientImage.getVoxelDataType().maxValue()).toAll();

        ObjectMask object = new ObjectMask(marker.binaryVoxels());
        gradientImage.copyVoxelsTo(object, out, object.boundingBox());
        return out;
    }
}
