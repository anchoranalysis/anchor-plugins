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

package org.anchoranalysis.plugin.image.bean.object.match;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.bean.object.ObjectMatcher;
import org.anchoranalysis.image.core.object.MatchedObject;
import org.anchoranalysis.image.voxel.object.ObjectCollection;
import org.anchoranalysis.image.voxel.object.factory.ObjectCollectionFactory;

/**
 * Matches to another object, and then uses that object to bridge to another
 *
 * @author Owen Feehan
 */
public class Bridge extends ObjectMatcher {

    // START BEAN PROPERTIES
    /** Used to match each input-object to an intermediary-object */
    @BeanField @Getter @Setter private ObjectMatcher bridgeMatcher;

    /** Used to match each intermediary-object to a final-object */
    @BeanField @Getter @Setter private ObjectMatcher objectMatcher;
    // END BEAN PROPERTIES

    @Override
    public List<MatchedObject> findMatch(ObjectCollection sourceObjects)
            throws OperationFailedException {

        List<MatchedObject> bridgeMatches = bridgeMatcher.findMatch(sourceObjects);

        ObjectCollection bridgeObjects =
                ObjectCollectionFactory.flatMapFrom(
                        bridgeMatches.stream().map(MatchedObject::getMatches),
                        OperationFailedException.class,
                        Bridge::checkExactlyOneMatch);

        return objectMatcher.findMatch(bridgeObjects);
    }

    private static ObjectCollection checkExactlyOneMatch(ObjectCollection matches)
            throws OperationFailedException {

        if (matches.size() == 0) {
            throw new OperationFailedException("At least one object has no match. One is needed");
        }

        if (matches.size() > 1) {
            throw new OperationFailedException(
                    "At least one object has multiple matches. Only one is allowed.");
        }

        return matches;
    }
}
