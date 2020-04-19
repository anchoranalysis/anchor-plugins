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
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.feature.cachedcalculation.RslvdCachedCalculation;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.bean.provider.ObjMaskProvider;
import org.anchoranalysis.image.feature.objmask.pair.FeatureObjMaskPairParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.anchoranalysis.image.objmask.ObjMaskCollection;

import ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation.CalculatePairIntersectionCommutative;

/**
 * Accepts two objects. Finds their intersection (after optional dilation, and optional erosion at the end)
 * 
 * <p>If there is no intersection, then no object is returned</p>
 * 
 * See {@link CalculatePairIntersectionCommutative} 
 * 
 * @author Owen Feehan
 *
 */
public class ObjMaskProviderPairIntersection extends ObjMaskProviderDimensions {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ObjMaskProvider objs;
	
	/**
	 * Number of times each input-object is dilated before intersection
	 */
	@BeanField
	private int iterationsDilation;
	
	/**
	 * After intersectioon, the number of erosions that occur
	 */
	@BeanField
	private int iterationsErosion;
	
	/**
	 * Should dilation and erosion operations occur in 3D
	 */
	@BeanField
	private boolean do3D = false;
	// END BEAN PROPERTIES

	@Override
	public ObjMaskCollection create() throws CreateException {

		ObjMaskCollection objPair = objs.create();
		
		if (objPair.size()!=2) {
			throw new CreateException(
				String.format("objPair has %d items. It must have exactly 2.", objPair.size())	
			);
		}
		
		ObjMask om1 = objPair.get(0);
		ObjMask om2 = objPair.get(1);
		
		Optional<ObjMask> omIntersection = calcIntersection(om1,om2);
		if (omIntersection.isPresent()) {
			return new ObjMaskCollection(omIntersection.get());
		} else {
			return new ObjMaskCollection();
		}
	}
	
	private Optional<ObjMask> calcIntersection( ObjMask om1, ObjMask om2 ) throws CreateException {
		
		try {
			FeatureObjMaskPairParams params = createParams(om1, om2);
			
			RslvdCachedCalculation<Optional<ObjMask>,FeatureObjMaskPairParams> op = CalculatePairIntersectionCommutative
					.createWithoutCache(iterationsDilation, iterationsErosion, do3D);
			return op.getOrCalculate( params );
		} catch (ExecuteException e) {
			throw new CreateException(e.getCause());
		}
	}
	
	private FeatureObjMaskPairParams createParams( ObjMask om1, ObjMask om2 ) throws CreateException {
		FeatureObjMaskPairParams params = new FeatureObjMaskPairParams(om1, om2);
		params.setNrgStack(
			new NRGStackWithParams(
				createDims()
			)
		);
		return params;
	}

	public ObjMaskProvider getObjs() {
		return objs;
	}

	public void setObjs(ObjMaskProvider objs) {
		this.objs = objs;
	}

	public int getIterationsDilation() {
		return iterationsDilation;
	}

	public void setIterationsDilation(int iterationsDilation) {
		this.iterationsDilation = iterationsDilation;
	}

	public int getIterationsErosion() {
		return iterationsErosion;
	}

	public void setIterationsErosion(int iterationsErosion) {
		this.iterationsErosion = iterationsErosion;
	}

	public boolean isDo3D() {
		return do3D;
	}

	public void setDo3D(boolean do3d) {
		do3D = do3d;
	}
}
