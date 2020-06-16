package ch.ethz.biol.cell.imageprocessing.objmask.filter;

/*
 * #%L
 * anchor-plugin-image
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


import java.util.List;
import java.util.Optional;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.bean.objmask.filter.ObjMaskFilter;
import org.anchoranalysis.image.bean.objmask.match.ObjMaskMatcher;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectMaskCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;
import org.anchoranalysis.plugin.image.intensity.IntensityMeanCalculator;

// Insists that the ratio of holes in this object is less than a certain %
// Holes are provided by a series of 'filled in' objects, which are considered
//  as joined if they have an intersecting pixel
public class ObjMaskFilterHasSpheroidSmallInner extends ObjMaskFilter {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskMatcher objMaskMatcherForContainedObjects;
	
	@BeanField
	private ChnlProvider chnlIntensity;
	
	@BeanField
	private ChnlProvider chnlDistance;
	
	@BeanField
	private double minIntensityDifference = 0;
	
	@BeanField
	private double minSizeRatio = 0.0;
	
	@BeanField
	private double maxSizeRatio = 1.0;
	
	@BeanField
	private int minDistance = 0;
	// END BEAN PROPERTIES
	
	@Override
	public void filter(ObjectMaskCollection objs, Optional<ImageDim> dim, Optional<ObjectMaskCollection> objsRejected)
			throws OperationFailedException {
		
		List<ObjWithMatches> matchList = objMaskMatcherForContainedObjects.findMatch(objs);
		
		Channel intensity;
		try {
			intensity = chnlIntensity.create();
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
		
		
		Channel distance;
		try {
			distance = chnlDistance.create();
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
		
		
		for( ObjWithMatches owm : matchList ) {
			if (!includeObj(owm, intensity, distance)) {
				objs.remove(owm.getSourceObj());
				
				if (objsRejected.isPresent()) {
					objsRejected.get().add( owm.getSourceObj() );
				}
			}
		}
	}
	
	private boolean isSmallInner( ObjectMask om, ObjectMask omContainer, Channel chnlIntensity, Channel chnlDistance ) throws OperationFailedException {
		
		ObjectMask omInverse = omContainer.duplicate();
		try {
			omInverse.invertContainedMask(om);
		} catch (OperationFailedException e) {
			throw new OperationFailedException(e);
		}
		


		
		// Intensity check
		try {
			// Calculate intensity of object
			double intensitySmall = IntensityMeanCalculator.calcMeanIntensityObjMask(chnlIntensity, om);
			double intensityContainer = IntensityMeanCalculator.calcMeanIntensityObjMask(chnlIntensity, omInverse);
			
			double intensityDiff = intensitySmall - intensityContainer;
			
			//System.out.printf("Intensity:  Obj=%f  Container=%f  Diff=%f  MinIntensityDifference=%f\n", intensitySmall, intensityContainer, intensityDiff, minIntensityDifference);
			
			if (intensityDiff < minIntensityDifference) {
				return false;
			}
		} catch (FeatureCalcException e) {
			throw new OperationFailedException(e);
		}
		
		// Size check
		if (!sizeCheck(om, omContainer)) {
			return false;	
		}
		
		if (!distanceCheck(chnlDistance, om, omInverse)) {
			return false;
		}
		
		return true;
	}
	
	private boolean sizeCheck(ObjectMask om, ObjectMask omContainer) throws OperationFailedException {
		int sizeSmall = om.numPixels();
		int sizeContainer = omContainer.numPixels();
		double sizeRatio = ((double) sizeSmall) / sizeContainer;
		
		if (sizeRatio < minSizeRatio) {
			return false;
		}
		
		if (sizeRatio > maxSizeRatio) {
			return false;
		}
		return true;
	}
	
	private boolean distanceCheck(Channel chnlDistance, ObjectMask om, ObjectMask omInverse) throws OperationFailedException {
		// Distance check
		try{
			// Calculate intensity of object
			double distanceObj = IntensityMeanCalculator.calcMeanIntensityObjMask(chnlDistance, om);
			double distanceInverse= IntensityMeanCalculator.calcMeanIntensityObjMask(chnlDistance, omInverse)  + minDistance;
			
			if (distanceObj < distanceInverse) {
				return false;
			}
			return true;
			
		} catch (FeatureCalcException e) {
			throw new OperationFailedException(e);
		}
	}
	
	private ObjectMask findMaxVolumeObjAndCnt( ObjectMaskCollection objs ) {
		
		ObjectMask max = null;
		int maxVolume = 0;
		
		for( ObjectMask om : objs ) {
			int vol = om.numPixels();
			
			if (max==null || vol>maxVolume ) {
				max = om;
				maxVolume = vol;
			}
		}
		return max;
	}
	
	private boolean includeObj( ObjWithMatches owm, Channel chnlIntensity, Channel chnlDistance ) throws OperationFailedException {
		
		if (owm.getMatches().size()==0) {
			return true;
		}
		
		// We only consider the largest of the segmented objects as a possibility
		ObjectMask biggestMatch = findMaxVolumeObjAndCnt( owm.getMatches() );
		
		if (isSmallInner(biggestMatch, owm.getSourceObj(), chnlIntensity, chnlDistance)) {
			return false;
		}

		return true;
	}
	
	public double getMinIntensityDifference() {
		return minIntensityDifference;
	}


	public void setMinIntensityDifference(double minIntensityDifference) {
		this.minIntensityDifference = minIntensityDifference;
	}

	public double getMinSizeRatio() {
		return minSizeRatio;
	}


	public void setMinSizeRatio(double minSizeRatio) {
		this.minSizeRatio = minSizeRatio;
	}


	public ObjMaskMatcher getObjMaskMatcherForContainedObjects() {
		return objMaskMatcherForContainedObjects;
	}


	public void setObjMaskMatcherForContainedObjects(
			ObjMaskMatcher objMaskMatcherForContainedObjects) {
		this.objMaskMatcherForContainedObjects = objMaskMatcherForContainedObjects;
	}


	public int getMinDistance() {
		return minDistance;
	}


	public void setMinDistance(int minDistance) {
		this.minDistance = minDistance;
	}


	public double getMaxSizeRatio() {
		return maxSizeRatio;
	}


	public void setMaxSizeRatio(double maxSizeRatio) {
		this.maxSizeRatio = maxSizeRatio;
	}

	public ChnlProvider getChnlIntensity() {
		return chnlIntensity;
	}

	public void setChnlIntensity(ChnlProvider chnlIntensity) {
		this.chnlIntensity = chnlIntensity;
	}

	public ChnlProvider getChnlDistance() {
		return chnlDistance;
	}

	public void setChnlDistance(ChnlProvider chnlDistance) {
		this.chnlDistance = chnlDistance;
	}
}
