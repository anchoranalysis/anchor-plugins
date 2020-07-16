/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.shared.object;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.calculation.CalcForChild;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.input.FeatureInputNRG;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.binary.mask.Mask;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * Calculate a feature, treating a binary-channel as a single-object on the nrg-stack
 *
 * @author Owen Feehan
 */
public class BinaryChannelAsSingleObject<T extends FeatureInputNRG>
        extends FeatureSingleObjectFromShared<T> {

    // START BEAN PROPERTIES
    @BeanField @SkipInit @Getter @Setter private BinaryChnlProvider binaryChnl;
    // END BEAN PROPERTIES

    private Mask chnl;

    @Override
    protected void beforeCalcWithImageInitParams(ImageInitParams params) throws InitException {
        binaryChnl.initRecursive(params, getLogger());

        try {
            chnl = binaryChnl.create();
        } catch (CreateException e) {
            throw new InitException(e);
        }
    }

    @Override
    protected double calc(
            CalcForChild<T> calcForChild, Feature<FeatureInputSingleObject> featureForSingleObject)
            throws FeatureCalcException {
        return calcForChild.calc(
                featureForSingleObject,
                new CalculateBinaryChnlInput<>(chnl),
                new ChildCacheName(BinaryChannelAsSingleObject.class, chnl.hashCode()));
    }
}
