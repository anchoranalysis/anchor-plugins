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

import static ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider.BinaryChnlFixture.*;
import static org.junit.Assert.*;

import java.util.Optional;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.plugin.image.test.ProviderFixture;
import org.junit.Test;

public class BinaryChnlProviderInvertTest {

    private static final Point3i CORNER_RECTANGLE = new Point3i(7, 14, 0);
    private static final Point3i CORNER_MASK = addHalfHeightInY(CORNER_RECTANGLE);

    @Test
    public void testWithoutMask2d() throws CreateException {
        testRectangle(false, false, expectedNumberVoxelsAfterWithoutRestriction(false));
    }

    @Test
    public void testWithoutMask3d() throws CreateException {
        testRectangle(true, false, expectedNumberVoxelsAfterWithoutRestriction(true));
    }

    @Test
    public void testWithMask2d() throws CreateException {
        testRectangle(false, true, 240);
    }

    @Test
    public void testWithMask3d() throws CreateException {
        testRectangle(true, true, 720);
    }

    private static int expectedNumberVoxelsBefore(boolean do3D) {
        return WIDTH * HEIGHT * depth(do3D);
    }

    private static long expectedNumberVoxelsAfterWithoutRestriction(boolean do3D) {
        return extent(do3D).calculateVolume() - expectedNumberVoxelsBefore(do3D);
    }

    private static void testRectangle(boolean do3D, boolean mask, long expectedNumberVoxelsAfter)
            throws CreateException {

        Mask maskBefore = createWithRectangle(CORNER_RECTANGLE, do3D);

        Optional<Mask> restrictTo = createRestrictTo(mask, do3D);

        assertVoxelsOn("before", expectedNumberVoxelsBefore(do3D), maskBefore);

        Mask maskAfter = createProviderInvert(maskBefore, restrictTo).create();

        assertVoxelsOn("after", expectedNumberVoxelsAfter, maskAfter);
    }

    private static Optional<Mask> createRestrictTo(boolean flag, boolean do3D) throws CreateException {
        return OptionalUtilities.createFromFlagChecked(flag, ()->createWithRectangle(CORNER_MASK, do3D) );
    }

    private static MaskProvider createProviderInvert(Mask mask, Optional<Mask> restrictTo) {
        BinaryChnlProviderInvert provider = new BinaryChnlProviderInvert();
        provider.setBinaryChnl(ProviderFixture.providerFor(mask));
        restrictTo.ifPresent(m -> provider.setRestrictTo(ProviderFixture.providerFor(m)));
        return provider;
    }

    private static void assertVoxelsOn(String messagePrefix, long expectedNumberVoxels, Mask mask) {
        assertEquals(
                messagePrefix + "VoxelsOn", expectedNumberVoxels, mask.voxelsOn().count());
    }

    private static Point3i addHalfHeightInY(Point3i in) {
        Point3i out = new Point3i(in);
        out.incrementY(HEIGHT / 2);
        return out;
    }
}
