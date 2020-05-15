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


import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.log.LogReporter;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;

import com.github.quickhull3d.Point3d;
import com.github.quickhull3d.QuickHull3D;

public class BinaryImgChnlProviderConvexHull3D extends ConvexHullBase {

	@Override
	protected BinaryChnl createFromChnl(BinaryChnl chnlIn, BinaryChnl outline) throws CreateException {
		LogReporter logger = getLogger().getLogReporter();
		List<Point3d> extPnts = pointsFromChnl(outline);
		
		Point3d[] pntArr = extPnts.toArray( new Point3d[]{} );
		
		QuickHull3D hull = new QuickHull3D();
		hull.build(pntArr);
		
		logger.log("Vertices:");
		Point3d[] vertices = hull.getVertices();
		for (int i = 0; i < vertices.length; i++) {
			Point3d pnt = vertices[i];
			logger.log(pnt.x + " " + pnt.y + " " + pnt.z);
		}

		logger.log("Faces:");
		int[][] faceIndices = hull.getFaces();
		for (int i = 0; i < faceIndices.length; i++) {
			for (int k = 0; k < faceIndices[i].length; k++) {
				logger.log(faceIndices[i][k] + " ");
		    }
			logger.log("");
		}
				
		// we write the vertices to the outline
		Chnl out = outline.getChnl();
		VoxelBox<ByteBuffer> vbOut = out.getVoxelBox().asByte();
				
		vbOut.setAllPixelsTo(outline.getBinaryValues().getOffInt());
		for (int i = 0; i < vertices.length; i++) {
			Point3d pnt = vertices[i];
			vbOut.setVoxel( (int) pnt.x, (int) pnt.y, (int) pnt.z, outline.getBinaryValues().getOnInt());
	    }		
		   
		return outline;
	}

	// We use it here as it uses the quickHull3D Point3d primitive
	private static List<Point3d> pointsFromChnl( BinaryChnl chnl ) throws CreateException {
		
		List<Point3d> listOut = new ArrayList<>();
		
		BinaryValuesByte bvb = chnl.getBinaryValues().createByte();
					
		Extent e = chnl.getVoxelBox().extnt();
		for( int z=0; z<e.getZ(); z++) {
			
			ByteBuffer bb = chnl.getVoxelBox().getPixelsForPlane(z).buffer();
			
			for( int y=0; y<e.getY(); y++) {
				for( int x=0; x<e.getX(); x++) {
					
					if (bb.get()==bvb.getOnByte()) {
						listOut.add( new Point3d(x,y,z) );
					}
					
				}
			}
		}
		
		return listOut;
	}

}
