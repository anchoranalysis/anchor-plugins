package org.anchoranalysis.plugin.image.task.bean.slice;

/*-
 * #%L
 * anchor-plugin-image-task
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

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.extent.IncorrectImageSizeException;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

/**
 * Takes three RGB channels and projects them into a canvas of width/height in the form
 *   of a new RGB stack
 *   
 * @author FEEHANO
 *
 */
class ExtractProjectedStack {

	private int width;
	private int height;
	
	public ExtractProjectedStack(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}
	
	public Stack extractAndProjectStack( Chnl red, Chnl green, Chnl blue, int z ) throws IncorrectImageSizeException {
		Stack stack = new Stack();
		extractAndProjectChnl( red, z, stack );
		extractAndProjectChnl( green, z, stack );
		extractAndProjectChnl( blue, z, stack );
		return stack;
	}
	
	private void extractAndProjectChnl(	Chnl chnl,int z,	Stack stack	) throws IncorrectImageSizeException {
		Chnl chnlProjected = createProjectedChnl(
			chnl.extractSlice(z)
		);
		stack.addChnl(chnlProjected);
	}
	
	private Chnl createProjectedChnl( Chnl chnlIn ) {
		
		// Then the mode is off
		if (width==-1 || height==-1 || (chnlIn.getDimensions().getX()==width && chnlIn.getDimensions().getY()==height) ) {
			return chnlIn;
		} else {
			Extent eOut = new Extent(width,height,1);
			Point3i crnrPos = createTarget(chnlIn.getDimensions(), eOut );
			
			BoundingBox bboxToProject = boxToProject(crnrPos, chnlIn.getDimensions().getExtnt(), eOut );
						
			BoundingBox bboxSrc = bboxSrc( bboxToProject, chnlIn.getDimensions() );
			
			return copyPixels( bboxSrc, bboxToProject, chnlIn, eOut );
		}
	}
	
	private static Point3i createTarget( ImageDim sd, Extent e ) {
		Point3i crnrPos = new Point3i();
		
		crnrPos.setX( (e.getX() - sd.getX()) / 2);
		crnrPos.setY( (e.getY() - sd.getY()) / 2);
		crnrPos.setZ( 0 );
		return crnrPos;
	}
	
	private static BoundingBox boxToProject( Point3i crnrPos, Extent eChnl, Extent eTrgt ) {
		BoundingBox bboxToProject = new BoundingBox( crnrPos, eChnl );
		bboxToProject.intersect(new BoundingBox(eTrgt), true);
		return bboxToProject;
	}
		
	private static BoundingBox bboxSrc( BoundingBox bboxToProject, ImageDim sd ) {
		Point3i srcCrnrPos = createSrcCrnrPos(bboxToProject, sd);
		return new BoundingBox(srcCrnrPos, bboxToProject.extnt() );
	}
	
	private static Point3i createSrcCrnrPos( BoundingBox bboxToProject, ImageDim sd ) {
		Point3i srcCrnrPos = new Point3i(0,0,0);
		
		if (bboxToProject.extnt().getX() < sd.getX()) {
			srcCrnrPos.setX(
				(sd.getX() - bboxToProject.extnt().getX())/2
			);
		}
		
		if (bboxToProject.extnt().getY() < sd.getY()) {
			srcCrnrPos.setY(
				(sd.getY() - bboxToProject.extnt().getY())/2
			);
		}
		return srcCrnrPos;
	}
	
	private Chnl copyPixels( BoundingBox bboxSrc, BoundingBox bboxToProject, Chnl chnl, Extent eOut ) {
		
		Chnl chnlOut = ChnlFactory.instance().createEmptyInitialised(
			new ImageDim(
				eOut,
				chnl.getDimensions().getRes()
			),
			VoxelDataTypeUnsignedByte.instance
		);
		chnl.getVoxelBox().asByte().copyPixelsTo(bboxSrc, chnlOut.getVoxelBox().asByte(), bboxToProject);
		return chnlOut;		
	}
}
