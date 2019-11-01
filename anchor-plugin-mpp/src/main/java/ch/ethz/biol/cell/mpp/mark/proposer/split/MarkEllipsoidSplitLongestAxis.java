package ch.ethz.biol.cell.mpp.mark.proposer.split;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkSplitProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipsoid;
import org.anchoranalysis.anchor.mpp.pair.PairPxlMarkMemo;
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
import org.anchoranalysis.math.rotation.RotationMatrix;
import org.anchoranalysis.math.rotation.RotationMatrix3DFromRadianCreator;

import anchor.provider.bean.ProposalAbnormalFailureException;
import ch.ethz.biol.cell.mpp.cfg.CfgGen;

public class MarkEllipsoidSplitLongestAxis extends MarkSplitProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2434034209101025377L;
	
	// START BEAN
	@BeanField
	private MarkProposer markProposer;
	// END BEAN

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkEllipsoid;
	}

	private static class OrientationRadius {
		

		private double radius;
		private RotationMatrix orientation;
		private int col;
		
		public double getRadius() {
			return radius;
		}
		public void setRadius(double radius) {
			this.radius = radius;
		}
		@SuppressWarnings("unused")
		public RotationMatrix getOrientation() {
			return orientation;
		}
		public void setOrientation(RotationMatrix orientation) {
			this.orientation = orientation;
		}
		
		public int getCol() {
			return col;
		}
		public void setCol(int col) {
			this.col = col;
		}
	}
	
	private OrientationRadius calcOrientationRadius( MarkEllipsoid markC, RotationMatrix matrix ) {
		
		OrientationRadius or = new OrientationRadius();
		
		Point3d rad = markC.getRadii();
		
		if (rad.getX() > rad.getY()) {
			if (rad.getX() > rad.getZ() ) {
				// X is longest
				
				or.setOrientation(matrix);
				or.setRadius(rad.getX());
				or.setCol(0);
			} else {
				// Z is longest
				
				// We rotate the matrix by the y-axis
				RotationMatrix rm = new RotationMatrix3DFromRadianCreator( 0, Math.PI/2, 0 ).createRotationMatrix();
				
				or.setOrientation( matrix.mult(rm) );
				or.setRadius(rad.getZ());
				or.setCol(2);
			}
		} else {
			if (rad.getY() > rad.getZ() ) {
				// Y is the longest
				
				// We rotate the matrix by the x-axis
				RotationMatrix rm = new RotationMatrix3DFromRadianCreator( Math.PI/2, 0, 0 ).createRotationMatrix();
				or.setOrientation( matrix.mult(rm) );
				or.setRadius(rad.getY());
				or.setCol(1);
				
			} else {
				// Z is the longest
				// We rotate the matrix by the y-axis
				RotationMatrix rm = new RotationMatrix3DFromRadianCreator( 0, Math.PI/2, 0 ).createRotationMatrix();
				
				or.setOrientation( matrix.mult(rm) );
				or.setRadius(rad.getZ());
				or.setCol(2);
			}
		}
		return or;
	}
	
	@Override
	public PairPxlMarkMemo propose(PxlMarkMemo mark, ProposerContext context, CfgGen cfgGen) throws ProposalAbnormalFailureException {
		
		MarkEllipsoid markC = (MarkEllipsoid) mark.getMark();
		
		// Find out which is the longest radius
		RotationMatrix matrix = markC.getOrientation().createRotationMatrix();
		
		OrientationRadius or = calcOrientationRadius(markC, matrix);
		
		// We pick a random point between the 0.25 + 0.75 parts of the radius
		double ratio = 0.25 + (context.getRe().nextDouble() * 0.5); 
		
		double d = or.getRadius() * ratio;
		
		Point3d marg = matrix.column( or.getCol() );
		double l2norm = Math.sqrt( Math.pow(marg.getX(), 2) +  Math.pow(marg.getY(), 2) + Math.pow(marg.getZ(), 2));
		marg.scale( 1/l2norm );
		
		marg.scale( d );
		
		Point3d point1 = new Point3d( markC.centerPoint());
		point1.add( marg );
		
		marg.scale(-1);
		
		Point3d point2 = new Point3d( markC.centerPoint());
		point2.add( marg );
		
		
		PxlMarkMemo memo1 = mark.duplicateFresh();
		memo1.getMark().setId( cfgGen.idAndIncrement() );
		
		MarkEllipsoid memoMark1 = (MarkEllipsoid) memo1.getMark();
		memoMark1.setPos(point1);

		try {
			if( !markProposer.propose(memo1, context) ) {
				context.getErrorNode().add("Cannot create mark1");
				return null;
			}
		} catch (ProposalAbnormalFailureException e) {
			throw new ProposalAbnormalFailureException("Failed to create mark1", e);
		}
		
		
		PxlMarkMemo memo2 = mark.duplicateFresh();
		memo2.getMark().setId( cfgGen.idAndIncrement() );

		MarkEllipsoid memoMark2 = (MarkEllipsoid) memo2.getMark();
		memoMark2.setPos(point2);
		

		try {
			if( !markProposer.propose(memo2, context) ) {
				context.getErrorNode().add("Cannot create mark2");
				return null;
			}
		} catch (ProposalAbnormalFailureException e) {
			throw new ProposalAbnormalFailureException("Failed to create mark2", e);
		}
		
		return new PairPxlMarkMemo(memo1,memo2);
	}

	public MarkProposer getMarkProposer() {
		return markProposer;
	}

	public void setMarkProposer(MarkProposer markProposer) {
		this.markProposer = markProposer;
	}
}
