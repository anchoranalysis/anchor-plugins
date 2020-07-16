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

package ch.ethz.biol.cell.mpp.mark.provider;

import ch.ethz.biol.cell.imageprocessing.dim.provider.GuessDimFromInputImage;
import lombok.Getter;
import lombok.Setter;
import java.util.Optional;
import org.anchoranalysis.anchor.mpp.bean.provider.MarkProvider;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.FeatureInputMark;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.extent.ImageDimensions;

public class MarkProviderRequireFeatureRelationThreshold extends MarkProvider {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MarkProvider markProvider;

    @BeanField @Getter @Setter private FeatureProvider<FeatureInputMark> featureProvider;

    @BeanField @Getter @Setter private double threshold;

    @BeanField @Getter @Setter private RelationBean relation;

    @BeanField @OptionalBean @Getter @Setter private ImageDimProvider dim = new GuessDimFromInputImage();
    // END BEAN PROPERTIES

    @Override
    public Optional<Mark> create() throws CreateException {

        Optional<Mark> mark = markProvider.create();
        return OptionalUtilities.flatMap(
                mark,
                m -> {
                    if (isFeatureAccepted(m)) {
                        return mark;
                    } else {
                        return Optional.empty();
                    }
                });
    }

    private boolean isFeatureAccepted(Mark mark) throws CreateException {
        Feature<FeatureInputMark> feature = featureProvider.create();

        double featureVal = calculateInput(feature, mark);

        return relation.create().isRelationToValueTrue(featureVal, threshold);
    }

    private Optional<ImageDimensions> dimensions() throws CreateException {
        if (dim != null) {
            return Optional.of(dim.create());
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
                            new FeatureInitParams(),
                            getInitializationParameters().getFeature().getSharedFeatureSet(),
                            getLogger());
            return session.calc(input);

        } catch (FeatureCalcException e) {
            throw new CreateException(e);
        }
    }
}
