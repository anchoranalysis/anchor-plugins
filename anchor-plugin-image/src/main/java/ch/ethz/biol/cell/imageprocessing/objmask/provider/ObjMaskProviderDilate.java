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
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.object.ObjectCollection;
import org.anchoranalysis.image.object.ObjectMask;
import org.anchoranalysis.image.object.morph.MorphologicalDilation;

public class ObjMaskProviderDilate extends ObjMaskProviderDimensionsOptional {
	
	// START BEAN PROPERTIES
	@BeanField
	private boolean do3D = false;
	
	@BeanField @NonNegative
	private int iterations = 1;
	
	@BeanField
	private boolean bigNghb = false;
	// END BEAN PROPERTIES

	@Override
	public ObjectCollection createFromObjs(ObjectCollection objsIn) throws CreateException {
		Optional<ImageDimensions> dim = createDims();
		return objsIn.stream().map( om->dilate(om,dim) );
	}
	
	private ObjectMask dilate(ObjectMask om, Optional<ImageDimensions> dim) throws CreateException {
		ObjectMask omGrown = MorphologicalDilation.createDilatedObjMask(
			om,
			dim.map(ImageDimensions::getExtent),
			do3D,
			iterations,
			bigNghb
		);
		assert( !dim.isPresent() || dim.get().contains( omGrown.getBoundingBox()) );
		return omGrown;
	}
	
	public boolean isDo3D() {
		return do3D;
	}


	public void setDo3D(boolean do3D) {
		this.do3D = do3D;
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
