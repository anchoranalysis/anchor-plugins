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

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.image.bean.unitvalue.distance.UnitValueDistance;
import org.anchoranalysis.plugin.image.object.merge.condition.BeforeCondition;
import org.anchoranalysis.plugin.image.object.merge.condition.DistanceCondition;

/**
 * Expands {@link MergeBase} by optionally imposing a maximum-distance requirement between objects
 * that are possibly merged.
 *
 * @author Owen Feehan
 */
public abstract class MergeWithOptionalDistanceConstraint extends MergeBase {

    // START BEAN FIELDS
    /** If true, suppresses the Z-dimension when calculating distances between objects. */
    @BeanField @Getter @Setter private boolean suppressZ = false;

    /** An optional maximum distance between objects that can be merged. */
    @BeanField @OptionalBean @Getter @Setter private UnitValueDistance maxDistance;
    // END BEAN FIELDS

    /**
     * Creates a {@link BeforeCondition} that checks the distance between objects before merging.
     *
     * @return a {@link BeforeCondition} that enforces the distance constraint if {@code
     *     maxDistance} is set, otherwise a condition that always returns true
     */
    protected BeforeCondition maybeDistanceCondition() {
        return new DistanceCondition(
                Optional.ofNullable(maxDistance), suppressZ, getLogger().messageLogger());
    }
}
