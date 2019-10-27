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
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.match.ObjWithMatches;
import org.anchoranalysis.image.voxel.box.VoxelBox;

import ch.ethz.biol.cell.imageprocessing.objmask.matching.ObjMaskMatchUtilities;

// Matches source-objects to target objects, based upon intersection, and assigns the
//   value in the respective source object to the target object
public class ChnlProviderAssignFromIntersectingObjects extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private ObjMaskProvider objMaskProviderSource;
	
	@BeanField
	private ObjMaskProvider objMaskProviderTarget;
	// END BEAN PROPERTIES
	
	private int getValForMask( Chnl chnl, ObjMask om ) {
		
		VoxelBox<?> vb = chnl.getVoxelBox().any();
		
		Point3i pnt = om.findAnyPntOnMask();
		assert(pnt!=null);
		int a = vb.getVoxel(pnt.getX(), pnt.getY(), pnt.getZ());
		return a;
	}
	
	@Override
	public Chnl create() throws CreateException {

		Chnl chnl = chnlProvider.create();
		
		VoxelBox<?> vb = chnl.getVoxelBox().any();
		
		ObjMaskCollection objsSrc = objMaskProviderSource.create();
		ObjMaskCollection objsTarget = objMaskProviderTarget.create();

		List<ObjWithMatches> matchList = matchIntersectingObjectsSingle( objsSrc, objsTarget );
		for( ObjWithMatches own : matchList ) {
			
			int level = getValForMask( chnl, own.getSourceObj() );
			
//			if (own.getMatches().size()==0) {
//				getLogErrorReporter().getLogReporter().log("Cannot find a match for an src-object");
//				continue;
//			}
			assert(own.getMatches().size()==1);
			
			vb.setPixelsCheckMask( own.getMatches().get(0), level);
		}
		
		return chnl;
	}
	
	
	// Matches each object in objsSrc against objsTarget ensuring that it is a one-to-one mapping
	public static List<ObjWithMatches> matchIntersectingObjectsSingle( ObjMaskCollection objsSrc, ObjMaskCollection objsTarget ) {
		
		List<ObjWithMatches> matchList = ObjMaskMatchUtilities.matchIntersectingObjects( objsSrc, objsTarget );
		
		for( ObjWithMatches own : matchList ) {
			
			//System.out.printf("Number of matches=%d\n", own.getMatches().size() );
			
			ObjMask selectedObj = selectBestMatch( own.getSourceObj(), own.getMatches() );
			
			// We make sure the object only matches this item
			own.getMatches().clear();
			own.getMatches().add(selectedObj);
		}
		
		return matchList;
	}
	
	private static ObjMask selectBestMatch( ObjMask source, ObjMaskCollection matches ) {
		assert(matches.size()>0);
		
		if (matches.size()==1) {
			return matches.get(0);
		}
		
		int maxIntersection = -1;
		ObjMask omMostIntersecting = null;
		for( ObjMask om : matches) {
			int intersectingPixels = source.countIntersectingPixels(om);
			if (intersectingPixels > maxIntersection) {
				omMostIntersecting = om;
				maxIntersection = intersectingPixels;
			}
		}
		return omMostIntersecting;
	}
	
	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public ObjMaskProvider getObjMaskProviderSource() {
		return objMaskProviderSource;
	}

	public void setObjMaskProviderSource(ObjMaskProvider objMaskProviderSource) {
		this.objMaskProviderSource = objMaskProviderSource;
	}

	public ObjMaskProvider getObjMaskProviderTarget() {
		return objMaskProviderTarget;
	}

	public void setObjMaskProviderTarget(ObjMaskProvider objMaskProviderTarget) {
		this.objMaskProviderTarget = objMaskProviderTarget;
	}

	
	
}
