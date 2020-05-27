package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

/*
 * #%L
 * anchor-plugin-image-feature
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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureInputSingleObj;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.voxel.kernel.ApplyKernel;
import org.anchoranalysis.image.voxel.kernel.outline.OutlineKernel3;

public class NumBorderVoxels extends FeatureObjMask {

	// START BEAN PROPERTIES
	@BeanField
	private boolean outsideAtThreshold = false;
	
	@BeanField
	private boolean ignoreAtThreshold = false;
	
	@BeanField
	private boolean do3D = false;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(SessionInput<FeatureInputSingleObj> input) throws FeatureCalcException {

		ObjMask om = input.get().getObjMask();
		return (double) numBorderPixels(om, ignoreAtThreshold, outsideAtThreshold, do3D);
	}
	
	public static int numBorderPixels( ObjMask om, boolean ignoreAtThreshold, boolean outsideAtThreshold, boolean do3D ) {
		OutlineKernel3 kernel = new OutlineKernel3(om.getBinaryValuesByte(), outsideAtThreshold, do3D, ignoreAtThreshold);
		return (int) ApplyKernel.applyForCount(kernel, om.getVoxelBox());
	}

	public boolean isOutsideAtThreshold() {
		return outsideAtThreshold;
	}

	public void setOutsideAtThreshold(boolean outsideAtThreshold) {
		this.outsideAtThreshold = outsideAtThreshold;
	}

	public boolean isDo3D() {
		return do3D;
	}

	public void setDo3D(boolean do3D) {
		do3D = do3D;
	}

	public boolean isIgnoreAtThreshold() {
		return ignoreAtThreshold;
	}

	public void setIgnoreAtThreshold(boolean ignoreAtThreshold) {
		this.ignoreAtThreshold = ignoreAtThreshold;
	}



}
