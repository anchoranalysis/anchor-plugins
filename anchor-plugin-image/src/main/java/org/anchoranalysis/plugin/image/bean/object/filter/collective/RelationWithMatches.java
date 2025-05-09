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

package org.anchoranalysis.plugin.image.bean.object.filter.collective;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.exception.OperationFailedException;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.bound.FeatureCalculatorSingle;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorCache;
import org.anchoranalysis.image.bean.object.ObjectMatcher;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.object.MatchedObject;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.math.relation.DoubleBiPredicate;
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
    protected void start(Optional<Dimensions> dimensions, ObjectCollection objectsToFilter)
            throws OperationFailedException {
        super.start(dimensions, objectsToFilter);

        setupEvaluators();

        matches = createMatchesMap(matcher.findMatch(objectsToFilter));

        // If any object has no matches, throw an exception
        if (matches.values().stream().anyMatch(ObjectCollection::isEmpty)) {
            throw new OperationFailedException("No matches found for at least one object");
        }
    }

    private void setupEvaluators() throws OperationFailedException {

        evaluatorForSource = featureEvaluator.createFeatureSession();

        FeatureCalculatorSingle<FeatureInputSingleObject> calculator =
                featureEvaluatorMatch != null
                        ? featureEvaluatorMatch.createFeatureSession()
                        : evaluatorForSource;

        // Use a specific evaluator for matching if defined, otherwise reuse the existing evaluator
        // and cache it so we don't have to repeatedly calculate on the same object
        evaluatorForMatch = FeatureCalculatorCache.cache(calculator, cacheSize);
    }

    @Override
    protected boolean match(ObjectMask object, Optional<Dimensions> dim, DoubleBiPredicate relation)
            throws OperationFailedException {
        try {
            double val = evaluatorForSource.calculate(new FeatureInputSingleObject(object));
            return doesMatchAllAssociatedObjects(val, matches.get(object), relation);
        } catch (FeatureCalculationException e) {
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
            double val, ObjectCollection matches, DoubleBiPredicate relation)
            throws FeatureCalculationException {

        for (ObjectMask match : matches) {

            double valMatch = evaluatorForMatch.calculate(new FeatureInputSingleObject(match));

            if (!relation.test(val, valMatch)) {
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
