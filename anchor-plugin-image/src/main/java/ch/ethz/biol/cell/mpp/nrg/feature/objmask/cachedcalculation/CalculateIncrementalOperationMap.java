package ch.ethz.biol.cell.mpp.nrg.feature.objmask.cachedcalculation;

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


import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculationMapHash;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.image.objmask.ObjMask;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public abstract class CalculateIncrementalOperationMap extends CachedCalculationMapHash<ObjMask,FeatureObjMaskParams,Integer> {

	private boolean do3D;
	
	public CalculateIncrementalOperationMap(boolean do3d) {
		super(100);
		this.do3D = do3d;
	}
	
	protected CalculateIncrementalOperationMap(CalculateIncrementalOperationMap other) {
		super(100);
		this.do3D = other.do3D;
	}
	

	@Override
	protected ObjMask execute(FeatureObjMaskParams params, Integer key)
			throws FeatureCalcException {
		Extent extnt = params.getNrgStack().getDimensions().getExtnt();

		if (key==0) {
			throw new FeatureCalcException("Key must be > 0");
		}
		
		int lowestExistingKey = findHighestExistingKey( key - 1 );
		
		
		ObjMask omIn = lowestExistingKey!=0 ? getOrNull(lowestExistingKey) : params.getObjMask();
		
		
		//System.out.printf("Foung existing key: %d. Loading %s \n", lowestExistingKey, omIn);
		
		try {
			for( int i=(lowestExistingKey+1); i<=key; i++ ) {
				ObjMask omNext = applyOperation( omIn, extnt, do3D );
				
				// save in cache, as long as it's not the final one, as this will save after the function executes
				if (i!=key) {
					//System.out.printf("Saving key: %d (%s)\n", i, omNext);
					this.put(i, omNext);
				}
				
				omIn = omNext;
			}
			
			//System.out.printf("Eroded by %d reduces %d to %d pixels\n", key, params.getObjMask().numPixels(), omIn.numPixels() );
			
			//System.out.printf("Finally calculated: %d (%s)\n", key, omIn);
			
			return omIn;
			
		} catch (OperationFailedException e) {
			throw new FeatureCalcException(e);
		}
	}
	
	/**
	 * Lowest existing key. 0 if no key exists.
	 * 
	 * @param max
	 * @return
	 */
	private int findHighestExistingKey( int max ) {
		for( int i=max; i>=1; i-- ) {
			if (this.hasKey(i)) {
				return i;
			}
		}
		return 0;
	}
	
	protected abstract ObjMask applyOperation( ObjMask om, Extent extnt, boolean do3D ) throws OperationFailedException;
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(do3D).toHashCode();
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof CalculateIncrementalOperationMap){
	        final CalculateIncrementalOperationMap other = (CalculateIncrementalOperationMap) obj;
	        return new EqualsBuilder()
	            .append(do3D, other.do3D)
	            .isEquals();
	    } else{
	        return false;
	    }
	}
}
