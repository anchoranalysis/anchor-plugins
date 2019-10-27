package ch.ethz.biol.cell.sgmn.objmask;

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


import java.nio.ByteBuffer;
import java.util.Stack;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.bean.sgmn.objmask.ObjMaskSgmn;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.seed.SeedCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;

// NOT EFFICIENT
// Consider replacing with "Fast connected-componentlabeling", He et al.
public class ObjMaskSgmnFloodFillStack extends ObjMaskSgmn {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	
	// END BEAN PROPERTIES
	
	
	private static byte CURRENT_OBJ = (byte) 2; 
			
	//private BoundOutputManagerRouteErrors outputManager;
	
	private static final BinaryValuesByte bv = BinaryValuesByte.getDefault();
	
	@Override
	public ObjMaskCollection sgmn(Chnl chnl, SeedCollection seeds)
			throws SgmnFailedException {
		
		//outputManager.getWriterCheckIfAllowed().write("beforeFloodFill", new ChnlGenerator(chnl,"chnl") );
		
		ObjMaskCollection omc = new ObjMaskCollection();
		
		boolean do3D = chnl.getDimensions().getZ()>1;
		
		byte maskOn = bv.getOnByte();
		
		VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();
		
		// Loop through every voxel, and add to stack
		for (int z=0; z<chnl.getDimensions().getZ(); z++) {

			ByteBuffer in = vb.getPixelsForPlane(z).buffer();
			
			int i = 0;
			for (int y=0; y<chnl.getDimensions().getY(); y++) {
				for (int x=0; x<chnl.getDimensions().getX(); x++) {
					
					// We recurse over this object and mark all the pixels as 2
					//  and calculate a bounding box for the object
					if (in.get(i)==maskOn) {
						
						in.put(i, CURRENT_OBJ);
						
						BoundingBox bbox = new BoundingBox( new Point3i(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MAX_VALUE), 
							new Point3i(Integer.MIN_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE) );
						
						Stack<Point3i> stack = new Stack<>(); 
						bbox.add(x,y,z);
						
						stack.push( new Point3i( x, y, z ) );
						
						processStack( stack, bbox, vb, do3D );
						
						// Now we create an ObjMask from the bounding box
						ObjMask om = vb.equalMask(
							bbox,
							ByteConverter.unsignedByteToInt(CURRENT_OBJ)
						);
						vb.setPixelsCheckMask(om, 0);
						
						omc.add(om);
					}
					i++;
				}
			}
		}
		
		//outputManager.getWriterCheckIfAllowed().write("afterFloodFill", new ChnlGenerator(chnl,"chnl") );
		
		return omc;
	}
	
	
	@Override
	public ObjMaskCollection sgmn(Chnl chnl, ObjMask objMask,
			SeedCollection seeds) throws SgmnFailedException {
		throw new SgmnFailedException("Unsupported operation");
	}
	
	
	private boolean testPoint( Stack<Point3i> stack, BoundingBox bbox, Point3i pnt, int xChange, int yChange, int zChange, Extent extnt, ByteBuffer bb ) {
		
		int x1 = pnt.getX() + xChange;
		int y1 = pnt.getY() + yChange;
		int z1 = pnt.getZ() + zChange;
				
		int offset = extnt.offset( x1, y1 );
		if (bb.get(offset)==bv.getOnByte()) {
			bbox.add( x1,y1,z1 );
			bb.put(offset, CURRENT_OBJ);
			stack.push( new Point3i(x1, y1, z1) );
			return true;
		} else {
			return false;
		}
	}
	
	
	private void processStack( Stack<Point3i> stack, BoundingBox bbox, VoxelBox<ByteBuffer> voxelBox, boolean do3D ) {

		while( !stack.isEmpty() ) {
			Point3i pnt = stack.pop();

			int x = pnt.getX();
			int y = pnt.getY();
			int z = pnt.getZ();
			
			// Check neighbours
			ByteBuffer bb = voxelBox.getPixelsForPlane( pnt.getZ() ).buffer();

			Extent extnt = voxelBox.extnt();
			
			// x+i
			for (int i=1; (x+i)<=(extnt.getX()-1); i++) { 
				if (!testPoint(stack,bbox,pnt,i,0,0,extnt,bb)) {
					break;
				}
			}
			
			// x-i
			for (int i=1; (x-i)>=0; i++) {
				if (!testPoint(stack,bbox,pnt,-1*i,0,0,extnt,bb)) {
					break;
				}
			}
			
			// y+i
			for (int i=1; (y+i)<=(extnt.getY()-1); i++) {
				if (!testPoint(stack,bbox,pnt,0,i,0,extnt,bb)) {
					break;
				}
			}
			
			// y-i
			for (int i=1; (y-i)>=0; i++) {
				if (!testPoint(stack,bbox,pnt,0,-1*i,0,extnt,bb)) {
					break;
				}
			}
			
			if (do3D) {
				// As buffers take time to retrieve, we don't move alone in lines in the z-dimension

				if ( (z+1)<=(extnt.getZ()-1) ) {
					ByteBuffer bbAdj = voxelBox.getPixelsForPlane( z+1 ).buffer();	
					testPoint(stack,bbox,pnt,0,0,1,extnt,bbAdj);
				}
				
				if ( (z-1)>=0) {
					ByteBuffer bbAdj = voxelBox.getPixelsForPlane( z-1 ).buffer();	
					testPoint(stack,bbox,pnt,0,0,-1,extnt,bbAdj);
				}
			}
		}
	}

}
