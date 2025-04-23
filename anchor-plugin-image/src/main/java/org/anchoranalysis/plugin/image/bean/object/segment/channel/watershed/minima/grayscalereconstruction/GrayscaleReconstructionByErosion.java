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

package org.anchoranalysis.plugin.image.bean.object.segment.channel.watershed.minima.grayscalereconstruction;

import java.util.Optional;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.voxel.VoxelsUntyped;
import org.anchoranalysis.image.voxel.object.ObjectMask;

/** Abstract base class for performing grayscale reconstruction by erosion. */
public abstract class GrayscaleReconstructionByErosion
        extends AnchorBean<GrayscaleReconstructionByErosion> {

    /**
     * Performs grayscale reconstruction of a mask image from a marker image.
     *
     * <p>The reconstruction is performed such that {@code mask <= markerImg}, but only inside the
     * containingMask (if provided).
     *
     * @param mask the {@link VoxelsUntyped} representing the mask image
     * @param marker the {@link VoxelsUntyped} representing the marker image
     * @param containingMask an optional {@link ObjectMask} that limits the reconstruction to a
     *     specific region
     * @return the reconstructed {@link VoxelsUntyped}
     * @throws OperationFailedException if the reconstruction operation fails
     */
    public abstract VoxelsUntyped reconstruction(
            VoxelsUntyped mask, VoxelsUntyped marker, Optional<ObjectMask> containingMask)
            throws OperationFailedException;
}
