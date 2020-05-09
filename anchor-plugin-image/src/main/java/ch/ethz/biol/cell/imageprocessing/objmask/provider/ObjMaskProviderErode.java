package ch.ethz.biol.cell.imageprocessing.objmask.provider;

import org.anchoranalysis.bean.BeanInstanceMap;

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
import org.anchoranalysis.bean.error.BeanMisconfiguredException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;
import org.anchoranalysis.image.objmask.morph.MorphologicalDilation;
import org.anchoranalysis.image.objmask.morph.MorphologicalErosion;
import org.anchoranalysis.image.objmask.morph.MorphologicalDilation.AcceptIterationList;

public class ObjMaskProviderErode extends ObjMaskProviderDimensionsOptional {
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objs;
	
	@BeanField
	private boolean do3D = false;
	
	@BeanField
	private int iterations = 1;
	
	@BeanField
	private boolean outsideAtThreshold = true;
	
	@BeanField
	private boolean rejectIterationIfAllLow = false;
	
	@BeanField
	private boolean rejectIterationifDisconnected = false;
	// END BEAN PROPERTIES



	@Override
	public void checkMisconfigured( BeanInstanceMap defaultInstances ) throws BeanMisconfiguredException {
		super.checkMisconfigured( defaultInstances );
		if (outsideAtThreshold==false && getDimProvider()==null) {
			throw new BeanMisconfiguredException("If outsideAtThreshold==false then dimProvider must be set");
		}
	}
	
	@Override
	public ObjMaskCollection create() throws CreateException {
		
		ObjMaskCollection objsIn = objs.create();
		
		ObjMaskCollection objsOut = new ObjMaskCollection();
		
		for( ObjMask om : objsIn ) {
			
			AcceptIterationList acceptConditionsDilation = new AcceptIterationList();
			if (rejectIterationIfAllLow) {
				acceptConditionsDilation.add( new MorphologicalDilation.RejectIterationIfAllHigh() );
			}
			if (rejectIterationifDisconnected) {
				acceptConditionsDilation.add( new MorphologicalDilation.RejectIterationIfLowDisconnected() );
			}
			
			ObjMask omOut = MorphologicalErosion.createErodedObjMask(
				om,
				calcExtent(),
				do3D,
				iterations,
				outsideAtThreshold,
				acceptConditionsDilation
			);
			
			objsOut.add(omOut);
		}
		return objsOut;
	}
	 
	private Extent calcExtent() throws CreateException {
		ImageDim dims = createDims();
		return dims!=null ? dims.getExtnt() : null;
	}

	public ObjMaskProvider getObjs() {
		return objs;
	}


	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
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


	public boolean isRejectIterationIfAllLow() {
		return rejectIterationIfAllLow;
	}


	public void setRejectIterationIfAllLow(boolean rejectIterationIfAllLow) {
		this.rejectIterationIfAllLow = rejectIterationIfAllLow;
	}


	public boolean isOutsideAtThreshold() {
		return outsideAtThreshold;
	}


	public void setOutsideAtThreshold(boolean outsideAtThreshold) {
		this.outsideAtThreshold = outsideAtThreshold;
	}

	public boolean isRejectIterationifDisconnected() {
		return rejectIterationifDisconnected;
	}

	public void setRejectIterationifDisconnected(boolean rejectIterationifDisconnected) {
		this.rejectIterationifDisconnected = rejectIterationifDisconnected;
	}
}
