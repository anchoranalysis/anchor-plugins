package org.anchoranalysis.plugin.opencv.bean.feature;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.bean.list.FeatureList;
import org.anchoranalysis.feature.bean.list.FeatureListProvider;
import org.anchoranalysis.image.bean.size.SizeXY;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;

/**
 * Creates the entire HOG descriptor for an image.
 * 
 * <p>The user is required to specify a size to which each image is resized
 * so as to give a constant-sized feature-descriptor for each image (and
 * descriptors that are meaningful to compare across images).
 * 
 * @author Owen Feehan
 *
 */
public class HOGDescriptor extends FeatureListProvider<FeatureInputStack> {

	// START BEAN PROPERTIES
	/** The input-image is rescaled to this width/height before calculating HOG descriptors */
	@BeanField
	private SizeXY resizeTo;
	
	/** Parameters used for calculating HOG */
	@BeanField
	private HOGParameters params = new HOGParameters();
	// END BEAN PROPERTIES
	
	@Override
	public FeatureList<FeatureInputStack> create() throws CreateException {

		FeatureList<FeatureInputStack> out = new FeatureList<>();
		
		int sizeDescriptor = params.sizeDescriptor( resizeTo.asExtent() );
		for( int i=0; i<sizeDescriptor; i++) {
			out.add(
				featureFor(i)
			);
		}
		return out;
	}
	
	private HOGFeature featureFor(int index) {
		HOGFeature feature = new HOGFeature();
		feature.setResizeTo(resizeTo);
		feature.setParams(params);
		feature.setIndex(index);
		return feature;
	}

	public SizeXY getResizeTo() {
		return resizeTo;
	}

	public void setResizeTo(SizeXY resizeTo) {
		this.resizeTo = resizeTo;
	}
}
