package ch.ethz.biol.cell.sgmn.objmask.watershed.encoding;

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


import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.voxel.box.VoxelBox;

public final class EncodedVoxelBox {

	private final VoxelBox<IntBuffer> delegate;
	
	private final WatershedEncoding encoding;

	public EncodedVoxelBox(VoxelBox<IntBuffer> voxelBox) {
		super();
		this.encoding = new WatershedEncoding();
		this.delegate = voxelBox;
	}

	public VoxelBox<IntBuffer> getVoxelBox() {
		return delegate;
	}

	public Extent extnt() {
		return delegate.extnt();
	}
	
	// TODO optimize
	public void setPoint( Point3i pnt, int code ) {
		int offset = delegate.extnt().offset( pnt.getX(), pnt.getY() );
		IntBuffer bbS = delegate.getPixelsForPlane( pnt.getZ() ).buffer();
		bbS.put(offset, code );		
	}

	// TODO optimize	
	public void setPointConnectedComponentID( Point3i pnt, int connectedComponentID ) {
		
		int code = encoding.encodeConnectedComponentID(connectedComponentID);
		setPoint(pnt,code);
	}
	
	// TODO optimize	
	public void setPointDirection( Point3i pnt, int xChange, int yChange, int zChange ) {
		
		int code = encoding.encodeDirection(xChange, yChange, zChange);
		setPoint(pnt,code);
	}
	
	// Points a list of points, at the first point
	public void pointListAtFirst( List<Point3i> points ) {
		
		Point3i rootPoint = points.get(0);
		int rootPointOffsetEncoded = encoding.encodeConnectedComponentID( delegate.extnt().offset(rootPoint) );
		
		setPoint( rootPoint, rootPointOffsetEncoded );

		for (int i=1; i<points.size(); i++ ) {
			Point3i pnt = points.get(i);
			setPoint( pnt, rootPointOffsetEncoded );
		}
	}

	public WatershedEncoding getEncoding() {
		return encoding;
	}

	public EncodedIntBuffer getPixelsForPlane(int z) {
		return new EncodedIntBuffer( delegate.getPixelsForPlane(z).buffer(), encoding );
	}
	
	public boolean hasTemporary() {
		int volumeXY = extnt().getVolumeXY();
		
		for (int z=0; z<extnt().getZ(); z++) {
			EncodedIntBuffer bb = getPixelsForPlane(z);
			
			for (int i=0; i<volumeXY; i++) {
				if( bb.isTemporary(i) ) {
					return true;
				}
			}
		}
		return false;
	}
	
	public List<Point3i> getTemporary() {
		
		ArrayList<Point3i> listOut = new ArrayList<>();
		
		for (int z=0; z<extnt().getZ(); z++) {
			EncodedIntBuffer bb = getPixelsForPlane(z);
			
			int offset =0;
			for (int y=0; y<extnt().getY(); y++) {
				for (int x=0; x<extnt().getX(); x++) {

					if( bb.isTemporary(offset++) ) {
						listOut.add( new Point3i(x,y,z) );
					}
				}
			}
		}
		return listOut;
	}
	
	// This is a debug method
	public Set<Integer> setOfConnectedComponentIDs() {
		
		Set<Integer> setOut = new HashSet<>();
		
		for (int z=0; z<extnt().getZ(); z++) {
			EncodedIntBuffer bb = getPixelsForPlane(z);
			
			int offset =0;
			for (int y=0; y<extnt().getY(); y++) {
				for (int x=0; x<extnt().getX(); x++) {

					if( bb.isConnectedComponentID(offset)) {
						setOut.add( bb.getCode(offset) );
					}
					offset++;
					
				}
			}
		}
		
//		System.out.print("START ");
//		for( Integer id : setOut) {
//			System.out.print(id);
//			System.out.print(" ");
//		}
//		System.out.print(" END\n");
		
		return setOut;
	}
	
	// Returns the CODED final index (global)
	public int calculateConnectedComponentID(Point3i pnt, int firstChainCode ) {
		
		Extent e = delegate.extnt();
		
		int crntChainCode = firstChainCode;
		
		do {
			// Get local index from global index
			pnt = addDirectionFromChainCode(pnt, crntChainCode);
			
			assert( e.contains(pnt));
			// Replace with intelligence slices buffer?
			int nextVal = delegate.getPixelsForPlane(
				pnt.getZ()
			).buffer().get(
				e.offsetSlice(pnt)
			);
		
			assert(nextVal!=WatershedEncoding.CODE_UNVISITED);
			assert(nextVal!=WatershedEncoding.CODE_TEMPORARY);
			
			if (encoding.isConnectedComponentIDCode(nextVal)) {
				return nextVal;
			}
			
			if (nextVal==WatershedEncoding.CODE_MINIMA) {
				return encoding.encodeConnectedComponentID(
					e.offset(pnt)
				);
			}
			
			crntChainCode = nextVal;

		} while (true);
		
	}
	
	/**
	 * Adds a direction to a point from a chain-code, without altering the incoming point
	 * 
	 * @param pnt an input-point that is unchanged (immutable)
	 * @param chainCode a chain-code from which to decode a direction, which is added to pnt
	 * @return a new point which is the sum of pnt and the decoded-direction
	 */
	private Point3i addDirectionFromChainCode(Point3i pnt, int chainCode) {
		Point3i out = encoding.chainCodes(chainCode);
		out.add(pnt);
		return out;
	}
	
	public boolean isPlateau( int code ) {
		return code==WatershedEncoding.CODE_PLATEAU;
	}
	
	public boolean isMinima( int code ) {
		return code==WatershedEncoding.CODE_MINIMA;
	}
	
	public boolean isTemporary( int code ) {
		return code==WatershedEncoding.CODE_TEMPORARY;
	}
	
	public boolean isUnvisited( int code ) {
		return code==WatershedEncoding.CODE_UNVISITED;
	}

	public boolean isDirectionChainCode(int code) {
		return encoding.isDirectionChainCode(code);
	}

	public boolean isConnectedComponentIDCode(int code) {
		return encoding.isConnectedComponentIDCode(code);
	}
}
