/*-
 * #%L
 * anchor-plugin-annotation
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

package org.anchoranalysis.plugin.annotation.bean.comparison;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.annotation.io.image.findable.Findable;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.experiment.JobExecutionException;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.io.stack.input.ProvidesStackInput;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationComparisonInput;
import org.anchoranalysis.plugin.annotation.comparison.ObjectsToCompare;
import org.anchoranalysis.plugin.annotation.counter.ImageCounter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ObjectsToCompareFactory {

    public static Optional<ObjectsToCompare> create(
            AnnotationComparisonInput<ProvidesStackInput> input,
            ImageCounter<?> addAnnotation,
            Dimensions dimensions,
            InputOutputContext context)
            throws JobExecutionException {

        // Both objects need to be found
        return OptionalUtilities.mapBoth(
                createObjects(true, "leftObj", addAnnotation, input, dimensions, context),
                createObjects(false, "rightObj", addAnnotation, input, dimensions, context),
                ObjectsToCompare::new);
    }

    private static Optional<ObjectCollection> createObjects(
            boolean left,
            String objName,
            ImageCounter<?> addAnnotation,
            AnnotationComparisonInput<ProvidesStackInput> input,
            Dimensions dimensions,
            InputOutputContext context)
            throws JobExecutionException {
        Findable<ObjectCollection> findable =
                createFindable(
                        left, input, dimensions, context.isDebugEnabled(), context.getLogger());
        return foundOrLogAddUnnannotated(findable, objName, addAnnotation, context.getLogger());
    }

    private static Optional<ObjectCollection> foundOrLogAddUnnannotated(
            Findable<ObjectCollection> objects,
            String objName,
            ImageCounter<?> addAnnotation,
            Logger logger) {
        Optional<ObjectCollection> found = objects.getOrLog(objName, logger);
        if (!found.isPresent()) {
            addAnnotation.addUnannotatedImage();
        }
        return found;
    }

    private static Findable<ObjectCollection> createFindable(
            boolean left,
            AnnotationComparisonInput<ProvidesStackInput> input,
            Dimensions dimensions,
            boolean debugMode,
            Logger logger)
            throws JobExecutionException {
        try {
            return input.getComparerMultiplex(left)
                    .loadAsObjects(input.pathForBindingRequired(), dimensions, debugMode, logger);
        } catch (InputReadFailedException e) {
            throw new JobExecutionException(e);
        }
    }
}
