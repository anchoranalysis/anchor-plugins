/* (C)2020 */
package org.anchoranalysis.plugin.annotation.bean.comparison.assigner;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.annotation.io.assignment.AssignmentObjectFactory;
import org.anchoranalysis.annotation.io.assignment.AssignmentOverlapFromPairs;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluatorSimple;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.io.output.bound.BoundIOContext;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroup;
import org.anchoranalysis.plugin.annotation.comparison.AnnotationGroupObject;
import org.anchoranalysis.plugin.annotation.comparison.ObjectsToCompare;

public class FeatureCostAssigner extends AnnotationComparisonAssigner<AssignmentOverlapFromPairs> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter
    private FeatureEvaluatorSimple<FeatureInputPairObjects> featureEvaluator;

    @BeanField @Getter @Setter private double maxCost = 1.0;

    @BeanField @Getter @Setter private int numDecimalPlaces = 3;

    @BeanField @Getter @Setter private boolean removeTouchingBorderXY = false;
    // END BEAN PROPERTIES

    @Override
    public AssignmentOverlapFromPairs createAssignment(
            ObjectsToCompare objectsToCompare,
            ImageDimensions dimensions,
            boolean useMIP,
            BoundIOContext context)
            throws CreateException {
        try {
            SharedFeaturesInitParams soFeature =
                    SharedFeaturesInitParams.create(
                            context.getLogger(), context.getModelDirectory());
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
                assignment.removeTouchingBorderXY(dimensions);
            }

            context.getOutputManager()
                    .getWriterCheckIfAllowed()
                    .write(
                            "costMatrix",
                            () ->
                                    new ObjectsDistanceMatrixGenerator(
                                            assignmentCreator.getCost(), numDecimalPlaces));

            return assignment;
        } catch (FeatureCalcException | InitException e1) {
            throw new CreateException(e1);
        }
    }

    @Override
    public AnnotationGroup<AssignmentOverlapFromPairs> groupForKey(String key) {
        return new AnnotationGroupObject(key);
    }

    @Override
    public boolean moreThanOneObj() {
        return true;
    }
}
