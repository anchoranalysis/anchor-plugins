package org.anchoranalysis.plugin.mpp.bean.proposer.split;

import java.util.Optional;

import org.anchoranalysis.anchor.mpp.bean.cfg.CfgGen;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.MarkSplitProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.mark.conic.MarkEllipse;
import org.anchoranalysis.anchor.mpp.pair.PairPxlMarkMemo;
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
import org.anchoranalysis.core.geometry.Point2d;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.random.RandomNumberGenerator;
import org.anchoranalysis.image.extent.ImageDimensions;
import org.anchoranalysis.image.orientation.Orientation2D;
import org.anchoranalysis.math.rotation.RotationMatrix;

import lombok.Getter;
import lombok.Setter;

public class MarkEllipseSimple extends MarkSplitProposer {
	
	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private double minRadScaleStart = 0.3;
	
	@BeanField @Getter @Setter
	private double minRadScaleEnd = 0.55;
	
	@BeanField @Getter @Setter
	private MarkProposer markProposer;
	// END BEAN PROPERTIES
	
	@Override
	public Optional<PairPxlMarkMemo> propose( PxlMarkMemo mark, ProposerContext context, CfgGen cfgGen ) throws ProposalAbnormalFailureException {
		
		MarkEllipse markCast = (MarkEllipse) mark.getMark();
		
		Orientation2D orientation = (Orientation2D) markCast.getOrientation();
		
		if (markCast.getRadii().getX() > markCast.getRadii().getY()) {
			// We do not correct the orientation
			return createMarksForOrientation(
				orientation,
				mark,
				context,
				cfgGen,
				false
			);
		} else {
			
			PxlMarkMemo markCorrected = correctEllipseSoXAxisLongest(mark);
			// We change the orientation
			return createMarksForOrientation(
				orientation,
				markCorrected,
				context,
				cfgGen,
				false
			);
		}
	}
	
	private PxlMarkMemo correctEllipseSoXAxisLongest( PxlMarkMemo pmmExst ) {
		
		MarkEllipse exst = (MarkEllipse) pmmExst.getMark();
		
		PxlMarkMemo pmmDup = pmmExst.duplicateFresh();
		MarkEllipse dup = (MarkEllipse) pmmDup.getMark();
		
		Orientation2D exstOrientation = (Orientation2D) exst.getOrientation();
		
		dup.setMarks( new Point2d( exst.getRadii().getY(), exst.getRadii().getX() ), new Orientation2D(exstOrientation.getAngleRadians() + (Math.PI/2)) );
		return pmmDup;
	}
	
	private Optional<PairPxlMarkMemo> createMarksForOrientation( Orientation2D orientation, PxlMarkMemo markExst, ProposerContext context, CfgGen cfgGen, boolean wigglePos ) throws ProposalAbnormalFailureException {
		
		MarkEllipse markExstCast = (MarkEllipse) markExst.getMark();
		
		Optional<Point3d[]> pntArr = createNewMarkPos(orientation, markExstCast, context.getRandomNumberGenerator(), context.getDimensions(), minRadScaleStart, minRadScaleEnd, wigglePos);
		
		if (!pntArr.isPresent()) {
			return Optional.empty();
		}
		
		Orientation2D exstOrientation = (Orientation2D) markExstCast.getOrientation();
		Orientation2D orientationRight = new Orientation2D( exstOrientation.getAngleRadians() + (Math.PI/2) );
		
		PxlMarkMemo markNew1 = extractMemo(
			markExst,
			pntArr.get(),
			0,
			orientationRight,
			context,
			cfgGen,
			"pos1"
		);
		
		PxlMarkMemo markNew2 = extractMemo(
			markExst,
			pntArr.get(),
			1,
			orientationRight,
			context,
			cfgGen,
			"pos2"
		);
		
		return Optional.of(
			new PairPxlMarkMemo(markNew1, markNew2)
		);
	}
	
	private PxlMarkMemo extractMemo(
		PxlMarkMemo markExst,
		Point3d[] pntArr,
		int dimensionIndex,
		Orientation2D orientationRight,
		ProposerContext context,
		CfgGen cfgGen,
		String positionDescription
	) throws ProposalAbnormalFailureException {
		try {
			PxlMarkMemo pmm = createMarkAtPos(
				markExst,
				pntArr[dimensionIndex],
				orientationRight,
				context
			);
			pmm.getMark().setId( cfgGen.idAndIncrement() );
			return pmm;
		} catch (ProposalAbnormalFailureException e) {
			throw new ProposalAbnormalFailureException("Failed to create mark at " + positionDescription);
		}
	}
	
	private PxlMarkMemo createMarkAtPos( PxlMarkMemo exst, Point3d pos, Orientation2D orientation, ProposerContext context ) throws ProposalAbnormalFailureException {
		
		PxlMarkMemo exstMark = exst.duplicateFresh();
		MarkEllipse mark = (MarkEllipse) exstMark.getMark();
		//mark.setPos(pos);
		
		// THE OLD WAY WAS TO SHIFT 90 from orientation (the existing orientation)
		mark.setMarksExplicit(pos, new Orientation2D(orientation.getAngleRadians() + (Math.PI/2)), mark.getRadii());
		exstMark.reset();
		
		markProposer.propose( exstMark, context );
		return exstMark;
	}
	
	
	public static Optional<Point3d[]> createNewMarkPos( Orientation2D orientation, MarkEllipse markExst, RandomNumberGenerator re, ImageDimensions sd, double minRadScaleStart, double minRadScaleEnd, boolean wigglePos ) {
		
		double interval = minRadScaleEnd-minRadScaleStart;
		
		double extent = markExst.getRadii().getX() * ((re.nextDouble() * interval) + minRadScaleStart); 
		
		RotationMatrix rotMat = orientation.createRotationMatrix();
		double[] pntArr1 = rotMat.calcRotatedPoint( new double[] { -1 * extent, 0 } );
		double[] pntArr2 = rotMat.calcRotatedPoint( new double[] { extent, 0 } );
		
		Point3d pnt1 = new Point3d( pntArr1[0] + markExst.getPos().getX(), pntArr1[1] + markExst.getPos().getY(), 0);
		Point3d pnt2 = new Point3d( pntArr2[0] + markExst.getPos().getX(), pntArr2[1] + markExst.getPos().getY(), 0);
		
		if (wigglePos) {
			// We add some randomness around this point, say 4 pixels
	    	int q = 2;
	    	{
		    	double randX = (re.nextDouble() * q * 2) - q;
		    	double randY = (re.nextDouble() * q * 2) - q;
		    	pnt1.add( new Point3d(randX,randY,0 ) );
	    	}
	    	{
		    	double randX = (re.nextDouble() * q * 2) - q;
		    	double randY = (re.nextDouble() * q * 2) - q;
		    	pnt2.add( new Point3d(randX,randY,0 ) );
	    	}	    	
		}
		return ifBothPointsInside(sd, pnt1, pnt2);
	}
	
	private static Optional<Point3d[]> ifBothPointsInside(ImageDimensions dim, Point3d pnt1, Point3d pnt2) {
		if (!dim.contains(pnt1)) {
			return Optional.empty();
		}
		
		if (!dim.contains(pnt2)) {
			return Optional.empty();
		}
		
		return Optional.of(
			new Point3d[]{pnt1, pnt2}
		);
	}

	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return testMark instanceof MarkEllipse;
	}
}
