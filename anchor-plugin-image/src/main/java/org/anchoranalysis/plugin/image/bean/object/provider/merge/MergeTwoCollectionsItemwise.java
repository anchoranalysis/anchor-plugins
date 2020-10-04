/*-
 * #%L
 * anchor-plugin-image
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

package org.anchoranalysis.plugin.image.bean.object.provider.merge;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.merge.ObjectMaskMerger;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.factory.ObjectCollectionFactory;

/**
 * Merges itemwise objects from two collections.
 *
 * <p>e.g. element 0 from both collections are merged together, then element 1 etc.
 *
 * <p>Each collection must have the same number of objects, as will the newly created merged
 * collection.
 *
 * @author Owen Feehan
 */
public class MergeTwoCollectionsItemwise extends ObjectCollectionProvider {

    // START BEAN PROPERTIES
    /** First collection with items to be merged */
    @BeanField @Getter @Setter private ObjectCollectionProvider objects1;

    /** Second collection with items to be merged */
    @BeanField @Getter @Setter private ObjectCollectionProvider objects2;
    // END BEAN PROPERTIES

    @Override
    public ObjectCollection create() throws CreateException {

        ObjectCollection first = objects1.create();
        ObjectCollection second = objects2.create();

        if (first.size() != second.size()) {
            throw new CreateException(
                    String.format(
                            "Both object-providers must have the same number of items, currently %d and %d",
                            first.size(), second.size()));
        }

        return ObjectCollectionFactory.mapFromRange(
                0,
                first.size(),
                index -> ObjectMaskMerger.merge(first.get(index), second.get(index)));
    }
}
