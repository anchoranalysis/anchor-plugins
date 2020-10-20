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

package org.anchoranalysis.plugin.image.object.merge.priority;

import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.image.feature.evaluator.PayloadCalculator;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.object.merge.ObjectVertex;

/**
 * Allows merges if there is an increase in the payload i.e.
 *
 * <p>i.e. if {@code payload(merged) >= avg( payload(src), payload(dest)}
 *
 * <p>Prioritises merges in order of greatest improvement.
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
public class AssignPriorityFromImprovement extends AssignPriority {

    private final PayloadCalculator payloadCalculator;

    @Override
    public PrioritisedVertex assignPriorityToEdge(
            ObjectVertex source,
            ObjectVertex destination,
            ObjectMask merged,
            ErrorReporter errorReporter)
            throws OperationFailedException {
        double payloadMerge = calculatePayload(payloadCalculator, merged);
        double payloadExisting = weightedAverageFeatureVal(source, destination);
        double improvement = payloadMerge - payloadExisting;

        if (payloadMerge <= payloadExisting) {
            // We add an edge anyway, as a placeholder as future merges might make it viable
            return new PrioritisedVertex(merged, payloadMerge, improvement, false);
        }

        return new PrioritisedVertex(merged, payloadMerge, improvement, true);
    }

    private double calculatePayload(PayloadCalculator payloadCalculator, ObjectMask object)
            throws OperationFailedException {
        try {
            return payloadCalculator.calc(object);
        } catch (FeatureCalculationException e) {
            throw new OperationFailedException(e);
        }
    }

    /**
     * A weighted-average of the feature value (weighted according to the relative number of pixeks
     */
    private static double weightedAverageFeatureVal(ObjectVertex first, ObjectVertex second) {
        long size1 = first.numberVoxels();
        long size2 = second.numberVoxels();

        double weightedSum = (first.getPayload() * size1) + (second.getPayload() * size2);
        return weightedSum / (size1 + size2);
    }
}
