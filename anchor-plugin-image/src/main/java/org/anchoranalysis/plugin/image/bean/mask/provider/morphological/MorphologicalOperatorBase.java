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

package org.anchoranalysis.plugin.image.bean.mask.provider.morphological;

import java.util.Optional;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.bean.provider.ChannelProvider;
import org.anchoranalysis.image.bean.provider.MaskProviderUnary;
import org.anchoranalysis.image.core.mask.Mask;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.spatial.point.Point3i;

/** Base class for performing morphological operations on {@link BinaryVoxels}. */
public abstract class MorphologicalOperatorBase extends MaskProviderUnary {

    // START PROPERTIES
    /** Optional provider for a background channel to use in intensity-based conditions. */
    @BeanField @OptionalBean @Getter @Setter private ChannelProvider backgroundChannelProvider;

    /** Number of iterations to apply the morphological operation. */
    @BeanField @Positive @Getter @Setter private int iterations = 1;

    /** Minimum intensity value for voxels to be considered in the operation. */
    @BeanField @Getter @Setter private int minIntensityValue = 0;

    /** Whether to suppress 3D operations and only perform 2D operations. */
    @BeanField @Getter @Setter private boolean suppress3D = false;
    // END PROPERTIES

    /**
     * Applies the morphological operation to the source mask.
     *
     * @param source the source mask to apply the operation to
     * @param do3D whether to perform the operation in 3D
     * @throws ProvisionFailedException if the operation fails
     */
    protected abstract void applyMorphologicalOperation(Mask source, boolean do3D)
            throws ProvisionFailedException;

    @Override
    public Mask createFromMask(Mask mask) throws ProvisionFailedException {

        // Gets outline
        applyMorphologicalOperation(mask, (mask.dimensions().z() > 1) && !suppress3D);

        return mask;
    }

    /**
     * Creates a precondition for the morphological operation based on intensity values.
     *
     * @return an optional predicate that checks if a voxel meets the intensity condition
     * @throws ProvisionFailedException if creating the precondition fails
     */
    protected Optional<Predicate<Point3i>> precondition() throws ProvisionFailedException {
        if (minIntensityValue > 0) {
            Voxels<UnsignedByteBuffer> background =
                    backgroundChannelProvider.get().voxels().asByte();
            return Optional.of(point -> intensityCondition(background, point, minIntensityValue));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Checks if a voxel meets the intensity condition.
     *
     * @param voxels the voxels to check
     * @param point the point to check
     * @param minIntensityValue the minimum intensity value
     * @return true if the voxel meets the intensity condition, false otherwise
     */
    private static boolean intensityCondition(
            Voxels<UnsignedByteBuffer> voxels, Point3i point, int minIntensityValue) {
        return minIntensityValue == 0 || voxels.extract().voxel(point) >= minIntensityValue;
    }
}
