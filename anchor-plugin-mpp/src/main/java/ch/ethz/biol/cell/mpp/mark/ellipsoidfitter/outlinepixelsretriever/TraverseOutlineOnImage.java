package ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever;

/*-
 * #%L
 * anchor-plugin-mpp
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

import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.Tuple3i;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.outline.traverser.OutlineTraverser;

import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler.VisitScheduler;
import ch.ethz.biol.cell.mpp.mark.ellipsoidfitter.outlinepixelsretriever.visitscheduler.VisitSchedulerMaxDist;

// Traverses a pixel location
public class TraverseOutlineOnImage extends OutlinePixelsRetriever {

	// START BEAN
	@BeanField
	private VisitScheduler visitScheduler;
	
	@BeanField
	private boolean nghb8 = true;
	
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProviderOutline;
	
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProviderFilled;
	
	@BeanField
	private boolean useZ = true;
	// END BEAN
	
	private ObjMask omFilled;		// Not-changed as traversal occurs
	private ObjMask omOutline;		// Changed as traversal occurs (visited pixels are removed)
	
	public TraverseOutlineOnImage() {
		super();
	}
	
	@Override
	public void traverse( Point3i root, List<Point3i> listOut, RandomNumberGenerator re ) throws TraverseOutlineException {
		
		BinaryChnl chnlOutline;

		try {
			chnlOutline = binaryImgChnlProviderOutline.create();
		} catch (CreateException e) {
			throw new TraverseOutlineException("Unable to create binaryImgChnlProviderOutline", e);
		}

		
		BinaryChnl chnlFilled;
		try {
			chnlFilled = binaryImgChnlProviderFilled.create(); 
		} catch (CreateException e) {
			throw new TraverseOutlineException("Unable to create binaryImgChnlProviderFilled", e);
		}
		
		if (!chnlOutline.getDimensions().equals(chnlFilled.getDimensions())) {
			throw new TraverseOutlineException( String.format("Dimensions %s and %s are not equal", chnlOutline.getDimensions(), chnlFilled.getDimensions()) );
		}
		
		useZ = useZ && (chnlOutline.getDimensions().getZ() > 1);
		
		try {
			visitScheduler.beforeCreateObjMask(re, chnlOutline.getDimensions().getRes());
		} catch (InitException e1) {
			throw new TraverseOutlineException("Failure to call beforeCreateObjMask on visitScheduler", e1);
		}
		
		try {
			omOutline = createObjMaskForPoint(root, chnlOutline );
		} catch (CreateException e) {
			throw new TraverseOutlineException("Unable to create an object-mask for the outline", e);
		}
		
		try {
			omFilled = createObjMaskForPoint(root, chnlFilled );
		} catch (CreateException e) {
			throw new TraverseOutlineException("Unable to create an object-mask for the filled object", e);
		}
		
		// Important, so we can use the contains function later
		Point3i crnrMin = omFilled.getBoundingBox().getCrnrMin();
		crnrMin.setX(0);
		crnrMin.setY(0);
		crnrMin.setZ(0);
		
		Point3i rootRelToMask = BoundingBox.relPosTo(root, omOutline.getBoundingBox().getCrnrMin());
		try {
			visitScheduler.afterCreateObjMask( rootRelToMask, chnlOutline.getDimensions().getRes(), re );
		} catch (InitException e) {
			throw new TraverseOutlineException("Cannot call afterCreateObjMask on visitScheduler", e);
		}
		
		try {
			new OutlineTraverser(
				omOutline,
				(pnt,dist) -> visitScheduler.considerVisit(pnt,dist,omFilled),
				useZ,
				nghb8
			).applyGlobal(
				root,
				listOut
			);
		} catch (OperationFailedException e) {
			throw new TraverseOutlineException("Cannot traverse outline", e);
		}
	}
	
	private ObjMask createObjMaskForPoint( Point3i root, BinaryChnl chnl ) throws CreateException {
				
		try {
			Tuple3i maxDist = visitScheduler.maxDistFromRootPoint(chnl.getDimensions().getRes()) ;
			if (maxDist==null) {
				throw new CreateException("A null maxDist is not supported");
			}
			BoundingBox box = VisitSchedulerMaxDist.createBoxAroundPoint(root, maxDist );
			
			// We make sure the box is within our scene boundaries
			box.clipTo( chnl.getDimensions().getExtnt() );
			
			// This is our final intersection box, that we use for traversing and memorizing pixels
			//  that we have already visited
			
			assert( box.extnt().getVolume() > 0 );
			
			return chnl.createMaskAlwaysNew(box);
			
		} catch (OperationFailedException e) {
			throw new CreateException(e);
		}
	}

	public boolean isNghb8() {
		return nghb8;
	}

	public void setNghb8(boolean nghb8) {
		this.nghb8 = nghb8;
	}

	public VisitScheduler getVisitScheduler() {
		return visitScheduler;
	}

	public void setVisitScheduler(VisitScheduler visitScheduler) {
		this.visitScheduler = visitScheduler;
	}

	public boolean isUseZ() {
		return useZ;
	}

	public void setUseZ(boolean useZ) {
		this.useZ = useZ;
	}

	public BinaryImgChnlProvider getBinaryImgChnlProviderOutline() {
		return binaryImgChnlProviderOutline;
	}

	public void setBinaryImgChnlProviderOutline(
			BinaryImgChnlProvider binaryImgChnlProviderOutline) {
		this.binaryImgChnlProviderOutline = binaryImgChnlProviderOutline;
	}

	public BinaryImgChnlProvider getBinaryImgChnlProviderFilled() {
		return binaryImgChnlProviderFilled;
	}

	public void setBinaryImgChnlProviderFilled(
			BinaryImgChnlProvider binaryImgChnlProviderFilled) {
		this.binaryImgChnlProviderFilled = binaryImgChnlProviderFilled;
	}
}
