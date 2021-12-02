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

package org.anchoranalysis.plugin.image.feature.bean.list.pair;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.xml.exception.ProvisionFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.name.AssignFeatureNameUtilities;
import org.anchoranalysis.image.feature.bean.object.pair.FeatureDeriveFromPair;
import org.anchoranalysis.image.feature.bean.object.pair.First;
import org.anchoranalysis.image.feature.bean.object.pair.Merged;
import org.anchoranalysis.image.feature.bean.object.pair.Second;
import org.anchoranalysis.image.feature.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.input.FeatureInputSingleObject;

/**
 * Embeds each feature in a {@link FeatureDeriveFromPair} feature (<i>first</i>, <i>second</i> or
 * <i>merge</i>) and prepends with a string.
 *
 * @author Owen Feehan
 */
public class DeriveFromPair extends FeatureListProvider<FeatureInputPairObjects> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FeatureListProvider<FeatureInputSingleObject> item;

    @BeanField @AllowEmpty @Getter @Setter private String prefix = "";

    /** Either "merged" or "first" or "second" indicating which feature to delegate from */
    @BeanField @Getter @Setter private String select;
    // END BEAN PROPERTIES

    @Override
    public FeatureList<FeatureInputPairObjects> get() throws ProvisionFailedException {
        return item.get().map(this::pairFromSingle);
    }

    private FeatureDeriveFromPair pairFromSingle(Feature<FeatureInputSingleObject> existing)
            throws ProvisionFailedException {
        Feature<FeatureInputSingleObject> existingDuplicated = existing.duplicateBean();

        FeatureDeriveFromPair featDelegate = createNewDelegateFeature();
        featDelegate.setItem(existingDuplicated);

        AssignFeatureNameUtilities.assignWithPrefix(
                featDelegate, existingDuplicated.getFriendlyName(), prefix);
        return featDelegate;
    }

    private FeatureDeriveFromPair createNewDelegateFeature() throws ProvisionFailedException {
        if (select.equalsIgnoreCase("first")) {
            return new First();
        } else if (select.equalsIgnoreCase("second")) {
            return new Second();
        } else if (select.equalsIgnoreCase("merged")) {
            return new Merged();
        } else {
            throw new ProvisionFailedException(
                    "An invalid input existings for 'select'. Select one of 'first', 'second' or 'merged'");
        }
    }
}
