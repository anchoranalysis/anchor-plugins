/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.shared;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.feature.bean.operator.FeatureOperator;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;

/**
 * Retrieves a parameter from a collection in shared-objects.
 *
 * <p>This differs from {@link org.anchoranalysis.plugin.operator.feature.bean.Param} which reads
 * the parameter from the nrg-stack, whereas this from a specific parameters collection.
 *
 * @author Owen Feehan
 * @param <T> feature-input-type
 */
public class ParamFromCollection<T extends FeatureInput> extends FeatureOperator<T> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private String collectionID = "";

    @BeanField @Getter @Setter private String key = "";
    // END BEAN PROPERTIES

    private double val;

    @Override
    protected void beforeCalc(FeatureInitParams paramsInit) throws InitException {
        super.beforeCalc(paramsInit);

        ImageInitParams imageInit = new ImageInitParams(paramsInit.sharedObjectsRequired());
        try {
            KeyValueParams kpv =
                    imageInit
                            .getParams()
                            .getNamedKeyValueParamsCollection()
                            .getException(collectionID);
            this.val = kpv.getPropertyAsDouble(key);

        } catch (NamedProviderGetException e) {
            throw new InitException(e.summarize());
        }
    }

    @Override
    public double calc(SessionInput<T> input) throws FeatureCalcException {
        return val;
    }
}
