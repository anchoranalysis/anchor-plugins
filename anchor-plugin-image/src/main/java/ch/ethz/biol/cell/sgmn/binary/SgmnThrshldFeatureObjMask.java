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
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.session.calculator.FeatureCalculatorSingle;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.bean.threshold.ThresholderGlobal;
import org.anchoranalysis.image.binary.values.BinaryValuesByte;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.extent.BoundingBox;
import org.anchoranalysis.image.feature.bean.evaluator.FeatureEvaluator;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.box.factory.VoxelBoxFactory;
import org.anchoranalysis.plugin.image.bean.histogram.threshold.Constant;

public class SgmnThrshldFeatureObjMask extends BinarySgmn {

	// START PARAMETERS
	@BeanField
	private FeatureEvaluator<FeatureInputSingleObj> featureEvaluator;
	
	@BeanField
	private ObjMaskProvider objs;
	// END PARAMETERS
	
	private double calculateLevel() throws SgmnFailedException {
		
		try {
			FeatureCalculatorSingle<FeatureInputSingleObj> session = featureEvaluator.createAndStartSession();
			
			ObjMaskCollection omc = objs.create();
			
			if (omc.size()==0) {
				throw new SgmnFailedException("objMaskProvider returned 0 objects. Exactly 1 required");
			} else if (omc.size()>1) {
				throw new SgmnFailedException("objMaskProvider returned more than 1 object. Exactly 1 required");
			}
		
			return session.calc(
				new FeatureInputSingleObj(omc.get(0))
			);
			
		} catch (FeatureCalcException e) {
			throw new SgmnFailedException(e);
		} catch (OperationFailedException e) {
			throw new SgmnFailedException(e);
		} catch (CreateException e) {
			throw new SgmnFailedException(e);
		}
	}
	
	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox, BinarySgmnParameters params) throws SgmnFailedException {
		
		BinaryValuesByte bvOut = BinaryValuesByte.getDefault();
		
		int level = (int) Math.round( calculateLevel() );
		
		ThresholderGlobal thresholder = new ThresholderGlobal();
		thresholder.setCalculateLevel( new Constant(level) );
		try {
			return thresholder.threshold(voxelBox, bvOut, null);
		} catch (OperationFailedException e) {
			throw new SgmnFailedException(e);
		}
	}
	
	@Override
	public BinaryVoxelBox<ByteBuffer> sgmn(VoxelBoxWrapper voxelBox, BinarySgmnParameters params, ObjMask objMask) throws SgmnFailedException {
		
		BoundingBox bboxE = new BoundingBox(objMask.getVoxelBox().extnt());
		
		// We just want to return the area under the objMask
		VoxelBox<ByteBuffer> maskDup = VoxelBoxFactory.instance().getByte().create( objMask.getVoxelBox().extnt() );
		voxelBox.asByte().copyPixelsToCheckMask(
			objMask.getBoundingBox(),
			maskDup,
			bboxE,
			objMask.getVoxelBox(),
			objMask.getBinaryValuesByte()
		);
		
		
		int level = (int) Math.round( calculateLevel() );
		
		ThresholderGlobal thresholder = new ThresholderGlobal();
		thresholder.setCalculateLevel( new Constant(level) );
		
		
		BinaryValuesByte bvOut = BinaryValuesByte.getDefault();
		try {		
			return thresholder.threshold( new VoxelBoxWrapper(maskDup),new ObjMask(bboxE,objMask.getVoxelBox(),objMask.getBinaryValuesByte()), bvOut, params.getIntensityHistogram() );
		}catch (OperationFailedException e) {
			throw new SgmnFailedException(e);
		}
					
	}

	@Override
	public VoxelBox<ByteBuffer> getAdditionalOutput() {
		return null;
	}

	public FeatureEvaluator<FeatureInputSingleObj> getFeatureEvaluator() {
		return featureEvaluator;
	}

	public void setFeatureEvaluator(FeatureEvaluator<FeatureInputSingleObj> featureEvaluator) {
		this.featureEvaluator = featureEvaluator;
	}

	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}
}
