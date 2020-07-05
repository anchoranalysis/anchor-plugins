package org.anchoranalysis.plugin.mpp.experiment.bean.feature;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import java.util.List;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.input.FeatureInput;
import org.anchoranalysis.feature.resultsvectorcollection.FeatureInputResults;
import org.anchoranalysis.image.feature.object.input.FeatureInputPairObjects;
import org.anchoranalysis.image.feature.object.input.FeatureInputSingleObject;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.test.TestLoader;

/** Loads features-lists of different types from particular XML files */
class ExportObjectsFeatureLoader {

	/** The "single" and "pair" and "image" features in use.*/
	private LoadFeatureListProviderFixture<FeatureInputSingleObject> single;
	private LoadFeatureListProviderFixture<FeatureInputPairObjects> pair;
	private LoadFeatureListProviderFixture<FeatureInputStack> image;
	
	/** The features used for the shared-feature set */
	private LoadFeatureListProviderFixture<FeatureInput> shared;
	
	/** The features used for aggregating results */
	private LoadFeatureListProviderFixture<FeatureInputResults> aggregated;
	
	public ExportObjectsFeatureLoader(TestLoader loader) {
		super();
		this.single = new LoadFeatureListProviderFixture<>(loader, "singleFeatures.xml");
		this.pair = new LoadFeatureListProviderFixture<>(loader, "pairFeatures.xml");
		this.image = new LoadFeatureListProviderFixture<>(loader, "imageFeatures.xml");
		this.shared = new LoadFeatureListProviderFixture<>(loader, "sharedFeatures.xml");
		this.aggregated = new LoadFeatureListProviderFixture<>(loader, "aggregatedFeatures.xml");
	}
	
	public List<NamedBean<FeatureListProvider<FeatureInputSingleObject>>> single() {
		return single.asListNamedBeansProvider();
	}
	
	public List<NamedBean<FeatureListProvider<FeatureInputPairObjects>>> pair() {
		return pair.asListNamedBeansProvider();
	}
	
	public List<NamedBean<FeatureListProvider<FeatureInputStack>>> image() {
		return image.asListNamedBeansProvider();
	}
	
	public List<NamedBean<FeatureListProvider<FeatureInput>>> shared() {
		return shared.asListNamedBeansProvider();
	}
	
	public List<NamedBean<FeatureListProvider<FeatureInputResults>>> aggregated() {
		return aggregated.asListNamedBeansProvider();
	}
	
	public void changeSingleToReferenceShared() {
		changeSingleTo("referenceWithShared.xml");
	}
	
	public void changeSingleToReferenceWithInclude() {
		changeSingleTo("referenceWithInclude.xml");
	}
	
	public void changeSingleToShellFeatures() {
		changeSingleTo("singleFeaturesWithShell.xml");
	}
	
	/** 
	 * Uses this feature instead of whatever list has been loaded for the single-features
	 * 
	 * <p>It does not initialize the feature.</p>
	 * */
	public void changeSingleTo( Feature<FeatureInputSingleObject> feature ) {
		single.useSingleFeature(feature);
	}

	/** 
	 * Uses this feature instead of whatever list has been loaded for the pair-features
	 * 
	 * <p>It does not initialize the feature.</p>
	 * */
	public void changePairTo( Feature<FeatureInputPairObjects> feature ) {
		pair.useSingleFeature(feature);
	}
	
	/** 
	 * Uses this feature instead of whatever list has been loaded for the pair-features
	 * 
	 * <p>It does not initialize the feature.</p>
	 * */
	public void changeImageTo( Feature<FeatureInputStack> feature ) {
		image.useSingleFeature(feature);
	}
	
	/** 
	 * Additionally include a shell feature in the "single" features
	 *  
	 * @throws CreateException */
	private void changeSingleTo(String alternativeFileName) {
		single.useAlternativeXMLList(alternativeFileName);
	}
}
