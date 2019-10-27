package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

/*
 * #%L
 * anchor-plugin-points
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
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.outline.FindOutline;
import org.anchoranalysis.image.points.PointsFromBinaryChnl;
import org.anchoranalysis.image.voxel.box.VoxelBox;

import ch.ethz.biol.cell.mpp.mark.pointsfitter.ConvexHullUtilities;


// USES Gift wrap algorithm taken from FIJI PolygonRoi.java
public class BinaryImgChnlProviderConvexHull2D extends BinaryImgChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProvider;
	
	@BeanField
	private boolean erodeEdges = false;
	// END BEAN PROPERTIES
	
	@Override
	public BinaryChnl create() throws CreateException {
		
		BinaryChnl chnlIn = binaryImgChnlProvider.create();
		
		BinaryChnl outline = FindOutline.outline(
			chnlIn,
			false,
			erodeEdges
		);
		
		List<Point2i> extPnts = PointsFromBinaryChnl.pointsFromChnl2D(outline);
		
		List<Point2i> pntsConvexHull = ConvexHullUtilities.convexHull2D(extPnts);
		
		// we write the vertices to the outline
		Chnl out = outline.getChnl();
		VoxelBox<?> vbOut = out.getVoxelBox().any();
		
		vbOut.setAllPixelsTo(outline.getBinaryValues().getOffInt());
		for (Point2i pnt : pntsConvexHull) {
			vbOut.setVoxel( pnt.getX(), pnt.getY(), 0, outline.getBinaryValues().getOnInt());
	    }		
		   
		return outline;
	}

	public BinaryImgChnlProvider getBinaryImgChnlProvider() {
		return binaryImgChnlProvider;
	}

	public void setBinaryImgChnlProvider(BinaryImgChnlProvider binaryImgChnlProvider) {
		this.binaryImgChnlProvider = binaryImgChnlProvider;
	}

	public boolean isErodeEdges() {
		return erodeEdges;
	}

	public void setErodeEdges(boolean erodeEdges) {
		this.erodeEdges = erodeEdges;
	}

}
