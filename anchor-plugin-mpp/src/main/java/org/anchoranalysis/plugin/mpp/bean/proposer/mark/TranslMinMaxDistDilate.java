package org.anchoranalysis.plugin.mpp.bean.proposer.mark;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.OrientationProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.radii.RadiiProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.ICreateProposalVisualization;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;

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
import org.anchoranalysis.plugin.mpp.bean.mark.distance.MarkDistanceAbstractPosition;

public class TranslMinMaxDistDilate extends MarkProposer {

	// BEAN PARAMETERS
	@BeanField
	private DistTo transl = new DistTo();
	
	@BeanField
	private OrientationAndRadiiProposer radii = new OrientationAndRadiiProposer();
	// END BEAN PARAMETERS
		
	private MarkProposer delegate;
	
	public TranslMinMaxDistDilate() {
		OrderedList list = new OrderedList();
		
		transl.setDistance( new MarkDistanceAbstractPosition() );
		
		list.getList().add(transl);
		list.getList().add(radii);
		
		delegate = list;
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return delegate.isCompatibleWith(testMark);
	}

	@Override
	public boolean propose(PxlMarkMemo inputMark, ProposerContext context) throws ProposalAbnormalFailureException {
		return delegate.propose(inputMark, context);
	}
	
	public MarkProposer getPositionMarkProposer() {
		return transl.getProposer();
	}

	public void setPositionMarkProposer(MarkProposer positionMarkProposer) {
		transl.setProposer(positionMarkProposer);
	}

	public double getMinDist() {
		return transl.getMinDist();
	}

	public void setMinDist(double minDist) {
		transl.setMinDist(minDist);
	}

	public double getMaxDist() {
		return transl.getMaxDist();
	}

	public void setMaxDist(double maxDist) {
		transl.setMaxDist(maxDist);
	}

	public int getMaxNumTries() {
		return transl.getMaxNumTries();
	}

	public void setMaxNumTries(int maxNumTries) {
		transl.setMaxNumTries(maxNumTries);
	}

	public RadiiProposer getRadiiProposer() {
		return radii.getRadiiProposer();
	}

	public void setRadiiProposer(RadiiProposer radiiProposer) {
		radii.setRadiiProposer(radiiProposer);
	}

	public OrientationProposer getOrientationProposer() {
		return radii.getOrientationProposer();
	}

	public void setOrientationProposer(OrientationProposer orientationProposer) {
		radii.setOrientationProposer(orientationProposer);
	}

	@Override
	public Optional<ICreateProposalVisualization> proposalVisualization(boolean detailed) {
		return delegate.proposalVisualization(detailed);
	}
}
