/* (C)2020 */
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
import org.anchoranalysis.feature.calc.FeatureCalcException;
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

        } catch (CreateException | FeatureCalcException e) {
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
            double nrg = session.calc(createFeatureCalcParams(mark, regionMap, nrgStack));

            return (nrg >= minVal);

        } catch (FeatureCalcException e) {

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
