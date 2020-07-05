package org.anchoranalysis.plugin.image.feature.bean.list.permute;

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
import org.anchoranalysis.feature.bean.list.FeatureListFactory;
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
public class PermuteFeature<S, T extends FeatureInput> extends PermuteFeatureBase<T> {

	/**
	 * 
	 */
	
	// START BEAN PROPERTIES
	@BeanField @OptionalBean
	private StringSet referencesFeatureListCreator;	// Makes sure a particular feature list creator is evaluated
	
	@BeanField @NonEmpty
	private List<PermuteProperty<S>> listPermuteProperty = new ArrayList<PermuteProperty<S>>();
	// END BEAN PROPERTIES
		
	@Override
	protected FeatureList<T> createPermutedFeaturesFor( Feature<T> feature ) throws CreateException {
		
		// Create many copies of 'item' with properties adjusted
		List<Feature<T>> list = createInitialList(feature).asList();
		for( PermuteProperty<S> pp : listPermuteProperty ) {
			
			try {
				PermutationSetter permutationSetter = pp.createSetter(feature);
				
				list = new ApplyPermutations<Feature<T>>(
					Feature::getCustomName,
					(feat,name) -> feat.setCustomName(name)
				).applyPermutationsToCreateDuplicates(
					list,
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
				
		return FeatureListFactory.wrapReuse(list);
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
			return FeatureListFactory.from(
				duplicateAndRemoveName(feature)
			);
		} catch (BeanDuplicateException e) {
			throw new CreateException(e);
		}
	}
	
	private Feature<T> duplicateAndRemoveName( Feature<T> feature ) {
		// We add our item to fl as the 'input' item, knowing there's at least one permutation
		// The named doesn't matter, as will be replaced by next permutation
		return feature.duplicateChangeName(""); 
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
}
