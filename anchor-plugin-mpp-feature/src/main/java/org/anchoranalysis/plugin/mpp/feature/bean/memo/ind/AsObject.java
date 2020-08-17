/*-
 * #%L
 * anchor-plugin-mpp-feature
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

package org.anchoranalysis.plugin.mpp.feature.bean.memo.ind;

import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.anchor.mpp.bean.regionmap.RegionMap;
import org.anchoranalysis.anchor.mpp.feature.input.memo.FeatureInputSingleMemo;
import org.anchoranalysis.anchor.mpp.mark.conic.RegionMapSingleton;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.bean.operator.FeatureSingleElem;
import org.anchoranalysis.feature.cache.ChildCacheName;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calculate.FeatureCalculationException;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;

public class AsObject extends FeatureSingleElem<FeatureInputSingleMemo, FeatureInputSingleObject> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private RegionMap regionMap = RegionMapSingleton.instance();

    @BeanField @Getter @Setter private int index = 0;
    // END BEAN PROPERTIES

    @Override
    public double calculate(SessionInput<FeatureInputSingleMemo> input)
            throws FeatureCalculationException {
        return input.forChild()
                .calculate(
                        getItem(),
                        new CalculateSingleObjFromMemo(regionMap, index),
                        new ChildCacheName(AsObject.class, index));
    }

    // We change the default behaviour, as we don't want to give the same paramsFactory
    //   as the item we pass to
    @Override
    public Class<? extends FeatureInput> inputType() {
        return FeatureInputSingleMemo.class;
    }

    @Override
    public String describeParams() {
        return getItem().describeParams();
    }
}
