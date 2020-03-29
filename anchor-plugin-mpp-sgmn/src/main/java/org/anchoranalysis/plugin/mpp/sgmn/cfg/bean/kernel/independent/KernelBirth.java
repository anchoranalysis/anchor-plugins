package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

import java.util.Set;

import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.image.extent.ImageDim;

/*
 * #%L
 * anchor-plugin-mpp
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



import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

public abstract class KernelBirth<T> extends KernelPosNeg<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3031519899206383216L;
	
	private Set<Mark> setMarksNew;
	
	// START BEAN PROPERTIES
	/** Total number of births */
	@BeanField
	private int repeats = 1;
	// END BEAN PROPERTIES
	
	public KernelBirth() {
	}
	
	@Override
	public T makeProposal(T exst, KernelCalcContext context ) throws KernelCalcNRGException {
		
		setMarksNew = proposeNewMarks(exst, repeats, context);
		
		if (setMarksNew==null) {
			return null;
		}
		
		return calcForNewMark(exst, setMarksNew, context);
	}
		
	protected abstract Set<Mark> proposeNewMarks( T exst, int number, KernelCalcContext context );
	
	protected abstract T calcForNewMark( T exst, Set<Mark> listMarksNew, KernelCalcContext context ) throws KernelCalcNRGException;
	
	@Override
	public double calcAccptProb(int exstSize, int propSize,
			double poisson_intens, ImageDim scene_size, double densityRatio) {
		
        double num = getProbNeg() * scene_size.getVolume() * poisson_intens;
        double dem = getProbPos() * propSize;
        
        assert(num>0);
        if (dem==0) {
        	return 1.0;
        }
		
        return Math.min( (densityRatio * num) / dem, 1.0);
	}
	
	
	@Override
	public String dscrLast() {
		return String.format("birth_%d(%s)", repeats, idStr(setMarksNew) );
	}

	@Override
	public int[] changedMarkIDArray() {
		return idArr(setMarksNew);
	}

	protected Set<Mark> getMarkNew() {
		return setMarksNew;
	}
	
	public int getRepeats() {
		return repeats;
	}

	public void setRepeats(int repeats) {
		this.repeats = repeats;
	}
	
	private static String idStr( Set<Mark> list ) {
		StringBuilder sb = new StringBuilder();
		
		boolean first = true;
		for( Mark m : list) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append( m.getId() );
		}
		
		return sb.toString();
	}
	
	private static int[] idArr( Set<Mark> set ) {
		int[] arr = new int[set.size()];
		int i = 0;
		for( Mark m : set) {
			arr[i++] = m.getId();
		}
		return arr;
	}
}
