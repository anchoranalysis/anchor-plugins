package org.anchoranalysis.plugin.mpp.experiment.bean.feature.flexi;

/*-
 * #%L
 * anchor-plugin-mpp-experiment
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.anchoranalysis.bean.NamedBean;
import org.anchoranalysis.bean.StringSet;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.error.BeanDuplicateException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.graph.EdgeTypeWithVertices;
import org.anchoranalysis.core.graph.GraphWithEdgeTypes;
import org.anchoranalysis.core.log.LogErrorReporter;
import org.anchoranalysis.feature.bean.Feature;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.feature.calc.params.FeatureCalcParams;
import org.anchoranalysis.feature.list.NamedFeatureStore;
import org.anchoranalysis.feature.list.NamedFeatureStoreFactory;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.feature.objmask.pair.merged.FeatureObjMaskPairMergedParams;
import org.anchoranalysis.image.feature.stack.FeatureStackParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.voxel.nghb.CreateNghbGraph;
import org.anchoranalysis.plugin.mpp.experiment.feature.FeatureSessionFlexiFeatureTable;
import org.anchoranalysis.plugin.mpp.experiment.feature.FeatureSessionMergedPairs;

/**
 * Creates a set of features, that creates pairs of neighbouring-objects and applies a mixture of single-object features
 *    and pair features. 
 * 
 * Specifically:
 * 1. Creates a graph of neighbouring-objects
 * 2. Passes each pair of immediately-neighbouring as params, together with their merged object
 * 
 * Features are formed by duplicating the input-feature list (inputfeatures, single-object features only):
 *   a) First.inputfeatures     applies the features to the first-object in the pair
 *   b) Second.inputfeatures    applies the features to the second-object in the pair
 *   c) Merged.inputfeatures    applies the features to the merged-object
 *
 * Features (that are not duplicated) are also possible:
 *   d) Image.					additional single-object features that don't depend on any individual-object, only the image
 *   e) Pair.					additional pair-features (FeatureObjMaskParamsPair)
 *   
 *   The column order output is:  Image, First, Second, Pair, Merged.
 * 
 *  For First and Second, we use a cache, to avoid repeated feature values
 *    
 *  TODO This latter caching-step, could also be avoided in @see ch.ethz.biol.cell.countchrom.experiment.ExportFeaturesObjMaskTask , due to knowledge of the topology of the repeated features in the resulting output
 *    but for now, it's done by putting a cache on each feature.
 * 
 * @author Owen Feehan
 *
 */
