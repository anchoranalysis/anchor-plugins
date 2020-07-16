/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.shared;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.feature.bean.object.pair.FeaturePairObjects;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * Evaluates the object as a pair-feature together with the binary-mask from the shard objects.
 *
 * @author Owen Feehan
 */
public class PairedWithBinaryChannel extends FeatureSingleObject {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FeaturePairObjects item;

    // This cannot be initialized in the normal way, as Feature isn't contained in a Shared-Objects
    // container. So instead it's initialized at a later point.
    @BeanField @SkipInit @Getter @Setter private BinaryChnlProvider binaryChnl;
    // END BEAN PROPERTIES

    private Mask chnl;

    @Override
    protected void beforeCalc(FeatureInitParams paramsInit) throws InitException {
        super.beforeCalc(paramsInit);

        binaryChnl.initRecursive(
                new ImageInitParams(paramsInit.sharedObjectsRequired()), getLogger());

        try {
            chnl = binaryChnl.create();
        } catch (CreateException e) {
            throw new InitException(e);
        }
    }

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {
        return input.forChild()
                .calc(
                        item,
                        new CalculatePairInput(chnl),
                        new ChildCacheName(PairedWithBinaryChannel.class, chnl.hashCode()));
    }
}
