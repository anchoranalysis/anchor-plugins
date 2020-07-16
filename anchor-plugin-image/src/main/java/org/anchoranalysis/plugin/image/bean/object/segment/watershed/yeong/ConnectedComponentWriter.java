package org.anchoranalysis.plugin.image.bean.object.segment.watershed.yeong;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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

import java.util.Optional;

import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.plugin.image.segment.watershed.encoding.EncodedVoxelBox;

/**
 * Writes all points in a particular connected-component using the same ID
 * 
 * @author Owen Feehan
 *
 */
final class ConnectedComponentWriter {

	private final EncodedVoxelBox matS;
	private final Optional<MinimaStore> minimaStore;

	/** Keeps track of the IDs used */
	private int id = -1;
	
	public ConnectedComponentWriter(EncodedVoxelBox matS, Optional<MinimaStore> minimaStore) {
		super();
		this.matS = matS;
		this.minimaStore = minimaStore;
	}
	
	/**
	 * 
	 * @param point a point that is treated immutably
	 */
	public void writePoint( Point3i point ) {
		// We write a connected component id based upon the first voxel encountered
		if (id==-1) {
			id = matS.extent().offset(point);
			
			if (minimaStore.isPresent()) {
				minimaStore.get().addDuplicated(point);
			}
		}
		
		matS.setPointConnectedComponentID(point, id);
	}
}
