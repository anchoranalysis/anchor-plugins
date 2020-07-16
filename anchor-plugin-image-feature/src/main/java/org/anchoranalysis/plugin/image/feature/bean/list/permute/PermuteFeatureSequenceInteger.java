/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.list.permute;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.BeanInstanceMap;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.bean.permute.property.PermutePropertySequenceInteger;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.input.FeatureInputParams;
import org.anchoranalysis.plugin.operator.feature.bean.Param;

/**
 * Permutes a property on a feature with a sequence of integers.
 *
 * @author Owen Feehan
 * @param <T> feature-input
 */
public abstract class PermuteFeatureSequenceInteger<T extends FeatureInputParams>
        extends PermuteFeatureBase<T> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String paramPrefix;

    @BeanField @Getter @Setter private PermutePropertySequenceInteger permuteProperty;
    // END BEAN PROPERTIES

    // Possible defaultInstances for beans......... saved from checkMisconfigured for delayed checks
    // elsewhere
    private BeanInstanceMap defaultInstances;

    @Override
    public void checkMisconfigured(BeanInstanceMap defaultInstances)
            throws BeanMisconfiguredException {
        super.checkMisconfigured(defaultInstances);
        this.defaultInstances = defaultInstances;
    }

    @Override
    protected FeatureList<T> createPermutedFeaturesFor(Feature<T> feature) throws CreateException {
        PermuteFeature<Integer, T> delegate = createDelegate(feature);

        configurePermutePropertyOnDelegate(delegate);
        try {
            delegate.checkMisconfigured(defaultInstances);
        } catch (BeanMisconfiguredException e) {
            throw new CreateException(e);
        }

        return delegate.create();
    }

    protected abstract PermuteFeature<Integer, T> createDelegate(Feature<T> feature)
            throws CreateException;

    protected abstract PermutePropertySequenceInteger configurePermuteProperty(
            PermutePropertySequenceInteger permuteProperty);

    protected Feature<T> createParam(String suffix, boolean appendNumber) {
        Param<T> param = new Param<>();
        param.setKeyPrefix(paramPrefix);
        param.setKey(sequenceNumberOrEmpty(appendNumber));
        param.setKeySuffix(suffix);
        return param;
    }

    private String sequenceNumberOrEmpty(boolean appendNumber) {
        if (appendNumber) {
            return Integer.toString(permuteProperty.getSequence().getStart());
        } else {
            return "";
        }
    }

    private void configurePermutePropertyOnDelegate(PermuteFeature<Integer, T> delegate) {
        PermutePropertySequenceInteger permutePropertyConfigured =
                configurePermuteProperty(
                        (PermutePropertySequenceInteger) permuteProperty.duplicateBean());

        delegate.setListPermuteProperty(new ArrayList<>());
        delegate.getListPermuteProperty().add(permutePropertyConfigured);
    }
}
