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

package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.relation.RelationBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.MaskProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectMask;

// Treats the entire binaryimgchnl as an object, and sees if it passes an {@link ObjectFilter}
public class BinaryChnlProviderFeatureRelationAsObjects extends BinaryChnlProviderChnlSource {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private MaskProvider binaryChnlMain;

    @BeanField @Getter @Setter private MaskProvider binaryChnlCompareTo;

    @BeanField @Getter @Setter private MaskProvider binaryChnlElse;

    @BeanField @Getter @Setter private FeatureProvider<FeatureInputSingleObject> featureProvider;

    @BeanField @Getter @Setter private RelationBean relation;
    // END BEAN PROPERTIES

    @Override
    protected Mask createFromSource(Channel chnlSource) throws CreateException {

        Mask mask = binaryChnlMain.create();

        FeatureCalculatorSingle<FeatureInputSingleObject> calculator = createCalculator();

        return calcRelation(
                new ObjectMask(mask),
                new ObjectMask(binaryChnlCompareTo.create()),
                mask,
                NRGStackUtilities.addNrgStack(calculator, chnlSource));
    }

    private FeatureCalculatorSingle<FeatureInputSingleObject> createCalculator()
            throws CreateException {
        try {
            return FeatureSession.with(
                    featureProvider.create(),
                    new FeatureInitParams(),
                    getInitializationParameters().getFeature().getSharedFeatureSet(),
                    getLogger());
        } catch (InitException e1) {
            throw new CreateException(e1);
        }
    }

    private Mask calcRelation(
            ObjectMask objectMain,
            ObjectMask objectCompareTo,
            Mask maskMain,
            FeatureCalculatorSingle<FeatureInputSingleObject> calculator)
            throws CreateException {
        try {
            double valMain = calculator.calculate(new FeatureInputSingleObject(objectMain));
            double valCompareTo =
                    calculator.calculate(new FeatureInputSingleObject(objectCompareTo));

            if (relation.create().isRelationToValueTrue(valMain, valCompareTo)) {
                return maskMain;
            } else {
                return binaryChnlElse.create();
            }
        } catch (FeatureCalculationException e) {
            throw new CreateException(e);
        }
    }
}
