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

package org.anchoranalysis.plugin.annotation.bean.comparison.assigner;

import org.anchoranalysis.annotation.io.assignment.Assignment;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.annotation.comparison.ObjectsToCompare;
import org.anchoranalysis.plugin.annotation.counter.ImageCounterWithStatistics;

/**
 * Compares similar object-sets produced from two different sources.
 * 
 * <p>An assignment is created indicating the differences and similarities between the object-sets.
 *
 * @author Owen Feehan
 * @param <T> the type of {@link Assignment} used for object masks.
 */
public abstract class AnnotationComparisonAssigner<T extends Assignment<ObjectMask>>
        extends AnchorBean<AnnotationComparisonAssigner<T>> {

    /**
     * Creates an assignment based on the objects to compare.
     *
     * @param objectsToCompare the objects to compare.
     * @param dimensions the dimensions of the image.
     * @param useMIP whether to use Maximum Intensity Projection.
     * @param context the input-output context.
     * @return the created assignment.
     * @throws CreateException if the assignment cannot be created.
     */
    public abstract T createAssignment(
            ObjectsToCompare objectsToCompare,
            Dimensions dimensions,
            boolean useMIP,
            InputOutputContext context)
            throws CreateException;

    /**
     * Gets the {@link ImageCounterWithStatistics} for a specific key.
     *
     * @param key the key to get the group for.
     * @return the {@link ImageCounterWithStatistics} for the given key.
     */
    public abstract ImageCounterWithStatistics<T> groupForKey(String key);

    /**
     * Checks if more than one object can exist in the assignment.
     *
     * @return true if more than one object can exist, false otherwise.
     */
    public abstract boolean moreThanOneObject();

    /**
     * Adds any default outputs that should occur from the assigner.
     *
     * @param outputs the {@link OutputEnabledMutable} to add the default outputs to.
     */
    public abstract void addDefaultOutputs(OutputEnabledMutable outputs);
}