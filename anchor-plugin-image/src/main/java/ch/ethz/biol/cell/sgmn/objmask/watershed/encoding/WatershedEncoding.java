package ch.ethz.biol.cell.sgmn.objmask.watershed.encoding;

import org.anchoranalysis.core.geometry.Point3i;

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


// Encodes 3 things in an integer range
//   * A number of constants
//   * 27 Directions representing [-1,0,-1]X[-1,0,-1]X[-1,0,-1]
//   * A sequence of connected components IDs starting at 0
public class WatershedEncoding implements IEncodeDirection {

	private ChainCodesDirection chainCodes = new ChainCodesDirection();
	
	public static final int CODE_UNVISITED = 0;
	public static final int CODE_TEMPORARY = 1;
	public static final int CODE_MINIMA = 2;
	public static final int CODE_PLATEAU = 3;
	
	private static final int START_CHAIN_CODE_RANGE = CODE_PLATEAU + 1;
	private static final int END_CHAIN_CODE_RANGE = START_CHAIN_CODE_RANGE + ChainCodesDirection.MAX_VALUE;
	
	@Override
	public int encodeDirection( int x, int y, int z ) {
		return chainCodes.chainCode(x, y, z) + START_CHAIN_CODE_RANGE;
	}
	
	/**
	 * Decodes a chain-code into a point
	 * 
	 * TODO is it a good idea to cache the creation of chain codes, to avoid work on the heap? There is a finite number.
	 * 
	 * @param chainCode the chain-code
	 * @return a new point (always newly created) for the given chain-code.
	 */
	public Point3i chainCodes(int chainCode) {
		int chainCodeSubbed = chainCode - START_CHAIN_CODE_RANGE;
		return new Point3i(
			chainCodes.xFromChainCode(chainCodeSubbed),
			chainCodes.yFromChainCode(chainCodeSubbed),
			chainCodes.zFromChainCode(chainCodeSubbed)
		);
	}
	
	public int encodeConnectedComponentID( int connected_component_id ) {
		return connected_component_id + END_CHAIN_CODE_RANGE; 
	}
	
	public boolean isDirectionChainCode( int code ) {
		return code >= START_CHAIN_CODE_RANGE && code < END_CHAIN_CODE_RANGE;
	}
	
	public boolean isConnectedComponentIDCode( int code ) {
		return code >= END_CHAIN_CODE_RANGE;
	}
	
	public int decodeConnectedComponentID( int code ) {
		return code - END_CHAIN_CODE_RANGE;
	}
}
