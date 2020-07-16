package org.anchoranalysis.plugin.mpp.sgmn.cfg.bean.kernel.independent;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.cfg.Cfg;
import org.anchoranalysis.anchor.mpp.mark.Mark;

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

import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.mpp.sgmn.bean.kernel.KernelPosNeg;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcContext;
import org.anchoranalysis.mpp.sgmn.kernel.KernelCalcNRGException;

public abstract class KernelDeath<T> extends KernelPosNeg<T> {
	
	private Optional<Mark> markRmv = Optional.empty();
	
	@Override
	public Optional<T> makeProposal(Optional<T> exst,KernelCalcContext context ) throws KernelCalcNRGException {

		if (!exst.isPresent()) {
			return Optional.empty();
		}
		
		Optional<MarkAnd<Mark,T>> markNrg = removeAndUpdateNrg( exst.get(), context.proposer() );
		markRmv = markNrg.map(MarkAnd::getMark);
		return markNrg.map(MarkAnd::getCfgNrg);
	}
	
	protected abstract Optional<MarkAnd<Mark,T>> removeAndUpdateNrg( T exst, ProposerContext context ) throws KernelCalcNRGException;
	
	@Override
	public double calcAccptProb(int exstSize, int propSize,
			double poissonIntens, ImageDimensions dimensions, double densityRatio) {

		if (exstSize<=1) {
			return Math.min(1.0, densityRatio);
		}
		
		// Birth prob
		double num = getProbNeg() * exstSize;
		
		// Death prob
        double dem = getProbPos() * dimensions.getVolume() * poissonIntens;
         
        return Math.min(1.0, densityRatio * num / dem );
	}
	
	
	@Override
	public String dscrLast() {
		if (markRmv.isPresent()) {
			return String.format("death(%d)", markRmv.get().getId() );
		} else {
			return "death";
		}
	}
	
	@Override
	public int[] changedMarkIDArray() {
		if (markRmv.isPresent()) {
			return new int[]{ this.markRmv.get().getId() };	
		} else {
			return new int[] {};
		}
		
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return true;
	}
	
	protected Optional<Mark> getMarkRmv() {
		return markRmv;
	}
	
	protected static int selectIndexToRmv( Cfg exst, ProposerContext propContext ) {
		
		if (exst.size()==0) {
			propContext.getErrorNode().add("configuration size is 0");
			return -1;
		}
				
		// Random mark
		return exst.randomIndex( propContext.getRandomNumberGenerator() );
	}
	
	protected static class MarkAnd<S,T> {
		
		private S mark;
		private T cfgNrg;
		
		public MarkAnd(S mark, T cfgNrg) {
			super();
			this.mark = mark;
			this.cfgNrg = cfgNrg;
		}

		public S getMark() {
			return mark;
		}

		public T getCfgNrg() {
			return cfgNrg;
		}
		
	}
}
	
