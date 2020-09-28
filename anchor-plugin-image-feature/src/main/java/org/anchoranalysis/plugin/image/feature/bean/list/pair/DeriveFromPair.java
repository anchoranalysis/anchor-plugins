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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.bean.list.PrependName;
import org.anchoranalysis.image.feature.bean.object.pair.FeatureDeriveFromPair;
import org.anchoranalysis.image.feature.bean.object.pair.First;
import org.anchoranalysis.image.feature.bean.object.pair.Merged;
import org.anchoranalysis.image.feature.bean.object.pair.Second;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

/**
 * Embeds each feature in a {@link FeatureDeriveFromPair} feature (<i>first</i>, <i>second</i> or
 * <i>merge</i>) and prepends with a string.
 *
 * @author Owen Feehan
 */
public class DeriveFromPair extends FeatureListProvider<FeatureInputPairObjects> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FeatureListProvider<FeatureInputSingleObject> item;

    @BeanField @AllowEmpty @Getter @Setter private String prependString = "";

    /** Either "merged" or "first" or "second" indicating which feature to delegate from */
    @BeanField @Getter @Setter private String select;
    // END BEAN PROPERTIES

    @Override
    public FeatureList<FeatureInputPairObjects> create() throws CreateException {
        return item.create().map(this::pairFromSingle);
    }

    private FeatureDeriveFromPair pairFromSingle(Feature<FeatureInputSingleObject> featExst)
            throws CreateException {
        Feature<FeatureInputSingleObject> featExstDup = featExst.duplicateBean();

        FeatureDeriveFromPair featDelegate = createNewDelegateFeature();
        featDelegate.setItem(featExstDup);

        PrependName.setNewNameOnFeature(featDelegate, featExstDup.getFriendlyName(), prependString);
        return featDelegate;
    }

    private FeatureDeriveFromPair createNewDelegateFeature() throws CreateException {
        if (select.equalsIgnoreCase("first")) {
            return new First();
        } else if (select.equalsIgnoreCase("second")) {
            return new Second();
        } else if (select.equalsIgnoreCase("merged")) {
            return new Merged();
        } else {
            throw new CreateException(
                    "An invalid input existings for 'select'. Select one of 'first', 'second' or 'merged'");
        }
    }
}
