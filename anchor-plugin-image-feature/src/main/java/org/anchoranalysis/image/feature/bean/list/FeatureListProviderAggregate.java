package org.anchoranalysis.image.feature.bean.list;

/*
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.bean.list.FeatureListProviderPrependName;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.merged.FeatureObjMaskPairMergedParams;

import ch.ethz.biol.cell.mpp.nrg.feature.objmaskpairmerged.FromFirst;
import ch.ethz.biol.cell.mpp.nrg.feature.objmaskpairmerged.FromMerged;
import ch.ethz.biol.cell.mpp.nrg.feature.objmaskpairmerged.FromSecond;

/**
 * Takes a list of features, and creates a new list of features, where each
 *   feature is embedded in a FromExisting feature and prepended with a prepend String
 * 
 * @author Owen Feehan
 * 
 * @param T feature-calc-params type
 *
 */
public abstract class FeatureListProviderAggregate extends FeatureListProvider<FeatureObjMaskPairMergedParams> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FeatureListProvider<FeatureObjMaskParams> item;
	
	@BeanField
	private String prependString;
	// END BEAN PROPERTIES
	
	@Override
	public FeatureList<FeatureObjMaskPairMergedParams> create() throws CreateException {

		FeatureList<FeatureObjMaskParams> in = item.create();
		FeatureList<FeatureObjMaskPairMergedParams> out = new FeatureList<>(); 
		
		for( Feature<FeatureObjMaskParams> featExst : in ) {
			
			FromFirst featFirst = new FromFirst();
			featFirst.setItem( featExst.duplicateBean() );
			
			FromSecond featSecond = new FromSecond();
			featSecond.setItem( featExst.duplicateBean() );
			
			FromMerged featMerged = new FromMerged();
			featMerged.setItem( featExst.duplicateBean() );
			
			Feature<FeatureObjMaskPairMergedParams> featOut = createAggregateFeature(featFirst, featSecond, featMerged);
			
			FeatureListProviderPrependName.setNewNameOnFeature(featOut, featExst.getFriendlyName(), prependString);
			
			out.add( featOut );
		}
		
		return out;
	}
	
	protected abstract Feature<FeatureObjMaskPairMergedParams> createAggregateFeature(
		Feature<FeatureObjMaskPairMergedParams> featFirst,
		Feature<FeatureObjMaskPairMergedParams> featSecond,
		Feature<FeatureObjMaskPairMergedParams> featMerged
	);
	


	public String getPrependString() {
		return prependString;
	}

	public void setPrependString(String prependString) {
		this.prependString = prependString;
	}

	public FeatureListProvider<FeatureObjMaskParams> getItem() {
		return item;
	}

	public void setItem(FeatureListProvider<FeatureObjMaskParams> item) {
		this.item = item;
	}



}
