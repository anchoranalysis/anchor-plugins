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

import org.anchoranalysis.annotation.io.assignment.AssignmentMaskIntersection;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroup;
import org.anchoranalysis.plugin.annotation.comparison.ObjectsToCompare;

/**
 * No outputs are produced.
 *
 * @author Owen Feehan
 */
public class MaskIntersectionAssigner
        extends AnnotationComparisonAssigner<AssignmentMaskIntersection> {

    @Override
    public AssignmentMaskIntersection createAssignment(
            ObjectsToCompare objectsToCompare,
            Dimensions dimensions,
            boolean useMIP,
            InputOutputContext context)
            throws CreateException {

        return new AssignmentMaskIntersection(
                extractSingleObject("left", objectsToCompare.getLeft()),
                extractSingleObject("right", objectsToCompare.getRight()));
    }

    @Override
    public AnnotationGroup<AssignmentMaskIntersection> groupForKey(String key) {
        return new AnnotationGroup<>(key);
    }

    @Override
    public boolean moreThanOneObject() {
        return false;
    }

    @Override
    public void addDefaultOutputs(OutputEnabledMutable outputs) {
        // NO OUTPUTS TO ADD
    }

    private static ObjectMask extractSingleObject(String description, ObjectCollection objects)
            throws CreateException {
        if (objects.size() == 0) {
            throw new CreateException(
                    String.format(
                            "%s object-collection contains no objects. Exactly one must exist.",
                            description));
        } else if (objects.size() > 1) {
            throw new CreateException(
                    String.format(
                            "%s object-collection contains %d objects. Only one is allowed.",
                            description, objects.size()));
        } else {
            return objects.get(0);
        }
    }
}
