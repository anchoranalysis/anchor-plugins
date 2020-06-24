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
import java.util.Optional;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.geometry.Point2i;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.points.PointsFromBinaryChnl;
import org.anchoranalysis.image.voxel.box.VoxelBox;


// USES Gift wrap algorithm taken from FIJI PolygonRoi.java
public class BinaryChnlProviderConvexHull2D extends ConvexHullBase {

	@Override
	protected BinaryChnl createFromChnl(BinaryChnl chnlIn, BinaryChnl outline) throws CreateException {
		List<Point2i> extPnts = PointsFromBinaryChnl.pointsFromChnl2D(outline);
		
		Optional<List<Point2i>> pntsConvexHull = ConvexHullUtilities.convexHull2D(extPnts);
				
		// we write the vertices to the outline
		Channel out = outline.getChnl();
		VoxelBox<?> vbOut = out.getVoxelBox().any();
		
		vbOut.setAllPixelsTo(
			outline.getBinaryValues().getOffInt()
		);
		pntsConvexHull.ifPresent(pnts->pnts.forEach( pnt->
			vbOut.setVoxel( pnt.getX(), pnt.getY(), 0, outline.getBinaryValues().getOnInt() )
		));
		return outline;
	}
}
