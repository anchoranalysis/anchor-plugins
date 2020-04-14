package org.anchoranalysis.plugin.points.bean.fitter;

import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.points.fitter.InsufficientPointsException;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitter;
import org.anchoranalysis.anchor.mpp.bean.points.fitter.PointsFitterException;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.AnchorBean;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.image.bean.provider.ImageDimProvider;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

public class PointsFitterToMark extends AnchorBean<PointsFitterToMark> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	@BeanField
	private PointsFitter pointsFitter;
	
	@BeanField
	private ImageDimProvider dimProvider;
	
	/** If an object has fewer points than before being fitted, we ignore */
	@BeanField
	private int minNumPnts = 0;
	
	@BeanField
	private ObjMaskProvider objs;
	// END BEAN PROPERTIES
	
	public void fitPointsToMark( List<Point3f> pntsForFitter, Mark mark, ImageDim dim) throws OperationFailedException {
		try {
			pointsFitter.fit( pntsForFitter, mark, dim );
		} catch (PointsFitterException | InsufficientPointsException e) {
			throw new OperationFailedException(e);
		}
	}
	
	public ObjMaskCollection createObjs() throws CreateException {
		return objs.create();
	}
	
	public ImageDim createDim() throws CreateException {
		return dimProvider.create();
	}
		
	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}

	public int getMinNumPnts() {
		return minNumPnts;
	}

	public void setMinNumPnts(int minNumPnts) {
		this.minNumPnts = minNumPnts;
	}
	
	public PointsFitter getPointsFitter() {
		return pointsFitter;
	}

	public void setPointsFitter(PointsFitter pointsFitter) {
		this.pointsFitter = pointsFitter;
	}

	public ImageDimProvider getDimProvider() {
		return dimProvider;
	}


	public void setDimProvider(ImageDimProvider dimProvider) {
		this.dimProvider = dimProvider;
	}
}
