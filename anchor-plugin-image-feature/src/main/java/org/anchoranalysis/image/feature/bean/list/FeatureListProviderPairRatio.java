package org.anchoranalysis.image.feature.bean.list;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.operator.FeatureListElem;
import org.anchoranalysis.image.feature.objmask.pair.merged.FeatureInputPairObjsMerged;

import ch.ethz.biol.cell.mpp.nrg.feature.operator.Divide;

public abstract class FeatureListProviderPairRatio extends FeatureListProviderAggregate {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Feature<FeatureInputPairObjsMerged> ratioTwoFeatures(
		Feature<FeatureInputPairObjsMerged> featFirst,
		Feature<FeatureInputPairObjsMerged> featSecond
	) {
		Divide<FeatureInputPairObjsMerged> out = new Divide<>();
		out.setAvoidDivideByZero(true);
		out.getList().add(featFirst);
		out.getList().add(featSecond);
		return out;
	}
	
	@Override
	protected Feature<FeatureInputPairObjsMerged> createAggregateFeature(
		Feature<FeatureInputPairObjsMerged> featFirst,
		Feature<FeatureInputPairObjsMerged> featSecond,
		Feature<FeatureInputPairObjsMerged> featMerged
	) {
		Feature<FeatureInputPairObjsMerged> firstToSecond = ratioTwoFeatures(featFirst, featSecond);
		Feature<FeatureInputPairObjsMerged> secondToFirst = ratioTwoFeatures(featSecond, featFirst);
		return createAggregateFeatureOnRatio( firstToSecond, secondToFirst, featMerged );
	}
	
	protected Feature<FeatureInputPairObjsMerged> createAggregateFeatureOnRatio(
		Feature<FeatureInputPairObjsMerged> firstToSecond,
		Feature<FeatureInputPairObjsMerged> secondToFirst,
		Feature<FeatureInputPairObjsMerged> featMerged
	) {
		FeatureListElem<FeatureInputPairObjsMerged> featWithList = createFeature();
		ListUtilities.addFeaturesToList( firstToSecond, secondToFirst, featWithList.getList() );
		return featWithList;
	}
	
	protected abstract FeatureListElem<FeatureInputPairObjsMerged> createFeature();
}
