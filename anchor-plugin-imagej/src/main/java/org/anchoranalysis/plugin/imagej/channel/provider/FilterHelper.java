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

package org.anchoranalysis.plugin.imagej.channel.provider;

import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.image.core.channel.Channel;
import org.anchoranalysis.image.voxel.VoxelsUntyped;
import org.anchoranalysis.image.voxel.binary.BinaryVoxels;
import org.anchoranalysis.image.voxel.buffer.primitive.UnsignedByteBuffer;
import org.anchoranalysis.image.voxel.buffer.slice.SliceBufferIndex;
import org.anchoranalysis.io.imagej.convert.ConvertToImageProcessor;
import org.anchoranalysis.io.imagej.convert.ImageJConversionException;

/**
 * Helper class for applying filters to {@link Channel}s and {@link BinaryVoxels} using ImageJ
 * operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilterHelper {

    /**
     * Applies a 2D rank-filter to each slice independently of a {@link Channel}.
     *
     * @param channel the channel to apply the filter to
     * @param radius the radius of the filter
     * @param filterType the type of rank filter to apply
     * @return the filtered channel
     * @throws OperationFailedException if {@code channel} contains an unsupported data-type
     */
    public static Channel applyRankFilter(Channel channel, int radius, int filterType)
            throws OperationFailedException {
        try {
            RankFilters rankFilters = new RankFilters();
            processEachSlice(channel, processor -> rankFilters.rank(processor, radius, filterType));
            return channel;
        } catch (ImageJConversionException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * Applies a {@link Consumer} to each slice independently of a {@link Channel}.
     *
     * <p>The slice is exposed as a {@link ImageProcessor}.
     *
     * @param channel the channel whose slices will be processed
     * @param consumer successively applied to the {@link ImageProcessor} derived from each slice
     * @throws ImageJConversionException if the voxels are neither unsigned byte nor unsigned short
     *     (the only two supported types)
     */
    public static void processEachSlice(Channel channel, Consumer<ImageProcessor> consumer)
            throws ImageJConversionException {
        VoxelsUntyped voxels = channel.voxels();
        channel.extent()
                .iterateOverZ(
                        z -> {
                            ImageProcessor processor = ConvertToImageProcessor.from(voxels, z);
                            consumer.accept(processor);
                        });
    }

    /**
     * Applies a {@link Consumer} to each slice independently of a {@link BinaryVoxels} with {@link
     * UnsignedByteBuffer}.
     *
     * <p>The slice is exposed as a {@link ImageProcessor}.
     *
     * @param voxels the voxels whose slices will be processed
     * @param consumer successively applied to the {@link ImageProcessor} derived from each slice
     */
    public static void processEachSlice(
            BinaryVoxels<UnsignedByteBuffer> voxels, Consumer<ImageProcessor> consumer) {
        SliceBufferIndex<UnsignedByteBuffer> slices = voxels.slices();
        voxels.extent()
                .iterateOverZ(
                        z -> {
                            ImageProcessor processor = ConvertToImageProcessor.fromByte(slices, z);
                            consumer.accept(processor);
                        });
    }
}
