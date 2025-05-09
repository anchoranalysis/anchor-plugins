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

package org.anchoranalysis.plugin.image.bean.channel.provider.arithmetic;

import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.Voxels;
import org.anchoranalysis.image.voxel.iterator.IterateVoxelsAll;
import org.anchoranalysis.plugin.image.bean.channel.provider.UnaryWithValueBase;

/**
 * Base class for arithmetic operations that conditionally overwrite voxel values with a constant.
 *
 * <p>This class extends {@link UnaryWithValueBase} to provide a framework for operations that
 * selectively replace voxel values with a constant based on a condition.
 */
public abstract class ArithmeticWithConstantConditionallyWriteBase extends UnaryWithValueBase {

    @Override
    public Channel createFromChannelWithConstant(Channel channel, double value)
            throws ProvisionFailedException {
        processVoxels(channel.voxels().any(), value);
        return channel;
    }

    /**
     * Determines whether to overwrite the current voxel value with the constant.
     *
     * @param voxel the current voxel value
     * @param constant the constant value to potentially overwrite with
     * @return true if the voxel should be overwritten, false otherwise
     */
    protected abstract boolean shouldOverwriteVoxelWithConstant(int voxel, int constant);

    /**
     * Processes all voxels in the given {@link Voxels} object, potentially overwriting values.
     *
     * @param voxels the {@link Voxels} object to process
     * @param constantToAssign the constant value to potentially assign to voxels
     */
    private void processVoxels(Voxels<?> voxels, double constantToAssign) {
        int constantAsInt = (int) Math.floor(constantToAssign);

        IterateVoxelsAll.assignEachMatchingPoint(
                voxels,
                value -> shouldOverwriteVoxelWithConstant(value, constantAsInt),
                constantAsInt);
    }
}
