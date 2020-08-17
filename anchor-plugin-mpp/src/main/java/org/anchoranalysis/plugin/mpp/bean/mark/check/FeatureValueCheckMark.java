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

package org.anchoranalysis.plugin.mpp.bean.mark.check;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.init.MPPInitParams;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.bean.mark.CheckMark;
import org.anchoranalysis.anchor.mpp.feature.error.CheckException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.provider.FeatureProvider;
import org.anchoranalysis.feature.calc.FeatureCalculationException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.feature.session.FeatureSession;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.feature.shared.SharedFeatureMulti;

public abstract class FeatureValueCheckMark<T extends FeatureInput> extends CheckMark {

    // START BEANS
    @BeanField @Getter @Setter private FeatureProvider<T> featureProvider;

    @BeanField @Getter @Setter protected double minVal = 0;

    @BeanField @OptionalBean @Getter @Setter private KeyValueParamsProvider keyValueParamsProvider;
    // END BEANS

    private SharedFeatureMulti sharedFeatureSet;

    private FeatureCalculatorSingle<T> session;

    @Override
    public void onInit(MPPInitParams soMPP) throws InitException {
        super.onInit(soMPP);
        sharedFeatureSet = soMPP.getFeature().getSharedFeatureSet();
    }

    @Override
    public void start(NRGStackWithParams nrgStack) throws OperationFailedException {

        try {
            Feature<T> feature = featureProvider.create();

            KeyValueParams kpv = createKeyValueParams();

            session =
                    FeatureSession.with(
                            feature, new FeatureInitParams(kpv), sharedFeatureSet, getLogger());

        } catch (CreateException | InitException e) {
            session = null;
            throw new OperationFailedException(e);
        }
    }

    @Override
    public boolean check(Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack)
            throws CheckException {

        if (session == null) {
            throw new CheckException("No session initialized");
        }

        try {
            double nrg = session.calculate(createFeatureCalcParams(mark, regionMap, nrgStack));

            return (nrg >= minVal);

        } catch (FeatureCalculationException e) {

            throw new CheckException(String.format("Error calculating feature:%n%s", e));
        }
    }

    protected abstract T createFeatureCalcParams(
            Mark mark, RegionMap regionMap, NRGStackWithParams nrgStack);

    private KeyValueParams createKeyValueParams() throws CreateException {
        if (keyValueParamsProvider != null) {
            return keyValueParamsProvider.create();
        } else {
            return new KeyValueParams();
        }
    }

    @Override
    public boolean isCompatibleWith(Mark testMark) {
        return true;
    }
}
