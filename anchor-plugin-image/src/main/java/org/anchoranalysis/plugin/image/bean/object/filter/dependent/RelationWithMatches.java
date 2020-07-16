/* (C)2020 */
package org.anchoranalysis.plugin.image.bean.object.filter.dependent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.relation.RelationToValue;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.session.calculator.cached.FeatureCalculatorCachedSingle;
import org.anchoranalysis.image.bean.object.ObjectMatcher;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.filter.ObjectFilterRelation;

/**
 * Matches each object with others, and keeps only those where a relation holds true for all matches
 * (in terms of features)
 *
 * @author Owen Feehan
 */
public class RelationWithMatches extends ObjectFilterRelation {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FeatureEvaluator<FeatureInputSingleObject> featureEvaluator;

    @BeanField @OptionalBean @Getter @Setter
    private FeatureEvaluator<FeatureInputSingleObject>
            featureEvaluatorMatch; // Optionally uses a different evaluator for the matched objects

    @BeanField @Getter @Setter private ObjectMatcher matcher;

    /** Size of feature evaluation cache for featureEvaluatorMatch */
    @BeanField @Getter @Setter private int cacheSize = 50;
    // END BEAN PROPERTIES

    private FeatureCalculatorSingle<FeatureInputSingleObject> evaluatorForMatch;
    private FeatureCalculatorSingle<FeatureInputSingleObject> evaluatorForSource;
    private Map<ObjectMask, ObjectCollection> matches;

    @Override
    protected void start(Optional<ImageDimensions> dim, ObjectCollection objectsToFilter)
            throws OperationFailedException {
        super.start(dim, objectsToFilter);

        setupEvaluators();

        matches = createMatchesMap(matcher.findMatch(objectsToFilter));

        // If any object has no matches, throw an exception
        if (matches.values().stream().anyMatch(ObjectCollection::isEmpty)) {
            throw new OperationFailedException("No matches found for at least one object");
        }
    }

    private void setupEvaluators() throws OperationFailedException {

        evaluatorForSource = featureEvaluator.createAndStartSession();

        // Use a specific evaluator for matching if defined, otherwise reuse the existing evaluator
        // and cache it so we don't have to repeatedly calculate on the same object
        evaluatorForMatch =
                new FeatureCalculatorCachedSingle<>(
                        featureEvaluatorMatch != null
                                ? featureEvaluatorMatch.createAndStartSession()
                                : evaluatorForSource,
                        cacheSize);
    }

    @Override
    protected boolean match(
            ObjectMask object, Optional<ImageDimensions> dim, RelationToValue relation)
            throws OperationFailedException {
        try {
            double val = evaluatorForSource.calc(new FeatureInputSingleObject(object));
            return doesMatchAllAssociatedObjects(val, matches.get(object), relation);
        } catch (FeatureCalcException e) {
            throw new OperationFailedException(e);
        }
    }

    @Override
    protected void end() throws OperationFailedException {
        super.end();
        evaluatorForMatch = null;
        evaluatorForSource = null;
        matches = null;
    }

    private boolean doesMatchAllAssociatedObjects(
            double val, ObjectCollection matches, RelationToValue relation)
            throws FeatureCalcException {

        for (ObjectMask match : matches) {

            double valMatch = evaluatorForMatch.calc(new FeatureInputSingleObject(match));

            if (!relation.isRelationToValueTrue(val, valMatch)) {
                return false;
            }
        }
        return true;
    }

    private static Map<ObjectMask, ObjectCollection> createMatchesMap(List<MatchedObject> list) {
        return list.stream()
                .collect(Collectors.toMap(MatchedObject::getSource, MatchedObject::getMatches));
    }
}
