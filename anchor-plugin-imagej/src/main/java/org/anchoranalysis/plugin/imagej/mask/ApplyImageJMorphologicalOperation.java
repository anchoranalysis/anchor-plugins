/*-
 * #%L
 * anchor-plugin-ij
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
package org.anchoranalysis.plugin.imagej.mask;

import ij.Prefs;
import ij.plugin.filter.Binary;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.binary.values.BinaryValuesInt;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.plugin.imagej.channel.provider.FilterHelper;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Applies an ImageJ (2D) morphological operation to voxels.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplyImageJMorphologicalOperation {

    /**
     * Applies the fill operation to the given binary voxels.
     *
     * @param voxels the {@link BinaryVoxels} to fill
     * @throws OperationFailedException if the operation fails
     */
    public static void fill(BinaryVoxels<UnsignedByteBuffer> voxels)
            throws OperationFailedException {
        applyOperation(voxels, "fill", 1);
    }

    /**
     * Applies a specified morphological operation to the given binary voxels.
     *
     * @param voxels the {@link BinaryVoxels} to apply the operation to
     * @param command the name of the morphological operation to apply
     * @param iterations the number of times to apply the operation
     * @return the modified {@link BinaryVoxels}
     * @throws OperationFailedException if the operation fails or if the binary values are not the
     *     default (255 for on, 0 for off)
     */
    public static BinaryVoxels<UnsignedByteBuffer> applyOperation(
            BinaryVoxels<UnsignedByteBuffer> voxels, String command, int iterations)
            throws OperationFailedException {

        if (!voxels.binaryValues().equals(BinaryValuesInt.getDefault())) {
            throw new OperationFailedException("On byte must be 255, and off byte must be 0");
        }

        Prefs.blackBackground = true;

        Binary plugin = createPlugin(command, voxels.extent());

        for (int i = 0; i < iterations; i++) {
            FilterHelper.processEachSlice(voxels, plugin::run);
        }

        return voxels;
    }

    /**
     * Creates an ImageJ Binary plugin with the specified command and extent.
     *
     * @param command the name of the morphological operation to apply
     * @param extent the {@link Extent} of the voxels
     * @return a configured {@link Binary} plugin
     */
    private static Binary createPlugin(String command, Extent extent) {
        Binary plugin = new Binary();
        plugin.setup(command, null);
        plugin.setNPasses(extent.z());
        return plugin;
    }
}
