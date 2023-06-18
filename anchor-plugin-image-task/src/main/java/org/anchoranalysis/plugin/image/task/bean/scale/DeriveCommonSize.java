/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2023 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.bean.scale;

import java.util.List;
import java.util.function.Function;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.functional.checked.CheckedFunction;
import org.anchoranalysis.plugin.image.task.size.SizeMapping;
import org.anchoranalysis.spatial.box.Extent;

/**
 * Derives a common size for the images.
 *
 * <p>This is formed from the maximum of each dimension (independently) after scaling each image.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
class DeriveCommonSize {

    /**
     * Scales the size present in each {@link SizeMapping} in a list, and takes the maximum value of
     * all sizes in each dimension.
     *
     * @param mappings the mappings who are scaled, and from which max sizes are derived.
     * @param scaler how to scale a mapping.
     * @return the maximum size in each dimension (separately) after scaling.
     * @throws OperationFailedException if scaling cannot be applied to any mapping.
     */
    public static Extent scaleAndFindMax(
            List<SizeMapping> mappings,
            CheckedFunction<SizeMapping, SizeMapping, OperationFailedException> scaler)
            throws OperationFailedException {
        List<SizeMapping> scaled = scaleSizes(mappings, scaler);
        return deriveMaxSize(scaled);
    }

    /** Scale all the extents in {@code sizes} to produce a new mappings. */
    private static List<SizeMapping> scaleSizes(
            List<SizeMapping> sizes,
            CheckedFunction<SizeMapping, SizeMapping, OperationFailedException> scaler)
            throws OperationFailedException {
        return FunctionalList.mapToList(
                sizes, OperationFailedException.class, mapping -> scaler.apply(mapping));
    }

    /** Derives the maximum of all sizes in {@code mappings}. */
    private static Extent deriveMaxSize(List<SizeMapping> mappings)
            throws OperationFailedException {
        return new Extent(
                maxSizeSingleDimension(mappings, Extent::x),
                maxSizeSingleDimension(mappings, Extent::y),
                maxSizeSingleDimension(mappings, Extent::z));
    }

    /** Derives the maximum of all sizes in {@code mappings} - along a single dimension. */
    private static int maxSizeSingleDimension(
            List<SizeMapping> mappings, Function<Extent, Integer> extractDimension) {
        return mappings.stream()
                .mapToInt(mapping -> extractDimension.apply(mapping.getExtent()))
                .max()
                .orElse(0);
    }
}
