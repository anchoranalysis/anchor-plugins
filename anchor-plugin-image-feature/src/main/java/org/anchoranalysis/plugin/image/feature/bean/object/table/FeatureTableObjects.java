/* (C)2020 */
package org.anchoranalysis.plugin.image.feature.bean.object.table;

import java.util.List;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.session.FeatureTableCalculator;
import org.anchoranalysis.image.object.ObjectCollection;

/**
 * A feature-table describing objects defined by columns (features) and rows (inputs).
 *
 * <p>A row may represent a single object, or a pair of objects, or any other derived inputs from an
 * object-collection.
 *
 * @author Owen Feehan
 * @param T type of fexture used in the table
 */
public abstract class FeatureTableObjects<T extends FeatureInput>
        extends AnchorBean<FeatureTableObjects<T>> {

    /**
     * Creates features that will be applied on the objects. Features should always be duplicated
     * from the input list.
     *
     * @param list
     * @param storeFactory
     * @param suppressErrors
     * @return
     * @throws CreateException
     */
    public abstract FeatureTableCalculator<T> createFeatures(
            List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> list,
            NamedFeatureStoreFactory storeFactory,
            boolean suppressErrors)
            throws CreateException, InitException;

    /** Generates a unique identifier for a particular input */
    public abstract String uniqueIdentifierFor(T input);

    public abstract List<T> createListInputs(
            ObjectCollection objects, NRGStackWithParams nrgStack, Logger logger)
            throws CreateException;
}
