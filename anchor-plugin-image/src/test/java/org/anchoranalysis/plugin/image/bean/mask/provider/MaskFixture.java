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

package org.anchoranalysis.plugin.image.bean.mask.provider;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelsFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.test.image.ChannelFixture;

/**
 * Creates {@link Mask} instantiations for tests.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MaskFixture {

    public static final int WIDTH = 40;
    public static final int HEIGHT = 7;
    public static final int DEPTH = 3;

    public static Mask createWithRectangle(Point3i crnr, boolean do3D) throws CreateException {

        Mask mask = new Mask(BinaryVoxelsFactory.createEmptyOff(extent(do3D)));
        mask.assignOn().toObject(createRectange(crnr, do3D));
        return mask;
    }

    public static Extent extent(boolean do3D) {
        return do3D ? ChannelFixture.MEDIUM_3D : ChannelFixture.MEDIUM_2D;
    }

    public static int depth(boolean do3D) {
        return do3D ? DEPTH : 1;
    }

    /** Creates a rectangle (2d) or cuboid (3d) */
    private static ObjectMask createRectange(Point3i crnr, boolean do3D) {
        ObjectMask object =
                new ObjectMask(new BoundingBox(crnr, new Extent(WIDTH, HEIGHT, depth(do3D))));
        object.assignOn().toAll();
        return object;
    }
}