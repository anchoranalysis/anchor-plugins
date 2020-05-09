package ch.ethz.biol.cell.sgmn.binary;

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
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class SgmnSequence extends BinarySgmn {

	// START BEAN PROPERTIES
	@OptionalBean
	private List<BinarySgmn> listSgmn = new ArrayList<>();
	// END BEAN PROPERTIES
	
	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox, BinarySgmnParameters params) throws SgmnFailedException {
		return sgmn(voxelBox, params, null);
	}

	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox,
			BinarySgmnParameters params, ObjMask objMask) throws SgmnFailedException {
		
		BinaryVoxelBox<ByteBuffer> outOld = null;
		BoundingBox BoundingBox = objMask!=null ? objMask.getBoundingBox() : new BoundingBox( voxelBox.any().extnt() );
		
		for( BinarySgmn sgmn : listSgmn) {
			
			
			BinaryVoxelBox<ByteBuffer> outNew;
			if (objMask!=null) {
				outNew = sgmn.sgmn(voxelBox, params, objMask);
			} else {
				outNew = sgmn.sgmn(voxelBox, params);
			}
			
			if (outNew==null) {
				return outOld;
			}
			
			outOld = outNew;
			objMask = new ObjMask(BoundingBox, outNew);
		}
		
		return outOld;
	}

	public List<BinarySgmn> getListSgmn() {
		return listSgmn;
	}

	public void setListSgmn(List<BinarySgmn> listSgmn) {
		this.listSgmn = listSgmn;
	}

	@Override
	public VoxelBox<ByteBuffer> getAdditionalOutput() {
		return null;
	}
}
