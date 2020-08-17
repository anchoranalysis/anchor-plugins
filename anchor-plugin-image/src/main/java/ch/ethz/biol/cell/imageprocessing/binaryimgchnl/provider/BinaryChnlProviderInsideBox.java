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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.values.BinaryValues;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

/**
 * Creates a binary img-chnl where all pixels are 'on' within a certain x, y, z coordinate range
 *
 * <p>This effectively creates a mask that is a box
 */
public class BinaryChnlProviderInsideBox extends BinaryChnlProviderDimSource {

    // START BEAN PROPERTIES
    /** Minimum X coordinate inclusive */
    @BeanField @Getter @Setter private int minX = 0;

    /** Maximum X coordinate inclusive */
    @BeanField @Getter @Setter private int maxX = Integer.MAX_VALUE;

    /** Minimum Y coordinate inclusive */
    @BeanField @Getter @Setter private int minY = 0;

    /** Maximum Y coordinate inclusive */
    @BeanField @Getter @Setter private int maxY = Integer.MAX_VALUE;

    /** Minimum Z coordinate inclusive */
    @BeanField @Getter @Setter private int minZ = 0;

    /** Maximum Z coordinate inclusive */
    @BeanField @Getter @Setter private int maxZ = Integer.MAX_VALUE;
    // END BEAN PROPERTIES

    @Override
    protected Mask createFromDimensions(ImageDimensions dimensions) throws CreateException {
        Channel chnl =
                ChannelFactory.instance().create(dimensions, VoxelDataTypeUnsignedByte.INSTANCE);

        BoundingBox box = createBox(dimensions);

        return createBinaryChnl(box, chnl);
    }

    private static Mask createBinaryChnl(BoundingBox box, Channel channel) {
        BinaryValues bv = BinaryValues.getDefault();
        channel.assignValue(bv.getOnInt()).toBox(box);
        return new Mask(channel, bv);
    }

    private BoundingBox createBox(ImageDimensions dimensions) {
        BoundingBox box =
                new BoundingBox(new Point3d(minX, minY, minZ), new Point3d(maxX, maxY, maxZ));

        // Make sure box is inside channel
        return box.clipTo(dimensions.extent());
    }
}
