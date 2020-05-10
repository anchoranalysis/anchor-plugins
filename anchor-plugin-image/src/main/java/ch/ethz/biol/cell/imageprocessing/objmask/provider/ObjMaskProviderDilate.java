package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import java.util.Optional;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.NonNegative;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.morph.MorphologicalDilation;

public class ObjMaskProviderDilate extends ObjMaskProviderDimensions {
	
	// START BEAN PROPERTIES
	@BeanField
	private boolean do3D = false;
	
	@BeanField @NonNegative
	private int iterations = 1;
	
	@BeanField
	private boolean bigNghb = false;
	// END BEAN PROPERTIES

	@Override
	public ObjMaskCollection createFromObjs(ObjMaskCollection objsIn) throws CreateException {
	
		ImageDim dims = createDims();
				
		ObjMaskCollection objsOut = new ObjMaskCollection();
		
		for( ObjMask om : objsIn ) {

			ObjMask omGrown = MorphologicalDilation.createDilatedObjMask(
				om,
				Optional.of(
					dims.getExtnt()
				),
				do3D,
				iterations,
				bigNghb
			);
			assert( dims.contains( omGrown.getBoundingBox()) );
			objsOut.add(omGrown);
		}
		return objsOut;
	}
	
	public boolean isDo3D() {
		return do3D;
	}


	public void setDo3D(boolean do3d) {
		do3D = do3d;
	}


	public int getIterations() {
		return iterations;
	}


	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public boolean isBigNghb() {
		return bigNghb;
	}

	public void setBigNghb(boolean bigNghb) {
		this.bigNghb = bigNghb;
	}

}
