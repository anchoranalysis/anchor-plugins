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

import java.awt.Color;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.color.ColorList;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.name.value.SimpleNameValue;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.io.generator.raster.bbox.ExtractedBoundingBoxGenerator;
import org.anchoranalysis.image.io.generator.raster.bbox.ExtractedObjectGenerator;
import org.anchoranalysis.image.io.generator.raster.obj.ObjectWithBoundingBoxGenerator;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.stack.NamedStackCollection;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.io.generator.IterableGenerator;
import org.anchoranalysis.io.generator.IterableObjectGenerator;
import org.anchoranalysis.io.generator.combined.IterableCombinedListGenerator;

/**
 * Builds a generator for all relevant stacks, outputting only if a predicate succeeds
 * 
 * @author Owen Feehan
 *
 */
@AllArgsConstructor
class GeneratorHelper {

    private static final String MANIFEST_FUNCTION = "extractedObjectOutline";
    
    private static final String MANIFEST_FUNCTION_EXTRACTED_BOUNDING_BOX = "boundingBoxExtract";

    private static final ColorList COLORS = new ColorList(Color.GREEN);
    
    /** The width of the outline of the object (e.g. 1 pixel) */
    private final int outlineWidth;

    public IterableCombinedListGenerator<ObjectMask> buildGeneratorsForStacks(
            ImageDimensions dimensions,
            NamedStackCollection stacks,
            NamedStackCollection stacksFlattened
            )
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
            NamedStackCollection stacks,
            IterableCombinedListGenerator<ObjectMask> out,
            boolean mip)
            throws NamedProviderGetException {

        for (String key : stacks.keys()) {

            // Bounding box-generator
            ExtractedBoundingBoxGenerator generator = new ExtractedBoundingBoxGenerator(stacks.getException(key), MANIFEST_FUNCTION_EXTRACTED_BOUNDING_BOX);
            out.add(key, WrapGeneratorHelper.boundingBoxAsObject(generator, mip));
            
            // Outline on raster generator
            String suffix = mip ? "_RGBOutlineMIP" : "_RGBOutline";
            out.add(key + suffix, createExtractedObjectGenerator(generator) );
        }
    }
    
    private IterableGenerator<ObjectMask> createExtractedObjectGenerator(
          IterableObjectGenerator<BoundingBox, Stack> backgroundGenerator
    ) {
        return new ExtractedObjectGenerator(backgroundGenerator, outlineWidth, COLORS, true, MANIFEST_FUNCTION);
    }
}
