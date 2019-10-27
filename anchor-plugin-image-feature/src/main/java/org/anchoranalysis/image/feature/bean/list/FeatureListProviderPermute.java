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


import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonEmpty;
import org.anchoranalysis.bean.annotation.Optional;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.bean.permute.ApplyPermutations;
import org.anchoranalysis.bean.permute.property.PermuteProperty;
import org.anchoranalysis.bean.permute.setter.PermutationSetter;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.index.GetOperationFailedException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;

/**
 * Permutes one or more properties of a Feature, so as to create a set of Features
 * 
 * @author Owen Feehan
 *
 * @param T permutation type
 */
public class FeatureListProviderPermute<T> extends FeatureListProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5595892123306950453L;
	/**
	 * 
	 */
	
	// START BEAN PROPERTIES
	@BeanField @SkipInit
	private Feature feature;
	
	@BeanField @Optional
	private StringSet referencesFeatureListCreator;	// Makes sure a particular feature list creator is evaluated
	
	@BeanField @NonEmpty
	private List<PermuteProperty<T>> listPermuteProperty = new ArrayList<PermuteProperty<T>>();
	// END BEAN PROPERTIES

	private static FeatureList createInitialList( Feature feature ) throws CreateException {
		try {
			FeatureList flInput = new FeatureList();
			
			// We add our item to fl as the 'input' item, knowing there's at least one permutation
			Feature itemDup = feature.duplicateBean();
			itemDup.setCustomName("");  // Doesn't matter, as will be replaced by next permutation
			flInput.add(itemDup);
			
			return flInput;
			
		} catch (BeanDuplicateException e) {
			throw new CreateException(e);
		}
	}
	
	@Override
	public FeatureList create() throws CreateException {
				
		FeatureList flInput = createInitialList( feature );
		
		// Create many copies of 'item' with properties adjusted
		List<Feature> fl = flInput;
		for( PermuteProperty<T> pp : listPermuteProperty ) {
			
			PermutationSetter permutationSetter = pp.createSetter(feature);
			
			fl = new ApplyPermutations<Feature>(
				(a) -> a.getCustomName(),
				(a,s) -> a.setCustomName(s)
				).applyPermutationsToCreateDuplicates(
				fl,
				pp,
				permutationSetter
			);
		}
				
		return new FeatureList(fl);
	}



	@Override
	public void onInit(SharedFeaturesInitParams so)
			throws InitException {
		super.onInit(so);
		if (referencesFeatureListCreator!=null && so!=null) {
			for( String s : referencesFeatureListCreator.set() ) {
				try {
					getSharedObjects().getFeatureListSet().getException(s);
				} catch (GetOperationFailedException e) {
					throw new InitException(e);
				}
			}
		}
	}

	public StringSet getReferencesFeatureListCreator() {
		return referencesFeatureListCreator;
	}

	public void setReferencesFeatureListCreator(
			StringSet referencesFeatureListCreator) {
		this.referencesFeatureListCreator = referencesFeatureListCreator;
	}

	public List<PermuteProperty<T>> getListPermuteProperty() {
		return listPermuteProperty;
	}

	public void setListPermuteProperty(List<PermuteProperty<T>> listPermuteProperty) {
		this.listPermuteProperty = listPermuteProperty;
	}


	public Feature getFeature() {
		return feature;
	}


	public void setFeature(Feature feature) {
		this.feature = feature;
	}

}
