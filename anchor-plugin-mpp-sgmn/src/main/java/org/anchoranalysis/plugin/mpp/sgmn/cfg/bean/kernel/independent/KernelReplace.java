package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.feature.mark.ListUpdatableMarkSetCollection;
import org.anchoranalysis.anchor.mpp.mark.set.UpdateMarkSetException;

/*-
 * #%L
 * anchor-plugin-mpp-sgmn
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.mpp.sgmn.bean.kernel.Kernel;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;
import org.apache.commons.lang.ArrayUtils;

public abstract class KernelReplace<T> extends KernelPosNeg<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private KernelBirth<T> kernelBirth;
	private KernelDeath<T> kernelDeath;
	
	private Optional<T> afterDeathProp;
	
	// START BEAN PROPERTIES
	@BeanField
	private int birthRepeats = 1;
	// END BEAN PROPERTIES
	
	private boolean hasBeenInit = false;
	
	public KernelReplace() {
		super();
	}

	/** Must be called before makeProposal */
	protected void init(KernelBirth<T> kernelBirth, KernelDeath<T> kernelDeath) {
		this.kernelBirth = kernelBirth;
		this.kernelDeath = kernelDeath;
		hasBeenInit = true;
	}

	
	@Override
	public Optional<T> makeProposal(Optional<T> exst, KernelCalcContext context) throws KernelCalcNRGException {

		if (exst.isPresent()) {
			return Optional.empty();
		}
		
		afterDeathProp = kernelDeath.makeProposal(exst, context);
		return OptionalUtilities.flatMap(
			afterDeathProp,
			prop -> kernelBirth.makeProposal(
				Optional.of(prop),
				context
			)
		);
	}

	@Override
	public double calcAccptProb(int exstSize, int propSize, double poissonIntens, ImageDim sceneSize,
			double densityRatio) {
		return Math.min( densityRatio, 1.0 );
	}

	@Override
	public String dscrLast() {
		return String.format("replace_%d(%s->%s)", birthRepeats, changedID(kernelDeath), changedID(kernelBirth)  );
	}

	@Override
	public void updateAfterAccpt(ListUpdatableMarkSetCollection updatableMarkSetCollection, T nrgExst, T nrgNew)
			throws UpdateMarkSetException {

		OptionalUtilities.ifPresent(
			afterDeathProp,
			prop -> {
				kernelDeath.updateAfterAccpt(updatableMarkSetCollection, nrgExst, prop);
				kernelBirth.updateAfterAccpt(updatableMarkSetCollection, prop, nrgNew);
			}
		);
	}

	@Override
	public int[] changedMarkIDArray() {
		return makeArray( kernelBirth.changedMarkIDArray(), kernelDeath.changedMarkIDArray() );
	}
	
	public int getBirthRepeats() {
		return birthRepeats;
	}

	public void setBirthRepeats(int birthRepeats) {
		this.birthRepeats = birthRepeats;
	}
	
	private static <S> String changedID( Kernel<S> kernel ) {
		int[] arr = kernel.changedMarkIDArray();
		
		StringBuilder sb = new StringBuilder();
		for( int i=0; i<arr.length; i++ ) {
			if (i!=0) {
				sb.append(", ");
			}
			sb.append( arr[i] );
		}
		return sb.toString();
	}
	
	private static <S> int[] makeArray( int[] first, int[] second ) {
		
		if (first!=null) {
			
			if(second!=null) {
				return ArrayUtils.addAll(first,second);
			} else {
				return first;
			}
		
		} else {
			
			if(second!=null) {
				return second;
			} else {
				return new int[] {};
			}
		}
	}

	public boolean hasBeenInit() {
		return hasBeenInit;
	}
	
}
