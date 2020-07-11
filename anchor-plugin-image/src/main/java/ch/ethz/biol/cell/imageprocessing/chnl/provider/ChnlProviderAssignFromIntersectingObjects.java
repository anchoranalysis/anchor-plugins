package ch.ethz.biol.cell.imageprocessing.chnl.provider;

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
import java.util.stream.Stream;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.provider.ObjectCollectionProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.MatchedObject;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.apache.commons.math3.util.Pair;

import ch.ethz.biol.cell.imageprocessing.objmask.matching.ObjMaskMatchUtilities;

// Matches source-objects to target objects, based upon intersection, and assigns the
//   value in the respective source object to the target object
public class ChnlProviderAssignFromIntersectingObjects extends ChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private ObjectCollectionProvider objsSource;
	
	@BeanField
	private ObjectCollectionProvider objsTarget;
	// END BEAN PROPERTIES
	
	@Override
	public Channel createFromChnl(Channel chnl) throws CreateException {
		
		VoxelBox<?> vb = chnl.getVoxelBox().any();
		
		ObjectCollection source = objsSource.create();
		ObjectCollection target = objsTarget.create();

		streamIntersectingObjects(source, target).forEach(pair->
			vb.setPixelsCheckMask(
				pair.getSecond(),
				getValForMask( chnl, pair.getFirst() )
			)
		);
		return chnl;
	}
		
	/**
	 * Matches each object in objsSrc against objsTarget ensuring that it is a one-to-one mapping
	 * 
	 * @param source
	 * @param target
	 * @return a pair with source object (left) and the matched object (right)
	 */
	private static Stream<Pair<ObjectMask,ObjectMask>> streamIntersectingObjects(ObjectCollection source, ObjectCollection target) {
		
		List<MatchedObject> matchList = ObjMaskMatchUtilities.matchIntersectingObjects(source, target);
				
		return matchList.stream().map( owm->
			new Pair<>(
				owm.getSource(),
				selectBestMatch( owm.getSource(), owm.getMatches() )
			)
		);
	}
	
	private static ObjectMask selectBestMatch( ObjectMask source, ObjectCollection matches ) {
		assert(matches.size()>0);
		
		if (matches.size()==1) {
			return matches.get(0);
		}
		
		int maxIntersection = -1;
		ObjectMask omMostIntersecting = null;
		for( ObjectMask om : matches) {
			int intersectingPixels = source.countIntersectingPixels(om);
			if (intersectingPixels > maxIntersection) {
				omMostIntersecting = om;
				maxIntersection = intersectingPixels;
			}
		}
		return omMostIntersecting;
	}

	private static int getValForMask( Channel chnl, ObjectMask om ) {
		
		VoxelBox<?> vb = chnl.getVoxelBox().any();
 
		return vb.getVoxel(
			om.findArbitraryOnVoxel().orElseThrow(AnchorImpossibleSituationException::new)
		);
	}
	
	public ObjectCollectionProvider getObjsSource() {
		return objsSource;
	}

	public void setObjsSource(ObjectCollectionProvider objsSource) {
		this.objsSource = objsSource;
	}

	public ObjectCollectionProvider getObjsTarget() {
		return objsTarget;
	}

	public void setObjsTarget(ObjectCollectionProvider objsTarget) {
		this.objsTarget = objsTarget;
	}
}
