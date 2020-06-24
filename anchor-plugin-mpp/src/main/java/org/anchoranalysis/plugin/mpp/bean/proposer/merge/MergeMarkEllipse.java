package org.anchoranalysis.plugin.mpp.bean.proposer.merge;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkMergeProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.MarkConic;
import org.anchoranalysis.anchor.mpp.points.PointClipper;
import org.anchoranalysis.anchor.mpp.proposer.ProposalAbnormalFailureException;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
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
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.feature.nrg.NRGStackWithParams;
import org.anchoranalysis.image.orientation.Orientation2D;

public class MergeMarkEllipse extends MarkMergeProposer {

	// START BEAN
	@BeanField
	private MarkProposer markProposer = null;
	
	@BeanField
	private double mergedPosTolerance = 0.2;	// formerly 0.2
	// END BEAN
	
	@Override
	public Optional<Mark> propose(PxlMarkMemo mark1, PxlMarkMemo mark2, ProposerContext context) throws ProposalAbnormalFailureException {
		
		PxlMarkMemo targetPmm = mark1.duplicateFresh();
		MarkConic target = (MarkConic) targetPmm.getMark();
		
		// We calculate a position for the mark
		Point3d pos = generateMergedPos( mark1.getMark(), mark2.getMark(), mergedPosTolerance, context.getRandomNumberGenerator(), context.getNrgStack() );
		
		target.setMarksExplicit(pos );
		
		try {
			if (!markProposer.propose(targetPmm, context)) {
				return Optional.empty();
			}
		} catch (ProposalAbnormalFailureException e) {
			throw new ProposalAbnormalFailureException("Failed to propose mark", e);
		}
		
		return Optional.of(
			targetPmm.getMark()
		);
	}
	
	
	
	 private static Point3d generateMergedPos( Mark mark1, Mark mark2, double posTolerance, RandomNumberGenerator re, NRGStackWithParams nrgStack ) {
			
    	Point3d pntNew = new Point3d(mark1.centerPoint());
    	pntNew.subtract( mark2.centerPoint() );
    	
    	// Gives us half way between the marks
    	
    	double scaleAdd = (re.nextDouble() * 2 * posTolerance) - posTolerance;
    	
    	pntNew.scale( 0.5 + scaleAdd );
    	
    	pntNew.add( mark2.centerPoint() );
    	
    	// We add some randomness around this point, say 4 pixels
    	int q = 2;
    	double randX = (re.nextDouble() * q * 2) - q;
    	double randY = (re.nextDouble() * q * 2) - q;
    	double randZ = (re.nextDouble() * q * 2) - q;
    	
    	pntNew.add( new Point3d(randX,randY,randZ ) );
    	
    	return PointClipper.clip(pntNew, nrgStack.getDimensions());
	}
	 
	
	
	@SuppressWarnings("unused")
	private static Orientation2D generateOrientationBetweenPoints( Point3d pntA, Point3d pntB ) {

		// We get the relative vector between them
		Point3d rel = new Point3d( pntA );
		rel.subtract( pntB );
		
		// Calculation an angle from this vector
		return new Orientation2D( Math.atan2( rel.getY(), rel.getX() ) );
	}
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkConic && markProposer.isCompatibleWith(testMark);
	}

	public double getMergedPosTolerance() {
		return mergedPosTolerance;
	}

	public void setMergedPosTolerance(double mergedPosTolerance) {
		this.mergedPosTolerance = mergedPosTolerance;
	}

	public MarkProposer getMarkProposer() {
		return markProposer;
	}

	public void setMarkProposer(MarkProposer markProposer) {
		this.markProposer = markProposer;
	}
}
