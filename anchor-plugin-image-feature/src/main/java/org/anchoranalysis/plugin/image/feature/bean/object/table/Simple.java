/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.table;

import java.util.List;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.session.FeatureTableCalculator;
import org.anchoranalysis.image.feature.session.SingleObjTableSession;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;

/**
 * Simply selects features directly from the list, and objects directly from the list passed.
 *
 * @author Owen Feehan
 */
public class Simple extends FeatureTableObjects<FeatureInputSingleObject> {

    @Override
    public FeatureTableCalculator<FeatureInputSingleObject> createFeatures(
            List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> list,
            NamedFeatureStoreFactory storeFactory,
            boolean suppressErrors)
            throws CreateException {
        return new SingleObjTableSession(storeFactory.createNamedFeatureList(list));
    }

    @Override
    public String uniqueIdentifierFor(FeatureInputSingleObject input) {
        return UniqueIdentifierUtilities.forObject(input.getObject());
    }

    @Override
    public List<FeatureInputSingleObject> createListInputs(
            ObjectCollection objects, NRGStackWithParams nrgStack, Logger logger)
            throws CreateException {
        return objects.stream()
                .mapToList(
                        object ->
                                new FeatureInputSingleObject(
                                        checkObjectInsideScene(
                                                object, nrgStack.getDimensions().getExtent()),
                                        nrgStack));
    }

    private ObjectMask checkObjectInsideScene(ObjectMask object, Extent extent)
            throws CreateException {
        if (!extent.contains(object.getBoundingBox())) {
            throw new CreateException(
                    String.format(
                            "Object is not (perhaps fully) contained inside the scene: %s is not in %s",
                            object.getBoundingBox(), extent));
        }
        return object;
    }
}
