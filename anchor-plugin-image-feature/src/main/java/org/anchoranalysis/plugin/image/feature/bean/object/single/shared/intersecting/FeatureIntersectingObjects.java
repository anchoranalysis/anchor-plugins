/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.single.shared.intersecting;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.ResolvedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.calc.FeatureInitParams;
import org.anchoranalysis.image.bean.nonbean.init.ImageInitParams;
import org.anchoranalysis.image.feature.bean.object.single.FeatureSingleObject;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.object.ObjectCollection;

public abstract class FeatureIntersectingObjects extends FeatureSingleObject {

    // START BEAN PROPERTIES
    /** ID for the particular object-collection */
    @BeanField @Getter @Setter private String id = "";

    @BeanField @Getter @Setter private double valueNoObjects = Double.NaN;
    // END BEAN PROPERTIES

    private ObjectCollection searchObjects;

    @Override
    protected void beforeCalc(FeatureInitParams paramsInit) throws InitException {
        super.beforeCalc(paramsInit);

        ImageInitParams imageInit = new ImageInitParams(paramsInit.sharedObjectsRequired());
        try {
            this.searchObjects = imageInit.getObjectCollection().getException(id);
        } catch (NamedProviderGetException e) {
            throw new InitException(e.summarize());
        }
    }

    @Override
    public double calc(SessionInput<FeatureInputSingleObject> input) throws FeatureCalcException {

        if (getSearchObjects().isEmpty()) {
            return getValueNoObjects();
        }

        return valueFor(
                input,
                input.resolver().search(new CalculateIntersectingObjects(id, searchObjects)));
    }

    protected abstract double valueFor(
            SessionInput<FeatureInputSingleObject> params,
            ResolvedCalculation<ObjectCollection, FeatureInputSingleObject> intersecting)
            throws FeatureCalcException;

    protected ObjectCollection getSearchObjects() {
        return searchObjects;
    }
}
