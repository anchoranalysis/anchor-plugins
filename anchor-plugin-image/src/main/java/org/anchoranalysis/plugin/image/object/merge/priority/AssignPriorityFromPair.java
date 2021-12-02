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

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.core.log.error.ErrorReporter;
import org.anchoranalysis.feature.calculate.bound.FeatureCalculatorSingle;
import org.anchoranalysis.image.feature.input.FeatureInputPairObjects;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.math.relation.DoubleBiPredicate;
import org.anchoranalysis.plugin.image.object.merge.ObjectVertex;

/**
 * Calculates pair-feature on each potential merge, and this value determines priority..
 *
 * @author Owen Feehan
 */
@AllArgsConstructor
public class AssignPriorityFromPair extends AssignPriority {

    private final FeatureCalculatorSingle<FeatureInputPairObjects> featureCalculator;
    private final double threshold;
    private final DoubleBiPredicate relation;

    @Override
    public PrioritisedVertex assignPriorityToEdge(
            ObjectVertex source,
            ObjectVertex destination,
            ObjectMask merged,
            ErrorReporter errorReporter)
            throws OperationFailedException {

        double resultPair =
                featureCalculator.calculateSuppressErrors(
                        createInput(source, destination, merged), errorReporter);

        return new PrioritisedVertex(merged, 0, resultPair, relation.test(resultPair, threshold));
    }

    private FeatureInputPairObjects createInput(
            ObjectVertex sourceWithFeature,
            ObjectVertex destinationWithFeature,
            ObjectMask merged) {
        return new FeatureInputPairObjects(
                sourceWithFeature.getObject(),
                destinationWithFeature.getObject(),
                Optional.empty(),
                Optional.of(merged));
    }
}
