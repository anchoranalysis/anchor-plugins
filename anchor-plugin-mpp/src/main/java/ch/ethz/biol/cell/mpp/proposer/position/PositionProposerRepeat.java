package ch.ethz.biol.cell.mpp.proposer.position;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.proposer.PositionProposerBean;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;

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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;

public class PositionProposerRepeat extends PositionProposerBean {

	// START BEAN PROPERTIES
	@BeanField
	private PositionProposerBean positionProposer;
	
	@BeanField
	private int maxIter;
	// END BEAN PROPERTIES
		
	@Override
	public Optional<Point3d> propose(ProposerContext context) {

		for (int i=0; i<maxIter; i++) {
			Optional<Point3d> pnt = positionProposer.propose(
				context.addErrorLevel(positionProposer.getBeanName())
			);
			
			if (pnt.isPresent()) {
				return pnt;
			}
		}
		
		return Optional.empty();
	}

	public PositionProposerBean getPositionProposer() {
		return positionProposer;
	}

	public void setPositionProposer(PositionProposerBean positionProposer) {
		this.positionProposer = positionProposer;
	}

	public int getMaxIter() {
		return maxIter;
	}

	public void setMaxIter(int maxIter) {
		this.maxIter = maxIter;
	}

}
