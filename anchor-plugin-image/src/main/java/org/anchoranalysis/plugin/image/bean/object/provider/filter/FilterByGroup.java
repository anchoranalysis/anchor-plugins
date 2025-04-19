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

package org.anchoranalysis.plugin.image.bean.object.provider.filter;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.core.dimensions.Dimensions;
import org.anchoranalysis.image.core.object.MatchedObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.ObjectCollectionFactory;
import org.anchoranalysis.image.voxel.object.ObjectMask;
import org.anchoranalysis.plugin.image.bean.object.match.MatcherIntersectionHelper;

/**
 * Filters an {@link ObjectCollection} by grouping objects and then applying a filter to each group.
 *
 * <p>Objects are grouped based on their intersection with objects from another collection,
 * and then the filter is applied to each group independently.</p>
 */
public class FilterByGroup extends ObjectCollectionProviderFilterBase {

    // START BEAN PROPERTIES
    /**
     * Provides the {@link ObjectCollection} used for grouping the objects to be filtered.
     */
    @BeanField @Getter @Setter private ObjectCollectionProvider objectsGrouped;
    // END BEAN PROPERTIES

    // The createFromObjects method is intentionally left without a doc-string as it's an override.
    @Override
    protected ObjectCollection createFromObjects(
            ObjectCollection objects,
            Optional<List<ObjectMask>> objectsRejected,
            Optional<Dimensions> dim)
            throws ProvisionFailedException {

        List<MatchedObject> matchList =
                MatcherIntersectionHelper.matchIntersectingObjects(objectsGrouped.get(), objects);

        return ObjectCollectionFactory.flatMapFromCollection(
                matchList.stream().map(MatchedObject::getMatches),
                CreateException.class,
                matches -> filter(matches, dim, objectsRejected).streamStandardJava());
    }
}