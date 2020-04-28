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
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.FeatureInputPairObjs;
import ch.ethz.biol.cell.mpp.nrg.feature.objmaskpairmerged.FromFirst;
import ch.ethz.biol.cell.mpp.nrg.feature.objmaskpairmerged.FromMerged;
import ch.ethz.biol.cell.mpp.nrg.feature.objmaskpairmerged.FromSecond;

/**
 * Takes a list of features, and creates a new list of features, where each
 *   feature is embedded in a FromExisting feature and prepended with a prepend String
 * 
 * @author Owen Feehan
 * 
 * @param T feature-inout
 *
 */
public abstract class FeatureListProviderAggregate extends FeatureListProvider<FeatureInputPairObjs> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FeatureListProvider<FeatureInputSingleObj> item;
	
	@BeanField
	private String prependString;
	// END BEAN PROPERTIES
	
	@Override
	public FeatureList<FeatureInputPairObjs> create() throws CreateException {

		FeatureList<FeatureInputSingleObj> in = item.create();
		FeatureList<FeatureInputPairObjs> out = new FeatureList<>(); 
		
		for( Feature<FeatureInputSingleObj> featExst : in ) {
			
			FromFirst featFirst = new FromFirst();
			featFirst.setItem( featExst.duplicateBean() );
			
			FromSecond featSecond = new FromSecond();
			featSecond.setItem( featExst.duplicateBean() );
			
			FromMerged featMerged = new FromMerged();
			featMerged.setItem( featExst.duplicateBean() );
			
			Feature<FeatureInputPairObjs> featOut = createAggregateFeature(featFirst, featSecond, featMerged);
			
			FeatureListProviderPrependName.setNewNameOnFeature(featOut, featExst.getFriendlyName(), prependString);
			
			out.add( featOut );
		}
		
		return out;
	}
	
	protected abstract Feature<FeatureInputPairObjs> createAggregateFeature(
		Feature<FeatureInputPairObjs> featFirst,
		Feature<FeatureInputPairObjs> featSecond,
		Feature<FeatureInputPairObjs> featMerged
	);
	


	public String getPrependString() {
		return prependString;
	}

	public void setPrependString(String prependString) {
		this.prependString = prependString;
	}

	public FeatureListProvider<FeatureInputSingleObj> getItem() {
		return item;
	}

	public void setItem(FeatureListProvider<FeatureInputSingleObj> item) {
		this.item = item;
	}



}
