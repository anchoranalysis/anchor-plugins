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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.ChnlProviderOne;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.objectmask.ObjectCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;
import org.anchoranalysis.image.voxel.box.VoxelBox;

import ch.ethz.biol.cell.imageprocessing.objmask.matching.ObjMaskMatchUtilities;

// Matches source-objects to target objects, based upon intersection, and assigns the
//   value in the respective source object to the target object
public class ChnlProviderAssignFromIntersectingObjects extends ChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objsSource;
	
	@BeanField
	private ObjMaskProvider objsTarget;
	// END BEAN PROPERTIES
	
	@Override
	public Channel createFromChnl(Channel chnl) throws CreateException {
		
		VoxelBox<?> vb = chnl.getVoxelBox().any();
		
		ObjectCollection objsSrcCollection = objsSource.create();
		ObjectCollection objsTargetCollection = objsTarget.create();

		List<ObjWithMatches> matchList = matchIntersectingObjectsSingle( objsSrcCollection, objsTargetCollection );
		for( ObjWithMatches own : matchList ) {
			
			int level = getValForMask( chnl, own.getSourceObj() );
			assert(own.getMatches().size()==1);
			
			vb.setPixelsCheckMask( own.getMatches().get(0), level);
		}
		return chnl;
	}
	
	
	// Matches each object in objsSrc against objsTarget ensuring that it is a one-to-one mapping
	public static List<ObjWithMatches> matchIntersectingObjectsSingle( ObjectCollection objsSrc, ObjectCollection objsTarget ) {
		
		List<ObjWithMatches> matchList = ObjMaskMatchUtilities.matchIntersectingObjects( objsSrc, objsTarget );
		
		for( ObjWithMatches own : matchList ) {
			
			ObjectMask selectedObj = selectBestMatch( own.getSourceObj(), own.getMatches() );
			
			// We make sure the object only matches this item
			own.getMatches().clear();
			own.getMatches().add(selectedObj);
		}
		
		return matchList;
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

	private int getValForMask( Channel chnl, ObjectMask om ) {
		
		VoxelBox<?> vb = chnl.getVoxelBox().any();
		
		Point3i pnt = om.findAnyPntOnMask().orElseThrow( ()->
			new AnchorImpossibleSituationException()
		);
		int a = vb.getVoxel(pnt.getX(), pnt.getY(), pnt.getZ());
		return a;
	}
	
	public ObjMaskProvider getObjsSource() {
		return objsSource;
	}

	public void setObjsSource(ObjMaskProvider objsSource) {
		this.objsSource = objsSource;
	}

	public ObjMaskProvider getObjsTarget() {
		return objsTarget;
	}

	public void setObjsTarget(ObjMaskProvider objsTarget) {
		this.objsTarget = objsTarget;
	}
}