public class MergedPairs extends FlexiFeatureTable<FeatureObjMaskPairMergedParams> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/**
	 * Additional features that are processed on the pair of images (i.e. First+Second as a pair)
	 */
	@BeanField
	private List<NamedBean<FeatureListProvider<FeatureObjMaskPairMergedParams>>> listFeaturesPair = new ArrayList<>();
	
	/**
	 * Additional features that only depend on the image, so do not need to be replicated for every object.
	 */
	@BeanField
	private List<NamedBean<FeatureListProvider<FeatureStackParams>>> listFeaturesImage = new ArrayList<>();
	
	/**
	 * Include features for the First-object of the pair
	 */
	@BeanField
	private boolean includeFirst = true;

	/**
	 * Include features for the Second-object of the pair
	 */
	@BeanField
	private boolean includeSecond = true;
	
	/**
	 * Include features for the Merged-object of the pair
	 */
	@BeanField
	private boolean includeMerged = true;
	
	/**
	 * If true, no overlapping objects are treated as pairs
	 */
	@BeanField
	private boolean avoidOverlappingObjects = false;
	
	/**
	 * Strings that are removed from the beginning of features.
	 */
	@BeanField
	private StringSet ignoreFeaturePrefixes = new StringSet();
	
	/**
	 * Checks if the Pairwise-inverse features are the same in both directions
	 * 
	 * This slows down calculations, but can provide a useful sanity check (e.g. where all features
	 * don't depend on the order of {@link org.anchoranalysis.image.feature.bean.objmask.pair.FeatureObjMaskPair}
	 */
	@BeanField
	private boolean checkInverse = false;
	
	@BeanField
	private boolean suppressErrors = false;
	
	@BeanField
	private boolean do3D = true;
	// END BEAN PROPERTIES
	
	@Override
	public FeatureSessionFlexiFeatureTable<FeatureObjMaskPairMergedParams> createFeatures(
			List<NamedBean<FeatureListProvider<FeatureObjMaskParams>>> list
	) throws CreateException {
		
		try {
			NamedFeatureStore<FeatureObjMaskParams> featuresSingle = NamedFeatureStoreFactory.createNamedFeatureList(list);
			FeatureList<FeatureObjMaskParams> listSingle = copyFeaturesCreateCustomName(featuresSingle);
			
			// Image features
			NamedFeatureStore<FeatureStackParams> featuresImage = NamedFeatureStoreFactory.createNamedFeatureList(listFeaturesImage);
			FeatureList<FeatureStackParams> listImage = copyFeaturesCreateCustomName(featuresImage );
									
			// Pair features
			NamedFeatureStore<FeatureObjMaskPairMergedParams> featuresPair = NamedFeatureStoreFactory.createNamedFeatureList(listFeaturesPair);
			FeatureList<FeatureObjMaskPairMergedParams> listPair = copyFeaturesCreateCustomName( featuresPair );
						
			return new FeatureSessionMergedPairs(
				includeFirst,
				includeSecond,
				listImage,
				listSingle,
				listPair,
				ignoreFeaturePrefixes.set(),
				checkInverse,
				suppressErrors
			);
			
		} catch (BeanDuplicateException | OperationFailedException e) {
			throw new CreateException(e);
		}
	}


	
	@Override
	public List<FeatureObjMaskPairMergedParams> createListCalcParams(ObjMaskCollection objs,
			NRGStackWithParams nrgStack, LogErrorReporter logErrorReporter) throws CreateException {

		List<FeatureObjMaskPairMergedParams> out = new ArrayList<>();
		
		// We create a neighbour-graph of our input objects
		CreateNghbGraph<ObjMask> graphCreator = new CreateNghbGraph<ObjMask>( avoidOverlappingObjects );
		GraphWithEdgeTypes<ObjMask,Integer> graphNghb = graphCreator.createGraphWithNumPixels(
			objs.asList(),
			v -> v,
			nrgStack.getNrgStack().getDimensions().getExtnt(),
			do3D
		);
		
		// We iterate through every edge in the graph, edges can exist in both directions
		Collection<EdgeTypeWithVertices<ObjMask,Integer>> edges = graphNghb.edgeSetUnique();
		for( EdgeTypeWithVertices<ObjMask,Integer> e : edges ) {
			
			FeatureObjMaskPairMergedParams params = new FeatureObjMaskPairMergedParams( e.getNode1(), e.getNode2() );
			params.setNrgStack(nrgStack);
			out.add(params);
		}
		
		return out;
	}

	
	/**
	 * Takes some features, and puts a copy in featuresOut, but updating their name with a prefix.  Inits with PAIR params
	 * 
	 * @param featuresIn		Where to read features in
	 * @param prefix			Prefix for feature-names
	 * @param featuresOut		Where to add the new features to
	 * @param proxyCreator		Instantiates an empty proxy-feature
	 * @param paramsInit		Instantiation parameters 
	 */
	private static <T extends FeatureCalcParams> FeatureList<T> copyFeaturesCreateCustomName( NamedFeatureStore<T> features ) throws OperationFailedException {
		
		FeatureList<T> out = new FeatureList<>();
		try {
			// First features
			for( int i=0; i<features.size(); i++ ) {
				NamedBean<Feature<T>> ni = features.get(i);
				
				Feature<T> featureAdd = ni.getValue().duplicateBean();
				featureAdd.setCustomName( ni.getName() );					
				out.add(featureAdd);
			}
		} catch (BeanDuplicateException e) {
			throw new OperationFailedException(e);
		}
		return out;
	}
	
	public boolean isIncludeFirst() {
		return includeFirst;
	}

	public void setIncludeFirst(boolean includeFirst) {
		this.includeFirst = includeFirst;
	}

	public boolean isIncludeSecond() {
		return includeSecond;
	}

	public void setIncludeSecond(boolean includeSecond) {
		this.includeSecond = includeSecond;
	}

	public boolean isIncludeMerged() {
		return includeMerged;
	}

	public void setIncludeMerged(boolean includeMerged) {
		this.includeMerged = includeMerged;
	}


	public List<NamedBean<FeatureListProvider<FeatureObjMaskPairMergedParams>>> getListFeaturesPair() {
		return listFeaturesPair;
	}


	public void setListFeaturesPair(
			List<NamedBean<FeatureListProvider<FeatureObjMaskPairMergedParams>>> listFeaturesPair) {
		this.listFeaturesPair = listFeaturesPair;
	}


	public List<NamedBean<FeatureListProvider<FeatureStackParams>>> getListFeaturesImage() {
		return listFeaturesImage;
	}


	public void setListFeaturesImage(
			List<NamedBean<FeatureListProvider<FeatureStackParams>>> listFeaturesImage) {
		this.listFeaturesImage = listFeaturesImage;
	}


	public boolean isAvoidOverlappingObjects() {
		return avoidOverlappingObjects;
	}


	public void setAvoidOverlappingObjects(boolean avoidOverlappingObjects) {
		this.avoidOverlappingObjects = avoidOverlappingObjects;
	}


	public boolean isCheckInverse() {
		return checkInverse;
	}


	public void setCheckInverse(boolean checkInverse) {
		this.checkInverse = checkInverse;
	}


	public boolean isSuppressErrors() {
		return suppressErrors;
	}


	public void setSuppressErrors(boolean suppressErrors) {
		this.suppressErrors = suppressErrors;
	}


	public StringSet getIgnoreFeaturePrefixes() {
		return ignoreFeaturePrefixes;
	}


	public void setIgnoreFeaturePrefixes(StringSet ignoreFeaturePrefixes) {
		this.ignoreFeaturePrefixes = ignoreFeaturePrefixes;
	}


	public boolean isDo3D() {
		return do3D;
	}


	public void setDo3D(boolean do3d) {
		do3D = do3d;
	}




}
