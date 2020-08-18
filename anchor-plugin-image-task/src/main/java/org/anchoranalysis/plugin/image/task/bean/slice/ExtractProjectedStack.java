/*-
 * #%L
 * anchor-plugin-image-task
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

package org.anchoranalysis.plugin.image.task.bean.slice;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.error.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.UnsignedByte;

/**
 * Takes three RGB channels and projects them into a canvas of width/height in the form of a new RGB
 * stack
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class ExtractProjectedStack {

    private Optional<Extent> extent;

    public Stack extractAndProjectStack(Channel red, Channel green, Channel blue, int z)
            throws IncorrectImageSizeException {
        Stack stack = new Stack();
        extractAndProjectChnl(red, z, stack);
        extractAndProjectChnl(green, z, stack);
        extractAndProjectChnl(blue, z, stack);
        return stack;
    }

    private void extractAndProjectChnl(Channel chnl, int z, Stack stack)
            throws IncorrectImageSizeException {
        Channel chnlProjected = createProjectedChannel(chnl.extractSlice(z).duplicate());
        stack.addChannel(chnlProjected);
    }

    private Channel createProjectedChannel(Channel chnlIn) {

        // Then the mode is off
        if (!extent.isPresent() || chnlIn.extent().equals(extent.get())) {
            return chnlIn;
        } else {
            Point3i crnrPos = createTarget(chnlIn.dimensions(), extent.get());

            BoundingBox boxToProject =
                    boxToProject(crnrPos, chnlIn.extent(), extent.get());

            BoundingBox boxSrc = boxSrc(boxToProject, chnlIn.dimensions());

            return copyPixels(boxSrc, boxToProject, chnlIn, extent.get());
        }
    }

    private static Point3i createTarget(ImageDimensions dimensions, Extent extent) {
        Point3i crnrPos = new Point3i();
        crnrPos.setX((extent.x() - dimensions.x()) / 2);
        crnrPos.setY((extent.y() - dimensions.y()) / 2);
        crnrPos.setZ(0);
        return crnrPos;
    }

    private static BoundingBox boxToProject(
            Point3i crnrPos, Extent extentChannel, Extent extentTarget) {
        return new BoundingBox(crnrPos, extentChannel)
                .intersection()
                .with(new BoundingBox(extentTarget))
                .orElseThrow(AnchorImpossibleSituationException::new);
    }

    private static BoundingBox boxSrc(BoundingBox boxToProject, ImageDimensions dimensions) {
        Point3i srcCrnrPos = createSourceCorner(boxToProject, dimensions);
        return new BoundingBox(srcCrnrPos, boxToProject.extent());
    }

    private static Point3i createSourceCorner(
            BoundingBox boxToProject, ImageDimensions dimensions) {
        Point3i sourceCorner = new Point3i(0, 0, 0);

        if (boxToProject.extent().x() < dimensions.x()) {
            sourceCorner.setX((dimensions.x() - boxToProject.extent().x()) / 2);
        }

        if (boxToProject.extent().y() < dimensions.y()) {
            sourceCorner.setY((dimensions.y() - boxToProject.extent().y()) / 2);
        }
        return sourceCorner;
    }

    private Channel copyPixels(
            BoundingBox boxSource,
            BoundingBox boxToProject,
            Channel channelDestination,
            Extent extentOut) {

        Channel chnlOut =
                ChannelFactory.instance()
                        .create(
                                new ImageDimensions(
                                        extentOut, channelDestination.dimensions().resolution()),
                                UnsignedByte.INSTANCE);
        channelDestination
                .voxels()
                .asByte()
                .extracter()
                .boxCopyTo(boxSource, chnlOut.voxels().asByte(), boxToProject);
        return chnlOut;
    }
}
