/*-
 * #%L
 * anchor-plugin-image-feature
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

package org.anchoranalysis.plugin.image.feature.bean.object.combine;

import java.util.List;
import java.util.Optional;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.annotation.BeanField;
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
import org.anchoranalysis.image.stack.DisplayStack;
import org.anchoranalysis.plugin.image.bean.thumbnail.object.OutlinePreserveRelativeSize;
import org.anchoranalysis.plugin.image.bean.thumbnail.object.ThumbnailFromObjects;
import lombok.Getter;
import lombok.Setter;

/**
 * A way to combine (or not combine) objects so that they provide a feature-table.
 * <p>
 * Columns in the feature-table always represent features.
 * <p>
 * A row may represent a single object, or a pair of objects, or any other derived inputs from an
 * object-collection, depending on the implementation of the sub-class.
 *
 * @author Owen Feehan
 * @param T type of feature used in the table
 */
public abstract class CombineObjectsForFeatures<T extends FeatureInput>
        extends AnchorBean<CombineObjectsForFeatures<T>> {

    /** Generates a thumbnail representation of one or more objects combine, as form a single input */
    @BeanField @Getter @Setter private ThumbnailFromObjects thumbnail = new OutlinePreserveRelativeSize();
    
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

    /**
     * Derives a list of inputs (i.e. rows in a feature table)
     * <p>
     * This should be called only ONCE, and always before {@link #createThumbailFor(FeatureInput)}
     * 
     * @param objects the objects from which inputs are derived
     * @param nrgStack nrg-stack used during feature calculation
     * @param logger logger
     * @return the list of inputs
     * @throws CreateException
     */
    public List<T> deriveInputs(
            ObjectCollection objects, NRGStackWithParams nrgStack, Logger logger)
            throws CreateException {
        List<T> inputs = deriveInputsFromObjects(objects, nrgStack, logger);
        thumbnail.start(objects, Optional.of(nrgStack.asStack()));
        return inputs;
    }
    
    /**
     * Derives a list of inputs from an object-collection
     * 
     * @param objects the object-collection
     * @param nrgStack nrg-stack used during feature calculation
     * @param logger logger
     * @return the list of inputs
     * @throws CreateException
     */
    protected abstract List<T> deriveInputsFromObjects(
            ObjectCollection objects, NRGStackWithParams nrgStack, Logger logger)
            throws CreateException;
    
    /**
     * Creates a thumbnail for a particular input
     * 
     * @param input the input
     * @return the thumbnail (if its supported)
     */
    public abstract Optional<DisplayStack> createThumbailFor(T input) throws CreateException;
}
