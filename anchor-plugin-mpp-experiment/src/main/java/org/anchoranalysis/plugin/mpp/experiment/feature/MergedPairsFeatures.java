package org.anchoranalysis.plugin.mpp.experiment.feature;

import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.feature.objmask.pair.merged.FeatureInputPairObjsMerged;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;

/**
 * The list of features that can be used in a {@link MergedPairsSession}
 * 
 * @author Owen Feehan
 *
 */
public class MergedPairsFeatures {

	private FeatureList<FeatureInputStack> listImage;

	private FeatureList<FeatureInputSingleObj> listSingle;
	private FeatureList<FeatureInputPairObjsMerged> listPair;

	/**
	 * Constructor
	 * 
	 * @param listImage features for image as a whole
	 * @param listSingle features for single-objects
	 * @param listPair features for a pair of objects
	 */
	public MergedPairsFeatures(FeatureList<FeatureInputStack> listImage, FeatureList<FeatureInputSingleObj> listSingle,
			FeatureList<FeatureInputPairObjsMerged> listPair) {
		super();
		this.listImage = listImage;
		this.listSingle = listSingle;
		this.listPair = listPair;
	}	
	
	public FeatureList<FeatureInputStack> getImage() {
		return listImage;
	}
	public FeatureList<FeatureInputSingleObj> getSingle() {
		return listSingle;
	}
	public FeatureList<FeatureInputPairObjsMerged> getPair() {
		return listPair;
	}
	
	/** Immutably creates entirely new duplicated features */
	public MergedPairsFeatures duplicate() {
		return new MergedPairsFeatures(
			listImage.duplicateBean(),
			listSingle.duplicateBean(),
			listPair.duplicateBean()
		);
	}
	
	public int numImageFeatures() {
		return listImage.size();
	}
	
	public int numSingleFeatures() {
		return listSingle.size();
	}
	
	public int numPairFeatures() {
		return listPair.size();
	}
}
