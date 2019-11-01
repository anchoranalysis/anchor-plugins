package ch.ethz.biol.cell.mpp.mark.proposer;

/*
 * #%L
 * anchor-plugin-points
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


import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.anchor.mpp.bean.proposer.MarkProposer;
import org.anchoranalysis.anchor.mpp.bean.proposer.PointsProposer;
import org.anchoranalysis.anchor.mpp.mark.Mark;
import org.anchoranalysis.anchor.mpp.proposer.ProposerContext;
import org.anchoranalysis.anchor.mpp.proposer.visualization.ICreateProposalVisualization;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.geometry.Point3d;
import org.anchoranalysis.core.geometry.Point3f;
import org.anchoranalysis.core.geometry.Point3i;
import org.anchoranalysis.core.geometry.PointConverter;
import org.anchoranalysis.core.log.LogErrorReporter;

import ch.ethz.biol.cell.mpp.gui.videostats.internalframe.markredraw.ColoredCfg;
import ch.ethz.biol.cell.mpp.mark.pointsfitter.InsufficientPointsException;
import ch.ethz.biol.cell.mpp.mark.pointsfitter.PointsFitter;
import ch.ethz.biol.cell.mpp.mark.pointsfitter.PointsFitterException;
import ch.ethz.biol.cell.mpp.mark.pxlmark.memo.PxlMarkMemo;

public class MarkProposerPointsFitter extends MarkProposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3794711261087672093L;

	// START BEAN
	@BeanField
	private PointsProposer pointsProposer;
	
	@BeanField
	private PointsFitter pointsFitter;
	
	@BeanField
	private boolean reportFitterErrors = true;
	// END BEAN
	
	@SuppressWarnings("unused")
	private LogErrorReporter logErrorReporter;
	
	@Override
	public boolean isCompatibleWith(Mark testMark) {
		return pointsFitter.isCompatibleWith(testMark) && pointsProposer.isCompatibleWith(testMark);
	}

	@Override
	public boolean propose(PxlMarkMemo inputMark, ProposerContext context) {
		
		inputMark.reset();
		
		Point3d pnt = inputMark.getMark().centerPoint();
		
		try {
			List<Point3i> pnts = pointsProposer.propose(
				pnt,
				inputMark.getMark(),
				context.getDimensions(),
				context.getRe(),
				context.getErrorNode().add("pointsProposer")
			);
			
			if (pnts==null) {
				return false;	
			}
			
			// Now we create a list of point2d, and run the ellipse fitter on these
			ArrayList<Point3f> fitList = new ArrayList<>();
			for( Point3i p : pnts) {
				fitList.add(
					PointConverter.floatFromInt(p)
				);
			}

			pointsFitter.fit(fitList, inputMark.getMark(), context.getDimensions());
			
		} catch (PointsFitterException | InsufficientPointsException e) {
			
			if (reportFitterErrors) {
				// TEMPORARY FIX
				System.out.println(e.toString());
				//logErrorReporter.getErrorReporter().recordError(MarkProposerPointsFitter.class, e);
			}
			context.getErrorNode().add( e.toString() );
			return false;
		}
		return true;
	}
	
	@Override
	public ICreateProposalVisualization proposalVisualization(final boolean detailed) {
		return new ICreateProposalVisualization() {

			@Override
			public void addToCfg(ColoredCfg cfg) {

				ICreateProposalVisualization pv = pointsProposer.proposalVisualization(detailed); 
				if (pv!=null) {
					pv.addToCfg(cfg);
				}
			}
			
		};
	}
	
	public PointsProposer getPointsProposer() {
		return pointsProposer;
	}

	public void setPointsProposer(PointsProposer pointsProposer) {
		this.pointsProposer = pointsProposer;
	}

	public boolean isReportFitterErrors() {
		return reportFitterErrors;
	}

	public void setReportFitterErrors(boolean reportFitterErrors) {
		this.reportFitterErrors = reportFitterErrors;
	}

	public PointsFitter getPointsFitter() {
		return pointsFitter;
	}

	public void setPointsFitter(PointsFitter pointsFitter) {
		this.pointsFitter = pointsFitter;
	}
}
