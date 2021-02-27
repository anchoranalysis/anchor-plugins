/*-
 * #%L
 * anchor-plugin-mpp
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

package org.anchoranalysis.plugin.mpp.bean.provider.single;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.provider.Provider;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.exception.InitException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.calculate.FeatureInitialization;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.single.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.DimensionsProvider;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.mpp.bean.provider.SingleMarkProvider;
import org.anchoranalysis.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.mpp.mark.Mark;
import org.anchoranalysis.plugin.image.bean.dimensions.provider.GuessDimensions;

public class RequireFeatureRelationThreshold extends SingleMarkProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private Provider<Optional<Mark>> markProvider;

    @BeanField @Getter @Setter private FeatureProvider<FeatureInputMark> feature;

    @BeanField @Getter @Setter private double threshold;

    @BeanField @Getter @Setter private RelationBean relation;

    @BeanField @OptionalBean @Getter @Setter
    private DimensionsProvider dimensions = new GuessDimensions();
    // END BEAN PROPERTIES

    @Override
    public Optional<Mark> create() throws CreateException {

        return OptionalUtilities.flatMap(
                markProvider.create(),
                mark -> OptionalUtilities.createFromFlag(isFeatureAccepted(mark), mark));
    }

    private boolean isFeatureAccepted(Mark mark) throws CreateException {
        Feature<FeatureInputMark> featureCreated = feature.create();

        double featureVal = calculateInput(featureCreated, mark);

        return relation.create().isRelationToValueTrue(featureVal, threshold);
    }

    private Optional<Dimensions> dimensions() throws CreateException {
        if (dimensions != null) {
            return Optional.of(dimensions.create());
        } else {
            return Optional.empty();
        }
    }

    private double calculateInput(Feature<FeatureInputMark> feature, Mark mark)
            throws CreateException {

        FeatureInputMark input = new FeatureInputMark(mark, dimensions());

        try {
            FeatureCalculatorSingle<FeatureInputMark> session =
                    FeatureSession.with(
                            feature,
                            new FeatureInitialization(),
                            getInitialization().getFeature().getSharedFeatures(),
                            getLogger());
            return session.calculate(input);

        } catch (FeatureCalculationException | InitException e) {
            throw new CreateException(e);
        }
    }
}
