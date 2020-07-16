package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

import java.util.Optional;
import java.util.Set;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.extent.ImageDimensions;

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

import lombok.Getter;
import lombok.Setter;


/**
 * Adds a new mark (a "birth") to create a proposal
 * 
 * @author Owen Feehan
 *
 * @param <T> proposal-type
 */
public abstract class KernelBirth<T> extends KernelPosNeg<T> {
	
	private Optional<Set<Mark>> setMarksNew = Optional.empty();
	
	// START BEAN PROPERTIES
	/** Total number of births */
	@BeanField @Getter @Setter
	private int repeats = 1;
	// END BEAN PROPERTIES

	@Override
	public Optional<T> makeProposal(Optional<T> exst, KernelCalcContext context ) throws KernelCalcNRGException {
		
		if (!exst.isPresent()) {
			return Optional.empty();
		}
		
		setMarksNew = proposeNewMarks(exst.get(), repeats, context);
		return OptionalUtilities.flatMap(
			setMarksNew,
			set -> calcForNewMark(exst.get(), set, context)
		);
	}
		
	protected abstract Optional<Set<Mark>> proposeNewMarks( T exst, int number, KernelCalcContext context );
	
	protected abstract Optional<T> calcForNewMark( T exst, Set<Mark> listMarksNew, KernelCalcContext context ) throws KernelCalcNRGException;
	
	@Override
	public double calcAccptProb(int exstSize, int propSize,
			double poissonIntensity, ImageDimensions dimensions, double densityRatio) {
		
        double num = getProbNeg() * dimensions.getVolume() * poissonIntensity;
        double dem = getProbPos() * propSize;
        
        assert(num>0);
        if (dem==0) {
        	return 1.0;
        }
		
        return Math.min( (densityRatio * num) / dem, 1.0);
	}
	
	
	@Override
	public String dscrLast() {
		return String.format(
			"birth_%d(%s)",
			repeats,
			idStr(setMarksNew.get())
		);
	}

	@Override
	public int[] changedMarkIDArray() {
		return setMarksNew.map(KernelBirth::idArr).orElse( new int[]{} );
	}

	protected Optional<Set<Mark>> getMarkNew() {
		return setMarksNew;
	}
	
	private static String idStr( Set<Mark> list ) {
		return String.join(
			", ",
			FunctionalList.mapToList(list, mark ->
				Integer.toString( mark.getId())
			)
		);
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
