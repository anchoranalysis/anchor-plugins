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

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;

public final class EncodedIntBuffer {

	private final VoxelBuffer<IntBuffer> delegate;
	private final WatershedEncoding encoding;

	public EncodedIntBuffer(final VoxelBuffer<IntBuffer> delegate, final WatershedEncoding encoding) {
		super();
		this.delegate = delegate;
		this.encoding = encoding;
	}
	
	public boolean isTemporary( int offset ) {
		return (delegate.buffer().get(offset)==WatershedEncoding.CODE_TEMPORARY);
	}
	
	public boolean isUnvisited( int offset ) {
		return (delegate.buffer().get(offset)==WatershedEncoding.CODE_UNVISITED);
	}
	
	public boolean isMinima( int offset ) {
		return (delegate.buffer().get(offset)==WatershedEncoding.CODE_MINIMA);
	}
	
	public boolean isConnectedComponentID( int offset ) {
		return( encoding.isConnectedComponentIDCode( delegate.buffer().get(offset)) );
	}
	
	public void markAsTemporary( int offset ) {
		delegate.buffer().put(offset,WatershedEncoding.CODE_TEMPORARY);
	}

	public int getCode(int index) {
		return delegate.buffer().get(index);
	}

	public IntBuffer putCode(int index, int code) {
		return delegate.buffer().put(index, code);
	}
	
	/** Convert code to connected-component */
	public void convertCode(int indxBuffer, int indxGlobal, EncodedVoxelBox matS, Point3i pnt) {
		int crntVal = getCode(indxBuffer);
		
		assert( !matS.isPlateau(crntVal) );
		assert( !matS.isUnvisited(crntVal) );
		assert( !matS.isTemporary(crntVal) );
		
		// We translate the value into directions and use that to determine where to
		//   travel to
		if ( matS.isMinima(crntVal) ) {

			putConnectedComponentID(indxBuffer, indxGlobal);
			
			// We maintain a mapping between each minimas indxGlobal and 
		} else if (matS.isConnectedComponentIDCode(crntVal)) {
			// NO CHANGE
		} else {
			int finalIndex = matS.calculateConnectedComponentID(pnt, crntVal);
			putCode(indxBuffer, finalIndex);
		}
	}
	
	private IntBuffer putConnectedComponentID(int index, int connectedComponentID) {
		int encoded = encoding.encodeConnectedComponentID(connectedComponentID);
		return delegate.buffer().put(index, encoded );
	}
}
