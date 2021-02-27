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

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.assignment.AssignmentObjectFactory;
import org.anchoranalysis.annotation.io.assignment.AssignmentOverlapFromPairs;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.shared.FeaturesInitialization;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.input.FeatureInputPairObjects;
import org.anchoranalysis.io.output.enabled.OutputEnabledMutable;
import org.anchoranalysis.io.output.outputter.InputOutputContext;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroup;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroupObject;
import org.anchoranalysis.plugin.annotation.comparison.ObjectsToCompare;

/**
 * Assigns an objects from one set of objects to another based upon a cost (degree of overlap).
 *
 * <p>This is a form of <a href="https://en.wikipedia.org/wiki/Bipartite_graph">bipartite
 * matching</a>.
 *
 * <p>The following outputs are produced:
 *
 * <table>
 * <caption></caption>
 * <thead>
 * <tr><th>Output Name</th><th>Default?</th><th>Description</th></tr>
 * </thead>
 * <tbody>
 * <tr><td>costMatrix</td><td>no</td><td>a CSV file showing a matrix of costs calculated for the objects.</td></tr>
 * </tbody>
 * </table>
 *
 * @author Owen Feehan
 */
public class FeatureCostAssigner extends AnnotationComparisonAssigner<AssignmentOverlapFromPairs> {

    private static final String OUTPUT_COST_MATRIX = "costMatrix";

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FeatureEvaluator<FeatureInputPairObjects> featureEvaluator;

    @BeanField @Getter @Setter private double maxCost = 1.0;

    @BeanField @Getter @Setter private int numberDecimalPlaces = 3;

    @BeanField @Getter @Setter private boolean removeTouchingBorderXY = false;
    // END BEAN PROPERTIES

    @Override
    public AssignmentOverlapFromPairs createAssignment(
            ObjectsToCompare objectsToCompare,
            Dimensions dimensions,
            boolean useMIP,
            InputOutputContext context)
            throws CreateException {
        try {
            FeaturesInitialization soFeature =
                    FeaturesInitialization.create(context.getLogger(), context.getModelDirectory());
            featureEvaluator.initRecursive(soFeature, context.getLogger());

            AssignmentObjectFactory assignmentCreator =
                    new AssignmentObjectFactory(featureEvaluator, useMIP);

            AssignmentOverlapFromPairs assignment =
                    assignmentCreator.createAssignment(
                            objectsToCompare.getLeft(),
                            objectsToCompare.getRight(),
                            maxCost,
                            dimensions);

            // We remove any border items from the assignment
            if (removeTouchingBorderXY) {
                assignment.removeTouchingBorderXY(dimensions.extent());
            }

            context.getOutputter()
                    .writerSelective()
                    .write(
                            OUTPUT_COST_MATRIX,
                            () -> new ObjectsCostMatrixGenerator(numberDecimalPlaces),
                            assignmentCreator::getCosts);

            return assignment;
        } catch (FeatureCalculationException | InitException e) {
            throw new CreateException(e);
        }
    }

    @Override
    public AnnotationGroup<AssignmentOverlapFromPairs> groupForKey(String key) {
        return new AnnotationGroupObject(key);
    }

    @Override
    public boolean moreThanOneObject() {
        return true;
    }

    @Override
    public void addDefaultOutputs(OutputEnabledMutable outputs) {
        // NO OUTPUTS TO ADD
    }
}
