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
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.bean.annotation.SkipInit;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.bean.permute.ApplyPermutations;
import org.anchoranalysis.bean.permute.property.PermuteProperty;
import org.anchoranalysis.bean.permute.setter.PermutationSetter;
import org.anchoranalysis.bean.permute.setter.PermutationSetterException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.name.provider.NamedProviderGetException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.shared.SharedFeaturesInitParams;

/**
 * Permutes one or more properties of a Feature, so as to create a set of Features
 * 
 * @author Owen Feehan
 *
 * @param S permutation type
 * @param T feature-input
 */
public class FeatureListProviderPermute<S, T extends FeatureInput> extends FeatureListProvider<T> {

	/**
	 * 
	 */
	
	// START BEAN PROPERTIES
	@BeanField @SkipInit
	private Feature<T> feature;
	
	@BeanField @OptionalBean
	private StringSet referencesFeatureListCreator;	// Makes sure a particular feature list creator is evaluated
	
	@BeanField @NonEmpty
	private List<PermuteProperty<S>> listPermuteProperty = new ArrayList<PermuteProperty<S>>();
	// END BEAN PROPERTIES
	
	@Override
	public FeatureList<T> create() throws CreateException {
				
		FeatureList<T> flInput = createInitialList(feature);
		
		// Create many copies of 'item' with properties adjusted
		List<Feature<T>> fl = flInput;
		for( PermuteProperty<S> pp : listPermuteProperty ) {
			
			try {
				PermutationSetter permutationSetter = pp.createSetter(feature);
				
				fl = new ApplyPermutations<Feature<T>>(
					(a) -> a.getCustomName(),
					(a,s) -> a.setCustomName(s)
					).applyPermutationsToCreateDuplicates(
					fl,
					pp,
					permutationSetter
				);
			} catch (PermutationSetterException e) {
				throw new CreateException(
					String.format("Cannot create a permutation-setter for %s",pp),
					e
				);
			}
		}
				
		return new FeatureList<>(fl);
	}

	@Override
	public void onInit(SharedFeaturesInitParams so)
			throws InitException {
		super.onInit(so);
		if (referencesFeatureListCreator!=null && so!=null) {
			for( String s : referencesFeatureListCreator.set() ) {
				try {
					getSharedObjects().getFeatureListSet().getException(s);
					
				} catch (NamedProviderGetException e) {
					throw new InitException(e.summarize());
				}
			}
		}
	}

	private FeatureList<T> createInitialList( Feature<T> feature ) throws CreateException {
		try {
			FeatureList<T> flInput = new FeatureList<>();
			
			// We add our item to fl as the 'input' item, knowing there's at least one permutation
			Feature<T> itemDup = feature.duplicateBean();
			itemDup.setCustomName("");  // Doesn't matter, as will be replaced by next permutation
			flInput.add(itemDup);
			
			return flInput;
			
		} catch (BeanDuplicateException e) {
			throw new CreateException(e);
		}
	}
	
	public StringSet getReferencesFeatureListCreator() {
		return referencesFeatureListCreator;
	}

	public void setReferencesFeatureListCreator(
			StringSet referencesFeatureListCreator) {
		this.referencesFeatureListCreator = referencesFeatureListCreator;
	}

	public List<PermuteProperty<S>> getListPermuteProperty() {
		return listPermuteProperty;
	}

	public void setListPermuteProperty(List<PermuteProperty<S>> listPermuteProperty) {
		this.listPermuteProperty = listPermuteProperty;
	}


	public Feature<T> getFeature() {
		return feature;
	}


	public void setFeature(Feature<T> feature) {
		this.feature = feature;
	}

}
