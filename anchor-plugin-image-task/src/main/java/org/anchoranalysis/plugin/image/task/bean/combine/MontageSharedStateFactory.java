/*-
 * #%L
 * anchor-plugin-image-task
 * %%
 * Copyright (C) 2010 - 2022 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.image.task.bean.combine;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.anchoranalysis.core.time.OperationContext;
import org.anchoranalysis.experiment.ExperimentExecutionException;
import org.anchoranalysis.image.bean.nonbean.spatial.arrange.ArrangeStackException;
import org.anchoranalysis.image.bean.nonbean.spatial.arrange.BoundingBoxEnclosed;
import org.anchoranalysis.image.bean.nonbean.spatial.arrange.StackArrangement;
import org.anchoranalysis.image.bean.spatial.arrange.StackArranger;
import org.anchoranalysis.image.voxel.resizer.VoxelsResizer;
import org.anchoranalysis.plugin.image.task.slice.MontageSharedState;
import org.anchoranalysis.spatial.box.Extent;
import org.apache.commons.math3.util.Pair;

/**
 * Creates a {@link MontageSharedState}.
 *
 * @author Owen Feehan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class MontageSharedStateFactory {

    /**
     * Create the {@link MontageSharedState}.
     *
     * @param sizes the size of each image to place into the arrangement, together with the path it
     *     was derived from.
     * @param arranger how to arrange the images into a {@link StackArrangement}.
     * @param resizer how to resize an image.
     * @param context operation context.
     * @return the newly created shared-state.
     * @throws ExperimentExecutionException if a stack arrangement cannot be successfully
     *     determined.
     */
    public static MontageSharedState create(
            List<Pair<Path, Extent>> sizes,
            StackArranger arranger,
            VoxelsResizer resizer,
            OperationContext context)
            throws ExperimentExecutionException {

        try {
            StackArrangement arrangement =
                    context.getExecutionTimeRecorder()
                            .recordExecutionTime(
                                    "Arranging the stacks",
                                    () ->
                                            arranger.arrangeStacks(
                                                    sizes.stream().map(Pair::getSecond).iterator(),
                                                    context));

            // Create the shared state with the bounding-box mapping and overall size for the
            // combined image.
            return new MontageSharedState(
                    mapFromArrangement(sizes, arrangement),
                    arrangement.extent(),
                    resizer,
                    context.getExecutionTimeRecorder());
        } catch (ArrangeStackException e) {
            throw new ExperimentExecutionException("Cannot determine a stack arrangement for", e);
        }
    }

    /** Map each path to the corresponding bounding-box from the arrangement. */
    private static Map<Path, BoundingBoxEnclosed> mapFromArrangement(
            List<Pair<Path, Extent>> sizes, StackArrangement arrangement) {
        Map<Path, BoundingBoxEnclosed> boxMapping = new HashMap<>();
        for (int i = 0; i < sizes.size(); i++) {
            boxMapping.put(sizes.get(i).getFirst(), arrangement.get(i));
        }
        return boxMapping;
    }
}
