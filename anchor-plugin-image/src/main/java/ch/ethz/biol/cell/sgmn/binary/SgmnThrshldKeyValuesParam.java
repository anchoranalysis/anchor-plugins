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

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.params.keyvalue.KeyValueParamsProvider;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.core.params.KeyValueParams;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.bean.threshold.Thresholder;
import org.anchoranalysis.image.bean.threshold.ThresholderGlobal;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.plugin.image.bean.threshold.calculatelevel.Constant;

public class SgmnThrshldKeyValuesParam extends BinarySgmn {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START PARAMETERS
	@BeanField
	private KeyValueParamsProvider keyValueParamsProvider;
	
	@BeanField
	private String key;
	// END PARAMETERS
		
	private Thresholder createThresholder() throws SgmnFailedException {
		
		KeyValueParams keyValueParams;
		try {
			keyValueParams = keyValueParamsProvider.create();
		} catch (CreateException e) {
			throw new SgmnFailedException(e);
		}
		
		String value = keyValueParams.getProperty(key);
		if (value==null) {
			throw new SgmnFailedException( String.format("Cannot find key '%s'",key) );
		}
		
		int level = (int) Math.floor( Double.parseDouble(value) );
		
		ThresholderGlobal thresholder = new ThresholderGlobal();
		thresholder.setCalculateLevel( new Constant(level) );
		return thresholder;
	}
	
	
	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox, BinarySgmnParameters params, RandomNumberGenerator re) throws SgmnFailedException {
		
		BinaryValuesByte bvOut = BinaryValuesByte.getDefault();
		
		try {
			Thresholder thresholder = createThresholder();
			
			return thresholder.threshold(voxelBox, bvOut, null);
		} catch (OperationFailedException e) {
			throw new SgmnFailedException(e);
		}
	}
	
	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox, BinarySgmnParameters params, ObjMask objMask, RandomNumberGenerator re) throws SgmnFailedException {
		
		VoxelBox<ByteBuffer> voxelBoxByte = voxelBox.asByte();
		
		Thresholder thresholder = createThresholder();
		
		BoundingBox bboxE = new BoundingBox(objMask.getVoxelBox().extnt());
		
		// We just want to return the area under the objMask
		VoxelBox<ByteBuffer> maskDup = VoxelBoxFactory.instance().getByte().create( objMask.getVoxelBox().extnt() );
		voxelBoxByte.copyPixelsToCheckMask(
			objMask.getBoundingBox(),
			maskDup,
			bboxE,
			objMask.getVoxelBox(),
			objMask.getBinaryValuesByte()
		);
		
		BinaryValuesByte bvOut = BinaryValuesByte.getDefault();
		try {		
			return thresholder.threshold(new VoxelBoxWrapper(maskDup),new ObjMask(bboxE,objMask.getVoxelBox(),objMask.getBinaryValuesByte()), bvOut, params.getIntensityHistogram() );
		} catch (OperationFailedException e) {
			throw new SgmnFailedException(e);
		}
					
	}

	@Override
	public VoxelBox<ByteBuffer> getAdditionalOutput() {
		return null;
	}

	public KeyValueParamsProvider getKeyValueParamsProvider() {
		return keyValueParamsProvider;
	}

	public void setKeyValueParamsProvider(
			KeyValueParamsProvider keyValueParamsProvider) {
		this.keyValueParamsProvider = keyValueParamsProvider;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
