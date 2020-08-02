/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

package org.anchoranalysis.plugin.mpp.experiment.bean.objects;

import lombok.AllArgsConstructor;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.name.value.SimpleNameValue;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.generator.raster.bbox.DrawObjectOnStackGenerator;
import org.anchoranalysis.image.io.generator.raster.bbox.ExtractBoundingBoxAreaFromStackGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.ObjectWithBoundingBoxGenerator;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.NamedStacks;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.combined.IterableCombinedListGenerator;

/**
 * Builds a generator for all relevant stacks that combines several generators
 *
 * <ul>
 *   <li>An object-mask and bounding box generator
 *   <li>A generator that extracts the bounding-box portion from a stack
 *   <li>A generator that draws an object on an extracted bounding-box portion from a stack
 * </ul>
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
class BuildGeneratorHelper {

    /**
     * Added to the name of the stack to give an outline in an extracted portion of the stack (not
     * flattened in z dimension)
     */
    private static final String OUTPUT_OUTLINE_SUFFIX_NOT_FLATTENED = "_outline";

    /**
     * Added to the name of the stack to give an outline in an extracted portion of the stack
     * (flattened in z dimension)
     */
    private static final String OUTPUT_OUTLINE_SUFFIX_FLATTENED = "_outline_flattened";

    /** The width of the outline of the object (e.g. 1 pixel) */
    private final int outlineWidth;

    public IterableGenerator<ObjectMask> forStacks(
            ImageDimensions dimensions, NamedStacks stacks, NamedStacks stacksFlattened)
            throws CreateException {
        IterableCombinedListGenerator<ObjectMask> out =
                new IterableCombinedListGenerator<>(
                        new SimpleNameValue<>(
                                "mask", new ObjectWithBoundingBoxGenerator(dimensions.getRes())));

        try {
            addGeneratorForEachStack(stacks, out, false);
            addGeneratorForEachStack(stacksFlattened, out, true);
        } catch (NamedProviderGetException e) {
            throw new CreateException(e);
        }
        return out;
    }

    private void addGeneratorForEachStack(
            NamedStacks stacks, IterableCombinedListGenerator<ObjectMask> out, boolean flatten)
            throws NamedProviderGetException {

        for (String key : stacks.keys()) {

            // TODO does the first generator get added twice for both flattened and non-flattened
            // stacks?

            // Bounding box-generator
            ExtractBoundingBoxAreaFromStackGenerator generator =
                    new ExtractBoundingBoxAreaFromStackGenerator(stacks.getException(key));
            out.add(key, WrapBoundingBoxGeneratorAsObject.wrap(generator, flatten));

            // Outline on raster generator, reusing the previous generator for the background
            out.add(
                    outlineOutputName(key, flatten),
                    DrawObjectOnStackGenerator.createFromGenerator(generator, outlineWidth));
        }
    }

    private static String outlineOutputName(String key, boolean flatten) {
        String suffix =
                flatten ? OUTPUT_OUTLINE_SUFFIX_FLATTENED : OUTPUT_OUTLINE_SUFFIX_NOT_FLATTENED;
        return key + suffix;
    }
}
